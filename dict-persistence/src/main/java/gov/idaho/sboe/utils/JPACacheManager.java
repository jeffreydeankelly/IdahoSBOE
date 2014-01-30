package gov.idaho.sboe.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.GlossaryType;
import gov.idaho.sboe.jpa.beans.GlossaryXRef;
import gov.idaho.sboe.services.CatalogFacade;
import gov.idaho.sboe.services.FacadeException;


/**
 * Because this application is a read-mostly (and I don't see that JPA
 * persistence/cache management is designed to handle caching individual,
 * PK-referenced objects, I've added this cache which will hold collections
 * of glossary entries and will be added to the *application* scope.
 * 
 * @author Chance_M
 */
public class JPACacheManager {
    private static Logger log = Logger.getLogger(JPACacheManager.class
            .getName());

    public JPACacheManager() {
    }
    
    void init(CatalogFacade catalogFacade) {
        try {
            glossaryTypes = catalogFacade.findAllGlossaryType();
            xrefTable = catalogFacade.loadGlossaryXwalk();
            
            glossaryRoots = new HashMap<GlossaryType,List<Glossary.PK>>();
            for (GlossaryType type:glossaryTypes) {
                if (type.isGlossVisible()) {
                    glossaryRoots.put(type, catalogFacade.getNarrativeIndexingFacade().searchGlossary(type.getGlossType()));
                }
            }

        } catch (FacadeException e) {
            log.log(Level.SEVERE, "FacadeException", e);
        }
    }


    public void reset(CatalogFacade catalogFacade) {
        xrefTable = null;
        init(catalogFacade);
    }

    private Map<String, List<GlossaryXRef>> xrefTable = null;
    
    /**
     * This method added with default access for use in testing.
     * Generally the table will be set via the init method.
     * 
     * @param xrefTable
     */
    void setGlossaryXwalkTable(Map<String, List<GlossaryXRef>> xrefTable) {
        this.xrefTable = xrefTable;
    }
    
    public Map<String, List<GlossaryXRef>> getGlossaryXwalkTable() {
        return xrefTable;
    }
 
    private List<GlossaryType> glossaryTypes = null;
    
    /**
     * This method added with default access for use in testing.
     * Generally the table will be set via the init method.
     * 
     * @param glossaryTypes
     */
    void setGlossaryTypes(List<GlossaryType> glossaryTypes) {
        this.glossaryTypes = glossaryTypes;
    }
    
    public List<GlossaryType> getGlossaryTypes() {
        return glossaryTypes;
    }
    
    private Map<GlossaryType,List<Glossary.PK>> glossaryRoots = null;
    
    /**
    * This method added with default access for use in testing.
    * Generally the table will be set via the init method.
    * 
    * @param glossaryRoots
    */
    void setGlossaryRoots(Map<GlossaryType,List<Glossary.PK>> glossaryRoots) {
       this.glossaryRoots = glossaryRoots;
    }
    
    public Map<GlossaryType,List<Glossary.PK>> getGlossaryRoots() {
       return glossaryRoots;
    }

    public List<Glossary.PK> getGlossaryListForType(GlossaryType type) {
       return glossaryRoots.get(type);
    }
    
}
