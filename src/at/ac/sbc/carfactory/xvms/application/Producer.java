package at.ac.sbc.carfactory.xvms.application;

import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.domain.CarPart;
import at.ac.sbc.carfactory.util.CarFactoryException;
import at.ac.sbc.carfactory.xvms.util.ConfigSettings;
import at.ac.sbc.carfactory.xvms.util.SpaceUtil;
import at.ac.sbc.carfactory.xvms.util.WorkTaskLabel;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Producer implements Runnable {

	private long id;
	private int delay;
	private boolean running = true;
	private BlockingQueue<CarPart> tasks;
	private SpaceUtil space;
	private Logger logger = Logger.getLogger(Producer.class);

	public Producer(long id) throws CarFactoryException {
		this.id = id;
		this.delay = ConfigSettings.maxDelayWorkers;
		this.init();
	}

	public Producer(long id, int delay) throws CarFactoryException {
		this.id = id;
		this.delay = delay;
		this.init();
	}

	private void init() throws CarFactoryException {
		this.tasks = new LinkedBlockingQueue<CarPart>();
		this.space = new SpaceUtil();
	}

	@Override
	public void run() {
		while (running == true) {
			try {
				CarPart task = this.tasks.poll(1000, TimeUnit.MILLISECONDS);
				if(task != null) {
					this.produce(task);
				}
			} catch (InterruptedException e) {
				this.logger.info(e.getMessage());
				e.printStackTrace();
			}
		}
		System.out.println("Producer finished");
	}
	
	public void produce(CarPart carPart) {
		try {
			Thread.sleep(delay);
			try {
				WorkTaskLabel label = null;
				switch (carPart.getCarPartType()) {
					case CAR_BODY: 
						label = WorkTaskLabel.CAR_BODY; 
						break;
					case CAR_MOTOR: 
						label = WorkTaskLabel.CAR_MOTOR; 
						break;
					case CAR_TIRE: 
						label = WorkTaskLabel.CAR_TIRE; 
						break;
					default:
						// DO NOTHING
				}
				carPart.setProducerId(this.id);
				this.space.writeLabelEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), carPart, label);
			} catch (CarFactoryException e) {
				this.logger.info(e.getMessage());
			}
		} catch (InterruptedException e) {
			this.logger.info(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void addProducerTasks(List<CarPart> carParts) {
		for(CarPart carPart : carParts) {
			this.addProducerTask(carPart);
		}
	}
	
	public void addProducerTask(CarPart carPart) {
		try {
			this.tasks.put(carPart);
		} catch (InterruptedException e) {
			this.logger.info(e.getMessage());
			e.printStackTrace();
		}
	}

	public void shutdown() {
		this.running = false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
