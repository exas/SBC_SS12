package at.ac.sbc.carfactory.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;


public class SpaceUtil {
	
	private URI spaceURI;
	private Capi capi;
	private MzsCore core;

	SpaceUtil() {
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
	public void init() {
		core = DefaultMzsCore.newInstance();
        capi = new Capi(core);
	}
	
	public void createContainer(String name) {
		// create a container
        try {
			ContainerReference container = capi.createContainer(name, this.spaceURI, Container.UNBOUNDED, null, null, null);
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		this.core.shutdown(true);
	}
}
