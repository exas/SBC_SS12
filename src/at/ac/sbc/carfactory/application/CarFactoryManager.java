package at.ac.sbc.carfactory.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import at.ac.sbc.carfactory.domain.CarPartEnum;
import at.ac.sbc.carfactory.util.CarFactoryException;
import at.ac.sbc.carfactory.util.ConfigSettings;
import at.ac.sbc.carfactory.util.LogListener;
import at.ac.sbc.carfactory.util.SpaceUtil;

/**
 * class handles connection to space as well as storing information about the
 * overall facility. Furthermore it creates the producer-threads
 * 
 * @author spookyTU
 * 
 */
public class CarFactoryManager implements ICarFactoryManager, NotificationListener {

	private static final int poolSize = 15;
	private static final int maxPoolSize = 50;
	private static final long keepAliveTime = 10;
	private final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(poolSize);
	private ThreadPoolExecutor threadPool;
	private Map<Long, Producer> producers;
	private Long idCounter;
	private NotificationManager notifManager;
	private ArrayList<Notification> notifications;
	private List<LogListener> logListeners;
	private SpaceUtil space;

	public CarFactoryManager() {
		this.threadPool = new ThreadPoolExecutor(CarFactoryManager.poolSize, CarFactoryManager.maxPoolSize,
				CarFactoryManager.keepAliveTime, TimeUnit.SECONDS, queue);
		this.producers = new HashMap<Long, Producer>();
		this.idCounter = 1L;
		// ExecutorService executorService = Executors.newCachedThreadPool();
		this.initSpace();
		this.initNotificationManager();
		this.log("CarFactoryManager instantiated");
	}

	/*
	 * connect to space and create (if not existent) a container
	 */
	private void initSpace() {
		try {
			this.space = new SpaceUtil();
			if(this.space.lookupContainer(ConfigSettings.containerName) == null) {
				this.space.createContainer(ConfigSettings.containerName);
			}
		} catch (CarFactoryException ex) {
			this.log(ex.getMessage());
		}
	}

	private void initNotificationManager() {
		this.notifManager = new NotificationManager(this.space.getMozartSpaceCore());
		this.notifications = new ArrayList<Notification>();
		try {
			this.notifications.add(notifManager.createNotification(
					this.space.lookupContainer(ConfigSettings.containerName), this, Operation.WRITE, null, null));
		} catch (MzsCoreException e) {
			this.log(e.getMessage());
		} catch (CarFactoryException e) {
			this.log(e.getMessage());
		} catch (InterruptedException e) {
			this.log(e.getMessage());
		}
	}
	
	@Override
	public long createProducer() {
		long id = this.idCounter;
		Producer producer = null;
		try {
			producer = new Producer(id, (new Random()).nextInt(ConfigSettings.maxDelayWorkers));
		} catch (CarFactoryException e) {
			this.log(e.getMessage());
			return -1;
		}
		this.producers.put(id, producer);
		this.threadPool.execute(producer);
		this.idCounter++;
		this.log("Created Producer with ID: " + id);
		return id;
	}

	@Override
	public long createProducer(int numParts, CarPartEnum carPart) {
		long id = this.createProducer();
		if(id == -1) {
			return id;
		}
		this.assignWorkToProducer(numParts, carPart, id);
		return id;
	}

	public void shutdownProducer(long id) {
		if (this.producers.get(id) != null) {
			this.producers.get(id).shutdown();
		}
	}
	
	public void shutdown() {
		// TODO: give threads time to finish work
		System.out.println("Number of Producers: " + this.producers.size());
		Iterator<Long> it = this.producers.keySet().iterator();
		while(it.hasNext()) {
			this.producers.get(it.next()).shutdown();
		}
		this.threadPool.shutdown();
		this.space.disconnect();
	}

	@Override
	public void entryOperationFinished(Notification notif, Operation oper, List<? extends Serializable> objs) {
		System.out.println("Notified: " + notif + " with operation: " + oper + " on:" + objs);
	}

	@Override
	public void assignWorkToProducer(int numParts, CarPartEnum carPart, long producerID) {
		Producer producer = this.producers.get(producerID);
		if (producer == null) {
			//TODO: notify gui
			this.log("AssignWorkError: Could not find producer with id " + producerID);
			return;
		}
		producer.setWorkProperties(numParts, carPart);
	}

	@Override
	public void deleteProducer(long id) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void log(String message) {
		Logger.getLogger(CarFactoryManager.class.getName()).log(Level.INFO, message, message);
		//this.logger.info(message);
		if(this.logListeners != null) {
			for (int i = 0; i < this.logListeners.size(); i++) {
				this.logListeners.get(i).logMessageAdded(message);
			}
		}
	}

	@Override
	public void addLogListener(LogListener listener) {
		if(this.logListeners == null) {
			this.logListeners = new ArrayList<LogListener>();
		}
		this.logListeners.add(listener);
	}

}
