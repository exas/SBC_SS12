package at.ac.sbc.carfactory.jms.worker;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Painter extends Worker {
	
	public Painter(long id) {
		super(id);
	}
	
	public void connectToSpace() {
		
	}

	public static void main(String[] args) {
		 if(args.length != 1) {
			 Logger.getLogger(Painter.class.getName()).log(Level.INFO, "Provide ID as argument", "Provide ID as argument");
			 System.exit(-1);
		 }
		 Long id = null;
		 try {
			 id = Long.parseLong(args[0]);
		 } catch (Exception ex) {
			 Logger.getLogger(Painter.class.getName()).log(Level.INFO, "Provide ID as argument", "Provide ID as argument");
			 System.exit(-1);
		 }
		 
		 new Painter(id);
	}

}
