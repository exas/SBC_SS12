package at.ac.sbc.carfactory.jms.application;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

public class UpdateGUI {
	private ConnectionFactory cf = null;
	private Connection connection = null;
	private Session session = null;
	private Queue updateGUIQueue = null;
	private MessageConsumer messageConsumer = null;
	private Context context = null;
	private CarFactoryManager model = null;
	
	
	private final Logger logger = Logger.getLogger(UpdateGUI.class);
	private MessageListener updateGUIListener;
	
	public UpdateGUI(CarFactoryManager carFactoryManager) {
		this.model = carFactoryManager;
		this.updateGUIListener = new UpdateGUIListener(model);
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
    		this.updateGUIQueue = (Queue)context.lookup("/queue/updateGUIQueue");

            connection = cf.createConnection();
           
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            messageConsumer = session.createConsumer(updateGUIQueue);
    		messageConsumer.setMessageListener(updateGUIListener);
    		connection.start();
		} catch (JMSException e) {
			logger.error("JMS Exception!");
			e.printStackTrace();
		} catch (NamingException e) {
			logger.error("NamingException from JNDI Lookup!");
			e.printStackTrace();
		}
	}
	
	public void stopListening() {
		try { if( messageConsumer != null ) messageConsumer.close(); } catch( Exception ex ) {/*ok*/}
	    try { if( session != null ) session.close();  } catch( Exception ex ) {/*ok*/}
	    try { if( connection != null ) connection.close();  } catch( Exception ex ) {/*ok*/}
	    try { if( context != null ) context.close(); } catch( Exception ex ) {/*ok*/}
	}
}
