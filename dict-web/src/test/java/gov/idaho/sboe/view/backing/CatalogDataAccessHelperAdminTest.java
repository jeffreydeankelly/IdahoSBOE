package gov.idaho.sboe.view.backing;

import java.util.ArrayList;
import java.util.List;

import java.util.Vector;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import junit.framework.TestCase;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.services.CatalogFacade;
import gov.idaho.sboe.services.FacadeException;
import gov.idaho.sboe.utils.JPAResourceBean;
import gov.idaho.sboe.view.utils.JdbcUtils;

public class CatalogDataAccessHelperAdminTest extends TestCase {
    final CatalogDataAccessHelper objUnderTest;
    final CatalogFacade catalogFacade;
    final JPAResourceBean resourceBean;
    final Security user;
    final EntityManagerFactory emf;
    final EntityManager em;

    public CatalogDataAccessHelperAdminTest(String sTestName) {
        super(sTestName);
        resourceBean = new JPAResourceBean("edwi");
        catalogFacade = new CatalogFacade();
        catalogFacade.setPersistenceUnit(resourceBean.getFactory());
        user = new Security() {
            public boolean getIsAuthenticated() { return true; }
            public String getUserId() { return "SYSTEM"; }
        };
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
                if (id.equals(Globals.SECURITY)) {
                    return user;
                }
               return null;
            }
        };
        emf = catalogFacade.getPersistenceUnit();
        em = emf.createEntityManager();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        objUnderTest.init();
    }
    
    @Override
    protected void tearDown() throws Exception {
        em.close();
    }
    /**
     * @see CatalogDataAccessHelper#deleteItemFromTree(CatalogElementBean)
     */
    public void testDeleteItemFromTree() throws FacadeException {
        final Glossary.PK pkDE_Homeless = 
            new Glossary.PK("Data Element", "Homeless");
        Glossary gDE_Homeless = new Glossary();
        gDE_Homeless.setItemNarrative("Homeless DE");
        gDE_Homeless.setPk(pkDE_Homeless);

        List<Glossary.PK> results = JdbcUtils.existsItem(em, pkDE_Homeless);
        if (results == null || results.size()==0) {
            JdbcUtils.insertItem(em, gDE_Homeless, user);
        }
        
        final Glossary.PK pkBR_Homeless = 
            new Glossary.PK("Business Rule", "Homeless");
        Glossary gBR_Homeless = new Glossary();
        gBR_Homeless.setItemNarrative("Homeless BR");
        gBR_Homeless.setPk(pkBR_Homeless);

        results = JdbcUtils.existsItem(em, pkBR_Homeless);
        if (results == null || results.size()==0) {
            JdbcUtils.insertItem(em, gBR_Homeless, user);
        }
        JdbcUtils.insertReference(em, pkBR_Homeless, pkDE_Homeless);

        CatalogElementBean child = new CatalogElementBean(gDE_Homeless);
        child.setParent(new CatalogElementBean(gBR_Homeless));
        objUnderTest.deleteItemFromTree(child);
        assertFalse(JdbcUtils.existsReference(em, pkBR_Homeless, pkDE_Homeless));
    }

    /**
     * @see CatalogDataAccessHelper#deleteItem(CatalogElementBean)
     */
    public void testDeleteItem() throws FacadeException {
        final Glossary.PK pkBR_Homeless = 
            new Glossary.PK("Data Element", "Homeless");
        List<Glossary.PK> results = JdbcUtils.existsItem(em, pkBR_Homeless);
        if (results == null || results.size()==0) {
            Glossary gBR_Homeless = new Glossary();
            gBR_Homeless.setItemNarrative("Homeless DE");
            gBR_Homeless.setPk(pkBR_Homeless);
            JdbcUtils.insertItem(em, gBR_Homeless, user);
        }
        CatalogElementBean child = new CatalogElementBean(pkBR_Homeless);
        objUnderTest.deleteItem(child);
        results = JdbcUtils.existsItem(em, pkBR_Homeless);
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    /**
     * @see CatalogDataAccessHelper#getNewItem()
     */
    public void testGetNewItem() {
        CatalogElementBean child = objUnderTest.getNewItem();
        assertNotNull(child);
        assertEquals("Data Element", child.getItemType());
        assertEquals("New Item", child.getItemName());
        assertEquals("Narrative", child.getItemNarrative());
    }

    /**
     * @see CatalogDataAccessHelper#saveItems(List<CatalogElementBean>)
     */
    public void testSaveItems() throws FacadeException {
        final Glossary.PK pkBR_Homeless = 
            new Glossary.PK("Business Rule", "Homeless");
        JdbcUtils.deleteItemAndReferences(em, pkBR_Homeless);
        Glossary gBR_Homeless = new Glossary();
        gBR_Homeless.setItemNarrative("Homeless BR");
        gBR_Homeless.setPk(pkBR_Homeless);
        CatalogElementBean child = new CatalogElementBean(gBR_Homeless);
        List<CatalogElementBean> children = new ArrayList<CatalogElementBean>();
        children.add(child);
        objUnderTest.saveItems(children);
        List<Glossary.PK> results = JdbcUtils.existsItem(em, pkBR_Homeless);
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    /**
     * @see CatalogDataAccessHelper#addItemsToParent(CatalogElementBean,List<CatalogElementBean>)
     */
    public void testAddItemsToParent() throws FacadeException {
        final Glossary.PK pkDE_HJU = 
            new Glossary.PK("Data Element", "HomelessJUnit");
        final Glossary.PK pkBR_Homeless = 
            new Glossary.PK("Business Rule", "Homeless");
        JdbcUtils.deleteItemAndReferences(em, pkDE_HJU);
        final Glossary g = new Glossary();
        g.setPk(pkDE_HJU);
        g.setItemNarrative("JUnit Narrative");
        final CatalogElementBean child = new CatalogElementBean(g);
        final List<CatalogElementBean> children = new ArrayList<CatalogElementBean>();
        children.add(child);
        final CatalogElementBean selectedCatalogElement = new CatalogElementBean(pkBR_Homeless);
        objUnderTest.addItemsToParent(selectedCatalogElement, children);
        assertTrue(JdbcUtils.existsReference(em, pkBR_Homeless, pkDE_HJU));
        List<Glossary.PK> results = JdbcUtils.existsItem(em, pkDE_HJU);
        assertNotNull(results);
        assertEquals(1, results.size());
        results = JdbcUtils.existsParentReferences(em, pkBR_Homeless);
        assertNotNull(results);
        assertTrue(results.size()>0);       
    }
}
