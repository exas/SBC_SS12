package at.ac.sbc.carfactory.util;

import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.domain.CarPart;

public interface DomainListener {

	public void carPartUpdated(CarPart part, boolean finshed);
	
	public void carUpdated(Car car);
}
