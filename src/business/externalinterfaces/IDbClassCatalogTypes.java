package business.externalinterfaces;

import java.sql.ResultSet;

import business.productsubsystem.CatalogTypesImpl;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbClass;

public interface IDbClassCatalogTypes extends DbClass {

	CatalogTypesImpl getCatalogTypes() throws DatabaseException;

	String getQuery();

	Object[] getQueryParams();

	int[] getParamTypes();

	void populateEntity(ResultSet resultSet) throws DatabaseException;

	String getDbUrl();

}