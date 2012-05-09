package at.ac.sbc.carfactory.domain;

public class CarMotor extends CarPart {

	private static final long serialVersionUID = 5306530929885189479L;

	public CarMotor() {
		this.carPartType = CarPartType.CAR_MOTOR;
	}
	
	public CarMotor(Long id, Long producerId) {
		super();
		this.id = id;
		this.producerId = producerId;
		this.carPartType = CarPartType.CAR_MOTOR;
	}
	
	@Override
	public CarPartType getCarPartType() {
		return CarPartType.CAR_MOTOR;
	}
	
	
	
	
}
