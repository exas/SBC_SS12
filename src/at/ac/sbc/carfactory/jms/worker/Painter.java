package at.ac.sbc.carfactory.jms.worker;


import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;

import org.apache.log4j.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import at.ac.sbc.carfactory.util.TestCaseType;

import at.ac.sbc.carfactory.domain.CarColor;
import at.ac.sbc.carfactory.domain.CarPartType;
import at.ac.sbc.carfactory.jms.dto.CarDTO;
import at.ac.sbc.carfactory.jms.dto.CarPartDTO;

public class Painter extends Worker {

	private CarColor color;

	private ConnectionFactory cf;
	private Connection connection;
	private Session session;

	private Queue painterJobQueue; //consumer
	private Queue painterJobHiPrioQueue; //consumer

	private Queue assembledCarQueue;
	private Queue assembledCarHiPrioQueue;

	private Queue updateDBQueue;

	private Queue updateGUIQueue;

	private MessageConsumer messagePainterJobAssembledCarConsumer;
	private MessageConsumer messagePainterJobCarPartConsumer;
	private MessageConsumer messageAssembledCarHiPrioConsumer;
	private MessageConsumer messageCarBodyHiPrioConsumer;
	private Context context;

	private MessageProducer messageAssembledCarProducer = null;
	private MessageProducer messageAssembledCarHiPrioProducer = null;
	private MessageProducer messageUpdateGUIProducer = null;
	private MessageProducer messageUpdateDBProducer = null;

	private final static Logger logger = Logger.getLogger(Painter.class);

	public Painter(long id, CarColor color) {
		super(id);
		this.color = color;
		this.messageAssembledCarProducer = null;
		this.messageAssembledCarHiPrioProducer = null;
		this.messageUpdateGUIProducer = null;
		this.messageUpdateDBProducer = null;
		this.setupConnections();
	}

	public void setupConnections() {
        try {
        	Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
            env.put("java.naming.provider.url", "jnp://localhost:1099");
            env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
            context = new InitialContext(env);

    		this.cf = (ConnectionFactory)context.lookup("/cf");

    		this.assembledCarQueue = (Queue) context.lookup("/queue/assembledCarQueue");
    		this.assembledCarHiPrioQueue = (Queue) context.lookup("/queue/assembledCarHiPrioQueue");
    		this.painterJobQueue = (Queue) context.lookup("/queue/painterJobQueue");
    		this.painterJobHiPrioQueue = (Queue) context.lookup("/queue/painterJobHiPrioQueue");

    		this.updateGUIQueue = (Queue) context.lookup("/queue/updateGUIQueue");
            this.updateDBQueue = (Queue) context.lookup("/queue/updateDBQueue");

            connection = cf.createConnection();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            String selectorCar = "type='car'";
            String selectorCarBody = "type='carBody'";
            //create Consumers for normal and highprio stuff
            messagePainterJobAssembledCarConsumer = session.createConsumer(painterJobQueue, selectorCar);
            messagePainterJobCarPartConsumer = session.createConsumer(painterJobQueue, selectorCarBody);

            logger.debug("<"+this.getWorkerId()+">: setup");

            String selectorCarHiPrio = "type='car_"+this.color.toString() +"'";
            String selectorCarBodyHiPrio = "type='carBody_"+this.color.toString() +"'";
            messageAssembledCarHiPrioConsumer = session.createConsumer(painterJobHiPrioQueue,selectorCarHiPrio);
            messageCarBodyHiPrioConsumer = session.createConsumer(painterJobHiPrioQueue,selectorCarBodyHiPrio);


            connection.start();

        } catch (Throwable t) {
            // JMSException could be thrown
            logger.error("<"+this.getWorkerId()+">:setup:" + "Exception: "+ t.toString());
        }
    }


