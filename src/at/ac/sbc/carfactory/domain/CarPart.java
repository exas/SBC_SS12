package at.ac.sbc.carfactory.domain;

import java.io.Serializable;

public abstract class CarPart implements Serializable {

	private static final long serialVersionUID = 6513813391956502127L;
	private long id;
	private CarPartEnum carPartType;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public CarPartEnum getCarPartType() {
		return carPartType;
	}

	public void setCarPartType(CarPartEnum carPartType) {
		this.carPartType = carPartType;
	}
}
