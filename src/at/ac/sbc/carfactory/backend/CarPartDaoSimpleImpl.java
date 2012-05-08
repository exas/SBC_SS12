package at.ac.sbc.carfactory.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.xvsm.protocol.EntryValueList;

import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarTire;

public class CarPartDaoSimpleImpl {
	private static final CarPartDaoSimpleImpl INSTANCE = new CarPartDaoSimpleImpl();
	
	private final ConcurrentMap<Long, CarBody> carBodys = new ConcurrentHashMap<Long, CarBody>();
	private final ConcurrentMap<Long, CarMotor> carMotors = new ConcurrentHashMap<Long, CarMotor>();
	private final ConcurrentMap<Long, CarTire> carTires = new ConcurrentHashMap<Long, CarTire>();
	
	private final ConcurrentMap<Long, CarBody> freeCarBodys = new ConcurrentHashMap<Long, CarBody>();
	private final ConcurrentMap<Long, CarMotor> freeCarMotors = new ConcurrentHashMap<Long, CarMotor>();
	private final ConcurrentMap<Long, CarTire> freeCarTires = new ConcurrentHashMap<Long, CarTire>();
	
	
	//private static final AtomicLong NEXT_CAR_ID = new AtomicLong(1);
	
	private CarPartDaoSimpleImpl() {
	}
	
	public static CarPartDaoSimpleImpl getInstance() { return INSTANCE; }
	
	
	public void saveCarBody(final CarBody carBody) {
		carBodys.putIfAbsent(carBody.getId(), carBody);
	}
	
	public Set<CarBody> getAllCarBodys() {
        return (Set<CarBody>) Collections.synchronizedCollection(carBodys.values());
    }
	
	public CarBody getCarBodyById(final Long id) {
	    return carBodys.get(id);
	}
	
	public void updateCarBodyById(final Long id,
	                               final CarBody carBodyForUpdate) {
	    final CarBody carBodyToUpdate = getCarBodyById(id);
	    //update fields
	}
	
	public void deleteCarBodyById(final Long id) { carBodys.remove(id); }
	
	public void saveCarMotor(final CarMotor carMotor) {
		carMotors.putIfAbsent(carMotor.getId(), carMotor);
	}
	
	public Set<CarMotor> getAllCarMotors() {
        return (Set<CarMotor>) Collections.synchronizedCollection(carMotors.values());
    }
	
	public CarMotor getCarMotorById(final Long id) {
	    return carMotors.get(id);
	}
	
	public void updateCarMotorById(final Long id,
	                               final CarMotor carMotorForUpdate) {
	    final CarMotor carMotorToUpdate = getCarMotorById(id);
	    //update fields
	}
	
	public void deleteCarMotorById(final Long id) { carMotors.remove(id); }
	
	
	public void saveCarTire(final CarTire carTire) {
		carTires.putIfAbsent(carTire.getId(), carTire);
	}
	
	public Set<CarTire> getAllCarTires() {
        return (Set<CarTire>) Collections.synchronizedCollection(carTires.values());
    }
	
	public CarTire getCarTireById(final Long id) {
	    return carTires.get(id);
	}
	
	public void updateCarTireById(final Long id,
	                               final CarTire carTireForUpdate) {
	    final CarTire carBodyToUpdate = getCarTireById(id);
	    //update fields
	}
	
	public void deleteCarTireById(final Long id) { carTires.remove(id); }

	
	//free maps
	public void saveFreeCarBody(CarBody carBody) {
		freeCarBodys.putIfAbsent(carBody.getId(), carBody);
	}
	
	public Set<CarBody> getAllFreeCarBodys() {
        return (Set<CarBody>) Collections.synchronizedCollection(freeCarBodys.values());
    }
	
	public CarBody getFreeCarBodyById(final Long id) {
	    return freeCarBodys.get(id);
	}
	
	public void updateFreeCarBodyById(final Long id,
	                               final CarBody carBodyForUpdate) {
	    final CarBody carBodyToUpdate = getFreeCarBodyById(id);
	    //update fields
	}
	
	public void deleteFreeCarBodyById(final Long id) { freeCarBodys.remove(id); }
	
	
	public void saveFreeCarMotor(final CarMotor carMotor) {
		freeCarMotors.putIfAbsent(carMotor.getId(), carMotor);
	}
	
	public Set<CarMotor> getAllFreeCarMotors() {
        return (Set<CarMotor>) Collections.synchronizedCollection(freeCarMotors.values());
    }
	
	public CarMotor getFreeCarMotorById(final Long id) {
	    return freeCarMotors.get(id);
	}
	
	public void updateFreeCarMotorById(final Long id,
	                               final CarMotor carMotorForUpdate) {
	    final CarMotor carMotorToUpdate = getFreeCarMotorById(id);
	    //update fields
	}
	
	public void deleteFreeCarMotorById(final Long id) { freeCarMotors.remove(id); }
	
	
	public void saveFreeCarTire(final CarTire carTire) {
		freeCarTires.putIfAbsent(carTire.getId(), carTire);
	}
	
	public Set<CarTire> getAllFreeCarTires() {
        return (Set<CarTire>) Collections.synchronizedCollection(freeCarTires.values());
    }
	
	public CarTire getFreeCarTireById(final Long id) {
	    return freeCarTires.get(id);
	}
	
	public void updateFreeCarTireById(final Long id,
	                               final CarTire carTireForUpdate) {
	    final CarTire carBodyToUpdate = getFreeCarTireById(id);
	    //update fields
	}
	
	public void deleteFreeCarTireById(final Long id) { freeCarTires.remove(id); }
	
	public synchronized List<CarTire> getNextFreeCarTireSetAndRemove() {
		List<CarTire> carTireSet = new ArrayList<CarTire>();
		
		if (freeCarTires.isEmpty())
			return carTireSet; //return EMPTY LIST if nothing in it.
		
		for(Map.Entry<Long, CarTire> e : freeCarTires.entrySet()) {
			carTireSet.add(e.getValue());
			
			freeCarTires.remove(e.getKey());
			
			if(carTireSet.size() == 4)
				break;
			
		}
		
		return carTireSet;
	}
	
	public synchronized CarBody getNextFreeCarBodyAndRemove() {
		if (freeCarBodys.isEmpty())
			return null;
		
		// Get the first entry that the iterator returns
		Entry<Long, CarBody> entry = freeCarBodys.entrySet().iterator().next();
		
		CarBody carBody = entry.getValue();
		
		freeCarBodys.remove(entry.getKey());
		
		return carBody;
	}
	
	public synchronized CarMotor getNextFreeCarMotorAndRemove() {
		if (freeCarMotors.isEmpty())
			return null;
		
		// Get the first entry that the iterator returns
		Entry<Long, CarMotor> entry = freeCarMotors.entrySet().iterator().next();
		
		CarMotor carMotor = entry.getValue();
		
		freeCarMotors.remove(entry.getKey());
		
		return carMotor;
	}



	
	
//	private String generateNextCarId() {
//	    return Long.toString(NEXT_CAR_ID.getAndIncrement());
//	}
}
