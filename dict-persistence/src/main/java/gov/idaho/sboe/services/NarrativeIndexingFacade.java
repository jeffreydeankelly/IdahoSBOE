package gov.idaho.sboe.services;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.eclipse.persistence.exceptions.DatabaseException;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.GlossaryHistory;
import gov.idaho.sboe.jpa.beans.SearchResult;
import gov.idaho.sboe.utils.Messages;


/**
 * <p>This facade handles parsing a {@link Glossary}'s 
 * <code>narrative</code> into words for ad-hoc searches.
 * The tokenized narrative is filtered removing Oracle stop words.
 * <br/><br/>
 * On a search, the text is tokenize into words, the 
 * {@link GlossaryNoiseWord}'s removed and the {@link GlossaryAliasWord}s
 * are added to the seach criteria.
 * <br/><br/>
 * There are two entites that contain a cross reference to the 
 * {@link Glossary}.  The {@link GlossaryNarrativeIndex} contains
 * a direct link.  The {@link GlossaryHistoryNarrativeIndex} provides an
 * indirect link thru the {@link GlossaryHistory}.
 * </p>
 */
public class NarrativeIndexingFacade extends AbstractFacade {
    public static enum SearchMode {
        ALLTERMS_AND,
        ALLTERMS_OR
    }

    /**
     * Java logger utility.</p>
     */
    private static Logger log = Logger.getLogger(NarrativeIndexingFacade.class.getName());
    
    /**
     * <p>Message resources for this class.</p>
     */
    private static Messages messages = new Messages("gov.idaho.sboe.services.Bundle", NarrativeIndexingFacade.class.getClassLoader());

