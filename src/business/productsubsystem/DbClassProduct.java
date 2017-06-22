package business.productsubsystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;
import middleware.externalinterfaces.PreparedStatementCreatorWrapper;
import business.externalinterfaces.Catalog;
import business.externalinterfaces.IDbClassProduct;
import business.externalinterfaces.Product;
import business.productsubsystem.DbClassCatalog.CatalogImplRowMapper;
import business.util.Convert;
import business.util.TwoKeyHashMap;

@Repository
class DbClassProduct implements IDbClassProduct {
	enum Type {LOAD_PROD_TABLE, READ_PRODUCT, READ_PROD_LIST, SAVE_NEW_PROD};

	private static final Logger LOG = Logger.getLogger(DbClassProduct.class
			.getPackage().getName());
	private Type queryType;

	private String loadProdTableQuery = "SELECT * FROM product";
	private String readProductQuery = "SELECT * FROM Product WHERE productid = ?";
	private String readProdListQuery = "SELECT * FROM Product WHERE catalogid = ?";
	private final String saveNewProdQuery = "INSERT into Product " 
			+ "(catalogid, productname, totalquantity, priceperunit, mfgdate, description)" 
			+ " VALUES(?,?,?,?,?,?)";
	private Object[] loadProdTableParams, readProductParams, 
		readProdListParams, saveNewProdParams;
	private int[] loadProdTableTypes, readProductTypes, readProdListTypes, 
	    saveNewProdTypes;
	
	/**
	 * The productTable matches product ID and product name with
	 * the corresponding Product object. It is static so
	 * that requests for "read product" based on product ID can be handled
	 * without extra db hits. Useful for customer use cases, but not
	 * for manage products use case
	 */
	private static TwoKeyHashMap<Integer, String, Product> productTable;
	private Product product;
	private List<Product> productList;
	
	private JdbcOperations jdbcTemplate;

	@Inject
	@Named("dataSourceProducts")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	class ProductImpRowMapper implements RowMapper<Product> {
		public Product mapRow(ResultSet resultSet, int rownum) throws SQLException {
			int catalogId = resultSet.getInt("catalogid");
			CatalogImpl catalog = new CatalogImpl(catalogId, 
					(new CatalogTypesImpl()).getCatalogName(catalogId));
			
			return new ProductImpl(catalog,
					resultSet.getInt("productid"), 
					resultSet.getString("productname"),
					resultSet.getInt("totalquantity"),
					resultSet.getDouble("priceperunit"),
					Convert.localDateForString(resultSet.getString("mfgdate")),
					resultSet.getString("description"));
		}
	}

	/* (non-Javadoc)
	 * @see business.productsubsystem.IDbClassProduct#readProductTable()
	 */
	@Override
	public TwoKeyHashMap<Integer, String, Product> readProductTable()
			throws DatabaseException {
		if (productTable != null) {
			return productTable.clone();
		}
		//productTable needs to be populated, so call refresh
		return refreshProductTable();
	}

	/* (non-Javadoc)
	 * @see business.productsubsystem.IDbClassProduct#refreshProductTable()
	 */
	@Override
	public TwoKeyHashMap<Integer, String, Product> refreshProductTable()
			throws DatabaseException {
//		queryType = Type.LOAD_PROD_TABLE;
//		loadProdTableParams = new Object[]{};
//		loadProdTableTypes = new int[]{};
//		dataAccessSS.atomicRead(this);
		
		try {
			productList = jdbcTemplate.query(loadProdTableQuery, new ProductImpRowMapper());
			productTable = new TwoKeyHashMap();
			productList.forEach(p -> {
				productTable.put(p.getProductId(), p.getProductName(), p);
			});
		} catch (DataAccessException e) { // this is a subclass of
											// RuntimeException used by Spring
			LOG.warning("Rolling back transaction for getCatalogTypes() with query = " + readProdListQuery);
			LOG.warning("Error details:\n" + e.getMessage());
		}
		
		// Return a clone since productTable must not be corrupted
		return productTable.clone();
	}

	/* (non-Javadoc)
	 * @see business.productsubsystem.IDbClassProduct#readProductList(business.externalinterfaces.Catalog)
	 */
	@Override
	public List<Product> readProductList(Catalog cat)
			throws DatabaseException {
		if (productList == null) {
			refreshProductTable();
		}
		return productList
				.stream()
				.filter(p -> p.getCatalog().getId() == cat.getId())
				.collect(Collectors.toList());
		
//		try {
//			return jdbcTemplate.query(readProdListQuery, new Object[] { cat.getId() }, new ProductImpRowMapper());
//		} catch (DataAccessException e) { // this is a subclass of
//											// RuntimeException used by Spring
//			LOG.warning("Rolling back transaction for getCatalogTypes() with query = " + readProdListQuery);
//			LOG.warning("Error details:\n" + e.getMessage());
//		}
//		return null;
	}

