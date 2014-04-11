package gov.idaho.sboe.services;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.GlossaryFeedback;
import gov.idaho.sboe.jpa.beans.GlossaryHistory;
import gov.idaho.sboe.jpa.beans.GlossaryType;
import gov.idaho.sboe.jpa.beans.GlossaryUsage;
import gov.idaho.sboe.jpa.beans.GlossaryXRef;
import gov.idaho.sboe.jpa.beans.GlossaryXwalk;
import gov.idaho.sboe.jpa.beans.GlossaryXwalkHistory;
import gov.idaho.sboe.jpa.beans.StatisticFeedbackUsage;
import gov.idaho.sboe.jpa.beans.StatisticSessionLength;
import gov.idaho.sboe.jpa.beans.StatisticSessionPerDay;
import gov.idaho.sboe.jpa.beans.DictUser;
import gov.idaho.sboe.utils.Messages;


public class CatalogFacade extends AbstractFacade {

    private static Logger log = Logger.getLogger(CatalogFacade.class.getName());
    
    /**
     * <p>Message resources for this class.</p>
     */
    private static Messages messages = new Messages("gov.idaho.sboe.services.Bundle", CatalogFacade.class.getClassLoader());

    /**
     * <p>
     * Internal enumeration that indicates the type of operation to audit in
     * history.
     * </p>
     */
    private enum AuditType {
        CREATE, UPDATE, DELETE;
    }

    /**
     * <p>This dependent facade handles operations associated
     * with indexing narratives in the {@link Glossary} and 
     * {@link GlossaryHistory}.
     * </p>
     */
    private NarrativeIndexingFacade narrativeIndexingFacade = null;
    
    public CatalogFacade() {
        narrativeIndexingFacade = new NarrativeIndexingFacade();
    }

    public NarrativeIndexingFacade getNarrativeIndexingFacade() {
        return narrativeIndexingFacade;
    }

    /**
     * <p>Sets aggregated <code>narrativeIndexingFacade</code>'s 
     * persistence unit so that both facades share a common
     * unit.</p>
     * 
     * @param persistenceUnit JPA persistence unit
     */
    @Override
    public void setPersistenceUnit(EntityManagerFactory persistenceUnit) {
        super.setPersistenceUnit(persistenceUnit);
        narrativeIndexingFacade.setPersistenceUnit(persistenceUnit);
    }

    /**
     * <p>
     * Creates a {@link GlossaryXwalkHistory} that audits a relationship change
     * between {@link Glossary} items recorded as a {@link GlossaryXwalk}.
     * </p>
     * @param em persistence context
     * @param auditType
     *            enumerations are CREATE or DELETE
     * @param userId
     *            authenticated user's id
     * @param glossaryXwalk
     *            a parent child relationship between to {@link Glossary} items
     * @return an object that audits activity on a {@link GlossaryXwalk}
     * @exception FacadeException error finding the DB's CURRENT_DATE
     */
    private GlossaryXwalkHistory createGlossaryXwalkHistory(EntityManager em,
            AuditType auditType, String userId, GlossaryXwalk glossaryXwalk) throws FacadeException{
        log.entering(CatalogFacade.class.getName(),
                "createGlossaryXwalkHistory");

        GlossaryXwalkHistory history = new GlossaryXwalkHistory();
        history.setItemNameChild(glossaryXwalk.getPk().getItemNameChild());
        history.setItemNameParent(glossaryXwalk.getPk().getItemNameParent());
        history.setGlossTypeChild(glossaryXwalk.getPk().getGlossTypeChild());
        history.setGlossTypeParent(glossaryXwalk.getPk().getGlossTypeParent());

        if (auditType.equals(AuditType.CREATE)) {
            history.setCreateUserid(userId);
            history.setCreateDate(getSystemDate(em));
            glossaryXwalk.setCreateUserid(history.getCreateUserid());
            glossaryXwalk.setCreateDate(history.getCreateDate());
        } else if (auditType.equals(AuditType.DELETE)) {
            history.setDeleteUserid(userId);
            history.setDeleteDate(getSystemDate(em));
            history.setCreateUserid(glossaryXwalk.getCreateUserid());
            history.setCreateDate(glossaryXwalk.getCreateDate());
        }

        log.exiting(CatalogFacade.class.getName(),
                        "createGlossaryXwalkHistory");

        return history;
    }

    /**
     * <p>
     * Instantiates a {@link GlossaryHistory} object the reflects the
     * <code>auditType</code>.
     * </p>
     * @param em persistence context
     * @param auditType
     *            enumerations are CREATE, UPDATE or DELETE
     * @param userId
     *            authenticated user's Id
     * @param glossary
     *            dictionary element being acted on
     * @return an object that audits the activity on a {@link Glossary}
     * @exception FacadeException error finding DB's CURRENT_DATE
     */
    private GlossaryHistory createGlossaryHistory(EntityManager em, AuditType auditType,
            String userId, Glossary glossary) throws FacadeException {

        log.entering(CatalogFacade.class.getName(), "createGlossaryHistory");

        GlossaryHistory history = new GlossaryHistory();

        // common elements pulled regardless of the audit type
        history.setGlossType(glossary.getPk().getGlossType());
        history.setItemName(glossary.getPk().getItemName());
        history.setItemNarrative(glossary.getItemNarrative());
        history.setEffDateStart(glossary.getEffDateStart());
        history.setEffDateEnd(glossary.getEffDateEnd());

        if (auditType.equals(AuditType.CREATE)) {
            history.setCreateUserid(userId);
            history.setCreateDate(getSystemDate(em));

            glossary.setCreateDate(history.getCreateDate());
            glossary.setCreateUserid(history.getCreateUserid());
            if (glossary.getEffDateStart() == null) {
                glossary.setEffDateStart(history.getCreateDate());
                history.setEffDateStart(history.getCreateDate());
            }
            log.finest("Audit Create Glossary");

        } else if (auditType.equals(AuditType.UPDATE)) {

            history.setCreateUserid(glossary.getCreateUserid());
            history.setCreateDate(glossary.getCreateDate());
            history.setUpdateUserid(userId);
            history.setUpdateDate(getSystemDate(em));

            glossary.setUpdateDate(history.getUpdateDate());
            glossary.setUpdateUserid(history.getUpdateUserid());

            log.finest("Audit UPDATE Glossary");

        } else if (auditType.equals(AuditType.DELETE)) {

            history.setCreateUserid(glossary.getCreateUserid());
            history.setCreateDate(glossary.getCreateDate());
            history.setUpdateUserid(glossary.getUpdateUserid());
            history.setUpdateDate(glossary.getUpdateDate());
            history.setDeleteUserid(userId);
            history.setDeleteDate(getSystemDate(em));

            log.finest("Audit DELETE Glossary");
        }

        log.exiting(CatalogFacade.class.getName(), "createGlossaryHistory");

        return history;
    }

