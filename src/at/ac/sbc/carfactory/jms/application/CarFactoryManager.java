package at.ac.sbc.carfactory.jms.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.domain.CarPart;
import at.ac.sbc.carfactory.domain.CarPartType;
import at.ac.sbc.carfactory.domain.WorkTask;
import at.ac.sbc.carfactory.jms.application.Producer;

import at.ac.sbc.carfactory.ui.util.Model;
import at.ac.sbc.carfactory.util.CarFactoryException;
import at.ac.sbc.carfactory.util.DomainListener;
import at.ac.sbc.carfactory.util.LogListener;

/**
 * 
 * @author exas
 * 
 */
public class CarFactoryManager extends Model {

	private static final int poolSize = 15;
	private static final int maxPoolSize = 50;
	private static final long keepAliveTime = 10;

	private final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(poolSize);
	private ThreadPoolExecutor threadPool;
	private Map<Long, Producer> producers;
	private Long idCounter;
	private Logger logger = Logger.getLogger(CarFactoryManager.class);
	private List<LogListener> logListeners;
	private List<DomainListener> domainListeners;
	
	private UpdateGUI updateGUI = null;
	
	public CarFactoryManager() {
		this.threadPool = new ThreadPoolExecutor(CarFactoryManager.poolSize, CarFactoryManager.maxPoolSize,
				CarFactoryManager.keepAliveTime, TimeUnit.SECONDS, queue);
		this.producers = new HashMap<Long, Producer>();
		this.idCounter = 1L;
		this.updateGUI = new UpdateGUI(this);
		// ExecutorService executorService = Executors.newCachedThreadPool();
		this.logger.debug("CarFactoryManager instantiated");
	}
	
	@Override
	public long createProducer() {
		long id = this.idCounter;
		Producer producer = null;
		try {
			producer = new Producer(id);
		} catch (CarFactoryException e) {
			this.logger.debug("CarFactoryException at creating a new Producer", e);
			return -1;
		}
		
		this.producers.put(id, producer);
		this.threadPool.execute(producer);
		this.idCounter++;
		this.logger.debug("Created Producer with ID: " + id);
		return id;
	}

	@Override
	public long createProducer(int numParts, CarPartType carPartType) {
		long id = this.createProducer();
		if(id == -1) {
			return id;
		}
		this.assignWorkToProducer(numParts, carPartType, id);
		return id;
	}
	
	@Override
	public boolean shutdownProducer(long id) {
		if (this.producers.get(id) != null) {
			this.producers.get(id).shutdown();
		}
		return true;
	}
	
	@Override
	public boolean shutdown() {
		System.out.println("Number of Producers: " + this.producers.size());
		Iterator<Long> it = this.producers.keySet().iterator();
		while(it.hasNext()) {
			this.producers.get(it.next()).shutdown();
		}
		this.threadPool.shutdown();
		this.updateGUI.stopListening();
		return true;
	}
	
	public void entryOperationFinished(Serializable obj) {
		System.out.println("Notified:  on:" + obj);
		this.updatedDomainObjects(obj);
	}
	
	private void updatedDomainObjects(Serializable obj) {
		if(this.domainListeners == null || this.domainListeners.size() == 0) {
			return;
		}
		for (int i = 0; i < this.domainListeners.size(); i++) {
			if (obj instanceof CarPart) {
				this.domainListeners.get(i).carPartUpdated((CarPart)obj, false);
			}
			else {
				this.domainListeners.get(i).carPartUpdated(((Car)obj).getBody(), true);
				this.domainListeners.get(i).carPartUpdated(((Car)obj).getMotor(), true);
				for(int j = 0; j < ((Car)obj).getTires().size(); j++) {
					this.domainListeners.get(i).carPartUpdated(((Car)obj).getTires().get(j), true);
				}
				this.domainListeners.get(i).carUpdated((Car)obj);
			
			}
		}
	}
	
	@Override
	public boolean assignWorkToProducer(int numParts, CarPartType carPartType, long producerID) {
		Producer producer = this.producers.get(producerID);
		if (producer == null) {
			//TODO: notify gui
			this.logger.debug("AssignWorkError: Could not find producer with id " + producerID);
			return false;
		}
		producer.addWorkTask(new WorkTask(numParts, carPartType));
		return true;
	}
	
	@Override
	public boolean deleteProducer(long id) {
		this.shutdownProducer(id);
		return true;
	}

	@Override
	public void log(String message) {
	}

	@Override
	public void addLogListener(LogListener listener) {
		if(this.logListeners == null) {
			this.logListeners = new ArrayList<LogListener>();
		}
		this.logListeners.add(listener);
	}

	@Override
	public void addDomainListener(DomainListener listener) {
		if(this.domainListeners == null) {
			this.domainListeners = new ArrayList<DomainListener>();
		}
		this.domainListeners.add(listener);
	}

}
