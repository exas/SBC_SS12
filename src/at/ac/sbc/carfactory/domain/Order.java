package at.ac.sbc.carfactory.domain;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mozartspaces.capi3.Queryable;

@Queryable
public class Order implements Serializable {

	private static final long serialVersionUID = -5463318055982788409L;
	private Long id;
	private int carAmount;
	private CarColor carColor;
	private CarMotorType carMotorType;

	private final List<Car> finishedCars = new CopyOnWriteArrayList<Car>();

	public Order(Long id, int carAmount, CarColor carColor, CarMotorType carMotorType) {
		super();
		this.id = id;
		this.setCarAmount(carAmount);
		this.setCarColor(carColor);
		this.setCarMotorType(carMotorType);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getCarAmount() {
		return carAmount;
	}

	public void setCarAmount(int carAmount) {
		this.carAmount = carAmount;
	}

	public CarColor getCarColor() {
		return carColor;
	}

	public void setCarColor(CarColor carColor) {
		this.carColor = carColor;
	}

	public CarMotorType getCarMotorType() {
		return carMotorType;
	}

	public void setCarMotorType(CarMotorType carMotorType) {
		this.carMotorType = carMotorType;
	}

	public List<Car> getFinishedCars() {
		return finishedCars;
	}

	public void addCar(Car car) {
		this.finishedCars.add(car);
	}

	public void resetFinishedCars() {
		finishedCars.clear();
	}


}
