package at.ac.sbc.carfactory.xvms.util;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.Matchmaker;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.util.CarFactoryException;

public class SpaceUtil {

	private URI spaceURI;
	private Capi capi;
	private MzsCore core;

	public SpaceUtil() throws CarFactoryException {
		// TODO: Singleton?
		try {
			this.spaceURI = new URI(ConfigSettings.spaceProtocol + "://"
					+ ConfigSettings.spaceURL + ":" + ConfigSettings.spacePort);
		} catch (URISyntaxException e) {
			throw new CarFactoryException("URI-Syntax error: " + e.getMessage());
		}
		this.init();
		this.initContainers();
	}

	/*
	 * connect to space
	 */
	private void init() {
		this.core = DefaultMzsCore.newInstance();
		this.capi = new Capi(core);
	}

	public MzsCore getMozartSpaceCore() {
		return this.core;
	}

	public void initContainers() throws CarFactoryException {
		if (this.lookupContainer(ConfigSettings.containerCarPartsName) == null) {
			this.createContainer(ConfigSettings.containerCarPartsName,
					CoordinatorType.LABEL, CoordinatorType.QUERY);
		}
		if (this.lookupContainer(ConfigSettings.containerFinishedCarsName) == null) {
			this.createContainer(ConfigSettings.containerFinishedCarsName,
					CoordinatorType.FIFO);
		}
	}

