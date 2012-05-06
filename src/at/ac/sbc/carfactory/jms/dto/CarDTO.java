package at.ac.sbc.carfactory.jms.dto;

//Data Transfer Object, just a simple class which holds all relevant data for transfering this object via Messaging/Queues.

public class CarDTO implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String color;
	private Long assemblyWorkerId;
	private Long painterWorkerId;
	private Long logisticWorkerId;
	
	private Long carBodyId;
	private Long carMotorId;
	
	private Long carTireId_1;
	private Long carTireId_2;
	private Long carTireId_3;
	private Long carTireId_4;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
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

	public Long getCarTireId_1() {
		return carTireId_1;
	}

	public void setCarTireId_1(Long carTireId_1) {
		this.carTireId_1 = carTireId_1;
	}

	public Long getCarTireId_2() {
		return carTireId_2;
	}

	public void setCarTireId_2(Long carTireId_2) {
		this.carTireId_2 = carTireId_2;
	}

	public Long getCarTireId_3() {
		return carTireId_3;
	}

	public void setCarTireId_3(Long carTireId_3) {
		this.carTireId_3 = carTireId_3;
	}

	public Long getCarTireId_4() {
		return carTireId_4;
	}

	public void setCarTireId_4(Long carTireId_4) {
		this.carTireId_4 = carTireId_4;
	}
	
}
