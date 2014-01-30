package gov.idaho.sboe.view.backing;

import java.util.List;
import java.util.logging.Logger;

import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import oracle.adf.view.faces.component.core.data.CoreTable;
import oracle.adf.view.faces.component.core.nav.CoreCommandLink;

import org.apache.shale.tiger.managed.Bean;
import org.apache.shale.tiger.managed.Scope;
import org.apache.shale.tiger.view.View;
import org.apache.shale.view.AbstractViewController;
import org.apache.shale.view.Constants;
import org.apache.shale.view.ExceptionHandler;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.SearchResult;


/**
 * <p>The backing bean for the <code>kwsearch.jspx</code>.  This managed bean
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
@Bean(name = "kwsearch", scope = Scope.REQUEST)
@View
public class KeywordSearch extends AbstractViewController {

    /**
     * <p>Java log utility class.</p>
     */
    private static Logger log = Logger.getLogger(KeywordSearch.class.getName());

    /**
     * <p>This is a callback from the Shale ViewController.  It is invoked 
     * before the view is rendered.  In this callback, we stage data used
     * by the page.</p>
     */
    public void prerender() {
        try {
            ScratchPad.KWContext scratchPad = ((ScratchPad)getBean(Globals.SCRATCH_PAD)).searchPage;
            String sarg = scratchPad.sarg;
            if (sarg != null && sarg.length()>0) {
                CatalogDataAccessHelper dataHelper = 
                    (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
                dataHelper.logUsage(Globals.PAGE_SEARCH, sarg);
                scratchPad.setKeywordSearchResults(dataHelper.keywordSearch(sarg));
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * @return returns the ad-hoc search criteria
     */
    public String getSarg() {
        return ((ScratchPad)getBean(Globals.SCRATCH_PAD)).searchPage.sarg;
    }

    public List<SearchResult> getKeywordSearchResults() {
        return ((ScratchPad)getBean(Globals.SCRATCH_PAD)).searchPage.getKeywordSearchResults();
    }

    public boolean getHasResults() {
        return getKeywordSearchResults().size()>0;
    }
    /**
     * @param sarg seas the ad-hoc search criteria
     */
    public void setSarg(String sarg) {
        ((ScratchPad)getBean(Globals.SCRATCH_PAD)).searchPage.sarg = sarg;
    }

    /**
     * <p>This callback is invoked with the user clicks on the search button.</p>
     * @param event actionListener fired on the search commandButton
     * @throws gov.idaho.sboe.view.backing.ShowException error queuing the search 
     */
    public void search(ActionEvent event) {
        try {
            ScratchPad.KWContext scratchPad = ((ScratchPad)getBean(Globals.SCRATCH_PAD)).searchPage;
            scratchPad.clearSearch();
            FacesContext facesContext = FacesContext.getCurrentInstance();
            // Force the table to redisplay from the beginning
            CoreTable table = (CoreTable)facesContext.getViewRoot().findComponent("resultTable");
            table.setFirst(scratchPad.getFirstTableRow());
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * <p>This callback is invoked with the user clicks on the clear button.</p>
     * @param event actionListener fired on the clear commandButton
     * @throws gov.idaho.sboe.view.backing.ShowException error queuing the search 
     */
    public void resetSearch(ActionEvent event) {
        log.entering(KeywordSearch.class.getName(), "resetSearch");
        try {
           ((ScratchPad)getBean(Globals.SCRATCH_PAD)).searchPage.clearKWSearch();
       } catch (Exception e) {
            handleException(e);
        }
        log.exiting(KeywordSearch.class.getName(), "resetSearch");
    }

    /**
     * <p>Handle the specified exception according to the strategy
     * defined by our current {@link ShaleExceptionHandler}.</p>
     *
     * @param exception Exception to be handled
     */
    private void handleException(Exception exception) {
        ((ExceptionHandler)getBean(Constants.EXCEPTION_HANDLER)).handleException(exception);
    }

    /**
     * @return a description of the most previous filter criteria used 
     * to populate the view
     */
    public String getFilterCriteria() {
        try {
        String sarg = ((ScratchPad)getBean(Globals.SCRATCH_PAD)).searchPage.sarg;
        return (sarg==null || sarg.length()==0)?
                "":"Search results for: " + sarg;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    /**
     * <p>This is fired when a parent glossary item is selected.</p>
     * @param event actionListener callback
     */
    public void selectReference(ActionEvent event) {
        try {
            String itemName = null;
            String itemType = null;
            CoreCommandLink ccl = (CoreCommandLink)event.getSource();
            List<UIParameter> childs = ccl.getChildren();
            for (UIParameter param: childs) {
                if (param.getName().equals("itemName")) {
                    itemName = (String)param.getValue();
                } else if (param.getName().equals("glossType")) {
                    itemType = (String)param.getValue();
                }
            }
            if (itemName != null && itemType != null) {
                ScratchPad scratchPad = (ScratchPad)getBean(Globals.SCRATCH_PAD);
                CatalogDataAccessHelper dataHelper = 
                    (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
                Glossary.PK pk = new Glossary.PK(itemType, itemName);
                // being a static method this is a little tricky.
                // the Rolodex method looks at the pk type and calls
                // TreeViewController.preSelectReference for Collection types
                Rolodex.preSelectReference(scratchPad, dataHelper, pk);
                FacesContext context = FacesContext.getCurrentInstance();
                String returnToViewId = null;
                if (pk.getGlossType().equals(Globals.COLLECTIONS_TYPE)) returnToViewId = Globals.COLLECTIONS_VIEWID;
                else if (pk.getGlossType().equals(Globals.DATA_ELEMENT_TYPE)) returnToViewId = Globals.DATAELEMENTS_VIEWID;

                UIViewRoot root = 
                    context.getApplication().getViewHandler().createView(context, 
                    returnToViewId);
                context.setViewRoot(root);
                context.renderResponse();

                // remove the cached value
//                setReturnViewId(null);
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
     * @return returns the ad-hoc search criteria
     */
    public int getFirstTableRow() {
        return ((ScratchPad)getBean(Globals.SCRATCH_PAD)).searchPage.getFirstTableRow();
    }
    public void setFirstTableRow(int ftr) {
        ((ScratchPad)getBean(Globals.SCRATCH_PAD)).searchPage.setFirstTableRow(ftr);
    }
}
