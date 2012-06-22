package at.ac.sbc.carfactory.jms.worker;

import java.util.Hashtable;
import java.util.Scanner;
import org.apache.log4j.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
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

import at.ac.sbc.carfactory.jms.dto.CarPartDTO;

import at.ac.sbc.carfactory.jms.dto.CarDTO;

public class Logistician extends Worker implements ExceptionListener {

	private ConnectionFactory cf;
	private Connection connection;
	private Session session;

	private Queue updateGUIQueue;
	private Queue updateDBQueue;
//	private Queue defectCarQueue;

	private Queue assembledAndTestedCarQueue;
	private Queue assembledAndTestedCarHiPrioQueue;

	private MessageConsumer messageAssembledAndTestedCarConsumer;
	private MessageConsumer messageAssembledAndTestedCarHiPrioConsumer;

	private Context context;

	private final static Logger logger = Logger.getLogger(Logistician.class);

	public Logistician(long id) {
		super(id);
		this.startConnections();
	}

	public void startConnections() {
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put("java.naming.factory.initial",
					"org.jnp.interfaces.NamingContextFactory");
			env.put("java.naming.provider.url", "jnp://localhost:1099");
			env.put("java.naming.factory.url.pkgs",
					"org.jboss.naming:org.jnp.interfaces");
			context = new InitialContext(env);

			this.cf = (ConnectionFactory) context.lookup("/cf");
			// TODO connect to server do not create a new Server Instance!
			this.assembledAndTestedCarQueue = (Queue) context
					.lookup("/queue/assembledAndTestedCarQueue");
			this.assembledAndTestedCarHiPrioQueue = (Queue) context
					.lookup("/queue/assembledAndTestedCarHiPrioQueue");
			this.updateGUIQueue = (Queue) context
					.lookup("/queue/updateGUIQueue");
			this.updateDBQueue = (Queue) context.lookup("/queue/updateDBQueue");
//			this.defectCarQueue = (Queue) context
//					.lookup("/queue/defectCarQueue");

			connection = cf.createConnection();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// listens to painterJobQueue
			messageAssembledAndTestedCarConsumer = session
					.createConsumer(assembledAndTestedCarQueue);

			// listens to painterJobQueue
			messageAssembledAndTestedCarHiPrioConsumer = session
					.createConsumer(assembledAndTestedCarHiPrioQueue);

			connection.start();
		} catch (Throwable t) {
			// JMSException could be thrown
			logger.error("<" + this.getWorkerId() + ">:setup:" + "Exception: "
					+ t.toString());
		}
	}

	@Override
	public void receiveMessage() {
		if (session == null) {
			logger.error("<"
					+ this.getWorkerId()
					+ ">:receiveMessage  Session is NULL, RETURN, no processing of message possibel.");
			return;
		}

		Message inMessage = null;

		try {
			// check HI PRIO
			inMessage = messageAssembledAndTestedCarHiPrioConsumer
					.receiveNoWait();

			if (inMessage == null) {
				// check LOW PRIO
				inMessage = messageAssembledAndTestedCarConsumer
						.receiveNoWait();
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
		logger.debug("<" + this.getWorkerId() + ">: on Message");

		if (session == null) {
			logger.error("<"
					+ this.getWorkerId()
					+ ">:onMessage  Session is NULL, RETURN, no processing of message possibel.");
			return;
		}

		ObjectMessage inObjectMessage = null;
		ObjectMessage outObjectMessage = null;

		MessageProducer producerUpdateGUI = null;
		MessageProducer producerUpdateDB = null;
//		MessageProducer producerDefectCar = null;

		try {
			producerUpdateGUI = session.createProducer(updateGUIQueue);
			producerUpdateDB = session.createProducer(updateDBQueue);
//			producerDefectCar = session.createProducer(defectCarQueue);

			if (inMessage instanceof ObjectMessage) {
				inObjectMessage = (ObjectMessage) inMessage;

				if (inObjectMessage.getObject() instanceof CarDTO) {
					CarDTO carDTO = (CarDTO) inObjectMessage.getObject();

					logger.debug("<" + this.getWorkerId()
							+ ">: Received Msg from AssembledCarQueue");

					if (carDTO.id != null) {
						carDTO.logisticWorkerId = (this.getWorkerId());


						if (carDTO.isDefect) {
							logger.debug("<"
									+ this.getWorkerId()
									+ ">: AssembledCar with Car<"
									+ carDTO.id
									+ "> is DEFECT - disassembling and sending back. Updating GUI and DB via Message.");

							outObjectMessage = session.createObjectMessage(carDTO);
							producerUpdateGUI.send(outObjectMessage);
							// TODO disassemble and reintegrate each carpart
							// again into the system.
							if(!carDTO.carBody.isDefect()) {
								//if not defect back to system
								carDTO.carBody.carId = null;
								carDTO.carBody.orderId = null;
								carDTO.carBody.requestedBodyColorByOrder = null;

								outObjectMessage = session.createObjectMessage(carDTO.carBody);
								producerUpdateDB.send(outObjectMessage);

								logger.debug("<"
										+ this.getWorkerId()
										+ ">:CarBody<"
										+ carDTO.carBody.id
										+ "> is ok sending back to System. Updating GUI and DB via Message.");
							}

							if(!carDTO.carMotor.isDefect()) {
								//if not defect back to system
								carDTO.carMotor.carId = null;
								carDTO.carMotor.orderId = null;

								outObjectMessage = session.createObjectMessage(carDTO.carMotor);
								producerUpdateDB.send(outObjectMessage);

								logger.debug("<"
										+ this.getWorkerId()
										+ ">:carMotor<"
										+ carDTO.carMotor.id
										+ "> is ok sending back to System. Updating GUI and DB via Message.");
							}

							for(CarPartDTO carTireDTO : carDTO.carTires) {
								if(!carTireDTO.isDefect()) {
									//if not defect back to system
									carTireDTO.carId = null;
									carTireDTO.orderId = null;

									outObjectMessage = session.createObjectMessage(carTireDTO);
									producerUpdateDB.send(outObjectMessage);

									logger.debug("<"
											+ this.getWorkerId()
											+ ">:carTire<"
											+ carTireDTO.id
											+ "> is ok sending back to System. Updating GUI and DB via Message.");
								}

							}



						} else {
							outObjectMessage = session.createObjectMessage(carDTO);

							producerUpdateGUI.send(outObjectMessage);
							producerUpdateDB.send(outObjectMessage);

							logger.debug("<"
									+ this.getWorkerId()
									+ ">: AssembledCar with Car<"
									+ carDTO.id
									+ "> is send out and finished. Updating GUI and DB via Message.");
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
			// JMS close connection and session
			logger.info("JMS:Closing Message Producers!");
			try {
				if (producerUpdateGUI != null)
					producerUpdateGUI.close();
				if (producerUpdateDB != null)
					producerUpdateDB.close();
			} catch (Exception ex) {/* ok */
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void closeConnections() {
		// close all connections
		try {
			if (messageAssembledAndTestedCarConsumer != null)
				messageAssembledAndTestedCarConsumer.close();

			if (messageAssembledAndTestedCarHiPrioConsumer != null)
				messageAssembledAndTestedCarHiPrioConsumer.close();

			if (session != null)
				session.close();
			if (connection != null)
				connection.close();
			if( context != null )
				context.close();

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			logger.error("Provide numeric ID as argument");
			System.exit(-1);
		}
		Long id = null;
		try {
			id = Long.parseLong(args[0]);
		} catch (Exception ex) {
			logger.error("Provide numeric ID as argument");
			System.exit(-2);
		}

		Logistician logistician = new Logistician(id);

		logger.info("Enter 'quit' to exit LogisticianWorker...");
		Scanner sc = new Scanner(System.in);
		logistician.start();
		while (!sc.nextLine().equals("quit"))
			;

		logistician.stopWorker();
		logger.info("Stopped Listening properly.");

		logger.info("LogisticianWorker exited.");
	}

	@Override
	public void onException(JMSException e) {
		logger.error("Exception:" + e.toString());
	}

}
