package business.ordersubsystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import middleware.exceptions.DatabaseException;
import business.exceptions.BackendException;
import business.externalinterfaces.Address;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderItem;
import business.externalinterfaces.OrderSubsystem;
import business.externalinterfaces.ShoppingCart;
import business.util.Convert;

@Service("oss")
public class OrderSubsystemFacade implements OrderSubsystem {
	private static final Logger LOG = Logger.getLogger(OrderSubsystemFacade.class.getPackage().getName());
	CustomerProfile custProfile;

	List<Order> orderList = new ArrayList<>();

	// private DbClassOrder dbClassOrder = new DbClassOrder();
	@Inject
	private IDbClassOrder dbClassOrder;
	//
	// public OrderSubsystemFacade(CustomerProfile custProfile) {
	// this.custProfile = custProfile;
	// }

	public void setCustomerProfile(CustomerProfile custProfile) {
		this.custProfile = custProfile;
	}

	/**
	 * Used by customer subsystem at login to obtain this customer's order
	 * history from the database. Assumes cust id has already been stored into
	 * the order subsystem facade This is created by using auxiliary methods at
	 * the bottom of this class file. First get all order ids for this customer.
	 * For each such id, get order data and form an order, and with that order
	 * id, get all order items and insert into the order.
	 */
	public List<Order> getOrderHistory() throws BackendException {
		// implemented
		try {
			List<Integer> orderIdList = getAllOrderIds();
			for (int orderId : orderIdList) {
				Order order = getOrderData(orderId);

				List<OrderItem> orderItems = getOrderItems(orderId);
				order.setOrderItems(orderItems);

				orderList.add(order);
			}
		} catch (DatabaseException e1) {
			throw new BackendException(e1);
		}

		return orderList;

		// LOG.warning("Method getOrderHistory() still needs to be
		// implemented");
		// return new ArrayList<Order>();
	}

	public void submitOrder(ShoppingCart cart) throws BackendException {
		// implement
		LOG.warning("The method submitOrder(ShoppingCart cart) in OrderSubsystemFacade has not been implemented");
		try {
			List<CartItem> cartItems = cart.getCartItems();
			List<OrderItem> orderItems = createOrderItemsFromCartItems(cartItems);

			Order order = new OrderImpl();

			order.setDate(LocalDate.now());
			order.setOrderItems(orderItems);
			Address shippingAddress = cart.getShippingAddress();
			Address billingAddress = cart.getBillingAddress();
			CreditCard paymentInfo = cart.getPaymentInfo();

			order.setShipAddress(shippingAddress);
			order.setBillAddress(billingAddress);
			order.setPaymentInfo(paymentInfo);

			dbClassOrder.submitOrder(custProfile, order);
		} catch (DatabaseException e1) {
			throw new BackendException(e1);
		}
	}

	public static List<OrderItem> createOrderItemsFromCartItems(List<CartItem> cartItems) {
		List<OrderItem> orderItems = new ArrayList<>();
		for (CartItem cartItem : cartItems) {
			OrderItem orderItem = new OrderItemImpl();
			orderItem.setProductId(cartItem.getProductid());
			orderItem.setProductName(cartItem.getProductName());
			orderItem.setQuantity(Integer.valueOf(cartItem.getQuantity()));

			orderItem.setUnitPrice((Double.valueOf(cartItem.getTotalprice()) / Integer.valueOf(cartItem.getQuantity())));
			
			orderItems.add(orderItem);
		}
		return orderItems;

	}

	/**
	 * Used whenever an order item needs to be created from outside the order
	 * subsystem
	 */
	public static OrderItem createOrderItem(Integer prodId, Integer orderId, String quantityReq, String unitPrice) {
		// implemented

		OrderItem orderItem = new OrderItemImpl();
		orderItem.setOrderId(orderId);
		orderItem.setProductId(prodId);
		orderItem.setQuantity(Integer.valueOf(quantityReq));
		orderItem.setUnitPrice(Double.valueOf(unitPrice));
		return orderItem;

	}

	/** to create an Order object from outside the subsystem */
	public static Order createOrder(Integer orderId, String orderDate, String totalPrice) {
		// implement
		OrderImpl order = new OrderImpl();
		order.setOrderId(orderId);
		order.setDate(Convert.localDateForString(orderDate));
		order.setTotalPrice(Double.valueOf(totalPrice));

		// LOG.warning("Method createOrder(Integer orderId, String orderDate,
		// String totalPrice) still needs to be implemented");
		return order;
	}

	///////////// Methods internal to the Order Subsystem -- NOT public
	List<Integer> getAllOrderIds() throws DatabaseException {
		return dbClassOrder.getAllOrderIds(custProfile);
	}

	/** Part of getOrderHistory */
	List<OrderItem> getOrderItems(Integer orderId) throws DatabaseException {
		return dbClassOrder.getOrderItems(orderId);
	}

	/**
	 * Uses cust id to locate top-level order data for customer -- part of
	 * getOrderHistory
	 */
	Order getOrderData(Integer custId) throws DatabaseException {
		return dbClassOrder.getOrderData(custId);
	}
}
