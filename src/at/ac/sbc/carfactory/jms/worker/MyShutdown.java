package at.ac.sbc.carfactory.jms.worker;

public class MyShutdown extends Thread {
    @Override
	public void run() {
        System.out.println("MyShutdown hook called");
    }
}
