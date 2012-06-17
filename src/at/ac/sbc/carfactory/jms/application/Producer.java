package at.ac.sbc.carfactory.jms.application;

import org.apache.log4j.Logger;
import at.ac.sbc.carfactory.domain.WorkTask;

import at.ac.sbc.carfactory.jms.dto.CarPartDTO;

import at.ac.sbc.carfactory.util.CarFactoryException;

import java.util.Hashtable;
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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Producer implements Runnable{

	private Long id;
	//private int numParts;
	//private CarPartType carPartType;
	int minWorkTime,maxWorkTime;

	private boolean running = true;
	private SynchronousQueue<WorkTask> tasks;

	private ConnectionFactory cf = null;
	private Connection connection = null;
	private Session session = null;

	private Queue carPartQueue = null;
	private Queue updateGUIQueue = null;

	private MessageProducer producerUpdateGUI = null;
	private MessageProducer producerCarPart = null;

	private Logger logger = Logger.getLogger(Producer.class);
	private Context context;

	public Producer(long id) throws CarFactoryException {
		this.id = id;
		this.init();
	}

	private void init() {
		//used for getting random value between 1 and 3 sec. putting thread to sleep.
		//set now to 50ms and 200ms
		minWorkTime = 50; //ms.
		maxWorkTime = 200; //ms

		this.tasks = new SynchronousQueue<WorkTask>();
		Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        env.put("java.naming.provider.url", "jnp://localhost:1099");
        env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");

		try {
			context = new InitialContext(env);
			this.cf = (ConnectionFactory)context.lookup("/cf");
			this.carPartQueue = (Queue)context.lookup("/queue/carPartQueue");
			this.updateGUIQueue = (Queue)context.lookup("/queue/updateGUIQueue");

		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		logger.debug("Producer <"+this.id+">: Start producing "+task.getNumParts()+" x <"+task.getCarPartTyp().toString()+">.");
		try {

			//create Connection for JMS Queue
			connection = cf.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			//  Create a JMS Message Producer to send a message on the queue
			producerCarPart = session.createProducer(carPartQueue);
			producerUpdateGUI = session.createProducer(updateGUIQueue);

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
				carPartDTO.setProducerId(this.id);

				carPartDTO.setId(task.getCarPartId());

				logger.debug("Producer <"+this.id+">: Producing "+task.getCarPartTyp().toString()+" <"+carPartDTO.getId()+">.");

				//check if more than 1 part is created and if not last iteration is reached
				//then generate new CarPartId and set this in task since taskId = carPartId
				if(task.getNumParts() >= 1 && (i+1) < task.getNumParts() ) {
					task.setCarPartId(task.generateNextCarPartId());
				}

				ObjectMessage outObjectMessage = null;

            	outObjectMessage = session.createObjectMessage();
            	outObjectMessage.setObject(carPartDTO);
            	outObjectMessage.setStringProperty("type", "newProducedCarPart");

            	//outObjectMessage.setStringProperty("type", "carPartType?");
            	logger.debug("Producer <"+this.id+">: Finished producing <"+task.getCarPartTyp().toString()+">. Send Message to CarPartQueue." );
				//send it using the producer
            	producerCarPart.send(outObjectMessage);

				//update GUI
    			producerUpdateGUI.send(outObjectMessage);
    			logger.debug("<"+this.getId()+">: CarBody is painted - Update GUI Queue, Msg sent.");

			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//JMS close connection and session
			logger.info("Closing Session and then connection!");
			try { if( producerCarPart != null ) producerCarPart.close();  } catch( Exception ex ) {/*ok*/}
			try { if( producerUpdateGUI != null ) producerUpdateGUI.close();  } catch( Exception ex ) {/*ok*/}
			try { if( session != null ) session.close();  } catch( Exception ex ) {/*ok*/}
		    try { if( connection != null ) connection.close();  } catch( Exception ex ) {/*ok*/}
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

		try { if( context != null ) context.close(); } catch( Exception ex ) {/*ok*/}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


}
