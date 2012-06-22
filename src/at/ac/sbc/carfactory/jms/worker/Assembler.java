package at.ac.sbc.carfactory.jms.worker;

import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;

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
import javax.naming.NamingException;

import at.ac.sbc.carfactory.util.TestCaseType;

import at.ac.sbc.carfactory.util.TestCase;

import at.ac.sbc.carfactory.domain.CarMotorType;

import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.jms.dto.CarDTO;

public class Assembler extends Worker {

	private ConnectionFactory cf;
	private Connection connection;
	private Session session;
	private Context context;

	private Queue assemblingJobQueue;
	private Queue assemblingJobHiPrioQueue;

	private Queue painterJobQueue;
	private Queue painterJobHiPrioQueue;

	private Queue assembledCarQueue;
	private Queue assembledCarHiPrioQueue;

	private Queue updateGUIQueue;
	private Queue updateDBQueue;

	private MessageConsumer messageAssemblingCarConsumer;
	private MessageConsumer messageAssemblingCarHiPrioConsumer;

	private MessageProducer messagePainterJobAssembledCarProducer = null;
	private MessageProducer messagePainterJobAssembledCarHiPrioProducer = null;

	private MessageProducer messageAssembledCarProducer = null;
	private MessageProducer messageAssembledCarHiPrioProducer = null;

	private MessageProducer messageUpdateGUIProducer = null;
	private MessageProducer messageUpdateDBProducer = null;

	private final static Logger logger = Logger.getLogger(Assembler.class);

	public Assembler(Long id) {
		super(id);
		this.setupConnections();
	}

	public void setupConnections() {
		try {
			// TODO connect to server do not create a new Server !!
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put("java.naming.factory.initial",
					"org.jnp.interfaces.NamingContextFactory");
			env.put("java.naming.provider.url", "jnp://localhost:1099");
			env.put("java.naming.factory.url.pkgs",
					"org.jboss.naming:org.jnp.interfaces");
			context = new InitialContext(env);

			this.cf = (ConnectionFactory) context.lookup("/cf");

			this.assembledCarQueue = (Queue) context
					.lookup("/queue/assembledCarQueue");
			this.assembledCarHiPrioQueue = (Queue) context
					.lookup("/queue/assembledCarHiPrioQueue");
			this.assemblingJobQueue = (Queue) context
					.lookup("/queue/assemblingJobQueue");
			this.assemblingJobHiPrioQueue = (Queue) context
					.lookup("/queue/assemblingJobHiPrioQueue");
			this.painterJobQueue = (Queue) context
					.lookup("/queue/painterJobQueue");
			this.painterJobHiPrioQueue = (Queue) context
					.lookup("/queue/painterJobHiPrioQueue");

			this.updateDBQueue = (Queue) context.lookup("/queue/updateDBQueue");
			this.updateGUIQueue = (Queue) context
					.lookup("/queue/updateGUIQueue");

			connection = cf.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// create Consumers for normal and highprio stuff
			messageAssemblingCarConsumer = session
					.createConsumer(assemblingJobQueue);
			messageAssemblingCarHiPrioConsumer = session
					.createConsumer(assemblingJobHiPrioQueue);

			connection.start();
		} catch (Throwable t) {
			// JMSException could be thrown
			logger.error("setupConnection<" + this.getWorkerId() + ">:setup:"
					+ "Exception: " + t.toString());
			t.printStackTrace();
		}
	}

