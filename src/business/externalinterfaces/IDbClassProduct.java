package business.externalinterfaces;

import java.sql.ResultSet;
import java.util.List;

import business.util.TwoKeyHashMap;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbClass;

public interface IDbClassProduct extends DbClass {

	TwoKeyHashMap<Integer, String, Product> readProductTable() throws DatabaseException;

	/**
	 * Force a database call
	 */
	TwoKeyHashMap<Integer, String, Product> refreshProductTable() throws DatabaseException;

	List<Product> readProductList(Catalog cat) throws DatabaseException;

	List<Product> refreshProductList(Catalog cat) throws DatabaseException;

	Product readProduct(Integer productId) throws DatabaseException;

	/**
	 * Database columns: productid, productname, totalquantity, priceperunit,
	 * mfgdate, catalogid, description
	 */
	int saveNewProduct(Product product) throws DatabaseException;

	/// DbClass implemented methods
	String getDbUrl();

	String getQuery();

	Object[] getQueryParams();

	int[] getParamTypes();

	void populateEntity(ResultSet resultSet) throws DatabaseException;

	boolean deleteProduct(Product product) throws DatabaseException;

}