    /**
     * <p>
     * Looks to see if the {@link Glossary} item already exists. If it exists,
     * an update is performed; otherwise, a new row is created. The
     * {@link GlossaryHistory} is updated to reflect the database operation.
     * A optional list of <code>parentKeys</code> is passed as the last 
     * formal parameter.  If the {@link GlossaryXwalk} relationship doesn't exist,
     * one is created and audited to {@link GlossaryXwalkHistory}.<br/>
     * <br/>
     * <br/>
     * Processing steps:<br/>
     * <ol>
     *    <li>Look for an existing {@link Glossary}.</li>
     *    <li>Based on if the target {@link Glossary} already exists, 
     *    use <code>AuditType.UPDATE</code> or <code>AuditType.CREATE</code></li>
     *    <li>Create a {@link GlossaryHistory} to reflect the update or delete.</li>
     *    <li>If the audit type is "UPDATE", merge the {@link Glossary}, or if it's
     *    a new item, persist the {@link Glossary} item.</li>
     *    <li>Index or Reindex the {@link Glossary}'s narrative by updating 
     *    the {@link GlossaryNarrativeIndex}.  This is handled through the
     *    {@link NarrativeIndexFacade}.</li>
     *    <li>Persist the {@link GlossaryHistory}.</li>
     *    <li>If the Glossary already exists and an update was performed, index
     *    the {@link GlossaryHistory} with the {@link GlossaryHistoryNarrativeIndex}.
     *    This operation is handled through the {@link NarrativeIndexFacade}.</li>
     *    <li>Check to see if the option <code>parentKeys</code> is not null.  If
     *    it is not, we need to add {@link GlosaryXwalk} relationships for each
     *    {@link Glossary.PK} in the optional array. If the relationship doesn't 
     *    exist and the relationships is not circular, 
     *    a {@link GlossaryXwalkHistory} will be created to audit the operation.</li>
     * </ol>
     * 
     * </p>
     *
     * @param em JPA persistence context
     * @param userId
     *            authenticated user performing the operation
     * @param glossary
     *            dictionary item to be updated or create
     * @param parentsKeys an optional list of parents to associate the <code>glossary</code> to
     * @throws FacadeException
     *             error performing an update or create
     */
    public void persistGlossary(EntityManager em, String userId,
            Glossary glossary, Glossary.PK... parentsKeys)
            throws FacadeException {

        log.entering(CatalogFacade.class.getName(), "persistGlossary");

        try {
            Glossary existsGlossary = em.find(Glossary.class, glossary.getPk());

            AuditType auditType = AuditType.CREATE;
            if (existsGlossary != null) {
                auditType = AuditType.UPDATE;
                
                // this stuff is required but if it's a duplicate and we want
                // to update the narrative, just bring it forward from the 
                // previous state
                glossary.setCreateDate(existsGlossary.getCreateDate());
                glossary.setEffDateStart(existsGlossary.getEffDateStart());
                glossary.setCreateUserid(existsGlossary.getCreateUserid());
                
                /* check to see if there was an existing history
                 * if not create a new entry to match the exist glossary
                 * one so we don't loss history
                 */
                 List<GlossaryHistory> historyList = findHistoryForGlossary(em, 
                    existsGlossary.getPk().getGlossType(), existsGlossary.getPk().getItemName());
                 if (historyList == null || historyList.isEmpty()) 
                 {
                      GlossaryHistory missingHistory = createGlossaryHistory(em, auditType, userId,
                                         existsGlossary);
                     validate(missingHistory);
                     em.persist(missingHistory);
                 }
            }

            GlossaryHistory history = createGlossaryHistory(em, auditType, userId,
                    glossary);
            validate(glossary);                

            // if we are creating a new or updating and something has changed
            if (auditType.equals(AuditType.CREATE) 
                    || (auditType.equals(AuditType.UPDATE) && !existsGlossary.equals(glossary))) {
                
                
                if (auditType.equals(AuditType.UPDATE)) {                 
                    glossary = em.merge(glossary);
                } else {
                    em.persist(glossary);
                }

                //Reindex the narrative
                long now = new java.util.Date().getTime();
//                narrativeIndexingFacade.reindexGlossaryNarrative(em, userId, glossary.getPk().getGlossType(), 
//                        glossary.getPk().getItemName(), glossary.getItemNarrative());
//                log.info("Tokenized "+glossary.getItemNarrative().length()+" chars in "+((new Date().getTime())-now)+" ms.");
                validate(history);
                em.persist(history);

//                if (auditType.equals(AuditType.UPDATE)) {
//                    narrativeIndexingFacade.reindexGlossaryHistoryNarrative(em, userId, history.getHistId(), history.getItemNarrative());  
//                }

            } else {
                log.info(messages.getMessage("persistGlossary.notdirty", new Object[] {glossary}));
            }
            // parent keys are optional
            if (parentsKeys != null) {
                for (Glossary.PK parentKey : parentsKeys) {
                    GlossaryXwalk.PK relationshipPk = new GlossaryXwalk.PK(
                        parentKey.getGlossType(), parentKey.getItemName(),
                        glossary.getPk().getGlossType(), glossary.getPk().getItemName());

                    // look for existing relationship
                    GlossaryXwalk relationship = em.find(GlossaryXwalk.class,
                            relationshipPk);
                    if (relationship == null 
                            && !checkCircularAssociation(em, relationshipPk)) {
                        // one doesn't exist, create a new one
                                                
                        relationship = new GlossaryXwalk();
                        relationship.setPk(relationshipPk);
                        // create history for the relationship
                        GlossaryXwalkHistory relationshipHistory = createGlossaryXwalkHistory(em,
                                AuditType.CREATE, userId, relationship);

                        validate(relationship);
                        em.persist(relationship);
                        
                        validate(relationshipHistory);
                        em.persist(relationshipHistory);
                    } else {
                        log.info(messages.getMessage(
                                "persistGlossary.parentRelationshipExists",
                                new Object[] { parentKey, glossary.getPk() }));
                    }
                }
            }

        } catch (Exception e) {

            log.severe(e.toString());

            StringBuffer relMsg = new StringBuffer();
            if (parentsKeys != null) {
                for (Glossary.PK parent : parentsKeys) {
                    relMsg.append((relMsg.length() > 1 ? ", " : "")).append(
                            "\"").append(parent).append("\"");
                }
            }

            throw new FacadeException(messages.getMessage(
                    "persistGlossary.exception", new Object[] {
                            glossary.getPk(), relMsg.toString(), e.toString() }), e);

        }

        log.exiting(CatalogFacade.class.getName(), "persistGlossary");

    }

