package at.ac.sbc.carfactory.domain;

import java.io.Serializable;

public abstract class CarPart implements Serializable {

	private static final long serialVersionUID = 6513813391956502127L;
	private long id;
	private CarPartType carPartType;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public CarPartType getCarPartType() {
		return carPartType;
	}

	public void setCarPartType(CarPartType carPartType) {
		this.carPartType = carPartType;
	}
}
