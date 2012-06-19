package at.ac.sbc.carfactory.util;

import at.ac.sbc.carfactory.jms.worker.Tester;

import at.ac.sbc.carfactory.domain.CarColor;

import at.ac.sbc.carfactory.jms.worker.Painter;

import at.ac.sbc.carfactory.jms.worker.Logistician;

import at.ac.sbc.carfactory.jms.worker.Assembler;

import at.ac.sbc.carfactory.jms.server.JobManagementListener;

import at.ac.sbc.carfactory.jms.server.JMSServer;

public class Benchmark {
	public static void main(String[] args) {

		//start server
		JMSServer jmsServer = JMSServer.getInstance();
		jmsServer.start();

		@SuppressWarnings("unused")
		JobManagementListener jobManagementListener = new JobManagementListener();

		//start Worker inactive
		Assembler assembler1 = new Assembler((long)1);
		Assembler assembler2 = new Assembler((long)2);

		Painter painter1 = new Painter(1, CarColor.BLUE);
		Painter painter2 = new Painter(2, CarColor.RED);

		Logistician logistician = new Logistician(1);

		Tester tester1 = new Tester((long)1, TestCase.CHECK_ALL_PARTS);
		Tester tester2 = new Tester((long)2, TestCase.CHECK_DEFECT_PARTS);


		//PRODUCE RESSOURCES


		//wait until all produced!



		//START all WORKERs
		//TODO check if startConnection or startListening necessary?
		assembler1.startConnection();
		assembler2.startConnection();

		painter1.startListening();
		painter2.startListening();

		logistician.startListening();

		tester1.startConnection();
		tester2.startConnection();




		//BENCHMARK

		//TODO WAIT 60 sec or less and count finished cars!


		//STOP ALL WORKS
		assembler1.stopListening();
		assembler2.stopListening();
		painter1.stopListening();
		painter1.stopListening();
		logistician.stopListening();
		tester1.stopListening();
		tester2.stopListening();
	}
}
