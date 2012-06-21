package at.ac.sbc.carfactory.domain;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Queryable;

@Queryable
public class CarBody extends CarPart {

	private static final long serialVersionUID = 5596880421243550656L;
	@Index(label="painted")
	private CarColor color;
	private Long painterWorkerId;

	public CarBody() {
		this.color = null;
		this.carPartType = CarPartType.CAR_BODY;
	}

	public CarBody(Long id, Long producerId) {
		super();
		this.id = id;
		this.producerId = producerId;
		this.color = null;
		this.carPartType = CarPartType.CAR_BODY;
	}

	public CarBody(Long id, Long carId, Long orderId, Long painterId,
			Long producerId, CarPartType carPartType, CarColor bodyColor, Boolean isDefect) {
		super();
		this.id = id;
		this.carId = carId;
		this.orderId = orderId;
		this.painterWorkerId = painterId;
		this.producerId = producerId;
		this.carPartType = carPartType;
		this.color = bodyColor;
		this.isDefect = isDefect;
	}

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

	public Long getPainterWorkerId() {
		return painterWorkerId;
	}

	public void setPainterWorkerId(Long painterWorkerId) {
		this.painterWorkerId = painterWorkerId;
	}
}
