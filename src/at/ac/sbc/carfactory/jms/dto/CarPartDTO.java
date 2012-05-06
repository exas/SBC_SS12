package at.ac.sbc.carfactory.jms.dto;

import at.ac.sbc.carfactory.domain.CarPartType;

//Data Transfer Object, just a simple class which holds all relevant data for transfering this object via Messaging/Queues.

public class CarPartDTO implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private CarPartType carPartType;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public CarPartType getCarPartType() {
		return carPartType;
	}
	public void setCarPartType(CarPartType carPartType) {
		this.carPartType = carPartType;
	}
	
}
