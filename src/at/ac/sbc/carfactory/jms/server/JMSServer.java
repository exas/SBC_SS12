package at.ac.sbc.carfactory.jms.server;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.core.server.JournalType;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.jnp.server.Main;
import org.jnp.server.NamingBeanImpl;


public class JMSServer {

	private static JMSServer instance = null;
	private HornetQServer hornetqServer;
	private InitialContext context;
	private Main jndiServer;
	private NamingBeanImpl naming;
	private JMSConfiguration jmsConfig;
	private JMSServerManager jmsServer;
	
	private JMSServer() {
		Configuration configuration = new ConfigurationImpl();
        configuration.setPersistenceEnabled(true);
        configuration.setSecurityEnabled(false);
        configuration.getAcceptorConfigurations().add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));
        
        TransportConfiguration connectorConfig = new TransportConfiguration(NettyConnectorFactory.class.getName());
        configuration.getConnectorConfigurations().put("connector", connectorConfig);
        
        configuration.setPagingDirectory("${data.dir:../data}/paging");
        configuration.setBindingsDirectory("${data.dir:../data}/bindings");
        configuration.setJournalDirectory("${data.dir:../data}/journal");
        configuration.setJournalMinFiles(10);
        configuration.setLargeMessagesDirectory("${data.dir:../data}/large-messages");
        configuration.setJournalType(JournalType.NIO);
        
        hornetqServer = HornetQServers.newHornetQServer(configuration);
        
		//start naming and JNDI Server for easier lookup
		try {
			System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
			naming = new NamingBeanImpl();
         
			naming.start();
		
			jndiServer = new Main();
			jndiServer.setNamingInfo(naming);
			jndiServer.setPort(1099);

			jndiServer.setBindAddress("localhost");
			jndiServer.setRmiPort(1098);
			jndiServer.setRmiBindAddress("localhost");
			
			jndiServer.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//create JMS configuration
		
		jmsConfig = new JMSConfigurationImpl();
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
            env.put("java.naming.provider.url", "jnp://localhost:1099");
            env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
            
			context = new InitialContext(env);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        jmsConfig.setContext(context);
        
        // Step 3. Configure the JMS ConnectionFactory
        ArrayList<String> connectorNames = new ArrayList<String>();
        connectorNames.add("connector");
        ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl("cf", false,  connectorNames, "/cf");
        jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);
        
        JMSQueueConfiguration queueConfig1 = new JMSQueueConfigurationImpl("carPartQueue", null, false, "/queue/carPartQueue");
        JMSQueueConfiguration queueConfig2 = new JMSQueueConfigurationImpl("assemblingJobQueue", null, false, "/queue/assemblingJobQueue");
        JMSQueueConfiguration queueConfig3 = new JMSQueueConfigurationImpl("painterJobQueue", null, false, "/queue/painterJobQueue");
        JMSQueueConfiguration queueConfig4 = new JMSQueueConfigurationImpl("assembledCarQueue", null, false, "/queue/assembledCarQueue");
        JMSQueueConfiguration queueConfig5 = new JMSQueueConfigurationImpl("updateGUIQueue", null, false, "/queue/updateGUIQueue");
        
        
        jmsConfig.getQueueConfigurations().add(queueConfig1);
        jmsConfig.getQueueConfigurations().add(queueConfig2);
        jmsConfig.getQueueConfigurations().add(queueConfig3);
        jmsConfig.getQueueConfigurations().add(queueConfig4);
        jmsConfig.getQueueConfigurations().add(queueConfig5);
        
        try {
			jmsServer = new JMSServerManagerImpl(hornetqServer, jmsConfig);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	public synchronized static JMSServer getInstance() {
        if (instance == null) {
            instance = new JMSServer();
        }
        return instance;
    }
	
	public void start() {
		try {
			jmsServer.setContext(context);
			jmsServer.start();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stop() {
		try {
			jmsServer.stop();
			naming.stop();
			jndiServer.stop();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		JMSServer server = JMSServer.getInstance();
		server.start();
		
		@SuppressWarnings("unused")
		JobManagementListener jobManagementListener = new JobManagementListener();
		
		Scanner sc = new Scanner(System.in);
	    
		while(!sc.nextLine().equals("quit")) {
			server.stop();
		}
	}
	
//	public EmbeddedJMS getJms() {
//		return this.jms;
//	}
//
//	public Object lookup(String name) {
//		return jms.lookup(name);
//	}
	
}
