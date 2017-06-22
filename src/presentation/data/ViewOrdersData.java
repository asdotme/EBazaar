package presentation.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.logging.Logger;
import java.util.stream.Collectors;

import business.BusinessConstants;
import business.customersubsystem.CustomerSubsystemFacade;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderSubsystem;
import business.ordersubsystem.OrderSubsystemFacade;
import business.usecasecontrol.ViewOrdersController;

public enum ViewOrdersData {
	INSTANCE;
	private static final Logger LOG = Logger.getLogger(ViewOrdersData.class.getSimpleName());
	private OrderPres selectedOrder;

	public OrderPres getSelectedOrder() {
		return selectedOrder;
	}

	public void setSelectedOrder(OrderPres so) {
		selectedOrder = so;
	}

	public List<OrderPres> getOrders() {
		
		ViewOrdersController controller = new ViewOrdersController();
		 SessionCache context = SessionCache.getInstance();
		 CustomerSubsystem customer = (CustomerSubsystemFacade) context.get(SessionCache.CUSTOMER);
		 List<OrderPres> ops = controller.getOrderHistory(customer).stream()
				 .map(o -> new OrderPres(o))
				 .collect(Collectors.toList());
		 return ops;
		
//		LOG.warning("ViewOrdersData method getOrders() has not been implemented.");
//		// return DefaultData.ALL_ORDERS;
//		OrderPres o1 = new OrderPres();
//		o1.setOrder(OrderSubsystemFacade.createOrder(1, "10/21/2016", 2.2));
//		return Arrays.asList(o1);
	}
}