	@Override
	public void receiveMessage() {
//		logger.debug("<"+this.getWorkerId()+">: receiveMessage");

		if(session == null) {
    		logger.error("<"+this.getWorkerId()+">:receiveMessage  Session is NULL, RETURN, no processing of message possibel.");
    		return;
    	}

		Message inMessage = null;

		try {
			//check assembledCar HI PRIO in painterJobQUEUE
			inMessage = messageAssembledCarHiPrioConsumer.receiveNoWait();

			if(inMessage == null) {
				//check carPart HI PRIO
				inMessage = messageCarBodyHiPrioConsumer.receiveNoWait();
			}

			if(inMessage == null) {
				//check assembledCar
				inMessage = messagePainterJobAssembledCarConsumer.receiveNoWait();
			}

			if(inMessage == null) {
				//check carPart
				inMessage = messagePainterJobCarPartConsumer.receiveNoWait();
			}

			if(inMessage != null) {
				processMessage(inMessage);
			}

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void processMessage(Message inMessage) {

		ObjectMessage inObjectMessage = null;
    	ObjectMessage outObjectMessage = null;

        try {
        	messageAssembledCarProducer = session.createProducer(assembledCarQueue);
    		messageAssembledCarHiPrioProducer = session.createProducer(assembledCarHiPrioQueue);
        	messageUpdateDBProducer = session.createProducer(updateDBQueue);
        	messageUpdateGUIProducer = session.createProducer(updateGUIQueue);

        	if (inMessage instanceof ObjectMessage) {
    			inObjectMessage = (ObjectMessage) inMessage;

    			if (inObjectMessage.getObject() instanceof CarDTO) {
	    			CarDTO carDTO = (CarDTO)inObjectMessage.getObject();

					//Painter sent this message to the Queue
					logger.debug("<"+this.getWorkerId()+">: Received Msg from Assembler with an AssembledCar to paint.");

					if(carDTO.id != null) {
						carDTO.carBody.painterId = (this.getWorkerId());

						//paint the car
						carDTO.carBody.bodyColor = (this.color);

            			outObjectMessage = session.createObjectMessage(carDTO);

            			//update DB
            			messageUpdateDBProducer.send(outObjectMessage);
            			logger.debug("<"+this.getWorkerId()+">: Assembled Car is painted - Update DB Queue, Msg sent.");

            			//update GUI
            			messageUpdateGUIProducer.send(outObjectMessage);
            			logger.debug("<"+this.getWorkerId()+">: Assembled Car is painted - Update GUI Queue, Msg sent.");

            			//SET TEST TYPE
            			//get random value
						Random rn = new Random();
						//gets random value either 0,1,2  (excl. 3 !)
						int randomTestType = rn.nextInt(2);

						TestCaseType testCaseType = TestCaseType.getEnumByValue(randomTestType);
						outObjectMessage.setStringProperty("type", testCaseType.toString());

            			//check if highprio by checking if orderId is set and send to other QUEUE?
            			if(carDTO.orderId != null) {
            				messageAssembledCarHiPrioProducer.send(outObjectMessage);
            				logger.debug("<"+this.getWorkerId()+">: AssembledCar with Car<"+carDTO.id+">, Order<"+carDTO.orderId+"> is painted and send out to assembledCarHIPRIOQUEUE");
            			}
            			else {
            				messageAssembledCarProducer.send(outObjectMessage);
            				logger.debug("<"+this.getWorkerId()+">: AssembledCar with Car<"+carDTO.id+"> is painted and send out to assembledCarQUEUE");
            			}
					}
				} else if (inObjectMessage.getObject() instanceof CarPartDTO) {
					CarPartDTO carPartDTO = (CarPartDTO)inObjectMessage.getObject();

					//JobManagement/Server sent this message to the Queue
					logger.debug("<"+this.getWorkerId()+">: Received Msg from JobManagement with single CarBody to paint.");

					if(carPartDTO.id != null && carPartDTO.carPartType == CarPartType.CAR_BODY) {

						//paint the CarBody
						carPartDTO.painterId = this.getWorkerId();
						carPartDTO.bodyColor = this.color;

            			outObjectMessage = session.createObjectMessage(carPartDTO);

            			//update DB
            			messageUpdateDBProducer.send(outObjectMessage);
            			logger.debug("<"+this.getWorkerId()+">: CarBody is painted - Update DB Queue, Msg sent.");

            			//TODO UPDATE GUI over server??

            			//update GUI
            			messageUpdateGUIProducer.send(outObjectMessage);
            			logger.debug("<"+this.getWorkerId()+">: CarBody is painted - Update GUI Queue, Msg sent.");

            			//check if highprio by checking if orderId is set and send to other QUEUE?
            			if(carPartDTO.orderId != null) {
            				//mess.send(outObjectMessage);
            				logger.debug("<"+this.getWorkerId()+">: CarBody<"+carPartDTO.id+">, Order<"+carPartDTO.orderId+"> is painted and send out to assembledCarHIPRIOQUEUE");
            			}
            			else {
            				messageAssembledCarProducer.send(outObjectMessage);
            				logger.debug("<"+this.getWorkerId()+">: CarBody<"+carPartDTO.id+"> is painted and send out to assembledCarQUEUE");
            			}
					}
				}
        	}
        } catch (JMSException e) {
            logger.error("onMessage: JMSException: " + e.toString());
            e.printStackTrace();
        } catch (Throwable te) {
        	logger.error("onMessage: Exception: " + te.toString());
            te.printStackTrace();
        } finally {
			//close Producer
        	try { if( messageAssembledCarProducer != null ) messageAssembledCarProducer.close();  } catch( Exception ex ) {/*ok*/}
    		try { if( messageAssembledCarHiPrioProducer != null ) messageAssembledCarHiPrioProducer.close();  } catch( Exception ex ) {/*ok*/}
    		try { if( messageUpdateDBProducer != null ) messageUpdateDBProducer.close();  } catch( Exception ex ) {/*ok*/}
    		try { if( messageUpdateGUIProducer != null ) messageUpdateGUIProducer.close();  } catch( Exception ex ) {/*ok*/}
		}
	}

	@Override
	public void closeConnections() {
		try { if( messagePainterJobAssembledCarConsumer != null ) messagePainterJobAssembledCarConsumer.close(); } catch( Exception ex ) {/*ok*/}
		try { if( messageAssembledCarHiPrioConsumer != null ) messageAssembledCarHiPrioConsumer.close(); } catch( Exception ex ) {/*ok*/}
		try { if( messagePainterJobCarPartConsumer != null ) messagePainterJobCarPartConsumer.close(); } catch( Exception ex ) {/*ok*/}
		try { if( messageCarBodyHiPrioConsumer != null ) messageCarBodyHiPrioConsumer.close(); } catch( Exception ex ) {/*ok*/}
		try { if( session != null ) session.close();  } catch( Exception ex ) {/*ok*/}
	    try { if( connection != null ) connection.close();  } catch( Exception ex ) {/*ok*/}
	    try { if( context != null ) context.close(); } catch( Exception ex ) {/*ok*/}
	}

	public static void main(String[] args) {
		 if(args.length != 2) {
			 logger.error("Provide numeric ID and string Color as argument");
			 System.exit(-1);
		 }
		 Long id = null;
		 try {
			 id = Long.parseLong(args[0]);
		 } catch (Exception ex) {
			 logger.error("Provide numeric ID and  string Color as argument");
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
		painter.start();
		logger.info("Enter 'quit' to exit PainterWorker...");
		Scanner sc = new Scanner(System.in);

		while(!sc.nextLine().equals("quit"));

		painter.stopWorker();
		logger.info("Stopped Connection properly.");

		logger.info("PainterWorker exited.");
	}


}
