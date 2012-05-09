package at.ac.sbc.carfactory.jms.worker;


import java.util.Scanner;

import org.apache.log4j.Logger;


public class Assembler extends Worker{

	private final static Logger logger = Logger.getLogger(Assembler.class);
	@SuppressWarnings("unused")
	private AssemblerListener assembleListener;
	
	public Assembler(long id) {
		super(id);
		this.assembleListener = new AssemblerListener(id);
	}
	
	public static void main(String[] args) {
		 if(args.length != 1) {
			 logger.error("Provide a numeric ID as argument");
			 System.exit(-1);
		 }
		 Long id = null;
		 try {
			 id = Long.parseLong(args[0]);
		 } catch (Exception ex) {
			 logger.error("Provide a numeric ID as argument");
			 System.exit(-1);
		 }
		 logger.info("Enter 'quit' to exit AssemblerWorker...");
		 
		@SuppressWarnings("unused")
		Assembler assembler = new Assembler(id);
		Scanner sc = new Scanner(System.in);
	    
		while(!sc.nextLine().equals("quit"));
	}



}