    @SuppressWarnings("unchecked")
    Set<String> tokenizeNarrative(String narrative) throws FacadeException {
        Set<String> results = null;
        EntityManager em = null;
        try {
            em = getPersistenceUnit().createEntityManager();
            results = tokenizeNarrative(em, narrative);
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "tokenizeNarrative.exception", new Object[] { narrative,
                            e.toString() }));
        } finally {
            if (em != null) {
                em.close();
            }
        }

        return results;
    }
    /**
     * <p>Tokenizes the String <code>narrative</code> and verifies that
     * each token is not a {@link GlossaryNoiseWord}.  Next, each
     * token is expanded by any matching {@link GlossaryAliasWord}'s.
     * A token must be at least two chars in length.
     * </p>
     * @param narrative {@link Glossary} or {@link GlossaryHistory}'s
     * narrative. 
     * @return words in the <code>narrative</code> excluding the noise words
     */
    @SuppressWarnings("unchecked")
    Set<String> tokenizeNarrative(EntityManager em, String narrative) {
        Set<String> paredDownSet = new TreeSet<String>();
        
        StringBuffer buff = new StringBuffer(narrative);
        for (int i = buff.length() - 1; i > -1; i--) {
            char c = buff.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
               if (i < buff.length() - 1) {
                   
                   String word = buff.substring(i + 1);
                   if (word.length() >= 2
                       && !paredDownSet.contains(word)
                       && !isOracleStopWord(em, word)) {
                        paredDownSet.add(word);
                   }
                   
                   buff.delete(i, buff.length());
               } else {
                  buff.deleteCharAt(i);
               }
            } else {
                buff.setCharAt(i, Character.toLowerCase(c));
            }
        }
        
        // check if a word is left in the buffer
        if (buff.length() > 0) {
            String word = buff.toString();
            if (word.length() >= 2
                && !paredDownSet.contains(word)
                && !isOracleStopWord(em, word)) {
                    paredDownSet.add(word);
            }
        }
            
        return paredDownSet;
    }

    /**
     * Provide interface without EntityManager for testing
     * @param word
     * @return
     * @throws FacadeException
     */
    boolean isOracleStopWord(String word) throws FacadeException {
        boolean results = false;
        EntityManager em = null;
        try {
            em = getPersistenceUnit().createEntityManager();
            results = isOracleStopWord(em, word);
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "isOracleStopWord.exception", new Object[] { word,
                            e.toString() }));
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return results;
    }
   
    /**
     * Run a query to determine if a token is an Oracle stop word.  If so,
     * it should be removed from the search since stop words do not appear
     * it causes no results from the entire search.
     * @param word
     * @returns true if word is an Oracle stop word.
     */
    boolean isOracleStopWord(EntityManager em, String word) {
//        List<Vector> results = em.createNativeQuery("select SPW_WORD " + 
//            "from ctxsys.CTX_STOPWORDS where SPW_WORD='"+word+"'").
//                getResultList();
//        return results.size()>0;
        List<Vector> results = em.createNativeQuery("select STOPWORD " + 
                "from sys.FULLTEXT_STOPWORDS where STOPWORD='"+word+"'").
                    getResultList();
            return results.size()>0;
    }
     
    /**
     * <p>Performs a wild-card search against {@link GlossaryNarrativeIndex} 
     * and {@link GlossaryHistoryNarrativeIndex} returning all {@link gov.idaho.sboe.jpa.beans.Glossary.PK}s
     * that contains words matching the <code>sarg</code>.
     * <br/>
     * <br/>
     * Processing Steps:<br/>
     * <ol>
     *    <li>Tokenize the <code>sarg</code> into words by calling the <code>tokenizeNarrative</code>
     *    method.  This call will remove {@link GlossaryNoiseWord}s and insert {@link GlossaryAliasWord}s.</li>
     *    <li>For each token resulting from the <code>tokenizeNarrative</code> call, invoke a search on
     *    the {@link GlossaryNarrativeIndex} and {@link GlossaryHistoryNarrativeIndex}.  This will simulate
     *    an inclusive "or" of each word.</li>
     *    <li>The search results are pushed into a single <code>Set</code> collection eliminating duplicates.</li> 
     * </ol>
     * </p> 
     * @param em persistence context
     * @param sarg search arguments
     * @return all matching glossary items
     * @throws FacadeException fatal JPA error
     */
    public List<Glossary.PK> searchGlossaryWildcard(EntityManager em,
            String sarg, String glossType) throws FacadeException {
        return searchGlossaryOracleText(em, sarg, glossType);
    }

    /**
     * Use the Oracle Text search function
     * 
     * "SELECT score(1)+score(2) as totalscore, GlossType,ItemName, ItemNarrative " +
     * @param em
     * @param sarg
     * @return
     * @throws FacadeException
     */
    @SuppressWarnings("unchecked")
    List<Glossary.PK> searchGlossaryOracleText(EntityManager em,
            String sarg, String glossType) throws FacadeException {
        List<Glossary.PK> list = new ArrayList<Glossary.PK>();
        try {
            StringBuffer querySql = new StringBuffer("SELECT GlossType,ItemName " +
                "FROM Glossary WHERE ");
            if (glossType != null) {
                querySql.append("GlossType = '").append(glossType).append("' AND (");
            }
            querySql.append(buildWhereClause(em, sarg, null, null));
            if (glossType != null) {
                querySql.append(")");
            }
            System.out.println("Query: "+querySql.toString());
            List<Vector> results = em.createNativeQuery(querySql.toString()).
//                    " CONTAINS(ItemNarrative, '$student NEAR $homeless', 1) > 0 OR " + 
//                        "CONTAINS(ItemNarrative, 'syn(student) NEAR $homeless', 2) > 0 " + 
                 getResultList();
            // BigDecimal, String, String, String
            for (Vector v: results) {
                list.add(new Glossary.PK((String)v.get(0), (String)v.get(1)));
            }
        } catch (DatabaseException sqle) {
            // catch SQL Exception separately - may mean badly formed query
            // as in the search was "not and syn()"!
             log.severe("Database exception - bad search terms?: "+sarg);
            throw new FacadeException("We're sorry - the system can't understand your search terms.  Please try again.", sqle, true);
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "searchGlossaryWildcard.exception", new Object[] { sarg,
                            e.toString() }));
        }
        log.info(messages.getMessage("searchGlossaryWildcard.matching",
                new Object[] { list.size(), sarg }));
        return list;
    }

    void concatSingleTokenPhrase(StringBuffer querySql, StringBuffer selectSql, String field, String token, int base) {
        if (selectSql != null) {
            if (selectSql.length()>0) selectSql.append("+");
            selectSql.append(String.format("score(%1$d)+score(%2$d)", base, base+1));
        }
        if (querySql.length()>0) querySql.append(" OR");
        querySql.append(
            String.format(" CONTAINS(%1$s, '$%2$s', %3$d) > 0 OR CONTAINS(%1$s, '{syn(%2$s)}', %4$d) > 0",
                    field, token, base, base+1));
    }


    void concatDoubleTokenPhrase(StringBuffer querySql, StringBuffer selectSql, String field, 
        String tokenA, String tokenB, int base) {
        if (selectSql != null) {
            if (selectSql.length()>0) selectSql.append("+");
            selectSql.append(String.format("score(%d)+score(%d)+score(%d)+score(%d)", base, base+1, base+2, base+3));
        }
        if (querySql.length()>0) querySql.append(" OR");
        querySql.append(
            String.format(" CONTAINS(%1$s, '$%2$s NEAR $%3$s', %4$d) > 0 OR " +
                          " CONTAINS(%1$s, '$%2$s NEAR {syn(%3$s)}', %5$d) > 0 OR"+
                          " CONTAINS(%1$s, 'syn(%2$s) NEAR $%3$s', %6$d) > 0 OR"+
                          " CONTAINS(%1$s, 'syn(%2$s) NEAR {syn(%3$s)}', %7$d) > 0 ",
                    field, tokenA, tokenB, base, base+1, base+2, base+3));
    }

    /*
     * WHERE CONTAINS(ItemNarrative, '$student OR syn(student)',1) >0 AND 
     * CONTAINS(ItemNarrative, '$homeless OR syn(homeless)', 2) > 0
     */

    int concatFromIterator(StringBuffer querySql, StringBuffer selectSql, 
            String field, Set<String> tokens, int start) {
        int label = start;
        final Iterator<String> iterator = tokens.iterator();
        while (iterator.hasNext()) {
            final String tok1 = iterator.next().trim();
            if (label > start) {
                querySql.append(" AND");
                if (selectSql != null) selectSql.append("+");
            }
            if (selectSql != null) selectSql.append("score(").append(label).append(")");
            querySql.append(
                String.format(" CONTAINS(%1$s, '$%2$s OR {syn(%2$s)}', %3$d) > 0",
                        field, tok1, label));
            label++;
        }
        return label;
    }

    String buildWhereClause(String sarg, StringBuffer selectSql, StringBuffer orderBySql) throws FacadeException {
        String results = null;
        EntityManager em = null;
        try {
            em = getPersistenceUnit().createEntityManager();
            results = buildWhereClause(em, sarg, selectSql, orderBySql);
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "buildWhereClause.exception", new Object[] { sarg,
                            e.toString() }));
        } finally {
            if (em != null) {
                em.close();
            }
        }

        return results;
    }

    String buildWhereClause(EntityManager em, String sarg, StringBuffer selectSql, StringBuffer orderBySql) {
        StringBuffer querySql = new StringBuffer();
        Set<String> tokens = tokenizeNarrative(em, sarg);
        if (tokens.size()==1) {
            final String tok = tokens.iterator().next();
            concatSingleTokenPhrase(querySql, selectSql, "ItemNarrative", tok, 1);
            concatSingleTokenPhrase(querySql, selectSql, "ItemName", tok, 3);
            // The question is whether relevance is relevant on a one word search.
            // Perhaps the answer is yes because if the terms appears lots of
            // times in the narrative it scores higher than say where it appears
            // only once as a stem.
            // e.g. "student", the Business Rule where the only match was "students"
            // scored only 10, where another Business Rule where 'student' appeared
            // many times scored 33!
            // But since I added this, I will leave in for future reference.
//            if (orderBySql != null) orderBySql.append("GlossType, ItemName");
            if (orderBySql != null) orderBySql.append("totalscore DESC");
        } else
        /*
         * Since we're using synonyms and stems, need all combinations
         * WHERE CONTAINS(ItemNarrative, '$student NEAR syn(homeless)', 1) > 0 OR
        CONTAINS(ItemNarrative, '$student NEAR $homeless', 2) > 0 OR
        CONTAINS(ItemNarrative, 'syn(student) NEAR syn(homeless)', 3) > 0 OR
        CONTAINS(ItemNarrative, 'syn(student) NEAR $homeless', 4) > 0
         */
        if (tokens.size()==2) {
            final Iterator<String> iterator = tokens.iterator();
            final String tok1 = iterator.next().trim();
            final String tok2 = iterator.next().trim();
            concatDoubleTokenPhrase(querySql, selectSql, "ItemNarrative", tok1, tok2, 1);
            concatDoubleTokenPhrase(querySql, selectSql, "ItemName", tok1, tok2, 5);
            if (orderBySql != null) orderBySql.append("totalscore DESC");
        } else if (tokens.size()> 2) {
            querySql.append("(");
            int count = concatFromIterator(querySql, selectSql, "ItemNarrative", tokens, 1);
            if (selectSql != null) selectSql.append("+");
            querySql.append(") OR (");
            concatFromIterator(querySql, selectSql, "ItemName", tokens, count);
            querySql.append(")");
            if (orderBySql != null) orderBySql.append("totalscore DESC");
        }
        return querySql.toString();
    }

    public List<SearchResult> searchGlossaryKeywords(String sarg)
            throws FacadeException {
        List<SearchResult> results = null;
        EntityManager em = null;
        try {
            em = getPersistenceUnit().createEntityManager();
            results = searchGlossaryKeywords(em, sarg);
        } catch (FacadeException e) {
            log.severe(e.toString());
            throw e;
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "searchGlossaryWildcard.exception", new Object[] { sarg,
                            e.toString() }));
        } finally {
            if (em != null) {
                em.close();
            }
        }

        return results;

    }

    /**
     * Use the Oracle Text search function
     * 
     * "SELECT score(1)+score(2) as totalscore, GlossType,ItemName, ItemNarrative " +
     * @param em
     * @param sarg
     * @return
     * @throws FacadeException
     */
    List<SearchResult> searchGlossaryKeywords(EntityManager em,
            String sarg) throws FacadeException {
        List<SearchResult> list = new ArrayList<SearchResult>();
        try {
            StringBuffer selectSql = new StringBuffer("SELECT ");
            StringBuffer querySql = new StringBuffer(
                "FROM Glossary WHERE ");
            StringBuffer orderBySql = new StringBuffer(" ORDER BY ");
            System.out.println("Select: "+selectSql.toString());
            System.out.println("Query: "+querySql.toString());
            querySql.append(buildWhereClause(em, sarg, selectSql, orderBySql));
            selectSql.append(" as totalscore, GlossType,ItemName, ItemNarrative ");
            selectSql.append(querySql).append(orderBySql);
            log.info("ksearch: " + selectSql.toString());
            List<Vector> results = em.createNativeQuery(selectSql.toString()).
                 getResultList();
            // BigDecimal, String, String, String
            for (Vector v: results) {
                list.add(new SearchResult((BigDecimal)v.get(0), 
                    (String)v.get(1), (String)v.get(2), (String)v.get(3)));
            }
        } catch (DatabaseException sqle) {
            // catch SQL Exception separately - may mean badly formed query
            // as in the search was "not and syn()"!
             log.severe("Database exception - bad search terms?: "+sarg);
            throw new FacadeException("We're sorry - the system can't understand your search terms.  Please try again.", sqle, true);
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "searchGlossaryWildcard.exception", new Object[] { sarg,
                            e.toString() }));
        }
        log.info(messages.getMessage("searchGlossaryWildcard.matching",
                new Object[] { list.size(), sarg }));
        return list;
    }

    /**
     * <p>Performs a wild-card search against {@link GlossaryNarrativeIndex} 
     * and {@link GlossaryHistoryNarrativeIndex} returning all {@link gov.idaho.sboe.jpa.beans.Glossary.PK}s
     * that contains words matching the <code>sarg</code>.
     * <br/>
     * <br/>
     * Processing Steps:<br/>
     * <ol>
     *    <li>Tokenize the <code>sarg</code> into words by calling the <code>tokenizeNarrative</code>
     *    method.  This call will remove {@link GlossaryNoiseWord}s and insert {@link GlossaryAliasWord}s.</li>
     *    <li>For each token resulting from the <code>tokenizeNarrative</code> call, invoke a search on
     *    the {@link GlossaryNarrativeIndex} and {@link GlossaryHistoryNarrativeIndex}.  This will simulate
     *    an inclusive "or" of each word.</li>
     *    <li>The search results are pushed into a single <code>Set</code> collection eliminating duplicates.</li> 
     * </ol>
     * </p>
     * @param sarg search arguments
     * @return all matching glossary items
     * @throws FacadeException fatal JPA error
     */
    public List<Glossary.PK> searchGlossaryWildcard(String sarg, String glossType)
            throws FacadeException {
        List<Glossary.PK> results = null;
        EntityManager em = null;
        try {
            em = getPersistenceUnit().createEntityManager();
            results = searchGlossaryWildcard(em, sarg, glossType);
        } catch (FacadeException e) {
            log.severe(e.toString());
            throw e;
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "searchGlossaryWildcard.exception", new Object[] { sarg,
                            e.toString() }));
        } finally {
            if (em != null) {
                em.close();
            }
        }

        return results;

    }

    /**
     * <p>This search will be used when changing tabs.  It will filter
     * on the {@link Glossary}'s <code>itemName</code> where it's
     * between the <code>startRange</code> and the <code>endRange</code>.
     * <br/>
     * <br/>
     * Processing Steps:<br/>
     * <ol>
     *    <li>Performes a search on the character prefix of property 
     *    <code>itemName</code> for entity bean {@link Glossary}.
     *    The first character of the <code>itemName</code> must be within
     *    the <code>startRange</code> and <code>endRange</code> parameters.</li>
     *    <li>The results are added to a <code>Set</code> to eliminate duplicates.</li>
     *    <li>To make the search case insensitive, the search is performed twice.
     *    The first pass-through uses lower case applied to the actual parameters
     *    <code>startRange</code> and <code>endRange</code>. The second pass uses 
     *    upper case applied to these <code>char</code> parameters.</li>
     * </ol>
     * </p>
     * @param em persistence context
     * @param startRange starting range
     * @param endRange ending range
     * @return Glossary keys within the <code>startRange</code> and <code>endRange</code>
     * @throws FacadeException fatal error performing search
     */
    @SuppressWarnings("unchecked")
    public List<Glossary.PK> searchGlossaryRolodex(EntityManager em,
            char startRange, char endRange) throws FacadeException {
        Set<Glossary.PK> results = new TreeSet<Glossary.PK>();
        try {
            for (char i = Character.toLowerCase(startRange); i <= Character.toLowerCase(endRange); i++) {

               StringBuffer startsWith = new StringBuffer();
               startsWith.append(i).append("%");
               
               results.addAll(em.createNamedQuery("searchGlossaryRolodex")
                         .setParameter("startsWith", startsWith.toString())
                         .getResultList());
            }
            for (char i = Character.toUpperCase(startRange); i <= Character.toUpperCase(endRange); i++) {

                StringBuffer startsWith = new StringBuffer();
                startsWith.append(i).append("%");
                
                results.addAll(em.createNamedQuery("searchGlossaryRolodex")
                          .setParameter("startsWith", startsWith.toString())
                          .getResultList());
             }
            
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "searchGlossaryRolodex.exception", new Object[] {
                            startRange, endRange, e.toString() }));
        }

        List<Glossary.PK> list = new ArrayList<Glossary.PK>(results);
        return list;
    }

    
    /**
     * <p>This search will be used when changing tabs.  It will filter
     * on the {@link Glossary}'s <code>itemName</code> where it's
     * between the <code>startRange</code> and the <code>endRange</code>.
     * <br/>
     * <br/>
     * Processing Steps:<br/>
     * <ol>
     *    <li>Performes a search on the character prefix of property 
     *    <code>itemName</code> for entity bean {@link Glossary}.
     *    The first character of the <code>itemName</code> must be within
     *    the <code>startRange</code> and <code>endRange</code> parameters.</li>
     *    <li>The results are added to a <code>Set</code> to eliminate duplicates.</li>
     *    <li>To make the search case insensitive, the search is performed twice.
     *    The first pass-through uses lower case applied to the actual parameters
     *    <code>startRange</code> and <code>endRange</code>. The second pass uses 
     *    upper case applied to these <code>char</code> parameters.</li>
     * </ol>
     * </p>
     * @param startRange starting range
     * @param endRange ending range
     * @return Glossary keys within the <code>startRange</code> and <code>endRange</code>
     * @throws FacadeException fatal error performing search
     */
    public List<Glossary.PK> searchGlossaryRolodex(char startRange, char endRange) throws FacadeException {
        List<Glossary.PK> results = new ArrayList<Glossary.PK>();
        EntityManager em = null;
        try {
            em = getPersistenceUnit().createEntityManager();
            
            results = searchGlossaryRolodex(em, startRange, endRange);
            
        } catch (FacadeException e) {
            log.severe(e.toString());
            throw e;
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "searchGlossaryRolodex.exception", new Object[] {
                            startRange, endRange, e.toString() }));
        }

        return results;
    }


    /**
     * <p>This search will return all {@link Glossary} entities that
     * have a <code>glossType</code> of 'Collection'.  
     * </p>
     * @return Glossary keys for all collections
     * @throws FacadeException fatal error performing search
     */
    public List<Glossary.PK> searchGlossaryCollections() throws FacadeException {
        return searchGlossary("Collection");
    }
     
    @SuppressWarnings("unchecked")
    public List<Glossary.PK> searchGlossary(String glossType) throws FacadeException {
        List<Glossary.PK> results = new ArrayList<Glossary.PK>();
        EntityManager em = null;
        try {
            em = getPersistenceUnit().createEntityManager();
            
            results = em.createNamedQuery("searchGlossaryAll")
                .setParameter("type", glossType)
                .getResultList();
            
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "searchGlossary.exception",
                     new Object[] {e.toString()}));
        }

        return results;
    }
    
}
