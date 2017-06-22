package business.customersubsystem;

import java.util.List;
import java.util.stream.Collectors;

import java.util.logging.Logger;

import business.externalinterfaces.*;
import launch.Start;
import middleware.creditverifcation.CreditVerificationFacade;
import middleware.exceptions.DatabaseException;
import middleware.exceptions.MiddlewareException;
import middleware.externalinterfaces.CreditVerification;
import middleware.externalinterfaces.CreditVerificationProfile;
import business.exceptions.BackendException;
import business.exceptions.BusinessException;
import business.exceptions.RuleException;
import business.ordersubsystem.OrderSubsystemFacade;
import business.shoppingcartsubsystem.ShoppingCartSubsystemFacade;
import business.util.DataUtil;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service("css")
public class CustomerSubsystemFacade implements CustomerSubsystem {
	private static final Logger LOG =
		Logger.getLogger(CustomerSubsystemFacade.class.getName());
	@Inject
	private IDbClassAddress dbClassAddress;

	@Inject
	private IDbClassCustomerProfile dbClassCustomerProfile;

	@Inject
	private IDbClassCreditCard dbClassCreditCard;

	ShoppingCartSubsystem shoppingCartSubsystem;
	OrderSubsystem orderSubsystem;
	List<Order> orderHistory;
	AddressImpl defaultShipAddress;
	AddressImpl defaultBillAddress;
	CreditCardImpl defaultPaymentInfo;
	CustomerProfileImpl customerProfile;

	/**
	 * Loads order history, default addresses, default payment info,
	 * saved shopping cart,cust profile after login
	 */
	public void initializeCustomer(Integer id, List<CartItem> cartItems, int authorizationLevel) 
			throws BackendException {
		//validate cartItems
		if(cartItems == null) throw new IllegalArgumentException("CustomerSubsystemFacade.initializeCustomer"
				+ " received a null value for cartItems");
		boolean isAdmin = (authorizationLevel >= 1);
		loadCustomerProfile(id, isAdmin);
		loadDefaultShipAddress();
		loadDefaultBillAddress();
		loadDefaultPaymentInfo();
		shoppingCartSubsystem = (ShoppingCartSubsystemFacade) Start.ctx.getBean("sss");
//		shoppingCartSubsystem = new ShoppingCartSubsystemFacade();

		shoppingCartSubsystem.setCustomerProfile(customerProfile);
		//set default address and payment info so that shopping cart can be saved, if this is desired
		shoppingCartSubsystem.setShippingAddress(defaultShipAddress);
		shoppingCartSubsystem.setBillingAddress(defaultBillAddress);
		shoppingCartSubsystem.setPaymentInfo(defaultPaymentInfo);
		//places the previously saved shopping cart into the ShoppingCartSubsystem
		shoppingCartSubsystem.retrieveSavedCart();
		//sets any live cart items obtained before login into the ShoppingCartSubsystem's live cart
		shoppingCartSubsystem.getLiveCart().setCartItems(cartItems);
		loadOrderData();
	}

	void loadCustomerProfile(int id, boolean isAdmin) throws BackendException {
		try {
//			DbClassCustomerProfileTest dbclass = new DbClassCustomerProfileTest();
			customerProfile = (CustomerProfileImpl) dbClassCustomerProfile.readCustomerProfile(id);
			customerProfile.setIsAdmin(isAdmin);
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}
	}

	void loadDefaultShipAddress() throws BackendException {
		try {
//			DbClassAddress dbClassAddress=new DbClassAddress();
			defaultShipAddress=(AddressImpl)dbClassAddress.readDefaultShipAddress(customerProfile);
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}

		// implement
//		LOG.warning("Method CustomerSubsystemFacade.loadDefaultShipAddress has not been implemented.");
	}

	void loadDefaultBillAddress() throws BackendException {
		try {
//			DbClassAddress dbClassAddress=new DbClassAddress();
			defaultBillAddress=(AddressImpl)dbClassAddress.readDefaultBillAddress(customerProfile);
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}

		// implement
//		LOG.warning("Method CustomerSubsystemFacade.loadDefaultBillAddress has not been implemented.");
	}

	void loadDefaultPaymentInfo() throws BackendException {
		try {
//			DbClassCreditCard dbClassCreditCard=new DbClassCreditCard();
			defaultPaymentInfo= (CreditCardImpl) dbClassCreditCard.readDefaultPaymentInfo(customerProfile);
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}
		LOG.warning("Method CustomerSubsystemFacade.loadDefaultPaymentInfo has not been implemented.");
	}

	void loadOrderData() throws BackendException {
		//implement
		// retrieve the order history for the customer and store here
		orderSubsystem=(OrderSubsystemFacade)Start.ctx.getBean("oss");
		orderSubsystem.setCustomerProfile(customerProfile);
        orderHistory = orderSubsystem.getOrderHistory();
		LOG.warning("Method CustomerSubsystemFacade.loadOrderData has not been implemented.");

	}

	/**
	 * Customer Subsystem is responsible for obtaining all the data needed by
	 * Credit Verif system -- it does not (and should not) rely on the
	 * controller for this data.
	 */
	@Override
	public void checkCreditCard() throws BusinessException {
		List<CartItem> items = shoppingCartSubsystem.getCartItems();
		ShoppingCart theCart = shoppingCartSubsystem.getLiveCart();
		Address billAddr = theCart.getBillingAddress();
		CreditCard cc = theCart.getPaymentInfo();
		double amount = DataUtil.computeTotal(items);
		CreditVerification creditVerif = new CreditVerificationFacade();
		try {
			CreditVerificationProfile profile = CreditVerificationFacade.getCreditProfileShell();
			profile.setFirstName(customerProfile.getFirstName());
			profile.setLastName(customerProfile.getLastName());
			profile.setAmount(amount);
			profile.setStreet(billAddr.getStreet());
			profile.setCity(billAddr.getCity());
			profile.setState(billAddr.getState());
			profile.setZip(billAddr.getZip());
			profile.setCardNum(cc.getCardNum());
			profile.setExpirationDate(cc.getExpirationDate());
			creditVerif.checkCreditCard(profile);
		} catch (MiddlewareException e) {
			throw new BusinessException(e);
		}
	}

