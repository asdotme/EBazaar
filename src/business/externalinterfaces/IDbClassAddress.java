package business.externalinterfaces;

/**
 * Created by Asme on 10/23/2016.
 */

import business.externalinterfaces.Address;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.DbClassAddressForTest;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbClass;
import java.util.List;

public interface IDbClassAddress extends DbClass, DbClassAddressForTest {
     void saveAddress(CustomerProfile custProfile) throws DatabaseException;
     Address readDefaultShipAddress(CustomerProfile custProfile) throws DatabaseException;
    Address readDefaultBillAddress(CustomerProfile custProfile) throws DatabaseException;
    public List<Address> readAllAddresses(CustomerProfile custProfile) throws DatabaseException;
     public void setAddress(Address addr);



}
