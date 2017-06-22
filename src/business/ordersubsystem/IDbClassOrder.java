package business.ordersubsystem;

import java.sql.ResultSet;
import java.util.List;

import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderItem;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbClass;

public interface IDbClassOrder extends DbClass {

	List<OrderItem> getOrderItems(Integer orderId) throws DatabaseException;

	List<OrderItem> refreshOrderItems(Integer orderId) throws DatabaseException;

	void populateEntity(ResultSet resultSet) throws DatabaseException;

	String getDbUrl();

	String getQuery();

	Object[] getQueryParams();

	int[] getParamTypes();
	void submitOrder(CustomerProfile custProfile, Order order) throws DatabaseException;
	List<Integer> getAllOrderIds(CustomerProfile custProfile) throws DatabaseException;
	OrderImpl getOrderData(Integer orderId) throws DatabaseException;
}