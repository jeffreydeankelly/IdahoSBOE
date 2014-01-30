package gov.idaho.sboe.view.backing;

import java.beans.IntrospectionException;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import oracle.adf.view.faces.model.ChildPropertyTreeModel;
import oracle.adf.view.faces.model.MenuModel;
import oracle.adf.view.faces.model.TreeModel;
import oracle.adf.view.faces.model.ViewIdPropertyMenuModel;


public class MenuPathAdapter implements Serializable
 {
//    private           String    _propertyName = null;
    private           Object    _instance = null;
    private transient MenuModel _model = null;
//    private           List      _aliasList = null;
    private           Stack<MenuItem> _breadCrumbTrail = new Stack<MenuItem>();
    private final static Logger log = Logger.getLogger(MenuPathAdapter.class.getName());
    private final String defaultViewId;
    private final String defaultOutcome;
    private String rootLabel;

    public MenuPathAdapter(String viewId, String menuOutcome, String label) {
        defaultViewId = viewId;
        defaultOutcome = menuOutcome;
        rootLabel = label;
    }

    public void clearBreadCrumbTrail() {
        _breadCrumbTrail.clear();
        _model = null;
    }

    public MenuModel getModel() 
    {
          if (_model == null)
          {
             // Add the root the menu items  
             addMenuPathCrumb(rootLabel);
          }
          return _model;
        }

//        public String getViewIdProperty()
//        {
//          return _propertyName;
//        }
        /**
         * Sets the property to use to get at view id
         * @param propertyName
         */
//        public void setViewIdProperty(String propertyName)
//        {
//          _propertyName = propertyName;
//          _model = null;
//        }
     
        public Object getInstance()
        {
          return _instance;
        }
        /**
         * Sets the treeModel
         * @param instance must be something that can be converted into a TreeModel
         */
        public void setInstance(Object instance)
        {
          _instance = instance;
          _model = null;
        }

//        public List getAliasList()
//        {
//          return _aliasList;
//        }
//        public void setAliasList(List aliasList)
//        {
//          _aliasList = aliasList;
//        }  

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
    
    public void addMenuPathCrumb(String name)
    {
        addMenuPathCrumb(name, null, null, null);
    }  
    
    public void addMenuPathCrumb(String name, ScratchPad.Modes mode)
    {
        addMenuPathCrumb(name, mode, null, null);
    }
    
    public void addMenuPathCrumb(String name, ScratchPad.Modes mode, CatalogElementBean selectedElement)
    {
        addMenuPathCrumb(name, mode, selectedElement, null);
    }
    
    public void addMenuPathCrumb(String name, ScratchPad.Modes mode, String arg)
    {
        addMenuPathCrumb(name, mode,  null, arg);
    }
    
    public MenuItem createMenuItem(String name, ScratchPad.Modes mode, 
        CatalogElementBean selectedElement, String arg) {
        MenuItem newItem = new MenuItem();
        newItem.setLabel(name);
        newItem.setViewId(defaultViewId);
        newItem.setOutcome(defaultOutcome);    
        newItem.setSarg(arg);
        newItem.setMode(mode);
        newItem.setSelectedElement(selectedElement);
        // What does this do? nothing, it seems.  10/2 mchance
        //            List itemChildren = new ArrayList();
        //            itemChildren.add(newItem);
        return newItem;        
    }
    private void addMenuPathCrumb(String name, ScratchPad.Modes mode, 
        CatalogElementBean selectedElement, String arg)
    {
            MenuItem newItem = createMenuItem(name, mode, selectedElement, arg);
            getBreadCrumbTrail().push(newItem);
            _model = rebuildBrumbPath();     
    }

    public void setBreadCrumbTrail(Stack<MenuItem> breadCrumbTrail)
    {
        this._breadCrumbTrail = breadCrumbTrail;
    }

    public Stack<MenuItem> getBreadCrumbTrail()
    {
        return _breadCrumbTrail;
    }
    
    protected MenuModel rebuildBrumbPath() 
    {
        final FacesContext context = FacesContext.getCurrentInstance();
        String vid = context.getViewRoot().getViewId();
        try
        {
            Stack<MenuItem> tempStack = (Stack<MenuItem> )getBreadCrumbTrail().clone();
            MenuItem item = null;
            MenuItem parent = null;
            List<MenuItem> newMenuList = new ArrayList<MenuItem>();
            while (tempStack.size() > 0)
            {
                item = tempStack.pop();
                if (tempStack.size() > 0)
                { 
                    parent = tempStack.peek();
                    List newChildList = new ArrayList();
                    newChildList.add(item);
                    parent.setChildren(newChildList);
                }
            }
           
            if (item != null)
                newMenuList.add(item);

            TreeModel treeModel = new ChildPropertyTreeModel(newMenuList, "children");
            ViewIdPropertyMenuModel model = new ViewIdPropertyMenuModel(treeModel, "viewId");

//            if(_aliasList != null && !_aliasList.isEmpty())    
//            {
//              int size = _aliasList.size();
//              if (size % 2 == 1)
//                size = size - 1;
//                
//              for ( int i = 0; i < size; i=i+2)
//              {
//                model.addViewId(_aliasList.get(i).toString(),
//                               _aliasList.get(i+1).toString());
//              }
//            }
            
            _model = model;
        }
        catch (IntrospectionException ie)
        {
            log.log(Level.ALL, "Failed to build bread crumb", ie);
        }
        
        return _model;
    }
    
    public void setRootLabel(String label) {
        rootLabel = label;
    }
    
    public String getRootLabel() {
        return rootLabel;
    }
}
