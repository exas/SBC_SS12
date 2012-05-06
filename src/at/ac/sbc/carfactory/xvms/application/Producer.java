package at.ac.sbc.carfactory.xvms.application;

import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.WorkTask;
import at.ac.sbc.carfactory.util.CarFactoryException;
import at.ac.sbc.carfactory.util.ConfigSettings;
import at.ac.sbc.carfactory.util.SpaceUtil;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class Producer implements Runnable {

	private long id;
	private int delay;
	private boolean running = true;
	private SynchronousQueue<WorkTask> tasks;
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
		this.tasks = new SynchronousQueue<WorkTask>();
		this.space = new SpaceUtil();
	}

	@Override
	public void run() {
		while (running == true) {
			try {
				WorkTask task = this.tasks.poll(1000, TimeUnit.MILLISECONDS);
				if(task != null) {
					this.produce(task);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Producer finished");
	}

	public void produce(WorkTask task) {
		try {
			while (task.getNumParts() > 0) {
				Thread.sleep(delay);
				// TODO: produce part
				switch (task.getCarPartTyp()) {
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
				
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addWorkTask(WorkTask task) {
		try {
			this.tasks.put(task);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
