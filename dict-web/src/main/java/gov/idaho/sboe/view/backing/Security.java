package gov.idaho.sboe.view.backing;

import java.io.IOException;

import java.security.Principal;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import javax.faces.event.ActionEvent;

import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpSession;

import org.apache.shale.tiger.managed.Bean;
import org.apache.shale.tiger.managed.Scope;
import org.apache.shale.view.AbstractRequestBean;

import gov.idaho.sboe.services.CatalogFacade;
import gov.idaho.sboe.utils.JPAResourceBean;


/**
 * <p>This managed bean keeps track of the authenticated users. 
 * It is also the backing bean of the logon page.</p>
 */
@Bean(name = Globals.SECURITY, scope = Scope.SESSION)
public class Security extends AbstractRequestBean {

    private boolean isLoggingIn = false;   

    /**
     * <p>The authenticating users id.</p>
     */
    private String userId = null;

    /**
     * <p>The authenticating users password.</p>
     */
//    private String password = null;
    
//    private List<String> roles = new ArrayList<String>();

    public Security()
    {
//        //need to work out how to get to the request
//        FacesContext ctx = FacesContext.getCurrentInstance();
//        ExternalContext ectx = ctx.getExternalContext(); 
//        
//       
//        //Ask the container who the user logged in as 
//        userId = ectx.getRemoteUser();
//        
    }


    /**
     * @param viewId pushes the target view to session scope.
     */
//    public void setReturnViewId(String viewId) {
//        getSessionMap().put(Globals.RETURN_VIEWID, viewId);
//    }

    /**
     * @return <code>RETURN_VIEWID</code> from session scope or the <code>DEFAULT_RETURN_VIEWID</code>
     */
//    public String getReturnViewId() {
//        String returnViewId = (String)getSessionMap().get(Globals.RETURN_VIEWID);
//        if (returnViewId == null) {
//            return Globals.DEFAULT_RETURN_VIEWID;
//        } else {
//            return returnViewId;
//        }
//
//    }

    /**
     * <p>Programmatic navigation to the <code>returnToViewId</code></p>
     * @param context faces context
     */
//    private void gotoReturnView(FacesContext context) {
//        String returnToViewId = getReturnViewId();
//
//        UIViewRoot root = 
//            getApplication().getViewHandler().createView(context, returnToViewId);
//        context.setViewRoot(root);
//        context.renderResponse();
//
//        // remove the cached value
//        setReturnViewId(null);
//    }
    
    /**
     * <p>Programmatic navigation to the login page.</p>
     * @param context faces context
     */
//    public void gotoLoginView(FacesContext context) {
//        if (context.getViewRoot() != null) {
//            setReturnViewId(context.getViewRoot().getViewId());
//        }
//
//
//        UIViewRoot root = 
//            getApplication().getViewHandler().createView(context, Globals.LOGIN_VIEWID);
//        context.setViewRoot(root);
//        context.renderResponse();
//    }


    /**
     * @param event actionListener invoked when the logout menu item is clicked
     */
    public void logout(ActionEvent event) throws IOException {
        ExternalContext ectx =      
                  FacesContext.getCurrentInstance().getExternalContext();
//        HttpServletResponse response = 
//                  (HttpServletResponse)ectx.getResponse();
        HttpSession session = (HttpSession)ectx.getSession(false);
        session.invalidate();
        isLoggingIn = false;
        userId = null;
    }

    /**
     * @param event actionListener invoked when the login menu item is clicked
     */
    public void login(ActionEvent event) {
        isLoggingIn = true;
    }


    /**
     * @param event actionListener event that captures the <code>returnViewId</code>
     * when the login menu link is clicked.
     */
//    public void captureReturnView(ActionEvent event) {
//        FacesContext context = getFacesContext();
//        if (context.getViewRoot() != null) {
//            setReturnViewId(context.getViewRoot().getViewId());
//        }
//    }


   /**
     * <p>Checks to see if the user is authenticated</p>
     * @return <code>true</code> if the user is logged on
     */
    public boolean getIsAuthenticated() {
        if (!isLoggingIn) return false;
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = ctx.getExternalContext(); 

        //Ask the container who the user logged in as 
        userId = ectx.getRemoteUser();
        if (userId == null) {
            isLoggingIn = false;
            return false;
        }
        
        return true;
    }
    
    /**
      * <p>Checks to see if the user is run in demo mode</p>
      * @return <code>true</code> if the user is logged on
      */
     public boolean getIsDemo() {
         if (!isLoggingIn) return false;
         FacesContext ctx = FacesContext.getCurrentInstance();
         ExternalContext ectx = ctx.getExternalContext(); 

         //Ask the container who the user logged in as 
         userId = ectx.getRemoteUser();
         if (userId == null) {
             isLoggingIn = false;
             return false;
         }
         if (!ctx.getExternalContext().isUserInRole("ccat_demo")) {
             return false;
         }

         return true;
     }
     
    /**
      * <p>Checks to see if the user is run in admin mode</p>
      * @return <code>true</code> if the user is logged on
      */
     public boolean getIsAdmin() {
             if (!isLoggingIn) return false;
             FacesContext ctx = FacesContext.getCurrentInstance();
             ExternalContext ectx = ctx.getExternalContext(); 

             //Ask the container who the user logged in as 
             userId = ectx.getRemoteUser();
             if (userId == null) {
                 isLoggingIn = false;
                 return false;
             }
             if (!ctx.getExternalContext().isUserInRole("ccat_admin")) {
                 return false;
             }
             return true;
     }

    /**
     * @param userId id of the authenticating user
     */
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }

    /**
     * @return id of the authenticating user
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param password of the authenticating user
     */
//    public void setPassword(String password) {
//        this.password = password;
//    }

    /**
     * @return password of the authenticating user
     */
//    public String getPassword() {
//        return password;
//    }

    public void flushCache(ActionEvent actionEvent) {
        CatalogDataAccessHelper dataHelper = 
            (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
        CatalogFacade catalogFacade = (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        ((JPAResourceBean)getBean(Globals.JPA_RESOURCE)).
            getCacheManager(catalogFacade).reset(catalogFacade);
        dataHelper.registerErrorMessage("Data cache has been re-loaded.");
    }


    /**
     * <p>Handle the specified exception according to the strategy
     * defined by our current {@link ShaleExceptionHandler}.</p>
     *
     * @param exception Exception to be handled
     */
    //    private void handleException(Exception exception) {
    //        FacesContext context = FacesContext.getCurrentInstance();
    //        ExceptionHandler handler = 
    //            (ExceptionHandler)getBean(Constants.EXCEPTION_HANDLER);
    //        handler.handleException(exception);
    //    }

    public void setIsLoggingIn(boolean isLoggingIn) {
        this.isLoggingIn = isLoggingIn;
    }

    public boolean getIsLoggingIn() {
        return isLoggingIn;
    }
}
