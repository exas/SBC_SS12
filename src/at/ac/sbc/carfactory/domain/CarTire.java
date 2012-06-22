package at.ac.sbc.carfactory.domain;

public class CarTire extends CarPart {

	private static final long serialVersionUID = -8368727475515016116L;

	public CarTire() {
		this.carPartType = CarPartType.CAR_TIRE;
	}

	public CarTire(Long id, Long producerId) {
		super();
		this.id = id;
		this.producerId = producerId;
		this.carPartType = CarPartType.CAR_TIRE;
	}

	public CarTire(Long id, Long carId, Long orderId, Long producerId,
			CarPartType carPartType, Boolean isDefect) {
		super();
		this.id = id;
		this.carId = carId;
		this.orderId = orderId;
		this.producerId = producerId;
		this.carPartType = carPartType;
		this.isDefect = isDefect;
	}

	@Override
	public CarPartType getCarPartType() {
		return CarPartType.CAR_TIRE;
	}
}
