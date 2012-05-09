package at.ac.sbc.carfactory.jms.worker;

import java.util.Hashtable;

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


public class AssemblerListener implements MessageListener, ExceptionListener{
	private ConnectionFactory cf;
	private Connection connection;
	private Session session;
	private Queue assemblingJobQueue;
	private Queue painterJobQueue;
	private Queue assembledCarQueue;

	private MessageConsumer messageConsumer;
	private Long assemblerId;
	
	private final static Logger logger = Logger.getLogger(AssemblerListener.class);
	
	public AssemblerListener(Long id) {
		this.assemblerId = id;
		this.setup();
	}
		
	public void setup() {
        try {
        	//TODO connect to server do not create a new Server !!
        	Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
            env.put("java.naming.provider.url", "jnp://localhost:1099");
            env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
            Context context = new InitialContext(env);
            
    		this.cf = (ConnectionFactory)context.lookup("/cf");
			
    		this.assembledCarQueue = (Queue)context.lookup("/queue/assembledCarQueue");
    		this.assemblingJobQueue = (Queue)context.lookup("/queue/assemblingJobQueue");
    		this.painterJobQueue = (Queue)context.lookup("/queue/painterJobQueue");
            
            connection = cf.createConnection();
            
          //TODO check at every listener if this is necessary since in onMessage i create again a Session??
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            this.messageConsumer = session.createConsumer(assemblingJobQueue);
    		messageConsumer.setMessageListener(this);
    		connection.start();
            //topicConnFactory = 
            //producer = session.createProducer(topic);
        } catch (Throwable t) {
            // JMSException could be thrown
            logger.error("<"+assemblerId+">:setup:" + "Exception: "+ t.toString());
        }
    }
	
	@Override
	public void onMessage(Message inMessage) {
		try {
			logger.debug("<"+assemblerId+">: on Message");
		
			if(session == null) {
	    		logger.error("<"+assemblerId+">:onMessage  Session is NULL, RETURN, no processing of message possibel.");
	    		return;
	    	}
			
			ObjectMessage inObjectMessage = null;
	    	ObjectMessage outObjectMessage = null;
	    	//Session session = null;
	        MessageProducer producerAssembledCar = null;
	        MessageProducer producerPainterJob = null;
	        CarDTO carDTO = null;

        
        	//session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
        	producerAssembledCar = session.createProducer(assembledCarQueue);
        	producerPainterJob = session.createProducer(painterJobQueue);
        	
        	if (inMessage instanceof ObjectMessage) {
    			inObjectMessage = (ObjectMessage) inMessage;
            	
    			if (inObjectMessage.getObject() instanceof CarDTO) {
	    			carDTO = (CarDTO)inObjectMessage.getObject();

					//Painter sent this message to the Queue
					logger.debug("<"+assemblerId+">: Received Msg from Server JobManagement Car to Assemble.");
					
					if(carDTO.getId() != null) {
						carDTO.setAssemblyWorkerId(assemblerId);
						
						//check if painted
						if(carDTO.getColor() != null) {
							//send CarDTO to assembledCarQueue
	            			
	            			outObjectMessage = session.createObjectMessage(carDTO);
	            			
	            			producerAssembledCar.send(outObjectMessage);
	            			
	            			logger.debug("<"+assemblerId+">: AssembledCar with Car<"+carDTO.getId()+"> is send out to assembledCarQUEUE");
						} else {
							//send CarDTO to painterJobQueue
	            			
	            			outObjectMessage = session.createObjectMessage(carDTO);
	            			outObjectMessage.setStringProperty("type", "assembledCar");
	            			
	            			producerPainterJob.send(outObjectMessage);
	            			
	            			logger.debug("<"+assemblerId+">: AssembledCar with Car<"+carDTO.getId()+"> is send out to painterJobQUEUE since not painted.");
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
        }
	}

	@Override
	public void onException(JMSException e) {
		logger.error("Listener-JMSException: " + e.toString());
	}
}
