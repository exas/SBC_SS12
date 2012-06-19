package at.ac.sbc.carfactory.domain;

import java.util.concurrent.atomic.AtomicLong;

public class WorkTask {

	private CarPartType carPartType;
	private int numParts;
	private Double errorRate;

    private static final AtomicLong NEXT_CAR_PART_ID = new AtomicLong(1);

    //carPartId is used adequately to taskId, this means taskID = carPartId
    //for each new WorkTask Object a new Id is given, it is incremented as soon as it is iterated numParts often in Producer (see there)
    private Long carPartId = null;

	public WorkTask() {
		this.carPartId = generateNextCarPartId();
	}

	public WorkTask(int numParts, Double errorRate, CarPartType carPartType) {
		this.carPartType = carPartType;
		this.setErrorRate(errorRate);
		this.numParts = numParts;
		this.carPartId = generateNextCarPartId();
	}

	public CarPartType getCarPartTyp() {
		return carPartType;
	}
	public void setCarPartTyp(CarPartType carPartType) {
		this.carPartType = carPartType;
	}
	public int getNumParts() {
		return numParts;
	}
	public void setNumParts(int numParts) {
		this.numParts = numParts;
	}

	public Long generateNextCarPartId() {
		return NEXT_CAR_PART_ID.getAndIncrement();
	}

	public Long getCarPartId() {
		return carPartId;
	}

	public void setCarPartId(Long carPartId) {
		this.carPartId = carPartId;
	}

	public Double getErrorRate() {
		return errorRate;
	}

	public void setErrorRate(Double errorRate) {
		this.errorRate = errorRate;
	}

	public Object getCarMotorTyp() {
		// TODO Auto-generated method stub
		return null;
	}

}
