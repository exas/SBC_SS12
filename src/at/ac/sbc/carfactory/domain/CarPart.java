package at.ac.sbc.carfactory.domain;

import java.io.Serializable;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Queryable;

@Queryable
public abstract class CarPart implements Serializable {

	private static final long serialVersionUID = 6513813391956502127L;

	protected long id;
	protected Long producerId;

	protected Long orderId;

	@Index(label="type")
	protected CarPartType carPartType;
	protected Long carId;

	protected Boolean isFree;

	protected Boolean isDefect;

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

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
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

	public Boolean isFree() {
		return isFree;
	}

	public void setFree(Boolean isFree) {
		this.isFree = isFree;
	}

	public Boolean isDefect() {
		return isDefect;
	}

	public void setDefect(Boolean isDefect) {
		this.isDefect = isDefect;
	}
}
