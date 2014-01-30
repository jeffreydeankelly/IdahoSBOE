package gov.idaho.sboe.view.backing;

import java.io.Serializable;

/**
 * <p>This bean holds state to keep track of the Tab selection
 * on the Catalog page.</p>
 */
public class TabBean implements Serializable {
    /**
     * <p>Is this tab selected.</p>
     */
    private boolean selected = false;
    
    /**
     * <p>The starting range of the tab filter.</p> 
     */
    private char beginFilter;
    
    /**
     * <p>Ending range of the tab filter.</p>
     */
    private char endFilter;

    /**
     * @param isSelected Tab's visual selection state
     * @param beginFilter starting tab filter
     * @param endFilter ending tab filter
     */
    public TabBean(boolean isSelected, char beginFilter, char endFilter) {
        this.selected = isSelected;
        this.beginFilter = beginFilter;
        this.endFilter = endFilter;
    }

    /**
     * @return text describing the <code>beginFilter</code> and the
     * <code>endFilter</code>.
     */
    public String getText() {
        StringBuffer buff = new StringBuffer();
        if (beginFilter != endFilter) {
           buff.append(beginFilter)
               .append(" - ").append(endFilter);
        } else { 
           buff.append(beginFilter);
        }
        
        
        
        return buff.toString();
    }

    /**
     * The viewId of a node can be null, but if set it must be unique.
     * @return null
     */
    public Object getViewId() {
        return null;
    }

    /**
     * 
     * @param selected sets the selectability
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * @return <code>true</code> if the tab is selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @return starting tab range
     */
    public char getBeginFilter() {
        return beginFilter;
    }

    /**
     * @return ending tab range
     */
    public char getEndFilter() {
        return endFilter;
    }
}
