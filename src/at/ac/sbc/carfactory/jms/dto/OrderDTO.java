package at.ac.sbc.carfactory.jms.dto;

import at.ac.sbc.carfactory.domain.CarMotorType;

import at.ac.sbc.carfactory.domain.CarColor;

//Data Transfer Object, just a simple class which holds all relevant data for transfering this object via Messaging/Queues.

public class OrderDTO implements java.io.Serializable{


	private static final long serialVersionUID = 1L;

	public Long id = null;
	public Integer carAmount = null;
	public CarColor carColor = null;
	public CarMotorType carMotorType = null;

	public OrderDTO() {
		this.id = null;
		this.carAmount = null;
		this.carColor = null;
		this.carMotorType = null;
	}


}
