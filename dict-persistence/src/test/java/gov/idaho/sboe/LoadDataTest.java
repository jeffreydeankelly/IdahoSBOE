package gov.idaho.sboe;

import java.util.List;

import javax.persistence.EntityManager;

import junit.framework.Test;
import junit.framework.TestSuite;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.GlossaryHistory;
import gov.idaho.sboe.jpa.beans.GlossaryType;
import gov.idaho.sboe.jpa.beans.GlossaryXwalk;
import gov.idaho.sboe.jpa.beans.GlossaryXwalkHistory;
import gov.idaho.sboe.services.AbstractFacade;
import gov.idaho.sboe.services.CatalogFacade;
import gov.idaho.sboe.services.NarrativeIndexingFacade;
import gov.idaho.sboe.services.SystemDateGetter;
import gov.idaho.sboe.utils.DatabaseImport;
import gov.idaho.sboe.utils.MockDataLoader;


public class LoadDataTest extends AbstractJPA {

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(LoadDataTest.class);
    }

    /**
     * <p>User Id used to create new rows.</p>
     */
    private static final String CREATE_USERID = "TEST_CREATE_USERID";

    /**
     * <p>User Id used to delete new rows.</p>
     */
    private static final String DELETE_USERID = "TEST_DELETE_USERID";


    /**
     * <p>User Id used to update rows.</p>
     */
    private static final String UPDATE_USERID = "TEST_UPDATE_USERID";

    private MockDataHolder dataHolder = null;
    private EntityManager em = null;
    private CatalogFacade catalogFacade = null;

    private static boolean isDatabaseInitialized = false;

    /**
     * <p>Loads the mock data sources into a list of string arrays.</p>
     */
    @Override
    protected void setUp() throws Exception {
        this.persistenceUnitName = "default";
        //this.persistenceUnitName = "edwin";
        
        super.setUp();
        dataHolder = new MockDataHolder();
        em = resourceBean.getEntityManager();
        catalogFacade = new CatalogFacade();
        catalogFacade.setPersistenceUnit(resourceBean.getFactory());
        if (!isDatabaseInitialized) {
            MockDataLoader mdl = new MockDataLoader();
            mdl.setUp();
            mdl.truncateTables();
            mdl.tearDown();
            // don't really want to repeat these here, but don't see how
            // to get them from the JPA layer :(
            DatabaseImport di = new DatabaseImport("app", "derby",
                "jdbc:derby:c:/eclipspace/DataDictionary/ccat-parent/ccat-jpa/target/ccat-db;create=true",
                "org.apache.derby.jdbc.EmbeddedDriver");
            di.loadTheTables();
            isDatabaseInitialized = true;
        }
        AbstractFacade.setSystemDateGetter(new SystemDateGetter.FromJava());
        em.getTransaction().begin();
    }
    /**
      * <p>Closes the JPA resources held by <code>resourceBean</code>.</p>
      */
    @Override
    public void tearDown() throws Exception {
        if (em != null) {
            if (em.getTransaction() != null && em.getTransaction().isActive()) {
                // prevent changes to the database
                em.getTransaction().rollback();
            }
            em.close();
        }
        super.tearDown();
    }

    public void testLoadTheDatabase() throws Exception {
        
    }
   
    /**
     * <p>Tests finding all the glossary types.</p>
     */
    public void testFindAllGlossaryTypes() throws Exception {
        List<GlossaryType> types = catalogFacade.findAllGlossaryType();
        assertNotNull("Glossary Types", types);
        assertEquals("size", dataHolder.getGlossaryTypeData().size(), types.size());
    }

    /**
     * <p>Test deleting a Glossary item that has children.  Verify both 
     * glossary and glossary relationship history is logged.</p>
     * @throws Exception test failed due to exception
     */
    @SuppressWarnings("unchecked")
    public void testGlossaryRemove() throws Exception {
        Glossary glossary = catalogFacade.findGlossary(em, "Collection", "Student October");
        assertNotNull(glossary);
        List<Glossary.PK> children = catalogFacade.findGlossaryChildren(em, glossary.getPk().getGlossType(), glossary.getPk().getItemName());
        assertNotNull(children);
    
        // delete the glossary item and all it's child relationships
        catalogFacade.removeGlossary(em, DELETE_USERID, glossary.getPk().getGlossType(), glossary.getPk().getItemName());

        Glossary checkGlossary = catalogFacade.findGlossary(em, "Collection", "Student October");
        assertNull("glossary deleted", checkGlossary);

        // look for history on the deleted glossary item
        List<GlossaryHistory> glossaryHistory = catalogFacade.findHistoryForGlossary(em, glossary.getPk().getGlossType(), glossary.getPk().getItemName());
        assertNotNull(glossaryHistory);
        // make sure we have history for each glossary item
        for (Glossary.PK child: children) {

            List<GlossaryXwalkHistory> relHistory = em.createNamedQuery("findGlossaryXwalkHistory")
            .setParameter("glossTypeParent", glossary.getPk().getGlossType())
            .setParameter("itemNameParent", glossary.getPk().getItemName())
            .setParameter("glossTypeChild", child.getGlossType())
            .setParameter("itemNameChild", child.getItemName())
            .getResultList();

            assertNotNull("relationship history", relHistory);

            boolean hasAuditDelete = false;
            for (GlossaryXwalkHistory hist: relHistory) {
                if (hist.getDeleteUserid() != null) {
                    hasAuditDelete = true;
                    assertEquals("delete userid", DELETE_USERID, hist.getDeleteUserid());
                    assertNotNull("delete date", hist.getDeleteDate());
                }
            }

            assertTrue("has (delete) GlossaryXwalkHistory", hasAuditDelete);

        }
    }
    
    
    /**
     * <p>Creates a tree structure and verifies.<br/>
     * <ul>
     *    <li>folderA1
     *        <ul>
     *            <li>folderB
     *               <ul>
     *                   <li>folderC</li>
     *               </ul>
     *            </li>
     *        </ul>
     *    </li>
     *    <li>folderA2
     *        <ul>
     *            <li>folderB
     *               <ul>
     *                   <li>folderC</li>
     *               </ul>
     *            </li>
     *        </ul>
     *    </li>
     * </ul>
     * </p>
     * @throws Exception error runing test
     */
    public void testGlossaryCreateTree() throws Exception {
        // create two root containers
        Glossary folderA1 = new Glossary();
        folderA1.setPk(new Glossary.PK("Container", "folderA1"));
        folderA1.setItemNarrative("Folder A1");

        Glossary folderA2 = new Glossary();
        folderA2.setPk(new Glossary.PK("Container", "folderA2"));
        folderA2.setItemNarrative("Folder A2");

        // save both folder
        catalogFacade.persistGlossary(em, CREATE_USERID, folderA1);
        catalogFacade.persistGlossary(em, CREATE_USERID, folderA2);

        assertNotNull("folderA1", catalogFacade.findGlossary(em, folderA1.getPk().getGlossType(), folderA1.getPk().getItemName()));
        assertNotNull("folderA2", catalogFacade.findGlossary(em, folderA2.getPk().getGlossType(), folderA2.getPk().getItemName()));
        
        // create a sub folder
        Glossary folderB = new Glossary();
        folderB.setPk(new Glossary.PK("Container", "folderB"));
        folderB.setItemNarrative("Folder B");
        
        // save the sub folder and associate with two parent containers
        catalogFacade.persistGlossary(em, CREATE_USERID, folderB, new Glossary.PK[] {folderA1.getPk(), folderA2.getPk()});
        
        assertNotNull("folderB", catalogFacade.findGlossary(em, folderB.getPk().getGlossType(), folderB.getPk().getItemName()));
        
        // check to make sure that folder A1 has one child
        List<Glossary.PK> folderA1Children = catalogFacade.findGlossaryChildren(em, folderA1.getPk().getGlossType(), folderA1.getPk().getItemName());
        assertNotNull("folderA1's children", folderA1Children);
        assertEquals("folderA1 has 1 child", 1, folderA1Children.size());
        
        Glossary.PK child = folderA1Children.get(0);
        assertEquals("folderA1's child is folderB", folderB.getPk(), child);
        

        // check to make sure that folder A2 has one child
        List<Glossary.PK> folderA2Children = catalogFacade.findGlossaryChildren(em, folderA2.getPk().getGlossType(), folderA2.getPk().getItemName());
        assertNotNull("folderA2's children", folderA2Children);
        assertEquals("folderA2 has 1 child", 1, folderA2Children.size());
        
        child = folderA2Children.get(0);
        assertEquals("folderA2's child is folderB", folderB.getPk(), child);

        
        // create a sub folder
        Glossary folderC = new Glossary();
        folderC.setPk(new Glossary.PK("Container", "folderC"));
        folderC.setItemNarrative("Folder C");
        
        // save the sub folder and associate with folderB
        catalogFacade.persistGlossary(em, CREATE_USERID, folderC, new Glossary.PK[] {folderB.getPk()});

        assertNotNull("folderC", catalogFacade.findGlossary(em, folderC.getPk().getGlossType(), folderC.getPk().getItemName()));

        // check to make sure that folder A2 has one child
        List<Glossary.PK> folderBChildren = catalogFacade.findGlossaryChildren(em, folderB.getPk().getGlossType(), folderB.getPk().getItemName());
        assertNotNull("folderB's children", folderBChildren);
        assertEquals("folderB has 1 child", 1, folderBChildren.size());
        
        child = folderBChildren.get(0);
        assertEquals("folderC's child is folderB", folderC.getPk(), child);
    }
    
    /**
     * <p>Checks to make sure that "folderC" has one parent that is 
     * "folderB".</p>
     * 
     * @throws Exception error finding parents
     */
    public void testFindGlossaryParents() throws Exception {
        List<Glossary.PK> parents = catalogFacade.findGlossaryParents(em, "Container", "folderC");
        assertNotNull("folderC parents", parents);
        assertEquals("folderC parents",  1, parents.size());
        
        Glossary.PK folderBKey = parents.get(0);
        assertEquals("folderB", "folderB", folderBKey.getItemName());
    } 
    
    /**
     * <p>Checks the logic to determine if the relationship being added is
     * circular.</p>
     */
    public void testExplicitCircularReferenceCheck() {
        GlossaryXwalk.PK pk = new GlossaryXwalk.PK();
        pk.setGlossTypeParent("Container");
        pk.setItemNameParent("folderC");
        pk.setGlossTypeChild("Container");
        pk.setItemNameChild("folderA1");
        
        assertTrue("circular reference", catalogFacade.checkCircularAssociation(em, pk));
    }

    /**
     * <p>Verifies that an optional parent relationship is ignored 
     * if it's circular.  This is an implicit check when calling
     * <code>CatalogFacade.persistGlossary</code></p> 
     * @throws Exception error updating a Glossary
     */
    public void testImplicitCircularReferenceCheck() throws Exception {
        Glossary folderA1 = catalogFacade.findGlossary(em, "Container", "folderA1");
        assertNotNull("folderA1", folderA1);

        Glossary.PK parentRel = new Glossary.PK("Container", "folderC");
        catalogFacade.persistGlossary(em, UPDATE_USERID, folderA1, new Glossary.PK[] {parentRel});

        List<Glossary.PK> children = catalogFacade.findGlossaryChildren(em, parentRel.getGlossType(), parentRel.getItemName());
        assertEquals("folderC no children", 0, children.size());
    }
    
    /**
     * <p>Checks changes to the item's narrative.</p>
     * @throws Exception test failed
     */
    @SuppressWarnings("unchecked")
    public void testNarrativeUpdate() throws Exception {
        Glossary folderA1 = catalogFacade.findGlossary(em, "Container", "folderA1");
        assertNotNull("folderA1", folderA1);
//        List<GlossaryNarrativeIndex> index = em.createNamedQuery("findGlossaryIndex")
//             .setParameter("glossType", "Container")
//             .setParameter("itemName", "folderA1")
//             .getResultList();
//        
//        assertNotNull("index", index);
                    
        //folderA1, folder A1
//        assertEquals(3, index.size());
//        folderA1.setItemNarrative("this is A1 test go cat");
//        catalogFacade.persistGlossary(em, UPDATE_USERID, folderA1);
//        index = em.createNamedQuery("findGlossaryIndex")
//                .setParameter("glossType", "Container")
//                .setParameter("itemName", "folderA1")
//                .getResultList();
//           
//        assertNotNull("index", index);
           
           //folderA1 = word (itemName)
           //this = noise word
           //is   = noise word
           //A1   = word
           //test = word
           //go   = word
           //leave = alias to go
           //cat   = word
           //feline = alias to cat
           
//           assertEquals(7, index.size());
    }

    /**
     * <p>Test a wildcard search.  This will combine the results from the
     * {@link GlossaryNavigationIndex} and {@link GossaryHistoryNavigationIndex}.</p>
     * @exception Exception test failed due to an exception
     */
    public void testNarrativeWildcardSearch() throws Exception {
        NarrativeIndexingFacade indexFacade = catalogFacade.getNarrativeIndexingFacade();
        
        List<Glossary.PK> results = indexFacade.searchGlossaryWildcard(em, 
            "the feline ate a mouse", "Collection"); 
        assertNotNull(results);
        
        assertEquals(1, results.size());
    }
    
    /**
     * <p>This call simulates tab changes.  The results are between a 
     * character start and end offset.  This search is case insensitive.</p>
     * @throws Exception error performing test
     */
    public void testRolodexSearch() throws Exception {
        NarrativeIndexingFacade indexFacade = catalogFacade.getNarrativeIndexingFacade();
        List<Glossary.PK> results = indexFacade.searchGlossaryRolodex(em, 'a', 'b');
        assertNotNull(results);
        assertEquals(3, results.size());
    }
    
    /**
     * <p>Tests deleting relationships only.  This test uses the tree created in the 
     * previous example.</p>
     *
     * @throws Exception error running test 
     */
    public void testDeleteRelationships() throws Exception {
        List<Glossary.PK> folderA2Children = catalogFacade.findGlossaryChildren(em, "Container", "folderA2");
        assertEquals("folderA2 children", 1, folderA2Children.size());

        List<Glossary.PK> folderBChildren = catalogFacade.findGlossaryChildren(em, "Container", "folderB");
        assertEquals("folderB children", 1, folderBChildren.size());

        // invoke a cascade delete 
        catalogFacade.removeGlossaryRelationship(em, DELETE_USERID, "Container", "folderA2", "Container", "folderB");

        folderA2Children = catalogFacade.findGlossaryChildren(em, "Container", "folderA2");
        assertEquals("folderA2 children", 0, folderA2Children.size());

        folderBChildren = catalogFacade.findGlossaryChildren(em, "Container", "folderB");
        assertEquals("folderB children", 0, folderBChildren.size());
    }
    
    /**
     * <p>Delete a Glossary "folderB".  This will cause a cascade
     * delete of the relationships associated with folderB.
     * We will also check to make sure the parent relationship
     * to "folderA1" is removed.</p>
     * @throws Exception error running test 
     */
    public void testDeleteGlossaryCascadeRelationships() throws Exception {
        // invoke a cascade delete 
        catalogFacade.removeGlossary(em, DELETE_USERID, "Container", "folderB");
        assertNull("folderB", catalogFacade.findGlossary(em, "Container", "folderB"));
        
        final String[][] RELATIONSHIPS = {
                {"folderA1", "folderB"},
                {"folderB", "folderC"}
        };

        for (int i = 0; i < RELATIONSHIPS.length; i++) {
            GlossaryXwalk.PK pk = new GlossaryXwalk.PK();
            pk.setGlossTypeParent("Container");
            pk.setItemNameChild("Container");
            pk.setItemNameParent(RELATIONSHIPS[i][0]);
            pk.setItemNameParent(RELATIONSHIPS[i][1]);

            assertNull(pk.toString(), em.find(GlossaryXwalk.class, pk));
        }
        
    }
    
}
