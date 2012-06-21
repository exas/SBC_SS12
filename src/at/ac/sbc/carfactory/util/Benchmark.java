package at.ac.sbc.carfactory.util;

import java.util.Scanner;

import at.ac.sbc.carfactory.jms.worker.Tester;

import at.ac.sbc.carfactory.domain.CarColor;

import at.ac.sbc.carfactory.jms.worker.Painter;

import at.ac.sbc.carfactory.jms.worker.Logistician;

import at.ac.sbc.carfactory.jms.worker.Assembler;

import at.ac.sbc.carfactory.jms.server.JobManagementListener;

import at.ac.sbc.carfactory.jms.server.JMSServer;

public class Benchmark {
	private final static Thread mainThread = Thread.currentThread();
	private final static int timeToRun = 60000; // 1 minute;
	private static Tester tester1;
	private static Tester tester2;

	public static void main(String[] args){

		//start server
		final JMSServer jmsServer = JMSServer.getInstance();
		jmsServer.start();

//		@SuppressWarnings("unused")
//		JobManagementListener jobManagementListener = new JobManagementListener();

//		//start Worker inactive
		Assembler assembler1 = new Assembler((long)1);
//		Assembler assembler2 = new Assembler((long)2);

//		Painter painter1 = new Painter(1, CarColor.BLUE);
//		Painter painter2 = new Painter(2, CarColor.RED);
//
//		Logistician logistician = new Logistician(1);

		tester1 = new Tester((long)1, TestCaseType.CHECK_ALL_PARTS);
//		Tester tester2 = new Tester((long)2, TestCase.CHECK_DEFECT_PARTS);

		Thread t = new Thread(tester1);
		t.start();

		System.out.println("Benchmark Thread putting to sleep");


		Thread countDownThread = new Thread(new Runnable() {
				    @Override
					public void run() {
				        try {
							Thread.sleep(timeToRun);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        tester1.stopWorker();
				        jmsServer.stop();
				        System.exit(0);
				    }}
				);


		countDownThread.start();

		//assembler1.stopListening();
//		System.out.println("Benchmark Thread woke up ");
		//assembler1.stopWorker();

		//PRODUCE RESSOURCES


		//wait until all produced!



		//START all WORKERs
		//TODO check if startConnection or startListening necessary?
//		assembler1.startConnection();
//		assembler2.startConnection();
//
//		painter1.startListening();
//		painter2.startListening();
//
//		logistician.startListening();
//
//		tester1.startConnection();
//		tester2.startConnection();




		//BENCHMARK

		//TODO WAIT 60 sec or less and count finished cars!


		//STOP ALL WORKS
//		assembler1.stopListening();
//		assembler2.stopListening();
//		painter1.stopListening();
//		painter1.stopListening();
//		logistician.stopListening();
//		tester1.stopListening();
//		tester2.stopListening();

//		jmsServer.stop();
//
//		System.exit(0);
	}
}
