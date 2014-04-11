package gov.idaho.sboe.view.backing;

import java.sql.Date;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.faces.event.ActionEvent;

import oracle.adf.view.faces.component.core.input.CoreSelectInputDate;

import org.apache.shale.tiger.managed.Bean;
import org.apache.shale.tiger.managed.Scope;
import org.apache.shale.tiger.view.View;
import org.apache.shale.view.AbstractViewController;
import org.apache.shale.view.Constants;
import org.apache.shale.view.ExceptionHandler;

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
@Bean(name = "statistics", scope = Scope.REQUEST)
@View
public class Statistics extends AbstractViewController {

    /**
     * <p>Java log utility class.</p>
     */
    private static Logger log = Logger.getLogger(Statistics.class.getName());
   
    protected Date startDate;
    protected Date endDate;
    
    protected Calendar cal = Calendar.getInstance();

    public CoreSelectInputDate selectStartDate;
    public CoreSelectInputDate selectEndDate;
    
    public static String StartDate = "StartDate";
    public static String EndDate = "EndDate";
    public static String DateChangeFlag = "DateChangeFlag";
    
    public static String FeedbackUsage = "FeedbackUsage";
    public static String SessionLength = "SessionLength";
    public static String SessionPerDay = "SessionPerDay";

    /**
     * <p>This is a callback from the Shale ViewController.  It is invoked 
     * before the view is rendered.  In this callback, we stage data used
     * by the page.</p>
     */
    public void prerender() {
        try {
            JSFUtils.storeOnProcess(DateChangeFlag, true);
            JSFUtils.removeFromProcess(StartDate);
            JSFUtils.removeFromProcess(EndDate);
            endDate = new java.sql.Date(cal.getTime().getTime());
            cal.add(Calendar.MONTH,  -1);
            startDate = new java.sql.Date(cal.getTime().getTime());
        } catch (Exception e) {
            handleException(e);
        }
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

    public void setSelectStartDate(CoreSelectInputDate selectStartDate){
        this.selectStartDate=selectStartDate;
    }
    
    public CoreSelectInputDate getSelectStartDate(){
        return selectStartDate;
    }
    
    public void setSelectEndDate(CoreSelectInputDate selectEndDate){
        this.selectEndDate=selectEndDate;
    }
    
    public CoreSelectInputDate getSelectEndDate(){
        return selectEndDate;
    }
    
    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
        
        JSFUtils.storeOnProcess(DateChangeFlag, true);
        
        // Clear the statistic lists
        cleanStatisticList();
        
        JSFUtils.storeOnProcess(StartDate, startDate);
    }

    public Date getStartDate()
    {
        if (startDate == null)
           startDate = (Date)JSFUtils.getFromProcess(StartDate);
        return startDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
        
        JSFUtils.storeOnProcess(DateChangeFlag, true);
        
        // Clear the statistic lists
        cleanStatisticList();
        
        JSFUtils.storeOnProcess(EndDate, endDate);
    }

    public Date getEndDate()
    {
        if (endDate == null)
           endDate = (Date)JSFUtils.getFromProcess(EndDate);
        return endDate;
    }
    
    protected void cleanStatisticList()
    {
        JSFUtils.removeFromProcess(FeedbackUsage);
        JSFUtils.removeFromProcess(SessionLength);
        JSFUtils.removeFromProcess(SessionPerDay);
    }
    
}
