package at.ac.sbc.carfactory.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import at.ac.sbc.carfactory.domain.Order;

import at.ac.sbc.carfactory.domain.CarPart;
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

	private final ConcurrentMap<Long, CarBody> freeCarBodysForOrder = new ConcurrentHashMap<Long, CarBody>();
	private final ConcurrentLinkedQueue<Long> freeCarBodyIdForOrderQueue = new ConcurrentLinkedQueue<Long>();

	private final ConcurrentMap<Long, CarMotor> freeCarMotorsForOrder = new ConcurrentHashMap<Long, CarMotor>();
	private final ConcurrentLinkedQueue<Long> freeCarMotorIdForOrderQueue = new ConcurrentLinkedQueue<Long>();

	private final ConcurrentMap<Long, CarTire> freeCarTiresForOrder = new ConcurrentHashMap<Long, CarTire>();
	private final ConcurrentLinkedQueue<Long> freeCarTireIdForOrderQueue = new ConcurrentLinkedQueue<Long>();

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

	public synchronized void updateCarBodyById(final Long id,
			CarBody carBodyForUpdate) {
		final CarBody carBodyToUpdate = getCarBodyById(id);

		carBodyToUpdate.setCarId(carBodyForUpdate.getCarId());
		carBodyToUpdate.setCarPartType(carBodyForUpdate.getCarPartType());
		carBodyToUpdate.setColor(carBodyForUpdate.getColor());
		carBodyToUpdate.setDefect(carBodyForUpdate.isDefect());
		carBodyToUpdate.setFree(carBodyForUpdate.isFree());
		carBodyToUpdate.setOrderId(carBodyForUpdate.getOrderId());
		carBodyToUpdate.setPainterWorkerId(carBodyForUpdate
				.getPainterWorkerId());
		carBodyToUpdate.setProducerId(carBodyForUpdate.getProducerId());

		deleteCarBodyById(id);

		freeCarBodys.putIfAbsent(carBodyToUpdate.getId(), carBodyToUpdate);

		// ADD CAR BODY IN FRONT OF ALL OTHERs since already used and should be
		// the oldest
		Object[] items = freeCarBodyIdQueue.toArray();
		freeCarBodyIdQueue.clear();
		freeCarBodyIdQueue.add(carBodyToUpdate.getId()); // new first element

		for (Object item : items) {
			Long itemId = (Long) item;
			freeCarBodyIdQueue.add(itemId);
		}
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

	public synchronized void updateCarMotorById(final Long id,
			CarMotor carMotorForUpdate) {
		final CarMotor carMotorToUpdate = getCarMotorById(id);
		carMotorToUpdate.setCarId(carMotorForUpdate.getCarId());
		carMotorToUpdate.setCarPartType(carMotorForUpdate.getCarPartType());
		carMotorToUpdate.setMotorType(carMotorForUpdate.getMotorType());
		carMotorToUpdate.setDefect(carMotorForUpdate.isDefect());
		carMotorToUpdate.setFree(carMotorForUpdate.isFree());
		carMotorToUpdate.setOrderId(carMotorForUpdate.getOrderId());
		carMotorToUpdate.setProducerId(carMotorForUpdate.getProducerId());

		deleteCarMotorById(id);

		freeCarMotors.putIfAbsent(carMotorToUpdate.getId(), carMotorToUpdate);

		// ADD CAR MOTOR IN FRONT OF ALL OTHERs since already used and should be
		// the oldest
		Object[] items = freeCarMotorIdQueue.toArray();
		freeCarMotorIdQueue.clear();
		freeCarMotorIdQueue.add(carMotorToUpdate.getId()); // new first element

		for (Object item : items) {
			Long itemId = (Long) item;
			freeCarMotorIdQueue.add(itemId);
		}
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

	public synchronized void updateCarTireById(final Long id,
			CarTire carTireForUpdate) {
		final CarTire carTireToUpdate = getCarTireById(id);
		carTireToUpdate.setCarId(carTireForUpdate.getCarId());
		carTireToUpdate.setCarPartType(carTireForUpdate.getCarPartType());
		carTireToUpdate.setDefect(carTireForUpdate.isDefect());
		carTireToUpdate.setFree(carTireForUpdate.isFree());
		carTireToUpdate.setOrderId(carTireForUpdate.getOrderId());
		carTireToUpdate.setProducerId(carTireForUpdate.getProducerId());

		deleteCarTireById(id);

		freeCarTires.putIfAbsent(carTireToUpdate.getId(), carTireToUpdate);

		// ADD CAR TIRE IN FRONT OF ALL OTHERs since already used and should be
		// the oldest
		Object[] items = freeCarTireIdQueue.toArray();
		freeCarTireIdQueue.clear();
		freeCarTireIdQueue.add(carTireToUpdate.getId()); // new first element

		for (Object item : items) {
			Long itemId = (Long) item;
			freeCarTireIdQueue.add(itemId);
		}
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

	public synchronized void updateFreeCarMotorById(final Long id,
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

	public void saveFreeCarBodyForOrder(final CarBody carBody) {
		freeCarBodysForOrder.putIfAbsent(carBody.getId(), carBody);
		freeCarBodyIdForOrderQueue.add(carBody.getId());
	}

	public Set<CarBody> getAllFreeCarBodysForOrder() {
		return (Set<CarBody>) Collections.synchronizedCollection(carBodys
				.values());
	}

	public CarBody getFreeCarBodyForOrderById(final Long id) {
		return carBodys.get(id);
	}

	public void deleteFreeCarBodyForOrderById(final Long id) {
		carBodys.remove(id);
	}

	public void saveFreeCarMotorForOrder(final CarMotor carMotor) {
		freeCarMotorsForOrder.putIfAbsent(carMotor.getId(), carMotor);
		freeCarMotorIdForOrderQueue.add(carMotor.getId());
	}

	public Set<CarMotor> getAllFreeCarMotorsForOrder() {
		return (Set<CarMotor>) Collections
				.synchronizedCollection(freeCarMotorsForOrder.values());
	}

	public CarMotor getFreeCarMotorForOrderById(final Long id) {
		return freeCarMotorsForOrder.get(id);
	}

	public void deleteFreeCarMotorForOrderById(final Long id) {
		freeCarMotorsForOrder.remove(id);
	}

	public void saveFreeCarTireForOrder(final CarTire carTire) {
		freeCarTiresForOrder.putIfAbsent(carTire.getId(), carTire);
		freeCarTireIdForOrderQueue.add(carTire.getId());
	}

	public Set<CarTire> getAllFreeCarTiresForOrder() {
		return (Set<CarTire>) Collections
				.synchronizedCollection(freeCarTiresForOrder.values());
	}

	public CarTire getFreeCarTireForOrderById(final Long id) {
		return freeCarTiresForOrder.get(id);
	}

	public void deleteFreeCarTireForOrderById(final Long id) {
		freeCarTiresForOrder.remove(id);
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
			if (it.hasNext())
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

	// public boolean checkIfSingleBodyPainterJob() {
	// Long nextCarBodyId = freeCarBodyIdQueue.peek();
	// CarBody nextCarBody = null;
	//
	// if (nextCarBodyId != null) {
	// nextCarBody = freeCarBodys.get(nextCarBodyId);
	// }
	//
	// if (nextCarBody != null) {
	// if(!nextCarBody.isPainted() || nextCarBody.getColor() == null)
	// return true;
	// }
	//
	// return false;
	// }
	// private String generateNextCarId() {
	// return Long.toString(NEXT_CAR_ID.getAndIncrement());
	// }

	public synchronized CarBody getSingleBodyPainterJob() {
		Long nextCarBodyId = null;
		CarBody nextCarBody = null;
		boolean foundCarBodyNotPainted = false;

		Iterator<Long> it = freeCarBodyIdQueue.iterator();

		while (it.hasNext()) {
			nextCarBodyId = it.next();

			if (nextCarBodyId != null) {
				nextCarBody = freeCarBodys.get(nextCarBodyId);
			}

			if (nextCarBody != null) {
				if (!nextCarBody.isPainted() || nextCarBody.getColor() == null) {
					freeCarBodyIdQueue.remove(nextCarBodyId);
					freeCarBodys.remove(nextCarBodyId);
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

	/**
	 * returns updated carParts which are assigned now to Order!
	 * @param order
	 * @return
	 */
	public synchronized List<CarPart> assignFreeCarPartsOrder(Order order) {
		List<CarPart> assignedCarParts = new ArrayList<CarPart>();

		// CHECK for each carPart all the lists and assign carParts to order

		if (order.requiresCarTire()) {
			Iterator<Long> it = freeCarTireIdQueue.iterator();

			while (it.hasNext()) {
				Long id = it.next();

				CarTire carTire = freeCarTires.get(id);
				if (carTire != null) {

					if (order.addCarTire(carTire)) {

						freeCarTireIdQueue.remove(id);
						freeCarTires.remove(id);

						carTire.setOrderId(order.getId());
						assignedCarParts.add(carTire);
						freeCarTiresForOrder.putIfAbsent(carTire.getId(),
								carTire);
						freeCarTireIdForOrderQueue.add(carTire.getId());
					}

					// if no more carTire required break!
					if (!order.requiresCarTire())
						break;

				}
			}

		}

		if (order.requiresCarMotor()) {
			Iterator<Long> it = freeCarMotorIdQueue.iterator();

			// check every carMotor and add to OrderQueue if needed by Order
			while (it.hasNext()) {
				Long id = it.next();

				CarMotor carMotor = freeCarMotors.get(id);
				if (carMotor != null) {
					// check if right motor
					if (order.requiresCarMotor(carMotor.getMotorType())) {

						if (order.addCarMotor(carMotor)) {

							freeCarMotorIdQueue.remove(id);
							freeCarMotors.remove(id);

							carMotor.setOrderId(order.getId());

							assignedCarParts.add(carMotor);
							freeCarMotorsForOrder.putIfAbsent(carMotor.getId(),
									carMotor);
							freeCarMotorIdForOrderQueue.add(carMotor.getId());
						}

						// if no more carMotor required break!
						if (!order.requiresCarMotor())
							break;

					}
				}
			}
		}

		if (order.requiresCarBody()) {
			Iterator<Long> it = freeCarBodyIdQueue.iterator();

			while (it.hasNext()) {
				Long id = it.next();

				CarBody carBody = freeCarBodys.get(id);
				if (carBody != null) {
					// check if colored and if yes if color needed by order!
					if (order.requiresCarBody(carBody.getColor())) {

						if (order.addCarBody(carBody)) {
							freeCarBodyIdQueue.remove(id);

							freeCarBodys.remove(id);

							carBody.setOrderId(order.getId());

							if (!carBody.isPainted()) {
								// if not painted request COLOR (mark Body with
								// color)!
								carBody.setRequestedColorByOrder(order
										.getCarColor());
							}

							assignedCarParts.add(carBody);
							freeCarBodysForOrder.putIfAbsent(carBody.getId(),
									carBody);
							freeCarBodyIdForOrderQueue.add(carBody.getId());
						}

						// if no more carbodys required break!
						if (!order.requiresCarBody())
							break;

					}
				}
			}
		}

		return  assignedCarParts;

	}

	public synchronized CarBody getSingleBodyPainterJobByOrdes() {
		Long nextCarBodyId = null;
		CarBody nextCarBody = null;
		boolean foundCarBodyNotPainted = false;

		Iterator<Long> it = freeCarBodyIdForOrderQueue.iterator();

		while (it.hasNext()) {
			nextCarBodyId = it.next();

			if (nextCarBodyId != null) {
				nextCarBody = freeCarBodysForOrder.get(nextCarBodyId);
			}

			if (nextCarBody != null) {
				if (!nextCarBody.isPainted() || nextCarBody.getColor() == null) {
					freeCarBodyIdForOrderQueue.remove(nextCarBodyId);
					freeCarBodysForOrder.remove(nextCarBodyId);
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
