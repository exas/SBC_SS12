package at.ac.sbc.carfactory.jms.application;


import java.util.ArrayList;
import java.util.List;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;

import javax.jms.MessageListener;

import javax.jms.ObjectMessage;


import org.apache.log4j.Logger;


import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarPart;
import at.ac.sbc.carfactory.domain.CarTire;
import at.ac.sbc.carfactory.jms.dto.CarDTO;
import at.ac.sbc.carfactory.jms.dto.CarPartDTO;

public class UpdateGUIListener implements MessageListener, ExceptionListener {

	private final Logger logger = Logger.getLogger(UpdateGUIListener.class);
	private CarFactoryManager model = null;
	
	public UpdateGUIListener(CarFactoryManager model) {
		this.model = model;
		logger.debug("UpdateGUIListener<"+this.toString()+">: instantiated");
	}
	
	@Override
	public void onMessage(Message inMessage) {
		logger.debug("UpdateGUIListener<"+this.toString()+">: on Message -updating GUI");
		
		ObjectMessage inObjectMessage = null;
        CarDTO carDTO = null;
        CarPartDTO carPartDTO = null;
        
        try {
        	if (inMessage instanceof ObjectMessage) {
    			inObjectMessage = (ObjectMessage) inMessage;
            	
    			if (inObjectMessage.getObject() instanceof CarDTO) {
	    			carDTO = (CarDTO)inObjectMessage.getObject();
	            	
	    			
	    			Car car = new Car();
	    			car.setId(carDTO.getId());
	    			car.setAssemblyWorkerId(carDTO.getAssemblyWorkerId());		
	    			car.setLogisticWorkerId(carDTO.getLogisticWorkerId());

	    			if(carDTO.getCarBody() != null) {
		    			CarBody carBody = new CarBody();
		    			carBody.setCarId(carDTO.getCarBody().getCarId());
		    			carBody.setColor(carDTO.getCarBody().getBodyColor());
		    			carBody.setId(carDTO.getCarBody().getId());
		    			carBody.setPainterWorkerId(carDTO.getCarBody().getPainterId());
		    			carBody.setProducerId(carDTO.getCarBody().getProducerId());
		    			car.setBody(carBody);
	    			}
	    			
	    			if(carDTO.getCarMotor() != null) {
		    			CarMotor carMotor = new CarMotor();
		    			carMotor.setCarId(car.getId());
		    			carMotor.setId(carDTO.getCarMotor().getId());
		    			carMotor.setProducerId(carDTO.getCarMotor().getProducerId());
		    			
		    			car.setMotor(carMotor);
	    			}
	    			
	    			List<CarPartDTO> carPartTires = carDTO.getCarTires();
	    			
	    			List<CarTire> carTires = new ArrayList<CarTire>();
	    			
	    			if(carPartTires != null && !carPartTires.isEmpty()) {
		    			for(CarPartDTO c: carPartTires) {
		    				CarTire carTire = new CarTire();
		    				carTire.setId(c.getId());
		    				carTire.setCarId(c.getCarId());
		    				carTire.setProducerId(c.getProducerId());
		    				
		    				carTires.add(carTire);
		    			}
		    			
		    			car.setTires(carTires);
	    			}
	    			
	    			model.entryOperationFinished(car);
	    			
		        } else if (inObjectMessage.getObject() instanceof CarPartDTO) {
	    			carPartDTO = (CarPartDTO)inObjectMessage.getObject();
	    			
	    			CarPart carPart = null;
	    			
	    			switch(carPartDTO.getCarPartType()) {
	    				case CAR_BODY:
	    					carPart = new CarBody();
	    					((CarBody) carPart).setColor(carPartDTO.getBodyColor());
	    					((CarBody) carPart).setPainterWorkerId(carPartDTO.getPainterId());
	    					break;
	    				case CAR_MOTOR:
	    					carPart = new CarMotor();
	    					break;
	    				case CAR_TIRE:
	    					carPart = new CarTire();
	    					break;
	    				default:
	    					break;
	    			}
	    			
	    			if(carPart != null) {
	    				carPart.setId(carPartDTO.getId());
	    				carPart.setCarId(carPartDTO.getCarId());
	    				carPart.setCarPartType(carPartDTO.getCarPartType());
	    				carPart.setProducerId(carPartDTO.getProducerId());

	    				model.entryOperationFinished(carPart);
	    			}
		        }
    		}
        } catch (JMSException e) {
            logger.error("UpdateGUIListener.onMessage: JMSException: " + e.toString());
            e.printStackTrace();
        } catch (Throwable te) {
        	logger.error("UpdateGUIListener.onMessage: Exception: " + te.toString());
            te.printStackTrace();
        }

	}

	@Override
	public void onException(JMSException e) {
		logger.error("Listener-JMSException: " + e.toString());
	}

}
