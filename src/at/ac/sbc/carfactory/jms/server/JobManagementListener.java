package at.ac.sbc.carfactory.jms.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.backend.CarPartDaoSimpleImpl;
import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarPart;
import at.ac.sbc.carfactory.domain.CarTire;
import at.ac.sbc.carfactory.jms.dto.CarDTO;
import at.ac.sbc.carfactory.jms.dto.CarPartDTO;
import at.ac.sbc.carfactory.util.JMSServer;


public class JobManagementListener implements MessageListener {

    private ConnectionFactory cf;
	private Connection connection;
	private Session session;
	private Queue carPartQueue;
	private Queue assemblingJobQueue;
	private Queue painterJobQueue;
	private MessageConsumer messageConsumer;
	
	
	private final Logger logger = Logger.getLogger(JobManagementListener.class);
	
	public JobManagementListener() {
		logger.debug("JobManagementListener<"+this.toString()+">: instantiated");
		setup();
		
	}

    public void setup() {
        try {
        	this.cf = (ConnectionFactory) JMSServer.getInstance().lookup("/ConnectionFactory"); 
    		this.carPartQueue = (Queue) JMSServer.getInstance().lookup("/queue/carPartQueue");
    		this.assemblingJobQueue = (Queue) JMSServer.getInstance().lookup("/queue/assemblingJobQueue");
    		this.painterJobQueue = (Queue) JMSServer.getInstance().lookup("/queue/painterJobQueue");
            
            connection = cf.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            messageConsumer = session.createConsumer(carPartQueue);
    		messageConsumer.setMessageListener(this);
    		connection.start();
            //topicConnFactory = 
            //producer = session.createProducer(topic);
        } catch (Throwable t) {
            // JMSException could be thrown
            logger.error("JobManagementListener<"+this.toString()+">:setup:" + "Exception: "+ t.toString());
        }
    }
	
	
	@Override
	public void onMessage(Message inMessage) {
		logger.debug("JobManagementListener<"+this.toString()+">: on Message");
		
		if(session == null) {
    		logger.error("JobManagementListener<"+this.toString()+">:onMessage  Session is NULL, RETURN, no processing of message possibel.");
    		return;
    	}
		
		ObjectMessage inObjectMessage = null;
    	ObjectMessage outObjectMessage = null;
    	Session session = null;
        MessageProducer producerAssemblingJob = null;
        MessageProducer producerPainterJob = null;
        CarPartDTO carPartDTO = null;

        try {
        	session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
        	producerAssemblingJob = session.createProducer(assemblingJobQueue);
        	producerPainterJob = session.createProducer(painterJobQueue);
        	
        	if (inMessage instanceof ObjectMessage) {
    			inObjectMessage = (ObjectMessage) inMessage;
            	
    			if (inObjectMessage.getObject() instanceof CarPartDTO) {
	    			carPartDTO = (CarPartDTO)inObjectMessage.getObject();
	            	
	    			if(inMessage.getStringProperty("type").equals("updateCarBodyByPainter")) {
	    				//Painter sent this message to the Queue
	    				logger.debug("<"+this.hashCode()+">: Received Msg from Painter (painted Body).");
            			
	    				if(carPartDTO.getId() != null) {
	    					
	    					CarBody carBody = CarPartDaoSimpleImpl.getInstance().getCarBodyById(carPartDTO.getId());
	    					carBody.setId(new Long(carPartDTO.getId()));
	    					carBody.setPainterWorkerId(carPartDTO.getPainterId());
	    					carBody.setColor(carPartDTO.getBodyColor());
	    					
	    					logger.debug("CarBody:<"+carBody.getId()+">, Producer:<"+carBody.getProducerId()+">");
	    					logger.debug("PainterWorkerId:<"+carBody.getPainterWorkerId()+">");
	    					logger.debug("Color:<"+carBody.getColor()+">");
	    					
	    					//delete from used part Carts and add to FreeCarBody List
	    					CarPartDaoSimpleImpl.getInstance().deleteCarBodyById(carBody.getId());
	    					
	    					//save into free CarBodyList
	    					CarPartDaoSimpleImpl.getInstance().saveFreeCarBody(carBody);
	    				}
	    				
	    			} 
	    			else if (inMessage.getStringProperty("type").equals("newProducedCarPart")) {
	    				//Producer send new produced Car Part into Queue
		            	if(carPartDTO.getId() != null) {
		            		
		            		switch(carPartDTO.getCarPartType()) {
			            		case CAR_BODY:
			            			logger.debug("<"+this.hashCode()+">: creating CarBody for in-memory-DB");
			            			CarBody carBody = null;
			            			carBody = new CarBody(carPartDTO.getId(), carPartDTO.getProducerId());
			            			CarPartDaoSimpleImpl.getInstance().saveFreeCarBody(carBody);
			            			logger.debug("<"+this.hashCode()+">: saved CarBody in in-memory-DB");
			            			break;
			            		case CAR_MOTOR:
			            			logger.debug("<"+this.hashCode()+">: creating CarMotor in-memory-DB");
			            			CarMotor carMotor = null;
			            			carMotor = new CarMotor(carPartDTO.getId(), carPartDTO.getProducerId());
			            			CarPartDaoSimpleImpl.getInstance().saveFreeCarMotor(carMotor);
			            			logger.debug("<"+this.hashCode()+">: saved CarMotor in-memory-DB");
			            			break;
			            		case CAR_TIRE:
			            			logger.debug("<"+this.hashCode()+">: creating CarTire in-memory-DB");
			            			CarTire carTire = null;
			            			carTire = new CarTire(carPartDTO.getId(), carPartDTO.getProducerId());
			            			CarPartDaoSimpleImpl.getInstance().saveFreeCarTire(carTire);
			            			logger.debug("<"+this.hashCode()+">: saved CarTire in-memory-DB");
			            			break;
			            		default: 
			            			break;
		            		}
		            		
		            		//check if enough parts free for assemblingJob
		            		CarBody carBody = CarPartDaoSimpleImpl.getInstance().getNextFreeCarBodyAndRemove();
		            		CarMotor carMotor = CarPartDaoSimpleImpl.getInstance().getNextFreeCarMotorAndRemove();
		            		List<CarTire> carTires = CarPartDaoSimpleImpl.getInstance().getNextFreeCarTireSetAndRemove();
		            		
		            		if(carBody != null && carMotor != null && carTires.size() == 4) {
		            			//assemble is possible
		            			logger.debug("<"+this.hashCode()+">: AssembleJob is possible (Body,Motor,4xTire reserved).");
		            			logger.debug("CarBody:<"+carBody.getId()+">, Producer:<"+carBody.getProducerId()+">");
		            			logger.debug("CarMotor:<"+carMotor.getId()+">, Producer:<"+carMotor.getProducerId()+">");
		            			//save back to in-memory-DB but not in free LIST, into used List
		            			CarPartDaoSimpleImpl.getInstance().saveCarBody(carBody);
		            			CarPartDaoSimpleImpl.getInstance().saveCarMotor(carMotor);
		            			
		            			
		            			List<Long> carTireIds = new ArrayList<Long>();
		            			
		            			for(CarTire carTire: carTires) {
		            				logger.debug("CarTire:<"+carTire.getId()+">, Producer:<"+carTire.getProducerId()+">");
		            				carTireIds.add(carTire.getId());
		            				CarPartDaoSimpleImpl.getInstance().saveCarTire(carTire);
		            			}
		            			
		            			//create Data Transfer Object (minimal objects containing only IDs...)
		            			CarDTO carDTO = new CarDTO();
		            			carDTO.setCarBodyId(new Long(carBody.getId()));
		            			carDTO.setCarMotorId(new Long(carMotor.getId()));
		            			carDTO.setCarTireIds(carTireIds);
		            			
		            			//send carDTO to assemblingJobQueue
		            			
		            			outObjectMessage = session.createObjectMessage(carDTO);
		            			producerAssemblingJob.send(outObjectMessage);

		            		} else if (carBody != null && carBody.isPainted() == false) {
		            			//PainterJob for single Body is available
		            			logger.debug("<"+this.hashCode()+">: PainterJob is possible (CarBody<"+carBody.getId()+"> reserved).");
		            			
		            			//save back to in-memory-DB but not in free LIST, into used CarPart List
		            			CarPartDaoSimpleImpl.getInstance().saveCarBody(carBody);
		            			
		            			//save Motor and Tires into Free list since not USED for PainterJob Creation!
		            			if(carMotor != null) {
		            				CarPartDaoSimpleImpl.getInstance().saveFreeCarMotor(carMotor);
		            			}
		            			
		            			for(CarTire carTire: carTires) {
		            				CarPartDaoSimpleImpl.getInstance().saveFreeCarTire(carTire);
		            			}
		            			
		            			CarPartDTO carBodyDTO = new CarPartDTO();
		            			carBodyDTO.setId(new Long(carBody.getId()));
		            			carBodyDTO.setCarPartType(carBody.getCarPartType());
		            			carBodyDTO.setProducerId(carBody.getProducerId());
		            			
		            			//send carBodyDTO to painterJobQueue
		            			
		            			outObjectMessage = session.createObjectMessage(carBodyDTO);
		            			producerPainterJob.send(outObjectMessage);
		            			
		            			logger.debug("JobManagementListener<"+this.toString()+">: PainterJob with CarBody<"+carBodyDTO.getId()+"> is send out to QUEUE");
		            			
		            		} else {
		            			//save back to in-memory-DB in free LIST since no match and give up reserved ones!
		            		
		            			if(carBody != null) {
		            				CarPartDaoSimpleImpl.getInstance().saveFreeCarBody(carBody);
		            			}
		            			//save Motor and Tires into Free list since not USED for PainterJob Creation!
		            			if(carMotor != null) {
		            				CarPartDaoSimpleImpl.getInstance().saveFreeCarMotor(carMotor);
		            			}
		            			
		            			for(CarTire carTire: carTires) {
		            				CarPartDaoSimpleImpl.getInstance().saveFreeCarTire(carTire);
		            			}
		            		}
		            	}
		            }
    			}
    		}
        } catch (JMSException e) {
            logger.error("JobManagementListener.onMessage: JMSException: " + e.toString());
            e.printStackTrace();
        } catch (Throwable te) {
        	logger.error("JobManagementListener.onMessage: Exception: " + te.toString());
            te.printStackTrace();
        }
	}
    
	 /**
     * Closes the connection.
     */
    @PreDestroy
    public void endConnection() throws RuntimeException {
    	logger.debug("JobManagementListener<"+this.toString()+">: PREDESTROY");
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
