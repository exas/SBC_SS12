package at.ac.sbc.carfactory.domain;

import java.io.Serializable;

public abstract class CarPart implements Serializable {

	private static final long serialVersionUID = 6513813391956502127L;
	protected long id;
	protected Long producerId;
	private CarPartType carPartType;
	private Long carId;
	
	private boolean isFree;
	
	public CarPart() {
		this.setCarId(null);
		this.setFree(false);
	}
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

	public Long getProducerId() {
		return producerId;
	}

	public void setProducerId(Long producerId) {
		this.producerId = producerId;
	}
	public Long getCarId() {
		return carId;
	}
	public void setCarId(Long carId) {
		this.carId = carId;
	}
	public boolean isFree() {
		return isFree;
	}
	public void setFree(boolean isFree) {
		this.isFree = isFree;
	}
}
