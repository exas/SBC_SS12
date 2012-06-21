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

				if (inObjectMessage.getObject() instanceof CarDTO) {
					//CAR
					carDTO = (CarDTO) inObjectMessage.getObject();
					updateCar(carDTO);
				}
				else if (inObjectMessage.getObject() instanceof CarPartDTO) {
					//CARPART
					carPartDTO = (CarPartDTO) inObjectMessage.getObject();

					// Painter sent this message to the Queue
					logger.debug("Received Msg from Painter (painted Body).");

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
			try{
				if(producerAssemblingJob != null)
					producerAssemblingJob.close();

				if(producerPainterJob != null)
					producerPainterJob.close();

			} catch (JMSException e) {
				e.printStackTrace();
			}

		}

	}

	private void updateCar(CarDTO carDTO) {
		logger.debug("updateCar");

		return;

//		final Car car = new Car();
//
//		final CarBody carBody = new CarBody();
//		final CarMotor carMotor = new CarMotor();
//		final CarTire carTire = new CarTire();
//
//		// TODO check all lists and then get the one and check all fields to see
//		// where to put the existing/updated car?
//		CarDaoSimpleImpl.getInstance()
//				.updateCarToAssembleById(car.getId(), car);
	}

	private void updateCarPart(CarPartDTO carPartDTO) {
		logger.debug("updateCarPart");

		if (carPartDTO.carPartType != CarPartType.CAR_BODY) {
			logger.debug("Wrong CarPart only CarBody should be updated??");
			return;
		}

		// TODO check which part and save and rechcekc before if not double
		// existent?
		final CarBody carBody = new CarBody(carPartDTO.id, carPartDTO.carId,
				carPartDTO.orderId, carPartDTO.painterId,
				carPartDTO.producerId, carPartDTO.carPartType,
				carPartDTO.bodyColor, carPartDTO.isDefect);

		if (carBody.getPainterWorkerId() != null) {
			CarPartDaoSimpleImpl.getInstance().updateCarBodyFromPainter(
					carBody.getId(), carBody);
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

			carDTO.carBody = carBodyDTO;

			CarPartDTO carMotorDTO = new CarPartDTO();
			carMotorDTO.id = carMotor.getId();
			carMotorDTO.producerId = carMotor.getProducerId();

			carDTO.carMotor = carMotorDTO;

			carDTO.carTires = carTireDTOs;

			Car car = new Car(carBody, carMotor, carTires);
			CarDaoSimpleImpl.getInstance().saveCarToAssemble(car);

			if (car.getId() != null) {
				carDTO.id = car.getId();
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
			if(context != null) {
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
