package at.ac.sbc.carfactory.jms.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozartspaces.core.TransactionReference;

import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.util.CarFactoryException;
import at.ac.sbc.carfactory.xvms.util.ConfigSettings;
import at.ac.sbc.carfactory.xvms.util.CoordinatorType;
import at.ac.sbc.carfactory.xvms.util.SpaceUtil;
import at.ac.sbc.carfactory.xvms.util.WorkTaskLabel;

public class Logistician extends Worker {
	
	private SpaceUtil space;

	public Logistician(long id) {
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
			Logger.getLogger(Logistician.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
		}
	}
	
	public void processCar() {
		TransactionReference tx;
		try {
			tx = this.space.createTransaction();
		} catch (CarFactoryException ex) {
			// COULD NOT CREATE TRANSACTION:
			Logger.getLogger(Assembler.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
			return;
		}
		try {
			ArrayList<Serializable> carE = this.space.readEntry(this.space.lookupContainer(ConfigSettings.containerFinishedCarsName), CoordinatorType.FIFO, null, 1, tx, false);
			if(carE != null && carE.size() > 0) {
				Car car = (Car)carE.get(0);
				car.setLogisticWorkerId(this.getId());
				this.space.writeLabelEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), car, WorkTaskLabel.CAR_FINISHED);
			}
		} catch (CarFactoryException ex) {
			ex.printStackTrace();
			Logger.getLogger(Logistician.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
			try {
				this.space.rollbackTransaction(tx);
			} catch (CarFactoryException e) {
				Logger.getLogger(Logistician.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
				e.printStackTrace();
			}
		}	
	}
	
	public static void main(String[] args) {
		 if(args.length != 1) {
			 Logger.getLogger(Logistician.class.getName()).log(Level.INFO, "Provide ID as argument", "Provide ID as argument");
			 System.exit(-1);
		 }
		 Long id = null;
		 try {
			 id = Long.parseLong(args[0]);
		 } catch (Exception ex) {
			 Logger.getLogger(Logistician.class.getName()).log(Level.INFO, "Provide ID as argument", "Provide ID as argument");
			 System.exit(-1);
		 }
		 
		 Logistician logistician = new Logistician(id);
		 while(true) {
			logistician.processCar();
		 }
	}

}
