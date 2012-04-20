package at.ac.sbc.carfactory.application;

import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarPartEnum;
import at.ac.sbc.carfactory.util.ConfigSettings;
import at.ac.sbc.carfactory.util.SpaceUtil;

public class Producer implements Runnable {

	private long id;
	private int delay;
	private int curNumParts;
	private CarPartEnum curCarPart;
	private boolean running = true;
	private boolean isWorking = false;
	private SpaceUtil space;

	public Producer(long id) {
		this.delay = ConfigSettings.maxDelayWorkers;
		this.init();
	}

	public Producer(long id, int delay) {
		this.delay = delay;
		this.init();
	}

	private void init() {
		this.space = new SpaceUtil();
	}

	@Override
	public void run() {
		while (running == true) {
			// TODO: wait for work
			if (this.isWorking == true) {
				this.produce();
			}
		}
		while (this.isWorking == true) {
			// TODO: wait for work to finish
		}
		System.out.println("Producer finished");
	}

	public void produce() {
		try {
			while (this.curNumParts > 0) {
				// TODO: produce part
				switch (this.curCarPart) {
				case CAR_BODY:
					// TODO: Produce CAR_BODY
				case CAR_TIRE:
					// TODO: Produce CAR_TIRE
				case CAR_MOTOR:
					CarMotor motor = new CarMotor();
					motor.setId(1);
					this.space.writeCarPartEntry(this.space.lookupContainer(ConfigSettings.containerName), motor);
					System.out.println("Written to space");
				default:
					// TODO: NOTHING
				}
				System.out.println("PRODUCED: and delay is: " + delay);
				Thread.sleep(delay);
				this.curNumParts--;
			}
			this.isWorking = false;

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setWorkProperties(int numParts, CarPartEnum carPart) {
		this.curNumParts = numParts;
		this.curCarPart = carPart;
		this.isWorking = true;
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
