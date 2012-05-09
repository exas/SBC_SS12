package at.ac.sbc.carfactory.jms.server;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.PreDestroy;
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

import at.ac.sbc.carfactory.backend.CarDaoSimpleImpl;
import at.ac.sbc.carfactory.backend.CarPartDaoSimpleImpl;
import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarTire;
import at.ac.sbc.carfactory.jms.dto.CarDTO;
import at.ac.sbc.carfactory.jms.dto.CarPartDTO;


public class UpdateGUIListener implements MessageListener, ExceptionListener {

    private ConnectionFactory cf;
	private Connection connection;
	private Session session;
	private Queue updateGUIQueue;
	private MessageConsumer messageConsumer;
	
	private final Logger logger = Logger.getLogger(UpdateGUIListener.class);
	
	public UpdateGUIListener() {
		logger.debug("UpdateGUIListener<"+this.toString()+">: instantiated");
		setup();
	}

    public void setup() {
        try {
        	Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
            env.put("java.naming.provider.url", "jnp://localhost:1099");
            env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
            Context context = new InitialContext(env);
            
        	
    		this.cf = (ConnectionFactory)context.lookup("/cf");
    		this.updateGUIQueue = (Queue)context.lookup("/queue/updateGUIQueue");

            connection = cf.createConnection();
           
            //TODO check at every listener if this is necessary since in onMessage i create again a Session??
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            messageConsumer = session.createConsumer(updateGUIQueue);
    		messageConsumer.setMessageListener(this);
    		connection.start();
            //topicConnFactory = 
            //producer = session.createProducer(topic);
        } catch (Throwable t) {
            // JMSException could be thrown
            logger.error("UpdateGUIListener<"+this.toString()+">:setup:" + "Exception: "+ t.toString());
        }
    }
	
	
	@Override
	public void onMessage(Message inMessage) {
		logger.debug("UpdateGUIListener<"+this.toString()+">: on Message -updating GUI");
		
		if(session == null) {
    		logger.error("UpdateGUIListener<"+this.toString()+">:onMessage  Session is NULL, RETURN, no processing of message possibel.");
    		return;
    	}
		
		ObjectMessage inObjectMessage = null;
    	ObjectMessage outObjectMessage = null;
    	//Session session = null;
        CarDTO carDTO = null;

        try {
        	if (inMessage instanceof ObjectMessage) {
    			inObjectMessage = (ObjectMessage) inMessage;
            	
    			if (inObjectMessage.getObject() instanceof CarDTO) {
	    			carDTO = (CarDTO)inObjectMessage.getObject();
	            	
	    			
		        }
    		}
        } catch (JMSException e) {
            logger.error("JobManagementListener.onMessage: JMSException: " + e.toString());
            e.printStackTrace();
        } catch (Throwable te) {
        	logger.error("JobManagementListener.onMessage: Exception: " + te.toString());
            te.printStackTrace();
        }
//        finally {
//			//JMS close connection and session
//			try {
//				if(connection != null) {
//					connection.close();
//				}
//				if (session != null) {
//					session.close();
//				}
//			} catch (JMSException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
    
	 /**
     * Closes the connection.
     */
    @PreDestroy
    public void endConnection() throws RuntimeException {
    	logger.debug("JobManagementListener<"+this.toString()+">: PREDESTROY");
    	try {
    		if (connection != null) {
    			connection.close();
    		}
        	if (session != null) {
				session.close();
            }
        } catch (Exception e) {
                e.printStackTrace();
        }
        
    }

	@Override
	public void onException(JMSException e) {
		logger.error("Listener-JMSException: " + e.toString());
	}

}
