package at.ac.sbc.carfactory.jms.dto;

import java.util.List;


//Data Transfer Object, just a simple class which holds all relevant data for transfering this object via Messaging/Queues.

public class CarDTO implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	private CarPartDTO carBody;
	private CarPartDTO carMotor;
	private List<CarPartDTO> carTires;
	
	private Long assemblyWorkerId;
	private Long logisticWorkerId;
	

	public CarDTO() {

	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAssemblyWorkerId() {
		return assemblyWorkerId;
	}

	public void setAssemblyWorkerId(Long assemblyWorkerId) {
		this.assemblyWorkerId = assemblyWorkerId;
	}
	
	public Long getLogisticWorkerId() {
		return logisticWorkerId;
	}

	public void setLogisticWorkerId(Long logisticWorkerId) {
		this.logisticWorkerId = logisticWorkerId;
	}

	public CarPartDTO getCarBody() {
		return carBody;
	}

	public void setCarBody(CarPartDTO carBody) {
		this.carBody = carBody;
	}

	public CarPartDTO getCarMotor() {
		return carMotor;
	}

	public void setCarMotor(CarPartDTO carMotor) {
		this.carMotor = carMotor;
	}

	public List<CarPartDTO> getCarTires() {
		return carTires;
	}

	public void setCarTires(List<CarPartDTO> carTires) {
		this.carTires = carTires;
	}
	

	
	
}
