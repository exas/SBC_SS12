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

import at.ac.sbc.carfactory.jms.dto.CarDTO;

public class Logistician extends Worker implements MessageListener, ExceptionListener {
	
	private ConnectionFactory cf;
	private Connection connection;
	private Session session;
	private Queue updateGUIQueue;
	private Queue assembledCarQueue;
	private MessageConsumer messageConsumer;
	private Context context;
	
	private final static Logger logger = Logger.getLogger(Logistician.class);
	
	public Logistician(long id) {
		super(id);
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
        	//TODO connect to server do not create a new Server Instance!
    		this.assembledCarQueue = (Queue) context.lookup("/queue/assembledCarQueue");
    		this.updateGUIQueue = (Queue) context.lookup("/queue/updateGUIQueue");

            
            connection = cf.createConnection();
            
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            //listens to painterJobQueue
            messageConsumer = session.createConsumer(assembledCarQueue);
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
    	
        MessageProducer producerUpdateGUI = null;
        
        try {

        	producerUpdateGUI = session.createProducer(updateGUIQueue);

        	if (inMessage instanceof ObjectMessage) {
    			inObjectMessage = (ObjectMessage) inMessage;
    			
    			if (inObjectMessage.getObject() instanceof CarDTO) {
	    			CarDTO carDTO = (CarDTO)inObjectMessage.getObject();

					logger.debug("<"+this.getId()+">: Received Msg from AssembledCarQueue");
					
					if(carDTO.getId() != null) {
						carDTO.setLogisticWorkerId(this.getId());
							
            			outObjectMessage = session.createObjectMessage(carDTO);
            			
            			producerUpdateGUI.send(outObjectMessage);
            			
            			logger.debug("<"+this.getId()+">: AssembledCar with Car<"+carDTO.getId()+"> is send out and finished. Updating GUI via Message.");
            			
            			//TODO Correct behaviour would be update DB via Queue and then inform GUI Queue from Server
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
		 if(args.length != 1) {
			 logger.error("Provide numeric ID as argument");
			 System.exit(-1);
		 }
		 Long id = null;
		 try {
			 id = Long.parseLong(args[0]);
		 } catch (Exception ex) {
			 logger.error("Provide numeric ID as argument");
			 System.exit(-2);
		 }
		 
		Logistician logistician = new Logistician(id);
		
		logger.info("Enter 'quit' to exit LogisticianWorker...");
		Scanner sc = new Scanner(System.in);
	    
		while(!sc.nextLine().equals("quit"));
		
		logistician.stopListening();
		logger.info("Stopped Listening properly.");
		
		logger.info("LogisticianWorker exited.");
	}

	@Override
	public void onException(JMSException e) {
		logger.error("Exception:"+e.toString());
	}

}
