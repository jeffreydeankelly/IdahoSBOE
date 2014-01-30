package gov.idaho.sboe.view.utils;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import javax.faces.el.VariableResolver;

import oracle.adf.view.faces.context.AdfFacesContext;

import org.apache.shale.view.Constants;
import org.apache.shale.view.ViewController;
import org.apache.shale.view.ViewControllerMapper;
import org.apache.shale.view.faces.FacesConstants;

/**
 * General useful static utilities for working with JSF.
 * <br>
 */
public class JSFUtils {

  private static final String NO_RESOURCE_FOUND = "Missing resource: ";

  /**
   * Method for taking a reference to a JSF binding expression and returning
   * the matching object (or creating it).
   * @param expression
   * @return Managed object
   */
  public static Object resolveExpression(String expression) {
    FacesContext ctx = FacesContext.getCurrentInstance(); 
    Application app = ctx.getApplication();
    ValueBinding bind = app.createValueBinding(expression);
    return bind.getValue(ctx);
  }

  /**
   * Convenience method for resolving a reference to a managed bean by name
   * rather than by expression.
   * @param beanName
   * @return Managed object
   */
  public static Object getManagedBeanValue(String beanName) {
    StringBuffer buff = new StringBuffer("#{");
    buff.append(beanName);
    buff.append("}");
    return resolveExpression(buff.toString());
  }

  /**
   * Method for setting a new object into a JSF managed bean.
   * Note: will fail silently if the supplied object does
   * not match the type of the managed bean
   * @param expression
   * @param newValue
   */
  public static void setExpressionValue(String expression, 
                                        Object newValue) {
    FacesContext ctx = FacesContext.getCurrentInstance();                                        
    Application app = ctx.getApplication();
    ValueBinding bind = app.createValueBinding(expression);

    //Check that the input newValue can be cast to the property type
    //expected by the managed bean. 
    //If the managed Bean expects a primitive we rely on Auto-Unboxing
    //I could do a more comprehensive check and conversion from the object 
    //to the equivilent primitive but life is too short
    Class bindClass = bind.getType(ctx);
    if (bindClass.isPrimitive() || bindClass.isInstance(newValue)) {
      bind.setValue(ctx, newValue);
    }
  }

  /**
   * Convenience method for setting the value of a managed bean by name
   * rather than by expression.
   * @param beanName
   * @param newValue
   */
  public static void setManagedBeanValue(String beanName, 
                                         Object newValue) {
    StringBuffer buff = new StringBuffer("#{");
    buff.append(beanName);
    buff.append("}");
    setExpressionValue(buff.toString(), newValue);
  }


  /**
   * Convenience method for setting Session variables.
   * @param key object key
   * @param object value to store
   */
  public static

  void storeOnSession(String key, Object object) {
    FacesContext ctx = FacesContext.getCurrentInstance();
    Map sessionState = ctx.getExternalContext().getSessionMap();
    sessionState.put(key, object);
  }

  /**
   * Convenience method for getting Session variables.
   * @param key object key
   */
  public static Object getFromSession(String key) {
    FacesContext ctx = FacesContext.getCurrentInstance();
    Map sessionState = ctx.getExternalContext().getSessionMap();
    return sessionState.get(key);
  }

    /**
     * Convenience method for setting Process variables.
     * @param key object key
     * @param object value to store
     */
    public static void storeOnProcess(String key, Object object) {
      AdfFacesContext ctx = AdfFacesContext.getCurrentInstance();
      ctx.getProcessScope().put(key, object);
    }

    /**
     * Convenience method for getting Process variables.
     * @param key object key
     */
    public static Object getFromProcess(String key) {
      AdfFacesContext ctx = AdfFacesContext.getCurrentInstance();
      return ctx.getProcessScope().get(key);
    }


    /**
     * Convenience method for getting Process variables.
     * @param key object key
     */
    public static Object removeFromProcess(String key) {
      AdfFacesContext ctx = AdfFacesContext.getCurrentInstance();
      return ctx.getProcessScope().remove(key);
    }

