package at.ac.sbc.carfactory.application;

/**
 * class handles connection to space as well as storing information about
 * the overall facility. Furthermore it creates the producer-threads
 * 
 * @author spookyTU
 *
 */
public class CarFactoryManager {
	
	public CarFactoryManager() {
		
	}
	
	public void createProducer() {
		Producer producer = new Producer();
		producer.run();
	}

}
