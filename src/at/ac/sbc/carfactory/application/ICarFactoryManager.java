package at.ac.sbc.carfactory.application;

import at.ac.sbc.carfactory.domain.CarPartEnum;
import at.ac.sbc.carfactory.util.LogListener;

public interface ICarFactoryManager {

	public long createProducer();
	
	public long createProducer(int numParts, CarPartEnum carPart);
	
	public boolean assignWorkToProducer(int numParts, CarPartEnum carPart, long producerID);
	
	public boolean deleteProducer(long id);
	
	public boolean shutdownProducer(long id);
	
	public boolean shutdown();
	
	public void log(String message);
	
	public void addLogListener(LogListener listener);
}
