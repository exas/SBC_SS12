package at.ac.sbc.carfactory.domain;

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Queryable;

@Queryable
public class Car implements Serializable {

	private static final long serialVersionUID = -5463318055982788409L;
	private Long id;
	
	private Long assemblyWorkerId;
	@Index(label="painted")
	private Long painterWorkerId;
	private Long logisticWorkerId;
	
	private CarBody body;
	private CarMotor motor;
	private List<CarTire> tires;
	
	@Index(label="type")
	private CarPartType carPartType;

	public Car() {
		this.carPartType = CarPartType.CAR;
	}
	
	public Car(CarBody body, CarMotor motor, List<CarTire> tires) {
		super();
		this.carPartType = CarPartType.CAR;
		this.body = body;
		this.motor = motor;
		this.tires = tires;
	}
	
	public Car(Long assemblyWorkerId, CarBody body, CarMotor motor, List<CarTire> tires) {
		super();
		this.body = body;
		this.motor = motor;
		this.tires = tires;
		this.assemblyWorkerId = assemblyWorkerId;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public CarBody getBody() {
		return body;
	}

	public void setBody(CarBody body) {
		this.body = body;
	}

	public CarMotor getMotor() {
		return motor;
	}

	public void setMotor(CarMotor motor) {
		this.motor = motor;
	}

	public List<CarTire> getTires() {
		return tires;
	}

	public void setTires(List<CarTire> tires) {
		this.tires = tires;
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
}
