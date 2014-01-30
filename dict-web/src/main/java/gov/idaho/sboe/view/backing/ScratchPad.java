package gov.idaho.sboe.view.backing;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.shale.tiger.managed.Bean;
import org.apache.shale.tiger.managed.Scope;

import gov.idaho.sboe.jpa.beans.SearchResult;
import gov.idaho.sboe.view.utils.JSFUtils;


/**
 * <p>This object acts as the cache for the current activities 
 * on the Catalog page.</p>
 */
@Bean(name = Globals.SCRATCH_PAD, scope = Scope.SESSION)
public class ScratchPad implements Serializable {

    /**
     * <p>An indicator of the type of refresh needed based on the
     * selected navigation control.</p>
     */
    public static enum Modes {
        REFRESH_GLOBAL,
        REFRESH_SEARCH,
        REFRESH_REFERENCE,
        REFRESH_TAB,
        REFRESH_SELECTED,
        VIEW,
        PRINT
    }

    public TabbedContext dataElementsPage = new TabbedContext("/rolodex.jspx", "menu2.alpha","All Data Elements");
    public Context collectionsPage = new Context("/collection.jspx", "menu2.collection","All Collections");
    public KWContext searchPage = new KWContext();

//    public ScratchPad() {
//        Throwable t = new Throwable();
//        t.printStackTrace(); 
//        System.out.println("Creating scratchpad");
//    }

    /**
     * <p>Keeps track of the previous criteria used by the page
     * navigaion (search, tabs, current selection, references).</p>
     */
//    private Stack<MenuItem> caretaker = new Stack<MenuItem>();
//    
//    Stack<MenuItem> getCaretaker() {
//        return caretaker;
//    }

    /**
     * Capture things only specific to the KW search
     */
    public class KWContext {
        String sarg = null;

        /**
         * <p>The index of the first table row to be displayed
         */
        private int firstTableRow = 0;
        
        public int getFirstTableRow() {
            return firstTableRow;
        }
        public void setFirstTableRow(int ftr) {
            firstTableRow = ftr;
        }

        public void clearKWSearch() {
            kwSearchResults = null;
            sarg = null;
        }

        List<SearchResult> kwSearchResults = null;
        /**
         * @param results from most recent query
         */
        public void setKeywordSearchResults(List<SearchResult> results) {
            this.kwSearchResults = results;
        }

        /**
         * @return populates an list of results 
         */
        public List<SearchResult> getKeywordSearchResults() {
            if (kwSearchResults == null) {
                kwSearchResults = new ArrayList<SearchResult>();
            }
            return kwSearchResults;
        }
        /**
         * <p>Invoked when a new ad-hoc narrative search is performed.</p>
         */
        public void clearSearch() {
            firstTableRow = 0;
        }

    }
    public class TabbedContext extends Context {
        public TabbedContext(String viewId, String menu, String label) {
            super(viewId, menu, label);
        }

        /**
         * <p>Tab that is in focus.</p>
         */
        private TabBean selectedTab = null;
        
        /**
         * <p>Model used by the tabs.</p>
         */
        private TabMenuAdapter tabsMenuAdapter = null;    

        /**
         * @param selectedTab tab that has been selected via an actionListener callback.
         */
        public void setSelectedTab(TabBean selectedTab) {
            this.selectedTab = selectedTab;
        }

        /**
         * @return the selected tab
         */
        public TabBean getSelectedTab() {
            return selectedTab;
        }

        /**
         * @param tabsMenuAdapter model class behind the af:menuTabs component
         */
        public void setTabsMenuAdapter(TabMenuAdapter tabsMenuAdapter) {
            this.tabsMenuAdapter = tabsMenuAdapter;
        }

        /**
         * @return model class behind the af:menuTabs component
         */
        public TabMenuAdapter getTabsMenuAdapter() {
            if (tabsMenuAdapter == null) {
                tabsMenuAdapter = new TabMenuAdapter();
                tabsMenuAdapter.setChildProperty("viewId");
            }

            return tabsMenuAdapter;
        }

        public String getLabel(MenuItem memento) {
            Modes prevMode = memento.getMode();

            if (prevMode != null && prevMode.equals(Modes.REFRESH_TAB)) {
                TabBean prevSelectedTab = getSelectedTab();
                if (prevSelectedTab != null) {
                    StringBuffer msg = new StringBuffer();
                    msg.append("Selected Tab [").append(prevSelectedTab.getText()).append("]");
                    return msg.toString();
                }
                return "";
            }  else {
                return super.getLabel(memento);
            }
        }

        /**
         * <p>Save the curren state used by the navigation.</p>
         */
        protected void captureState() {
            super.captureState();
            currentState.setSelectedTab(selectedTab);
        }

