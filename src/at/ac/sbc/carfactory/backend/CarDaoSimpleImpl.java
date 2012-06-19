package at.ac.sbc.carfactory.backend;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import at.ac.sbc.carfactory.domain.Car;

public class CarDaoSimpleImpl {
	private static final CarDaoSimpleImpl INSTANCE = new CarDaoSimpleImpl();

	private final ConcurrentMap<Long, Car> carsToAssemble = new ConcurrentHashMap<Long, Car>();
	private final ConcurrentMap<Long, Car> assembledCars = new ConcurrentHashMap<Long, Car>();
	private final ConcurrentMap<Long, Car> finishedCars = new ConcurrentHashMap<Long, Car>();
	private static final AtomicLong NEXT_CAR_ID = new AtomicLong(1);

	private CarDaoSimpleImpl() {
	}

	public static CarDaoSimpleImpl getInstance() { return INSTANCE; }

	public void saveAssembledCar(final Car car) {

		assembledCars.putIfAbsent(car.getId(), car);
	}

	public Set<Car> getAllAssembledCars() {
        return (Set<Car>) Collections.synchronizedCollection(assembledCars.values());
    }

	public Car getAssembledCarById(final Long id) {
	    return assembledCars.get(id);
	}

	public void updateAssembledCarById(final Long id,
	                               final Car carForUpdate) {
	    final Car carToUpdate = getAssembledCarById(id);
	    //TODO update fields
	}

	public void deleteAssembledCarById(final Long id) { assembledCars.remove(id); }

	public void saveFinishedCar(final Car car) {
		finishedCars.putIfAbsent(car.getId(), car);
	}

	public Set<Car> getAllFinishedCars() {
        return (Set<Car>) Collections.synchronizedCollection(finishedCars.values());
    }

	public Car getFinishedCarById(final Long id) {
	    return finishedCars.get(id);
	}

	public void updateFinishedCarById(final Long id,
	                               final Car carForUpdate) {
	    final Car carToUpdate = getFinishedCarById(id);
	    //TODO update fields
	}

	public void deleteFinishedCarById(final Long id) { finishedCars.remove(id); }

	public void saveCarToAssemble(final Car car) {
		final Long id = generateNextCarId();
        car.setId(id);
		carsToAssemble.putIfAbsent(car.getId(), car);
	}

	public Set<Car> getAllCarsToAssemble() {
        return (Set<Car>) Collections.synchronizedCollection(carsToAssemble.values());
    }

	public Car getCarToAssembleById(final Long id) {
	    return carsToAssemble.get(id);
	}

	public void updateCarToAssembleById(final Long id,
	                               final Car carForUpdate) {
	    final Car carToUpdate = getCarToAssembleById(id);
	    //TODO update fields
	}

	public void deleteCarToAssembleById(final Long id) { carsToAssemble.remove(id); }

	private Long generateNextCarId() {
	    return NEXT_CAR_ID.getAndIncrement();
	}
}
