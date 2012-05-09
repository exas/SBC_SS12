package at.ac.sbc.carfactory.jms.worker;


import org.apache.log4j.Logger;

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

import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.domain.CarPartType;
import at.ac.sbc.carfactory.jms.dto.CarDTO;
import at.ac.sbc.carfactory.jms.dto.CarPartDTO;

import at.ac.sbc.carfactory.util.JMSServer;


public class Painter extends Worker implements MessageListener {
	
	private CarColor color;

	private ConnectionFactory cf;
	private Connection connection;
	private Session session;
	private Queue painterJobQueue;
	private Queue assembledCarQueue;
	private Queue carPartQueue;
	private MessageConsumer messageConsumer;
	
	
	private final static Logger logger = Logger.getLogger(Assembler.class);
	
	public Painter(long id, CarColor color) {
		super(id);
		this.color = color;
		this.setup();
	}
		
	public void setup() {
        try {
        	
        	//TODO connect to server do not create a new Server Instance!
        	this.cf = (ConnectionFactory) JMSServer.getInstance().lookup("/ConnectionFactory"); 
    		this.assembledCarQueue = (Queue) JMSServer.getInstance().lookup("/queue/assembledCarQueue");
    		this.painterJobQueue = (Queue) JMSServer.getInstance().lookup("/queue/painterJobQueue");
    		this.carPartQueue = (Queue) JMSServer.getInstance().lookup("/queue/carPartQueue");
            
            connection = cf.createConnection();
            
          //TODO check at every listener if this is necessary since in onMessage i create again a Session??
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
    	Session session = null;
        MessageProducer producerAssembledCar = null;
        MessageProducer producerCarPart = null;
        
        try {
        	session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
        	producerAssembledCar = session.createProducer(assembledCarQueue);
        	producerCarPart = session.createProducer(carPartQueue);
        	
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
						carDTO.setPainterWorkerId(this.getId());
						
						//paint the car
						carDTO.setColor(this.color);
							
            			outObjectMessage = session.createObjectMessage(carDTO);
            			
            			producerAssembledCar.send(outObjectMessage);
            			
            			logger.debug("<"+this.getId()+">: AssembledCar with Car<"+carDTO.getId()+"> is send out to assembledCarQUEUE");
					}
				} else if (inObjectMessage.getObject() instanceof CarPartDTO) {
					CarPartDTO carPartDTO = (CarPartDTO)inObjectMessage.getObject();

					//Painter sent this message to the Queue
					logger.debug("<"+this.getId()+">: Received Msg from Assembler with an AssembledCar to paint.");
					
					if(carPartDTO.getId() != null && carPartDTO.getCarPartType() == CarPartType.CAR_BODY) {
						
						//paint the CarBody
						carPartDTO.setPainterId(this.getId());
						carPartDTO.setBodyColor(this.color);
						
							
            			outObjectMessage = session.createObjectMessage(carPartDTO);
            			outObjectMessage.setStringProperty("type", "updateCarBodyByPainter");
            			
            			producerCarPart.send(outObjectMessage);
            			
            			logger.debug("<"+this.getId()+">: CarBody<"+carPartDTO.getId()+"> is painted and send back to carPartQueue.");
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
		 
		@SuppressWarnings("unused")
		Painter painter = new Painter(id, color);

	}

}
