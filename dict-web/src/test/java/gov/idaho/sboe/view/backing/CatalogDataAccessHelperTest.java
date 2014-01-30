package gov.idaho.sboe.view.backing;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.GlossaryXRef;
import gov.idaho.sboe.services.CatalogFacade;
import gov.idaho.sboe.utils.JPAResourceBean;


public class CatalogDataAccessHelperTest extends /*AbstractJsf*/TestCase {
    private CatalogDataAccessHelper objUnderTest;
    final CatalogFacade catalogFacade;
    final JPAResourceBean resourceBean;

    public CatalogDataAccessHelperTest(String sTestName) {
        super(sTestName);
        resourceBean = new JPAResourceBean("edwi");
        catalogFacade = new CatalogFacade();
        catalogFacade.setPersistenceUnit(resourceBean.getFactory());
    }

    protected void setUp() throws Exception {
        super.setUp();
        // a hack way of properly setting the beans
        objUnderTest = new CatalogDataAccessHelper() {
            public Object getBean(final String id) {
                if (id.equals(Globals.CATALOG_FACADE)) {
                    if (catalogFacade==null) {
                    }
                    return catalogFacade;
                }
                if (id.equals(Globals.JPA_RESOURCE)) {
                    return resourceBean;
                }
                return null;
            }
        };
        objUnderTest.init();
    }

    /**
     * @see CatalogDataAccessHelper#findGlossaryChildren(CatalogElementBean,Map<String, List<GlossaryXRef>>,String)
     */
    public void testFindGlossaryChildren() {
        CatalogElementBean parent = new CatalogElementBean(new Glossary.PK("Data Element", "Homeless"));
//        GlossaryType type;
        Map<String, List<GlossaryXRef>> xrefList = resourceBean.getCacheManager(catalogFacade).getGlossaryXwalkTable();
//        resourceBean.getCacheManager(catalogFacade).getGlossaryListForType(type);
        List<Glossary.PK> results = objUnderTest.findGlossaryChildren(parent, xrefList, "Data Element");
        assertNotNull(results);
    }

    /**
     * @see CatalogDataAccessHelper#retrieveCatalog(String)
     */
    public void testRetrieveCatalog() throws Exception {
        List<CatalogElementBean> results = objUnderTest.retrieveCatalog();
        assertNotNull(results);
    }

    /**
     * @see CatalogDataAccessHelper#retrieveCatalog()
     */
    public void testRetrieveCatalog1() {
    }

    /**
     * @see CatalogDataAccessHelper#retrieveCatalog(TabBean)
     */
    public void testRetrieveCatalog2() {
    }

    /**
     * @see CatalogDataAccessHelper#retrieveCatalog(CatalogElementBean)
     */
    public void testRetrieveCatalog3() {
    }

    /**
     * @see CatalogDataAccessHelper#retrieveTabs()
     */
    public void testRetrieveTabs() {
    }

    /**
     * @see CatalogDataAccessHelper#retrieveCatalogTypes()
     */
    public void testRetrieveCatalogTypes() {
    }

    /**
     * @see CatalogDataAccessHelper#retrieveReferences(CatalogElementBean)
     */
    public void testRetrieveReferences() {
    }

    /**
     * @see CatalogDataAccessHelper#retrieveNarrativeHistory(CatalogElementBean)
     */
    public void testRetrieveNarrativeHistory() {
    }

    /**
     * @see CatalogDataAccessHelper#getCompleteItem(CatalogElementBean)
     */
    public void testGetCompleteItem() {
    }

    /**
     * @see CatalogDataAccessHelper#getUser(String)
     */
    public void testGetUser() {
    }

    /**
     * @see CatalogDataAccessHelper#init()
     */
    public void testInit() {
    }
}