	@Override
	protected void closeConnections() {
		try {
			if (messageAssemblingCarConsumer != null)
				messageAssemblingCarConsumer.close();

			if (messageAssemblingCarHiPrioConsumer != null)
				messageAssemblingCarHiPrioConsumer.close();

			if (session != null)
				session.close();

			if (connection != null)
				connection.close();

			if (context != null)
				context.close();

		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage() {
//		logger.debug("<" + this.getWorkerId() + ">: receiveMessage");

		if (session == null) {
			logger.error("<"
					+ this.getWorkerId()
					+ ">:receiveMessage  Session is NULL, RETURN, no processing of message possibel.");
			return;
		}

		Message inMessage = null;

		try {
			// check assembledCar HI PRIO in painterJobQUEUE
			inMessage = messageAssemblingCarHiPrioConsumer.receiveNoWait();

			if (inMessage == null) {
				// check carPart LOW PRIO
				inMessage = messageAssemblingCarConsumer.receiveNoWait();
			}

			if (inMessage != null) {
				processMessage(inMessage);
			}

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processMessage(Message inMessage) {

		try {
			logger.debug("<" + this.getWorkerId() + ">: on Message");

			if (session == null) {
				logger.error("<"
						+ this.getWorkerId()
						+ ">:onMessage  Session is NULL, RETURN, no processing of message possibel.");
				return;
			}

			ObjectMessage inObjectMessage = null;
			ObjectMessage outObjectMessage = null;

			CarDTO carDTO = null;
			messageAssembledCarProducer = session
					.createProducer(assembledCarQueue);
			messageAssembledCarHiPrioProducer = session
					.createProducer(assembledCarHiPrioQueue);

			messagePainterJobAssembledCarProducer = session
					.createProducer(painterJobQueue);
			messagePainterJobAssembledCarHiPrioProducer = session
					.createProducer(painterJobHiPrioQueue);

			messageUpdateDBProducer = session.createProducer(updateDBQueue);
			messageUpdateGUIProducer = session.createProducer(updateGUIQueue);

			if (inMessage instanceof ObjectMessage) {
				inObjectMessage = (ObjectMessage) inMessage;

				if (inObjectMessage.getObject() instanceof CarDTO) {
					carDTO = (CarDTO) inObjectMessage.getObject();

					// Server sent this message to the Queue
					logger.debug("<"
							+ this.getWorkerId()
							+ ">: Received Msg from Server JobManagement Car to Assemble.");

					if (carDTO.id != null) {
						carDTO.assemblyWorkerId = (this.getWorkerId());

						outObjectMessage = session.createObjectMessage(carDTO);

						// check if painted
						if (carDTO.carBody.bodyColor != null) {
							// send CarDTO to assembledCarQueue

							//SET TEST TYPE
							//get random value
							Random rn = new Random();
							//gets random value either 0,1,2  (excl. 3 !)
							int randomTestType = rn.nextInt(2);

							TestCaseType testCaseType = TestCaseType.getEnumByValue(randomTestType);
							outObjectMessage.setStringProperty("type", testCaseType.toString());
							// update DB
							messageUpdateDBProducer.send(outObjectMessage);
							logger.debug("<"
									+ this.getWorkerId()
									+ ">: Car was assembled - Update DB Queue, Msg sent.");

							// TODO UPDATE GUI over server??

							// update GUI
							messageUpdateGUIProducer.send(outObjectMessage);
							logger.debug("<"
									+ this.getWorkerId()
									+ ">: Car was assembled - Update GUI Queue, Msg sent.");

							// check if highprio by checking if orderId is set
							// and send to other QUEUE?
							if (carDTO.orderId != null) {

								messageAssembledCarHiPrioProducer
										.send(outObjectMessage);
								logger.debug("HiPrio - Order<"
										+ carDTO.orderId
										+ "> Assembler<"
										+ this.getWorkerId()
										+ ">: AssembledCar with Car<"
										+ carDTO.id
										+ "> is send out to assembledCarHiPrioQUEUE");
							} else {
								messageAssembledCarProducer
										.send(outObjectMessage);
								logger.debug("Low Prio no OrderId <"
										+ this.getWorkerId()
										+ ">: AssembledCar with Car<"
										+ carDTO.id
										+ "> is send out to assembledCarQUEUE");
							}

						} else {
							// send CarDTO to painterJobQueue

							// TODO if HIpprio add Color to TYPE and send to
							// HiPrio

							// update DB
							messageUpdateDBProducer.send(outObjectMessage);
							logger.debug("<"
									+ this.getWorkerId()
									+ ">: Car was assembled - Update DB Queue, Msg sent.");

							// TODO UPDATE GUI over server??

							// update GUI
							messageUpdateGUIProducer.send(outObjectMessage);
							logger.debug("<"
									+ this.getWorkerId()
									+ ">: Car was assembled - Update GUI Queue, Msg sent.");

							// check if highprio by checking if orderId is set
							// and send to other QUEUE?
							if (carDTO.orderId != null) {
								outObjectMessage.setStringProperty("type", "car_"+carDTO.carBody.requestedBodyColorByOrder.toString());
								messagePainterJobAssembledCarHiPrioProducer
										.send(outObjectMessage);
								logger.debug("HiPrio - Order<"
										+ carDTO.orderId
										+ "> Assembler<"
										+ this.getWorkerId()
										+ ">: AssembledCar with Car<"
										+ carDTO.id
										+ "> is send out to painterJobCarHiPrioQUEUE");
							} else {
								outObjectMessage.setStringProperty("type", "car");
								messagePainterJobAssembledCarProducer
										.send(outObjectMessage);
								logger.debug("Low Prio no OrderId <"
										+ this.getWorkerId()
										+ ">: AssembledCar with Car<"
										+ carDTO.id
										+ "> is send out to painterJobCarQUEUE");
							}
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
			try {
				if (messageAssembledCarHiPrioProducer != null)
					messageAssembledCarHiPrioProducer.close();

				if (messageAssembledCarProducer != null)
					messageAssembledCarProducer.close();

				if (messagePainterJobAssembledCarHiPrioProducer != null)
					messagePainterJobAssembledCarHiPrioProducer.close();

				if (messagePainterJobAssembledCarProducer != null)
					messagePainterJobAssembledCarProducer.close();

				if (messageUpdateDBProducer != null)
					messageUpdateDBProducer.close();

				if (messageUpdateGUIProducer != null)
					messageUpdateGUIProducer.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
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

		Assembler assembler = new Assembler(id);
		Scanner sc = new Scanner(System.in);

		assembler.start();
		logger.info("Enter 'quit' to exit AssemblerWorker...");
		while (!sc.nextLine().equals("quit"))
			;

		assembler.stopWorker();

		logger.info("Stopped Listening properly.");

		logger.info("Assembler exited.");
	}

}
