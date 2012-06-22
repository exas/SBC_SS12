package at.ac.sbc.carfactory.backend;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import at.ac.sbc.carfactory.domain.Order;
import at.ac.sbc.carfactory.domain.Car;


public class OrderDaoSimpleImpl {
	private static final OrderDaoSimpleImpl INSTANCE = new OrderDaoSimpleImpl();

	private final ConcurrentMap<Long, Order> newOrders = new ConcurrentHashMap<Long, Order>();
	private final ConcurrentLinkedQueue<Long> newOrderQueue = new ConcurrentLinkedQueue<Long>();

	private final ConcurrentMap<Long, Order> finishedOrders = new ConcurrentHashMap<Long, Order>();


	private OrderDaoSimpleImpl() {
	}

	public static OrderDaoSimpleImpl getInstance() { return INSTANCE; }

	public void saveNewOrder(final Order order) {
		newOrders.putIfAbsent(order.getId(), order);
		newOrderQueue.add(order.getId());
	}

	public Collection<Order> getAllNewOrders() {
        return Collections.synchronizedCollection(newOrders.values());
    }

	public Order getNewOrderById(final Long id) {
	    return newOrders.get(id);
	}

	public void updateNewOrderById(final Long id,
	                               final Order orderForUpdate) {
	    final Order orderToUpdate = getNewOrderById(id);
	    orderToUpdate.resetFinishedCars();

	    for(Car car : orderForUpdate.getFinishedCars()) {
	    	orderToUpdate.addCar(car);
	    }
	    //only cars can change since how many are ready

	}

	public void deleteNewOrderById(final Long id) { newOrders.remove(id); }

	public void saveFinishedOrder(final Order order) {
		finishedOrders.putIfAbsent(order.getId(), order);
	}

	public Set<Order> getAllFinishedOrder() {
        return (Set<Order>) Collections.synchronizedCollection(finishedOrders.values());
    }

	public Order getFinishedOrderById(final Long id) {
	    return finishedOrders.get(id);
	}

	public void updateFinishedOrderById(final Long id,
	                               final Order orderForUpdate) {
	    final Order orderToUpdate = getFinishedOrderById(id);
	    //TODO update fields
	}

	public void deleteFinishedOrderById(final Long id) { finishedOrders.remove(id); }

	public synchronized Order getNextOrderForProcessing() {
		for(Order order: getAllNewOrders()) {
			//TODO check each order if carPart or Car fits for this order if yes then return order;
			return order;
		}
		return null;
	}

}
