package gov.idaho.sboe.view.backing;

import java.lang.Exception;

import java.util.Map;
import java.util.logging.Logger;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;

import org.apache.shale.view.Constants;
import org.apache.shale.view.ExceptionHandler;
import org.apache.shale.view.ViewController;
import org.apache.shale.view.ViewControllerMapper;
import org.apache.shale.view.faces.FacesConstants;


/**
 * <p>This is a custom implemenation of the shale ViewController 
 * <code>ExceptionHandler</code>.  This class is invoked when a runtime
 * exception is raised in one of the view controller callback method.</p>
 * <ul>
 * <li>init() - Called immediately after the view that this backing bean is associated with is created.</li>
 * <li>preprocess() - Called on a post back after a page has been submitted.</li>
 * <li>prerender() - Called immediately before the HTML is rendered.</li>
 * <li>destroy() - Called at the end of the request/response.</li>
 * </ul>
 * <p>The custom implementation navigates to an exception page.  The default
 * Shale implemenation dispatches to an error page too but it uses a phase
 * listener.  It appears that the ADF phase listener is getting in the way.
 * This version short-circuits the normal lifecycle but trys to reuse many
 * if the conventions in the original implemenation.
 * </p>
 * 
 * Steps:<br/>
 * <ol>
 *    <li>Register the handler in the faces config.<br/>
 *     <pre>
 *&lt;managed-bean&gt;
 *  &lt;description&gt;
 *    Custom implementation of org.apache.shale.view.ExceptionHandler
 *    used to process application-triggered exceptions.
 *  &lt;/description&gt;
 *  &lt;managed-bean-name&gt;org$apache$shale$view$EXCEPTION_HANDLER&lt;/managed-bean-name&gt;
 *  &lt;managed-bean-class&gt;gov.idaho.sboe.view.backing.ShaleExceptionHandler&lt;/managed-bean-class&gt;
 *  &lt;managed-bean-scope&gt;application&lt;/managed-bean-scope&gt;
 *&lt;/managed-bean&gt;
 *     
 *     </pre>
 *   </li> 
 *   <li>Register your error page in the web deployment descriptor (web.xml).<br/>
 *      <pre>
 *  &lt;context-param&gt;
 *      &lt;param-name&gt;org.apache.shale.view.EXCEPTION_DISPATCH_PATH&lt;/param-name&gt;
 *      &lt;param-value&gt;/showexception.jspx&lt;/param-value&gt;
 *   &lt;/context-param&gt;
 *      </pre>
 *   </li>
 *   <li>Create your jspx page and view controller for the page.</li>
 *   <li>Wrap you action and action listener method callbacks in a 
 *   try catch that invokes this class.  This is necessary because the JSF 1.1 RI
 *   version doesn't create the <code>UIViewRoot</code> using the componentType.
 *   The Shale view controller has a UIViewRoot implemenation that wrappers several
 *   calls that end up invoking callback events in the managed beans.  Since the 1.1
 *   version doesn't provide that hook, the additional logic is needed.
 *   <a href="http://svn.apache.org/viewvc/shale/framework/trunk/shale-view/src/main/java/org/apache/shale/view/faces/ShaleViewRoot.java?view=markup">ShaleViewRoot.java</a>
 *   <br/><br/>
 *   <pre>
 *       public void clipboardSave(ActionEvent event) {
 *       try {
 *       .....
 *
 *       } catch (Exception e) {
 *           handleException(e);
 *       }
 *   }
 *   private void handleException(Exception exception) {
 *       FacesContext context = FacesContext.getCurrentInstance();     
 *       ExceptionHandler handler = (ExceptionHandler) getBean(Constants.EXCEPTION_HANDLER);
 *       handler.handleException(exception);
 *   }
 *   </pre>
 *   </li>
 *   </ol>
 *   
 */
public class ShaleExceptionHandler implements ExceptionHandler {
        
    /**
     * <p>Java log utility class.</p>
     */
    private static Logger log = Logger.getLogger(ShaleExceptionHandler.class.getName());

    /**
     * <p>Return the view controller for the exception page.
     * This will cause the <code>init()</code> method to be
     * fired.</p>
     * @param context faces context
     * @param viewId veiw id of the target exception page
     * @return view controller for the exception page
     */
    private ViewController getViewController(FacesContext context, String viewId) {
        // Map our view identifier to a corresponding managed bean name
     
         ValueBinding vb = context.getApplication().createValueBinding("#{" + Constants.VIEW_MAPPER + "}");
         ViewControllerMapper vcm = (ViewControllerMapper) vb.getValue(context);
         String viewName = vcm.mapViewId(viewId);

        // Retrieve an existing instance, or one created and configured by
        // the managed bean facility
        ViewController vc = null;
        VariableResolver vr =context.getApplication().getVariableResolver();

        vc = (ViewController) vr.resolveVariable(context, viewName);

        return vc;
    }

    /**
     * <p>In our custom implemenation, we capture the exception and stage
     * the information in request scope using the same names that JSP/Servlet
     * would use.</p>
     * 
     * @param exception captured exception
     */
    public void handleException(Exception exception) {
    
        log.severe(exception.toString());
    
        FacesContext context = FacesContext.getCurrentInstance();
        
        String exceptionViewId =
          context.getExternalContext().getInitParameter(Constants.EXCEPTION_DISPATCH_PATH);
        
        Map requestMap = context.getExternalContext().getRequestMap();
        context.renderResponse();
        
        // fire the init method
        ViewController vc = getViewController(context, exceptionViewId);
        UIViewRoot root =  context.getApplication().getViewHandler().createView(context, exceptionViewId);
                                                                 
        requestMap.put("javax.servlet.error.status_code", new Integer(200));
        requestMap.put("javax.servlet.error.exception_type", 
                       exception.getClass());
        requestMap.put("javax.servlet.error.message", exception.getMessage());
        requestMap.put("javax.servlet.error.exception", exception);
        StringBuffer sb = new StringBuffer("");
        if (context.getExternalContext().getRequestServletPath() != null) {
            sb.append(context.getExternalContext().getRequestServletPath());
        }
        if (context.getExternalContext().getRequestPathInfo() != null) {
            sb.append(context.getExternalContext().getRequestPathInfo());
        }
        requestMap.put("javax.servlet.error.request_uri", sb.toString());
        requestMap.put("javax.servlet.error.servlet_name", 
                       "javax.faces.webapp.FacesServlet");
        requestMap.put("javax.servlet.error.exception", exception);               
                       
        // force a destroy before dispatching to the error page
        context.getExternalContext().getSessionMap().remove(FacesConstants.VIEWS_INITIALIZED);
                                                                 
        context.setViewRoot(root);
        // simulate the lifecycle since we are short circuiting
        vc.prerender();

    }
}
