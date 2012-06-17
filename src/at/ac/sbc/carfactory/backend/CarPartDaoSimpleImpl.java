package at.ac.sbc.carfactory.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import at.ac.sbc.carfactory.domain.CarPart;

import org.xvsm.protocol.EntryValueList;

import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarTire;

public class CarPartDaoSimpleImpl {
	private static final CarPartDaoSimpleImpl INSTANCE = new CarPartDaoSimpleImpl();

	private final ConcurrentMap<Long, CarBody> carBodys = new ConcurrentHashMap<Long, CarBody>();
	private final ConcurrentLinkedQueue<Long> carBodyIdQueue = new ConcurrentLinkedQueue<Long>();

	private final ConcurrentMap<Long, CarMotor> carMotors = new ConcurrentHashMap<Long, CarMotor>();
	private final ConcurrentLinkedQueue<Long> carMotorIdQueue = new ConcurrentLinkedQueue<Long>();

	private final ConcurrentMap<Long, CarTire> carTires = new ConcurrentHashMap<Long, CarTire>();
	private final ConcurrentLinkedQueue<Long> carTireIdQueue = new ConcurrentLinkedQueue<Long>();

	private final ConcurrentMap<Long, CarBody> freeCarBodys = new ConcurrentHashMap<Long, CarBody>();
	private final ConcurrentLinkedQueue<Long> freeCarBodyIdQueue = new ConcurrentLinkedQueue<Long>();

	private final ConcurrentMap<Long, CarMotor> freeCarMotors = new ConcurrentHashMap<Long, CarMotor>();
	private final ConcurrentLinkedQueue<Long> freeCarMotorIdQueue = new ConcurrentLinkedQueue<Long>();

	private final ConcurrentMap<Long, CarTire> freeCarTires = new ConcurrentHashMap<Long, CarTire>();
	private final ConcurrentLinkedQueue<Long> freeCarTireIdQueue = new ConcurrentLinkedQueue<Long>();

	// private static final AtomicLong NEXT_CAR_ID = new AtomicLong(1);

	private CarPartDaoSimpleImpl() {
	}

	public static CarPartDaoSimpleImpl getInstance() {
		return INSTANCE;
	}

	public void saveCarBody(final CarBody carBody) {
		carBodys.putIfAbsent(carBody.getId(), carBody);
		carBodyIdQueue.add(carBody.getId());
	}

	public Set<CarBody> getAllCarBodys() {
		return (Set<CarBody>) Collections.synchronizedCollection(carBodys
				.values());
	}

	public CarBody getCarBodyById(final Long id) {
		return carBodys.get(id);
	}

	public void updateCarBodyById(final Long id, final CarBody carBodyForUpdate) {
		final CarBody carBodyToUpdate = getCarBodyById(id);
		// TODO update fields
	}

	public void deleteCarBodyById(final Long id) {
		carBodys.remove(id);
	}

	public void saveCarMotor(final CarMotor carMotor) {
		carMotors.putIfAbsent(carMotor.getId(), carMotor);
		carMotorIdQueue.add(carMotor.getId());
	}

	public Set<CarMotor> getAllCarMotors() {
		return (Set<CarMotor>) Collections.synchronizedCollection(carMotors
				.values());
	}

	public CarMotor getCarMotorById(final Long id) {
		return carMotors.get(id);
	}

	public void updateCarMotorById(final Long id,
			final CarMotor carMotorForUpdate) {
		final CarMotor carMotorToUpdate = getCarMotorById(id);
		// TODO update fields
	}

	public void deleteCarMotorById(final Long id) {
		carMotors.remove(id);
	}

	public void saveCarTire(final CarTire carTire) {
		carTires.putIfAbsent(carTire.getId(), carTire);
		carTireIdQueue.add(carTire.getId());
	}

	public Set<CarTire> getAllCarTires() {
		return (Set<CarTire>) Collections.synchronizedCollection(carTires
				.values());
	}

	public CarTire getCarTireById(final Long id) {
		return carTires.get(id);
	}

	public void updateCarTireById(final Long id, final CarTire carTireForUpdate) {
		final CarTire carBodyToUpdate = getCarTireById(id);
		// TODO update fields
	}

	public void deleteCarTireById(final Long id) {
		carTires.remove(id);
	}

	// free maps
	public void saveFreeCarBody(CarBody carBody) {
		freeCarBodys.putIfAbsent(carBody.getId(), carBody);
		freeCarBodyIdQueue.add(carBody.getId());
	}

	public Set<CarBody> getAllFreeCarBodys() {
		return (Set<CarBody>) Collections.synchronizedCollection(freeCarBodys
				.values());
	}

	public CarBody getFreeCarBodyById(final Long id) {
		return freeCarBodys.get(id);
	}

	public void updateFreeCarBodyById(final Long id,
			final CarBody carBodyForUpdate) {
		final CarBody carBodyToUpdate = getFreeCarBodyById(id);
		// TODO update fields
	}

	public void deleteFreeCarBodyById(final Long id) {
		freeCarBodys.remove(id);
	}

	public void saveFreeCarMotor(final CarMotor carMotor) {
		freeCarMotors.putIfAbsent(carMotor.getId(), carMotor);
		freeCarMotorIdQueue.add(carMotor.getId());
	}

	public Set<CarMotor> getAllFreeCarMotors() {
		return (Set<CarMotor>) Collections.synchronizedCollection(freeCarMotors
				.values());
	}

	public CarMotor getFreeCarMotorById(final Long id) {
		return freeCarMotors.get(id);
	}

	public void updateFreeCarMotorById(final Long id,
			final CarMotor carMotorForUpdate) {
		final CarMotor carMotorToUpdate = getFreeCarMotorById(id);
		// TODO update fields
	}

