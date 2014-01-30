package gov.idaho.sboe.services;

import java.util.List;

import java.util.Map;

import gov.idaho.sboe.AbstractJPA;
import gov.idaho.sboe.MockDataHolder;
import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.GlossaryType;
import gov.idaho.sboe.jpa.beans.Glossary.PK;
import gov.idaho.sboe.jpa.beans.GlossaryXRef;

/**
 * Much like the LoadDataTest, run the CatalogFacade against the test
 * database.  Because it's the test database, we only run the read-only
 * methods.
 * 
 * N.B. The mock data doesn't really reflect what is in the db.
 * 
 * @author Chance_M
 *
 */
public class CatalogFacadeTest extends AbstractJPA {
    MockDataHolder dataHolder = null;
    private CatalogFacade catalogFacade;

    protected void setUp() throws Exception {
        this.persistenceUnitName = "edwi";
        super.setUp();
        catalogFacade = new CatalogFacade();
        catalogFacade.setPersistenceUnit(resourceBean.getFactory());
        dataHolder = new MockDataHolder();
    }

    public void testFindGlossaryStringString() throws Exception {
        // go through all the data we know about and look to see if it already exists
        Glossary glossary = 
            catalogFacade.findGlossary("Business Rule", "Base Salary");
        assertNotNull("Glossary not found 'Business Rule'|'Base Salary'", 
                      glossary);
    }

    public void testFindAllGlossaryTypes() throws Exception {
        List<GlossaryType> types = catalogFacade.findAllGlossaryType();
        assertNotNull("Glossary Types", types);
        assertEquals("size", 4, types.size());
    }

    public void testLoadSubtreeXwalk() throws Exception {
        Map<String, List<GlossaryXRef>> xrefs = catalogFacade.loadGlossaryXwalk();
        assertNotNull("GlossaryXwalk ", xrefs);
        List<GlossaryType> types = catalogFacade.findAllGlossaryType();
        assertNotNull("Types", types);
        for (GlossaryType gt: types) {
            List<GlossaryXRef> references = xrefs.get(gt.getGlossType());
            assertNotNull(references);
            assertTrue(gt.getGlossType(), references.size()>0); // might not be true
        }
    }

    public void testFindGlossaryChildrenStringString() throws Exception {
        List<PK> children = 
            catalogFacade.findGlossaryChildren("Collection", "Student October");
        assertNotNull("Glossary children", children);
    }

    public void testFindGlossaryParentsStringString() throws Exception {
        Glossary.PK testParent = 
            new Glossary.PK("Collection", "Student October");
        List<PK> children = 
            catalogFacade.findGlossaryChildren(testParent.getGlossType(), 
                                               testParent.getItemName());
        assertNotNull("Glossary children", children);
        for (PK pk: children) {
            List<PK> Parents = 
                catalogFacade.findGlossaryParents(pk.getGlossType(), 
                                                  pk.getItemName());
            assertNotNull("Glossary Parents", Parents);
            assertTrue(Parents.contains(testParent));
        }
    }
}
