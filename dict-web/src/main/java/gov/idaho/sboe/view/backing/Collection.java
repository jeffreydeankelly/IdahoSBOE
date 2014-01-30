package gov.idaho.sboe.view.backing;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.faces.event.ActionEvent;

import oracle.adf.view.faces.event.ReturnEvent;

import oracle.adf.view.faces.model.TreeModel;

import org.apache.shale.tiger.managed.Bean;
import org.apache.shale.tiger.managed.Scope;
import org.apache.shale.tiger.view.View;

import gov.idaho.sboe.jpa.beans.Glossary;


/**
 * <p>The backing bean for the <code>collection.jspx</code>.  This managed bean
 * is also a shale view controller.  We take advantage of the <code>prerender()</code>
 * callback to stage data for the page.  The {@link ScratchPad} is used as the work
 * area between page actions.  It is kept in session scope.  This managed bean
 * is stateless and kept in request scope.  
 * <br/>
 * <br/>
 * The data access layer is bridged via the {@link CatalogDataAccessHelper} managed
 * bean that is also stateless and kept in application scope.  This calls wappers
 * various JPA entity beans so that they can be used by JSF components.  The
 * {@link CatalogElementBean} is the entity bean wrapper.
 * <br/>
 * <br/>
 * This <a href="http://shale.apache.org/shale-view/index.html">view controller</a> 
 * handles security using the {@link Security} bean. This bean 
 * verifes that a user is login in order to perform certain operations.
 * <br/>
 * <br/>
 * Fatal exceptions are handled using the {@link ShaleExceptionHandler}.  This is
 * a custom implementation of the <a href="http://shale.apache.org/shale-view/apidocs/org/apache/shale/view/impl/DefaultExceptionHandler.html">default</a>
 * Shale view controller exception handling
 * logic.
 * </p>
 */
@Bean(name = "collection", scope = Scope.REQUEST)
@View
public class Collection extends TreeViewController {

    private boolean emptySearch = false;

    /**
     * <p>This is a callback from the Shale ViewController.  It is invoked 
     * before the view is rendered.  In this callback, we stage data used
     * by the page.</p>
     */
    @Override
     protected boolean prerenderImpl(ScratchPad.Context scratchPad, CatalogDataAccessHelper dataHelper) 
     throws Exception {
         boolean eventHandled = false;
//        FacesContext facesContext = FacesContext.getCurrentInstance();

            MenuPathAdapter breadCrumb = scratchPad.getMenuPathAdapter();
            CatalogElementBean selectedElement = scratchPad.getSelectedElement();

            switch (scratchPad.getMode()) {
                case REFRESH_GLOBAL:
                    dataHelper.logUsage(Globals.PAGE_CLEAR, "");
                    scratchPad.setCatalogTypes(dataHelper.retrieveCatalogTypes());

                    /* if one has more than the root, which only occurs when 
                     * one navigates to home or login page and then navigates
                     * back, so perform a clean 
                     */
                    if (breadCrumb.getBreadCrumbTrail().size() > 1)
                    {
                       breadCrumb.getBreadCrumbTrail().clear();
                    }
                    
                    // the default is to retrieve all collections
                    scratchPad.getCatalogTreeAdapter().setInstance(dataHelper.retrieveCollections());
                    // put the code here to auto-open the "Collections" root node
                    expandRootNode(scratchPad.getCatalogTreeAdapter().getModel());
                    eventHandled = true;
                    break;
                case REFRESH_SEARCH:
                    dataHelper.logUsage(Globals.PAGE_SEARCH, scratchPad.getSarg());
                    List searchList = dataHelper.retrieveCatalog(scratchPad.getSarg());
                    scratchPad.getCatalogTreeAdapter().setInstance(searchList);
                    if (searchList.isEmpty())
                        emptySearch = true;
                    else
                        emptySearch = false;
                        
                    if (selectedElement != null && 
                        scratchPad.getCatalogTreeAdapter().getModel() != null) {
                        findRowKey(scratchPad.getCatalogTreeAdapter().getModel(), 
                                   selectedElement);
                                   
                        String label = scratchPad.getCrumbLabel();
                        if (label != null)
                            breadCrumb.addMenuPathCrumb(label, scratchPad.getMode(), scratchPad.getSarg()); 
                    }
                    if (!expandRootNode(scratchPad.getCatalogTreeAdapter().getModel()))
                        collapseAllNodes();
                    eventHandled = true;
                    break;
            }
            if (!eventHandled) eventHandled = super.prerenderImpl(scratchPad, dataHelper);
            return eventHandled;
    }
    
