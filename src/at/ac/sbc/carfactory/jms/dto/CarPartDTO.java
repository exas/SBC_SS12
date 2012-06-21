package at.ac.sbc.carfactory.jms.dto;

import at.ac.sbc.carfactory.domain.CarMotorType;

import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.domain.CarPartType;

//Data Transfer Object, just a simple class which holds all relevant data for transfering this object via Messaging/Queues.

public class CarPartDTO implements java.io.Serializable{

	private static final long serialVersionUID = 1L;

	public Long id = null;
	public Long producerId = null;
	public CarPartType carPartType = null;
	public CarColor bodyColor = null;
	public Long painterId = null;
	public Boolean isDefect = null;
	public Long carId = null;
	public CarMotorType carMotorType = null;

	public Long orderId = null;
	public CarColor requestedCarColorByOrder = null;

	public CarPartDTO() {
		this.id = null;
		this.producerId = null;
		this.carPartType = null;
		this.bodyColor = null;
		this.painterId = null;
		this.isDefect = null;
		this.carId = null;
		this.carMotorType = null;

		this.orderId = null;
		this.requestedCarColorByOrder = null;
	}

	public boolean isDefect() {
		return isDefect;
	}
}
