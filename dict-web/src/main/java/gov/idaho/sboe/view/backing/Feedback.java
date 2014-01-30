package gov.idaho.sboe.view.backing;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.shale.tiger.managed.Bean;
import org.apache.shale.tiger.managed.Scope;
import org.apache.shale.view.AbstractRequestBean;
import org.apache.shale.view.Constants;
import org.apache.shale.view.ExceptionHandler;


@Bean(name = Globals.FEEDBACK, scope = Scope.REQUEST)
    public class Feedback extends AbstractRequestBean {
    private static final String DEFAULT_RATING = "4";
   
    private String rating = DEFAULT_RATING;

    /**
     * @param rating id of the authenticating user
     */
    public void setRating(String rating) {
        this.rating = rating;
    }

    /**
     * @return id of the authenticating user
     */
    public String getRating() {
        return rating;
    }

    private String comment;
    /**
     * @param comment of the authenticating user
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return comment of the authenticating user
     */
    public String getComment() {
        return comment;
    }
    /**
     * @param viewId pushes the target view to session scope.
     */
    public void setReturnViewId(String viewId) {
        getSessionMap().put(Globals.RETURN_VIEWID, viewId);
    }

    /**
     * @return <code>RETURN_VIEWID</code> from session scope or the <code>DEFAULT_RETURN_VIEWID</code>
     */
    public String getReturnViewId() {
        String returnViewId = (String)getSessionMap().get(Globals.RETURN_VIEWID);
        if (returnViewId == null) {
            return Globals.DEFAULT_RETURN_VIEWID;
        } else {
            return returnViewId;
        }

    }

    /**
     * @param event actionListener event that captures the <code>returnViewId</code>
     * when the login menu link is clicked.
     */
    public void captureReturnView(ActionEvent event) {
        FacesContext context = getFacesContext();
        if (context.getViewRoot() != null) {
            setReturnViewId(context.getViewRoot().getViewId());
        }
    }

    /**
     * <p>Invoked when the send button is clicked.</p>
     * @param event actionListener event
     */
    public void send(ActionEvent event) {
        try {
            FacesContext context = getFacesContext();
            CatalogDataAccessHelper dataHelper = 
                (CatalogDataAccessHelper)getBean(Globals.DATA_ACCESS_HELPER);
            dataHelper.persistFeedback(comment, rating);
//            JSFUtils.addFacesInfoBundleMessage("panel_feedback_msg");
            context.addMessage("Thanks for the feedback!", new FacesMessage( "Thanks for the feedback!"));
            gotoReturnView(context);
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * <p>Programmatic navigation to the <code>returnToViewId</code></p>
     * @param context faces context
     */
    private void gotoReturnView(FacesContext context) {
        String returnToViewId = getReturnViewId();

        UIViewRoot root = 
            getApplication().getViewHandler().createView(context, returnToViewId);
        context.setViewRoot(root);
        context.renderResponse();

        // remove the cached value
        setReturnViewId(null);
    }

    /**
     * <p>Handle the specified exception according to the strategy
     * defined by our current {@link ShaleExceptionHandler}.</p>
     *
     * @param exception Exception to be handled
     */
    private void handleException(Exception exception) {
//        FacesContext context = FacesContext.getCurrentInstance();
        ExceptionHandler handler = 
            (ExceptionHandler)getBean(Constants.EXCEPTION_HANDLER);
        handler.handleException(exception);
    }

}
