package gov.idaho.sboe.view.backing;

import java.util.List;
import java.util.logging.Logger;

import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import oracle.adf.view.faces.component.core.data.CoreTable;
import oracle.adf.view.faces.component.core.nav.CoreCommandLink;

import oracle.adf.view.faces.event.RangeChangeEvent;

import org.apache.shale.tiger.managed.Bean;
import org.apache.shale.tiger.managed.Scope;
import org.apache.shale.tiger.view.View;
import org.apache.shale.view.AbstractViewController;
import org.apache.shale.view.Constants;
import org.apache.shale.view.ExceptionHandler;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.SearchResult;
import gov.idaho.sboe.jpa.beans.StatisticFeedbackUsage;
import gov.idaho.sboe.jpa.beans.StatisticSessionLength;
import gov.idaho.sboe.jpa.beans.StatisticSessionPerDay;
import gov.idaho.sboe.services.CatalogFacade;
import gov.idaho.sboe.view.utils.JSFUtils;



/**
 * <p>The backing bean for the <code>statistic.jspx</code>.  This managed bean
 * is also a shale view controller.  We take advantage of the <code>prerender()</code>
 * callback to stage data for the page.  
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
@Bean(name = "feedbackusage", scope = Scope.REQUEST)
@View
public class FeedbackUsage extends Statistics {

    /**
     * <p>Java log utility class.</p>
     */
    private static Logger log = Logger.getLogger(FeedbackUsage.class.getName());
    private List<StatisticFeedbackUsage> feedbackUsageList = null;


    /**
     * <p>This is a callback from the Shale ViewController.  It is invoked 
     * before the view is rendered.  In this callback, we stage data used
     * by the page.</p>
     */
    public void prerender() {
        try {
            calcFeedbackUsage();
        } catch (Exception e) {
            handleException(e);
        }
    }

    
    /**
     * <p>This callback is invoked with the user clicks on the Feedback Usage button.</p>
     * @param event actionListener fired on the commandButton
     * @throws gov.idaho.sboe.view.backing.ShowException error queuing the search 
     */
    public void calcFeedbackUsage() {
        log.entering(Statistics.class.getName(), "calcFeedbackUsage");
        try {
           feedbackUsageList = loadFeedbackUsageList();
           JSFUtils.storeOnProcess(Statistics.FeedbackUsage, feedbackUsageList);
           JSFUtils.removeFromProcess(Statistics.SessionLength);
           JSFUtils.removeFromProcess(Statistics.SessionPerDay);
       } catch (Exception e) {
            handleException(e);
        }
        log.exiting(Statistics.class.getName(), "calcFeedbackUsage");
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


    private void setFeedbackUsageList(List<StatisticFeedbackUsage> feedbackUsageList)
    {
        this.feedbackUsageList = feedbackUsageList;
    }

    private List<StatisticFeedbackUsage> loadFeedbackUsageList()
    {
        log.entering(Statistics.class.getName(), "getFeedbackUsageList");
        
        Boolean datChange = (Boolean)JSFUtils.getFromProcess(DateChangeFlag);
        if ((getFeedbackUsageList() == null) || (datChange))
        {
            try {
               JSFUtils.storeOnProcess(DateChangeFlag, false);
               CatalogDataAccessHelper dataHelper = (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
               setFeedbackUsageList(dataHelper.retrieveFeedbackUsage(getStartDate(), getEndDate()));
            } catch (Exception e) {
                handleException(e);
            }
        }
        log.exiting(Statistics.class.getName(), "getFeedbackUsageList");
        return feedbackUsageList;
    }

    public List<StatisticFeedbackUsage> getFeedbackUsageList()
    {
        if (feedbackUsageList == null)
            feedbackUsageList = (List<StatisticFeedbackUsage>)JSFUtils.getFromProcess(Statistics.FeedbackUsage);
            
        return feedbackUsageList;
    }

    
    
}
