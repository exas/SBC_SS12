package at.ac.sbc.carfactory.jms.application;

import org.apache.log4j.Logger;


import at.ac.sbc.carfactory.domain.CarPartType;
import at.ac.sbc.carfactory.domain.WorkTask;

import at.ac.sbc.carfactory.jms.dto.CarPartDTO;
import at.ac.sbc.carfactory.util.CarFactoryException;
import at.ac.sbc.carfactory.util.JMSServer;

import java.util.Random;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

public class Producer implements Runnable{

	private long id;
	private int numParts;
	private CarPartType carPartType;
	int minWorkTime,maxWorkTime;
	
	private boolean running = true;
	private SynchronousQueue<WorkTask> tasks;
	
	private ConnectionFactory cf =null;
	private Connection connection = null;
	private Session session = null;
	//TASK QUEUE
	private Queue carPartQueue = null;

	private Logger logger = Logger.getLogger(Producer.class);

	public Producer(long id) throws CarFactoryException {
		this.id = id;
		this.init();
		
		//new Thread(this).start();
	}
	
	private void init() {
		//used for getting random value between 1 and 3 sec. putting thread to sleep.
		minWorkTime = 1000; //1 sec.
		maxWorkTime = 3000; //3 sec.
		
		this.tasks = new SynchronousQueue<WorkTask>();
		
		cf = (ConnectionFactory) JMSServer.getInstance().lookup("/ConnectionFactory"); 
		carPartQueue = (Queue) JMSServer.getInstance().lookup("/queue/carPartQueue");
	}
	
	@Override
	public void run() {
		logger.debug("Start Producer <"+this.id+">");
		
		while (running == true) {
			try {
				//check every sec. for new tasks
				WorkTask task = this.tasks.poll(1000, TimeUnit.MILLISECONDS);
				
				if(task != null) {
					this.produce(task);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		logger.debug("Producer <"+this.id+"> is shutdown" );
	}

	public void produce(WorkTask task) {
		logger.debug("Producer <"+this.id+">: Start producing <"+numParts+"> x <"+carPartType.toString()+">." );
		try {
			
			//create Connection for JMS Queue
			connection = cf.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			for (int i = 0; i < task.getNumParts(); i++)
			{
				//get random value for between 1-3 sec. as work time
				Random rn = new Random();
				
				int range = maxWorkTime - minWorkTime + 1;
				int randomNum =  rn.nextInt(range) + minWorkTime;
				
				Thread.sleep(randomNum);
				
				//Create Data Transfer Object for Queue
				CarPartDTO carPartDTO = new CarPartDTO();
				carPartDTO.setCarPartType(task.getCarPartTyp());
				
				ObjectMessage outObjectMessage = null;
				
				//  Create a JMS Message Producer to send a message on the queue
				MessageProducer producer = session.createProducer(carPartQueue);
				
            	outObjectMessage = session.createObjectMessage(carPartDTO);
            	
            	//outObjectMessage.setStringProperty("type", "carPartType?");
            	
				//send it using the producer
				producer.send(outObjectMessage);

			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//JMS close connection and session
				try {
					if(connection != null) {
						connection.close();
					}
					if (session != null) {
						session.close();
					}
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		logger.debug("Producer <"+this.id+">: Finished producing <"+numParts+"> x <"+carPartType.toString()+">." );
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
