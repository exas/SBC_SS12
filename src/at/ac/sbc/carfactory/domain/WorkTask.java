package at.ac.sbc.carfactory.domain;

public class WorkTask {
	
	private CarPartType carPartType;
	private int numParts;
	
	public WorkTask() {
		
	}
	
	public WorkTask(int numParts, CarPartType carPartType) {
		this.carPartType = carPartType;
		this.numParts = numParts;
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

}
