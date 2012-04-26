package at.ac.sbc.carfactory.domain;

public class WorkTask {
	
	private CarPartEnum carPart;
	private int numParts;
	
	public WorkTask() {
		
	}
	
	public WorkTask(int numParts, CarPartEnum carPart) {
		this.carPart = carPart;
		this.numParts = numParts;
	}
	
	public CarPartEnum getCarPart() {
		return carPart;
	}
	public void setCarPart(CarPartEnum carPart) {
		this.carPart = carPart;
	}
	public int getNumParts() {
		return numParts;
	}
	public void setNumParts(int numParts) {
		this.numParts = numParts;
	}

}
