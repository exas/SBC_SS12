package at.ac.sbc.carfactory.domain;

public class CarMotor extends CarPart {

	private static final long serialVersionUID = 5306530929885189479L;
	
	@Override
	public CarPartType getCarPartType() {
		return CarPartType.CAR_MOTOR;
	}
	
}
