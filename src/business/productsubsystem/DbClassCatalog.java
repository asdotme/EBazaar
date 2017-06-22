package business.productsubsystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.logging.Logger;

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

import business.externalinterfaces.Catalog;
import business.externalinterfaces.IDbClassCatalog;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;
import middleware.externalinterfaces.PreparedStatementCreatorWrapper;

/**
 * This class is concerned with managing data for a single catalog. To read or
 * update the entire list of catalogs in the database, see DbClassCatalogs
 *
 */
@Repository
public class DbClassCatalog implements IDbClassCatalog {
	enum Type {
		INSERT
	};

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DbClassCatalog.class.getPackage().getName());

	private Type queryType;

	private final String readQuery = "SELECT * FROM CatalogType";
	private final String insertQuery = "INSERT into CatalogType (catalogname) VALUES(?)";
	private Object[] insertParams;
	private int[] insertTypes;

	private JdbcOperations jdbcTemplate;

	@Inject
	@Named("dataSourceProducts")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	class CatalogImplRowMapper implements RowMapper<Catalog> {
		public Catalog mapRow(ResultSet resultSet, int rownum) throws SQLException {
			return new CatalogImpl(resultSet.getInt("catalogid"), resultSet.getString("catalogname"));
		}
	}

	@Transactional(value = "txManagerProducts", propagation = Propagation.REQUIRES_NEW, readOnly = true)
	@Override
	public List<Catalog> getCatalogs() {
		try {
			return jdbcTemplate.query(readQuery, new CatalogImplRowMapper());
		} catch (DataAccessException e) { // this is a subclass of
											// RuntimeException used by Spring
			LOG.warning("Rolling back transaction for getCatalogTypes() with query = " + readQuery);
			LOG.warning("Error details:\n" + e.getMessage());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see business.productsubsystem.IDbClassCatalog#saveNewCatalog(java.lang.
	 * String)
	 */
	@Override
	public int saveNewCatalog(String catalogName) throws DatabaseException {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			//jdbcTemplate.update(insertQuery, insertParams, insertTypes);
			jdbcTemplate.update(new PreparedStatementCreatorWrapper() {
				public Object[] getParams(){
					return new Object[] { catalogName };
				}
				public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
					PreparedStatement stmt = conn.prepareStatement(insertQuery, new String[] { "catalogid" });
					stmt.setString(1, catalogName);
					return stmt;
				}
			}, keyHolder);
		} catch (DataAccessException e) { // this is a subclass of
											// RuntimeException used by Spring
			LOG.warning("Rolling back transaction for insertNewCatalog with query = " + insertQuery);
			LOG.warning("Error details:\n" + e.getMessage());
		}
		return keyHolder.getKey().intValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see business.productsubsystem.IDbClassCatalog#getDbUrl()
	 */
	@Override
	public String getDbUrl() {
		DbConfigProperties props = new DbConfigProperties();
		return props.getProperty(DbConfigKey.PRODUCT_DB_URL.getVal());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see business.productsubsystem.IDbClassCatalog#getQuery()
	 */
	@Override
	public String getQuery() {
		switch (queryType) {
		case INSERT:
			return insertQuery;
		default:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see business.productsubsystem.IDbClassCatalog#getQueryParams()
	 */
	@Override
	public Object[] getQueryParams() {
		switch (queryType) {
		case INSERT:
			return insertParams;
		default:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see business.productsubsystem.IDbClassCatalog#getParamTypes()
	 */
	@Override
	public int[] getParamTypes() {
		switch (queryType) {
		case INSERT:
			return insertTypes;
		default:
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see business.productsubsystem.IDbClassCatalog#populateEntity(java.sql.
	 * ResultSet)
	 */
	@Override
	public void populateEntity(ResultSet resultSet) throws DatabaseException {
		// do nothing

	}

	// delete
	String deleteQuery = "Delete from CatalogType where catalogid = ?";
	Object[] deleteParams = null;
	int[] deleteTypes = null;

	@Transactional(value = "txManagerProducts", propagation = Propagation.REQUIRES_NEW, readOnly = false)
	@Override
	public boolean deleteCatalog(Catalog catalog) throws DatabaseException {
		deleteParams = new Object[] { catalog.getId() };
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
