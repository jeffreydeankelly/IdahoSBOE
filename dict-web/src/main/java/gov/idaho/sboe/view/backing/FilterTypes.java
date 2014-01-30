package gov.idaho.sboe.view.backing;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.shale.tiger.managed.Bean;
import org.apache.shale.tiger.managed.Scope;

import org.apache.shale.tiger.view.View;

import org.apache.shale.view.AbstractViewController;

import gov.idaho.sboe.jpa.beans.GlossaryType;
import gov.idaho.sboe.services.CatalogFacade;
import gov.idaho.sboe.utils.JPAResourceBean;

/**
 * <p>Class to hold constants used by the view tier.</p>
 */
 @Bean(name = "rolodexFilter", scope = Scope.SESSION)
 @View
public class FilterTypes extends AbstractViewController
{
    private List<GlossaryType> types = null;
    public List<SelectItem> filterList = new ArrayList<SelectItem>();
    public String selectedFilter = Globals.DATA_ELEMENT_TYPE;

   public FilterTypes() 
   {
       init();    
   }
        
        
  public void init() 
  {
      if (types == null) 
      {
          types = ((JPAResourceBean)getBean(Globals.JPA_RESOURCE)).
                  getCacheManager((CatalogFacade)getBean(Globals.CATALOG_FACADE)).getGlossaryTypes();
                  
          for (GlossaryType glossType: types) 
          {
             filterList.add(new SelectItem(glossType.getGlossType(), glossType.getGlossType()));
          }
      }
  }
  
  public  List<SelectItem> getFilterList() 
  {
      return filterList;
  }


    public void setSelectedFilter(String selectedFilter) 
    {
        this.selectedFilter = selectedFilter;
    }

    public String getSelectedFilter() 
    {
        return selectedFilter;
    }
    
    public void resetFilterSelection() 
    {
        selectedFilter = Globals.DATA_ELEMENT_TYPE;
    }
}
