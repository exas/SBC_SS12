package at.ac.sbc.carfactory.domain;

public class CarTire extends CarPart {

	private static final long serialVersionUID = -8368727475515016116L;
	
	@Override
	public CarPartType getCarPartType() {
		return CarPartType.CAR_TIRE;
	}
}
