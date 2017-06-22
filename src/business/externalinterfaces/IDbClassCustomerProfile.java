package business.externalinterfaces;

import business.externalinterfaces.CustomerProfile;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbClass;

/**
 * Created by Asme on 10/23/2016.
 */
public interface IDbClassCustomerProfile extends DbClass {
    public CustomerProfile readCustomerProfile(Integer custId) throws DatabaseException;
    }