	/* (non-Javadoc)
	 * @see business.productsubsystem.IDbClassProduct#refreshProductList(business.externalinterfaces.Catalog)
	 */
	@Override
	public List<Product> refreshProductList(Catalog cat)
			throws DatabaseException {
		refreshProductTable();
		return productList
				.stream()
				.filter(p -> p.getCatalog().getId() == cat.getId())
				.collect(Collectors.toList());
	}

	/* (non-Javadoc)
	 * @see business.productsubsystem.IDbClassProduct#readProduct(java.lang.Integer)
	 */
	@Override
	public Product readProduct(Integer productId)
			throws DatabaseException {
		if (productTable != null && productTable.isAFirstKey(productId)) {
			return productTable.getValWithFirstKey(productId);
		}
//		queryType = Type.READ_PRODUCT;
//		readProductParams = new Object[] {productId};
//		readProductTypes = new int[] {Types.INTEGER};
//		dataAccessSS.atomicRead(this);
//		return product;
		
		List<Product> products = jdbcTemplate.query(readProductQuery, new Object[] { productId }, new ProductImpRowMapper());
		if(products.size() != 1) {
			throw new DatabaseException("Product Not found");
		}
		return products.get(0);
	}
	

	/* (non-Javadoc)
	 * @see business.productsubsystem.IDbClassProduct#saveNewProduct(business.externalinterfaces.Product, business.externalinterfaces.Catalog)
	 */
	@Override
	public int saveNewProduct(Product product) 
			throws DatabaseException {
		KeyHolder keyHolder = new GeneratedKeyHolder();System.out.println(product.getDescription());
		try {
			jdbcTemplate.update(new PreparedStatementCreatorWrapper() {
				public Object[] getParams(){
					return new Object[] { 
							product.getCatalog().getId(),
							product.getProductName(),
							product.getQuantityAvail(),
							product.getUnitPrice(),
							Convert.localDateAsString(product.getMfgDate()),
							product.getDescription()
					};
				}
				public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
					PreparedStatement stmt = conn.prepareStatement(saveNewProdQuery, new String[] { "catalogid" });
					stmt.setInt(1, product.getCatalog().getId());
					stmt.setString(2, product.getProductName());
					stmt.setInt(3, product.getQuantityAvail());
					stmt.setDouble(4, product.getUnitPrice());
					stmt.setString(5, Convert.localDateAsString(product.getMfgDate()));
					stmt.setString(6, product.getDescription());
					return stmt;
				}
			}, keyHolder);
		} catch (DataAccessException e) { // this is a subclass of
											// RuntimeException used by Spring
			LOG.warning("Rolling back transaction for insertNewCatalog with query = " + saveNewProdQuery);
			LOG.warning("Error details:\n" + e.getMessage());
		}
		int id = keyHolder.getKey().intValue();
		product.setProductId(id);
		return id;
	}
	
	/// DbClass implemented methods
	/* (non-Javadoc)
	 * @see business.productsubsystem.IDbClassProduct#getDbUrl()
	 */
	@Override
	public String getDbUrl() {
		DbConfigProperties props = new DbConfigProperties();
		return props.getProperty(DbConfigKey.PRODUCT_DB_URL.getVal());
	}

	/* (non-Javadoc)
	 * @see business.productsubsystem.IDbClassProduct#getQuery()
	 */
	@Override
	public String getQuery() {
		switch(queryType) {
			case LOAD_PROD_TABLE:
				return loadProdTableQuery;
			case READ_PRODUCT:
				return readProductQuery;
			case READ_PROD_LIST:
				return readProdListQuery;
			case SAVE_NEW_PROD :
				return saveNewProdQuery;
			default:
				return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see business.productsubsystem.IDbClassProduct#getQueryParams()
	 */
	@Override
	public Object[] getQueryParams() {
		switch(queryType) {
			case LOAD_PROD_TABLE:
				return loadProdTableParams;
			case READ_PRODUCT:
				return readProductParams;
			case READ_PROD_LIST:
				return readProdListParams;
			case SAVE_NEW_PROD :
				return saveNewProdParams;
			default:
				return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see business.productsubsystem.IDbClassProduct#getParamTypes()
	 */
	@Override
	public int[] getParamTypes() {
		switch(queryType) {
		case LOAD_PROD_TABLE:
			return loadProdTableTypes;
		case READ_PRODUCT:
			return readProductTypes;
		case READ_PROD_LIST:
			return readProdListTypes;
		case SAVE_NEW_PROD :
			return saveNewProdTypes;
		default:
			return null;
	}
	}

	/* (non-Javadoc)
	 * @see business.productsubsystem.IDbClassProduct#populateEntity(java.sql.ResultSet)
	 */
	@Override
	public void populateEntity(ResultSet resultSet) throws DatabaseException {
		switch(queryType) {
			case LOAD_PROD_TABLE :
				populateProdTable(resultSet);
			case READ_PRODUCT :
				populateProduct(resultSet);
			case READ_PROD_LIST :
				populateProdList(resultSet);
			default :
				//do nothing
		}
	}

	private void populateProdList(ResultSet rs) throws DatabaseException {
		productList = new LinkedList<Product>();
		try {
			Product product = null;
			Integer prodId = null;
			String productName = null;
			Integer quantityAvail = null;
			Double unitPrice = null;
			String mfgDate = null;
			Integer catalogId = null;
			String description = null;
			while (rs.next()) {
				prodId = rs.getInt("productid");
				productName = rs.getString("productname");
				quantityAvail = rs.getInt("totalquantity");
				unitPrice =rs.getDouble("priceperunit");
				mfgDate = rs.getString("mfgdate");
				catalogId = rs.getInt("catalogid");
				description = rs.getString("description");
				CatalogImpl catalog = new CatalogImpl(catalogId, 
						(new CatalogTypesImpl()).getCatalogName(catalogId));
				product = new ProductImpl(catalog, prodId, productName, quantityAvail,
						 unitPrice, Convert.localDateForString(mfgDate),
					    description);
				productList.add(product);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	/**
	 * Internal method to ensure that product table is up to date.
	 */
	private void populateProdTable(ResultSet rs) throws DatabaseException {
		productTable = new TwoKeyHashMap<Integer, String, Product>();
		try {
			Product product = null;
			Integer prodId = null;
			String productName = null;
			Integer quantityAvail = null;
			Double unitPrice = null;
			String mfgDate = null;
			Integer catalogId = null;
			String description = null;
			while (rs.next()) {
				prodId = rs.getInt("productid");
				productName = rs.getString("productname");
				quantityAvail = rs.getInt("totalquantity");
				unitPrice = rs.getDouble("priceperunit");
				mfgDate = rs.getString("mfgdate");
				catalogId = rs.getInt("catalogid");
				description = rs.getString("description");
				CatalogImpl catalog = new CatalogImpl(catalogId, 
						(new CatalogTypesImpl()).getCatalogName(catalogId));
				product = new ProductImpl(catalog, prodId, productName, quantityAvail,
						unitPrice, Convert.localDateForString(mfgDate), description);
				productTable.put(prodId, productName, product);
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	private void populateProduct(ResultSet rs) throws DatabaseException {
		try {
			if (rs.next()) {
				CatalogImpl catalog = new CatalogImpl(rs.getInt("catalogid"), 
						(new CatalogTypesImpl()).getCatalogName(rs.getInt("catalogid")));
				
				product = new ProductImpl(catalog, rs.getInt("productid"),
						rs.getString("productname"),
						rs.getInt("totalquantity"),
						rs.getDouble("priceperunit"),
						Convert.localDateForString(rs.getString("mfgdate")),
						rs.getString("description"));
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	// delete
	String deleteQuery = "Delete from product where productid = ?";
	Object[] deleteParams = null;
	int[] deleteTypes = null;

	@Transactional(value = "txManagerProducts", propagation = Propagation.REQUIRES_NEW, readOnly = false)
	@Override
	public boolean deleteProduct(Product product) throws DatabaseException {
		deleteParams = new Object[] { product.getProductId() };
		deleteTypes = new int[] { Types.INTEGER };
		try {
			jdbcTemplate.update(deleteQuery, deleteParams, deleteTypes);
			return true;
		} catch (DataAccessException e) { // this is a subclass of
											// RuntimeException used by Spring
			LOG.warning("Rolling back transaction for insertNewCatalog with query = " + deleteQuery);
			LOG.warning("Error details:\n" + e.getMessage());
		}
		return false;
	}
}
