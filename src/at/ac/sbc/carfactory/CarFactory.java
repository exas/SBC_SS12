package at.ac.sbc.carfactory;

import at.ac.sbc.carfactory.application.CarFactoryManager;
import at.ac.sbc.carfactory.application.ICarFactoryManager;
import at.ac.sbc.carfactory.ui.CarFactoryUI;

public class CarFactory {

	public static void main(String[] args) {
		// TODO: start UI and initialize logic layer
		ICarFactoryManager carFactoryManager = new CarFactoryManager();
		CarFactoryUI ui = new CarFactoryUI(carFactoryManager);
		
		carFactoryManager.addLogListener(ui);
		//new CarFactoryUI(null);
		
		//carFactoryManager.shutdown();
	}
}
