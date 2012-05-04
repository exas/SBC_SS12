package at.ac.sbc.carfactory.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;

import at.ac.sbc.carfactory.domain.CarPart;


public class SpaceUtil {
	
	private URI spaceURI;
	private Capi capi;
	private MzsCore core;

	public SpaceUtil() throws CarFactoryException {
		// TODO: Singleton?
		try {
			this.spaceURI = new URI(ConfigSettings.spaceProtocol + "://" + ConfigSettings.spaceURL + ":" + ConfigSettings.spacePort);
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
	
	public ContainerReference createContainer(String name) throws CarFactoryException {
		// create a container
		ContainerReference container = null;
        try {
        	container = capi.createContainer(name, this.spaceURI, Container.UNBOUNDED, Arrays.asList(new LabelCoordinator()), null, null);
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not create container " + name + " at " + this.spaceURI + "\n" + e.getMessage());
		}
        return container;
	}
	
	public ContainerReference lookupContainer(String name) throws CarFactoryException {
		// lookup a container
		ContainerReference container = null;
        try {
			container = capi.lookupContainer(name, this.spaceURI, Container.UNBOUNDED, null, null, null);
		} catch (MzsCoreException e) {
			//throw new CarFactoryException("Could not find container " + name + " at " + this.spaceURI + "\n" + e.getMessage());
			return null;
		}
        return container;
	}
	
	public void writeCarPartEntry(ContainerReference cRef, CarPart part) throws CarFactoryException {
        try {
        	System.out.println("writing");
        	capi.write(cRef, 0, null, new Entry(part)); //LabelCoordinator.newCoordinationData(label, name)
        	System.out.println("writing done");
		} catch (MzsCoreException e) {
			throw new CarFactoryException("Could not write to container " + cRef.getStringRepresentation() + "\n" + e.getMessage());
		}
	}
	
	public void disconnect() {
		this.core.shutdown(true);
		System.out.println("DONE SHUTDOWN");
		System.out.println(this.core);
	}
}
