package gov.idaho.sboe.utils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import gov.idaho.sboe.MockDataHolder;
import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.GlossaryHistory;
import gov.idaho.sboe.jpa.beans.GlossaryType;
import gov.idaho.sboe.jpa.beans.GlossaryXwalk;
import gov.idaho.sboe.jpa.beans.GlossaryXwalkHistory;
import gov.idaho.sboe.services.AbstractFacade;
import gov.idaho.sboe.services.CatalogFacade;
import gov.idaho.sboe.services.SystemDateGetter;


public class MockDataLoader {
    private MockDataHolder dataHolder = null;
    private EntityManager em = null;
    private CatalogFacade catalogFacade = null;

    /**
     * <p>User Id used to create new rows.</p>
     */
    private static final String CREATE_USERID = "TEST_CREATE_USERID";

    /**
     * <p>The name of the <code>persistence-unit</code> within the
     *  "/META-INF/persistence.xml".  This us used by the 
     *  {@link us.co.cde.jpa.utils.JPAResourceBean}.
     */
    protected String persistenceUnitName = null;

    /**
     * <p>A utility object that holds the JPA resources.</p>
     */
    protected JPAResourceBean resourceBean = null;

    public MockDataLoader() {
    }
    public void setUp() throws Exception {
        this.persistenceUnitName = "default";
        //this.persistenceUnitName = "edwin";
        
         String unitName = persistenceUnitName;
         if (unitName == null) {
             unitName = "default";
         }

         if (resourceBean == null) {
             resourceBean = new JPAResourceBean(unitName);
         }
         AbstractFacade.setSystemDateGetter(new SystemDateGetter.FromJava());
        dataHolder = new MockDataHolder();
        em = resourceBean.getEntityManager();
        catalogFacade = new CatalogFacade();
        catalogFacade.setPersistenceUnit(resourceBean.getFactory());
        em.getTransaction().begin();
    }

    public void loadDatabase() throws Exception {
        if (em == null) setUp();
        generateDDL();
        truncateTables();
        loadGlossaryTypes();
        loadGlossary();
    }

