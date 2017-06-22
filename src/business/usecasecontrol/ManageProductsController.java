
package business.usecasecontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import middleware.exceptions.DatabaseException;

import business.exceptions.BackendException;
import business.externalinterfaces.Catalog;
import business.externalinterfaces.Product;
import business.externalinterfaces.ProductSubsystem;
import business.productsubsystem.ProductSubsystemFacade;

@Service("pc")
public class ManageProductsController   {
    
    private static final Logger LOG = 
    	Logger.getLogger(ManageProductsController.class.getName());
    
    @Inject
    private ProductSubsystem pss; 
    
//    public List<Product> getProductsList(String catalog) throws BackendException {
//    	LOG.warning("ManageProductsController method getProductsList has not been implemented");
//    	//return pss.getProductList(catalog);
//    	return new ArrayList<Product>();
//    }
    
    public List<Product> getProducts(Catalog catalog) throws BackendException {
    	return pss.getProductList(catalog);
    }
    
    public List<Catalog> getCatalogs() throws BackendException {
    	return pss.getCatalogList();
    }
    
    public int saveNewCatalog(String catName) throws BackendException {
    	return pss.saveNewCatalog(catName);
    }
    
    public boolean deleteCatalog(Catalog catalog) throws BackendException {
    	return pss.deleteCatalog(catalog);
    }
    
    public int saveNewProduct(Product product) throws BackendException {
    	return pss.saveNewProduct(product);
    }
    
    public boolean deleteProduct(Product product) throws BackendException  {
    	return pss.deleteProduct(product);
    }     
}
