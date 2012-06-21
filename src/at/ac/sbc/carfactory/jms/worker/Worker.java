package at.ac.sbc.carfactory.jms.worker;

import org.apache.log4j.Logger;


public abstract class Worker extends Thread{

	private long workerId;
	private final static Logger logger = Logger.getLogger(Worker.class);
	private boolean isRunning = false;

	public Worker(long id) {
		this.workerId = id;
		this.isRunning = false;
	}

	public long getWorkerId() {
		return workerId;
	}

	public void setWorkerId(long id) {
		this.workerId = id;
	}

	@Override
	public void run() {
		logger.debug("Start Worker <" + this.getWorkerId() + ">");
		isRunning = true;

		while (this.isRunning == true) {
			receiveMessage();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		logger.debug("Tester <" + this.getWorkerId() + "> is shutdown");
	}

	public void stopWorker() {
		logger.debug("STOP Worker <" + this.getWorkerId() + ">");
		this.isRunning = false;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		closeConnections();
	}

	public abstract void receiveMessage();
	protected abstract void closeConnections();

}
