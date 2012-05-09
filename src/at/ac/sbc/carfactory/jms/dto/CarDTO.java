package at.ac.sbc.carfactory.jms.dto;

import java.util.ArrayList;
import java.util.List;

import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.domain.CarTire;

//Data Transfer Object, just a simple class which holds all relevant data for transfering this object via Messaging/Queues.

public class CarDTO implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private CarColor color;
	private Long assemblyWorkerId;
	private Long painterWorkerId;
	private Long logisticWorkerId;
	
	private Long carBodyId;
	private Long carMotorId;
	
	private List<Long> carTireIds;
	
	
	
	public CarDTO() {
		this.color = null;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CarColor getColor() {
		return color;
	}

	public void setColor(CarColor color) {
		this.color = color;
	}

	public Long getAssemblyWorkerId() {
		return assemblyWorkerId;
	}

	public void setAssemblyWorkerId(Long assemblyWorkerId) {
		this.assemblyWorkerId = assemblyWorkerId;
	}

	public Long getPainterWorkerId() {
		return painterWorkerId;
	}

	public void setPainterWorkerId(Long painterWorkerId) {
		this.painterWorkerId = painterWorkerId;
	}

	public Long getLogisticWorkerId() {
		return logisticWorkerId;
	}

	public void setLogisticWorkerId(Long logisticWorkerId) {
		this.logisticWorkerId = logisticWorkerId;
	}
	
	public Long getCarBodyId() {
		return carBodyId;
	}

	public void setCarBodyId(Long carBodyId) {
		this.carBodyId = carBodyId;
	}

	public Long getCarMotorId() {
		return carMotorId;
	}

	public void setCarMotorId(Long carMotorId) {
		this.carMotorId = carMotorId;
	}

	public List<Long> getCarTireIds() {
		return carTireIds;
	}

	public void setCarTireIds(List<Long> carTireIds) {
		this.carTireIds = carTireIds;
	}
	
	
}
