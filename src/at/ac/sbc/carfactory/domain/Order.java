package at.ac.sbc.carfactory.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.map.HashedMap;
import org.mozartspaces.capi3.Queryable;

@Queryable
public class Order implements Serializable {

	private static final long serialVersionUID = -5463318055982788409L;
	private Long id;
	private int carAmount;
	private CarColor carColor;
	private CarMotorType carMotorType;

	private final List<Car> finishedCars = new CopyOnWriteArrayList<Car>();

	private final ConcurrentMap<Long, CarTire> carTires = new ConcurrentHashMap<Long, CarTire>();
	private final ConcurrentMap<Long, CarMotor> carMotors = new ConcurrentHashMap<Long, CarMotor>();
	private final ConcurrentMap<Long, CarBody> carBodys = new ConcurrentHashMap<Long, CarBody>();

	private final ConcurrentLinkedQueue<Long> freeCarBodyIdQueue = new ConcurrentLinkedQueue<Long>();
	private final ConcurrentLinkedQueue<Long> freeCarMotorIdQueue = new ConcurrentLinkedQueue<Long>();
	private final ConcurrentLinkedQueue<Long> freeCarTireIdQueue = new ConcurrentLinkedQueue<Long>();



	public Order(Long id, int carAmount, CarColor carColor,
			CarMotorType carMotorType) {
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

	public boolean requiresCarPart() {
		if(requiresCarTire())
			return true;
		else if(requiresCarBody())
			return true;
		else if(requiresCarMotor())
			return true;
		else
			return false;
	}

	public boolean requiresCarTire() {
		if(carTires.size() < (carAmount*4))
			return true;
		else
			return false;
	}

	public boolean requiresCarMotor() {
		if(carMotors.size() < carAmount)
			return true;
		else
			return false;
	}

	public boolean requiresCarBody() {
		if(carBodys.size() < carAmount)
			return true;
		else
			return false;
	}

	public boolean addCarTire(CarTire carTire) {
		carTires.put(carTire.getId(), carTire);
		freeCarTireIdQueue.add(carTire.getId());

		if (carTires.containsKey(carTire.getId()))
			return true;
		else
			return false;
	}

	public boolean addCarMotor(CarMotor carMotor) {
		carMotors.put(carMotor.getId(), carMotor);
		freeCarMotorIdQueue.add(carMotor.getId());

		if (carMotors.containsKey(carMotor.getId()))
			return true;
		else
			return false;
	}

	public boolean addCarBody(CarBody carBody) {
		carBodys.put(carBody.getId(), carBody);
		freeCarBodyIdQueue.add(carBody.getId());

		if (carBodys.containsKey(carBody.getId()))
			return true;
		else
			return false;
	}

	public boolean requiresCarBody(CarColor color) {
		if (color == null)
			return true;

		if (this.carColor == color)
			return true;
		else
			return false;
	}

	public boolean requiresCarMotor(CarMotorType motorType) {
		if (this.carMotorType == motorType)
			return true;
		else
			return false;
	}

	public Map<Long, CarBody> getCarBodys() {
		return carBodys;
	}

	public Map<Long, CarMotor> getCarMotors() {
		return carMotors;
	}

	public Map<Long, CarTire> getCarTires() {
		return carTires;
	}

	public List<CarTire> getNextFreeCarTireSet() {
		List<CarTire> carTiresSet = new ArrayList<CarTire>();
		for(CarTire carTire : carTires.values()) {

			if(carTiresSet.size() < 4)
				carTiresSet.add(carTire);
			else
				break;
		}

		return carTiresSet;
	}

	public CarBody getNextFreeCarBody() {
		CarBody carBody = null;
		Long id = freeCarBodyIdQueue.poll();

		carBody = carBodys.get(id);
		return carBody;
	}

	public CarMotor getNextFreeCarMotor() {
		CarMotor carMotor = null;
		Long id = freeCarMotorIdQueue.poll();

		carMotor = carMotors.get(id);
		return carMotor;
	}

	public synchronized List<CarPart> getAllCarPartsForAssembleJob() {

		// check if available Tires
		if (freeCarTireIdQueue.isEmpty() || freeCarTireIdQueue.size() < 4)
			return null; // return EMPTY LIST if nothing in it or less then 4
		// car tires available

		// check Body availability
		if (freeCarBodyIdQueue.isEmpty())
			return null;

		// check Motor availability
		if (freeCarMotorIdQueue.isEmpty())
			return null;

		List<CarPart> carParts = new ArrayList<CarPart>();

		// get List of CarIds
		List<Long> carTireIdList = new ArrayList<Long>();
		List<CarTire> carTireSet = new ArrayList<CarTire>();

		Iterator<Long> it = freeCarTireIdQueue.iterator();

		for (int i = 0; i < 4; i++) {
			Long nextCarTireId = null;

			// check and get oldest carTires (first ones from queue)!
			if (it.hasNext())
				nextCarTireId = it.next();

			CarTire nextCarTire = null;

			if (nextCarTireId != null)
				nextCarTire = carTires.get(nextCarTireId);

			if (nextCarTire != null) {
				carTireSet.add(nextCarTire);
				carTireIdList.add(nextCarTireId); // for remove later
				carParts.add(nextCarTire);
			}
		}

		// get BodyId
		Long bodyId = null;
		Long nextCarBodyId = freeCarBodyIdQueue.peek();
		CarBody nextCarBody = null;

		if (nextCarBodyId != null) {
			nextCarBody = carBodys.get(nextCarBodyId);

		}

		if (nextCarBody != null) {
			bodyId = nextCarBodyId;
			carParts.add(nextCarBody);
		}

		// getMotorId
		Long motorId = null;

		Long nextCarMotorId = freeCarMotorIdQueue.peek();
		CarMotor nextCarMotor = null;

		if (nextCarMotorId != null)
			nextCarMotor = carMotors.get(nextCarMotorId);

		if (nextCarMotor != null) {
			motorId = nextCarMotorId;
			carParts.add(nextCarMotor);
		}

		// check all parts
		if (carTireIdList.size() < 4 || carTireIdList.isEmpty()
				|| bodyId == null || motorId == null)
			return null;

		// remove Tires
		for (Long carId : carTireIdList) {
			freeCarTireIdQueue.remove(carId);
		}

		// remove carBody elements
		freeCarBodyIdQueue.remove(bodyId);

		// remove carMotor elements
		freeCarMotorIdQueue.remove(motorId);

		// all found and removed from FreeLists
		return carParts;
	}

	public synchronized CarBody getSingleBodyPainterJob() {
		Long nextCarBodyId = null;
		CarBody nextCarBody = null;
		boolean foundCarBodyNotPainted = false;

		Iterator<Long> it = freeCarBodyIdQueue.iterator();

		while (it.hasNext()) {
			nextCarBodyId = it.next();

			if (nextCarBodyId != null) {
				nextCarBody = carBodys.get(nextCarBodyId);
			}

			if (nextCarBody != null) {
				if (!nextCarBody.isPainted() || nextCarBody.getColor() == null) {
					freeCarBodyIdQueue.remove(nextCarBodyId);

					foundCarBodyNotPainted = true;
					break;
				}
			}
		}
		if(foundCarBodyNotPainted)
			return nextCarBody;
		else
			return null;

	}

}
