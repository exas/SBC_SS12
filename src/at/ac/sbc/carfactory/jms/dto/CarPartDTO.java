package at.ac.sbc.carfactory.jms.dto;

import at.ac.sbc.carfactory.domain.CarMotorType;

import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.domain.CarPartType;

//Data Transfer Object, just a simple class which holds all relevant data for transfering this object via Messaging/Queues.

public class CarPartDTO implements java.io.Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	private Long producerId;
	private CarPartType carPartType;
	private CarColor bodyColor;
	private Long painterId;
	private boolean isDefect;
	private Long carId;
	private CarMotorType carMotorType;


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
	public Long getProducerId() {
		return producerId;
	}
	public void setProducerId(Long producerId) {
		this.producerId = producerId;
	}
	public CarColor getBodyColor() {
		return bodyColor;
	}
	public void setBodyColor(CarColor bodyColor) {
		this.bodyColor = bodyColor;
	}
	public Long getPainterId() {
		return painterId;
	}
	public void setPainterId(Long painterId) {
		this.painterId = painterId;
	}
	public Long getCarId() {
		return carId;
	}
	public void setCarId(Long carId) {
		this.carId = carId;
	}
	public void setIsDefect(boolean isDefect) {
		this.isDefect = isDefect;
	}
	public boolean isDefect() {
		return isDefect;
	}
	public CarMotorType getCarMotorType() {
		return carMotorType;
	}
	public void setCarMotorType(CarMotorType carMotorType) {
		this.carMotorType = carMotorType;
	}



}