        /**
         * <p>Clears the current cached data behind the <code>/catalog.jspx</code> view.</p>
         */
        public void clearAll() {
            super.clearAll();
            tabsMenuAdapter = null;
        }

        /**
         * <p>Invoked when a parent link to the <code>selectedElement</code> is
         * selected.</p>
         */
        public void clearReference() {
            super.clearReference();
            selectedTab = null;
        }

        /**
         * <p>Invoked when a tab changes.  Clears the currently selected element, 
         * the search argument, the previously selected tab, and the data behind
         * the af:tree component.</p>
         */
        public void clearTab() {
            clearSelected();
            sarg = null;
            selectedTab = null;
            catalogTreeAdapter = null;
            mode = Modes.REFRESH_TAB;
        }

        /**
         * <p>Invoked when a new ad-hoc narrative search is performed.</p>
         */
        public void clearSearch() {
            super.clearSearch();
            selectedTab = null;
        }        
    }

    public class Context {
    
    /**
     * <p>Search argument.</p>
     */
    protected String sarg = null;
    
    /**
     * <p>Model of where we've been.</p>
     */
    protected MenuPathAdapter menuPathAdapter = null;

    /**
     * <p>Module used to populate the af:tree component.</p>
     */
    protected TreeModelAdapter catalogTreeAdapter = null;
    
    /**
     * <p>All <code>GlossaryTypes</code>'s in a <code>SelectItem</code>
     * used to populate the af:selectOneChoice component.</p>
     */
    protected SelectItem[] catalogTypes;
    
    /**
     * <p>A list of <code>Glossary</code> entity beans that are currently 
     * being exited.</p>
     */
    protected List<CatalogElementBean> clipboard = null;

    /**
     * <p>Defines the type of refresh that needs to be performed by the 
     * {@link Collection}'s
     * <code>prerender()</code> Shale ViewController callback.</p>
     */
    protected Modes mode = Modes.REFRESH_GLOBAL;

    protected MenuItem currentState = null;

    protected String rootLabel;

    public Context(String viewId, String menu, String label) {
        menuPathAdapter = 
            new MenuPathAdapter(viewId, menu, label);

        rootLabel = label;
    }

    /**
     * <p>Save the curren state used by the navigation.</p>
     */
    protected void captureState() {
        currentState = menuPathAdapter.createMenuItem("", mode, selectedElement, sarg);
    }
    
    /**
     * <p>Restores the most previous state of the navigational criteria.
     * Used after an item is deleted.
     * </p>
     */
    public void restoreState() {
//        MenuItem memento = caretaker.pop();
//        if (memento != null) {
//            mode = memento.getMode();
//            sarg = memento.getSarg();
//            selectedTab = memento.getSelectedTab();
//            selectedElement  = memento.getSelectedElement(); 
//        }
    }

    /**
     * <p>Clears the current cached data behind the <code>/catalog.jspx</code> view.</p>
     */
    public void clearAll() {
        clearSearch();
        catalogTypes = null;
        mode = Modes.REFRESH_GLOBAL;
        currentState = null;
        getMenuPathAdapter().clearBreadCrumbTrail();
    }

    /**
     * <p>Invoked when a parent link to the <code>selectedElement</code> is
     * selected.</p>
     */
    public void clearReference() {
        clearSelected();
        sarg = null;
        catalogTreeAdapter = null;
        mode = Modes.REFRESH_REFERENCE;
    }

    /**
     * <p>Invoked when a new ad-hoc narrative search is performed.</p>
     */
    public void clearSearch() {
        clearSelected();
        catalogTreeAdapter = null;
        mode = Modes.REFRESH_SEARCH;
    }

    /**
     * <p>Invoked when the <code>selectedElement</code> looses focus.</p>
     */
    public void clearSelected() {
        selectedElement = null;
        mode = Modes.REFRESH_SELECTED;
    }

    /**
     * <p>Clears the content of the editor's workarea.</p>
     */
    public void clearClipboard() {
        clipboard = null;
    }

    /**
     * <p>Sets the current mode and also captures the current state
     * of the search criteria if the previous mode is <code>Modes.REFRESH_SEARCH</code>,
     * or <code>Modes.REFRESH_TAB</code> or <code>Modes.REFRESH_REFERENCE</code>.
     * @param m current search mode
     */
    public void setMode(ScratchPad.Modes m) {
        if (mode != null && m.equals(Modes.VIEW) 
            && (mode.equals(Modes.REFRESH_SEARCH) 
            || mode.equals(Modes.REFRESH_TAB) 
            || mode.equals(Modes.REFRESH_REFERENCE))) {
            
            captureState();
        }
    
        this.mode = m;
    }

