package business.shoppingcartsubsystem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import business.exceptions.BackendException;
import business.exceptions.BusinessException;
import business.exceptions.RuleException;
import business.externalinterfaces.Address;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.DbClassShoppingCartForTest;
import business.externalinterfaces.Rules;
import business.externalinterfaces.ShoppingCart;
import business.externalinterfaces.ShoppingCartSubsystem;
import middleware.exceptions.DatabaseException;

/**
 * This represents a class at the "Service" layer -- responsible for fulfilling
 * data management requests. The @Service annotation alerts Spring that this class
 * should be registered as a bean in the spring engine. The @Inject annotation
 * on IDbClassShoppingCart causes Spring to instantiate the class using
 * the default constructor.
 * 
 * The parameter "sss" in the Service annotation gives 
 * ShoppingCartSubsystemFacade, as a Spring bean,
 * a name. This name is used when getBean is called in the startup method.
 */

@Service("sss")
public class ShoppingCartSubsystemFacade implements ShoppingCartSubsystem {
	
	private static final Logger LOG = Logger.getLogger(ShoppingCartSubsystemFacade.class.getName());
	ShoppingCartImpl liveCart = new ShoppingCartImpl(new LinkedList<CartItem>());
	ShoppingCartImpl savedCart;
	Integer shopCartId;
	CustomerProfile customerProfile;
	Logger log = Logger.getLogger(this.getClass().getPackage().getName());

	@Inject
	private IDbClassShoppingCart IDbClassShoppingCart;
	

	public IDbClassShoppingCart getDbClassClassShoppingCart() {
			return IDbClassShoppingCart;
	}
	
	// interface methods
	public void setCustomerProfile(CustomerProfile customerProfile) {
		this.customerProfile = customerProfile;
	}
	
	public void makeSavedCartLive() {
		liveCart = savedCart;
	}
	
	public ShoppingCart getLiveCart() {
		return liveCart;
	}

	

	public void retrieveSavedCart() throws BackendException {
		try {
			//DbClassShoppingCart dbClass = new DbClassShoppingCart();
			//ShoppingCartImpl cartFound = dbClass.retrieveSavedCart(customerProfile);
			
			ShoppingCartImpl cartFound = IDbClassShoppingCart.retrieveSavedCart(customerProfile);
			if(cartFound == null) {
				savedCart = new ShoppingCartImpl(new ArrayList<CartItem>());
			} else {
				savedCart = cartFound;
			}
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}

	}
	
	@Override
	public void setShippingAddress(Address addr) {
		liveCart.setShipAddress(addr);
		
	}

	@Override
	public void setBillingAddress(Address addr) {
		liveCart.setBillAddress(addr);
		
	}

	@Override
	public void setPaymentInfo(CreditCard cc) {
		liveCart.setPaymentInfo(cc);
		
	}
	
	public void setCartItems(List<CartItem> list) {
		liveCart.setCartItems(list);
	}
	
	public List<CartItem> getCartItems() {
		return liveCart.getCartItems();
	}
	
	//static methods
	public static CartItem createCartItem(String productName, String quantity,
            String totalprice) {
		try {
			return new CartItemImpl(productName, quantity, totalprice);
		} catch(BackendException e) {
			throw new RuntimeException("Can't create a cartitem because of productid lookup: " + e.getMessage());
		}
	}


	
	//interface methods for testing
	
	public ShoppingCart getEmptyCartForTest() {
		return new ShoppingCartImpl();
	}

	
	public CartItem getEmptyCartItemForTest() {
		return new CartItemImpl();
	}

	
	
	
	
	@Override
	public void runShoppingCartRules() throws RuleException, BusinessException {
		// TODO runShoppingCartRules()  ShoppingCartRules are not implemented!
		//LOG.warning("Method ShoppingCartSubsystem.runShoppingCartRules() has not been implemented!");		
		
		Rules transferObject = new RulesShoppingCart(liveCart);
		transferObject.runRules();
	}
	

	@Override
	public List<CartItem> getLiveCartItems()  throws BackendException{
		// TODO getLiveCartItems() only a stub data		
			
		/*LOG.warning("Method ShoppingCartSubsystem.getLiveCartItems() has not been implemented!");
		CartItemImpl cartItem = new CartItemImpl("test product", "2","5.00");		
		List<CartItem> list = new ArrayList<CartItem>();
		list.add(cartItem);
		return list;*/
		
		
		if(liveCart.getCartItems().isEmpty()){
			return new ArrayList<CartItem>();
		}
		else{
			return liveCart.getCartItems();
		}
				
	}

	@Override
	public void clearLiveCart() {
		// TODO Implementation of clearLiveCart needed here
		LOG.warning("Method ShoppingCartSubsystem.clearLiveCart() has not been implemented!");
	}

	@Override
	public void runFinalOrderRules() throws RuleException, BusinessException {
		// TODO FinalOrderRules are not implemented!
		//LOG.warning("Method ShoppingCartSubsystem.runFinalOrderRules() has not been implemented!");
		
		Rules transferObject = new RulesFinalOrder(liveCart);
		transferObject.runRules();
	}

	@Override
	public void saveLiveCart() throws BackendException {
		// TODO saveLiveCart() is not implemented!
		//LOG.warning("Method ShoppingCartSubsystem.saveLiveCart() has  not been implemented!");		
		
		try {
			//DbClassShoppingCart dbClass = new DbClassShoppingCart();
			//dbClass.saveCart(customerProfile, liveCart);
			
			IDbClassShoppingCart.saveCart(customerProfile, liveCart);
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}
		
	
	}

	@Override
	public ShoppingCart getGenericShoppingCart() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbClassShoppingCartForTest getGenericShoppingCartForTest() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