    /**
     * <p>
     * Looks to see if the {@link Glossary} item already exists. If it exists,
     * an update is performed. Otherwise, a new row is created. The
     * {@link GlossaryHistory} is updated to reflect the database operation.
     * A optional list of <code>parentKeys</code> is passed as the last 
     * formal parameter.  If the {@link GlossaryXwalk} relationship doesn't exist,
     * one is created and audited to {@link GlossaryXwalkHistory}.<br/>
     * <br/>
     * <br/>
     * Processing steps:<br/>
     * <ol>
     *    <li>Create a JPA persistence context and start the unit-of-work.</li>
     *    <li>Invoke the overloaded persistGlossary method with the <code>EntityManager</code>
     *    as the first parameter.</li>
     *    <li>Commit the unit-of-work and close the persistence context.</li> 
     * </ol>
     * <br/>
     * <br/>
     * The unit-of-work is atomic for this method call.
     * </p>
     *
     * @param userId
     *            authenticated user performing the operation
     * @param glossary
     *            dictionary item to be updated or create
     * @param parentsKeys an optional list of parents to associate the <code>glossary</code> to
     * @throws FacadeException
     *             error performing an update or create
     */
    public void persistGlossary(String userId, Glossary glossary,
            Glossary.PK... parentsKeys) throws FacadeException {

        log.entering(CatalogFacade.class.getName(), "persistGlossary");

        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            persistGlossary(em, userId, glossary, parentsKeys);
            em.getTransaction().commit();

        } catch (FacadeException e) {
            // thrown from overloaded call
            log.severe(e.toString());

            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } catch (Exception e) {
            log.severe(e.toString());

            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            StringBuffer relMsg = new StringBuffer();
            if (parentsKeys != null) {
                for (Glossary.PK parent : parentsKeys) {
                    relMsg.append((relMsg.length() > 1 ? ", " : "")).append(
                            "\"").append(parent).append("\"");
                }
            }

            throw new FacadeException(messages.getMessage(
                    "persistGlossary.exception", new Object[] {
                            glossary.getPk(), relMsg.toString(), e.toString()}), e);

        } finally {
            if (em != null) {
                em.close();
            }
        }
        
