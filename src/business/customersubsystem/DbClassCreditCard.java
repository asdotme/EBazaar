package business.customersubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import business.externalinterfaces.CreditCard;

import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.IDbClassCreditCard;
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
public class DbClassCreditCard implements IDbClassCreditCard {
	enum Type {READ};
	private static final Logger LOG =
			Logger.getLogger(DbClassCreditCard.class.getPackage().getName());

	private Type queryType;
	private DataAccessSubsystem dataAccessSS =
			new DataAccessSubsystemFacade();

	private String readQuery
			= "SELECT custid,expdate,cardtype,cardnum FROM altpayment WHERE custid = ?";
	private Object[] readParams;
	private int[] readTypes;
	private CreditCardImpl customerCreditCard;
	private CustomerProfileImpl customerProfile;
	private JdbcOperations jdbcTemplate;

	@Inject
	@Named("dataSourceAccounts")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	class MyRowMapper implements RowMapper<CreditCard> {
		public CreditCard mapRow(ResultSet resultSet, int rownum)
				throws SQLException {
			CreditCard creditCard=new CreditCardImpl(
					customerProfile.getFirstName()+" "+customerProfile.getLastName(),
					resultSet.getString("expdate"),
					resultSet.getString("cardnum"),
					resultSet.getString("cardtype")
					);
			return creditCard;
		}
	}


	@Override
	public String getDbUrl() {
		DbConfigProperties props = new DbConfigProperties();
		return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());
	}

	@Override
	public String getQuery() {
		return readQuery;
	}

	@Override
	public Object[] getQueryParams() {
		return readParams;
	}

	@Override
	public int[] getParamTypes() {
		return readTypes;
	}
	//stub
	@Transactional(value = "txManagerAccounts", propagation = Propagation.REQUIRES_NEW, readOnly = true)
	@Override
	public CreditCard readDefaultPaymentInfo(CustomerProfile custProfile)
			 throws DatabaseException {
		 customerProfile=(CustomerProfileImpl)custProfile;
//		 queryType = Type.READ;
		 readParams = new Object[]{custProfile.getCustId()};
//		 readTypes = new int[]{Types.INTEGER};
//		 dataAccessSS.atomicRead(this);
		 try {
			 List<CreditCard> list = jdbcTemplate.query(readQuery, readParams, new MyRowMapper());
			 return list.get(0);
//			 customerCreditCard = new CreditCardImpl(customerProfile.getFirstName()+" "+customerProfile.getLastName(),entry.expdate,entry.cardNum,entry.cardType);
		 } catch(DataAccessException e) { //this is a subclass of RuntimeException used by Spring
			 LOG.warning("Rolling back transaction for readDefaultPaymentInfo(CustomerProfile custProfile) with query = " + readQuery);
			 LOG.warning("Error details:\n" + e.getMessage());
		 }
		 return null;
	 }

	@Override
	public void populateEntity(ResultSet resultSet) throws DatabaseException {
		try {
			//we take the first returned row
			if(resultSet.next()){
				customerCreditCard
						= new CreditCardImpl(customerProfile.getFirstName()+" "+customerProfile.getLastName(),
						resultSet.getString("expdate"),
						resultSet.getString("cardnum"),
						resultSet.getString("cardtype"));
			}
		}
		catch(SQLException e){
			throw new DatabaseException(e);
		}
		
	}

}
