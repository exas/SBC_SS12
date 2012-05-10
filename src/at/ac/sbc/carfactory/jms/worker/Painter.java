package at.ac.sbc.carfactory.jms.worker;


import java.util.Hashtable;
import java.util.Scanner;

import org.apache.log4j.Logger;

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

import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.domain.CarPartType;
import at.ac.sbc.carfactory.jms.dto.CarDTO;
import at.ac.sbc.carfactory.jms.dto.CarPartDTO;

public class Painter extends Worker implements MessageListener, ExceptionListener {
	
	private CarColor color;

	private ConnectionFactory cf;
	private Connection connection;
	private Session session;
	private Queue painterJobQueue; //consumer
	private Queue assembledCarQueue;
	private Queue carPartQueue;
	private Queue updateGUIQueue;
	private MessageConsumer messageConsumer;
	private Context context;
	
	private final static Logger logger = Logger.getLogger(Painter.class);
	
	public Painter(long id, CarColor color) {
		super(id);
		this.color = color;
		this.startListening();
	}
		
	public void startListening() {
        try {
        	Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
            env.put("java.naming.provider.url", "jnp://localhost:1099");
            env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
            context = new InitialContext(env);
            
    		this.cf = (ConnectionFactory)context.lookup("/cf");
        	
    		this.assembledCarQueue = (Queue) context.lookup("/queue/assembledCarQueue");
    		this.painterJobQueue = (Queue) context.lookup("/queue/painterJobQueue");
    		this.carPartQueue = (Queue) context.lookup("/queue/carPartQueue");
            this.updateGUIQueue = (Queue) context.lookup("/queue/updateGUIQueue");
            
            connection = cf.createConnection();
            
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            //listens to painterJobQueue
            messageConsumer = session.createConsumer(painterJobQueue);
    		messageConsumer.setMessageListener(this);
    		connection.start();
            //topicConnFactory = 
            //producer = session.createProducer(topic);
        } catch (Throwable t) {
            // JMSException could be thrown
            logger.error("<"+this.getId()+">:setup:" + "Exception: "+ t.toString());
        }
    }
	
	@Override
	public void onMessage(Message inMessage) {
		logger.debug("<"+this.getId()+">: on Message");
		
		if(session == null) {
    		logger.error("<"+this.getId()+">:onMessage  Session is NULL, RETURN, no processing of message possibel.");
    		return;
    	}
		
		ObjectMessage inObjectMessage = null;
    	ObjectMessage outObjectMessage = null;
        MessageProducer producerAssembledCar = null;
        MessageProducer producerCarPart = null;
        MessageProducer producerUpdateGUI = null;
        
        try {
        	//session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
        	producerAssembledCar = session.createProducer(assembledCarQueue);
        	producerCarPart = session.createProducer(carPartQueue);
        	producerUpdateGUI = session.createProducer(updateGUIQueue);
        	
        	if (inMessage instanceof ObjectMessage) {
    			inObjectMessage = (ObjectMessage) inMessage;
            	
    			//Queue Browser to get first types of assembled Car or CarPart to paint!
    			//TODO Preference, if not possible via QueueSelector then two Queues?
    			
    			//carDTO Type
    			//inMessage.getStringProperty("type").equals("assembledCar")
    			//inMessage.getStringProperty("type").equals("carPart")
    			
    			if (inObjectMessage.getObject() instanceof CarDTO) {
	    			CarDTO carDTO = (CarDTO)inObjectMessage.getObject();

					//Painter sent this message to the Queue
					logger.debug("<"+this.getId()+">: Received Msg from Assembler with an AssembledCar to paint.");
					
					if(carDTO.getId() != null) {
						carDTO.getCarBody().setPainterId(this.getId());
						
						//paint the car
						carDTO.getCarBody().setBodyColor(this.color);
							
            			outObjectMessage = session.createObjectMessage(carDTO);
            			
            			producerAssembledCar.send(outObjectMessage);
            			logger.debug("<"+this.getId()+">: AssembledCar with Car<"+carDTO.getId()+"> is painted and send out to assembledCarQUEUE");
            			
            			//update GUI
            			producerUpdateGUI.send(outObjectMessage);
            			logger.debug("<"+this.getId()+">: Assembled Car is painted - Update GUI Queue, Msg sent.");
            			
					}
				} else if (inObjectMessage.getObject() instanceof CarPartDTO) {
					CarPartDTO carPartDTO = (CarPartDTO)inObjectMessage.getObject();

					//Painter sent this message to the Queue
					logger.debug("<"+this.getId()+">: Received Msg from JobManagement with single CarBody to paint.");
					
					if(carPartDTO.getId() != null && carPartDTO.getCarPartType() == CarPartType.CAR_BODY) {
						
						//paint the CarBody
						carPartDTO.setPainterId(this.getId());
						carPartDTO.setBodyColor(this.color);
						
							
            			outObjectMessage = session.createObjectMessage(carPartDTO);
            			outObjectMessage.setStringProperty("type", "updateCarBodyByPainter");
            			
            			producerCarPart.send(outObjectMessage);
            			
            			logger.debug("<"+this.getId()+">: CarBody<"+carPartDTO.getId()+"> is painted and send back to carPartQueue.");
            			
            			//update GUI
            			producerUpdateGUI.send(outObjectMessage);
            			logger.debug("<"+this.getId()+">: CarBody is painted - Update GUI Queue, Msg sent.");
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
			try { if( producerCarPart != null ) producerCarPart.close();  } catch( Exception ex ) {/*ok*/}
			try { if( producerUpdateGUI != null ) producerUpdateGUI.close();  } catch( Exception ex ) {/*ok*/}
		}
	}
	
	public void stopListening() {
		try { if( messageConsumer != null ) messageConsumer.close(); } catch( Exception ex ) {/*ok*/}
	    try { if( session != null ) session.close();  } catch( Exception ex ) {/*ok*/}
	    try { if( connection != null ) connection.close();  } catch( Exception ex ) {/*ok*/}
	    try { if( context != null ) context.close(); } catch( Exception ex ) {/*ok*/}
	}

	public static void main(String[] args) {
		 if(args.length != 2) {
			 logger.error("Provide numeric ID and string Color as argument");
			 System.exit(-1);
		 }
		 Long id = null;
		 try {
			 id = Long.parseLong(args[0]);
		 } catch (Exception ex) {
			 logger.error("Provide numeric ID and  string Color as argument");
			 System.exit(-2);
		 }
		 
		 CarColor color = null;
		 if(args[1].equalsIgnoreCase("red")) {
			 color = CarColor.RED;
		 }
		 else if(args[1].equalsIgnoreCase("black")) {
			 color = CarColor.BLACK;
		 }
		 else if(args[1].equalsIgnoreCase("blue")) {
			 color = CarColor.BLUE;
		 }
		 else if(args[1].equalsIgnoreCase("green")) {
			 color = CarColor.GREEN;
		 }
		 else {
			 color = CarColor.WHITE;
		 }
		 
		Painter painter = new Painter(id, color);
		
		logger.info("Enter 'quit' to exit PainterWorker...");
		Scanner sc = new Scanner(System.in);
	    
		while(!sc.nextLine().equals("quit"));
		
		painter.stopListening();
		logger.info("Stopped Listening properly.");
		
		logger.info("PainterWorker exited.");
	}

	@Override
	public void onException(JMSException e) {
		logger.error("Exception:"+e.toString());
	}

}
