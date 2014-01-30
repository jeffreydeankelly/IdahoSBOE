package gov.idaho.sboe.view.backing;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;

import oracle.adf.view.faces.model.MenuModel;

import org.apache.shale.tiger.managed.Bean;
import org.apache.shale.tiger.managed.Scope;
import org.apache.shale.tiger.view.View;

import gov.idaho.sboe.jpa.beans.Glossary;


/**
 * <p>The backing bean for the <code>rolodex.jspx</code>.  This managed bean
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
@Bean(name = "rolodex", scope = Scope.REQUEST)
@View
public class Rolodex extends TreeViewController {

    /**
     * <p>This is a callback from the Shale ViewController.  It is invoked 
     * before the view is rendered.  In this callback, we stage data used
     * by the page.</p>
     */
    @Override
    protected boolean prerenderImpl(ScratchPad.Context spc, CatalogDataAccessHelper dataHelper) 
    throws Exception {
        ScratchPad.TabbedContext scratchPad = (ScratchPad.TabbedContext)spc;
        boolean eventHandled = false;
        String filterSelction = ((FilterTypes)getBean(Globals.ROLODEX_FILTER)).getSelectedFilter();
        final List<TabBean> tabs = dataHelper.retrieveTabs();
        switch (scratchPad.getMode()) {
            case REFRESH_GLOBAL:
                dataHelper.logUsage(Globals.PAGE_CLEAR, "");
              
                if (scratchPad.getSelectedTab()== null) {
                    Character tabCh = getBreadCrumbTab(scratchPad.getMenuPathAdapter().getRootLabel());
                    
                    if (tabCh == null)  
                        tabCh = new Character('A');
                
                    for (TabBean b: tabs) {
                        if (b.getBeginFilter()== tabCh.charValue()) {
                            b.setSelected(true);
                            scratchPad.setSelectedTab(b);
                            break;
                        }
                    }
                    changeTab(scratchPad.getSelectedTab());
                    scratchPad.getCatalogTreeAdapter().setInstance(
                        dataHelper.retrieveCatalog(scratchPad.getSelectedTab(),
                            filterSelction,
                            isAuthorized()));
                }
                scratchPad.setCatalogTypes(dataHelper.retrieveCatalogTypes());
                scratchPad.getTabsMenuAdapter().setInstance(tabs);
         
                scratchPad.getMenuPathAdapter().setRootLabel("Selected Tab [" + scratchPad.getSelectedTab().getText() + "]");
                /* if one has more than the root, which only occurs when 
                 * one navigates to home or login page and then navigates
                 * back, so perform a clean 
                 */
                MenuPathAdapter breadCrumb = scratchPad.getMenuPathAdapter();
                if (breadCrumb.getBreadCrumbTrail().size() > 1) {
                   breadCrumb.getBreadCrumbTrail().clear();
                }
                eventHandled = true;
                break;
            case REFRESH_TAB:
                dataHelper.logUsage(Globals.PAGE_TAB, scratchPad.getSelectedTab().getText());               
                scratchPad.getCatalogTreeAdapter().setInstance(
                dataHelper.retrieveCatalog(scratchPad.getSelectedTab(), 
                    filterSelction,
                    isAuthorized()));
                // put the code here to auto-open the "Data Elements" root node
                expandRootNode(scratchPad.getCatalogTreeAdapter().getModel());
                scratchPad.getMenuPathAdapter().clearBreadCrumbTrail();
                scratchPad.getMenuPathAdapter().setRootLabel("Selected Tab [" + scratchPad.getSelectedTab().getText() + "]");
                eventHandled = true;
                break;
        
           case REFRESH_REFERENCE:
               break;
        }
        if (!eventHandled) eventHandled = super.prerenderImpl(scratchPad, dataHelper);
        return eventHandled;
    }


    /**
     * <p>Returns the data model for the Tab control.</p>
     * @return data model behind the tab controls.
     */
    public MenuModel getTabsModel() {
        MenuModel model = null;
        try {
            model = scratchPad().getTabsMenuAdapter().getModel();
        } catch (Exception e) {
            handleException(e);
        }
        return model;
    }

    public void clear() {
        super.clear();
        clearTabSelection();
    }
    
    /**
     * <p>This event is fired when the user changes tabs.  The selected
     * tab is captured so that the tree will be filtered on the tab's
     * begining and ending filter criteria.</p>
     * @param event fired when changing tabs
     */
    public void changeTab(ActionEvent event) 
    {
        try {
            changeTab((TabBean)getTabsModel().getRowData());
        } catch (Exception e) {
            handleException(e);
        }
    }

    void changeTab(TabBean selectedTab) 
    {
        ScratchPad.TabbedContext scratchPad = ((ScratchPad)getBean(Globals.SCRATCH_PAD)).dataElementsPage;
        scratchPad.clearTab();
        scratchPad.setSelectedTab(selectedTab);
//        setBean(Globals.SCRATCH_PAD, scratchPad);

        clearTabSelection();
        selectedTab.setSelected(true);
    }

    /**
     * I don't know if this is "best practices" but...
     * provide a mechanism for 'deep-linking' into a page without
     * sending arguments to the URL for parsing...
     * 
     * @return
     * 
     * Note:  jeffk removed the @Override annotation as it introduced an error because this method is static.
     */
