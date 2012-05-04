package at.ac.sbc.carfactory.application;

import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarPart;
import at.ac.sbc.carfactory.domain.WorkTask;
import at.ac.sbc.carfactory.util.CarFactoryException;
import at.ac.sbc.carfactory.util.ConfigSettings;
import at.ac.sbc.carfactory.util.SpaceUtil;

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
		this.delay = ConfigSettings.maxDelayWorkers;
		this.init();
	}

	public Producer(long id, int delay) throws CarFactoryException {
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
				this.space.writeCarPartEntry(this.space.lookupContainer(ConfigSettings.containerName), carPart);
			} catch (CarFactoryException e) {
				this.logger.info(e.getMessage());
			}
		} catch (InterruptedException e) {
			this.logger.info(e.getMessage());
			e.printStackTrace();
		}
	}

	public void produce(WorkTask task) {
		try {
			while (task.getNumParts() > 0) {
				Thread.sleep(delay);
				// TODO: produce part
				switch (task.getCarPart()) {
				case CAR_BODY:
					// TODO: Produce CAR_BODY
				case CAR_TIRE:
					// TODO: Produce CAR_TIRE
				case CAR_MOTOR:
					CarMotor motor = new CarMotor();
					motor.setId(1);
					try {
						this.space.writeCarPartEntry(this.space.lookupContainer(ConfigSettings.containerName), motor);
					} catch (CarFactoryException e) {
						this.logger.info(e.getMessage());
					}
					System.out.println("Written to space");
				default:
					// TODO: NOTHING
				}
				System.out.println("PRODUCED: with delay: " + delay);
				task.setNumParts(task.getNumParts() - 1);
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
