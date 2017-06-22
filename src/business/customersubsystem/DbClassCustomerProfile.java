package business.customersubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.IDbClassCustomerProfile;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbConfigKey;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

@Transactional(propagation= Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
@Repository
class DbClassCustomerProfile implements IDbClassCustomerProfile {
	enum Type {READ};
	@SuppressWarnings("unused")
	private static final Logger LOG = 
		Logger.getLogger(DbClassCustomerProfile.class.getPackage().getName());
	private DataAccessSubsystem dataAccessSS = 
    	new DataAccessSubsystemFacade();
    
    private Type queryType;
    
    private String readQuery
        = "SELECT custid,fname,lname FROM Customer WHERE custid = ?";
    private Object[] readParams;
    private int[] readTypes;
    
    /** Used for reading in values from the database */
    private CustomerProfileImpl customerProfile;

	private JdbcOperations jdbcTemplate;

	@Inject
	@Named("dataSourceAccounts")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	class MyRowMapper implements RowMapper<CustomerProfile> {
		public CustomerProfile mapRow(ResultSet resultSet, int rownum)
				throws SQLException {
			CustomerProfileImpl customerProfile=new CustomerProfileImpl(
					resultSet.getInt("custid"),
					resultSet.getString("fname"),
					resultSet.getString("lname"));
			   customerProfile.setCustId(resultSet.getInt("custid"));
//
//			entry.custID = resultSet.getInt("custid");
//			entry.fname = resultSet.getString("fname");
//			entry.lname = resultSet.getString("lname");
			return customerProfile;
		}
	}

	static class Entry {
		Integer custID;
		String fname;
		String lname;
	}

	@Transactional(value = "txManagerAccounts", propagation = Propagation.REQUIRES_NEW, readOnly = true)
	@Override
	public CustomerProfile readCustomerProfile(Integer custId) throws DatabaseException {
//        queryType = Type.READ;
        readParams = new Object[]{custId};
//        readTypes = new int[]{Types.INTEGER};
//        dataAccessSS.atomicRead(this);
		try {
			List<CustomerProfile> list = jdbcTemplate.query(readQuery, readParams, new MyRowMapper());
			return list.get(0);
//			customerProfile = new CustomerProfileImpl(entry.custID,entry.fname,entry.lname);
		} catch(DataAccessException e) { //this is a subclass of RuntimeException used by Spring
			LOG.warning("Rolling back transaction for readCustomerProfile(CustomerProfile custProfile) with query = " + readQuery);
			LOG.warning("Error details:\n" + e.getMessage());
		}

		return null;
    }
 
    public void populateEntity(ResultSet resultSet) throws DatabaseException {
        try {
            //we take the first returned row
            if(resultSet.next()){
                customerProfile
                  = new CustomerProfileImpl(resultSet.getInt("custid"),
                							resultSet.getString("fname"),
                                            resultSet.getString("lname"));
            }
        }
        catch(SQLException e){
            throw new DatabaseException(e);
        }
    }

    public String getDbUrl() {
    	DbConfigProperties props = new DbConfigProperties();	
    	return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());
    }

    @Override
    public String getQuery() {
    	switch(queryType) {
	    	case READ :
	    		return readQuery;
	    	default :
	    		return null;
    	}
    }

	@Override
	public Object[] getQueryParams() {
		switch(queryType) {
	    	case READ :
	    		return readParams;
	    	default :
	    		return null;
		}
	}
	@Override
	public int[] getParamTypes() {
		switch(queryType) {
	    	case READ :
	    		return readTypes;
	    	default :
	    		return null;
		}
	}
}
