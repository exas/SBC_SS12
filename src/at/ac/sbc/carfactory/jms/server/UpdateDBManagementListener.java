package at.ac.sbc.carfactory.jms.server;

import java.util.ArrayList;
import java.util.Hashtable;
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

import at.ac.sbc.carfactory.domain.CarPart;

import at.ac.sbc.carfactory.backend.CarPartDaoSimpleImpl;
import at.ac.sbc.carfactory.domain.CarTire;
import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.jms.dto.CarPartDTO;
import at.ac.sbc.carfactory.backend.CarDaoSimpleImpl;
import at.ac.sbc.carfactory.domain.Car;
import at.ac.sbc.carfactory.jms.dto.CarDTO;

import org.apache.log4j.Logger;

public class UpdateDBManagementListener implements MessageListener,
		ExceptionListener {

	private ConnectionFactory cf;
	private Connection connection;
	private Session session;
	private Context context;
	private Queue updateDBQueue;
	private MessageConsumer messageConsumer;

	private Queue assemblingJobQueue = null;
	private Queue painterJobQueue = null;

	private MessageProducer producerAssemblingJob = null;
	private MessageProducer producerPainterJob = null;

	private final Logger logger = Logger
			.getLogger(UpdateDBManagementListener.class);

	public UpdateDBManagementListener() {
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
			this.updateDBQueue = (Queue) context.lookup("/queue/updateDBQueue");
			this.assemblingJobQueue = (Queue) context
					.lookup("/queue/assemblingJobQueue");
			this.painterJobQueue = (Queue) context
					.lookup("/queue/painterJobQueue");

			connection = cf.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			messageConsumer = session.createConsumer(updateDBQueue);
			messageConsumer.setMessageListener(this);

			connection.start();

		} catch (Throwable t) {
			// JMSException could be thrown
			logger.error("setup:" + "Exception: " + t.toString());
			t.printStackTrace();
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

		CarDTO carDTO = null;
		CarPartDTO carPartDTO = null;

		try {
			this.producerAssemblingJob = session
					.createProducer(assemblingJobQueue);
			this.producerPainterJob = session.createProducer(painterJobQueue);

			if (inMessage instanceof ObjectMessage) {
				inObjectMessage = (ObjectMessage) inMessage;

				if (inObjectMessage.getObject() instanceof CarPartDTO) {
					// CARPART
					carPartDTO = (CarPartDTO) inObjectMessage.getObject();

					// Painter sent this message to the Queue
					logger.debug("Received Msg");

					if (carPartDTO.id != null) {
						updateCarPart(carPartDTO);

						// Check if new AssembleJob or BodyPaintJob is
						// available and send JobMsg
						checkForNewJob(producerAssemblingJob,
								producerPainterJob);
					}

				}
			}
		} catch (JMSException e) {
			logger.error("UpdateDBManagementListener.onMessage: JMSException: "
					+ e.toString());
			e.printStackTrace();
		} catch (Throwable te) {
			logger.error("UpdateDBManagementListener.onMessage: Exception: "
					+ te.toString());
			te.printStackTrace();
		} finally {
			try {
				if (producerAssemblingJob != null)
					producerAssemblingJob.close();

				if (producerPainterJob != null)
					producerPainterJob.close();

			} catch (JMSException e) {
				e.printStackTrace();
			}

		}

	}

	private void updateCarPart(CarPartDTO carPartDTO) {
		logger.debug("updateCarPart");

		// TODO check which part and save and rechcekc before if not double
		// existent?
		if (carPartDTO.carPartType == CarPartType.CAR_BODY) {
			final CarBody carBody = new CarBody(carPartDTO.id,
					carPartDTO.carId, carPartDTO.orderId, carPartDTO.painterId,
					carPartDTO.producerId, carPartDTO.carPartType,
					carPartDTO.bodyColor, carPartDTO.requestedBodyColorByOrder,
					carPartDTO.isDefect);

			logger.debug("update CARBODY<" + carBody.getId() + ">, carId<"
					+ carBody.getCarId() + ">, orderId<" + carBody.getOrderId()
					+ ">, painterId<" + carBody.getPainterWorkerId() + ">, prodId<"
					+ carBody.getProducerId() + ">, color<" + carBody.getColor().toString()
					+ ">, requestCol<" + carBody.getRequestedColorByOrder() + ">, isDefect<"+carBody.isDefect()+">");

			CarPartDaoSimpleImpl.getInstance().updateCarBodyById(
					carBody.getId(), carBody);
		} else if (carPartDTO.carPartType == CarPartType.CAR_MOTOR) {
			final CarMotor carMotor = new CarMotor(carPartDTO.id,
					carPartDTO.carId, carPartDTO.orderId,
					carPartDTO.producerId, carPartDTO.carPartType,
					carPartDTO.carMotorType, carPartDTO.isDefect);

			logger.debug("update CarMotor<" + carMotor.getId() + ">, carId<"
					+ carMotor.getCarId() + ">, orderId<" + carMotor.getOrderId()
					+ ">, prodId<"
					+ carMotor.getProducerId() + ">, isDefect<"+carMotor.isDefect()+">, carMotorType<"+carMotor.getMotorType()+">");

			CarPartDaoSimpleImpl.getInstance().updateCarMotorById(
					carMotor.getId(), carMotor);
		} else if (carPartDTO.carPartType == CarPartType.CAR_TIRE) {
			final CarTire carTire = new CarTire(carPartDTO.id,
					carPartDTO.carId, carPartDTO.orderId,
					carPartDTO.producerId, carPartDTO.carPartType,
					carPartDTO.isDefect);

			logger.debug("update CarTire<" + carTire.getId() + ">, carId<"
					+ carTire.getCarId() + ">, orderId<" + carTire.getOrderId()
					+ ">, prodId<"
					+ carTire.getProducerId() + ">, isDefect<"+carTire.isDefect()+">");

			CarPartDaoSimpleImpl.getInstance().updateCarTireById(
					carTire.getId(), carTire);
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
			logger.debug("<"
					+ this.hashCode()
					+ ">: AssembleJob is possible (Body,Motor,4xTire reserved).");
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
				carDTO.orderId = car.getOrderId();
			}

			// send carDTO to assemblingJobQueue

			outObjectMessage = session.createObjectMessage(carDTO);
			outObjectMessage.setStringProperty("type", "car");
			// TODO set COLOR if HIPRIO and send to HIPRIO
			producerAssemblingJob.send(outObjectMessage);

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
		logger.debug("DESTROY");
		try {

			logger.info("JMS:Closing Message Producers!");
			try {
				if (producerAssemblingJob != null)
					producerAssemblingJob.close();
			} catch (Exception ex) {/* ok */
			}
			try {
				if (messageConsumer != null)
					messageConsumer.close();
			} catch (Exception ex) {/* ok */
			}
			try {
				if (producerPainterJob != null)
					producerPainterJob.close();
			} catch (Exception ex) {/* ok */
			}

			if (connection != null) {
				connection.close();
			}
			if (session != null) {
				session.close();
			}
			if (context != null) {
				context.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onException(JMSException e) {
		logger.error("Listener-JMSException: " + e.toString());
	}

}
