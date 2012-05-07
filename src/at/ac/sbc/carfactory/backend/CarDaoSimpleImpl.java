package at.ac.sbc.carfactory.backend;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import at.ac.sbc.carfactory.domain.Car;

public class CarDaoSimpleImpl {
	private static final CarDaoSimpleImpl INSTANCE = new CarDaoSimpleImpl();
	
	private final ConcurrentMap<Long, Car> assembledCars = new ConcurrentHashMap<Long, Car>();
	private final ConcurrentMap<Long, Car> finishedCars = new ConcurrentHashMap<Long, Car>();
	//private static final AtomicLong NEXT_CAR_ID = new AtomicLong(1);
	
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
	    //update fields
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
	    //update fields
	}
	
	public void deleteFinishedCarById(final Long id) { finishedCars.remove(id); }
    
//	private String generateNextCarId() {
//	    return Long.toString(NEXT_CAR_ID.getAndIncrement());
//	}
}
