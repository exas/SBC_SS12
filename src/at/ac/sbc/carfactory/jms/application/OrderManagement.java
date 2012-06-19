package at.ac.sbc.carfactory.jms.application;

import org.apache.log4j.Logger;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import at.ac.sbc.carfactory.jms.dto.OrderDTO;

import at.ac.sbc.carfactory.domain.Order;


public class OrderManagement {

	private Logger logger = Logger.getLogger(OrderManagement.class);

	private ConnectionFactory cf = null;
	private Connection connection = null;
	private Session session = null;

	private Queue orderQueue = null;

	private MessageProducer producerOrder = null;

	private Context context;

	public OrderManagement() {
		this.init();
	}

	private void init() {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put("java.naming.factory.initial",
				"org.jnp.interfaces.NamingContextFactory");
		env.put("java.naming.provider.url", "jnp://localhost:1099");
		env.put("java.naming.factory.url.pkgs",
				"org.jboss.naming:org.jnp.interfaces");

		try {
			context = new InitialContext(env);
			this.cf = (ConnectionFactory) context.lookup("/cf");
			this.orderQueue = (Queue) context.lookup("/queue/orderQueue");

		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendOrder(Order order) {
		logger.debug("Sending Order <" + order.getId()
				+ "> to orderQueue for Server.");
		try {

			// create Connection for JMS Queue
			connection = cf.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create a JMS Message Producer to send a message on the queue
			producerOrder = session.createProducer(orderQueue);
			// Create Data Transfer Object for Queue
			OrderDTO orderDTO = new OrderDTO();
			orderDTO.id = order.getId();
			orderDTO.carAmount = order.getCarAmount();
			orderDTO.carColor = order.getCarColor();
			orderDTO.carMotorType = order.getCarMotorType();

			ObjectMessage outObjectMessage = null;

			outObjectMessage = session.createObjectMessage();
			outObjectMessage.setObject(orderDTO);
			outObjectMessage.setStringProperty("type", "newOrder");

			// send it using the producer
			producerOrder.send(outObjectMessage);

			logger.debug("Order <" + orderDTO.id
					+ ">: is sent out. Msg sent.");

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// JMS close connection and session
			logger.info("Closing Session and then connection!");
			try {
				if (producerOrder != null)
					producerOrder.close();
			} catch (Exception ex) {/* ok */}

			try {
				if (session != null)
					session.close();
			} catch (Exception ex) {/* ok */
			}
			try {
				if (connection != null)
					connection.close();
			} catch (Exception ex) {/* ok */
			}
		}

	}

}
