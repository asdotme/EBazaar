package business.externalinterfaces;

import java.sql.ResultSet;
import java.util.List;

import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbClass;

public interface IDbClassCatalog extends DbClass {

	int saveNewCatalog(String catalogName) throws DatabaseException;

	String getDbUrl();

	String getQuery();

	Object[] getQueryParams();

	int[] getParamTypes();

	void populateEntity(ResultSet resultSet) throws DatabaseException;

	boolean deleteCatalog(Catalog catalog) throws DatabaseException;
	
	List<Catalog> getCatalogs();
}