	/**
	 * Returns true if user has admin access
	 */
	public boolean isAdmin() {
		return customerProfile.isAdmin();
	}

	/**
	 * Use for saving an address created by user
	 */
	public void saveNewAddress(Address addr) throws BackendException {
		try {
//			DbClassAddress dbClass = new DbClassAddress();
			dbClassAddress.setAddress(addr);
			dbClassAddress.saveAddress(customerProfile);
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}
	}

	public CustomerProfile getCustomerProfile() {

		return customerProfile;
	}

	public Address getDefaultShippingAddress() {
		return defaultShipAddress;
	}

	public Address getDefaultBillingAddress() {
		return defaultBillAddress;
	}

	public CreditCard getDefaultPaymentInfo() {
		return defaultPaymentInfo;
	}

	/** 
     * Use to supply all stored shipping addresses of a customer when he wishes to select an
	 * address in ship/bill window 
	 */
    public List<Address> getAllShipAddresses() throws BackendException {
    	try {
//	    	DbClassAddress dbClass = new DbClassAddress();
	    	List<Address> list = dbClassAddress.readAllAddresses(customerProfile);
	    	return list.stream()
	    			   .filter(addr -> addr.isShippingAddress())
	    			   .collect(Collectors.toList());
    	} catch(DatabaseException e) {
			throw new BackendException(e);
		}
    }
    
    /** 
     * Use to supply all stored billing addresses of a customer when he wishes to select an
	 * address in ship/bill window 
	 */
    public List<Address> getAllBillAddresses() throws BackendException {
    	try {
//	    	DbClassAddress dbClass = new DbClassAddress();
	    	List<Address> list = dbClassAddress.readAllAddresses(customerProfile);
	    	return list.stream()
	    			   .filter(addr -> addr.isBillingAddress())
	    			   .collect(Collectors.toList());
    	} catch(DatabaseException e) {
			throw new BackendException(e);
		}
    }

	public Address runAddressRules(Address addr) throws RuleException, BusinessException {

		Rules transferObject = new RulesAddress(addr);
		transferObject.runRules();

		// updates are in the form of a List; 0th object is the necessary
		// Address
		AddressImpl update = (AddressImpl) transferObject.getUpdates().get(0);
		return update;
	}
	
	public ShoppingCartSubsystem getShoppingCart() {
		return shoppingCartSubsystem;
	}

	public void runPaymentRules(Address addr, CreditCard cc) throws RuleException, BusinessException {
		Rules transferObject = new RulesPayment(addr, cc);
		transferObject.runRules();
	}

	public static Address createAddress(String street, String city, String state, String zip, boolean isShip,
			boolean isBill) {
		return new AddressImpl(street, city, state, zip, isShip, isBill);
	}

	public static CustomerProfile createCustProfile(Integer custid, String firstName, String lastName,
			boolean isAdmin) {
		return new CustomerProfileImpl(custid, firstName, lastName, isAdmin);
	}

	public static CreditCard createCreditCard(String nameOnCard, String expirationDate, String cardNum,
			String cardType) {
		return new CreditCardImpl(nameOnCard, expirationDate, cardNum, cardType);
	}

	@Override
	public List<Order> getOrderHistory() {
		return orderHistory;
	}



    /////////////////// For unit testing only ////////////////

//	public DbClassAddressForTest getGenericDbClassAddress() {
//		return dbClassAddress;
//	}
    @Override
	public IDbClassAddress getGenericDbClassAddress() {
		return new DbClassAddress();
}
	@Override
	public Address getGenericAddress() {
		return new AddressImpl("1000 N 4th St.","FairField","Iow","52557",true,false);
	}

	public CustomerProfile getGenericCustomerProfile() {
		return new CustomerProfileImpl(Integer.valueOf(1), "First", "Last");
	}
	public IDbClassCustomerProfile getGenericDbClassCustomerProfile(){
		return new DbClassCustomerProfile();
	}
	@Override
	public CreditCard getGenericCreditCard() {
		return new CreditCardImpl("First Last","12/29/2019","0123456789","Visa");
	}
	@Override
	public IDbClassCreditCard getGenericDbClassCreditCard() {
		return new DbClassCreditCard();
	}
























	@Override
	public void setShippingAddressInCart(Address addr) {
		LOG.warning("Method CustomerSubsystemFacade.setShippingAddressInCart has not been implemented.");		
	}

	@Override
	public void setBillingAddressInCart(Address addr) {
		LOG.warning("Method CustomerSubsystemFacade.setBillingAddressInCart has not been implemented.");		
	}

	@Override
	public void setPaymentInfoInCart(CreditCard cc) {
		LOG.warning("Method CustomerSubsystemFacade.setPaymentInfoInCart has not been implemented.");		
	}

	@Override
	public void submitOrder() throws BackendException {
		LOG.warning("Method CustomerSubsystemFacade.submitOrder has not been implemented.");		
	}

	@Override
	public void refreshAfterSubmit() throws BackendException {
		LOG.warning("Method CustomerSubsystemFacade.refreshAfterSubmit has not been implemented.");		
	}

	@Override
	public void saveShoppingCart() throws BackendException {
		LOG.warning("Method CustomerSubsystemFacade.saveShoppingCart has not been implemented.");		
	}
}