	public ContainerReference createContainer(String name,
			CoordinatorType... coordTypes) throws CarFactoryException {
		// create a container
		List<Coordinator> coords = new ArrayList<Coordinator>();
		for (int i = 0; i < coordTypes.length; i++) {
			switch (coordTypes[i]) {
			case LABEL:
				coords.add(new LabelCoordinator());
				break;
			case FIFO:
				coords.add(new FifoCoordinator());
				break;
			case QUERY:
				coords.add(new QueryCoordinator());
				break;
			default:
				// DO NOTHING
			}
		}
		for (Coordinator coordNew : coords) {
			System.out.println(coordNew);
		}
		ContainerReference container = null;
		try {
			container = capi.createContainer(name, this.spaceURI,
					Container.UNBOUNDED, coords, null, null);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not create container " + name
					+ " at " + this.spaceURI + "\n" + e.getMessage());
		}
		return container;
	}

	public ContainerReference lookupContainer(String name)
			throws CarFactoryException {
		// lookup a container
		ContainerReference container = null;
		try {
			container = capi.lookupContainer(name, this.spaceURI,
					Container.UNBOUNDED, null, null, null);
		} catch (MzsCoreException e) {
			// throw new CarFactoryException("Could not find container " + name
			// + " at " + this.spaceURI + "\n" + e.getMessage());
			return null;
		}
		return container;
	}

	public void writeEntry(ContainerReference cRef, Serializable obj,
			TransactionReference tx, List<CoordinatorType> coordTypes,
			List<Matchmaker> makers, WorkTaskLabel label)
			throws CarFactoryException {
		List<CoordinationData> coordinator = new ArrayList<CoordinationData>();
		for (int i = 0; i < coordTypes.size(); i++) {
			switch (coordTypes.get(i)) {
			case LABEL:
				if (label != null) {
					coordinator.add(LabelCoordinator.newCoordinationData(label
							.toString()));
				} else {
					throw new CarFactoryException(
							"No label for label-Coordinator given");
				}
				break;
			case FIFO:
				coordinator.add(FifoCoordinator.newCoordinationData());
				break;
			case QUERY:
				coordinator.add(QueryCoordinator.newCoordinationData());
				break;
			default:
				// DO NOTHING
			}
		}

		try {
			capi.write(cRef, 0, null, new Entry(obj, coordinator));
		} catch (MzsCoreException e) {
			e.printStackTrace();
			throw new CarFactoryException("Could not write to container "
					+ cRef.getStringRepresentation() + "\n" + e.getMessage());
		}
	}

	public void writeLabelEntry(ContainerReference cRef, Serializable obj,
			WorkTaskLabel label) throws CarFactoryException {
		try {
			capi.write(
					cRef,
					0,
					null,
					new Entry(obj, LabelCoordinator.newCoordinationData(label
							.toString())));
		} catch (MzsCoreException e) {
			e.printStackTrace();
			throw new CarFactoryException("Could not write to container "
					+ cRef.getStringRepresentation() + "\n" + e.getMessage());
		}
	}

	public void writeFinalCar(ContainerReference cRef, Car car,
			TransactionReference tx) throws CarFactoryException {
		try {
			capi.write(cRef, 0, tx,
					new Entry(car, FifoCoordinator.newCoordinationData()));
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not write to container "
					+ cRef.getStringRepresentation() + "\n" + e.getMessage());
		}
	}

	public ArrayList<Serializable> readTest() throws CarFactoryException {

		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(LabelCoordinator.newSelector(
				WorkTaskLabel.CAR_BODY.toString(), 1));

		try {
			System.out.println(selectors);
			return capi.take(
					this.lookupContainer(ConfigSettings.containerCarPartsName),
					selectors, RequestTimeout.INFINITE, null);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not write to container "
					+ ConfigSettings.containerCarPartsName + "\n"
					+ e.getMessage());
		}
	}
	
	public ArrayList<Serializable> readQueryEntry(ContainerReference cRef, Query query, TransactionReference tx, boolean once) throws CarFactoryException {
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(QueryCoordinator.newSelector(query));
		try {
			long timeout = 0;
			if (once == false) {
				timeout = RequestTimeout.INFINITE;
			} else {
				timeout = RequestTimeout.TRY_ONCE;
			}
			return capi.take(cRef, selectors, timeout, tx);
		} catch (MzsCoreException e) {
			 System.out.println("Could not read from container " +
			 cRef.getStringRepresentation() + "\n" + e.getMessage());
			return null;
			// throw new CarFactoryException("Could not read from container " +
			// cRef.getStringRepresentation() + "\n"
			// + e.getMessage());
		}
	}

	public ArrayList<Serializable> readEntry(ContainerReference cRef,
			CoordinatorType type, List<WorkTaskLabel> labels, int count,
			TransactionReference tx, boolean once) throws CarFactoryException {

		List<Selector> selectors = new ArrayList<Selector>();
		switch (type) {
		case LABEL:
			for (WorkTaskLabel label : labels) {
				selectors.add(LabelCoordinator.newSelector(label.toString(),
						count));
			}
			break;
		case FIFO:
			selectors.add(FifoCoordinator.newSelector());
			break;
		default:
			selectors = null;
			break;
		}
		try {
			/*
			 * DEBUG if (selectors != null) { for (int i = 0; i <
			 * selectors.size(); i++) { System.out.println("We got selector: " +
			 * selectors.get(i).getName()); } }
			 */
			long timeout = 0;
			if (once == false) {
				timeout = RequestTimeout.INFINITE;
			} else {
				timeout = RequestTimeout.TRY_ONCE;
			}
			return capi.take(cRef, selectors, timeout, tx);
		} catch (MzsCoreException e) {
			// System.out.println("Could not read from container " +
			// cRef.getStringRepresentation() + "\n" + e.getMessage());
			return null;
			// throw new CarFactoryException("Could not read from container " +
			// cRef.getStringRepresentation() + "\n"
			// + e.getMessage());
		}
	}

	public TransactionReference createTransaction() throws CarFactoryException {
		try {
			return capi.createTransaction(
					MzsConstants.TransactionTimeout.INFINITE, this.spaceURI);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not create transaction\n"
					+ e.getMessage());
		}
	}

	public void commitTransaction(TransactionReference tx)
			throws CarFactoryException {
		try {
			capi.commitTransaction(tx);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not commit transaction\n"
					+ e.getMessage());
		}
	}

	public void rollbackTransaction(TransactionReference tx)
			throws CarFactoryException {
		try {
			capi.rollbackTransaction(tx);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not rollback transaction\n"
					+ e.getMessage());
		}
	}

	public void disconnect() {
		this.core.shutdown(true);
		System.out.println("DONE SHUTDOWN");
		System.out.println(this.core);
	}

	/*
	 * public void writeCarPartEntry(ContainerReference lookupContainer, Car
	 * car, WorkTaskLabel car2) { // TODO Auto-generated method stub
	 * 
	 * }
	 */
}