	public void deleteFreeCarMotorById(final Long id) {
		freeCarMotors.remove(id);
	}

	public void saveFreeCarTire(final CarTire carTire) {
		freeCarTires.putIfAbsent(carTire.getId(), carTire);
		freeCarTireIdQueue.add(carTire.getId());
	}

	public Set<CarTire> getAllFreeCarTires() {
		return (Set<CarTire>) Collections.synchronizedCollection(freeCarTires
				.values());
	}

	public CarTire getFreeCarTireById(final Long id) {
		return freeCarTires.get(id);
	}

	public void updateFreeCarTireById(final Long id,
			final CarTire carTireForUpdate) {
		final CarTire carBodyToUpdate = getFreeCarTireById(id);
		// TODO update fields
	}

	public void deleteFreeCarTireById(final Long id) {
		freeCarTires.remove(id);
	}

	// returns empty list if no carTires available or a set of 4 is not
	// available.
	public synchronized List<CarTire> getNextFreeCarTireSetAndRemove() {
		List<CarTire> carTireSet = new ArrayList<CarTire>();

		if (freeCarTires.isEmpty() || freeCarTires.size() < 4)
			return carTireSet; // return EMPTY LIST if nothing in it or less
		// then 4 car tires available

		for (int i = 0; i < 4; i++) {
			// check and get oldest carTires!
			Long nextCarTireId = freeCarTireIdQueue.peek();
			CarTire nextCarTire = null;

			if (nextCarTireId != null)
				nextCarTire = freeCarTires.get(nextCarTireId);

			if (nextCarTire != null) {
				carTireSet.add(nextCarTire);
				freeCarTires.remove(nextCarTireId);
				freeCarTireIdQueue.remove(nextCarTireId);
			}
		}

		return carTireSet;
	}

	// gets oldest carBody
	public synchronized CarBody getNextFreeCarBodyAndRemove() {
		if (freeCarBodys.isEmpty())
			return null;

		Long nextCarBodyId = freeCarBodyIdQueue.peek();
		CarBody nextCarBody = null;

		if (nextCarBodyId != null)
			nextCarBody = freeCarBodys.get(nextCarBodyId);

		if (nextCarBody != null) {
			freeCarBodys.remove(nextCarBodyId);
			freeCarBodyIdQueue.remove(nextCarBodyId);
		}

		return nextCarBody;
	}

	// gets oldest carMotor
	public synchronized CarMotor getNextFreeCarMotorAndRemove() {
		if (freeCarMotors.isEmpty())
			return null;

		Long nextCarMotorId = freeCarMotorIdQueue.peek();
		CarMotor nextCarMotor = null;

		if (nextCarMotorId != null)
			nextCarMotor = freeCarMotors.get(nextCarMotorId);

		if (nextCarMotor != null) {
			freeCarBodys.remove(nextCarMotorId);
			freeCarBodyIdQueue.remove(nextCarMotorId);
		}

		return nextCarMotor;
	}

	public synchronized List<CarPart> getAllCarPartsForAssembleJob() {

		// check if available Tires
		if (freeCarTires.isEmpty() || freeCarTires.size() < 4)
			return null; // return EMPTY LIST if nothing in it or less then 4
		// car tires available

		// check Body availability
		if (freeCarBodys.isEmpty())
			return null;

		// check Motor availability
		if (freeCarMotors.isEmpty())
			return null;

		List<CarPart> carParts = new ArrayList<CarPart>();

		// get List of CarIds
		List<Long> carTireIdList = new ArrayList<Long>();
		List<CarTire> carTireSet = new ArrayList<CarTire>();

		Iterator<Long> it = freeCarTireIdQueue.iterator();

		for (int i = 0; i < 4; i++) {
			Long nextCarTireId = null;

			// check and get oldest carTires (first ones from queue)!
			if(it.hasNext())
				nextCarTireId = it.next();

			CarTire nextCarTire = null;

			if (nextCarTireId != null)
				nextCarTire = freeCarTires.get(nextCarTireId);

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
			nextCarBody = freeCarBodys.get(nextCarBodyId);

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
			nextCarMotor = freeCarMotors.get(nextCarMotorId);

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
			freeCarTires.remove(carId);
			freeCarTireIdQueue.remove(carId);
		}

		// remove carBody elements
		freeCarBodyIdQueue.remove(bodyId);
		freeCarBodys.remove(bodyId);

		// remove carMotor elements
		freeCarMotorIdQueue.remove(motorId);
		freeCarMotors.remove(motorId);

		// all found and removed from FreeLists
		return carParts;
	}

	public boolean checkIfSingleBodyPainterJob() {
		Long nextCarBodyId = freeCarBodyIdQueue.peek();
		CarBody nextCarBody = null;

		if (nextCarBodyId != null) {
			nextCarBody = freeCarBodys.get(nextCarBodyId);
		}

		if (nextCarBody != null) {
			if(!nextCarBody.isPainted() || nextCarBody.getColor() == null)
				return true;
		}

		return false;
	}
	// private String generateNextCarId() {
	// return Long.toString(NEXT_CAR_ID.getAndIncrement());
	// }

	public CarBody getSingleBodyPainterJob() {
		Long nextCarBodyId = freeCarBodyIdQueue.peek();
		CarBody nextCarBody = null;

		if (nextCarBodyId != null) {
			nextCarBody = freeCarBodys.get(nextCarBodyId);
		}

		if (nextCarBody != null) {
			if(!nextCarBody.isPainted() || nextCarBody.getColor() == null) {
				freeCarBodyIdQueue.remove(nextCarBodyId);
				freeCarBodys.remove(nextCarBodyId);
			}
		}

		return nextCarBody;
	}
}
