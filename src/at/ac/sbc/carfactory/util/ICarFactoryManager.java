package at.ac.sbc.carfactory.util;

import at.ac.sbc.carfactory.domain.CarPartType;

public interface ICarFactoryManager {

	public long createProducer();
	
	public long createProducer(int numParts, CarPartType carPart);
	
	public boolean assignWorkToProducer(int numParts, CarPartType carPart, long producerID);
	
	public boolean deleteProducer(long id);
	
	public boolean shutdownProducer(long id);
	
	public boolean shutdown();
	
	public void log(String message);
	
	public void addLogListener(LogListener listener);
	
	public void addDomainListener(DomainListener listener);
}
