package at.ac.sbc.carfactory.domain;

import java.io.Serializable;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Queryable;

@Queryable
public abstract class CarPart implements Serializable {

	private static final long serialVersionUID = 6513813391956502127L;
	
	protected long id;
	protected Long producerId;

	@Index(label="type")
	protected CarPartType carPartType;
	private Long carId;
	
	private boolean isFree;
	
	private boolean isDefect;
	
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
	
	public boolean isDefect() {
		return isDefect;
	}
	
	public void setDefect(boolean isDefect) {
		this.isDefect = isDefect;
	}
}