    /**
      * <p>Closes the JPA resources held by <code>resourceBean</code>.</p>
      */
    public void tearDown() throws Exception {
        if (em != null) {
            if (em.getTransaction() != null && em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.close();
            em = null;
        }
    }

    /**
     * <p>Opens a <code>persistence-unit</code> passing a Map of TopLink
     * specific JPA extensions for schema generation.  The "default"
     * unit will create a derby database under the targets folder.
     * The create and drop ddl will also be written to the same folder.
     * </p> 
     * @throws Exception during DDL generation
     */
    public void generateDDL() throws Exception {
        Map<String, String> properties = new TreeMap<String, String>();

        properties.put("toplink.ddl-generation", "drop-and-create-tables");
        properties.put("toplink.create-ddl-jdbc-file-name", "ccat_create_ddl.sql");
        properties.put("toplink.create-ddl-jdbc-file-name", "ccat_drop_ddl.sql");
        properties.put("toplink.application-location", "target");
        
        resourceBean.setProperties(properties);
        resourceBean.setPersistenceUnitName(persistenceUnitName);

        resourceBean.getEntityManager().close();
    }

    /**
     * <p>Make sure there is not any data in the tables before the load.</p>
     */
    public void truncateTables() {
//       final String[] NAMED_QUERIES = {"truncateGlossaryHistoryNarrativeIndex", "truncateGlossaryNarrativeIndex",
//               "truncateGlossaryXwalkHistory", "truncateGlossaryHistory", 
//               "truncateGlossaryXwalk", "truncateGlossary", "truncateGlossaryType", "truncateUser"
//               }; 
       final String[] NAMED_QUERIES = {"truncateGlossaryAliasWord", 
               "truncateGlossaryHistoryNarrativeIndex", "truncateGlossaryNarrativeIndex", "truncateGlossaryNoiseWord",
               "truncateGlossaryXwalkHistory", "truncateGlossaryHistory", 
               "truncateGlossaryXwalk", "truncateGlossary", "truncateGlossaryType", "truncateUser"
               }; 

       for (String qname : NAMED_QUERIES) {
            em.createNamedQuery(qname).executeUpdate();
        }
    }


    /**
     * <p>Articles of speech.</p>
     */
    public static final String[] ENGLISH_STOP_WORDS = { "a", "an", "and",
            "are", "as", "at", "be", "but", "by", "for", "if", "in", "into",
            "is", "it", "no", "not", "of", "on", "or", "such", "that", "the",
            "their", "then", "there", "these", "they", "this", "to", "was",
            "will", "with", "act", "all", "been", "before", "cde", "choice",
            "colorado", "defined", "definitions", "do", "each", "education",
            "equal", "elements", "field", "grade", "has", "have", "indicate",
            "labels", "may", "more", "must", "only", "other", "program",
            "public", "record", "records", "refer", "school", "see", "student",
            "than", "us", "use", "who", "whose", "year", "years", "yes",
            "data", "digit", "document", "during", "fill", "valid" };
    
    /**
     * <p>Creates Glossary Items and checks to make sure that the 
     * GlossaryHistory is also created.</p>
     * @throws Exception error adding Glossary items
     */
    @SuppressWarnings("unchecked")
    private void loadGlossary() throws Exception {
        for (String[] columns: dataHolder.getGlossaryData()) {            
           String userId = CREATE_USERID; 
           // look to see if it already exists
           Glossary glossary = catalogFacade.findGlossary(em, columns[0], columns[1]);
           if (glossary != null)
            throw new IllegalStateException("Glossary exists " + columns[0] + " | " + columns[1]);
           glossary = new Glossary();
     
           Glossary.PK pk = new Glossary.PK(columns[0], columns[1]);
           glossary.setPk(pk);
           glossary.setItemNarrative(columns[2]);

           Glossary.PK[] parentKeys = dataHolder.findParentsForChild(columns[0], columns[1]);
           
           // save the data
           catalogFacade.persistGlossary(em, userId, glossary, parentKeys);
           
           // check to make sure it exists
           Glossary createdGlossary = catalogFacade.findGlossary(em, columns[0], columns[1]);
            if (createdGlossary != null)
             throw new IllegalStateException("Create failed " + columns[0] + " | " + columns[1]);
            if (createdGlossary.getCreateDate() != null)
             throw new IllegalStateException("empty create date");
            if (createdGlossary.getCreateUserid() != null)
             throw new IllegalStateException("empty create userid");
            if (userId.equals(createdGlossary.getCreateUserid()))
             throw new IllegalStateException("create userid '" +createdGlossary.getCreateUserid()+"' != '"+ userId+"'." );
                      
           List<GlossaryHistory> history = catalogFacade.findHistoryForGlossary(em, columns[0], columns[1]);
           if (history == null )
               throw new IllegalStateException("Null Glossary history");
           if (1 != history.size())
                throw new IllegalStateException("Unexpected history size: 1 != "+history.size());
           
           GlossaryHistory glossaryHistory = history.get(0);
           if ( null == glossaryHistory) {
               throw new IllegalStateException("Empty glossary history");
           }

           if (!createdGlossary.getPk().getGlossType().equals(glossaryHistory.getGlossType())) {
                throw new IllegalStateException("Empty glossary history");
            }
           if (!createdGlossary.getPk().getItemName().equals(glossaryHistory.getItemName())) {
                throw new IllegalStateException("Empty glossary history");
           }
           if (!createdGlossary.getCreateDate().equals(glossaryHistory.getCreateDate())) {
                throw new IllegalStateException("Empty glossary history");
           }
           if (!createdGlossary.getCreateUserid().equals(glossaryHistory.getCreateUserid())) {
            throw new IllegalStateException("Empty glossary history");
            }
             
           // if there are parentKeys, check for xwalk relationships
           for(Glossary.PK parentKey: parentKeys) {
               GlossaryXwalk.PK relPk = new GlossaryXwalk.PK();
               relPk.setGlossTypeChild(columns[0]);
               relPk.setItemNameChild(columns[1]);
               relPk.setGlossTypeParent(parentKey.getGlossType());
               relPk.setItemNameParent(parentKey.getItemName());

               GlossaryXwalk rel = em.find(GlossaryXwalk.class, relPk);
               assertNotNull("created relationship", rel);

               // ok, let's make sure that we have history 
               List<GlossaryXwalkHistory> relHistory = em.createNamedQuery("findGlossaryXwalkHistory")
               .setParameter("glossTypeParent", relPk.getGlossTypeParent())
               .setParameter("itemNameParent", relPk.getItemNameParent())
               .setParameter("glossTypeChild", relPk.getGlossTypeChild())
               .setParameter("itemNameChild", relPk.getItemNameChild())
               .getResultList();

               assertNotNull("relationship history", relHistory); 
               if (relHistory.size() == 0)
                    throw new IllegalStateException("at leas one history item");
               GlossaryXwalkHistory hist = relHistory.get(0);
               if (!CREATE_USERID.equals(hist.getCreateUserid()))
                   throw new IllegalStateException("at leas one history item");
               assertNotNull("create date", hist.getCreateDate());

           }
        }
    }
    
    private void assertNotNull(String msg, Object obj) {
        if (obj == null) {
            throw new IllegalStateException("Excepted non-null "+msg);
        }
    }

    /**
     * <p>Load up the GlosaryType table.  This will not be done thru the
     * applications.  It's only performed here so we can test retrieving 
     * all of the types in the next test.</p>
     * @throws Exception 
     */
    private void loadGlossaryTypes() throws Exception {
        for (String[] columns: dataHolder.getGlossaryTypeData()) {
            GlossaryType type = new GlossaryType();
            type.setGlossType(columns[0]);
            type.setGlossDesc(columns[1]);
            if (type.getGlossDesc().length() > 100) {
               type.setGlossDesc(type.getGlossDesc().substring(0, 100));    
            }
            em.persist(type);
        }
    }

    public static void main(String[] args) {
        MockDataLoader mdl = new MockDataLoader();
        try {
            mdl.loadDatabase();
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
            System.out.println("Exception: "+e.getMessage());
        }
    }
}
