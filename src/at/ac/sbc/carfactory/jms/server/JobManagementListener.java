package at.ac.sbc.carfactory.jms.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import at.ac.sbc.carfactory.domain.CarPartType;

import at.ac.sbc.carfactory.domain.Order;

import at.ac.sbc.carfactory.backend.OrderDaoSimpleImpl;

import at.ac.sbc.carfactory.domain.CarPart;

import org.apache.log4j.Logger;

import at.ac.sbc.carfactory.backend.CarDaoSimpleImpl;
import at.ac.sbc.carfactory.backend.CarPartDaoSimpleImpl;
import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarTire;
import at.ac.sbc.carfactory.jms.dto.CarDTO;
import at.ac.sbc.carfactory.jms.dto.CarPartDTO;

public class JobManagementListener implements MessageListener,
		ExceptionListener {

	private ConnectionFactory cf;
	private Connection connection;
	private Context context;
	private Session session;
	private Queue carPartQueue;
	private Queue assemblingJobQueue;
	private Queue assemblingJobHiPrioQueue;

	private Queue painterJobQueue;
	private Queue painterJobHiPrioQueue;

	private Queue updateGUIQueue;

	private MessageConsumer messageConsumer;

	private final Logger logger = Logger.getLogger(JobManagementListener.class);

	public JobManagementListener() {
		logger.debug("instantiated");
		setup();
	}

	public void setup() {
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put("java.naming.factory.initial",
					"org.jnp.interfaces.NamingContextFactory");
			env.put("java.naming.provider.url", "jnp://localhost:1099");
			env.put("java.naming.factory.url.pkgs",
					"org.jboss.naming:org.jnp.interfaces");
			context = new InitialContext(env);

			this.cf = (ConnectionFactory) context.lookup("/cf");
			this.carPartQueue = (Queue) context.lookup("/queue/carPartQueue");
			this.assemblingJobQueue = (Queue) context
					.lookup("/queue/assemblingJobQueue");
			this.painterJobQueue = (Queue) context
					.lookup("/queue/painterJobQueue");

			this.assemblingJobHiPrioQueue = (Queue) context
					.lookup("/queue/assemblingJobHiPrioQueue");
			this.painterJobHiPrioQueue = (Queue) context
					.lookup("/queue/painterJobHiPrioQueue");

			this.updateGUIQueue = (Queue) context
					.lookup("/queue/updateGUIQueue");

			connection = cf.createConnection();

			// TODO check at every listener if this is necessary since in
			// onMessage i create again a Session??
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			messageConsumer = session.createConsumer(carPartQueue);
			messageConsumer.setMessageListener(this);
			connection.start();
			// topicConnFactory =
			// producer = session.createProducer(topic);
		} catch (Throwable t) {
			// JMSException could be thrown
			logger.error("setup:" + "Exception: " + t.toString());
		}
	}

	@Override
	public void onMessage(Message inMessage) {
		logger.debug("on Message");

		if (session == null) {
			logger.error("onMessage  Session is NULL, RETURN, no processing of message possibel.");
			return;
		}

		ObjectMessage inObjectMessage = null;

		MessageProducer producerAssemblingJob = null;
		MessageProducer producerPainterJob = null;
		MessageProducer producerAssemblingJobHiPrio = null;
		MessageProducer producerPainterJobHiPrio = null;
		MessageProducer messageUpdateGUIProducer = null;

		CarPartDTO carPartDTO = null;

		try {
			producerAssemblingJob = session.createProducer(assemblingJobQueue);
			producerPainterJob = session.createProducer(painterJobQueue);
			messageUpdateGUIProducer = session.createProducer(updateGUIQueue);

			producerAssemblingJobHiPrio = session
					.createProducer(assemblingJobHiPrioQueue);
			producerPainterJobHiPrio = session
					.createProducer(painterJobHiPrioQueue);

			if (inMessage instanceof ObjectMessage) {
				inObjectMessage = (ObjectMessage) inMessage;
				if (inObjectMessage.getObject() instanceof CarPartDTO) {
					carPartDTO = (CarPartDTO) inObjectMessage.getObject();

					if (inMessage.getStringProperty("type").equals(
							"newProducedCarPart")) {
						// Producer send new produced Car Part into Queue
						if (carPartDTO.id != null) {

							switch (carPartDTO.carPartType) {
							case CAR_BODY:
								logger.debug("<"
										+ this.hashCode()
										+ ">: creating CarBody for in-memory-DB");
								CarBody carBody = null;
								carBody = new CarBody(carPartDTO.id,
										carPartDTO.producerId);
								carBody.setDefect(carPartDTO.isDefect());
								carBody.setCarPartType(carPartDTO.carPartType);

								CarPartDaoSimpleImpl.getInstance()
										.saveFreeCarBody(carBody);
								logger.debug("<" + this.hashCode()
										+ ">: saved CarBody in in-memory-DB");
								break;
							case CAR_MOTOR:
								logger.debug("<" + this.hashCode()
										+ ">: creating CarMotor in-memory-DB");
								CarMotor carMotor = null;
								carMotor = new CarMotor(carPartDTO.id,
										carPartDTO.producerId);
								carMotor.setDefect(carPartDTO.isDefect());
								carMotor.setMotorType(carPartDTO.carMotorType);
								carMotor.setCarPartType(carPartDTO.carPartType);

								CarPartDaoSimpleImpl.getInstance()
										.saveFreeCarMotor(carMotor);
								logger.debug("<" + this.hashCode()
										+ ">: saved CarMotor in-memory-DB");
								break;
							case CAR_TIRE:
								logger.debug("<" + this.hashCode()
										+ ">: creating CarTire in-memory-DB");
								CarTire carTire = null;
								carTire = new CarTire(carPartDTO.id,
										carPartDTO.producerId);
								carTire.setDefect(carPartDTO.isDefect());
								carTire.setCarPartType(carPartDTO.carPartType);

								CarPartDaoSimpleImpl.getInstance()
										.saveFreeCarTire(carTire);
								logger.debug("<" + this.hashCode()
										+ ">: saved CarTire in-memory-DB");
								break;
							default:
								break;
							}

							checkOrders(messageUpdateGUIProducer);

							checkForNewJobByOrders(producerAssemblingJobHiPrio,
									producerPainterJobHiPrio);

							// Check if new AssembleJob or BodyPaintJob is
							// available and send JobMsg
							checkForNewJob(producerAssemblingJob,
									producerPainterJob);
						}
					}
				}
			}

		} catch (JMSException e) {
			logger.error("JobManagementListener.onMessage: JMSException: "
					+ e.toString());
			e.printStackTrace();
		} catch (Throwable te) {
			logger.error("JobManagementListener.onMessage: Exception: "
					+ te.toString());
			te.printStackTrace();
		} finally {
			// JMS close connection and session
			logger.info("JMS:Closing Message Producers!");
			try {
				if (messageUpdateGUIProducer != null)
					messageUpdateGUIProducer.close();

				if (producerAssemblingJob != null)
					producerAssemblingJob.close();

				if (producerPainterJob != null)
					producerPainterJob.close();

				if (producerAssemblingJobHiPrio != null)
					producerAssemblingJobHiPrio.close();

				if (producerPainterJobHiPrio != null)
					producerPainterJobHiPrio.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void checkForNewJobByOrders(
			MessageProducer producerAssemblingJobHiPrio,
			MessageProducer producerPainterJobHiPrio) throws JMSException {

		ObjectMessage outObjectMessage = null;

		// for all orders check if relevant parts can be assigned?
		Collection<Order> syncCollection = OrderDaoSimpleImpl.getInstance()
				.getAllNewOrders();

		synchronized (syncCollection) {
			if (syncCollection.isEmpty()) {
				return;
			}
			Iterator<Order> iterator = syncCollection.iterator();

			while (iterator.hasNext()) {
				Order order = iterator.next();

				List<CarPart> carParts = order.getAllCarPartsForAssembleJob();

				// check if full assemble job is possible
				if (carParts != null && !carParts.isEmpty()) {

					CarBody carBody = null;
					CarMotor carMotor = null;
					List<CarTire> carTires = new ArrayList<CarTire>();

					// assign all parts to specific objects back
					for (CarPart carPart : carParts) {
						if (carPart instanceof CarTire) {
							CarTire carTire = (CarTire) carPart;
							carTires.add(carTire);
						} else if (carPart instanceof CarBody) {
							carBody = (CarBody) carPart;
						} else if (carPart instanceof CarMotor) {
							carMotor = (CarMotor) carPart;
						}
					}

					// assemble is possible
					logger.debug("HiPrio OrderId<"
							+ carBody.getOrderId()
							+ "> AssembleJob is possible (Body,Motor,4xTire reserved).");
					logger.debug("CarBody:<" + carBody.getId()
							+ ">, Producer:<" + carBody.getProducerId() + ">,  OrderId<"+carBody.getOrderId()+">");
					logger.debug("CarMotor:<" + carMotor.getId()
							+ ">, Producer:<" + carMotor.getProducerId() + ">,  OrderId<"+carMotor.getOrderId()+">");

					// save back to in-memory-DB but not in free LIST, into used
					// List
					CarPartDaoSimpleImpl.getInstance().saveCarBody(carBody);
					CarPartDaoSimpleImpl.getInstance().saveCarMotor(carMotor);

					List<CarPartDTO> carTireDTOs = new ArrayList<CarPartDTO>();

					for (CarTire carTire : carTires) {
						logger.debug("CarTire:<" + carTire.getId()
								+ ">, Producer:<" + carTire.getProducerId()
								+ ">,  OrderId<"+carTire.getOrderId()+">");

						CarPartDTO carTireDTO = new CarPartDTO();
						carTireDTO.id = carTire.getId();
						carTireDTO.producerId = carTire.getProducerId();
						carTireDTO.carPartType = carTire.getCarPartType();
						carTireDTO.isDefect = carTire.isDefect();
						carTireDTO.orderId = carTire.getOrderId();

						carTireDTOs.add(carTireDTO);

						CarPartDaoSimpleImpl.getInstance().saveCarTire(carTire);
					}

					// create Data Transfer Object (minimal objects containing
					// only
					// IDs...)
					CarDTO carDTO = new CarDTO();

					CarPartDTO carBodyDTO = new CarPartDTO();
					carBodyDTO.id = carBody.getId();
					carBodyDTO.bodyColor = carBody.getColor();
					carBodyDTO.painterId = carBody.getPainterWorkerId();
					carBodyDTO.producerId = carBody.getProducerId();
					carBodyDTO.carPartType = carBody.getCarPartType();
					carBodyDTO.isDefect = carBody.isDefect();
					carBodyDTO.orderId = carBody.getOrderId();
					carBodyDTO.requestedBodyColorByOrder = carBody.getRequestedColorByOrder();
					carDTO.carBody = carBodyDTO;

					CarPartDTO carMotorDTO = new CarPartDTO();
					carMotorDTO.id = carMotor.getId();
					carMotorDTO.producerId = carMotor.getProducerId();
					carMotorDTO.carPartType = carMotor.getCarPartType();
					carMotorDTO.isDefect = carMotor.isDefect();
					carMotorDTO.carMotorType = carMotor.getMotorType();
					carMotorDTO.orderId = carMotor.getOrderId();

					carDTO.carMotor = carMotorDTO;

					carDTO.carTires = carTireDTOs;

					Car car = new Car(carBody, carMotor, carTires);
					CarDaoSimpleImpl.getInstance().saveCarToAssemble(car);

					if (car.getId() != null) {
						carDTO.id = car.getId();
						carDTO.orderId = car.getOrderId();
					}

					// send carDTO to assemblingJobQueue

					outObjectMessage = session.createObjectMessage(carDTO);
					outObjectMessage.setStringProperty("type", "car");
					// TODO set COLOR if HIPRIO and send to HIPRIO
					producerAssemblingJobHiPrio.send(outObjectMessage);

					logger.debug("HiPrio OrderId<" + carBody.getOrderId()
							+ "> AssembleJob was send out to HiPrio Queue");

				} else {

					CarBody carBody = order.getSingleBodyPainterJob();

					if (carBody != null) {
						// PainterJob for single Body is available

						logger.debug("HiPrio OrderId<" + carBody.getOrderId()
								+ ">: PainterJob is possible (CarBody<"
								+ carBody.getId() + "> reserved).");

						// save back to in-memory-DB but not in free LIST, into
						// used
						// CarPart
						// List
						CarPartDaoSimpleImpl.getInstance().saveCarBody(carBody);

						CarPartDTO carBodyDTO = new CarPartDTO();
						carBodyDTO.id = new Long(carBody.getId());
						carBodyDTO.carPartType = carBody.getCarPartType();
						carBodyDTO.producerId = carBody.getProducerId();
						carBodyDTO.carPartType = carBody.getCarPartType();
						carBodyDTO.isDefect = carBody.isDefect();
						carBodyDTO.requestedBodyColorByOrder = carBody
								.getRequestedColorByOrder();
						carBodyDTO.orderId = carBody.getOrderId();

						// send carBodyDTO to painterJobQueue

						outObjectMessage = session
								.createObjectMessage(carBodyDTO);
						outObjectMessage.setStringProperty(
								"type",
								"carBody_"
										+ carBodyDTO.requestedBodyColorByOrder
												.toString());
						// TODO set COLOR if HIPRIO and send to HIPRIO

						producerPainterJobHiPrio.send(outObjectMessage);

						logger.debug("HiPrio OrderId<" + carBody.getOrderId()
								+ "> PainterJob with CarBody<" + carBodyDTO.id
								+ "> is send out to HiPrio QUEUE");
					}

				}
			}
		}

	}

	private void checkOrders(MessageProducer producerUpdateGUI) throws JMSException {

		// for all orders check if relevant parts can be assigned?
		Collection<Order> syncCollection = OrderDaoSimpleImpl.getInstance()
				.getAllNewOrders();

		ObjectMessage outObjectMessage = null;

		synchronized (syncCollection) {
			Iterator<Order> iterator = syncCollection.iterator();

			while (iterator.hasNext()) {
				Order order = iterator.next();

				// only if order requires CarPart otherwise assign to next one!
				if (order.requiresCarPart()) {
					List<CarPart> assignedCarParts = CarPartDaoSimpleImpl
							.getInstance().assignFreeCarPartsOrder(order);

					for (CarPart carPart : assignedCarParts) {
						outObjectMessage = null;

						if (carPart.getCarPartType() == CarPartType.CAR_TIRE) {
							CarTire carTire = (CarTire) carPart;

							CarPartDTO carTireDTO = new CarPartDTO();
							carTireDTO.id = carTire.getId();
							carTireDTO.producerId = carTire.getProducerId();
							carTireDTO.carPartType = carTire.getCarPartType();
							carTireDTO.isDefect = carTire.isDefect();
							carTireDTO.orderId = carTire.getOrderId();

							outObjectMessage = session
									.createObjectMessage(carTireDTO);

							// updateGUI with OrderID
							producerUpdateGUI.send(outObjectMessage);

						} else if (carPart.getCarPartType() == CarPartType.CAR_BODY) {
							CarBody carBody = (CarBody) carPart;
							CarPartDTO carBodyDTO = new CarPartDTO();
							carBodyDTO.id = carBody.getId();
							carBodyDTO.bodyColor = carBody.getColor();
							carBodyDTO.painterId = carBody.getPainterWorkerId();
							carBodyDTO.producerId = carBody.getProducerId();
							carBodyDTO.carPartType = carBody.getCarPartType();
							carBodyDTO.isDefect = carBody.isDefect();
							carBodyDTO.requestedBodyColorByOrder = carBody.getRequestedColorByOrder();
							carBodyDTO.orderId = carBody.getOrderId();

							outObjectMessage = session
									.createObjectMessage(carBodyDTO);

							// updateGUI with OrderID
							producerUpdateGUI.send(outObjectMessage);

						} else if (carPart.getCarPartType() == CarPartType.CAR_MOTOR) {
							CarMotor carMotor = (CarMotor) carPart;
							CarPartDTO carMotorDTO = new CarPartDTO();
							carMotorDTO.id = carMotor.getId();
							carMotorDTO.producerId = carMotor.getProducerId();
							carMotorDTO.carPartType = carMotor.getCarPartType();
							carMotorDTO.isDefect = carMotor.isDefect();
							carMotorDTO.carMotorType = carMotor.getMotorType();
							carMotorDTO.orderId = carMotor.getOrderId();

							outObjectMessage = session
									.createObjectMessage(carMotorDTO);

							// updateGUI with OrderID
							producerUpdateGUI.send(outObjectMessage);
						}
					}
				}
			}
		}
	}

	private void checkForNewJob(MessageProducer producerAssemblingJob,
			MessageProducer producerPainterJob) throws JMSException {

		ObjectMessage outObjectMessage = null;
		List<CarPart> carParts = CarPartDaoSimpleImpl.getInstance()
				.getAllCarPartsForAssembleJob();

		// check if full assemble job is possible
		if (carParts != null && !carParts.isEmpty()) {

			CarBody carBody = null;
			CarMotor carMotor = null;
			List<CarTire> carTires = new ArrayList<CarTire>();

			// assign all parts to specific objects back
			for (CarPart carPart : carParts) {
				if (carPart instanceof CarTire) {
					CarTire carTire = (CarTire) carPart;
					carTires.add(carTire);
				} else if (carPart instanceof CarBody) {
					carBody = (CarBody) carPart;
				} else if (carPart instanceof CarMotor) {
					carMotor = (CarMotor) carPart;
				}
			}

			// assemble is possible
			logger.debug("AssembleJob is possible (Body,Motor,4xTire reserved).");
			logger.debug("CarBody:<" + carBody.getId() + ">, Producer:<"
					+ carBody.getProducerId() + ">");
			logger.debug("CarMotor:<" + carMotor.getId() + ">, Producer:<"
					+ carMotor.getProducerId() + ">");

			// save back to in-memory-DB but not in free LIST, into used List
			CarPartDaoSimpleImpl.getInstance().saveCarBody(carBody);
			CarPartDaoSimpleImpl.getInstance().saveCarMotor(carMotor);

			List<CarPartDTO> carTireDTOs = new ArrayList<CarPartDTO>();

			for (CarTire carTire : carTires) {
				logger.debug("CarTire:<" + carTire.getId() + ">, Producer:<"
						+ carTire.getProducerId() + ">");

				CarPartDTO carTireDTO = new CarPartDTO();
				carTireDTO.id = carTire.getId();
				carTireDTO.producerId = carTire.getProducerId();
				carTireDTO.carPartType = carTire.getCarPartType();
				carTireDTO.isDefect = carTire.isDefect();
				carTireDTO.orderId = carTire.getOrderId();

				carTireDTOs.add(carTireDTO);

				CarPartDaoSimpleImpl.getInstance().saveCarTire(carTire);
			}

			// create Data Transfer Object (minimal objects containing only
			// IDs...)
			CarDTO carDTO = new CarDTO();

			CarPartDTO carBodyDTO = new CarPartDTO();
			carBodyDTO.id = carBody.getId();
			carBodyDTO.bodyColor = carBody.getColor();
			carBodyDTO.painterId = carBody.getPainterWorkerId();
			carBodyDTO.producerId = carBody.getProducerId();
			carBodyDTO.carPartType = carBody.getCarPartType();
			carBodyDTO.isDefect = carBody.isDefect();
			carBodyDTO.orderId = carMotor.getOrderId();

			carDTO.carBody = carBodyDTO;

			CarPartDTO carMotorDTO = new CarPartDTO();
			carMotorDTO.id = carMotor.getId();
			carMotorDTO.producerId = carMotor.getProducerId();
			carMotorDTO.carPartType = carMotor.getCarPartType();
			carMotorDTO.isDefect = carMotor.isDefect();
			carMotorDTO.carMotorType = carMotor.getMotorType();
			carMotorDTO.orderId = carMotor.getOrderId();

			carDTO.carMotor = carMotorDTO;

			carDTO.carTires = carTireDTOs;

			Car car = new Car(carBody, carMotor, carTires);
			CarDaoSimpleImpl.getInstance().saveCarToAssemble(car);

			if (car.getId() != null) {
				carDTO.id = car.getId();
				carDTO.orderId = carDTO.orderId;
			}

			// send carDTO to assemblingJobQueue

			outObjectMessage = session.createObjectMessage(carDTO);
			outObjectMessage.setStringProperty("type", "car");
			// TODO set COLOR if HIPRIO and send to HIPRIO
			producerAssemblingJob.send(outObjectMessage);

			logger.debug("AssembleJob was send out");

		} else {

			CarBody carBody = CarPartDaoSimpleImpl.getInstance()
					.getSingleBodyPainterJob();

			if (carBody != null) {
				// PainterJob for single Body is available

				logger.debug("<" + this.hashCode()
						+ ">: PainterJob is possible (CarBody<"
						+ carBody.getId() + "> reserved).");

				// save back to in-memory-DB but not in free LIST, into used
				// CarPart
				// List
				CarPartDaoSimpleImpl.getInstance().saveCarBody(carBody);

				CarPartDTO carBodyDTO = new CarPartDTO();
				carBodyDTO.id = new Long(carBody.getId());
				carBodyDTO.carPartType = carBody.getCarPartType();
				carBodyDTO.producerId = carBody.getProducerId();
				carBodyDTO.carPartType = carBody.getCarPartType();
				carBodyDTO.isDefect = carBody.isDefect();
				carBodyDTO.orderId = carBody.getOrderId();

				// send carBodyDTO to painterJobQueue

				outObjectMessage = session.createObjectMessage(carBodyDTO);
				outObjectMessage.setStringProperty("type", "carBody");
				// TODO set COLOR if HIPRIO and send to HIPRIO

				producerPainterJob.send(outObjectMessage);

				logger.debug("PainterJob with CarBody<" + carBodyDTO.id
						+ "> is send out to QUEUE");
			}

		}

	}

	public void stopConnection() throws RuntimeException {
		logger.debug("PREDESTROY");
		try {
			if (messageConsumer != null)
				messageConsumer.close();

			if (connection != null) {
				connection.close();
			}
			if (session != null) {
				session.close();
			}
			if (context != null)
				context.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onException(JMSException e) {
		logger.error("Listener-JMSException: " + e.toString());
	}

}
