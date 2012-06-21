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
	    			car.setId(carDTO.id);
	    			car.setAssemblyWorkerId(carDTO.assemblyWorkerId);
	    			car.setLogisticWorkerId(carDTO.logisticWorkerId);
	    			car.setDefect(carDTO.isDefect);
	    			car.setOrderId(carDTO.orderId);
	    			car.setTestingFinished(carDTO.isTestingFinished);
	    			car.setTesterAllPartsAssembledWorkerId(carDTO.testerAllPartsAssembledWorkerId);
	    			car.setTesterIsDefectWorkerId(carDTO.testerIsDefectWorkerId);

	    			if(carDTO.carBody != null) {
		    			CarBody carBody = new CarBody();
		    			carBody.setCarId(carDTO.carBody.id);
		    			carBody.setColor(carDTO.carBody.bodyColor);
		    			carBody.setId(carDTO.carBody.id);
		    			carBody.setPainterWorkerId(carDTO.carBody.painterId);
		    			carBody.setProducerId(carDTO.carBody.producerId);
		    			car.setBody(carBody);
	    			}

	    			if(carDTO.carMotor != null) {
		    			CarMotor carMotor = new CarMotor();
		    			carMotor.setCarId(car.getId());
		    			carMotor.setId(carDTO.carMotor.id);
		    			carMotor.setProducerId(carDTO.carMotor.producerId);
		    			carMotor.setMotorType(carDTO.carMotor.carMotorType);
		    			car.setMotor(carMotor);
	    			}

	    			List<CarPartDTO> carPartTires = carDTO.carTires;

	    			List<CarTire> carTires = new ArrayList<CarTire>();

	    			if(carPartTires != null && !carPartTires.isEmpty()) {
		    			for(CarPartDTO c: carPartTires) {
		    				CarTire carTire = new CarTire();
		    				carTire.setId(c.id);
		    				carTire.setCarId(c.carId);
		    				carTire.setProducerId(c.producerId);

		    				carTires.add(carTire);
		    			}

		    			car.setTires(carTires);
	    			}

	    			model.entryOperationFinished(car);

		        } else if (inObjectMessage.getObject() instanceof CarPartDTO) {
	    			carPartDTO = (CarPartDTO)inObjectMessage.getObject();

	    			CarPart carPart = null;

	    			switch(carPartDTO.carPartType) {
	    				case CAR_BODY:
	    					carPart = new CarBody();
	    					((CarBody) carPart).setColor(carPartDTO.bodyColor);
	    					((CarBody) carPart).setPainterWorkerId(carPartDTO.painterId);
	    					break;
	    				case CAR_MOTOR:
	    					carPart = new CarMotor();
	    					((CarMotor) carPart).setMotorType(carPartDTO.carMotorType);
	    					break;
	    				case CAR_TIRE:
	    					carPart = new CarTire();
	    					break;
	    				default:
	    					break;
	    			}

	    			if(carPart != null) {
	    				carPart.setId(carPartDTO.id);
	    				carPart.setCarId(carPartDTO.carId);
	    				carPart.setCarPartType(carPartDTO.carPartType);
	    				carPart.setProducerId(carPartDTO.producerId);
	    				carPart.setDefect(carPartDTO.isDefect);
	    				carPart.setOrderId(carPartDTO.orderId);

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
