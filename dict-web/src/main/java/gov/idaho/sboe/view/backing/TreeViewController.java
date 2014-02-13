package gov.idaho.sboe.view.backing;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import oracle.adf.view.faces.component.core.data.CoreTree;

import oracle.adf.view.faces.component.core.nav.CoreCommandLink;
import oracle.adf.view.faces.event.DisclosureEvent;
import oracle.adf.view.faces.event.ReturnEvent;
import oracle.adf.view.faces.event.SelectionEvent;
import oracle.adf.view.faces.model.TreeModel;

import org.apache.shale.view.AbstractViewController;

import org.apache.shale.view.Constants;
import org.apache.shale.view.ExceptionHandler;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.view.utils.JSFUtils;
import gov.idaho.sboe.view.utils.PrintHelper;

public abstract class TreeViewController extends AbstractViewController {
    /**
     * <p>Java log utility class.</p>
     */
    protected static Logger log = Logger.getLogger(TreeViewController.class.getName());
    
    public static final String CLIPBOARD = "CLIPBOARD";
    
    protected abstract ScratchPad.Context scratchPad();

    protected boolean prerenderImpl(ScratchPad.Context scratchPad, CatalogDataAccessHelper dataHelper) 
    throws Exception {
        boolean eventHandled = false;
        CatalogElementBean selectedElement = scratchPad.getSelectedElement();
     
        switch (scratchPad.getMode()) {
            case PRINT:
                getMyPrintHelper().doPrint(getTreeModel(), getCatalogTree(), dataHelper);
                eventHandled = true;
                break;
            case REFRESH_REFERENCE:
                dataHelper.logUsage(Globals.PAGE_REFERENCE,
                    (selectedElement != null)?
                        (selectedElement.getItemType() + " - " + selectedElement.getItemName()):
                        "null"
                );
                scratchPad.getCatalogTreeAdapter().setInstance(
                    dataHelper.retrieveCatalog(selectedElement, isAuthorized()));
                if (selectedElement != null && 
                    scratchPad.getCatalogTreeAdapter().getModel() != null) {
                    findRowKey(scratchPad.getCatalogTreeAdapter().getModel(), 
                               selectedElement);
                    String label = scratchPad.getCrumbLabel();
                    if (label != null) {
                        MenuPathAdapter breadCrumb = scratchPad.getMenuPathAdapter();
                        breadCrumb.addMenuPathCrumb(label, scratchPad.getMode(), scratchPad.getSelectedElement()); 
                    }
                }
                eventHandled = true;
                break;
            case REFRESH_SELECTED:
                if (selectedElement != null && selectedElement.getShowingDetail()) {
                    selectedElement.loadNarrativeHistory(dataHelper);
                    selectedElement.loadReferences(dataHelper);
                }
                eventHandled = true;
                break;
        }
        return eventHandled;
    }

