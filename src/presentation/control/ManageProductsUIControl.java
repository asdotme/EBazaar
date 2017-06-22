package presentation.control;

import presentation.data.CatalogPres;
import presentation.data.ManageProductsData;
import presentation.data.ProductPres;
import presentation.data.SessionCache;
import presentation.gui.AddCatalogPopup;
import presentation.gui.AddProductPopup;
import presentation.gui.MaintainCatalogsWindow;
import presentation.gui.MaintainProductsWindow;
import presentation.gui.ShoppingCartWindow;
import presentation.util.CacheReader;
import presentation.util.TableUtil;
import business.exceptions.BackendException;
import business.exceptions.UnauthorizedException;
import business.externalinterfaces.Catalog;
import business.externalinterfaces.Product;
import business.productsubsystem.ProductSubsystemFacade;
import business.usecasecontrol.ManageProductsController;
import business.util.Convert;
import business.util.DataUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import launch.Start;

import java.util.logging.*;


public enum ManageProductsUIControl {
	INSTANCE;
	private static final Logger LOG 
		= Logger.getLogger(ManageProductsUIControl.class.getSimpleName());
	//private ManageProductsController controller = (ManageProductsController) Start.ctx.getBean("pc");
	private Stage primaryStage;
	private Callback startScreenCallback;

	public void setPrimaryStage(Stage ps, Callback returnMessage) {
		primaryStage = ps;
		startScreenCallback = returnMessage;
	}

	// windows managed by this class
	MaintainCatalogsWindow maintainCatalogsWindow;
	MaintainProductsWindow maintainProductsWindow;
	AddCatalogPopup addCatalogPopup;
	AddProductPopup addProductPopup;

	// Manage catalogs
	private class MaintainCatalogsHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			maintainCatalogsWindow = new MaintainCatalogsWindow(primaryStage);
    		SessionCache cache = SessionCache.getInstance();
    		boolean loggedIn = (Boolean)cache.get(SessionCache.LOGGED_IN);
    		if(!loggedIn){
//				if (!loggedIn) {
//					LoginUIControl loginControl
//							= new LoginUIControl(maintainCatalogsWindow, maintainCatalogsWindow, this);
//					loginControl.startLogin();
//				} else {
//					doUpdate();
//				}
            	startScreenCallback.displayError("You are not authorized to access this page");
            	return;
    		}

