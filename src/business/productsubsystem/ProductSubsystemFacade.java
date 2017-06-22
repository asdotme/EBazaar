package business.productsubsystem;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import business.exceptions.BackendException;
import business.externalinterfaces.Catalog;
import business.externalinterfaces.CatalogTypes;
import business.externalinterfaces.IDbClassCatalog;
import business.externalinterfaces.IDbClassCatalogTypes;
import business.externalinterfaces.IDbClassProduct;
import business.externalinterfaces.Product;
import business.externalinterfaces.ProductSubsystem;
import business.util.TwoKeyHashMap;
import launch.Start;
import middleware.exceptions.DatabaseException;

@Service("pss")
public class ProductSubsystemFacade implements ProductSubsystem {
	private static final Logger LOG = 
			Logger.getLogger(ProductSubsystemFacade.class.getPackage().getName());
	
	@Inject
	private IDbClassProduct dbclassProduct;
	
	@Inject
	private IDbClassCatalog dbclassCatalog;

	@Inject
	private IDbClassCatalogTypes dbclassCatalogTypes;
	
	public static Catalog createCatalog(int id, String name) {
		return new CatalogImpl(id, name);
	}
	public static Product createProduct(Catalog c, String name, 
			LocalDate date, int numAvail, double price, String desc) {
		return new ProductImpl(c, name, date, numAvail, price, desc);
	}
	public static Product createProduct(Catalog c, Integer pi, String pn, int qa, 
			double up, LocalDate md, String desc) {
		return new ProductImpl(c, pi, pn, qa, up, md, desc);
	}
	
	/** obtains product for a given product name */
    public Product getProductFromName(String prodName) throws BackendException {
    	try {
			return dbclassProduct.readProduct(getProductIdFromName(prodName));
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}	
    }
    public Integer getProductIdFromName(String prodName) throws BackendException {
		try {
			TwoKeyHashMap<Integer,String,Product> table = dbclassProduct.readProductTable();
			return table.getFirstKey(prodName);
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}
		
	}
    public Product getProductFromId(Integer prodId) throws BackendException {
		try {
			return dbclassProduct.readProduct(prodId);
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}
	}
    public CatalogTypes getCatalogTypes() throws BackendException {
    	try {
			return dbclassCatalogTypes.getCatalogTypes();
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}
    }
    
    public List<Catalog> getCatalogList() throws BackendException {
    	try {
			//return dbclassCatalogTypes.getCatalogTypes().getCatalogs();
			return dbclassCatalog.getCatalogs();
		} catch(Exception e) {
		//} catch(DatabaseException e) {
			throw new BackendException(e);
		}
		
    }
    
    public List<Product> getProductList(Catalog catalog) throws BackendException {
    	try {
    		return dbclassProduct.readProductList(catalog);
    	} catch(DatabaseException e) {
    		throw new BackendException(e);
    	}
    }
    
    
	public int readQuantityAvailable(Product product) throws BackendException {
    	try {
    		Product p = dbclassProduct.readProduct(product.getProductId());
    		return p.getQuantityAvail();
    	} catch(DatabaseException e) {
    		throw new BackendException(e);
    	}
	}
	
	public int saveNewCatalog(String catalogName) throws BackendException {
		try {
			return dbclassCatalog.saveNewCatalog(catalogName);
		} catch(DatabaseException e) {
    		throw new BackendException(e);
    	}
	}
	
	@Override
	public Catalog getCatalogFromName(String catName) throws BackendException {
		// TODO Auto-generated method stub
		try {
			return dbclassCatalogTypes.getCatalogTypes().getCatalogs().stream()
					.filter(c -> c.getName() == catName)
					.findFirst().get();
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}
	}
	@Override
	public int saveNewProduct(Product product) throws BackendException {
		try {
			return dbclassProduct.saveNewProduct(product);
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}	
	}
	@Override
	public boolean deleteProduct(Product product) throws BackendException {
		try {
			return dbclassProduct.deleteProduct(product);
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}		
	}
	@Override
	public boolean deleteCatalog(Catalog catalog) throws BackendException {
		try {
			return dbclassCatalog.deleteCatalog(catalog);
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}	
	}
	
    //TESTING
	@Override
	public IDbClassCatalog getGenericDbClassCatalog() {
		return new DbClassCatalog();
	}
	@Override
	public IDbClassProduct getGenericDbClassProduct() {
		return new DbClassProduct();
	}	
}
