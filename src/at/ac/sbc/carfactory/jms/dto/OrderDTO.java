package at.ac.sbc.carfactory.jms.dto;

import at.ac.sbc.carfactory.domain.CarMotorType;

import at.ac.sbc.carfactory.domain.CarColor;

//Data Transfer Object, just a simple class which holds all relevant data for transfering this object via Messaging/Queues.

public class OrderDTO implements java.io.Serializable{

	private static final long serialVersionUID = 1L;

	public Long id;
	public int carAmount;
	public CarColor carColor;
	public CarMotorType carMotorType;

}
