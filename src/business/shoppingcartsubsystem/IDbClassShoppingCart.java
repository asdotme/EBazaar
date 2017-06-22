/**
 * 
 */
package business.shoppingcartsubsystem;

import java.sql.ResultSet;

import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.ShoppingCart;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbClass;

/**
 * @author Yared
 *
 */
public interface IDbClassShoppingCart extends DbClass {
	
	 public void saveCart(CustomerProfile custProfile, ShoppingCart cart) 
	    		throws DatabaseException ;
	 
	public ShoppingCartImpl retrieveSavedCart(CustomerProfile custProfile) throws DatabaseException ;
	 
}
