package at.ac.sbc.carfactory.application;

import at.ac.sbc.carfactory.domain.CarPartEnum;
import at.ac.sbc.carfactory.util.LogListener;

public interface ICarFactoryManager {

	public void createProducer();
	
	public void createProducer(int numParts, CarPartEnum carPart);
	
	public void assignWorkToProducer(int numParts, CarPartEnum carPart, long producerID);
	
	public void deleteProducer(long id);
	
	public void shutdownProducer(long id);
	
	public void shutdown();
	
	public void log(String message);
	
	public void addLogListener(LogListener listener);
}
