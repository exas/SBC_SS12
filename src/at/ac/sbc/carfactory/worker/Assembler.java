package at.ac.sbc.carfactory.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozartspaces.core.TransactionReference;

import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarTire;
import at.ac.sbc.carfactory.xvms.util.CarFactoryException;
import at.ac.sbc.carfactory.xvms.util.ConfigSettings;
import at.ac.sbc.carfactory.xvms.util.CoordinatorType;
import at.ac.sbc.carfactory.xvms.util.SpaceUtil;
import at.ac.sbc.carfactory.xvms.util.WorkTaskLabel;

public class Assembler extends Worker {

	private SpaceUtil space;
	
	public Assembler(long id) {
		super(id);
		this.initSpace();
	}
		
	private void initSpace() {
		try {
			this.space = new SpaceUtil();
			if(this.space.lookupContainer(ConfigSettings.containerCarPartsName) == null) {
				this.space.createContainer(ConfigSettings.containerCarPartsName, CoordinatorType.LABEL);
			}
			if(this.space.lookupContainer(ConfigSettings.containerFinishedCarsName) == null) {
				this.space.createContainer(ConfigSettings.containerFinishedCarsName, CoordinatorType.FIFO);
			}
		} catch (CarFactoryException ex) {
			Logger.getLogger(Painter.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
		}
	}
	
	public void buildCar() {
		TransactionReference tx;
		try {
			tx = this.space.createTransaction();
		} catch (CarFactoryException ex) {
			// COULD NOT CREATE TRANSACTION:
			Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
			return;
		}
		try {
			ArrayList<Serializable> bodyE = this.space.readEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), CoordinatorType.LABEL, Arrays.asList(WorkTaskLabel.CAR, WorkTaskLabel.CAR_BODY), 1, tx, false);
			ArrayList<Serializable> motorE = this.space.readEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), CoordinatorType.LABEL, Arrays.asList(WorkTaskLabel.CAR_MOTOR), 1, tx, true);
			ArrayList<Serializable> tiresE = this.space.readEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), CoordinatorType.LABEL, Arrays.asList(WorkTaskLabel.CAR_TIRE), 4, tx, true);
			
			if ((bodyE == null || bodyE.size() == 0) || (motorE == null || motorE.size() == 0) || (tiresE == null || tiresE.size() != 4)) {
				System.out.println("NOT ALL PARTS FOUND");
				this.space.rollbackTransaction(tx);
				return;
			}
			System.out.println("BEFORE CREATING CAR");
			CarBody body = (CarBody)bodyE.get(0);
			CarMotor motor = (CarMotor)motorE.get(0);
			
			ArrayList<CarTire> tires = new ArrayList<CarTire>();
			for(Serializable tire : tiresE) {
				tires.add((CarTire)tire);
			}
			Car car = new Car(body, motor, tires);
			
			if (car.getBody().isPainted() == true) {
				this.space.writeFinalCar(this.space.lookupContainer(ConfigSettings.containerFinishedCarsName), car, tx);
			}
			else {
				this.space.writeCarPartEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), car, WorkTaskLabel.CAR);
			}
			this.space.commitTransaction(tx);
			System.out.println("DONE WRITING CAR");
		} catch (CarFactoryException ex) {
			ex.printStackTrace();
			Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
			try {
				this.space.rollbackTransaction(tx);
			} catch (CarFactoryException e) {
				Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		 if(args.length != 1) {
			 Logger.getLogger(Assembler.class.getName()).log(Level.INFO, "Provide ID as argument", "Provide ID as argument");
			 System.exit(-1);
		 }
		 Long id = null;
		 try {
			 id = Long.parseLong(args[0]);
		 } catch (Exception ex) {
			 Logger.getLogger(Assembler.class.getName()).log(Level.INFO, "Provide ID as argument", "Provide ID as argument");
			 System.exit(-1);
		 }
		 
		 Assembler assembler = new Assembler(id);
		 while(true) {
			assembler.buildCar();
			break;
		 }
	}

}
