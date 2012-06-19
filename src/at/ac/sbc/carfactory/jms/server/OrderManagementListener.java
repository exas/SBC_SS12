package at.ac.sbc.carfactory.jms.server;


import java.util.Hashtable;


import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import at.ac.sbc.carfactory.domain.Order;

import at.ac.sbc.carfactory.backend.OrderDaoSimpleImpl;

import at.ac.sbc.carfactory.jms.dto.OrderDTO;

import org.apache.log4j.Logger;

public class OrderManagementListener implements MessageListener,
		ExceptionListener {

	private ConnectionFactory cf;
	private Connection connection;
	private Session session;
	private Queue orderQueue;
	private MessageConsumer messageConsumer;

	private final Logger logger = Logger.getLogger(OrderManagementListener.class);

	public OrderManagementListener() {
		logger.debug("OrderManagementListener<" + this.toString()
				+ ">: instantiated");
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
			Context context = new InitialContext(env);

			this.cf = (ConnectionFactory) context.lookup("/cf");
			this.orderQueue = (Queue) context.lookup("/queue/orderQueue");

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
			logger.error("OrderManagementListener<" + this.toString()
					+ ">:setup:" + "Exception: " + t.toString());
		}
	}

	@Override
	public void onMessage(Message inMessage) {
		logger.debug("OrderManagementListener<" + this.toString()
				+ ">: on Message");

		if (session == null) {
			logger.error("OrderManagementListener<"
					+ this.toString()
					+ ">:onMessage  Session is NULL, RETURN, no processing of message possibel.");
			return;
		}

		ObjectMessage inObjectMessage = null;

		OrderDTO orderDTO = null;

		try {

			if (inMessage instanceof ObjectMessage) {
				inObjectMessage = (ObjectMessage) inMessage;

				if (inObjectMessage.getObject() instanceof OrderDTO) {
					orderDTO = (OrderDTO) inObjectMessage.getObject();

					if (inMessage.getStringProperty("type").equals(
							"updateOrder")) {
						// Order Update send to Queue
						logger.debug("<"
								+ this.hashCode()
								+ ">: Received message for Order update.");

						//update order
						if (orderDTO.id != null) {
							//update order
							updateOrder(orderDTO);
						}

					} else if (inMessage.getStringProperty("type").equals(
							"newOrder")) {
						// new Order send into Queue
						if (orderDTO.id != null) {
							//save new Order
							saveOrder(orderDTO);
						}
					}
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
			logger.info("JMS:Closing Message Producers!");

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

	/**
	 * Closes the connection.
	 */
	@PreDestroy
	public void endConnection() throws RuntimeException {
		logger.debug("JobManagementListener<" + this.toString()
				+ ">: PREDESTROY");
		try {
			if (connection != null) {
				connection.close();
			}
			if (session != null) {
				session.close();
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