			ObservableList<CatalogPres> list = ManageProductsData.INSTANCE.getCatalogList();
			maintainCatalogsWindow.setData(list);
			primaryStage.hide();
			try {
				Authorization.checkAuthorization(maintainCatalogsWindow, CacheReader.custIsAdmin());
				//show this screen if user is authorized
				maintainCatalogsWindow.show();
			} catch(UnauthorizedException ex) {   
            	startScreenCallback.displayError(ex.getMessage());
            	maintainCatalogsWindow.hide();
            	primaryStage.show();           	
            }

		}
	}
	
	public MaintainCatalogsHandler getMaintainCatalogsHandler() {
		return new MaintainCatalogsHandler();
	}
	
	private class MaintainProductsHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
    		SessionCache cache = SessionCache.getInstance();
    		boolean loggedIn = (Boolean)cache.get(SessionCache.LOGGED_IN);
    		if(!loggedIn){
            	startScreenCallback.displayError("You are not authorized to access this page");
            	return;
    		}

    		maintainProductsWindow = new MaintainProductsWindow(primaryStage);
			CatalogPres selectedCatalog = ManageProductsData.INSTANCE.getSelectedCatalog();
			if(selectedCatalog != null) {
				ObservableList<ProductPres> list = ManageProductsData.INSTANCE.getProductsList(selectedCatalog);
				maintainProductsWindow.setData(ManageProductsData.INSTANCE.getCatalogList(), list);
			}
			maintainProductsWindow.show();  
	        primaryStage.hide();
	        LOG.warning("Authorization has not been implemented for Maintain Products");
		}
	}
	public MaintainProductsHandler getMaintainProductsHandler() {
		return new MaintainProductsHandler();
	}
	
	private class BackButtonHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent evt) {		
			maintainCatalogsWindow.clearMessages();		
			maintainCatalogsWindow.hide();
			startScreenCallback.clearMessages();
			primaryStage.show();
		}
			
	}
	public BackButtonHandler getBackButtonHandler() {
		return new BackButtonHandler();
	}
	
	private class BackFromProdsButtonHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent evt) {		
			maintainProductsWindow.clearMessages();		
			maintainProductsWindow.hide();
			startScreenCallback.clearMessages();
			primaryStage.show();
		}			
	}
	
	public BackFromProdsButtonHandler getBackFromProdsButtonHandler() {
		return new BackFromProdsButtonHandler();
	}
	
	//////new
	/* Handles the request to delete selected row in catalogs table */
	private class DeleteCatalogHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent evt) {
			TableView<CatalogPres> table = maintainCatalogsWindow.getTable();
			ObservableList<CatalogPres> tableItems = table.getItems();
		    ObservableList<Integer> selectedIndices = table.getSelectionModel().getSelectedIndices();
		    ObservableList<CatalogPres> selectedItems = table.getSelectionModel()
					.getSelectedItems();
		    if(tableItems.isEmpty()) {
		    	maintainCatalogsWindow.displayError("Nothing to delete!");
		    } else if (selectedIndices == null || selectedIndices.isEmpty()) {
		    	maintainCatalogsWindow.displayError("Please select a row.");
		    } else {
		    	boolean result = false;
				try {
					result = ManageProductsData.INSTANCE.removeFromCatalogList(selectedItems);
				} catch (BackendException e) {
			    	maintainCatalogsWindow.displayError(
				    		"Selected catalog cannot be deleted from database!"); 
				}
			    if(!result) { 
			    	maintainCatalogsWindow.displayInfo("No items deleted.");
			    }
		    }
		}			
	}
	
	public DeleteCatalogHandler getDeleteCatalogHandler() {
		return new DeleteCatalogHandler();
	}
	
	/* Produces an AddCatalog popup in which user can add new catalog data */
	private class AddCatalogHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent evt) {
			addCatalogPopup = new AddCatalogPopup();
			addCatalogPopup.show(maintainCatalogsWindow);	
		}
	}
	public AddCatalogHandler getAddCatalogHandler() {
		return new AddCatalogHandler();
	} 
	
	/* Invoked by AddCatalogPopup - reads user input for a new catalog to be added to db */
	private class AddNewCatalogHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent evt) {
			//validate input
			TextField nameField = addCatalogPopup.getNameField();
			String catName = nameField.getText();
			if(catName.equals("")) 
				addCatalogPopup.displayError("Name field must be nonempty!");
			else {
				try {
					int newCatId = ManageProductsData.INSTANCE.saveNewCatalog(catName);
					CatalogPres catPres = ManageProductsData.INSTANCE.catalogPresFromData(newCatId, catName);
					ManageProductsData.INSTANCE.addToCatalogList(catPres);
					maintainCatalogsWindow.setData(ManageProductsData.INSTANCE.getCatalogList());
					TableUtil.refreshTable(maintainCatalogsWindow.getTable(), 
							ManageProductsData.INSTANCE.getManageCatalogsSynchronizer());
					addCatalogPopup.clearMessages();
					addCatalogPopup.hide();
				} catch(BackendException e) {
					addCatalogPopup.displayError("A database error has occurred. Check logs and try again later.");
				}
			}	
		}
		
	}
	public AddNewCatalogHandler getAddNewCatalogHandler() {
		return new AddNewCatalogHandler();
	} 

	
