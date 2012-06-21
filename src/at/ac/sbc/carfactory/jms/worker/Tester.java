package at.ac.sbc.carfactory.jms.worker;

import java.util.Hashtable;
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
import javax.naming.NamingException;

import at.ac.sbc.carfactory.util.TestCaseType;

import at.ac.sbc.carfactory.util.TestCase;
import at.ac.sbc.carfactory.jms.dto.CarDTO;

public class Tester extends Worker {

	private TestCaseType testCaseType;

	private ConnectionFactory cf;
	private Connection connection;
	private Session session;

	private Queue assembledAndTestedCarQueue;
	private Queue assembledAndTestedCarHiPrioQueue;

	private Queue assembledCarQueue;// consumer
	private Queue assembledCarHiPrioQueue;// consumer

	private Queue updateDBQueue;
	private Queue updateGUIQueue;

	private MessageConsumer messageAssembledCarConsumer;
	private MessageConsumer messageAssembledCarHiPrioConsumer;

	private Context context;

	private MessageProducer messageAssembledAndTestedCarProducer = null;
	private MessageProducer messageAssembledAndTestedCarHiPrioProducer = null;
	private MessageProducer messageAssembledCarProducer = null;
	private MessageProducer messageAssembledCarHiPrioProducer = null;
	private MessageProducer messageUpdateGUIProduder = null;
	private MessageProducer messageUpdateDBProducer = null;

	private final static Logger logger = Logger.getLogger(Tester.class);

	public Tester(long id, TestCaseType testCase) {
		super(id);
		this.testCaseType = testCase;
		this.messageAssembledAndTestedCarProducer = null;
		this.messageAssembledAndTestedCarHiPrioProducer = null;
		this.messageUpdateGUIProduder = null;
		this.messageUpdateDBProducer = null;
		this.setupConnection();
	}

	public void setupConnection() {
		try {
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
			this.updateGUIQueue = (Queue) context
					.lookup("/queue/updateGUIQueue");
			this.updateDBQueue = (Queue) context.lookup("/queue/updateDBQueue");

			connection = cf.createConnection();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			String selectorTestCaseType = "type='"
					+ this.testCaseType.toString() + "'";
			// create Consumers for normal and highprio stuff
			messageAssembledCarConsumer = session.createConsumer(
					assembledCarQueue, selectorTestCaseType);
			messageAssembledCarHiPrioConsumer = session.createConsumer(
					assembledCarHiPrioQueue, selectorTestCaseType);


			connection.start();

			logger.debug("Tester<"
					+ this.getWorkerId()
					+ ">: setup of Connection finished. Call .start() to start WorkerThread");

		} catch (Throwable t) {
			// JMSException could be thrown
			logger.error("Tester<" + this.getWorkerId() + ">:setup:"
					+ "Exception: " + t.toString());
		}
	}

