package gov.idaho.sboe.view.backing;

import java.io.Serializable;

import java.text.DecimalFormat;
import java.text.Format;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.adf.view.faces.model.TreeModel;

import gov.idaho.sboe.jpa.beans.AbstractEntityBean;
import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.GlossaryHistory;
import gov.idaho.sboe.jpa.beans.GlossaryType;
import gov.idaho.sboe.services.FacadeException;

/**
 * <p>This is a generic wrapper that will decorate flavors
 * of the <code>AbstractEntityBean</code>.  It adds a collection
 * of children allowing it to work with several of the ADF 
 * components.</p>
 */
public class CatalogElementBean implements Serializable, Comparable, 
                                           Cloneable {
        
    private static final Logger log = Logger.getLogger(CatalogElementBean.class.getName()); {
        log.setLevel(Level.FINEST);
    }
    /**
     * @param instance pass the instance of the decorated <code>Glossary</code> entity bean
     */
    public CatalogElementBean(Glossary instance, boolean showType) {
        this(instance);
        this.showType = showType;        
    }
    public CatalogElementBean(Glossary instance) {
        this.instance = instance;
        calcItemType();
        calcItemName();
//        System.out.println("Creating CEB for glossary: "+instance.getPk().getGlossType()+"|"+instance.getPk().getItemName());
    }

    /**
     * @param instance pass the instance of the decorated <code>Glossary.PK</code> entity bean
     */
    public CatalogElementBean(Glossary.PK instance, boolean showType) {
        this(instance);
        this.showType = showType;
    }
    public CatalogElementBean(Glossary.PK instance) {
        this.instance = instance;
        calcItemType();
        calcItemName();
//        System.out.println/*log.fine*/("Creating CEB for glossary.pk: "+instance.getGlossType()+"|"+instance.getItemName());
    }

   
    /**
     * @param instance pass the instance of the decorated 
     * <code>GlossaryHistory</code> entity bean
     */
    public CatalogElementBean(GlossaryHistory instance) {
        this.instance = instance;
        calcItemType();
        calcItemName();
//        System.out.println/*log.fine*/("Creating CEB for glossaryHist: "+instance.getGlossType()+"|"+instance.getItemName());
    }
    public CatalogElementBean(GlossaryHistory instance, boolean showType) {
        this(instance);
        this.showType = showType;
    }

    /**
     * @param instance pass the instance of the decorated 
     * <code>GlossaryType</code> entity bean
     */
    public CatalogElementBean(GlossaryType instance) {
        this.instance = instance;
        calcItemType();
        calcItemName();
//        System.out.println/*log.fine*/("Creating CEB for glossaryType: "+instance.getGlossType());
    }
    public CatalogElementBean(GlossaryType instance, boolean showType) {
        this(instance);
        this.showType = showType;
    }

        
    /**
     * <p>Instance of the decorated entity bean that comes
     * in three flavors: <code>Glossary</code>, <code>GlossaryHistory</code>
     * and <code>GlossaryType</code>.</p>
     */
    private AbstractEntityBean instance = null;
    
    /**
     * @return Instance of the decorated entity bean.
     */
    public AbstractEntityBean getInstance() {
        return instance;    
    }
    
    /**
     * <p>Collection of associated catalog elements of the same
     * base type.</p>
     */
    private List<CatalogElementBean> children = null;
    
    /**
     * <p>A flag that indicates if a node can be copied
     * to another node in the tree representing the 
     * parent to child relationships.</p>
     */
    private boolean canCopy = true;

    /*
     * <p>The parent to this instance of the catalog bean.</p>
     */
    private CatalogElementBean parent = null;
    
    /**
     * <p>A flag that indicates if a node's detail
     * has been shown.</p>
     */
    private boolean showingDetail = false;

    /**
     * @return performs a deep clone of the object.
     * @throws CloneNotSupportedException error performing a deep clone
     */
    public Object clone() throws CloneNotSupportedException {
        CatalogElementBean clone = null;
        if (instance instanceof Glossary) {
          clone = new CatalogElementBean((Glossary) instance.clone());
        } else if (instance instanceof GlossaryHistory) {
            clone = new CatalogElementBean((GlossaryHistory) instance.clone());            
        } else if (instance instanceof GlossaryType) {
            clone = new CatalogElementBean((GlossaryType) instance.clone());                        
        } else if (instance instanceof Glossary.PK) {
            clone = new CatalogElementBean((Glossary.PK) instance.clone());
        }
    
        clone.canCopy = this.canCopy;
        clone.showingDetail = this.showingDetail;

        if (this.children != null) {
            List<CatalogElementBean> cloneChildren = 
                new ArrayList<CatalogElementBean>();

            for (CatalogElementBean child: children) {
                cloneChildren.add((CatalogElementBean)child.clone());
            }
        }
        
        if (this.referencesMenuAdapter != null)
        { 
            // Just give then reference to the referenceMenu, no need for the deep clone
            clone.setReferencesMenuAdapter(this.getReferencesMenuAdapter());
        }

        return clone;

    }

    private String itemName = null;

    /**
     * <p>Returns the <code>itemName</code> from the <code>instance</code>
     * that is a <code>Glossary</code> or <code>GlossaryHistory</code> or
     * the <code>glossType</code> if <code>instance</code> is a <code>GlossaryType</code>. 
     * </p>
     * @return item name
     */
    public String getItemName() {
        return itemName;
    }
        
    void calcItemName() {
        if (instance instanceof Glossary) {
            itemName = ((Glossary) instance).getPk().getItemName();
        } else if (instance instanceof GlossaryHistory) {
            itemName = ((GlossaryHistory) instance).getItemName();
        } else if (instance instanceof GlossaryType) {
            itemName = ((GlossaryType) instance).getGlossType();    
        } else if (instance instanceof Glossary.PK) {
            itemName = ((Glossary.PK) instance).getItemName();           
        }
    }

    /**
     * <p>Returns the <code>itemName</code> from the <code>instance</code>
     * that is a <code>Glossary</code> or <code>GlossaryHistory</code> or
     * the <code>glossType</code> if <code>instance</code> is a <code>GlossaryType</code>. 
     * </p>
     * @param itemName name of the instance
     */
    public void setItemName(String itemName) {
        if (instance instanceof Glossary) {
            ((Glossary) instance).getPk().setItemName(itemName);
        } else if (instance instanceof GlossaryHistory) {
            ((GlossaryHistory) instance).setItemName(itemName);
        } else if (instance instanceof GlossaryType) {
            ((GlossaryType) instance).setGlossType(itemName);    
        } else if (instance instanceof Glossary.PK) {
            ((Glossary.PK) instance).setItemName(itemName);
        }
    }

    private String itemType = null;
    
    /**
     * <p>Returns the <code>glossType</code> from the <code>instance</code>
     * that is a <code>Glossary</code>, <code>GlossaryHistory</code> or
     * <code>GlossaryType</code>. 
     * </p>
     * @return glossType
     */
    public String getItemType() {
        return itemType;
    }
    void calcItemType() {
        if (instance instanceof Glossary) {
            itemType = ((Glossary) instance).getPk().getGlossType();
        } else if (instance instanceof GlossaryHistory) {
            itemType = ((GlossaryHistory) instance).getGlossType();
        } else if (instance instanceof GlossaryType) {
            itemType = ((GlossaryType) instance).getGlossType();    
        } else if (instance instanceof Glossary.PK) {
            itemType = ((Glossary.PK) instance).getGlossType();
        }
    }

    private boolean showType = false;
    public void setShowType(boolean st) {
        showType = st;
    }
    
    public boolean isShowType() {
        return showType;
    }
    /**
     * @return if the <code>instance</code> is of type <code>GlossaryType</code> the
     * children count is appended to the <code>itemName</code>; otherwise, the <code>itemName</code>
     * is returned.
     */
    public String getDisplayItemName() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getItemName());
        if (showType) {
            buff.append(" [").append(this.getItemType()).append("]");
        }
        if (getIsGlossaryType()) {
            Format fmt = new DecimalFormat("###,##0");
            buff.append(" (")
                .append(fmt.format((getHasChildren() ? getChildren().size() : 0)))
                .append(")");
        }
        
        return buff.toString();
    }


    /**
     * <p>Sets the <code>glossType</code> from the <code>instance</code>
     * that is a <code>Glossary</code>, <code>GlossaryHistory</code> or
     * <code>GlossaryType</code>. 
     * </p>
     * @param itemType glossType
     */
    public void setItemType(String itemType) {

        if (instance instanceof Glossary) {
            ((Glossary) instance).getPk().setGlossType(itemType);
        } else if (instance instanceof GlossaryHistory) {
            ((GlossaryHistory) instance).setGlossType(itemType);
        } else if (instance instanceof GlossaryType) {
            ((GlossaryType) instance).setGlossType(itemType);    
        } else if (instance instanceof Glossary.PK) {
            ((Glossary.PK) instance).setGlossType(itemType);
        }

    }

    /**
     * @return beans that are associations of the <code>instance</code>
     */
    public List<CatalogElementBean> getChildren() {
        return children;
    }

    /**
     * @param children beans that are associations of the <code>instance</code>
     */
    public void setChildren(List<CatalogElementBean> children) {
        this.children = children;
    }

    /**
     * <p>Returns the <code>itemNarrative</code> from the <code>instance</code>
     * that is a <code>Glossary</code> or <code>GlossaryHistory</code> or
     * the <code>glossDesc</code> if <code>instance</code> is a <code>GlossaryType</code>. 
     * </p>
     * @return description of the <code>instance</code>
     */
     public String getItemNarrative() {
        return getItemNarrative(true);
     }

    public String getItemNarrative(boolean filterATags) {
        String itemNarrative = "";
        
        if (instance instanceof Glossary) {
            itemNarrative = ((Glossary) instance).getItemNarrative();
        } else if (instance instanceof GlossaryHistory) {
            itemNarrative = ((GlossaryHistory) instance).getItemNarrative();
        } else if (instance instanceof GlossaryType) {
            itemNarrative = ((GlossaryType) instance).getGlossDesc();    
        } 
        /*
         * A hack to work around an annoying Oracle style sheet issue that
         * sets A tags inside the selected item of a tree to white text.
         */
        if (filterATags) {
            itemNarrative = itemNarrative.replace("<a href", "<a style='color:#000000' href");
            itemNarrative = itemNarrative.replace("<A HREF", "<a style='color:#000000' href");
        }
        return itemNarrative;
    }

     public String getEffDateStart() {
        return getEffDateStart(true);
     }

    public String getEffDateStart(boolean filterATags) {
        String effDateString = null;
        if (instance instanceof Glossary) {
            Date createDate = ((Glossary) instance).getCreateDate();
            Date updateDate = ((Glossary) instance).getUpdateDate();
            Date effDate = null;
            
            if (updateDate != null)
                effDate = updateDate;
            else
                effDate = createDate;
                
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            effDateString = formatter.format(effDate);
        }
        /*
         * A hack to work around an annoying Oracle style sheet issue that
         * sets A tags inside the selected item of a tree to white text.
         */
        if (filterATags) {
            effDateString = effDateString.replace("<a href", "<a style='color:#000000' href");
            effDateString = effDateString.replace("<A HREF", "<a style='color:#000000' href");
        }
        return effDateString;
    }



    /**
     * <p>Sets the <code>itemNarrative</code> from the <code>instance</code>
     * that is a <code>Glossary</code> or <code>GlossaryHistory</code> or
     * the <code>glossDesc</code> if <code>instance</code> is a <code>GlossaryType</code>. 
     * </p>
     * @param itemNarrative description of the <code>instance</code>
     */
    public void setItemNarrative(String itemNarrative) {
        if (instance instanceof Glossary) {
            ((Glossary) instance).setItemNarrative(itemNarrative);
        } else if (instance instanceof GlossaryHistory) {
            ((GlossaryHistory) instance).setItemNarrative(itemNarrative);
        } else if (instance instanceof GlossaryType) {
            ((GlossaryType) instance).setGlossDesc(itemNarrative);    
        }
    }


    /**
     * @param item child of the <code>instance</code>
     */
    public void addChild(CatalogElementBean item) {
        if (children == null) {
            children = new ArrayList<CatalogElementBean>();
        }
        children.add(item);
    }

    /**
     * @return <code>true</code> if the <code>children</code> collection
     * is not empty.
     */
    public boolean getHasChildren() {
        if (children == null || children.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * <p>Compares this instance with another by comparing
     * the <code>itemName</code> and <code>itemType</code>.
     * </p>
     * @param obj1 object to compare
     * @return zero if the objects are equal; -1 if this is less than <code>obj1</code>
     */
    public int compareTo(Object obj1) {
        CatalogElementBean comp = (CatalogElementBean)obj1;
        int order = getItemName().compareToIgnoreCase(comp.getItemName());
        if (order != 0) {
            return order;
        }
        return getItemType().compareToIgnoreCase(comp.getItemType());
    }

    /**
     * @param canCopy can the node be copied to another node
     */
    public void setCanCopy(boolean canCopy) {
        this.canCopy = canCopy;
    }

    /**
     * @return can the node be copied to another node
     */
    public boolean getCanCopy() {
        return (canCopy && !getIsGlossaryType());
    }

    /**
     * @return can the node be pasted to clipboard
     * i.e. be a new parent of items.  Only false for
     * glossary types.
     */
    public boolean getCanPaste() {
        return (!getIsGlossaryType());
    }

   /**
     * @param parent logical predecessor to this node
     */
    public void setParent(CatalogElementBean parent) {
        this.parent = parent;
    }

    /**
     * @return logical predecessor to this node
     */
    public CatalogElementBean getParent() {
        return parent;
    }

    /**
     * @return <code>true</code> if this relationship can
     * be removed.  If there is not a parent, there is not 
     * a relationship to remove.  Can't delete a node
     * that's a <code>GlossaryType</code>
     */
    public boolean getCanDelete() {
        return (getCanCopy() && parent != null);
    }

    /**
     * @param comp object to compare
     * @return returns <code>true</code> if the objects are the same
     */
    public boolean equals(Object comp) {
        return (compareTo(comp) == 0);
    }
  
  
  /**
     * @return <code>true</code> if <code>instance</code> is of type <code>GlossaryType</code>
     */
    public boolean getIsGlossaryType() {
        return instance instanceof GlossaryType;
    }

    public void setShowingDetail(boolean showingDetail) {
        this.showingDetail = showingDetail;
    }

    public boolean getShowingDetail() {
        return showingDetail;
    }
    
    CatalogElementBean loadNarrativeHistory(CatalogDataAccessHelper dataHelper) throws FacadeException {
        setNarrativeHistory(dataHelper.retrieveNarrativeHistory(getItemType(), getItemName()));
        return this;
    }

    public CatalogElementBean loadNarrative(CatalogDataAccessHelper dataHelper) throws FacadeException {
        if (!(instance instanceof GlossaryType)) {
            instance = dataHelper.findGlossary(getItemType(), getItemName());
        }
        return this;
    }
    /**
     * <p>History list for the <code>selectedElement</code>.</p>
     */
    private List<CatalogElementBean> narrativeHistory = null;

    /**
     * @param narrativeHistory list used to populate the af:table component
     */
    public void setNarrativeHistory(List<CatalogElementBean> narrativeHistory) {
        this.narrativeHistory = narrativeHistory;
    }

    /**
     * @return data used to populate the af:table component
     */
    public List<CatalogElementBean> getNarrativeHistory() {
        return narrativeHistory;
    }

    /**
     * <p>Model that populates the parent or references to the
     * <code>selectedElement</code></p>
     */
    private TreeModelAdapter referencesMenuAdapter = null;
    
   /**
     * @param referencesMenuAdapter model behind the af:menuList component
     */
    public void setReferencesMenuAdapter(TreeModelAdapter referencesMenuAdapter) {
        this.referencesMenuAdapter = referencesMenuAdapter;
    }
        
    CatalogElementBean loadReferences(CatalogDataAccessHelper dataHelper) throws FacadeException {
        getReferencesMenuAdapter().setInstance(dataHelper.retrieveReferences(this));
        return this;
    }
        
    /**
     * @return model behind the af:menuList component
     */
    public TreeModelAdapter getReferencesMenuAdapter() {
        if (referencesMenuAdapter == null) {
            referencesMenuAdapter = new TreeModelAdapter();
            referencesMenuAdapter.setChildProperty("children");
        }

        return referencesMenuAdapter;
    }

    /**
     * @return model that wrappers the parent associations to the 
     * selected glossary item
     */
    public TreeModel getReferencesModel() {
        TreeModel result = null;
        result = getReferencesMenuAdapter().getModel();
        
        return result;
    }

}
