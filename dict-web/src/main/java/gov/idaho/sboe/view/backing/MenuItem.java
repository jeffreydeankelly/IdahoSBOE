package gov.idaho.sboe.view.backing;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;

import oracle.adf.view.faces.component.core.nav.CoreCommandMenuItem;

public class MenuItem {
        private String _label          = null;
        private String _outcome        = null;
        private String _viewId         = "/catalog.jspx";
        private String _destination    = null;
        private String _icon           = null;
        private String _type           = CoreCommandMenuItem.TYPE_DEFAULT;
        private List   Children       = null;
        private ScratchPad.Modes mode  = null;
        private String sarg            = null;
        private CatalogElementBean selectedElement = null;
        private TabBean            selectedTab = null;
        
        //extended security attributes
        private boolean _readOnly = false;
        private boolean _shown = true; 
        private boolean _immediate = false;
       
       

        /**
         * Set the label of the menu item as it will appear in the UI
         * @param label
         */
        public void setLabel(String label) {
            this._label = label;
        }

        /**
         * Get the label for the menu item
         * @return String
         */
        public String getLabel() {
            return _label;
        }

        /**
         * Set the navigation rule to be raised by this menu
         * item when selected
         * @param outcome
         */
        public void setOutcome(String outcome) {
            this._outcome = outcome;
        }

        /**
         * Get the navigation outcome mapped by the menu item
         * @return String
         */
        public String getOutcome() {
            return _outcome;
        }

        /**
         * Set the ViewId (page) that this menu item represents
         * this is used to mark the menu item as current / selected when
         * on that page
         * @param viewId
         */
        public void setViewId(String viewId) {
            this._viewId = viewId;
        }

        /**
         * Getter for the viewId that this menu item points to
         * @return ViewID
         */
        public String getViewId() {
            return _viewId;
        }

        /**
         * Destination is used when the menu needs to render as a goMenuItem
         * rather than a commandMenuItem
         * @param destination
         */
        public void setDestination(String destination) {
            this._destination = destination;
        }

        /**
         * Get the destination (a string representation of a URL)
         * @return String
         */
        public String getDestination() {
            return _destination;
        }

        /** 
         * For Global items, the icon to use
         * @param icon
         */ 
        public void setIcon(String icon) {
            this._icon = icon;
        }

        /**
         * Get the icon for a global menu item
         * @return name of icon to use
         */
        public String getIcon() {
            return _icon;
        }

        /**
         * Type of Icon - default or global
         * used in the nodestamp for the menu facet to display the correct items
         * @param type
         */
        public void setType(String type) {
            this._type = type;
        }

        /**
         * Get the menu item type
         * @return default or global
         */
        public String getType() {
            return _type;
        }

        /**
         * Set the children of this menu item (subMenus)
         * @param children
         */
        public void setChildren(List children) {
            this.Children = children;
        }

        /**
         * Get the submenu items 
         * @return List of MenuItems
         */
        public List getChildren() {
            return Children;
        }

        /**
         * Define if the menu item should be read-only
         * @param readOnly
         */
        public void setReadOnly(boolean readOnly) {
            this._readOnly = readOnly;
        }

        /**
         * Get the readonly state of this item
         * @return boolean
         */
        public boolean isReadOnly() {
            return _readOnly;
        }

        /**
         * Define if this item should be visible at all
         * This is usually based on user role
         * @param shown
         */
        public void setShown(boolean shown) {
            this._shown = shown;
        }

        /**
         * Get the visibilty of this menu item
         * @return boolean
         */
        public boolean isShown() {
            return _shown;
        }

        public void setImmediate(boolean immediate)
        {
            this._immediate = immediate;
        }

        public boolean isImmediate()
        {
            return _immediate;
        }

    public void setSelectedElement(CatalogElementBean selectedElement) {
        this.selectedElement = selectedElement;
    }

    public CatalogElementBean getSelectedElement() {
        return selectedElement;
    }

    public void setSarg(String sarg)
    {
        this.sarg = sarg;
    }

    public String getSarg()
    {
        return sarg;
    }

    public void setMode(ScratchPad.Modes mode)
    {
        this.mode = mode;
    }

    public ScratchPad.Modes getMode()
    {
        return mode;
    }

    public void setSelectedTab(TabBean selectedTab)
    {
        this.selectedTab = selectedTab;
    }

    public TabBean getSelectedTab()
    {
        return selectedTab;
    }
}
