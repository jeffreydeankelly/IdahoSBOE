package gov.idaho.sboe.services;

import java.util.Set;

import gov.idaho.sboe.AbstractJPA;
import gov.idaho.sboe.MockDataHolder;
import gov.idaho.sboe.services.NarrativeIndexingFacade;

public class NarrativeIndexingFacadeTest extends AbstractJPA {
    NarrativeIndexingFacade nif;
    MockDataHolder dataHolder = null;

    protected void setUp() throws Exception {
        this.persistenceUnitName = "edwi";
        super.setUp();
        nif = new NarrativeIndexingFacade();
        nif.setPersistenceUnit(resourceBean.getFactory());
        dataHolder = new MockDataHolder();
    }

    /**
     * @see NarrativeIndexingFacade#concatSingleTokenPhrase(StringBuffer,StringBuffer,String,String,int)
     */
    public void testConcatSingleTokenPhrase() {
        StringBuffer querySql = new StringBuffer();
        StringBuffer selectSql = new StringBuffer();
        nif.concatSingleTokenPhrase(querySql, selectSql, "field", "search", 1);
        String sql = querySql.toString();
        assertTrue("Query string empty", sql.length()>0);
        sql = selectSql.toString();
        assertTrue("Select string empty", sql.length()>0);
    }

    /**
     * @see NarrativeIndexingFacade#concatDoubleTokenPhrase(StringBuffer,StringBuffer,String,String,String,int)
     */
    public void testConcatDoubleTokenPhrase() {
        StringBuffer querySql = new StringBuffer();
        StringBuffer selectSql = new StringBuffer();
        nif.concatDoubleTokenPhrase(querySql, selectSql, "field", "tok1", "tok2", 3);
        String sql = querySql.toString();
        assertTrue("Query string empty", sql.length()>0);
        sql = selectSql.toString();
        assertTrue("Select string empty", sql.length()>0);
    }
 
    /**
     * @see NarrativeIndexingFacade#concatDoubleTokenPhrase(StringBuffer,StringBuffer,String,String,String,int)
     */
    public void testConcatMultipleTokenPhrase() throws Exception {
        StringBuffer querySql = new StringBuffer();
        StringBuffer selectSql = new StringBuffer();
        nif.concatFromIterator(querySql, selectSql, "field", nif.tokenizeNarrative("this was the time"), 3);
        String sql = querySql.toString();
        assertTrue("Query string empty", sql.length()>0);
        sql = selectSql.toString();
        assertTrue("Select string empty", sql.length()>0);
    }

    ;
    /**
     * @see NarrativeIndexingFacade#buildWhereClause(String,StringBuffer,StringBuffer)
     */
    public void testBuildWhereOnlyClause() throws Exception {
        String where = nif.buildWhereClause("search", null, null);
        assertTrue("Where clause empty", where.length()>0);
        assertEquals("Unexpected where clause", 
            " CONTAINS(ItemNarrative, '$search', 1) > 0 OR CONTAINS(ItemNarrative, 'syn(search)', 2) > 0 OR"+
            " CONTAINS(ItemName, '$search', 3) > 0 OR CONTAINS(ItemName, 'syn(search)', 4) > 0",
            where);
    }

    /**
     * @see NarrativeIndexingFacade#buildWhereClause(String,StringBuffer,StringBuffer)
     */
    public void testBuildDoubleWhereOnlyClause() throws Exception {
        String where = nif.buildWhereClause("search replace", null, null);
        assertTrue("Where clause empty", where.length()>0);
//        assertEquals("Unexpected where clause", 
//            " CONTAINS(ItemNarrative, '$search', 1) > 0 OR CONTAINS(ItemNarrative, 'syn(search)', 2) > 0 OR"+
//            " CONTAINS(ItemName, '$search', 3) > 0 OR CONTAINS(ItemName, 'syn(search)', 4) > 0",
//            where);
    }

    public void testBuildWhereAndSelectClause() throws Exception {
        StringBuffer selectSql = new StringBuffer();
        String where = nif.buildWhereClause("search", selectSql, null);
        assertTrue("Where clause empty", where.length()>0);
        assertEquals("Unexpected where clause", 
            " CONTAINS(ItemNarrative, '$search', 1) > 0 OR CONTAINS(ItemNarrative, 'syn(search)', 2) > 0 OR"+
            " CONTAINS(ItemName, '$search', 3) > 0 OR CONTAINS(ItemName, 'syn(search)', 4) > 0",
            where);
        assertEquals("Unexpected select clause",
            "score(1)+score(2)+score(3)+score(4)",
            selectSql.toString());
    }


    public void testBuildMultiWhereAndSelectClause() throws Exception {
        StringBuffer selectSql = new StringBuffer();
        StringBuffer orderBySql = new StringBuffer();
        String where = nif.buildWhereClause("homeless student lunch program", selectSql, orderBySql);
        assertTrue("Where clause empty", where.length()>0);
        String sql = selectSql.toString();
        assertTrue("Select clause empty", sql.length()>0);
    }

    /**
     * @see NarrativeIndexingFacade#tokenizeNarrative(String)
     */
    public void testTokenizeNarrative() throws Exception {
        Set<String> res = nif.tokenizeNarrative("CO English Language Assessment Student Biographical Data Review - Form ASMT-108");
        assertNotNull("Null results", res);
        assertTrue("Empty results", res.size()>0);
    }
    /**
     * @see NarrativeIndexingFacade#tokenizeNarrative(String)
     * 
     * This test is Oracle-specific.  Would need to mock out in Derby :(
     */
    public void testIsOracleStopWordTrue() throws Exception {
        boolean res = nif.isOracleStopWord("for");
        assertTrue("Stop word not found", res);
    }
    /**
     * @see NarrativeIndexingFacade#tokenizeNarrative(String)
     *
     * This test is Oracle-specific.  Would need to mock out in Derby :(
     */
    public void testIsOracleStopWordFalse() throws Exception {
        boolean res = nif.isOracleStopWord("english");
        assertTrue("Stop word found incorrectly", !res);
    }
}