package at.ac.sbc.carfactory.jms.dto;

import java.util.ArrayList;
import java.util.List;

import at.ac.sbc.carfactory.util.TestCase;


//Data Transfer Object, just a simple class which holds all relevant data for transfering this object via Messaging/Queues.

public class CarDTO implements java.io.Serializable{

	private static final long serialVersionUID = 1L;

	public Long id = null;

	public CarPartDTO carBody = null;
	public CarPartDTO carMotor = null;
	public List<CarPartDTO> carTires = null;

	public Long assemblyWorkerId = null;
	public Long logisticWorkerId = null;

	public Long testerAllPartsAssembledWorkerId = null;
	public Long testerIsDefectWorkerId = null;

	public Boolean isDefect = null;
	public Long orderId = null;

	public List<TestCase> testCases = new ArrayList<TestCase>();

	public boolean isTestingFinished = false;

	public CarDTO() {
		this.id = null;

		this.carBody = null;
		this.carMotor = null;
		this.carTires = null;

		this.assemblyWorkerId = null;
		this.logisticWorkerId = null;
		this.isDefect = null;
		this.orderId = null;
	}
}
