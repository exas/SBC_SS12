package at.ac.sbc.carfactory.util;

import java.net.URI;
import java.net.URISyntaxException;

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

	public SpaceUtil() {
		// TODO: Singleton?
		try {
			this.spaceURI = new URI(ConfigSettings.spaceProtocol + "://" + ConfigSettings.spaceURL + ":" + ConfigSettings.spacePort);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	public ContainerReference createContainer(String name) {
		// create a container
		ContainerReference container = null;
        try {
        	container = capi.createContainer(name, this.spaceURI, Container.UNBOUNDED, null, null, null);
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return container;
	}
	
	public ContainerReference lookupContainer(String name) {
		// lookup a container
		ContainerReference container = null;
        try {
			container = capi.lookupContainer(name, this.spaceURI, Container.UNBOUNDED, null, null, null);
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return container;
	}
	
	public void writeCarPartEntry(ContainerReference cRef, CarPart part) {
        try {
        	System.out.println("writing");
        	capi.write(cRef, new Entry(part));
        	System.out.println("writing done");
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		this.core.shutdown(true);
		System.out.println("DONE SHUTDOWN");
		System.out.println(this.core);
	}
}
