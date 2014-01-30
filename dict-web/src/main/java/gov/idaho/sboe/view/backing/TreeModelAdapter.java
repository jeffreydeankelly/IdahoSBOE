package gov.idaho.sboe.view.backing;

import java.beans.IntrospectionException;

import java.io.Serializable;

import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.adf.view.faces.model.ChildPropertyTreeModel;
import oracle.adf.view.faces.model.TreeModel;

/**
 * <p>This model works with several of the ADF components.  It works
 * with the af:tree, af:menuTabs and af:menuList for starters.</p>
 */
public class TreeModelAdapter implements Serializable {
    
    private static Logger log = Logger.getLogger(TreeModelAdapter.class.getName());
    
    public TreeModelAdapter() {
    }

    private String _propertyName = null;

    private Object _instance = null;

    private transient TreeModel _model = null;

    public TreeModel getModel() {
        if (_model == null) {
            _model = new ChildPropertyTreeModel(getInstance(), getChildProperty());
        }
        return _model;
    }

    public String getChildProperty() {
        return _propertyName;
    }

    /**
     * Sets the property to use to get at child lists
     * 
     * @param propertyName
     */
    public void setChildProperty(String propertyName) {
        _propertyName = propertyName;
        _model = null;
    }

    public Object getInstance() {
        return _instance;
    }

    /**
     * Sets the root list for this tree.
     * 
     * @param instance
     *            must be something that can be converted into a List
     */
    public void setInstance(Object instance) {
        _instance = instance;
        _model = null;
    }

    /**
     * Sets the root list for this tree. This is needed for passing a List when
     * using the managed bean list creation facility, which requires the
     * parameter type is List.
     * 
     * @param instance
     *            the list of root nodes
     */
    public void setListInstance(List<Object> instance) {
        setInstance(instance);
    }

    /**
     * This should only be called if setListInstance was called.
     * 
     * This method shouldn't be needed according to faces spec 1.1 rev 1, see
     * 5.3.1.3 However without this we get the following error in websphere:
     * java.beans.IntrospectionException: No method "getListInstance" with 0
     * arg(s) of matching types in websphere
     */
    @SuppressWarnings("unchecked")
    public List<Object> getListInstance() {
        return (List<Object>)getInstance();
    }

}
