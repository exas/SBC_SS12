package at.ac.sbc.carfactory.jms.application;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.domain.CarPartType;
import at.ac.sbc.carfactory.domain.WorkTask;
import at.ac.sbc.carfactory.jms.application.Producer;
import at.ac.sbc.carfactory.jms.server.JobManagementListener;
import at.ac.sbc.carfactory.ui.util.Model;
import at.ac.sbc.carfactory.util.CarFactoryException;

/**
 * 
 * @author exas
 * 
 */
public class CarFactoryManager extends Model {

	private static final int poolSize = 15;
	private static final int maxPoolSize = 50;
	private static final long keepAliveTime = 10;
	private final JobManagementListener jobManagementListener = new JobManagementListener();
	
	private final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(poolSize);
	private ThreadPoolExecutor threadPool;
	private Map<Long, Producer> producers;
	private Long idCounter;
	private Logger logger = Logger.getLogger(CarFactoryManager.class);

	public CarFactoryManager() {
		this.threadPool = new ThreadPoolExecutor(CarFactoryManager.poolSize, CarFactoryManager.maxPoolSize,
				CarFactoryManager.keepAliveTime, TimeUnit.SECONDS, queue);
		this.producers = new HashMap<Long, Producer>();
		this.idCounter = 1L;
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
		return true;
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



}
