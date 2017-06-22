/**
 * 
 */
package business.externalinterfaces;

import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbClass;

/**
 * @author Yared
 *
 */
public interface DbClassShoppingCartForTest extends DbClass {
    public void saveCart(CustomerProfile custProfile, ShoppingCart cart) 
    		throws DatabaseException;
}
