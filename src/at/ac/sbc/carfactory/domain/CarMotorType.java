package at.ac.sbc.carfactory.domain;

public enum CarMotorType {
	MOTOR_80_KW(0),
	MOTOR_100_KW(1),
	MOTOR_160_KW(2);

	private int value;

	private CarMotorType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static CarMotorType getEnumByValue(int value) {
		for(CarMotorType carMotorType : CarMotorType.values()) {
			if(carMotorType.getValue() == value) {
				return carMotorType;
			}
		}
		return null;
	}
}