    /**
     * <p>This is a callback from the Shale ViewController.  It is invoked 
     * before the view is rendered.  In this callback, we stage data used
     * by the page.</p>
     */
    public void prerender() {
    //        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            ScratchPad.Context scratchPad = scratchPad();
            scratchPad.setClipboard((List<CatalogElementBean>)JSFUtils.getFromProcess(CLIPBOARD));
            
            CatalogDataAccessHelper dataHelper = 
                (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
            prerenderImpl(scratchPad, dataHelper);
            scratchPad.setMode(ScratchPad.Modes.VIEW);
        // set mode which captures state before getting the stack
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * <p>Catalog tree used to display the search results.</p>
     */
    private CoreTree catalogTree = null;
    
    /**
     * <p>Bound to a af:tree component using a EL binding expression.<br/><br/>
     * <code>binding="#{catalog.catalogTree}"</code></p>
     * @return Catalog tree used to display the search results
     */
    public CoreTree getCatalogTree() {        
        return catalogTree;
    }
    
    /**
     * <p>Bound to a af:tree component using a EL binding expression.<br/><br/>
     * <code>binding="#{catalog.catalogTree}"</code></p>
     * @param catalogTree Catalog tree used to display the search results
     */
    public void setCatalogTree(CoreTree catalogTree) {
       this.catalogTree = catalogTree;
    }

    /**
     * <p>Walks the tree looking for the selected node.  This will cause the
     * selected node to be expanded within the tree.</p>
     * @param model tree model
     * @param target selected element in the tree
     * @return <code>true</code> if a <code>model</code> node was found matching the <code>target</code>
     */
    protected boolean findRowKey(TreeModel model, CatalogElementBean target) {
//        for (int i = 0; i < model.getRowCount(); i++) {
//            if (model.isRowAvailable(i)) {
//                model.setRowIndex(i);
//                CatalogElementBean b = (CatalogElementBean)model.getRowData();
//                if (b.equals(target)) {
//                    return true;
//                }
//                model.enterContainer();
//                boolean found = findRowKey(model, target);
//                model.exitContainer(); 
//                if (found) {
//                    CoreTree tree = getCatalogTree();
//                    Object rowKey = model.getRowKey();
//                   tree.setRowKey(rowKey);
//                   tree.getTreeState().addAll();
//                   return true;
//                }
//            }
//        }
        
        return false;
    }

    /**
     * Attempt to automatically open the root node of the tree
     * @param treeModel
     * @return
     */
    protected boolean expandRootNode(TreeModel treeModel) {
        // the cure is worse than the disease!
        // the root node expands, but then none of the rest will! :(
//        if (treeModel.isRowAvailable(0)) {
//            treeModel.setRowIndex(0);
//            CoreTree tree = getCatalogTree();
//            if (tree!=null) {
//                tree.setRowKey(treeModel.getRowKey());
//                tree.getTreeState().add();
//                return true;
//            }
//        }
        return false;
    }

    /**
     * <p>This item is fired when a item is selected from the tree view.
     * If the item instance is a <code>Glossary.PK</code> we retrieve the
     * full <code>Glossary</code> entity containing the 4K narrative.</p>
     * @param event actionListner callback
     */
    public void select(ActionEvent event) {
//        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            TreeModel model = getTreeModel();
            CatalogElementBean selectedCatalogElement = 
                (CatalogElementBean) model.getRowData();
            selectedCatalogElement.setShowingDetail(!selectedCatalogElement.getShowingDetail());
            scratchPad().clearSelected();

            if (selectedCatalogElement.getShowingDetail()) {
                CatalogDataAccessHelper dataHelper = 
                    (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
                selectedCatalogElement.loadNarrative(dataHelper);
                scratchPad().setSelectedElement(selectedCatalogElement);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * @return a description of the most previous filter criteria used 
     * to populate the view
     */
    public String getFilterCriteria() {
        String filterCriteria = null;
        StringBuffer msg = new StringBuffer("Search Results: ");
        

        try {
    //            ScratchPad scratchPad = (ScratchPad)getBean(Globals.SCRATCH_PAD);
            filterCriteria = scratchPad().getFilterCriteria();
            
        } catch (Exception e) {
            handleException(e);
        }
       
        return filterCriteria;
    }

    /**
     * I don't know if this is "best practices" but...
     * provide a mechanism for 'deep-linking' into a page without
     * sending arguments to the URL for parsing...
     * 
     * @return
     */
    public static void preSelectReference(ScratchPad scratchPad, CatalogDataAccessHelper dataHelper, Glossary.PK pk)
        throws Exception {
        ScratchPad.Context spc = null;
        if (!pk.getGlossType().equals(Globals.COLLECTIONS_TYPE)) return;
        spc = scratchPad.collectionsPage;
        spc.clearReference();
        CatalogElementBean selectedCatalogElement = dataHelper.getCompleteItem(new CatalogElementBean(pk));
        // Bonus - open the narrative!
        selectedCatalogElement.setShowingDetail(true);
        selectedCatalogElement.loadNarrative(dataHelper);
        selectedCatalogElement.loadReferences(dataHelper);
        spc.setSelectedElement(selectedCatalogElement);
    }

    /**
     * <p>Handle the specified exception according to the strategy
     * defined by our current {@link ShaleExceptionHandler}.</p>
     *
     * @param exception Exception to be handled
     */
    protected void handleException(Exception exception) {
//        FacesContext context = FacesContext.getCurrentInstance();
        ExceptionHandler handler = 
            (ExceptionHandler)getBean(Constants.EXCEPTION_HANDLER);
        handler.handleException(exception);
    }

    private PrintHelper myPrintHelper = null;

    public void setMyPrintHelper(PrintHelper myPrintHelper) {
        this.myPrintHelper = myPrintHelper;
    }

    public PrintHelper getMyPrintHelper() {
        if (myPrintHelper == null) {
            myPrintHelper = new PrintHelper();
        }
        return myPrintHelper;
    }


    /**
     * <p>This event is fired when the af:showDetailHeader is toggled on
     * or off.  We want to refresh the selected item.</p>
     * @param event disclosureListener event fired
     */
    public void select(DisclosureEvent event) {
        if (!event.isExpanded()) {
            try {
                CatalogElementBean selectedCatalogElement = 
                    (CatalogElementBean)getTreeModel().getRowData();
                scratchPad().setSelectedElement(selectedCatalogElement);
                selectedCatalogElement.setShowingDetail(false);
            } catch (Exception e) {
                handleException(e);
            }
        }
    }

    /**
     * <p>Returns the backing state of the tree view component.</p>
     * @return tree model that wrappers a graph of objects
     * @throws IntrospectionException error setting up the tree model
     */
    public TreeModel getTreeModel() {
        //        FacesContext facesContext = FacesContext.getCurrentInstance();
        TreeModel model = null;
        try {
            model = scratchPad().getCatalogTreeAdapter().getModel();

        } catch (Exception e) {
            handleException(e);
        }

        return model;
    }

    /**
     * <p>Returns the backing state of the menu path component.</p>
     * @return menu model that wraps a list of menu items
     * @throws IntrospectionException error setting up the tree model
     */
    public TreeModel getMenuModel() {
        //        FacesContext facesContext = FacesContext.getCurrentInstance();
        TreeModel model = null;
        try {
            model = scratchPad().getMenuPathAdapter().getModel();

        } catch (Exception e) {
            handleException(e);
        }

        return model;
    }

    /**
     * @return the catalog item that is current in focus
     */
    public CatalogElementBean getSelectedCatalogElement() {
        return scratchPad().getSelectedElement();
    }

    /**
     * @param selectedCatalogElement the selected catalog element we are showing
     * detail on.
     */
    public void setSelectedCatalogElement(CatalogElementBean selectedCatalogElement) {
        scratchPad().setSelectedElement(selectedCatalogElement);
    }

    /**
     * <p>Clears the select element.</p>
     * 
     * @param event fired when selecting tree expansion 
     */
    public void selection(SelectionEvent event) {
        scratchPad().clearSelected();
    }

    /**
     * @return all the <code>GlossaryType</code>s that will populate a droplist.</p>
     */
    public SelectItem[] getCatalogTypes() {
        //        ScratchPad scratchPad = (ScratchPad)getBean(Globals.SCRATCH_PAD);
        return scratchPad().getCatalogTypes();

    }

    public void clear() {
        log.entering(TreeViewController.class.getName(), "resetSearch");
        try {
            scratchPad().clearAll();
            collapseAllNodes();
        } catch (Exception e) {
            handleException(e);
        }
        log.exiting(TreeViewController.class.getName(), "resetSearch");
    }

    /**
     * Collapse all nodes
     *
     * Expand/Collapse State
     * The ADF tree components use an instance of the oracle.adf.view.faces.model.PathSet
     * class to keep track of which elements are expanded. This instance is stored
     * as the "treeState" attribute on the component. You may use this instance
     * to programmatically control the expand/collapse state of an element in the hierarchy.
     * Any element contained by the PathSet instance is deemed expanded. All other elements
     * are collapsed. This class also supports operations like addAll() and removeAll().
     *
     */
    public void collapseAllNodes() {
    	if (getCatalogTree() != null) {
    		getCatalogTree().getTreeState().getKeySet().clear();
    	}
    }

    /**
     * <p>This is fired when a parent glossary item is selected.</p>
     * @param event actionListener callback
     */
    public void selectReference(ActionEvent event) {
        try {
            String itemName = null;
            String itemType = null;
            List<UIParameter> childs = 
                ((CoreCommandLink)event.getSource()).getChildren();
            for (UIParameter param: childs) {
                if (param.getName().equals("itemName")) {
                    itemName = (String)param.getValue();
                } else if (param.getName().equals("glossType")) {
                    itemType = (String)param.getValue();
                }
            }
            if (itemName != null && itemType != null) {
                selectReference(new Glossary.PK(itemType, itemName));
            } else {
                // throw up in some form
                log.info("Attempting to select reference itemname='" + 
                         itemName + "', itemType='" + itemType + "'");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * <p>This is fired when a parent glossary item is selected.</p>
     * @param pk actionListener callback
     */
    void selectReference(Glossary.PK pk) {

        try {
            CatalogDataAccessHelper dataHelper = (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
            scratchPad().clearReference();
            setSelectedCatalogElement(dataHelper.getCompleteItem(new CatalogElementBean(pk)));
//??            scratchPad.getCatalogTreeAdapter().setInstance(dataHelper.retrieveCatalog());
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * @return the work area that persists in session scope
     */
    public List<CatalogElementBean> getClipboard() {
        //        ScratchPad scratchPad = (ScratchPad)getBean(Globals.SCRATCH_PAD);
         List<CatalogElementBean> result = scratchPad().getClipboard();
         
         if (result.isEmpty())
         {
           List<CatalogElementBean> processClip = ( List<CatalogElementBean>)JSFUtils.getFromProcess(CLIPBOARD);
           
           if (processClip != null)
           {
             result = processClip;
             scratchPad().setClipboard(result);
           }
         }
        
         
        return result;
    }

    /**
     * @return <code>true</code> if the <code>clipboard</code> is empty.
     */
    public boolean getIsClipboardEmpty() {
//        ScratchPad scratchPad = (ScratchPad)getBean(Globals.SCRATCH_PAD);
        return getClipboard().isEmpty();
    }

    /**
     * @param event actionListener event fired when the paste icon is clicked
     * @throws ShowException error pasting from the clipboard into the tree
     */
    public void pasteFromClipboard(ActionEvent event) {
        if (!isAuthorized()) {
            return;
        }

        //        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            CatalogElementBean selectedCatalogElement = 
                (CatalogElementBean)getTreeModel().getRowData();
            CatalogDataAccessHelper dataHelper = (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
            dataHelper.addItemsToParent(selectedCatalogElement, getClipboard());
            scratchPad().restoreState();
        } catch (Exception e) {
            handleException(e);
        }
    }

    
    public void applyFilter(ValueChangeEvent event) {
        if (!isAuthorized()) {
            return;
        }

        try {
            if (event.getNewValue() != null) 
            {
                FilterTypes filter = ((FilterTypes)getBean(Globals.ROLODEX_FILTER));
                filter.setSelectedFilter((String)event.getNewValue());
                
                
            }
        } catch (Exception e) {
            handleException(e);
        }
    }
    

    /**
     * @param event actionListener fired when clicking on the copy link
     */
    public void copyToClipboard(ActionEvent event) {
        if (!isAuthorized()) {
            return;
        }

        try {
            CatalogElementBean selectedCatalogElement = 
                (CatalogElementBean)getTreeModel().getRowData();

            CatalogDataAccessHelper dataHelper = (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);

            if (!getClipboard().contains(selectedCatalogElement)) 
            {
                List<CatalogElementBean> clip = getClipboard();
                clip.add(dataHelper.getCompleteItem(selectedCatalogElement));
                scratchPad().setClipboard(clip);
                JSFUtils.storeOnProcess(CLIPBOARD, clip);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * <p>Removes a <code>Glossary</code> and all associated relationships.</p>
     */
    public void clipboardDelete(ActionEvent event) {
        if (!isAuthorized()) {
            return;
        }

        try {

            CatalogDataAccessHelper dataHelper = 
                (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);

            CatalogElementBean row = 
                (CatalogElementBean)getRequestMap().get("detail");
            if (row != null) {
                dataHelper.deleteItem(row);
                List<CatalogElementBean> clip = getClipboard();
                clip.remove(row);
                JSFUtils.storeOnProcess(CLIPBOARD, clip);
                scratchPad().restoreState();            
            }

        } catch (Exception e) {
            handleException(e);
        }

    }

    public void doPrint(ActionEvent event) {
        scratchPad().setMode(ScratchPad.Modes.PRINT);
    }

    /**
     * <p>Deleting a relationship from the tree.</p>
     * @param event actionListener fired when clicking the delete icon
     */
    public void deleteFromTree(ActionEvent event) {
        if (!isAuthorized()) {
            return;
        }

//        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            CatalogElementBean selectedCatalogElement = 
                (CatalogElementBean)getTreeModel().getRowData();

            CatalogDataAccessHelper dataHelper = 
                (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
            dataHelper.deleteItemFromTree(selectedCatalogElement);
            scratchPad().restoreState();

        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * @param event actionListener invoked when clicking on the new button
     * in the clipboard (editior).
     */
    public void clipboardNewItem(ActionEvent event) {
        if (!isAuthorized()) {
            return;
        }
        CatalogDataAccessHelper dataHelper = (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
        
        List<CatalogElementBean> clip = getClipboard();
        clip.add(dataHelper.getNewItem());
        scratchPad().setClipboard(clip);
    }

    /**
     * @param event actionListener invoked when the clear button is clicked in 
     * the scratchpad (editor)
     */
    public void clipboardClear(ActionEvent event) {
        if (!isAuthorized()) {
            return;
        }
        scratchPad().clearClipboard();
        JSFUtils.removeFromProcess(CLIPBOARD);
    }

    /**
     * @param event invoked with the save button is clicked in the
     * editor.
     */
    public void clipboardSave(ActionEvent event) {
        if (!isAuthorized()) {
            return;
        }

        try {
            CatalogDataAccessHelper dataHelper = (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
            dataHelper.saveItems(getClipboard());
            scratchPad().restoreState();
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * <p>Checks to see if the user has authenticated.  If a <code>User</code>
     * entity bean can not be found in session scope, navigation is directed to
     * the login page.</p>
     * 
     * @return <code>true</code> if the user is authenticated
     */
    protected boolean isAuthorized() {
//        FacesContext context = FacesContext.getCurrentInstance();
        Security security = (Security)getBean(Globals.SECURITY);
        return security.getIsAuthenticated();
    }

    /**
     * <p>
     * This is a method binding event fired from the <strong>Clay</strong>
     * component before building the component tree. The method signature is a
     * "Validator" event signature and the binding attribute is
     * <code>shapeValidator</code>.
     * </p>
     * 
     * @param context
     * facesContext
     * @param component
     * @param displayElementRoot
     */
    public void createNarrative(FacesContext context, UIComponent component, 
                                Object displayElementRoot) {


    }
    
    public String actionKeywordSearch() 
    {
        clear();
        return "menu2.keyword";
    }
    
    public String actionRolodexSearch() 
    {
        clear();
        return "menu2.alpha";
    }
    
    public String actionCollectionSearch() 
    {
        clear();
        return "menu2.collection";
    }
    
    
    
}