//  @Override  
    public static void preSelectReference(ScratchPad scratchPad, CatalogDataAccessHelper dataHelper, Glossary.PK pk)
        throws Exception {
        if (pk.getGlossType().equals(Globals.COLLECTIONS_TYPE)) TreeViewController.preSelectReference(scratchPad, dataHelper, pk);
        if (!pk.getGlossType().equals(Globals.DATA_ELEMENT_TYPE)) return;
        ScratchPad.TabbedContext spc = scratchPad.dataElementsPage;
        // by not going thru REFRESH_GLOBAL, we lose the tabs if we dont do this:
        spc.getTabsMenuAdapter().setInstance(dataHelper.retrieveTabs());
        spc.clearReference();
        CatalogElementBean selectedCatalogElement = dataHelper.getCompleteItem(new CatalogElementBean(pk));
        // Bonus - open the narrative!
        selectedCatalogElement.setShowingDetail(true);
        selectedCatalogElement.loadNarrative(dataHelper);
        selectedCatalogElement.loadReferences(dataHelper);
        spc.setSelectedElement(selectedCatalogElement);
    }

    public void menuPathNavigate(ActionEvent event) {
        try {
            MenuPathAdapter breadCrumb = scratchPad().getMenuPathAdapter();
            MenuItem menuItem = (MenuItem)breadCrumb.getModel().getRowData();
            if (menuItem.getMode() == null) {
                clear();
            } else {
                Stack<MenuItem> caretaker = breadCrumb.getBreadCrumbTrail();
                while (!caretaker.peek().equals(menuItem)) {
                    caretaker.pop();
                }
                // pop this one off also, since it will get re-pushed
                MenuItem memento = caretaker.pop();
                ScratchPad.Modes prevMode = memento.getMode();
    
                if (prevMode.equals(ScratchPad.Modes.REFRESH_REFERENCE)) {
                    CatalogElementBean prevSelectedElement  = memento.getSelectedElement(); 
                    selectReference(((Glossary)prevSelectedElement.getInstance()).getPk());
                } else if (prevMode.equals(ScratchPad.Modes.REFRESH_TAB)) {
                    TabBean prevSelectedTab = memento.getSelectedTab();
                    scratchPad().clearTab();
                    if (prevSelectedTab != null) {
                        scratchPad().setSelectedTab(prevSelectedTab);
                    }
                 } else {
                    clear();
                 }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * <p>Clears the selected tab.  This is the tab that has focus.</p>
     */
    private void clearTabSelection() {
        try {
            List list = (List)scratchPad().getTabsMenuAdapter().getInstance();
            if (list != null) {
                Iterator li = list.iterator();
                while (li.hasNext()) {
                    TabBean b = (TabBean)li.next();
                    b.setSelected(false);
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    void selectReference(Glossary.PK pk) {
        super.selectReference(pk);
        clearTabSelection();
    }


    /**
     * <p>This call will syncronize the clicked item with the known selected item.
     * This is necessary for partial page updates to the catalog tree.  Partial
     * updates will only expand what is seleced.  Re-rendering of previous
     * selections is not perfomed.  This means that multiple selections will
     * appear but only one as focus at a time.</p>
     */
     @Override
     protected ScratchPad.TabbedContext scratchPad() {
        return ((ScratchPad)getBean(Globals.SCRATCH_PAD)).dataElementsPage;
    }
    
    /* This is the filter value change
     * It is fired off when one changes the filter type
     * The filter option on the rolodex is aonly available under admin
     */
    public void applyFilter(ValueChangeEvent event) {
        if (!isAuthorized()) {
            return;
        }

        try {
            if (event.getNewValue() != null) 
            {
                FilterTypes filter = ((FilterTypes)getBean(Globals.ROLODEX_FILTER));
                filter.setSelectedFilter((String)event.getNewValue());
                clear();
            }
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
    
    
    private Character getBreadCrumbTab(String value) 
    {
       Character result = null;
       String prefix = "Selected Tab [";
       int idx = value.indexOf(prefix);
       if (idx != -1)
         result = new Character(value.charAt(prefix.length()));
         
       return result;
    }
    
    public void clipboardSave(ActionEvent event) 
    {
       super.clipboardSave(event);
       scratchPad().setMode(ScratchPad.Modes.REFRESH_TAB);
    }
    
   public void clipboardDelete(ActionEvent event) {
       super.clipboardDelete(event);
       scratchPad().setMode(ScratchPad.Modes.REFRESH_TAB);
   }
}
