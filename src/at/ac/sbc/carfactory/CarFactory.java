package at.ac.sbc.carfactory;

import at.ac.sbc.carfactory.application.CarFactoryManager;
import at.ac.sbc.carfactory.domain.CarPartEnum;
import at.ac.sbc.carfactory.ui.CarFactoryUI;

public class CarFactory {

	public static void main(String[] args) {
		// TODO: start UI and initialize logic layer
		//new CarFactoryUI();
		CarFactoryManager carFactoryManager = new CarFactoryManager();
		
		carFactoryManager.createProducer(2, CarPartEnum.CAR_MOTOR);
		try {
			Thread.sleep(25000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		carFactoryManager.shutdown();
	}
}