        log.exiting(CatalogFacade.class.getName(), "persistGlossary");
    }

    
    /**
     * <p>
     * Tries to find a {@link Glossary} by <code>glossType</code> and
     * <code>itemName</code>. If an item doesn't extist, a null value is
     * returned.
     * </p>
     * 
     * @param em
     *            JPA persistence context
     * @param glossType
     *            glossary item type
     * @param itemName
     *            name of the glossary item
     * @return existing {@link Glossary} or null if not found
     * @exception FacadeException runtime exception finding glossary 
     */
    public Glossary findGlossary(EntityManager em, String glossType,
            String itemName) throws FacadeException {

        log.entering(CatalogFacade.class.getName(), "findGlossary");

        Glossary target = null;
        Glossary.PK pk = new Glossary.PK(glossType, itemName);

        try {
            target = em.find(Glossary.class, pk);

            if (target == null) {
                log.fine(messages.getMessage("findGlossary.notfound",
                        new Object[] { pk }));
            }

        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "findGlossary.exception",
                    new Object[] { pk, e.toString() }), e);
        }

        log.exiting(CatalogFacade.class.getName(), "findGlossary");

        return target;
    }


    /**
     * <p>
     * Tries to find a {@link Glossary} by <code>glossType</code> and
     * <code>itemName</code>. If an item doesn't extist, a null value is
     * returned.
     * </p>
     * @param glossType
     *            glossary item type
     * @param itemName
     *            name of the glossary item
     * @return existing {@link Glossary} or null if not found
     * @exception FacadeException runtime exception finding glossary 
     */
    public Glossary findGlossary(String glossType, String itemName)
            throws FacadeException {

        log.entering(CatalogFacade.class.getName(), "findGlossary");

        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;
        Glossary target = null;

        try {
            em = emf.createEntityManager();
            target = findGlossary(em, glossType, itemName);
        } catch (FacadeException e) {
            log.severe(e.toString());
            throw e;
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "findGlossary.exception", new Object[] {
                            new Glossary.PK(glossType, itemName),
                            e.toString() }), e);
        } finally {
            if (em != null) {
                em.close();
            }
        }

        log.exiting(CatalogFacade.class.getName(), "findGlossary");

        return target;
    }

    /**
     * <p>Recursively traverses down the tree starting with a parent finding all 
     * child relationships.<br/>
     * </p>
     * 
     * @param em  JPA persistence context
     * @param glossTypeParent parent of the {@link Glossary} 
     * @param itemNameParent parent name of the {@link Glossary}
     * @return a linear list of all relationships that are under the subtree
     */
    @SuppressWarnings("unchecked")
    private List<GlossaryXwalk> loadSubtreeXwalk(EntityManager em,
            String glossTypeParent, String itemNameParent) {

        List<GlossaryXwalk> children = em.createNamedQuery(
                "findGlossaryXwalkForParent").setParameter("glossType",
                glossTypeParent).setParameter("itemName", itemNameParent)
                .getResultList();

        List<GlossaryXwalk> subChildren = new ArrayList<GlossaryXwalk>();
        for (GlossaryXwalk child : children) {
            subChildren.addAll(loadSubtreeXwalk(em, 
                child.getPk().getGlossTypeChild(),
                child.getPk().getItemNameChild()));
        }

        children.addAll(subChildren);

        return children;
    }

    /**
     * <p>
     * Returns all {@link GlossaryXwalk}.
     * </p>
     * 
     * @return all the {@link GlossaryXwalk} rows
     * @exception FacadeException fatal error finding all glossary types
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<GlossaryXRef>> loadGlossaryXwalk() throws FacadeException {
        log.entering(CatalogFacade.class.getName(), "loadGlossaryXwalk");

        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;

        Map<String, List<GlossaryXRef>> results = null;

        try {
            em = emf.createEntityManager();
            List<GlossaryType> allTypes = findAllGlossaryType(em);
            results = new HashMap<String, List<GlossaryXRef>>();
            Query q = em.createNamedQuery("loadGlossaryXwalkType");
            for (GlossaryType gt:allTypes) {
                q.setParameter("glossType", gt.getGlossType());
                List<GlossaryXRef> gloss4type = q.getResultList();
//                TreeSet<GlossaryXRef> glossSet = new TreeSet<GlossaryXRef>();
//                glossSet.addAll(gloss4type);
                Collections.sort(gloss4type); //, comparator);
                results.put(gt.getGlossType(), /*glossSet*/gloss4type);
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
        log.exiting(CatalogFacade.class.getName(), "loadGlossaryXwalk");
        return results;
    }

    /**
     * <p>
     * Recursively traverses up the tree starting with a child finding all
     * parent relationships.<br/>
     * </p>
     * 
     * @param em
     *            JPA persistence context
     * @param glossType
     *            the type of the target {@link Glossary}
     * @param itemName
     *            the name of the {@link Glossary}
     * @return a linear list of all relationships that are parents to a
     *         {@link Glossary} item
     */
    @SuppressWarnings("unchecked")
    private List<GlossaryXwalk> loadSuperXwalk(EntityManager em,
            String glossType, String itemName) {

        List<GlossaryXwalk> parents = em.createNamedQuery(
                "findGlossaryXwalkForChild").setParameter("glossType",
                glossType).setParameter("itemName", itemName).getResultList();

        List<GlossaryXwalk> superParents = new ArrayList<GlossaryXwalk>();
        for (GlossaryXwalk parent : parents) {
            superParents.addAll(loadSuperXwalk(em,
                parent.getPk().getGlossTypeParent(),
                parent.getPk().getItemNameParent()));
        }

        parents.addAll(superParents);

        return parents;
    }

    
    /**
     * <p>
     * This method will check to make sure that the <code>newRelationship</code>
     * will not introduce a circular association. This scenario would be be when
     * a parent {@link Glossary} becomes a child of one of it's child
     * {@link Glossary}. In this scenario, the logic traversing the
     * {@link GlossaryXwalk} relationships would end up in an infinite loop.
     * </p>
     * 
     * @param em
     *            JPA persistence context
     * @param  newRelationshipKey
     *            a new relationshipKey that has not been persisted
     * @return <code>true</code> if the <code>newRelationship</code> will
     *         cause a circular association; otherwise; <code>false</code> is
     *         returned
     */
    public boolean checkCircularAssociation(EntityManager em,
            GlossaryXwalk.PK newRelationshipKey) {

        List<GlossaryXwalk> associations = loadSuperXwalk(em,
                     newRelationshipKey.getGlossTypeParent(), 
                     newRelationshipKey.getItemNameParent());
        if (associations != null && associations.size() > 0) {
            for (GlossaryXwalk assoc : associations) {
                if (assoc.getPk().getGlossTypeParent().equals(
                        newRelationshipKey.getGlossTypeChild())
                        && assoc.getPk().getItemNameParent().equals(
                                newRelationshipKey.getItemNameChild())) {

                    log.warning(messages.getMessage(
                            "checkCircularAssociation.exists", new Object[] {
                                    new Glossary.PK(
                                        newRelationshipKey.getGlossTypeChild(),
                                        newRelationshipKey.getItemNameChild()),
                                    assoc.getPk() }));
                    return true;
                }
            }
        }

        return false;
    }
 
    /**
     * <p>This method will check to make sure that the <code>newRelationship</code> will not
     * introduce a circular association.  This scenario would be be when a parent {@link Glossary} 
     * becomes a child of one of it's child {@link Glossary}.  In this scenario, the logic traversing
     * the {@link GlossaryXwalk} relationships would end up in an infinite loop.</p>
     *  
     * @param newRelationshipKey a new relationship that has not been persisted 
     * @return <code>true</code> if the <code>newRelationship</code> will cause a circular association; otherwise; <code>false</code> is returned
     */
    public boolean checkCircularAssociation(GlossaryXwalk.PK newRelationshipKey) {
        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;
        boolean hasCircularAssociations = false;
        try {
            em = emf.createEntityManager();
            hasCircularAssociations = checkCircularAssociation(em, newRelationshipKey);
        } finally {
            if (em != null) {
               em.close();
            }
        }
        return hasCircularAssociations;
    }

    
    /**
     * <p>
     * Removes a {@link Glossary} item and audits to the {@link GlossaryHistory}.  It also removes all 
     * relationships {@link GlossaryXwalk} and audits to {@link GlossaryXwalkHistory}.
     * <br/>
     * <br/>
     * Processing Steps:<br/>
     * <ol>
     *    <li>Find the {@link Glossary} by <code>glossType</code> and <code>itemType</code>.
     *    if it doesn't exist, then just exist and log a message.</li>
     *    <li>Find all {@link GlossaryXwalk} relationships where the {@link Glossary} item being
     *    deleted is the child of another {@link Glossary} item.</li>
     *    <li>Find all {@link GlossaryXwalk} relationships under the subtree of the deleted 
     *    {@link Glossary} item.  This will be all child relationships.</li>
     *    <li>Merge the two types of collections of relationships, parent and child.</li>
     *    <li>Create a {@link GlossaryHistory} for the {@link Glossary} item being deleted.</li>
     *    <li>Remove all associated {@link gov.idaho.sboe.jpa.beans.GlossaryNarrativeIndex}'s using the 
     *    {@link NarrativeIndexingFacade}.</p>
     *    <li>Remove the {@link Glossary}.</li>
     *    <li>Add the {@link GlossaryHistory}.</li>
     *    <li>For each {@link GlossaryXwalk} relationship, create a {@link GlossaryXwalkHistory},
     *    remove the {@link GlossaryXwalk} and add a {@link GlossaryXwalkHistory}.</li>
     * </ol>
     * </p>
     * 
     * @param em persistence context
     * @param userId
     *            authenticated user's Id
     * @param glossType
     *            type of glossary item
     * @param itemName
     *            name of the glossary itme
     */
    @SuppressWarnings("unchecked")
    public void removeGlossary(EntityManager em, String userId,
            String glossType, String itemName) throws FacadeException {
        log.entering(CatalogFacade.class.getName(), "removeGlossary");

        Glossary.PK pk = new Glossary.PK(glossType, itemName);
        Glossary target = null;

        try {
            target = em.find(Glossary.class, pk);
            if (target == null) {
                return;
            }

            // find all relationships where the glossary item being deleted is a
            // child of the relationship
            List<GlossaryXwalk> parentRelationships = em.createNamedQuery(
                    "findGlossaryXwalkForChild").setParameter("glossType",
                    target.getPk().getGlossType()).setParameter("itemName",
                    target.getPk().getItemName()).getResultList();

            // find all child relationships under the delete target
            List<GlossaryXwalk> relationshipSubtree = loadSubtreeXwalk(em,
                    target.getPk().getGlossType(), target.getPk().getItemName());

            // merge all relationships together
            List<GlossaryXwalk> allRelationships = new ArrayList<GlossaryXwalk>();
            if (parentRelationships != null) {
                allRelationships.addAll(parentRelationships);
            }
            if (relationshipSubtree != null) {
                allRelationships.addAll(relationshipSubtree);
            }

            GlossaryHistory history = createGlossaryHistory(em, AuditType.DELETE,
                    userId, target);

            // removes all index rows
//            narrativeIndexingFacade.removeGlossaryIndex(em, glossType, itemName);
            
            StringBuffer relMsg = new StringBuffer();

            for (GlossaryXwalk relationship : allRelationships) {

                relMsg.append((relMsg.length() > 0 ? ", " : "")).append(
                        relationship.getPk());

                GlossaryXwalkHistory relationshipHistory = createGlossaryXwalkHistory(em,
                        AuditType.DELETE, userId, relationship);
                em.remove(relationship);
                em.persist(relationshipHistory);
            }

            em.remove(target);
            em.persist(history);

            log.info(messages.getMessage("removeGlossary.relationships",
                    new Object[] { pk, relMsg.toString() }));

        } catch (Exception e) {
            log.severe(e.toString());

            throw new FacadeException(messages.getMessage(
                    "removeGlossary.exception",
                    new Object[] { pk, e.toString() }), e);

        }

        log.exiting(CatalogFacade.class.getName(), "removeGlossary");

    }
    
    /**
     * <p>
     * Removes a {@link Glossary} item and audits to the {@link GlossaryHistory}.  It also removes all 
     * relationships {@link GlossaryXwalk} and audits to {@link GlossaryXwalkHistory}.
     * <br/>
     * <br/>
     * Processing Steps:<br/>
     * <ol>
     *    <li>Create a persistence context and begin the unit-of-work.</li>
     *    <li>Invoke the overloaded <code>removeGlossary</code> with the 
     *    <code>EntityManager</code>.</li>
     *    <li>Commit the unit-of-work and close the persistence context.</li>
     * </ol>
     * </p>
     * 
     * @param userId
     *            authenticated user's Id
     * @param glossType
     *            type of glossary item
     * @param itemName
     *            name of the glossary itme
     */

    public void removeGlossary(String userId, String glossType, String itemName)
            throws FacadeException {
        log.entering(CatalogFacade.class.getName(), "removeGlossary");

        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;

        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            removeGlossary(em, userId, glossType, itemName);
            em.getTransaction().commit();

        } catch (FacadeException e) {
            log.severe(e.toString());
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } catch (Exception e) {
            log.severe(e.toString());
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new FacadeException(messages.getMessage(
                    "removeGlossary.exception",
                    new Object[] { new Glossary.PK(glossType, itemName),
                            e.toString() }), e);

        } finally {
            if (em != null) {
                em.close();
            }
        }

        log.exiting(CatalogFacade.class.getName(), "removeGlossary");

    }
    
    
    
    /**
     * <p>Removes a parent/child {@link GlossaryXwalk} relationship and 
     * audits {@link GlossaryXwalkHistory} deletion.  If the target
     * relationship has children, they are also removed and audited
     * to history.<br/>
     * <br/>
     * <br/>
     * Processing Steps:<br/>
     * <ol>
     *    <li>Look for an existing {@link GlossaryXwalk} relationship.</li>
     *    <li>If a relationship doesn't exist, log a message and exit.</li>
     *    <li>Find all child {@link GlossaryXwalk} dependencies.</li>
     *    <li>Create a {@link GlossaryXwalkHistory} auditing the delete operation.</li>
     *    <li>Remove the target {@link GlossaryXwalk} relationship.</li>
     *    <li>Persist the {@link GlossaryXwalkHistory} auditing the delete.</li>
     *    <li>For each child {@link GlossaryXwalkHistory} relationships.<br/>
     *       <ol>
     *           <li>Create a {@link GlossaryXwalkHistory} for the delete.</li>
     *           <li>Remove the child {@link GlossaryXwalkHistory}.</li>
     *           <li>Persist the {@link GlossaryXwalkHistory}.</li>     
     *       </ol>
     *    </li> 
     * </ol>
     * </p>
     *
     * @param em persistence context
     * @param userId authenticated user requesting change
     * @param glossTypeParent parent glossary type
     * @param itemNameParent parent glossary item name
     * @param glossTypeChild child glossary type
     * @param itemNameChild child glossary name
     * @exception FacadeException fatal exception removing relationship
     */
    public void removeGlossaryRelationship(EntityManager em, String userId,
            String glossTypeParent, String itemNameParent,
            String glossTypeChild, String itemNameChild) throws FacadeException {

        log.entering(CatalogFacade.class.getName(),
                "removeGlossaryRelationship");

        GlossaryXwalk.PK pk = new GlossaryXwalk.PK(
            glossTypeParent, itemNameParent, glossTypeChild, itemNameChild);

        try {

            GlossaryXwalk target = em.find(GlossaryXwalk.class, pk);
            if (target == null) {
                log.warning(messages.getMessage(
                        "removeGlossaryRelationship.notfound",
                        new Object[] { pk }));
                return;
            }

            // find all child dependencies
//            List<GlossaryXwalk> relationshipSubtree = loadSubtreeXwalk(em,
//                    target.getPk().getGlossTypeChild(),
//                    target.getPk().getItemNameChild());
            GlossaryXwalkHistory relationshipHistory = createGlossaryXwalkHistory(em,
                    AuditType.DELETE, userId, target);

            StringBuffer relMsg = new StringBuffer();

            // remove all dependent relationships
//            for (GlossaryXwalk relationship : relationshipSubtree) {
//                relMsg.append((relMsg.length() > 0 ? ", " : "")).append(
//                        relationship.getPk());
//
//                GlossaryXwalkHistory childRelationshipHistory = createGlossaryXwalkHistory(em,
//                        AuditType.DELETE, userId, relationship);
//                em.remove(relationship);
//                em.persist(childRelationshipHistory);
//            }

            em.remove(target);
            em.persist(relationshipHistory);

            log.info(messages.getMessage(
                    "removeGlossaryRelationship.relationships", new Object[] {
                            pk, relMsg.toString() }));

        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "removeGlossaryRelationship.exception", new Object[] { pk,
                            e.toString() }), e);
        }

        log
                .exiting(CatalogFacade.class.getName(),
                        "removeGlossaryRelationship");

    }

    /**
     * <p>Removes a parent/child {@link GlossaryXwalk} relationship and 
     * audits {@link GlossaryXwalkHistory} deletion.  If the target
     * relationship has children, they are also removed and audited
     * to history.<br/>
     * <br/>
     * <br/>
     * Processing Steps:<br/>
     * <ol>
     *    <li>Create an <code>EntityManager</code> and begin the unit-of-work.</li>
     *    <li>Invoke the overloaded <code>removeGlossaryRelationship</code> with
     *    the <code>EntityManager</code> as the first parameter.</li>
     *    <li>Commit the unit-of-work and close the persistence context.</li> 
     * </ol>
     * </p>
     *
     * @param userId authenticated user requesting change
     * @param glossTypeParent parent glossary type
     * @param itemNameParent parent glossary item name
     * @param glossTypeChild child glossary type
     * @param itemNameChild child glossary name
     * @exception FacadeException fatal exception removing relationship
     */

    public void removeGlossaryRelationship(String userId,
            String glossTypeParent, String itemNameParent,
            String glossTypeChild, String itemNameChild) throws FacadeException {

        log.entering(CatalogFacade.class.getName(), "removeGlossaryRelationship");

        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;
  
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            removeGlossaryRelationship(em, userId, glossTypeParent, itemNameParent, glossTypeChild, itemNameChild);
            em.getTransaction().commit();
        } catch (FacadeException e) {
            log.severe(e.toString());
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }            
            throw e;

        } catch (Exception e) {
            log.severe(e.toString());
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            GlossaryXwalk.PK pk = new GlossaryXwalk.PK(
                glossTypeParent, itemNameParent, glossTypeChild, itemNameChild);

            throw new FacadeException(messages.getMessage(
                    "removeGlossaryRelationship.exception", new Object[] { pk,
                            e.toString() }), e);
            
        } finally {
            if (em != null) {
                em.close();
            }            
        }

        log.exiting(CatalogFacade.class.getName(), "removeGlossaryRelationship");
    }
    
    
    /**
     * <p>
     * Returns a list of {@link GlossaryHistory} for a {@link Glossary} item.
     * </p>
     * @param em persistence context
     * @param glossType
     *            the catagory of the item
     * @param itemName
     *            the name of the item
     * @return all historic changes to a {@link Glossary} item
     * @exception FacadeException fatal error finding glossary history
     */
    @SuppressWarnings("unchecked")
    public List<GlossaryHistory> findHistoryForGlossary(EntityManager em,
            String glossType, String itemName) throws FacadeException {

        log.entering(CatalogFacade.class.getName(), "findHistoryForGlossary");

        List<GlossaryHistory> results = null;
        try {
            Query q = em.createNamedQuery("findHistoryForGlossary");
            q.setParameter("glossType", glossType).setParameter("itemName",
                    itemName);
            results = q.getResultList();
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "findHistoryForGlossary.exception", new Object[] {
                            new Glossary.PK(glossType, itemName),
                            e.toString() }));
        }
 
        log.exiting(CatalogFacade.class.getName(), "findHistoryForGlossary");
        
        return results;
    }

    /**
     * <p>
     * Returns a list of {@link GlossaryHistory} for a {@link Glossary} item.
     * </p>
     * 
     * @param glossType
     *            the catagory of the item
     * @param itemName
     *            the name of the item
     * @return all historic changes to a {@link Glossary} item
     * @exception FacadeException fatal error finding glossary history
     */
    public List<GlossaryHistory> findHistoryForGlossary(String glossType,
            String itemName) throws FacadeException {

        log.entering(CatalogFacade.class.getName(), "findHistoryForGlossary");

        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;
        List<GlossaryHistory> results = null;
        try {
            em = emf.createEntityManager();
            results = findHistoryForGlossary(em, glossType, itemName);
        } finally {
            if (em != null) {
                em.close();
            }
        }

        log.exiting(CatalogFacade.class.getName(), "findHistoryForGlossary");

        return results;
    }
    
    
    /**
     * <p>
     * Returns all {@link GlossaryType} ordered by the type.
     * </p>
     * @param em persistence context
     * @return all the {@link GlossaryType} rows
     * @exception FacadeException fatal error finding all glossary types
     */
    @SuppressWarnings("unchecked")
    public List<GlossaryType> findAllGlossaryType(EntityManager em) throws FacadeException {

        log.entering(CatalogFacade.class.getName(), "findAllGlossaryType");

        List<GlossaryType> results = null;
        try {
            results = em.createNamedQuery("findAllGlossaryType")
                    .getResultList();
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "findAllGlossaryType.exception", new Object[] { e
                            .getMessage() }));
        }

        log.exiting(CatalogFacade.class.getName(), "findAllGlossaryType");

        return results;
    }

    /**
     * <p>
     * Returns all {@link GlossaryType} ordered by the type.
     * </p>
     * 
     * @return all the {@link GlossaryType} rows
     * @exception FacadeException fatal error finding all glossary types
     */
    @SuppressWarnings("unchecked")
    public List<GlossaryType> findAllGlossaryType() throws FacadeException {
        log.entering(CatalogFacade.class.getName(), "findAllGlossaryType");

        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;

        List<GlossaryType> results = null;

        try {
            em = emf.createEntityManager();

            results = em.createNamedQuery("findAllGlossaryType")
                    .getResultList();

        } finally {
            if (em != null) {
                em.close();
            }
        }

        log.exiting(CatalogFacade.class.getName(), "findAllGlossaryType");

        return results;
    }

    
    /**
     * <p>Finds all children for a {@link Glossary} item by <code>glossType</code>
     * and <code>itemName</code>  The child relationships are find by looking
     * at all {@link GlossaryXwalk} that the parent keys match the target
     * item.</p>
     * @param em persistence context
     * @param glossType type of the glossary item
     * @param itemName name of the glossary item
     * @return list of child Glosary items
     * @exception FacadeException fatal exception finding glossary children
     */
    @SuppressWarnings("unchecked")
    public List<Glossary.PK> findGlossaryChildren(EntityManager em,
            String glossType, String itemName) throws FacadeException {
        
        log.entering(CatalogFacade.class.getName(), "findGlossaryChildren");

        List<Glossary.PK> results = null;
        try {
            Query q = em.createNamedQuery("findGlossaryChildren");
            q.setParameter("glossType", glossType).setParameter("itemName",
                    itemName);
            results = q.getResultList();
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "findGlossaryChildren.exception", new Object[] {
                            new Glossary.PK(glossType, itemName),
                            e.toString() }));

        }
        log.exiting(CatalogFacade.class.getName(), "findGlossaryChildren");

        return results;
    }

    
    /**
     * <p>Finds all children for a {@link Glossary} item by <code>glossType</code>
     * and <code>itemName</code>  The child relationships are find by looking
     * at all {@link GlossaryXwalk} that the parent keys match the target
     * item.</p>
     * 
     * @param glossType type of the glossary item
     * @param itemName name of the glossary item
     * @return list of child Glosary items
     * @exception FacadeException fatal exception finding glossary children
     */
    @SuppressWarnings("unchecked")
    public List<Glossary.PK> findGlossaryChildren(String glossType, String itemName) throws FacadeException {
        log.entering(CatalogFacade.class.getName(), "findGlossaryChildren");
        
        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;

        List<Glossary.PK> results = null;

        try {
            em = emf.createEntityManager();
            results = findGlossaryChildren(em, glossType, itemName);
        } finally {
            if (em != null) {
                em.close();
            }
        }

        log.exiting(CatalogFacade.class.getName(), "findGlossaryChildren");

        return results;
    }

    
    /**
     * <p>Finds all parents for a {@link Glossary} item by <code>glossType</code>
     * and <code>itemName</code>  The parent relationships are find by looking
     * at all {@link GlossaryXwalk} that the child keys match the target
     * item.</p>
     * @param em persistence context
     * @param glossType type of the glossary item
     * @param itemName name of the glossary item
     * @return list of parent Glosary items
     * @exception FacadeException fatal error finding parent
     */
    @SuppressWarnings("unchecked")
    public List<Glossary.PK> findGlossaryParents(EntityManager em,
            String glossType, String itemName) throws FacadeException {
        
        log.entering(CatalogFacade.class.getName(), "findGlossaryParents");
        
        List<Glossary.PK> results = null;
        try {
            Query q = em.createNamedQuery("findGlossaryParents");
            q.setParameter("glossType", glossType).setParameter("itemName",
                    itemName);

            results = q.getResultList();
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage(
                    "findGlossaryParents.exception",
                    new Object[] { new Glossary.PK(glossType, itemName), e.toString()}));

        }
        log.exiting(CatalogFacade.class.getName(), "findGlossaryParents");

        return results;
    }

    
    /**
     * <p>Finds all parents for a {@link Glossary} item by <code>glossType</code>
     * and <code>itemName</code>  The parents relationships are find by looking
     * at all {@link GlossaryXwalk} that the child keys match the target
     * item.</p>
     * 
     * @param glossType type of the glossary item
     * @param itemName name of the glossary item
     * @return list of parent Glosary items
     * @exception FacadeException error invoking the find
     */
    @SuppressWarnings("unchecked")
    public List<Glossary.PK> findGlossaryParents(String glossType, String itemName) throws FacadeException {
        log.entering(CatalogFacade.class.getName(), "findGlossaryParents");
        
        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;

        List<Glossary.PK> results = null;

        try {
            em = emf.createEntityManager();
            results = findGlossaryParents(em, glossType, itemName);
        } finally {
            if (em != null) {
                em.close();
            }
        }

        log.exiting(CatalogFacade.class.getName(), "findGlossaryParents");

        return results;
    }


    /**
     * <p>Returns a {@link DictUser} by <code>userId</code>.</p>
     * @param userId id of the user
     * @return {@link DictUser} object
     * @throws FacadeException JPA exception find a user by id
     */
    public DictUser findUser(String userId) throws FacadeException {
        log.entering(CatalogFacade.class.getName(), "findUser");

        DictUser user = null;

        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            user = em.find(DictUser.class, userId);
        } catch (Exception e) {
            log.severe(e.toString());
            throw new FacadeException(messages.getMessage("findUser.exception", 
                                                          new Object[] { userId, 
                                                                         e.toString() }));
        } finally {
            if (em != null) {
                em.close();
            }
        }

        log.exiting(CatalogFacade.class.getName(), "findUser");

        return user;
    }

    public void persistFeedback(GlossaryFeedback glossFeed)
            throws FacadeException {
        log.entering(CatalogFacade.class.getName(), "persistFeedback");

        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;

        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            persistFeedback(em, glossFeed);
            em.getTransaction().commit();
        } catch (Exception e) {
            log.severe(e.toString());
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new FacadeException("persistFeedback.exception", e);

        } finally {
            if (em != null) {
                em.close();
            }
        }

        log.exiting(CatalogFacade.class.getName(), "removeGlossary");

    }

    void persistFeedback(EntityManager em, GlossaryFeedback gf) {
        Query insert = em.createNativeQuery("insert into " +
                "GlossaryUsagePageRating " +
                "   (GlossaryUsagePageIP, GlossaryUsagePageRating, GlossaryUsagePageSession, " +
                "    GlossaryUsagePageComment, GlossaryUsagePageTimestamp)" +
                "   VALUES" +
                "   (#addr, #rating, #sess, #comment,  GETDATE())"
            ).
                setParameter("addr", gf.getIpAddress()).
                setParameter("rating", gf.getRating()).
                setParameter("sess", gf.getSession()).
               setParameter("comment", gf.getComment());
        insert.executeUpdate();
    }

    public void persistUsage(GlossaryUsage glossUsage)
            throws FacadeException {
        log.entering(CatalogFacade.class.getName(), "persistUsage");

        EntityManagerFactory emf = getPersistenceUnit();
        EntityManager em = null;

        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            persistUsage(em, glossUsage);
            em.getTransaction().commit();
        } catch (Exception e) {
            log.severe(e.toString());
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new FacadeException("persistUsage.exception", e);

        } finally {
            if (em != null) {
                em.close();
            }
        }

        log.exiting(CatalogFacade.class.getName(), "removeGlossary");

    }

    void persistUsage(EntityManager em, GlossaryUsage gu) {
        Query insert = em.createNativeQuery("insert into " +
            "GlossaryUsageStatistics " +
            "  (GlossaryUsagePage, GlossaryUsageExtra," +
            "   GlossaryUsageIP, GlossaryUsageSession," +
            "   GlossaryUsageTimestamp" +
            "   )" +
            "   VALUES" +
            "   (#pi, #px, #addr, #sess,  GETDATE())"
            ).
                setParameter("addr", gu.getIpAddress()).
                setParameter("pi", gu.getPageId()).
                setParameter("px", gu.getPageExtra()).
                setParameter("sess", gu.getSession());
        insert.executeUpdate();
    }
    
    /**
      * <p>Finds session length {@link StatisticSessionLength} </p>
      * 
      * @return list of StatisticSessionLength
      * @exception FacadeException error invoking the find
      */
     @SuppressWarnings("unchecked")
     public List<StatisticSessionLength> findSessionLength(Date startDate, Date endDate) throws FacadeException {
         log.entering(CatalogFacade.class.getName(), "findSessionLength");
         
         EntityManagerFactory emf = getPersistenceUnit();
         EntityManager em = null;

         List<StatisticSessionLength> results = new ArrayList<StatisticSessionLength>();

         try {
         	StringBuffer sql = new StringBuffer("WITH cteOne AS "
         			+ " (SELECT	distinct GlossaryUsageSession,"
         			+ " CAST(MIN(GlossaryUsageTimestamp) OVER (PARTITION BY GlossaryUsageSession) AS DATE) AS day," 
         			+ " DATEDIFF(SECOND, MIN(GlossaryUsageTimestamp) OVER (PARTITION BY GlossaryUsageSession),"
         			+ " MAX(GlossaryUsageTimestamp) OVER (PARTITION BY GlossaryUsageSession)) AS slen "
         			+ " FROM   GlossaryUsageStatistics) "
         			+ " SELECT DISTINCT day, AVG(ABS(slen)) OVER (PARTITION BY day) AS avglen "
         			+ " FROM cteOne");
                 
                 if (startDate != null)
                    sql.append(" and GlossaryUsageTimestamp >= ? ");
                 
                 if (endDate != null)
                    sql.append(" and GlossaryUsageTimestamp <= ? ");
                    
                 sql.append(" ORDER BY day ");
                 
                 em = emf.createEntityManager();
                 Query query = em.createNativeQuery(sql.toString());
                 
                 int idx = 1;
                 if (startDate != null)
                   query.setParameter(idx++, startDate);
                   
                 if (endDate != null)
                    query.setParameter(idx++, endDate);
                 
                 List resultList = query.getResultList();
                 Iterator itr = resultList.iterator();
                 while (itr.hasNext())
                 {
                     Object[] resultRow = (Object[])itr.next();
                     StatisticSessionLength  row = new StatisticSessionLength();
                     row.setDay(resultRow[0].toString());
                     row.setSessionLength(Long.valueOf(resultRow[1].toString())/60);
                     results.add(row);
                 }
         } 
         catch (Exception e) {
               log.log(Level.SEVERE, "Failed getting session length data.", e);
         }finally {
             if (em != null) {
                 em.close();
             }
         }

         log.exiting(CatalogFacade.class.getName(), "findSessionLength");

         return results;
     }
     
        /**
         * <p>Finds session per day {@link StatisticSessionPerDay} </p>
         * 
         * @return list of StatisticSessionPerDay
         * @exception FacadeException error invoking the find
         */
        @SuppressWarnings("unchecked")
        public List<StatisticSessionPerDay> findSessionPerDay(Date startDate, Date endDate) throws FacadeException {
            log.entering(CatalogFacade.class.getName(), "findSessionPerDay");
            
            EntityManagerFactory emf = getPersistenceUnit();
            EntityManager em = null;
        
            List<StatisticSessionPerDay> results = new ArrayList<StatisticSessionPerDay>();
        
            try {
            	StringBuffer sql = new StringBuffer("WITH cteOne AS "
            									+ " (SELECT DISTINCT GlossaryUsageSession, "  
            											+ " CAST(MIN(GlossaryUsageTimestamp) OVER (PARTITION BY GlossaryUsageSession) AS DATE) day, "   
            											+ " CAST(MIN(GlossaryUsageTimestamp) OVER (PARTITION BY GlossaryUsageSession) AS TIME(0)) hour, "   
            											+ " CAST(MIN(GlossaryUsageTimestamp) OVER (PARTITION BY GlossaryUsageSession) AS SMALLDATETIME) timeofday "  
            									+ " FROM GlossaryUsageStatistics  WHERE 1=1 ) " 
            									+ " SELECT DISTINCT day, " 
            									+ " 		hour, " 
            									+ " 		COUNT(*) OVER (PARTITION BY timeofday) cnt "  
            									+ " FROM cteOne ");

            	if (startDate != null)
                  sql.append(" and GlossaryUsageTimestamp >= ? ");
                
                if (endDate != null)
                  sql.append(" and GlossaryUsageTimestamp <= ? ");
                  
                sql.append(" ORDER BY day, hour  ");
                   
                em = emf.createEntityManager();
                Query query = em.createNativeQuery(sql.toString());
                
                int idx = 1;
                if (startDate != null)
                 query.setParameter(idx++, startDate);
                 
                if (endDate != null)
                  query.setParameter(idx++, endDate);
                
                List resultList = query.getResultList();
                Iterator itr = resultList.iterator();
                while (itr.hasNext())
                {
                    Object[] resultRow = (Object[])itr.next();
                    StatisticSessionPerDay  row = new StatisticSessionPerDay();
                    row.setDay(resultRow[0].toString());
                    row.setHour(resultRow[1].toString());
                    row.setCount(Long.valueOf(resultRow[2].toString()));
                    results.add(row);
                }
            }
            catch (Exception e) {
                  log.log(Level.SEVERE, "Failed getting session per day data.", e);
            }
            finally {
                if (em != null) {
                    em.close();
                }
            }
        
            log.exiting(CatalogFacade.class.getName(), "findSessionPerDay");
        
            return results;
        }
        
        /**
         * <p>Finds feedback usage {@link StatisticFeedbackUsage} </p>
         * 
         * @return list of StatisticFeedbackUsage
         * @exception FacadeException error invoking the find
         */
        @SuppressWarnings("unchecked")
        public List<StatisticFeedbackUsage> findFeedbackUsage(Date startDate, Date endDate) throws FacadeException {
            log.entering(CatalogFacade.class.getName(), "findFeedbackUsage");
            
            EntityManagerFactory emf = getPersistenceUnit();
            EntityManager em = null;
        
            List<StatisticFeedbackUsage> results = new ArrayList<StatisticFeedbackUsage>();
        
            try {
                StringBuffer sql = new StringBuffer( "SELECT " + 
                    "    r.GlossaryUsagePageRating, " + 
                    "    r.GlossaryUsagePageTimestamp, " +
                    "    s.GlossaryUsagePage, " +
                    "    s.GlossaryUsageExtra, " +
                    "    r.GlossaryUsagePageComment " +
                    "FROM GlossaryUsagePageRating r " + 
                    "LEFT JOIN GlossaryUsageStatistics s " + 
                    "     ON r.GlossaryUsagePageSession = s.GlossaryUsageSession ");
                
                if (startDate != null)
                   sql.append(" AND r.GlossaryUsagePageTimestamp >= ? ");
                
                if (endDate != null)
                   sql.append(" AND r.GlossaryUsagePageTimestamp <= ? ");
                   
                sql.append(" ORDER BY GlossaryUsagePageTimestamp DESC");
                
                em = emf.createEntityManager();
                Query query = em.createNativeQuery(sql.toString());
                
                int idx = 1;
                if (startDate != null)
                  query.setParameter(idx++, startDate);
                  
                if (endDate != null)
                   query.setParameter(idx++, endDate);
                   
                List resultList = query.getResultList();
                Iterator itr = resultList.iterator();
                while (itr.hasNext())
                {
                    Object[] resultRow = (Object[])itr.next();
                    StatisticFeedbackUsage  row = new StatisticFeedbackUsage();
                    row.setUserRate(resultRow[0].toString());
                    row.setUsageDate((Timestamp)resultRow[1]);
                    
                    // User could submit an entry without a page attached
                    if (resultRow[2] != null)
                    {
                    	row.setUsagePage(resultRow[2].toString());
                    }

                    // User could submit an entry without entering a comment
                    if (resultRow[3] != null)
                    {
                    	row.setUsageExtra(resultRow[3].toString());
                    }
                    // User could submit an entry without an extra
                    if (resultRow[4] != null)
                    {
                    	row.setUserComment(resultRow[4].toString());
                    }

                    results.add(row);
                }
              
            } 
            catch (Exception e) {
                  log.log(Level.SEVERE, "Failed getting feedback usage.", e);
            }finally {
                if (em != null) {
                    em.close();
                }
            }
        
            log.exiting(CatalogFacade.class.getName(), "findFeedbackUsage");
        
            return results;
        }

}
