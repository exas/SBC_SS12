package at.ac.sbc.carfactory.backend;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import at.ac.sbc.carfactory.domain.Car;

public class CarDaoSimpleImpl {
	private static final CarDaoSimpleImpl INSTANCE = new CarDaoSimpleImpl();

	private final ConcurrentMap<Long, Car> cars = new ConcurrentHashMap<Long, Car>();
	private static final AtomicLong NEXT_CAR_ID = new AtomicLong(1);

	private CarDaoSimpleImpl() {
	}

	public static CarDaoSimpleImpl getInstance() { return INSTANCE; }

	public void saveCarToAssemble(final Car car) {
		final Long id = generateNextCarId();
        car.setId(id);
		cars.putIfAbsent(car.getId(), car);
	}

	public Set<Car> getAllCarsToAssemble() {
        return (Set<Car>) Collections.synchronizedCollection(cars.values());
    }

	public Car getCarToAssembleById(final Long id) {
	    return cars.get(id);
	}

	public void updateCarToAssembleById(final Long id,
	                               final Car carForUpdate) {
	    final Car carToUpdate = getCarToAssembleById(id);
	    //TODO update fields
	}

	public void deleteCarToAssembleById(final Long id) { cars.remove(id); }

	private Long generateNextCarId() {
	    return NEXT_CAR_ID.getAndIncrement();
	}
}
