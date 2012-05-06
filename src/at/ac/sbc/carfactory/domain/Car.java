package at.ac.sbc.carfactory.domain;

import java.io.Serializable;
import java.util.List;

public class Car implements Serializable {

	private static final long serialVersionUID = -5463318055982788409L;
	private long id;
	private CarBody body;
	private CarMotor motor;
	private List<CarTire> tires;

	public Car() {
	}
	
	public Car(CarBody body, CarMotor motor, List<CarTire> tires) {
		super();
		this.body = body;
		this.motor = motor;
		this.tires = tires;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
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
}
