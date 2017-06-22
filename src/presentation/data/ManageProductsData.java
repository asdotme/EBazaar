package presentation.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import launch.Start;
import business.exceptions.BackendException;
import business.externalinterfaces.Catalog;
import business.externalinterfaces.Product;
import business.util.Convert;
import business.productsubsystem.ProductSubsystemFacade;
import business.usecasecontrol.ManageProductsController;

public enum ManageProductsData {
	INSTANCE;

	ManageProductsController pc = (ManageProductsController) Start.ctx.getBean("pc"); // new
																						// ManageProductsController();

	//////// Catalogs List model
	private ObservableList<CatalogPres> catalogList = readCatalogsFromDataSource();

	/** Initializes the catalogList */
	private ObservableList<CatalogPres> readCatalogsFromDataSource() {
		List<Catalog> catList = null;
		try {
			catList = pc.getCatalogs();
		} catch (BackendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<CatalogPres> catPresList = catList.stream().map(c -> new CatalogPres(c)).collect(Collectors.toList());

		return FXCollections.observableList(catPresList);
		// return FXCollections.observableList(DefaultData.CATALOG_LIST_DATA);
	}

	private CatalogPres defaultCatalog = readDefaultCatalogFromDataSource();

	private CatalogPres readDefaultCatalogFromDataSource() {
		return catalogList.get(0);
	}

	public CatalogPres getDefaultCatalog() {
		return defaultCatalog;
	}

	private CatalogPres selectedCatalog = defaultCatalog;

	public void setSelectedCatalog(CatalogPres selCatalog) {
		selectedCatalog = selCatalog;
	}

	public CatalogPres getSelectedCatalog() {
		return selectedCatalog;
	}

	/** Delivers the already-populated catalogList to the UI */
	public ObservableList<CatalogPres> getCatalogList() {
		return catalogList;
	}

	public CatalogPres catalogPresFromData(int id, String name) {
		Catalog cat = ProductSubsystemFacade.createCatalog(id, name);
		return new CatalogPres(cat);
	}

	public void addToCatalogList(CatalogPres catPres) {
		ObservableList<CatalogPres> newCatalogs = FXCollections.observableArrayList(catPres);

		// Place the new item at the bottom of the list
		// catalogList is guaranteed to be non-null
		boolean result = catalogList.addAll(newCatalogs);
		if (result) { // must make this catalog accessible in productsMap
			productsMap.put(catPres, FXCollections.observableList(new ArrayList<ProductPres>()));
		}
	}

	/**
	 * This method looks for the 0th element of the toBeRemoved list in
	 * catalogList and if found, removes it. In this app, removing more than one
	 * catalog at a time is not supported.
	 * 
	 * This method also updates the productList by removing the products that
	 * belong to the Catalog that is being removed.
	 * 
	 * Also: If the removed catalog was being stored as the selectedCatalog, the
	 * next item in the catalog list is set as "selected"
	 * 
	 * @throws BackendException
	 */
	public boolean removeFromCatalogList(ObservableList<CatalogPres> toBeRemoved) throws BackendException {
		boolean result = false;
		if (toBeRemoved != null && !toBeRemoved.isEmpty()) {
			CatalogPres item = toBeRemoved.get(0);
			// If delete from database succeed
			if (pc.deleteCatalog(item.getCatalog())) {
				result = catalogList.remove(item);
				if (result) {// update productsMap
					if (item.equals(selectedCatalog)) {
						if (!catalogList.isEmpty()) {
							selectedCatalog = catalogList.get(0);
						} else {
							selectedCatalog = null;
						}
					}
					productsMap.remove(item);
				} else
					return false;
			}
		}
		return result;
	}

	//////////// Products List model
	private ObservableMap<CatalogPres, List<ProductPres>> productsMap = readProductsFromDataSource();

	/** Initializes the productsMap */
	private ObservableMap<CatalogPres, List<ProductPres>> readProductsFromDataSource() {
		HashMap<CatalogPres, List<ProductPres>> map = new HashMap();
		catalogList.forEach(c -> {
			try {
				map.put(c, pc.getProducts(c.getCatalog()).stream().map(p -> new ProductPres(p))
						.collect(Collectors.toList()));
			} catch (BackendException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		ObservableMap<CatalogPres, List<ProductPres>> observableMap = FXCollections.observableMap(map);
		return observableMap;
		// return DefaultData.PRODUCT_LIST_DATA;
	}

	/** Delivers the requested products list to the UI */
	public ObservableList<ProductPres> getProductsList(CatalogPres catPres) {
		return FXCollections.observableList(productsMap.get(catPres));
	}

	public ProductPres productPresFromData(Catalog c, String name, String date, // MM/dd/yyyy
			int numAvail, double price, String desc) {

		Product product = ProductSubsystemFacade.createProduct(c, name, Convert.localDateForString(date), numAvail,
				price, desc);
		return new ProductPres(product);
	}

	public void addToProdList(CatalogPres catPres, ProductPres prodPres) {
		ObservableList<ProductPres> newProducts = FXCollections.observableArrayList(prodPres);
		List<ProductPres> specifiedProds = productsMap.get(catPres);

		// Place the new item at the bottom of the list
		specifiedProds.addAll(newProducts);

	}

	/**
	 * This method looks for the 0th element of the toBeRemoved list and if
	 * found, removes it. In this app, removing more than one product at a time
	 * is not supported.
	 * 
	 * @throws BackendException
	 */
	public boolean removeFromProductList(CatalogPres cat, ObservableList<ProductPres> toBeRemoved)
			throws BackendException {
		if (toBeRemoved != null && !toBeRemoved.isEmpty()) {
			ProductPres item = toBeRemoved.get(0);
			// If delete from database succeed
			if (pc.deleteProduct(item.getProduct())) { 
				return productsMap.get(cat).remove(item);
			}
			return false;
		}
		return false;
	}

	// Synchronizers
	public class ManageProductsSynchronizer implements Synchronizer {
		@SuppressWarnings("rawtypes")
		@Override
		public void refresh(ObservableList list) {
			productsMap.put(selectedCatalog, list);
		}
	}

	public ManageProductsSynchronizer getManageProductsSynchronizer() {
		return new ManageProductsSynchronizer();
	}

	private class ManageCatalogsSynchronizer implements Synchronizer {
		@SuppressWarnings("rawtypes")
		@Override
		public void refresh(ObservableList list) {
			catalogList = list;
		}
	}

	public ManageCatalogsSynchronizer getManageCatalogsSynchronizer() {
		return new ManageCatalogsSynchronizer();
	}

	public int saveNewCatalog(String catName) throws BackendException {
		return pc.saveNewCatalog(catName);
	}

	public int saveNewProduct(Product product) throws BackendException {
		return pc.saveNewProduct(product);
	}
}