    /**
     * Convenience method for clearing Process scope.
     */
    public static void clearProcess() {
      AdfFacesContext ctx = AdfFacesContext.getCurrentInstance();
      ctx.getProcessScope().clear();
      return;
    }
 


  /**
   * Pulls a String resource from the property bundle that
   * is defined under the application &lt;message-bundle&gt; element in
   * the faces config. Respects Locale
   * @param key
   * @return Resource value or placeholder error String
   */
  public static String getStringFromBundle(String key) {
    ResourceBundle bundle = getBundle();
    return getStringSafely(bundle, key, null);
  }

  public static void addFacesErrorBundleMessage(String key) {
      FacesContext ctx = FacesContext.getCurrentInstance();
      String msg = getStringFromBundle(key);
      FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR,msg,msg);
      ctx.addMessage(getRootViewComponentId(),fm);
  }  
    
  public static void addFacesInfoBundleMessage(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String msg = getStringFromBundle(key);
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO,msg,msg);
        ctx.addMessage(getRootViewComponentId(),fm);
  }  
      
  public static void addFacesWarnBundleMessage(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String msg = getStringFromBundle(key);
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN,msg,msg);
        ctx.addMessage(getRootViewComponentId(),fm);
  }
  
  public static void addFacesErrorMessage(String msg) {
    FacesContext ctx = FacesContext.getCurrentInstance();
    FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR,msg,msg);
    ctx.addMessage(getRootViewComponentId(),fm);
  }  
  
  public static void addFacesInfoMessage(String msg) {
      FacesContext ctx = FacesContext.getCurrentInstance();
      FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO,msg, msg);
      ctx.addMessage(getRootViewComponentId(),fm);
  }  
    
    public static void addFacesWarnMessage(String msg) {
      FacesContext ctx = FacesContext.getCurrentInstance();
      FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN,msg,msg);
      ctx.addMessage(getRootViewComponentId(),fm);
 }  
  
  public static void addFacesErrorMessage(String attrName, 
                                          String msg) {
    // TODO: Need a way to associate attribute specific messages
    //       with the UIComponent's Id! For now, just using the view id.
    //TODO: make this use the internal getMessageFromBundle?
     FacesContext ctx = FacesContext.getCurrentInstance();
     FacesMessage fm =  new FacesMessage(FacesMessage.SEVERITY_ERROR,attrName,msg);
     
     ctx.addMessage(getRootViewComponentId(),fm);
  }
  
  // Informational getters
   public static String getRootViewId() {
     FacesContext ctx = FacesContext.getCurrentInstance();
     return ctx.getViewRoot().getViewId();
   }
   public static String getRootViewComponentId() {
     FacesContext ctx = FacesContext.getCurrentInstance();
     return ctx.getViewRoot().getId();
   }  
  
    
  /*
   * Internal method to pull out the correct local
   * message bundle
   */
  private static ResourceBundle getBundle() {
    FacesContext ctx = FacesContext.getCurrentInstance();
    UIViewRoot uiRoot = ctx.getViewRoot();
    Locale locale = uiRoot.getLocale();
    ClassLoader ldr = Thread.currentThread().getContextClassLoader();
    return ResourceBundle.getBundle(ctx.getApplication().getMessageBundle(), 
                                    locale, ldr);
  }

  /*
       * Internal method to proxy for resource keys that don't exist
       */

  private static String getStringSafely(ResourceBundle bundle, String key, 
                                        String defaultValue) {
    String resource = null;
    try {
      resource = bundle.getString(key);
    } catch (MissingResourceException mrex) {
      if (defaultValue != null) {
        resource = defaultValue;
      } else {
        resource = NO_RESOURCE_FOUND + key;
      }
    }
    return resource;
  }


  public static void returnFromDialog(Object value, Map map)
  {
      AdfFacesContext.getCurrentInstance().returnFromDialog(value, map);
  }
}