	@Override
	public void receiveMessage() {
		logger.debug("Tester<" + this.getWorkerId() + ">: receiveMessage");

		if (session == null) {
			logger.error("Tester<"
					+ this.getWorkerId()
					+ ">:receiveMessage  Session is NULL, RETURN, no processing of message possibel.");
			return;
		}

		Message inMessage = null;

		try {
			// check FIRST assembledCar HI PRIO
			inMessage = messageAssembledCarHiPrioConsumer.receiveNoWait();

			if (inMessage == null) {
				// check assembledCar
				inMessage = messageAssembledCarConsumer.receiveNoWait();
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
		logger.debug("Tester<" + this.getWorkerId() + ">: processing Message");
		ObjectMessage inObjectMessage = null;
		ObjectMessage outObjectMessage = null;
		boolean isTestOK = false;

		try {

			messageAssembledAndTestedCarProducer = session
					.createProducer(assembledAndTestedCarQueue);
			messageAssembledAndTestedCarHiPrioProducer = session
					.createProducer(assembledAndTestedCarHiPrioQueue);

			messageAssembledCarProducer = session
					.createProducer(assembledCarQueue);
			messageAssembledCarHiPrioProducer = session
					.createProducer(assembledCarHiPrioQueue);

			messageUpdateDBProducer = session.createProducer(updateDBQueue);
			messageUpdateGUIProduder = session.createProducer(updateGUIQueue);


			if (inMessage instanceof ObjectMessage) {
				inObjectMessage = (ObjectMessage) inMessage;

				if (inObjectMessage.getObject() instanceof CarDTO) {
					CarDTO carDTO = (CarDTO) inObjectMessage.getObject();

					// Painter sent this message to the Queue
					logger.debug("Tester<"
							+ this.getWorkerId()
							+ ">: Received Msg from Assembler with an AssembledCar to test.");

					if (carDTO.id != null) {

						// check if ERROR
						if (carDTO.testCases.size() >= 2) {
							// ERROR since after 2 tests should get fwd to
							// Logistician
							logger.error("Tester<"
									+ this.getWorkerId()
									+ ">: Received Car from AssemblerQueue with more or equal to 2 tests.");

							// TODO send to next QUEUE ?
							return;
						}

						// check if ERROR
						if (carDTO.testCases.size() == 1) {
							TestCase oldTest = carDTO.testCases.get(0);

							if (oldTest.getTestCaseType() == this.testCaseType) {
								// ERROR should not be double checked by same
								// Tester
								logger.error("Tester<"
										+ this.getWorkerId()
										+ ">: Received Car from AssemblerQueue is being tested twice by same Tester");
							}
						}

						TestCase testCase = new TestCase(this.testCaseType);
						isTestOK = testCase.test(carDTO);

						// add TestCase
						carDTO.testCases.add(testCase);

						if (carDTO.testCases.size() >= 2)
							carDTO.isTestingFinished = true;
						else
							carDTO.isTestingFinished = false;

						// set isDefect to false only if isTestOK is True and if
						// isDefect is not already on TRUE!
						if (carDTO.isDefect != null) {
							if (isTestOK && !carDTO.isDefect)
								carDTO.isDefect = false;
							else
								carDTO.isDefect = true;
						} else
							carDTO.isDefect = !isTestOK;

						// set workerID correctly
						if (this.testCaseType == TestCaseType.CHECK_ALL_PARTS) {
							carDTO.testerAllPartsAssembledWorkerId = this
									.getWorkerId();
						} else
							carDTO.testerIsDefectWorkerId = this.getWorkerId();

						outObjectMessage = session.createObjectMessage(carDTO);

						// check if highprio by checking if orderId is set and
						// send to other QUEUE?
						// only 1 test now for next test
						if (carDTO.orderId != null) {

							if (carDTO.isTestingFinished) {
								messageAssembledAndTestedCarHiPrioProducer
										.send(outObjectMessage);

								logger.debug("Tester<"
										+ this.getWorkerId()
										+ ">: HiPrio - Order<"
										+ carDTO.orderId
										+ "> - AssembledCar with Car<"
										+ carDTO.id
										+ "> was fully tested. New TestCase: <"
										+ testCase.getTestCaseType()
										+ ">::isTestOK<"
										+ testCase.isTestOK()
										+ "> and fwd to assembledAndTestedCarHiPrioQueue");
							} else {

								// SEND again to assembled car with other Info
								outObjectMessage.setStringProperty("testType",
										testCaseType.getNext().toString());

								messageAssembledCarHiPrioProducer
										.send(outObjectMessage);

								logger.debug("Tester<"
										+ this.getWorkerId()
										+ ">: HiPrio - Order<"
										+ carDTO.orderId
										+ "> - AssembledCar with Car<"
										+ carDTO.id
										+ "> is tested<"
										+ testCase.getTestCaseType()
										+ ">::isTestOK<"
										+ testCase.isTestOK()
										+ "> once and send again to assembledCarHiPrioQueue");
							}
						} else {
							// NO HI PRIO!!
							if (carDTO.isTestingFinished) {
								// TESTING FINISHED
								messageAssembledAndTestedCarProducer
										.send(outObjectMessage);

								logger.debug("Tester<"
										+ this.getWorkerId()
										+ ">: LoPrio - No OrderId- AssembledCar with Car<"
										+ carDTO.id
										+ "> was fully tested. New TestCase: <"
										+ testCase.getTestCaseType()
										+ ">::isTestOK<"
										+ testCase.isTestOK()
										+ "> and fwd to assembledAndTestedCarQueue");
							} else {
								// TESTING NOT FINISHED
								// SEND again to assembled car with other Info
								outObjectMessage.setStringProperty("testType",
										testCaseType.getNext().toString());

								messageAssembledCarProducer
										.send(outObjectMessage);

								logger.debug("Tester<"
										+ this.getWorkerId()
										+ ">: LoPrio - No OrderID - AssembledCar with Car<"
										+ carDTO.id
										+ "> is tested<"
										+ testCase.getTestCaseType()
										+ ">::isTestOK<"
										+ testCase.isTestOK()
										+ "> once and send again to assembledCarQueue");
							}
						}

						// update DB
						messageUpdateDBProducer.send(outObjectMessage);
						logger.debug("Tester<"
								+ this.getWorkerId()
								+ ">: Assembled Car was tested - Update DB Queue, Msg sent.");

						// update GUI
						messageUpdateGUIProduder.send(outObjectMessage);
						logger.debug("Tester<"
								+ this.getWorkerId()
								+ ">: Assembled Car was tested - Update GUI Queue, Msg sent.");

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
				if (messageAssembledAndTestedCarProducer != null)
					messageAssembledAndTestedCarProducer.close();

				if (messageAssembledAndTestedCarHiPrioProducer != null)
					messageAssembledAndTestedCarHiPrioProducer.close();

				if (messageAssembledCarProducer != null)
					messageAssembledCarProducer.close();

				if (messageAssembledCarHiPrioProducer != null)
					messageAssembledCarProducer.close();

				if (messageUpdateDBProducer != null)
					messageUpdateDBProducer.close();

				if (messageUpdateGUIProduder != null)
					messageUpdateGUIProduder.close();
			} catch (JMSException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void closeConnections() {
		try {
			if (messageAssembledCarConsumer != null)
				messageAssembledCarConsumer.close();

			if (messageAssembledCarHiPrioConsumer != null)
				messageAssembledCarHiPrioConsumer.close();

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

	public static void main(String[] args) {
		if (args.length != 2) {
			logger.error("Provide a numeric ID as argument and the type of testing CHECK_ALL_PARTS or CHECK_DEFECT_PARTS");
			System.exit(-1);
		}

		Long id = null;
		try {
			id = Long.parseLong(args[0]);
		} catch (Exception ex) {
			logger.error("Provide a numeric ID as argument");
			System.exit(-1);
		}

		TestCaseType testCaseType = null;
		try {
			testCaseType = TestCaseType.valueOf(args[1]);
		} catch (Exception ex) {
			logger.error("Provide a valid testcase either 'CHECK_ALL_PARTS' or 'CHECK_DEFECT_PARTS'.");
			System.exit(-1);
		}

		Tester tester = new Tester(id, testCaseType); // starts Worker.Thread

		tester.start();
		logger.info("Enter 'quit' to exit TESTER...");
		Scanner sc = new Scanner(System.in);

		while (!sc.nextLine().equals("quit"))
			;

		tester.stopWorker();
		logger.info("Stopped Connection properly.");

		logger.info("TESTER exited.");
	}

}
