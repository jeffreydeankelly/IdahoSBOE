package gov.idaho.sboe.view.backing;

import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.shale.tiger.managed.Bean;
import org.apache.shale.tiger.managed.Scope;
import org.apache.shale.view.AbstractViewController;

@Bean(name = Globals.SHOW_EXCEPTION, scope = Scope.REQUEST)
public class ShowException extends AbstractViewController {

    /**
     * <p>Mnemonics used by the servlet 2.3 API.</p>
     */
    static final String[] ERROR_PARAMS = 
    { "javax.servlet.error.status_code", "javax.servlet.error.exception_type", 
      "javax.servlet.error.message", "javax.servlet.error.exception", 
      "javax.servlet.error.request_uri", "javax.servlet.error.servlet_name", 
      "javax.faces.webapp.FacesServlet", "javax.servlet.error.exception" };


    /**
     * <p>Look in request scope for the <code>ERROR_PARAMS</code> and push them
     * into the <code>UIViewRoot</code>'s state.  We do this so we don't loose
     * value on the "showexception.jspx" page between refreshes.  The JSP page
     * binds to the view root.
     * For example:
     * <pre>
     *   &lt;af:outputText value="#{view.attributes['javax.servlet.error.status_code']}"/&gt;
     * </pre>
     * 
     * </p>
     */
    public void prerender() {
        // capture the exception parameters
        UIViewRoot root = this.getFacesContext().getViewRoot();
        Map requestMap = this.getRequestMap();
        for (String param: ERROR_PARAMS) {
            Object value = requestMap.get(param);
            if (value != null) {
                root.getAttributes().put(param, value);    
            }
        }


    }

}
