package business.usecasecontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import business.BusinessConstants;
import business.RulesQuantity;
import business.customersubsystem.CustomerSubsystemFacade;
import business.customersubsystem.RulesPayment;
import business.exceptions.BackendException;
import business.exceptions.BusinessException;
import business.exceptions.RuleException;
import business.externalinterfaces.*;
import business.ordersubsystem.OrderSubsystemFacade;
import business.productsubsystem.ProductSubsystemFacade;
import business.shoppingcartsubsystem.ShoppingCartSubsystemFacade;
import launch.Start;
import presentation.data.SessionCache;
import presentation.util.CacheReader;

public class CheckoutController  {
	ProductSubsystem pss = (ProductSubsystemFacade) Start.ctx.getBean("pss");

	private static final Logger LOG = Logger.getLogger(CheckoutController.class
			.getPackage().getName());
	
	
	public void runShoppingCartRules(ShoppingCartSubsystem shopCart) throws RuleException, BusinessException {
		//implement
		shopCart.runShoppingCartRules();

	}

	
	public void runPaymentRules(Address addr, CreditCard cc) throws RuleException, BusinessException {
		Rules rules=new RulesPayment(addr,cc);
		rules.runRules();

		//implement
	}
	
	public Address runAddressRules(CustomerSubsystem cust, Address addr) throws RuleException, BusinessException {

		return cust.runAddressRules(addr);
	}
	
	public List<Address> getShippingAddresses(CustomerSubsystem cust) throws BackendException {
		return cust.getAllShipAddresses();
	}
	
	public List<Address> getBillingAddresses(CustomerSubsystem cust) throws BackendException {
		return cust.getAllShipAddresses();
	}
	
	/** Asks the ShoppingCart Subsystem to run final order rules */
	public void runFinalOrderRules(ShoppingCartSubsystem scss) throws RuleException, BusinessException {
		scss.runFinalOrderRules();
		//implement
	}
	
	/** Asks Customer Subsystem to check credit card against 
	 *  Credit Verification System 
	 */
	public void verifyCreditCard(CustomerSubsystem cust) throws BusinessException {
		cust.checkCreditCard();
		//implement
	}
	
	public void saveNewAddress(CustomerSubsystem cust, Address addr) throws BackendException {		
		cust.saveNewAddress(addr);
	}
	
	/** Asks Customer Subsystem to submit final order */
	public void submitFinalOrder() throws BackendException {
		OrderSubsystem orderSubsystem=(OrderSubsystemFacade)Start.ctx.getBean("oss");
		orderSubsystem.setCustomerProfile(CacheReader.readCustomer().getCustomerProfile());
//		OrderSubsystemFacade orderSubsystemFacade=new OrderSubsystemFacade(CacheReader.readCustomer().getCustomerProfile());
		orderSubsystem.submitOrder((ShoppingCart) CacheReader.readCustomer().getShoppingCart().getLiveCart());
	}


}
