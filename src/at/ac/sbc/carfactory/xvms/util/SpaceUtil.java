package at.ac.sbc.carfactory.xvms.util;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
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
import at.ac.sbc.carfactory.domain.CarPart;
import at.ac.sbc.carfactory.util.CarFactoryException;

public class SpaceUtil {

	private URI spaceURI;
	private Capi capi;
	private MzsCore core;

	public SpaceUtil() throws CarFactoryException {
		// TODO: Singleton?
		try {
			this.spaceURI = new URI(ConfigSettings.spaceProtocol + "://" + ConfigSettings.spaceURL + ":"
							+ ConfigSettings.spacePort);
		} catch (URISyntaxException e) {
			throw new CarFactoryException("URI-Syntax error: " + e.getMessage());
		}
		this.init();
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

	public ContainerReference createContainer(String name, CoordinatorType coord) throws CarFactoryException {
		// create a container
		List<Coordinator> coords = new ArrayList<Coordinator>();
		switch (coord) {
		case LABEL:
			coords.add(new LabelCoordinator());
			break;
		case FIFO:
			coords.add(new FifoCoordinator());
			break;
		default:
			// DO NOTHING
		}
		for(Coordinator coordNew : coords) {
			System.out.println(coordNew);
		}
		ContainerReference container = null;
		try {
			container = capi.createContainer(name, this.spaceURI, Container.UNBOUNDED, coords, null, null);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not create container " + name + " at " + this.spaceURI + "\n"
							+ e.getMessage());
		}
		return container;
	}

	public ContainerReference lookupContainer(String name) throws CarFactoryException {
		// lookup a container
		ContainerReference container = null;
		try {
			container = capi.lookupContainer(name, this.spaceURI, Container.UNBOUNDED, null, null, null);
		} catch (MzsCoreException e) {
			// throw new CarFactoryException("Could not find container " + name
			// + " at " + this.spaceURI + "\n" + e.getMessage());
			return null;
		}
		return container;
	}

	public void writeCarPartEntry(ContainerReference cRef, Serializable obj, WorkTaskLabel label)
					throws CarFactoryException {
		try {
			capi.write(cRef, 0, null,
							new Entry(obj, LabelCoordinator.newCoordinationData(label.toString())));
		} catch (MzsCoreException e) {
			e.printStackTrace();
			throw new CarFactoryException("Could not write to container " + cRef.getStringRepresentation() + "\n"
							+ e.getMessage());
		}
	}
	
	public void writeFinalCar(ContainerReference cRef, Car car, TransactionReference tx) throws CarFactoryException {
		try {
			capi.write(cRef, 0, tx, new Entry(car, FifoCoordinator.newCoordinationData()));
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not write to container " + cRef.getStringRepresentation() + "\n"
							+ e.getMessage());
		}
	}
	
	public ArrayList<Serializable> readTest() throws CarFactoryException {

		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(LabelCoordinator.newSelector(WorkTaskLabel.CAR_BODY.toString(), 1));
		
		try {
			System.out.println(selectors);
			return capi.take(this.lookupContainer(ConfigSettings.containerCarPartsName), selectors, RequestTimeout.INFINITE, null);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not write to container " + ConfigSettings.containerCarPartsName + "\n"
							+ e.getMessage());
		}
	}

	public ArrayList<Serializable> readEntry(ContainerReference cRef, CoordinatorType type, List<WorkTaskLabel> labels,
					int count, TransactionReference tx, boolean once) throws CarFactoryException {

		List<Selector> selectors = new ArrayList<Selector>();
		switch (type) {
		case LABEL:
			for (WorkTaskLabel label : labels) {
				selectors.add(LabelCoordinator.newSelector(label.toString(), count));
			}
			break;
		case FIFO:
			selectors.add(FifoCoordinator.newSelector());
		default:
			selectors = null;
			break;
		}
		try {
			System.out.println(selectors);
			long timeout = 0;
			if(once == true) {
				timeout = RequestTimeout.INFINITE;
			}
			else {
				timeout = RequestTimeout.TRY_ONCE;
			}
			return capi.take(cRef, selectors, timeout, tx);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not write to container " + cRef.getStringRepresentation() + "\n"
							+ e.getMessage());
		}
	}
	
	public TransactionReference createTransaction() throws CarFactoryException {
		try {
			return capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, this.spaceURI);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not create transaction\n" + e.getMessage());
		}
	}
	
	public void commitTransaction(TransactionReference tx) throws CarFactoryException {
		try {
			capi.commitTransaction(tx);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not commit transaction\n" + e.getMessage());
		}
	}
	
	public void rollbackTransaction(TransactionReference tx) throws CarFactoryException {
		try {
			capi.rollbackTransaction(tx);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not rollback transaction\n" + e.getMessage());
		}
	}

	public void disconnect() {
		this.core.shutdown(true);
		System.out.println("DONE SHUTDOWN");
		System.out.println(this.core);
	}
}
