package business.customersubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import business.externalinterfaces.IDbClassAddress;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import middleware.DbConfigProperties;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbConfigKey;
import business.externalinterfaces.Address;
import business.externalinterfaces.CustomerProfile;

@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
@Repository
//class DbClassAddress implements DbClass, DbClassAddressForTest {
class DbClassAddress implements IDbClassAddress {
	enum Type {INSERT, READ_ALL, READ_DEFAULT_BILL, READ_DEFAULT_SHIP};
	private static final Logger LOG = Logger.getLogger(DbClassAddress.class.getPackage().getName());

//	private DataAccessSubsystem dataAccessSS = new DataAccessSubsystemFacade();

	DbClassAddress() {}

	private JdbcOperations jdbcTemplate;

	@Inject
	@Named("dataSourceAccounts")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}


	class MyRowMapperAll implements RowMapper<Address> {
		public Address mapRow(ResultSet resultSet, int rownum)
				throws SQLException {
			Address address=new AddressImpl(
					resultSet.getString("street"),
					resultSet.getString("city"),
					resultSet.getString("state"),
					resultSet.getString("zip"),
					resultSet.getBoolean("isship"),
					resultSet.getBoolean("isbill")

					);
			return address;
		}
	}




	class MyRowMapperShip implements RowMapper<Address> {
		public Address mapRow(ResultSet resultSet, int rownum)
				throws SQLException {
			Address address=new AddressImpl(
					resultSet.getString("shipaddress1"),
					resultSet.getString("shipcity"),
					resultSet.getString("shipstate"),
					resultSet.getString("shipzipcode"),true,false);
			return address;
		}
	}
	class MyRowMapperBill implements RowMapper<Address> {
		public Address mapRow(ResultSet resultSet, int rownum)
				throws SQLException {
			Address address=new AddressImpl(
					resultSet.getString("billaddress1"),
					resultSet.getString("billcity"),
					resultSet.getString("billstate"),
					resultSet.getString("billzipcode"),false,true);
			return address;
		}
	}


	//used when an Address object needs to be saved to the db
	public void setAddress(Address addr) {
		address = addr;
	}
	///// queries ///////
	private String insertQuery = "INSERT into altaddress " +
			"(custid,street,city,state,zip) " +
			"VALUES(?,?,?,?,?)";
	private String readAllQuery
			= "SELECT * from altaddress WHERE custid = ?";
	//param value to set: custProfile.getCustId()
	private String readDefaultBillQuery = "SELECT billaddress1, billaddress2, billcity, billstate, billzipcode " +
			"FROM Customer WHERE custid = ?";
	//param value to set: custProfile.getCustId()
	private String readDefaultShipQuery = "SELECT shipaddress1, shipaddress2, shipcity, shipstate, shipzipcode "+
			"FROM Customer WHERE custid = ?" ;
	//param value to set: custProfile.getCustId()

	private Object[] insertParams, readAllParams, readDefaultBillParams, readDefaultShipParams;
	private int[] insertTypes, readAllTypes, readDefaultBillTypes, readDefaultShipTypes;

	//this object is stored here, using setAddress, when it needs to be saved to db
	private Address address;

	//these are populated after database reads
	private List<Address> addressList;
	private AddressImpl defaultShipAddress;
	private AddressImpl defaultBillAddress;
	private Type queryType;


	//column names for Address table
	private final String STREET="street";
	private final String CITY = "city";
	private final String STATE = "state";
	private final String ZIP = "zip";
	private final String IS_SHIP = "isship";
	private final String IS_BILL = "isbill";

	//Precondition: Address has been set in this object

	@Transactional(value = "txManagerAccounts", propagation = Propagation.REQUIRES_NEW, readOnly = true)
	@Override
	public void saveAddress(CustomerProfile custProfile) throws DatabaseException {
//		queryType = Type.INSERT;
		insertParams = new Object[]
				{custProfile.getCustId(),address.getStreet(), address.getCity(), address.getState(),address.getZip()};
//		insertTypes = new int[]
//				{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
		//dataAccessSS.insertWithinTransaction(this);

		jdbcTemplate.update(insertQuery, insertParams);

	}

	@Transactional(value = "txManagerAccounts", propagation = Propagation.REQUIRES_NEW, readOnly = true)
	@Override
	public AddressImpl readDefaultShipAddress(CustomerProfile custProfile) throws DatabaseException {
		queryType = Type.READ_DEFAULT_SHIP;
		readDefaultShipParams = new Object[]{custProfile.getCustId()};
		readDefaultShipTypes = new int[]{Types.INTEGER};
//		dataAccessSS.atomicRead(this);
		try {
			List<Address> list = jdbcTemplate.query(readDefaultShipQuery, readDefaultShipParams, new MyRowMapperShip());
			return (AddressImpl) list.get(0);
//			defaultShipAddress = new AddressImpl(entry.street, entry.city, entry.state, entry.zip, true, false);
		} catch(DataAccessException e) { //this is a subclass of RuntimeException used by Spring
			LOG.warning("Rolling back transaction for readDefaultShipAddress(CustomerProfile custProfile) with query = " + readDefaultShipQuery);
			LOG.warning("Error details:\n" + e.getMessage());
		}
		return null;
	}

	@Transactional(value = "txManagerAccounts", propagation = Propagation.REQUIRES_NEW, readOnly = true)
	@Override
	public AddressImpl readDefaultBillAddress(CustomerProfile custProfile) throws DatabaseException {
		queryType = Type.READ_DEFAULT_BILL;
		readDefaultBillParams = new Object[]{custProfile.getCustId()};
		readDefaultBillTypes = new int[]{Types.INTEGER};
//		dataAccessSS.atomicRead(this);
		try {
			List<Address> list = jdbcTemplate.query(readDefaultBillQuery, readDefaultBillParams, new MyRowMapperBill());
			return (AddressImpl) list.get(0);
//			defaultBillAddress = new AddressImpl(entry.street, entry.city, entry.state, entry.zip, false, true);
		} catch(DataAccessException e) { //this is a subclass of RuntimeException used by Spring
			LOG.warning("Rolling back transaction for readDefaultBillAddress(CustomerProfile custProfile) with query = " + readDefaultBillQuery);
			LOG.warning("Error details:\n" + e.getMessage());
		}
		return null;
	}

	@Transactional(value = "txManagerAccounts", propagation = Propagation.REQUIRES_NEW, readOnly = true)
	@Override
	public List<Address> readAllAddresses(CustomerProfile custProfile) throws DatabaseException {
		//this.custProfile = custProfile;
		queryType = Type.READ_ALL;
		readAllParams = new Object[]{custProfile.getCustId()};
		readAllTypes = new int[]{Types.INTEGER};
//    	dataAccessSS.atomicRead(this);
		try {
			List<Address> list = jdbcTemplate.query(readAllQuery, readAllParams, new MyRowMapperAll());
			addressList = new ArrayList<>();
			for(Address addr : list) {
				addressList.add(addr);
			}
			return addressList;
		} catch(DataAccessException e) { //this is a subclass of RuntimeException used by Spring
			LOG.warning("Rolling back transaction for readAllAddresses(CustomerProfile custProfile) with query = " + readAllQuery);
			LOG.warning("Error details:\n" + e.getMessage());
		}
		return null;
	}

	@Override
	public String getDbUrl() {
		DbConfigProperties props = new DbConfigProperties();
		return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());

	}
	@Override
	public String getQuery() {
		switch(queryType) {
			case INSERT :
				return insertQuery;
			case READ_ALL:
				return readAllQuery;
			case READ_DEFAULT_BILL:
				return readDefaultBillQuery;
			case READ_DEFAULT_SHIP:
				return readDefaultShipQuery;
			default :
				return null;
		}
	}
	@Override
	public Object[] getQueryParams() {
		switch(queryType) {
			case INSERT :
				return insertParams;
			case READ_ALL:
				return readAllParams;
			case READ_DEFAULT_BILL:
				return readDefaultBillParams;
			case READ_DEFAULT_SHIP:
				return readDefaultShipParams;
			default :
				return null;
		}
	}

	@Override
	public int[] getParamTypes() {
		switch(queryType) {
			case INSERT :
				return insertTypes;
			case READ_ALL:
				return readAllTypes;
			case READ_DEFAULT_BILL:
				return readDefaultBillTypes;
			case READ_DEFAULT_SHIP:
				return readDefaultShipTypes;
			default :
				return null;
		}
	}
	////// populate objects after reads ///////////

	@Override
	public void populateEntity(ResultSet rs) throws DatabaseException {
		switch(queryType) {
			case READ_ALL:
				populateAddressList(rs);
				break;
			case READ_DEFAULT_SHIP:
				populateDefaultShipAddress(rs);
				break;
			case READ_DEFAULT_BILL:
				populateDefaultBillAddress(rs);
				break;
			default:
				//do nothing
		}
	}
	void populateAddressList(ResultSet rs) throws DatabaseException {
		addressList = new LinkedList<Address>();
		if(rs != null){
			try {
				while(rs.next()) {
					address = new AddressImpl();
					String str = rs.getString(STREET);
					address.setStreet(str);
					address.setCity(rs.getString(CITY));
					address.setState(rs.getString(STATE));
					address.setZip(rs.getString(ZIP));
					boolean isShipping = rs.getInt(IS_SHIP) == 1;
					boolean isBilling = rs.getInt(IS_BILL) == 1;
					address.isShippingAddress(isShipping);
					address.isBillingAddress(isBilling);
					addressList.add(address);
				}
			}
			catch(SQLException e){
				throw new DatabaseException(e);
			}
		}
	}

	void populateDefaultShipAddress(ResultSet rs) throws DatabaseException {
		try {
			if(rs.next()){
				defaultShipAddress
						= new AddressImpl(rs.getString("shipaddress1"),
						rs.getString("shipcity"), rs.getString("shipstate"),
						rs.getString("shipzipcode"),true,false);
			}
		}
		catch(SQLException e){
			throw new DatabaseException(e);
		}
//		LOG.warning("Method populateDefaultShipAddress(ResultSet rs) not yet implemented" );
		//implement

	}
	void populateDefaultBillAddress(ResultSet rs) throws DatabaseException {
		try {
			if(rs.next()){
				defaultBillAddress
						= new AddressImpl(rs.getString("billaddress1"),
						rs.getString("billcity"), rs.getString("billstate"),
						rs.getString("billzipcode"),false,true);
			}
		}
		catch(SQLException e){
			throw new DatabaseException(e);
		}

//		LOG.warning("Method populateDefaultBillAddress(ResultSet rs) not yet implemented" );
		//implement
	}



	public static void main(String[] args){
		DbClassAddress dba = new DbClassAddress();
		CustomerProfile cp = new CustomerProfileImpl(1, "John", "Smith");
		try {
			System.out.println(dba.readAllAddresses(cp));
		}
		catch(DatabaseException e){
			e.printStackTrace();
		}
	}

}
