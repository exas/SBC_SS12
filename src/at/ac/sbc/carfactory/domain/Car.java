package at.ac.sbc.carfactory.domain;

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Queryable;

@Queryable
public class Car implements Serializable {

	private static final long serialVersionUID = -5463318055982788409L;
	private Long id;
	private Long assemblyWorkerId;

	@Index(label="painted")
	private Long painterWorkerId;

	private Long logisticWorkerId;

	private CarBody body;
	private CarMotor motor;
	private List<CarTire> tires;

	private Boolean isDefect = null;

	private Long testerAllPartsAssembledWorkerId;
	private Long testerIsDefectWorkerId;

	private Long orderId;

	private Boolean isTestingFinished = null;

	@Index(label="type")
	private CarPartType carPartType;

	public Car() {
		this.setCarPartType(CarPartType.CAR);
		this.painterWorkerId = null;
		this.logisticWorkerId = null;
	}

	public Car(CarBody body, CarMotor motor, List<CarTire> tires) {
		super();
		this.setCarPartType(CarPartType.CAR);
		this.body = body;
		this.motor = motor;
		this.tires = tires;
		this.painterWorkerId = null;
		this.logisticWorkerId = null;
		this.orderId = body.orderId;
	}

	public Car(Long assemblyWorkerId, CarBody body, CarMotor motor, List<CarTire> tires) {
		super();
		this.body = body;
		this.motor = motor;
		this.tires = tires;
		this.assemblyWorkerId = assemblyWorkerId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CarBody getBody() {
		return body;
	}

	public void setBody(CarBody body) {
		this.body = body;
	}

	public CarMotor getMotor() {
		return motor;
	}

	public void setMotor(CarMotor motor) {
		this.motor = motor;
	}

	public List<CarTire> getTires() {
		return tires;
	}

	public void setTires(List<CarTire> tires) {
		this.tires = tires;
	}

	public Long getAssemblyWorkerId() {
		return assemblyWorkerId;
	}

	public void setAssemblyWorkerId(Long assemblyWorkerId) {
		this.assemblyWorkerId = assemblyWorkerId;
	}

	public Long getPainterWorkerId() {
		return painterWorkerId;
	}

	public void setPainterWorkerId(Long painterWorkerId) {
		this.painterWorkerId = painterWorkerId;
	}

	public Long getLogisticWorkerId() {
		return logisticWorkerId;
	}

	public void setLogisticWorkerId(Long logisticWorkerId) {
		this.logisticWorkerId = logisticWorkerId;
	}

	public Boolean isDefect() {
		return isDefect;
	}

	public void setDefect(Boolean isDefect) {
		this.isDefect = isDefect;
	}

	public CarPartType getCarPartType() {
		return carPartType;
	}

	public void setCarPartType(CarPartType carPartType) {
		this.carPartType = carPartType;
	}

	public Long getTesterAllPartsAssembledWorkerId() {
		return testerAllPartsAssembledWorkerId;
	}

	public void setTesterAllPartsAssembledWorkerId(
			Long testerAllPartsAssembledWorkerId) {
		this.testerAllPartsAssembledWorkerId = testerAllPartsAssembledWorkerId;
	}

	public Long getTesterIsDefectWorkerId() {
		return testerIsDefectWorkerId;
	}

	public void setTesterIsDefectWorkerId(Long testerIsDefectWorkerId) {
		this.testerIsDefectWorkerId = testerIsDefectWorkerId;
	}

	public Boolean isTestingFinished() {
		return isTestingFinished;
	}

	public void setTestingFinished(Boolean isTestingFinished) {
		this.isTestingFinished = isTestingFinished;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
}
