package at.ac.sbc.carfactory.domain;

public class CarBody extends CarPart {

	private static final long serialVersionUID = 5596880421243550656L;
	private CarColor color;
	private Long painterWorkerId;
	
	public CarBody() {
	}
	
	public CarBody(Long id, Long producerId) {
		super();
		this.id = id;
		this.producerId = producerId;
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
