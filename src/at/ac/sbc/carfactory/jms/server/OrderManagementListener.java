package at.ac.sbc.carfactory.jms.server;


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

import at.ac.sbc.carfactory.backend.CarPartDaoSimpleImpl;
import at.ac.sbc.carfactory.domain.CarBody;
import at.ac.sbc.carfactory.domain.CarMotor;
import at.ac.sbc.carfactory.domain.CarPart;
import at.ac.sbc.carfactory.domain.CarPartType;
import at.ac.sbc.carfactory.domain.CarTire;
import at.ac.sbc.carfactory.jms.dto.CarPartDTO;

import at.ac.sbc.carfactory.domain.Order;

import at.ac.sbc.carfactory.backend.OrderDaoSimpleImpl;

import at.ac.sbc.carfactory.jms.dto.OrderDTO;

import org.apache.log4j.Logger;

public class OrderManagementListener implements MessageListener,
		ExceptionListener {

	private ConnectionFactory cf;
	private Connection connection;
	private Context context;
	private Session session;
	private Queue orderQueue;
	private Queue updateGUIQueue;
	private MessageConsumer messageConsumer;

	private final Logger logger = Logger.getLogger(OrderManagementListener.class);

	public OrderManagementListener() {
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
			this.orderQueue = (Queue) context.lookup("/queue/orderQueue");
			this.updateGUIQueue = (Queue) context.lookup("/queue/updateGUIQueue");

			connection = cf.createConnection();

			// TODO check at every listener if this is necessary since in
			// onMessage i create again a Session??
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			messageConsumer = session.createConsumer(orderQueue);
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

		OrderDTO orderDTO = null;

		MessageProducer messageUpdateGUIProducer = null;

		try {
			messageUpdateGUIProducer = session.createProducer(updateGUIQueue);

			if (inMessage instanceof ObjectMessage) {
				inObjectMessage = (ObjectMessage) inMessage;

				if (inObjectMessage.getObject() instanceof OrderDTO) {
					orderDTO = (OrderDTO) inObjectMessage.getObject();

					if (inMessage.getStringProperty("type").equals(
							"updateOrder")) {
						// Order Update send to Queue
						logger.debug("Received message for Order update.");

						//update order
						if (orderDTO.id != null) {
							//update order
							updateOrder(orderDTO);
						}

					} else if (inMessage.getStringProperty("type").equals(
							"newOrder")) {
						// new Order send into Queue
						if (orderDTO.id != null) {
							logger.debug("Save Order<"+orderDTO.id+">.");
							//save new Order
							saveOrder(orderDTO);

							logger.debug("Order<"+orderDTO.id+"> SAVED.");
						}
					}
					checkOrders(messageUpdateGUIProducer);
				}
			}
		} catch (JMSException e) {
			logger.error("OrderManagementListener.onMessage: JMSException: "
					+ e.toString());
			e.printStackTrace();
		} catch (Throwable te) {
			logger.error("OrderManagementListener.onMessage: Exception: "
					+ te.toString());
			te.printStackTrace();
		} finally {
			// JMS close connection and session
			try {
				if (messageUpdateGUIProducer != null)
					messageUpdateGUIProducer.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}

	private void updateOrder(OrderDTO orderDTO) {
		final Order order = new Order(orderDTO.id, orderDTO.carAmount, orderDTO.carColor, orderDTO.carMotorType);
		OrderDaoSimpleImpl.getInstance().updateNewOrderById(order.getId(), order);
	}

	private void saveOrder(OrderDTO orderDTO) {
		final Order order = new Order(orderDTO.id, orderDTO.carAmount, orderDTO.carColor, orderDTO.carMotorType);
		OrderDaoSimpleImpl.getInstance().saveNewOrder(order);
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

	public void stopConnection() throws RuntimeException {
		logger.debug("DESTROY");
		try {
			if (messageConsumer != null) {
				messageConsumer.close();
			}
			if (connection != null) {
				connection.close();
			}
			if (session != null) {
				session.close();
			}
			if(context != null)
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
