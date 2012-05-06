package at.ac.sbc.carfactory.domain;

public class CarBody extends CarPart {

	private static final long serialVersionUID = 5596880421243550656L;
	private CarColor color;

	@Override
	public CarPartType getCarPartType() {
		return CarPartType.CAR_BODY;
	}
	
	public boolean isPainted() {
		if(this.color == null) {
			return false;
		}
		return true;
	}
	
	public CarColor getColor() {
		return color;
	}

	public void setColor(CarColor color) {
		this.color = color;
	}
}
