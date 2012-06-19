package at.ac.sbc.carfactory.util;

import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.domain.CarMotorType;

import at.ac.sbc.carfactory.domain.Order;

import at.ac.sbc.carfactory.domain.CarPartType;

public interface ICarFactoryManager {

	public long createProducer();

	public Order createOrder(Integer carAmount, CarMotorType carMotorType, CarColor carColor);

	public long createProducer(int numParts, Double errorRate, CarPartType carPart);

	public boolean assignWorkToProducer(int numParts, Double errorRate, CarPartType carPart, long producerID);

	public boolean deleteProducer(long id);

	public boolean shutdownProducer(long id);

	public boolean shutdown();

	public void log(String message);

	public void addLogListener(LogListener listener);

	public void addDomainListener(DomainListener listener);
}
