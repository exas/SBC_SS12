package at.ac.sbc.carfactory.domain;

public class CarMotor extends CarPart {

	private static final long serialVersionUID = 5306530929885189479L;
	private CarMotorType motorType;

	public CarMotor() {
		this.carPartType = CarPartType.CAR_MOTOR;
	}

	public CarMotor(Long id, Long producerId) {
		super();
		this.id = id;
		this.producerId = producerId;
		this.carPartType = CarPartType.CAR_MOTOR;
	}

	public CarMotor(Long id, Long carId, Long orderId, Long producerId,
			CarPartType carPartType, CarMotorType carMotorType, Boolean isDefect) {
		super();
		this.id = id;
		this.carId = carId;
		this.orderId = orderId;
		this.motorType = carMotorType;
		this.producerId = producerId;
		this.carPartType = carPartType;
		this.isDefect = isDefect;
	}

	@Override
	public CarPartType getCarPartType() {
		return CarPartType.CAR_MOTOR;
	}

	public CarMotorType getMotorType() {
		return motorType;
	}

	public void setMotorType(CarMotorType motorType) {
		this.motorType = motorType;
	}

}
