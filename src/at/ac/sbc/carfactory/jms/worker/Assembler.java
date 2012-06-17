package at.ac.sbc.carfactory.jms.worker;


import java.util.Hashtable;
import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.jms.dto.CarDTO;

public class Assembler extends Worker implements MessageListener, ExceptionListener {

	private ConnectionFactory cf;
	private Connection connection;
	private Session session;
	private Queue assemblingJobQueue;
	private Queue painterJobQueue;
	private Queue assembledCarQueue;
	private Queue updateGUIQueue;

	private MessageConsumer messageConsumer;
	private Context context;

	private final static Logger logger = Logger.getLogger(Assembler.class);

	public Assembler(Long id) {
		super(id);
		this.startListening();
		this.startConnection();
	}

	public void startConnection() {
		try {
			connection.start();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			 logger.error("<"+this.getId()+">:setup:" + "Exception: "+ e.toString());
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		try {
			if(connection != null)
				connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startListening() {
        try {
        	//TODO connect to server do not create a new Server !!
        	Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
            env.put("java.naming.provider.url", "jnp://localhost:1099");
            env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
            context = new InitialContext(env);

    		this.cf = (ConnectionFactory)context.lookup("/cf");

    		this.assembledCarQueue = (Queue)context.lookup("/queue/assembledCarQueue");
    		this.assemblingJobQueue = (Queue)context.lookup("/queue/assemblingJobQueue");
    		this.painterJobQueue = (Queue)context.lookup("/queue/painterJobQueue");
            this.updateGUIQueue = (Queue)context.lookup("/queue/updateGUIQueue");

            connection = cf.createConnection();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            this.messageConsumer = session.createConsumer(assemblingJobQueue);
    		messageConsumer.setMessageListener(this);


        } catch (Throwable t) {
            // JMSException could be thrown
            logger.error("<"+this.getId()+">:setup:" + "Exception: "+ t.toString());
        }
    }

	@Override
	public void onMessage(Message inMessage) {
		MessageProducer producerAssembledCar = null;
        MessageProducer producerPainterJob = null;
        MessageProducer producerUpdateGUI = null;

		try {
			logger.debug("<"+this.getId()+">: on Message");

			if(session == null) {
	    		logger.error("<"+this.getId()+">:onMessage  Session is NULL, RETURN, no processing of message possibel.");
	    		return;
	    	}

			ObjectMessage inObjectMessage = null;
	    	ObjectMessage outObjectMessage = null;


	        CarDTO carDTO = null;

        	producerAssembledCar = session.createProducer(assembledCarQueue);
        	producerPainterJob = session.createProducer(painterJobQueue);
        	producerUpdateGUI = session.createProducer(updateGUIQueue);

        	if (inMessage instanceof ObjectMessage) {
    			inObjectMessage = (ObjectMessage) inMessage;

    			if (inObjectMessage.getObject() instanceof CarDTO) {
	    			carDTO = (CarDTO)inObjectMessage.getObject();

					//Painter sent this message to the Queue
					logger.debug("<"+this.getId()+">: Received Msg from Server JobManagement Car to Assemble.");

					if(carDTO.getId() != null) {
						carDTO.setAssemblyWorkerId(this.getId());

						//check if painted
						if(carDTO.getCarBody().getBodyColor() != null) {
							//send CarDTO to assembledCarQueue

	            			outObjectMessage = session.createObjectMessage(carDTO);

	            			producerAssembledCar.send(outObjectMessage);

	            			logger.debug("<"+this.getId()+">: AssembledCar with Car<"+carDTO.getId()+"> is send out to assembledCarQUEUE");

	            			//update GUI
	            			producerUpdateGUI.send(outObjectMessage);
	            			logger.debug("<"+this.getId()+">: Car was assembled - Update GUI Queue, Msg sent.");
						} else {
							//send CarDTO to painterJobQueue

	            			outObjectMessage = session.createObjectMessage(carDTO);
	            			outObjectMessage.setStringProperty("type", "assembledCar");

	            			producerPainterJob.send(outObjectMessage);

	            			logger.debug("<"+this.getId()+">: AssembledCar with Car<"+carDTO.getId()+"> is send out to painterJobQUEUE since not painted.");

	            			//update GUI
	            			producerUpdateGUI.send(outObjectMessage);
	            			logger.debug("<"+this.getId()+">: Car was assembled but is not painted - Update GUI Queue, Msg sent.");
						}
					}
				}
        	}
        } catch (JMSException e) {
            logger.error("onMessage: JMSException: " + e.toString());
            e.printStackTrace();
        } catch (Throwable te) {
        	logger.error("onMessage: Exception: " + te.toString());
            te.printStackTrace();
        } finally {
			//JMS close connection and session
        	logger.info("JMS:Closing Message Producers!");
			try { if( producerAssembledCar != null ) producerAssembledCar.close();  } catch( Exception ex ) {/*ok*/}
			try { if( producerPainterJob != null ) producerPainterJob.close();  } catch( Exception ex ) {/*ok*/}
			try { if( producerUpdateGUI != null ) producerUpdateGUI.close();  } catch( Exception ex ) {/*ok*/}
		}
	}

	public void stopListening() {
		try { if( messageConsumer != null ) messageConsumer.close(); } catch( Exception ex ) {/*ok*/}
	    try { if( session != null ) session.close();  } catch( Exception ex ) {/*ok*/}
	    try { if( connection != null ) connection.close();  } catch( Exception ex ) {/*ok*/}
	    try { if( context != null ) context.close(); } catch( Exception ex ) {/*ok*/}
	}

	@Override
	public void onException(JMSException e) {
		logger.error("Listener-JMSException: " + e.toString());
	}

	public static void main(String[] args) {
		 if(args.length != 1) {
			 logger.error("Provide a numeric ID as argument");
			 System.exit(-1);
		 }
		 Long id = null;
		 try {
			 id = Long.parseLong(args[0]);
		 } catch (Exception ex) {
			 logger.error("Provide a numeric ID as argument");
			 System.exit(-1);
		 }
		 logger.info("Enter 'quit' to exit AssemblerWorker...");

		Assembler assembler = new Assembler(id);
		Scanner sc = new Scanner(System.in);

		while(!sc.nextLine().equals("quit"));

		assembler.stopListening();
		logger.info("Stopped Listening properly.");

		logger.info("PainterWorker exited.");
	}



}
