package at.ac.sbc.carfactory.jms.worker;


import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.jms.dto.CarDTO;
import at.ac.sbc.carfactory.util.JMSServer;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;


public class Assembler extends Worker implements MessageListener {

	private ConnectionFactory cf;
	private Connection connection;
	private Session session;
	private Queue assemblingJobQueue;
	private Queue painterJobQueue;
	private Queue assembledCarQueue;
	private MessageConsumer messageConsumer;
	
	
	private final static Logger logger = Logger.getLogger(Assembler.class);
	
	public Assembler(long id) {
		super(id);
		this.setup();
	}
		
	public void setup() {
        try {
        	//TODO connect to server do not create a new Server !!
        	
        	this.cf = (ConnectionFactory) JMSServer.getInstance().lookup("/ConnectionFactory"); 
    		this.assembledCarQueue = (Queue) JMSServer.getInstance().lookup("/queue/assembledCarQueue");
    		this.assemblingJobQueue = (Queue) JMSServer.getInstance().lookup("/queue/assemblingJobQueue");
    		this.painterJobQueue = (Queue) JMSServer.getInstance().lookup("/queue/painterJobQueue");
            
            connection = cf.createConnection();
            
          //TODO check at every listener if this is necessary since in onMessage i create again a Session??
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            messageConsumer = session.createConsumer(assemblingJobQueue);
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
    	Session session = null;
        MessageProducer producerAssembledCar = null;
        MessageProducer producerPainterJob = null;
        CarDTO carDTO = null;

        try {
        	session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
        	producerAssembledCar = session.createProducer(assembledCarQueue);
        	producerPainterJob = session.createProducer(painterJobQueue);
        	
        	if (inMessage instanceof ObjectMessage) {
    			inObjectMessage = (ObjectMessage) inMessage;
            	
    			if (inObjectMessage.getObject() instanceof CarDTO) {
	    			carDTO = (CarDTO)inObjectMessage.getObject();

					//Painter sent this message to the Queue
					logger.debug("<"+this.getId()+">: Received Msg from Server JobManagement Car to Assemble.");
					
					if(carDTO.getId() != null) {
						carDTO.setAssemblyWorkerId(this.getId());
						
						//check if painted
						if(carDTO.getColor() != null) {
							//send CarDTO to assembledCarQueue
	            			
	            			outObjectMessage = session.createObjectMessage(carDTO);
	            			
	            			producerAssembledCar.send(outObjectMessage);
	            			
	            			logger.debug("<"+this.getId()+">: AssembledCar with Car<"+carDTO.getId()+"> is send out to assembledCarQUEUE");
						} else {
							//send CarDTO to painterJobQueue
	            			
	            			outObjectMessage = session.createObjectMessage(carDTO);
	            			outObjectMessage.setStringProperty("type", "assembledCar");
	            			
	            			producerPainterJob.send(outObjectMessage);
	            			
	            			logger.debug("<"+this.getId()+">: AssembledCar with Car<"+carDTO.getId()+"> is send out to painterJobQUEUE since not painted.");
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
		 
		@SuppressWarnings("unused")
		Assembler assembler = new Assembler(id);
	}



}
