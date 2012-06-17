package at.ac.sbc.carfactory.xvms.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozartspaces.capi3.Matchmakers;
import org.mozartspaces.capi3.Property;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.core.TransactionReference;

import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.domain.CarPart;
import at.ac.sbc.carfactory.domain.CarPartType;
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
		} catch (CarFactoryException ex) {
			Logger.getLogger(Painter.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
		}
	}
	
	public void paintCar() {
		Query query = new Query().filter(Matchmakers.and((Matchmakers.or(Property.forName("type").equalTo(CarPartType.CAR), Property.forName("type").equalTo(CarPartType.CAR_BODY))), Property.forName("painted").equalTo(null)));
		TransactionReference tx;
 		try {
 			tx = this.space.createTransaction();
 		} catch (CarFactoryException ex) {
 			// COULD NOT CREATE TRANSACTION:
 			Logger.getLogger(Painter.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
 			return;
 		}
 		try {
 			this.labels = Arrays.asList(WorkTaskLabel.CAR);
 			ArrayList<Serializable> elems = this.space.readQueryEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), query, tx, false);
 			System.out.println("GOT SOMETHING: " + elems);
 			if(elems != null && elems.size() > 0) {
 				Serializable obj = null;
 				if (elems.get(0) instanceof CarPart) {
 					obj = (CarBody)elems.get(0);
 					((CarBody)obj).setPainterWorkerId(this.getId());
 					((CarBody)obj).setColor(this.color);
 					//this.space.writeLabelEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), body, WorkTaskLabel.CAR_BODY_PAINTED);
 					this.space.writeEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), obj, tx, Arrays.asList(CoordinatorType.LABEL, CoordinatorType.QUERY), null, WorkTaskLabel.CAR_BODY_PAINTED);
 				}
 				else {
 					obj = (Car)elems.get(0);
 	 				((Car)obj).getBody().setPainterWorkerId(this.getId());
 	 				((Car)obj).getBody().setColor(this.color);
 	 				this.space.writeFinalCar(this.space.lookupContainer(ConfigSettings.containerFinishedCarsName), (Car)obj, tx);
 				}
 				if (obj instanceof CarPart) {
 					System.out.println("Painte " + this.getId() + " painted CarBody " + ((CarBody)obj).getId() + " with color " + this.color);
 				}
 				else {
 					System.out.println("Painte " + this.getId() + " painted Car " + ((Car)obj).getId() + " with color " + this.color);
 				}
 				this.space.commitTransaction(tx);
 			}
 		} catch (CarFactoryException ex) {
 			Logger.getLogger(Painter.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
 			try {
 				this.space.rollbackTransaction(tx);
 			} catch (CarFactoryException e) {
 				Logger.getLogger(Painter.class.getName()).log(Level.SEVERE, "CarFactoryException", ex.getMessage());
 				e.printStackTrace();
 			}
 		} catch (Exception ex) {
 			Logger.getLogger(Painter.class.getName()).log(Level.SEVERE, "Exception", ex.getMessage());
			System.exit(-1);
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
			this.labels = Arrays.asList(WorkTaskLabel.CAR);
			ArrayList<Serializable> elems = this.space.readEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), CoordinatorType.LABEL, labels, 1, tx, true);
			System.out.println("GOT SOMETHING: " + elems);
			if(elems != null && elems.size() > 0) {
				Car car = (Car)elems.get(0);
				car.getBody().setPainterWorkerId(this.getId());
				car.getBody().setColor(this.color);
				this.space.writeFinalCar(this.space.lookupContainer(ConfigSettings.containerFinishedCarsName), car, tx);
				this.space.commitTransaction(tx);
			}
			else {
				this.labels = Arrays.asList(WorkTaskLabel.CAR_BODY);
				elems = this.space.readEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), CoordinatorType.LABEL, labels, 1, tx, true);
				if(elems != null && elems.size() > 0) {
					CarBody body = (CarBody)elems.get(0);
					body.setPainterWorkerId(this.getId());
					body.setColor(this.color);
					this.space.writeLabelEntry(this.space.lookupContainer(ConfigSettings.containerCarPartsName), body, WorkTaskLabel.CAR_BODY_PAINTED);
					this.space.commitTransaction(tx);
				}
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
			 //painter.paintCar();
			 painter.processCarBody();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	}

}
