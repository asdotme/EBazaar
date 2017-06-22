package business.externalinterfaces;

import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbClass;

/**
 * Created by Asme on 10/23/2016.
 */
public interface IDbClassCreditCard extends DbClass {
    public CreditCard readDefaultPaymentInfo(CustomerProfile custProfile) throws DatabaseException;

}
