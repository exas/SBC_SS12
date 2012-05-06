package at.ac.sbc.carfactory.util;


import org.hornetq.jms.server.embedded.EmbeddedJMS;


public class JMSServer {

	private static JMSServer instance = null;
	private EmbeddedJMS jms;
	
	private JMSServer() {
		jms = new EmbeddedJMS();
	}
	
	public synchronized static JMSServer getInstance() {
        if (instance == null) {
            instance = new JMSServer();
        }
        return instance;
    }
	
	public void start() {
		try {
			jms.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stop() {
		try {
			jms.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public EmbeddedJMS getJms() {
		return this.jms;
	}

	public Object lookup(String name) {
		return jms.lookup(name);
	}
	
}