//////new
	
	
	/* Handles the request to delete selected row in catalogs table */
	private class DeleteProductHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent evt) {
			CatalogPres selectedCatalog = ManageProductsData.INSTANCE.getSelectedCatalog();
		    ObservableList<ProductPres> tableItems = ManageProductsData.INSTANCE.getProductsList(selectedCatalog);
			TableView<ProductPres> table = maintainProductsWindow.getTable();
			ObservableList<Integer> selectedIndices = table.getSelectionModel().getSelectedIndices();
		    ObservableList<ProductPres> selectedItems = table.getSelectionModel().getSelectedItems();
		    if(tableItems.isEmpty()) {
		    	maintainProductsWindow.displayError("Nothing to delete!");
		    } else if (selectedIndices == null || selectedIndices.isEmpty()) {
		    	maintainProductsWindow.displayError("Please select a row.");
		    } else {	    
		    	boolean result = false;
				try {
					result = ManageProductsData.INSTANCE.removeFromProductList(selectedCatalog, selectedItems);
				} catch (BackendException e) {
					maintainProductsWindow.displayError(
				    		"Selected product cannot be deleted from database!"); 
				}
			    if(!result) { 
			    	maintainProductsWindow.displayInfo("No items deleted.");
			    }
		    }			
		}			
	}
	
	public DeleteProductHandler getDeleteProductHandler() {
		return new DeleteProductHandler();
	}
	
	/* Produces an AddProduct popup in which user can add new product data */
	private class AddProductHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent evt) {
			addProductPopup = new AddProductPopup();
			String catNameSelected 
			   = ManageProductsData.INSTANCE.getSelectedCatalog().getCatalog().getName();
			addProductPopup.setCatalog(catNameSelected);
			addProductPopup.show(maintainProductsWindow);
		}
	}
	public AddProductHandler getAddProductHandler() {
		return new AddProductHandler();
	} 
	
	/* Invoked by AddCatalogPopup - reads user input for a new catalog to be added to db */
	private class AddNewProductHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent evt) {
			String name = addProductPopup.getName().getText().trim();
			String mfgDate = addProductPopup.getManufactureDate().getText().trim();
			String numAvail = addProductPopup.getNumAvail().getText().trim();
			String unitPrice = addProductPopup.getUnitPrice().getText().trim();
			String description = addProductPopup.getDescription().getText().trim();
			
			//validate input (better to implement in rules engine
			if(name.equals("")) 
				addProductPopup.displayError("Product Name field must be nonempty!");
			else if(mfgDate.equals("")) 
				addProductPopup.displayError("Manufacture Date field must be nonempty!");
			else if(numAvail.equals("")) 
				addProductPopup.displayError("Number in Stock field must be nonempty!");
			else if(unitPrice.equals("")) 
				addProductPopup.displayError("Unit Price field must be nonempty!");
			else if(description.equals("")) 
				addProductPopup.displayError("Description field must be nonempty!");
			else {
				//code this as in AddNewCatalogHandler (above)		
				try {
					CatalogPres catPres = ManageProductsData.INSTANCE.getSelectedCatalog();
					ProductPres product = ManageProductsData.INSTANCE.productPresFromData(catPres.getCatalog(), name, mfgDate, Integer.parseInt(numAvail), Double.parseDouble(unitPrice), description);
					int newProductId = ManageProductsData.INSTANCE.saveNewProduct(product.getProduct());
					Product p = product.getProduct();
					p.setProductId(newProductId);
					product.setProduct(p);
					
					ManageProductsData.INSTANCE.addToProdList(catPres, product);
					maintainProductsWindow.setData(ManageProductsData.INSTANCE.getProductsList(catPres));
					TableUtil.refreshTable(maintainProductsWindow.getTable(), 
							ManageProductsData.INSTANCE.getManageProductsSynchronizer());
					addProductPopup.clearMessages();
					addProductPopup.hide();
				} catch(BackendException e) {
					addProductPopup.displayError("A database error has occurred. Check logs and try again later.");
				}
			}	
		}
		
	}
	public AddNewProductHandler getAddNewProductHandler() {
		return new AddNewProductHandler();
	} 

}