    /**
     * <p>Bound to a af:tree component using a EL binding expression.<br/><br/>
     * <code>binding="#{catalog.catalogTree}"</code></p>
     * @param catalogTree Catalog tree used to display the search results
     */
//    public void setCatalogTree(CoreTree catalogTree) {
//        TreeModel treeModel = scratchPad().getCatalogTreeAdapter().getModel();
//        if (treeModel.isRowAvailable(0)) {
//            treeModel.setRowIndex(0);
//            if (catalogTree!=null) {
//                catalogTree.setRowKey(treeModel.getRowKey());
//                catalogTree.getTreeState().add();
//            }
//        }
//       super.setCatalogTree(catalogTree);
//    }

    /**
     * @return returns the ad-hoc search criteria
     */
    public String getSarg() {
//        ScratchPad scratchPad = (ScratchPad)getBean(Globals.SCRATCH_PAD);
        return scratchPad().getSarg();
    }

    /**
     * @param sarg seas the ad-hoc search criteria
     */
    public void setSarg(String sarg) {
        scratchPad().setSarg(sarg);
    }

    public void menuPathNavigate(ActionEvent event) {
        try {
            ScratchPad.Context scratchPad = ((ScratchPad)getBean(Globals.SCRATCH_PAD)).collectionsPage;
            MenuPathAdapter breadCrumb = scratchPad.getMenuPathAdapter();
            MenuItem menuItem = (MenuItem)breadCrumb.getModel().getRowData();
            if (menuItem.getMode() == null) {
                resetSearch(null);
            } else {
                Stack<MenuItem> caretaker = breadCrumb.getBreadCrumbTrail();
                while (!caretaker.peek().equals(menuItem)) {
                    caretaker.pop();
                }
                // pop this one off also, since it will get re-pushed
                MenuItem memento = caretaker.pop();
                ScratchPad.Modes prevMode = memento.getMode();
    
                if (prevMode.equals(ScratchPad.Modes.REFRESH_SEARCH)) {
                    String prevSarg = memento.getSarg();
                   if (prevSarg != null && prevSarg.length() > 0) {
                        scratchPad.setSarg(prevSarg);
                   }
                    search(null);
                } else if (prevMode.equals(ScratchPad.Modes.REFRESH_REFERENCE)) {
                    CatalogElementBean prevSelectedElement  = memento.getSelectedElement(); 
                    selectReference(((Glossary)prevSelectedElement.getInstance()).getPk());
                } else {
                     resetSearch(null);
                }
    
    //            setBean(Globals.SCRATCH_PAD, scratchPad);
    //
    //            clearTabSelection();
    //            selectedTab.setSelected(true);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * <p>This callback is invoked with the user clicks on the search button.</p>
     * @param event actionListener fired on the search commandButton
     * @throws gov.idaho.sboe.view.backing.ShowException error queuing the search 
     */
    public void search(ActionEvent event) {
        try {
            scratchPad().clearSearch();
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * @return a description of the most previous filter criteria used 
     * to populate the view
     */
    public String getPageName() {
        return scratchPad().getFilterCriteria();
    }
    
    public boolean getHasPreviousSelections() {
        return scratchPad().getMenuPathAdapter().getBreadCrumbTrail().size()>1;
    }
    /**
     * @return a description of the most previous filter criteria used 
     * to populate the view
     */
    public String getFilterCriteria() {
        String filterCriteria = null;      

        try {
            StringBuffer msg = new StringBuffer("Search Results: ");
            msg.append(scratchPad().getFilterCriteria());
            filterCriteria = msg.toString();            
        } catch (Exception e) {
            handleException(e);
        }
       
        return filterCriteria;
    }

    /**
     * <p>This callback is invoked with the user clicks on the clear button.</p>
     * @param event actionListener fired on the clear commandButton
     * @throws gov.idaho.sboe.view.backing.ShowException error queuing the search 
     */
    public void resetSearch(ActionEvent event) {
        log.entering(Collection.class.getName(), "resetSearch");
        long now = new Date().getTime();
        try {
            scratchPad().clearAll();
            collapseAllNodes();
            long then = new Date().getTime();
            log.finest("After scratchPad.clearAll in "+(then - now));
            now = then;
            then = new Date().getTime();
            log.finest("After clearTabSelection in"+(then-now));
       } catch (Exception e) {
            handleException(e);
        }
        log.exiting(Collection.class.getName(), "resetSearch");
    }

    @Override
    protected ScratchPad.Context scratchPad() {
        return ((ScratchPad)getBean(Globals.SCRATCH_PAD)).collectionsPage;
    }
    
    
    public void clipboardSave(ActionEvent event) 
    {
       super.clipboardSave(event);
       scratchPad().setMode(ScratchPad.Modes.REFRESH_GLOBAL);
    }
    
    public void clipboardDelete(ActionEvent event) {
       super.clipboardDelete(event);
       scratchPad().setMode(ScratchPad.Modes.REFRESH_GLOBAL);
    }
   
    public boolean getIsEmptySearch()
   {
       return emptySearch;
   }
}