    /**
     * @return the current search mode
     */
    public ScratchPad.Modes getMode() {
        return mode;
    }

    /**
     * @param sarg narrative search argument
     */
    public void setSarg(String sarg) {
        this.selectedElement = null;
        this.sarg = sarg;
    }

    /**
     * @return ad-hoc narrative search criteria
     */
    public String getSarg() {
        if (sarg == null) {
            return "";
        }
        
        return sarg;
    }

    /**
     * @param catalogTreeAdapter model behind the af:tree component
     * @deprecated No uses found
     */
    public void setCatalogTreeAdapter(TreeModelAdapter catalogTreeAdapter) {
        this.catalogTreeAdapter = catalogTreeAdapter;
    }

    /**
     * @return model behind the af:tree component.
     */
    public TreeModelAdapter getCatalogTreeAdapter() {
        if (catalogTreeAdapter == null) {
            catalogTreeAdapter = new TreeModelAdapter();
            catalogTreeAdapter.setChildProperty("children");
        }
        return catalogTreeAdapter;
    }

    /**
     * @param catalogTypes list used to populate the af:selectOneChoice component
     */
    public void setCatalogTypes(SelectItem[] catalogTypes) {
        this.catalogTypes = catalogTypes;
    }

    /**
     * @return list used to populate the af:selectOneChoice component
     */
    public SelectItem[] getCatalogTypes() {
        if (catalogTypes == null) {
            catalogTypes = new SelectItem[0];
        }
        return catalogTypes;
    }

    /**
     * @param clipboard items that are currently being worked on
     */
    public void setClipboard(List<CatalogElementBean> clipboard) 
    {
        this.clipboard = clipboard;
        JSFUtils.storeOnProcess(TreeViewController.CLIPBOARD, clipboard);
    }

    /**
     * @return populates an editable af:table component 
     */
    public List<CatalogElementBean> getClipboard() {
        if (clipboard == null) {
            clipboard = new ArrayList<CatalogElementBean>();
        }
        return clipboard;
    }

    /**
     * @return a description of the most previous filter criteria used 
     * to populate the view
     */
    public String getFilterCriteria() {
        return (currentState == null)?
                rootLabel:
                getLabel(currentState);
    }

    public String getLabel(MenuItem memento) {
        StringBuffer msg = new StringBuffer();
        Modes prevMode = memento.getMode();
        String prevSarg = memento.getSarg();
        CatalogElementBean prevSelectedElement  = memento.getSelectedElement(); 

        if (prevMode == null) {
            msg.append(rootLabel);
        } else
        if (prevMode.equals(Modes.REFRESH_SEARCH)) {
            if (prevSarg != null && prevSarg.length() > 0) {
                msg.append("Search Argument [").append(prevSarg).append("]");
            }
        } else if (prevMode.equals(Modes.REFRESH_REFERENCE)) {
            if (prevSelectedElement != null) {
                msg.append("Selected Reference [").append(prevSelectedElement.getItemType())
                       .append(" - ").append(prevSelectedElement.getItemName()).append("]");
            }
        }  else {
            msg.append(rootLabel);
        }
                     
        return msg.toString();
    }

      public String getCrumbLabel() {
           String msg = null;
           
           CatalogElementBean prevSelectedElement  = getSelectedElement(); 

           if (getMode() != null) 
           {
              if ((getMode().equals(Modes.REFRESH_SEARCH)) && 
                   getSarg() != null && getSarg().length() > 0) 
              {
                    msg = "Search[" + getSarg() + "]";
              }
              else if (getMode().equals(Modes.REFRESH_REFERENCE) &&
                       prevSelectedElement != null) 
              {
                  msg = "Reference[" + prevSelectedElement.getItemType()
                        + " - " + prevSelectedElement.getItemName() + "]";
              } 
           }
                   
            return msg;
       }
       
       public MenuPathAdapter getMenuPathAdapter() 
       {
           return this.menuPathAdapter;
       }

        /**
         * <p>Item currently selected in the tree.</p>
         */
        private CatalogElementBean selectedElement = null;

        /**
         * @param selectedElement item in the main tree that has been selected
         */
        public void setSelectedElement(CatalogElementBean selectedElement) {
            this.selectedElement = selectedElement;
        }

        /**
         * @return selected item in the tree
         */
        public CatalogElementBean getSelectedElement() {
            return selectedElement;
        }
        
        public String getRootLabel() {
            return rootLabel;
        }
        
        public void setRootLabel(String label) {
            rootLabel = label;
        }
    }
}
