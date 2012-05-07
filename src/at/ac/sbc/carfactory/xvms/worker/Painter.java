package at.ac.sbc.carfactory.xvms.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozartspaces.core.TransactionReference;

import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.util.CarFactoryException;
import at.ac.sbc.carfactory.xvms.util.ConfigSettings;
import at.ac.sbc.carfactory.xvms.util.CoordinatorType;
import at.ac.sbc.carfactory.xvms.util.SpaceUtil;
import at.ac.sbc.carfactory.xvms.util.WorkTaskLabel;

public class Painter extends Worker {
	
	private SpaceUtil space;
	private List<WorkTaskLabel> labels;
	private CarColor color;
	
	public Painter(long id, CarColor color) {
		super(id);
		this.color = color;
		this.labels = Arrays.asList(WorkTaskLabel.CAR_BODY, WorkTaskLabel.CAR);
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
	
	public void processCarBody() {
		TransactionReference tx;
		try {
			tx = this.space.createTransaction();
		} catch (CarFactoryException ex) {
			// COULD NOT CREATE TRANSACTION:
			Logger.getLogger(Painter.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
			return;
		}
		try {
			ArrayList<Serializable> elems = this.space.readEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), CoordinatorType.LABEL, labels, 1, tx, false);
			System.out.println("GOT SOMETHING: " + elems);
			if(elems != null && elems.size() > 0) {
				System.out.println(elems.get(0).toString());
			}
		} catch (CarFactoryException ex) {
			Logger.getLogger(Painter.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
			try {
				this.space.rollbackTransaction(tx);
			} catch (CarFactoryException e) {
				Logger.getLogger(Painter.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		 if(args.length != 2) {
			 Logger.getLogger(Painter.class.getName()).log(Level.INFO, "Provide ID and Color as argument", "Provide ID as argument");
			 System.exit(-1);
		 }
		 Long id = null;
		 try {
			 id = Long.parseLong(args[0]);
		 } catch (Exception ex) {
			 Logger.getLogger(Painter.class.getName()).log(Level.INFO, "Provide ID and Color as argument", "Provide ID as argument");
			 System.exit(-2);
		 }
		 
		 CarColor color = null;
		 if(args[1].equalsIgnoreCase("red")) {
			 color = CarColor.RED;
		 }
		 else if(args[1].equalsIgnoreCase("black")) {
			 color = CarColor.BLACK;
		 }
		 else if(args[1].equalsIgnoreCase("blue")) {
			 color = CarColor.BLUE;
		 }
		 else if(args[1].equalsIgnoreCase("green")) {
			 color = CarColor.GREEN;
		 }
		 else {
			 color = CarColor.WHITE;
		 }
		 
		 Painter painter = new Painter(id, color);
		 while(true) {
			 painter.processCarBody();
		 }
	}

}
