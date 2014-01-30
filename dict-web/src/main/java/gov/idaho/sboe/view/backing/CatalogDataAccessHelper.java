package gov.idaho.sboe.view.backing;

import java.sql.Date;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import javax.persistence.EntityManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.shale.tiger.managed.Bean;
import org.apache.shale.tiger.managed.Scope;
import org.apache.shale.view.AbstractApplicationBean;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.Glossary.PK;
import gov.idaho.sboe.jpa.beans.GlossaryFeedback;
import gov.idaho.sboe.jpa.beans.GlossaryHistory;
import gov.idaho.sboe.jpa.beans.GlossaryType;
import gov.idaho.sboe.jpa.beans.GlossaryUsage;
import gov.idaho.sboe.jpa.beans.GlossaryXRef;
import gov.idaho.sboe.jpa.beans.SearchResult;
import gov.idaho.sboe.jpa.beans.StatisticFeedbackUsage;
import gov.idaho.sboe.jpa.beans.StatisticSessionLength;
import gov.idaho.sboe.jpa.beans.StatisticSessionPerDay;
import gov.idaho.sboe.jpa.beans.DictUser;
import gov.idaho.sboe.services.CatalogFacade;
import gov.idaho.sboe.services.FacadeException;
import gov.idaho.sboe.services.NarrativeIndexingFacade;
import gov.idaho.sboe.utils.JPACacheManager;
import gov.idaho.sboe.utils.JPAResourceBean;
import gov.idaho.sboe.view.utils.JSFUtils;


/**
 * <p>This is a stateless application scoped bean that acts as the
 * glue between the JSF API and the service tier.  Since it is a 
 * Shale application bean, the <code>destroy</code> method will be 
 * invoked when the container shuts down.  The <code>init()</code>
 * method is invoked when the bean is first loaded into application 
 * scope.</p>
 */
@Bean(name = Globals.DATA_ACCESS_HELPER, scope = Scope.APPLICATION)
public class CatalogDataAccessHelper extends AbstractApplicationBean {

    /**
     * <p>Java log utility class.</p>
     */
    private static Logger log = 
        Logger.getLogger(CatalogDataAccessHelper.class.getName());


    /**
     * <p>Retrieves the <code>GlossaryType</code>'s using the <code>CatalogFacade</code>
     * and decorated into a {@link CatalogElementBean}.  The glossary types are used
     * as the first level of the tree.  All search results will hang of this 
     * first level of glossary terms.</p>
     * 
     * @param glossTypeString the glossary type to include; null for all
     * @return  Map of GlossaryType wrapped by the {@link CatalogElementBean}
     * @throws FacadeException error retrieving glossary types
     */
    private Map<String, CatalogElementBean> getTreeFirstLevel(String glossTypeString) throws FacadeException {
        Map<String, CatalogElementBean> typesMap = 
            new TreeMap<String, CatalogElementBean>();

        List<GlossaryType> types = ((JPAResourceBean)getBean(Globals.JPA_RESOURCE)).
            getCacheManager((CatalogFacade)getBean(Globals.CATALOG_FACADE)).getGlossaryTypes();

        for (GlossaryType glossType: types) {
            if (glossTypeString == null || glossType.getGlossType().equals(glossTypeString)) {
                CatalogElementBean bean = new CatalogElementBean(glossType);
                typesMap.put(bean.getItemName(), bean);
            }
        }

        return typesMap;
    }

    
    /**
     * <p>Recursively builds the subtree finding a parents children.</p>
     * @param parent node that might have children
     * @param xrefList the Xwalk table
     * @throws FacadeException error retrieving the subtree
     */
    private void buildGlossarySubtree(CatalogElementBean parent, 
    		Map<String, List<GlossaryXRef>> xrefList, String thisType, boolean showType)
        throws FacadeException {
        List<Glossary.PK> children = 
            findGlossaryChildren(parent, xrefList, thisType);
           
      
//        boolean showOnlyDataElements = false;
//        for (String type: Globals.CONTAINER_TYPES) {
//            if (type.equals(parent.getItemType())) {
//                showOnlyDataElements = true;
//                break;
//            }
//        }
            
        // case insensitive
        Comparator<PK> comparator = new Comparator<PK>() {
                public int compare(Glossary.PK o1, Glossary.PK o2) {
                    return o1.getItemName().compareToIgnoreCase(o2.getItemName());
                }
            };

        Collections.sort(children, comparator);
        
        Security sec = (Security)JSFUtils.getFromSession(Globals.SECURITY);
        boolean isAdmin = false;
        if (sec != null && sec.getIsAuthenticated())
            isAdmin = true;
            
        for (Glossary.PK c: children) {

            if (c.getGlossType().equals(Globals.DATA_ELEMENT_TYPE) || 
                c.getGlossType().equals(Globals.COLLECTIONS_TYPE) ||
                (isAdmin))  
            {
                CatalogElementBean child = new CatalogElementBean(c, showType);
                parent.addChild(child);
                child.setParent(parent);
                // avoid infinite circular references
                CatalogElementBean tester = child;
                boolean sameParentFound = false;
                while (tester.getParent()!= null) {
                    if (tester.getParent().equals(child)) {
                        sameParentFound = true;
                        break;
                    }
                    tester = tester.getParent();
                }
                if (!sameParentFound) {
                    buildGlossarySubtree(child, xrefList, c.getGlossType(), showType);
                }
            }

        }
    }

    /**
     * For a given item, find all the children in the list
     * V0.1 - the brute force way - seems to increase performance
     * from ~18 sec. on flash to ~6 sec. locally
     * 
     * @param parent
     * @param xrefMap
     * @return the child PKs
     */
    public List<Glossary.PK> findGlossaryChildren(CatalogElementBean parent, 
    		Map<String, List<GlossaryXRef>> xrefMap,
                String thisType) {
        List<Glossary.PK> children = new ArrayList<Glossary.PK>();
        List<GlossaryXRef> xrefList = xrefMap.get(thisType);
        if (xrefList == null || xrefList.size()==0) return children;
        GlossaryXRef fromElement = new GlossaryXRef(
            parent.getItemType(), parent.getItemName(), "", "");
        Glossary.PK parentPK = fromElement.getParent();
        // use a comparator which only looks at parents
        // the binary search does not specifiy which will be returned
        // when there are multiple matches
        // it also returns <0 if not found
        // ...
        int i = Collections.binarySearch(xrefList, fromElement, new Comparator<GlossaryXRef>(){
            public int compare(GlossaryXRef one, GlossaryXRef two) {
                return one.getParent().compareTo(two.getParent());
            }
        });
        int j = i;
        // first, add the match and all matches previous
        while (j>=0) {
            GlossaryXRef test = xrefList.get(j);
            if (test.getParent().equals(parentPK)) {
                children.add(test.getChild());
            } else {
                break;
            }
            j--;
        }
        j = i+1;
        // now add all subsequent matches
        while (j>0 && j<xrefList.size()) {
          GlossaryXRef test = xrefList.get(j);
          if (test.getParent().equals(parentPK)) {
              children.add(test.getChild());
          } else {
              break;
          }
          j++;
        }
    	return children;
    }


    /**
     * <p>Returns a graph of <code>Glossary</code> search results
     * wrapped in {@link CatalogElementBean}.  The first level
     * of the tree, the root nodes, will always be the 
     * <code>GlossaryType</code>'s.  Search hits will be attached
     * to their owing types.  The subtree will be attached 
     * to each <code>Glossary</code>.
     * </p>
     * @param sarg search argument
     * @return graph of Glossary items organized by type
     * @throws FacadeException error performing search
     */
    public List<CatalogElementBean> retrieveCatalog(String sarg) throws FacadeException {
//        List<CatalogElementBean> roots = new ArrayList<CatalogElementBean>();

        // lookup our service layer
        NarrativeIndexingFacade indexingFacade = 
            (NarrativeIndexingFacade)getBean(Globals.INDEXING_FACADE);
        try {
            // ad-hoc narrative search
            List<Glossary.PK> results;
            if (sarg.trim().length() > 0) {
                results = indexingFacade.searchGlossaryWildcard(sarg, Globals.COLLECTIONS_TYPE);
            } else {
                // make empty list
                results = new ArrayList<Glossary.PK>();
            }
    
            return buildCatalogTree(results, true, false, Globals.COLLECTIONS_TYPE, false);
        } catch (FacadeException fe) {
            if (fe.isMessageForUser()) {
                registerErrorMessage(fe.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void registerErrorMessage(String msg) {
        FacesContext context = getFacesContext();
        context.addMessage(msg, new FacesMessage(msg));      
    }
    /**
     * <p>Returns a graph of <code>Glossary</code> search results
     * wrapped in {@link CatalogElementBean}.  The first level
     * of the tree, the root nodes, will always be the 
     * <code>GlossaryType</code>'s.  Search hits will be attached
     * to their owing types.  The subtree will be attached 
     * to each <code>Glossary</code>.
     * </p>
     * @param sarg search argument
     * @return graph of Glossary items organized by type
     * @throws FacadeException error performing search
     */
    public List<SearchResult> keywordSearch(String sarg) throws FacadeException {
        // lookup our service layer
        NarrativeIndexingFacade indexingFacade = 
            (NarrativeIndexingFacade)getBean(Globals.INDEXING_FACADE);

        try {
            // ad-hoc narrative search
            return indexingFacade.searchGlossaryKeywords(sarg);
        } catch (FacadeException fe) {
            if (fe.isMessageForUser()) {
                registerErrorMessage(fe.getMessage());
            } else 
                throw fe;
        }
        return null;
    }


    /**
     * <p>
     * </p>
     * @return graph of <code>Glossary.PK</code> for all collections
     * @throws FacadeException error performing search
     */
    public List<CatalogElementBean> retrieveCollections() throws FacadeException {
        List<Glossary.PK> results = new ArrayList<Glossary.PK>();
        final CatalogFacade catalogFacade = (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        final JPACacheManager cacheManager = ((JPAResourceBean)getBean(Globals.JPA_RESOURCE)).getCacheManager(catalogFacade);
        // we have to look through all types to get the GT for Collections
        final List<GlossaryType> types = cacheManager.getGlossaryTypes();
        GlossaryType gt = null;
        for (GlossaryType gtTest: types) {
            if (gtTest.getGlossType().equals(Globals.COLLECTIONS_TYPE)) {
                gt = gtTest;
                break;
            }
        }
        results.addAll(cacheManager.getGlossaryListForType(gt));
        return buildCatalogTree(results, true, false, Globals.COLLECTIONS_TYPE, false);
    }

    /**
     * <p>
     * </p>
     * @return graph of <code>Glossary.PK</code> for all collections
     * @throws FacadeException error performing search
     */
    public List<CatalogElementBean> retrieveCatalog() throws FacadeException {
        List<Glossary.PK> results = new ArrayList<Glossary.PK>();
        final CatalogFacade catalogFacade = (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        final JPACacheManager cacheManager = ((JPAResourceBean)getBean(Globals.JPA_RESOURCE)).getCacheManager(catalogFacade);
        final List<GlossaryType> types = cacheManager.getGlossaryTypes();
        for (GlossaryType type:types) {
            if (type.isGlossVisible()) {
                results.addAll(cacheManager.getGlossaryListForType(type));
            }
        }
        return buildCatalogTree(results, true, false, Globals.COLLECTIONS_TYPE, false);
    }



    /**
     * <p>Returns a graph of <code>Glossary</code> search results
     * wrapped in {@link CatalogElementBean}.  The first level
     * of the tree, the root nodes, will always be the 
     * <code>GlossaryType</code>'s.  Search hits will be attached
     * to their owing types.  The subtree will be attached 
     * to each <code>Glossary</code>.
     * </p>
     * @param selectedTab tab that is in focus
     * @return graph of Glossary items organized by type
     * @throws FacadeException error performing search
     */
    public List<CatalogElementBean> retrieveCatalog(TabBean selectedTab, boolean showType) throws FacadeException {
        return retrieveCatalog(selectedTab, Globals.COLLECTIONS_TYPE, showType);
    }

    public List<CatalogElementBean> retrieveCatalog(TabBean selectedTab, String glossType, boolean showType)
        throws FacadeException {

        // lookup our service layer
        NarrativeIndexingFacade indexingFacade = 
            (NarrativeIndexingFacade)getBean(Globals.INDEXING_FACADE);


        // ad-hoc narrative search
        List<Glossary.PK> results = 
            indexingFacade.searchGlossaryRolodex(selectedTab.getBeginFilter(), selectedTab.getEndFilter());


        return buildCatalogTree(results, true, false, glossType, showType);
    }

    /**
     * <p>Builds the search results for the selected {@link CatalogElementBean}.</p>
     * @param gloss
     * @return graph of Glossary items organized by type
     * @throws FacadeException error performing search
     */
//    public List<CatalogElementBean> retrieveCatalog(Glossary gloss) throws FacadeException {
//        // ad-hoc narrative search
//        List<Glossary.PK> results = new ArrayList<Glossary.PK>(); 
//        results.add(gloss.getPk());
//
//        return buildCatalogTree(results, true, false, gloss.getPk().getGlossType());
//    }

    /**
     * <p>Builds the search results for the selected {@link CatalogElementBean}.</p>
     * @param selectedElement
     * @return graph of Glossary items organized by type
     * @throws FacadeException error performing search
     */
    public List<CatalogElementBean> retrieveCatalog(CatalogElementBean selectedElement, boolean showType)
    throws FacadeException {
        return buildCatalogTree(selectedElement, showType);
    }
    

    /**
     * @param results all matching <code>Glossary</code> items that match the search criteria
     * @param fillSubtree indicates if the full subtree should be populated
     * @param addRoots is only used when creating a reference tree which is it's own root
     * @param glossTypeString CatalogElementBean element filter, only include item of this type
     * @param showType to indicate is the CatalogElementBean should be displayed
     * @return tree structure used as modelall for building a reference tree
     * @throws FacadeException error building tree
     */
    private List<CatalogElementBean> buildCatalogTree(List<Glossary.PK> results, boolean fillSubtree, boolean addRoots, 
                                                      String glossTypeString, boolean showType)  
    throws FacadeException {
        // case insensitive
        Comparator<PK> comparator = new Comparator<PK>() {
                public int compare(PK o1, PK o2) {
                    return o1.getItemName().compareToIgnoreCase(o2.getItemName());
                }
            };

        Collections.sort(results, comparator);

        List<CatalogElementBean> roots = new ArrayList<CatalogElementBean>();

        // returns the first level of the tree
        Map<String, CatalogElementBean> typesMap = getTreeFirstLevel(glossTypeString);
 
        JPAResourceBean bean = 
            (JPAResourceBean)getBean(Globals.JPA_RESOURCE);
        Map<String, List<GlossaryXRef>> xrefList = bean.getCacheManager((CatalogFacade)getBean(Globals.CATALOG_FACADE)).getGlossaryXwalkTable();
        // for each glossary that was a search hit, 
        // add it to its owning glossary type and then
        // build the subtree under the item
        for (Glossary.PK node: results) {
            CatalogElementBean child = null;
            if (addRoots) {
                // dead code if under addRoots, the real logic is below.
                CatalogElementBean glossType = typesMap.get(node.getGlossType());
                if (glossType == null) continue; // which would happen if not visible
                child = new CatalogElementBean(node, showType);
                glossType.addChild(child);
            } else {
                if (glossTypeString != null && !node.getGlossType().equals(glossTypeString)) continue;
                child = new CatalogElementBean(node, showType);
                roots.add(child);
            }
            child.setCanCopy(true);
            if (fillSubtree) {
                buildGlossarySubtree(child, xrefList, node.getGlossType(), showType);
            }
        }

        if (addRoots) {
            for (Map.Entry<String, CatalogElementBean> t: typesMap.entrySet()) {
                CatalogElementBean catalogElementBean = t.getValue();
                if ((catalogElementBean.getChildren()!=null && catalogElementBean.getChildren().size()!=0)) {
                    catalogElementBean.setCanCopy(false);
                    roots.add(catalogElementBean);
                }
            }
        }
        return roots;       
    }

    /**
     * Variation on a theme for only one "result" passed in...
     * @param item
     * @param fillSubtree
     * @param addRoots
     * @return
     * @throws FacadeException
     */
    private List<CatalogElementBean> buildCatalogTree(CatalogElementBean item, boolean showType)  
    throws FacadeException {
        List<CatalogElementBean> roots = new ArrayList<CatalogElementBean>();
        boolean fillSubtree = true;
        boolean addRoots = false;
        // returns the first level of the tree
        Map<String, CatalogElementBean> typesMap = getTreeFirstLevel(item.getItemType());
    
        JPAResourceBean bean = 
            (JPAResourceBean)getBean(Globals.JPA_RESOURCE);
        Map<String, List<GlossaryXRef>> xrefList = bean.getCacheManager((CatalogFacade)getBean(Globals.CATALOG_FACADE)).getGlossaryXwalkTable();
            CatalogElementBean child = null;
        try {
            child = (CatalogElementBean)item.clone();
        } catch (CloneNotSupportedException e) {
            child = new CatalogElementBean((Glossary)item.getInstance(), showType);
        }
        roots.add(child);
        child.setCanCopy(true);
        buildGlossarySubtree(child, xrefList, item.getItemType(), showType);
        return roots;       
    }

    /**
     * @return a list of simple Java beans used to create the interface Tabs
     */
    public List<TabBean> retrieveTabs() {
        List<TabBean> list = new ArrayList<TabBean>();

        for (char c = 'A'; c <= 'Z'; c++) {
            list.add(new TabBean(false, c, c));
        }
//        list.add(new TabBean(false, 'W', 'Z'));
        return list;
    }
    
    /**
     * <p>Returns all the <code>CatalogType</code> as <code>SelectItem</code>
     * objects which is needed to populate droplists.</p>
     *
     * @return list used to populate a droplist
     */
    public SelectItem[] retrieveCatalogTypes() throws FacadeException {
        JPAResourceBean bean = 
            (JPAResourceBean)getBean(Globals.JPA_RESOURCE);
        List<GlossaryType> types = bean.getCacheManager((CatalogFacade)getBean(Globals.CATALOG_FACADE)).getGlossaryTypes();
        
        SelectItem[] items = new SelectItem[types.size()];
        int i = 0;
        for (GlossaryType t: types) {
            SelectItem item = new SelectItem();
            item.setLabel(t.getGlossType());
            item.setValue(t.getGlossType());
            items[i++] = item;
        }

        return items;
    }


    /**
     * <p>Returns all of the direct parents of a <code>selectedElement</code>.</p>
     * 
     * @param selectedElement glossary item that is in focus
     * @return list of the elements parents
     * @throws gov.idaho.sboe.view.backing.ShowException error finding an items parents
     */
    public List<CatalogElementBean> retrieveReferences(CatalogElementBean selectedElement) throws FacadeException {
       List<Glossary.PK> references = 
            new ArrayList<Glossary.PK>();

        CatalogFacade catalogFacade = 
            (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        List<Glossary.PK> parents = 
            catalogFacade.findGlossaryParents(selectedElement.getItemType(),
                                              selectedElement.getItemName());
        if (parents.size() == 0) {
            return null;
        }

        
        Security sec = (Security)getBean(Globals.SECURITY);
        for (Glossary.PK parent: parents) {
            
            if (selectedElement.getItemType().equals(Globals.COLLECTIONS_TYPE))
            {
                if (sec.getIsAuthenticated())
                   references.add(parent);
                else 
                {
                   if (parent.getGlossType().equals(Globals.COLLECTIONS_TYPE) ||
                       parent.getGlossType().equals(Globals.COLLECTIONS_RULE_TYPE) ||
                       parent.getGlossType().equals(Globals.REPORT) )
                    {
                           references.add(parent);
                    }
                }
            }
            else 
            {
                references.add(parent);
            }
        }
        // in reference list, the types are root nodes, so no reason to
        // show data types in tree.
        return buildCatalogTree(references, false, true, null, false);
    }

    /**
     * <p>Saves a list of Glossary items.  This doesn't save relationships. 
     * Only the <code>Glossary</code> entity beans.</p>
     * 
     * @param children glossary items in the scratch pad
     * @throws gov.idaho.sboe.view.backing.ShowException error saving <code>Glossary</code> items
     */
    public void saveItems(List<CatalogElementBean> children) throws FacadeException {

        CatalogFacade catalogFacade = 
            (CatalogFacade)getBean(Globals.CATALOG_FACADE);

        Security user = (Security)getBean(Globals.SECURITY);

        EntityManager em = null;
        try {
            em = catalogFacade.getPersistenceUnit().createEntityManager();

            em.getTransaction().begin();
            for (CatalogElementBean c: children) {
            // FIXME instance.pk != c.itemName, c.glossType
                catalogFacade.persistGlossary(em, user.getUserId(), 
                                              (Glossary)c.getInstance());
                // the instance objects were updated, but not the corresponding
                // CatalogElementBean fields.
                c.calcItemName();
                c.calcItemType();
                
                
            }
            em.getTransaction().commit(); 
            final JPACacheManager cacheManager = ((JPAResourceBean)getBean(Globals.JPA_RESOURCE)).getCacheManager(catalogFacade);
            cacheManager.reset(catalogFacade);
        } catch (FacadeException e) {
            log.severe(e.toString());
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * <p>Fetches the history for a glossary item.</p>
     * @param selectedElement selected glossary item
     * @return list of historical glossary states
     * @throws FacadeException error finding history
     */
    public List<CatalogElementBean> retrieveNarrativeHistory(CatalogElementBean selectedElement) throws FacadeException {
        return retrieveNarrativeHistory(selectedElement.getItemType(), selectedElement.getItemName());
    }

    public List<CatalogElementBean> retrieveNarrativeHistory(String itemType, String itemName) throws FacadeException {
        List<CatalogElementBean> list = new ArrayList<CatalogElementBean>();
        CatalogFacade catalogFacade = 
            (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        List<GlossaryHistory> history = 
            catalogFacade.findHistoryForGlossary(itemType, itemName);

        Glossary latestGlossary = findGlossary(itemType, itemName);

        TreeSet<String> uniqueNarratives = new TreeSet<String>();
        uniqueNarratives.add(latestGlossary.getItemNarrative());
        
        for (GlossaryHistory h: history) {
            // only show updates if only one history item; otherwise, show the original
            // a delete could show up in history if the item was deleted and then recreated
            
            // show the original if more than one row of history
            if ((h.getUpdateDate() == null && history.size() > 1) 
               || (h.getUpdateDate() != null && h.getDeleteDate() == null)) {
               
               if (!uniqueNarratives.contains(h.getItemNarrative())) {
                  list.add(new CatalogElementBean(h));
                  uniqueNarratives.add(h.getItemNarrative());
               }
            }
        }

        if (list.size() == 0) {
           return null;    
        } else {
           return list;
        }
    }

    /**
     * <p>Associates a list of <code>Glossary</code> items to a
     * parent.</p>
     * 
     * @param parent subordinate <code>Glossary</code> 
     * @param children list of <code>Glossary</code> items to assocaite
     * @throws FacadeException error saving new glossary relationships
     */
    public void addItemsToParent(CatalogElementBean parent, 
                                 List<CatalogElementBean> children) throws FacadeException {

      Security user = (Security)getBean(Globals.SECURITY);

        CatalogFacade catalogFacade = 
            (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        EntityManager em = null;

        Glossary.PK[] parentKeys = 
        { new Glossary.PK(parent.getItemType(), parent.getItemName()) };

        try {
            em = catalogFacade.getPersistenceUnit().createEntityManager();
            em.getTransaction().begin();
            for (CatalogElementBean child: children) {
                catalogFacade.persistGlossary(em, user.getUserId(), 
                                              (Glossary)child.getInstance(), 
                                              parentKeys);
            }
            em.getTransaction().commit();
            JPAResourceBean bean = 
                (JPAResourceBean)getBean(Globals.JPA_RESOURCE);
            Map<String, List<GlossaryXRef>> xrefList = bean.getCacheManager((CatalogFacade)getBean(Globals.CATALOG_FACADE)).getGlossaryXwalkTable();
            
            for (CatalogElementBean child: children) {
               buildGlossarySubtree(child, xrefList, child.getItemType(), child.isShowType());
            }
            
            if (parent.getHasChildren()) {
                parent.getChildren().addAll(children);
                Collections.sort(parent.getChildren());
            }
            else {
                parent.setChildren(children);
            }
            
            final JPACacheManager cacheManager = bean.getCacheManager(catalogFacade);
            cacheManager.reset(catalogFacade);
        } catch (FacadeException e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * <p>Deletes a relationship between two <code>Glossary</code> items.</p> 
     * @param item glossary relationship to remove
     * @throws FacadeException error removing relationship
     */
    public void deleteItemFromTree(CatalogElementBean item) throws FacadeException {

      Security user = (Security)getBean(Globals.SECURITY);

        CatalogFacade catalogFacade = 
            (CatalogFacade)getBean(Globals.CATALOG_FACADE);

        CatalogElementBean parent = item.getParent();
        // if parent is null, icon shouldn't have been shown.
        if (item.getParent() == null) {
            log.log(Level.INFO, "deleteItemFromTree improperly called on "+item.toString());
            return;
        }
        catalogFacade.removeGlossaryRelationship(user.getUserId(),  
                                                 parent.getItemType(),
                                                 parent.getItemName(), 
                                                 item.getItemType(),
                                                 item.getItemName());

        if (parent.getHasChildren()) {
            parent.getChildren().remove(item);
        }
        final JPACacheManager cacheManager = ((JPAResourceBean)getBean(Globals.JPA_RESOURCE)).getCacheManager(catalogFacade);
        cacheManager.reset(catalogFacade);
    }


    /**
     * <p>This call returns a full <code>Glossary</code> item if the selected
     * <code>item</code> is a wrapper around the <code>Glossary.PK</code>.  The
     * "ghost" instance is meant to reduce memory by not dragging around the 4K
     * narrative.</p>
     * @param item selected item that has detail focus
     * @return a complete <code>Glossary</code> decorated by the {@link CatalogElementBean}
     * @throws FacadeException error fetching full glossary description
     */
    public CatalogElementBean getCompleteItem(CatalogElementBean item) throws FacadeException {

        CatalogFacade catalogFacade = 
            (CatalogFacade)getBean(Globals.CATALOG_FACADE);

        if (item.getInstance() instanceof Glossary.PK) {
            Glossary g = 
                catalogFacade.findGlossary(item.getItemType(), item.getItemName());
            CatalogElementBean b =  new CatalogElementBean(g, item.isShowType());
            // fixup the parent if one exists
            b.setParent(item.getParent());
            b.setShowingDetail(item.getShowingDetail());
            return b;
        } else {
            return item;
        }
    }
    
    public Glossary findGlossary(String itemType, String itemName) throws FacadeException {
        CatalogFacade catalogFacade = 
            (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        return catalogFacade.findGlossary(itemType, itemName);
    }
    /**
     * <p>Deletes the <code>item</code> and all associated relationships.</p>
     * @param item item to remove
     * @throws FacadeException error removing the item
     */
    public void deleteItem(CatalogElementBean item) throws FacadeException {
        CatalogFacade catalogFacade = 
            (CatalogFacade)getBean(Globals.CATALOG_FACADE);

        Security user = (Security)getBean(Globals.SECURITY);

        catalogFacade.removeGlossary(user.getUserId(), item.getItemType(), item.getItemName());
        
        if (item.getParent() != null && item.getParent().getHasChildren()) {
            item.getParent().getChildren().remove(item);
           
        }

        final JPACacheManager cacheManager = ((JPAResourceBean)getBean(Globals.JPA_RESOURCE)).getCacheManager(catalogFacade);
        cacheManager.reset(catalogFacade);
    }
   
    /**
     * @return an empty {@link CatalogElementBean} that decorates a <code>Glossary</code> entity bean
     */
    public CatalogElementBean getNewItem() {
        Glossary glossary = new Glossary();
        glossary.setPk(new Glossary.PK("Data Element", "New Item"));
        glossary.setItemNarrative("Narrative");
        
        return new CatalogElementBean(glossary);
    }
    
    
    public DictUser getUser(String userid) throws FacadeException {
        CatalogFacade catalogFacade = 
            (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        
        return catalogFacade.findUser(userid);     
    }

    /**
     * <p>Shale ApplicationBean fires the init method as soon as the 
     * object is added to <code>application</code> scope.  We take 
     * advantage of this callback to "prime" the JPA provider.</p>
     */
    public void init() {
        EntityManager em = null;
        try {
            log.finer("Init method called.. initializing cache");
            JPAResourceBean bean = 
                (JPAResourceBean)getBean(Globals.JPA_RESOURCE);
            em = bean.getEntityManager();
            bean.initCache((CatalogFacade)getBean(Globals.CATALOG_FACADE));
        } catch (Exception e) {
            log.log(Level.SEVERE, e.toString(), e);
        } finally {
            if (em != null) {
                em.close();
            }
        }

    }
    
    /**
     * <p>Shale application bean fires the destroy method when the
     * bean is removed from <code>application</code> scope.</p>
     */
    public void destroy() {

        JPAResourceBean bean = 
            (JPAResourceBean)getBean(Globals.JPA_RESOURCE);
        bean.close();   
    }
    
    public void persistFeedback(String comment, String rating) throws FacadeException {
        GlossaryFeedback gf = new GlossaryFeedback();
        gf.setComment(comment);
        gf.setIpAddress(((HttpServletRequest)getExternalContext().getRequest()).getRemoteAddr());
        gf.setRating(rating);
        HttpSession session = (HttpSession)getExternalContext().getSession(false);
        gf.setSession(session.getId());
        CatalogFacade catalogFacade = (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        catalogFacade.persistFeedback(gf);
    }
    public void logUsage(String page, String extra) {
      try {
        GlossaryUsage gu = new GlossaryUsage();
        gu.setPageId(page);
        gu.setPageExtra(extra);
        gu.setIpAddress(((HttpServletRequest)getExternalContext().getRequest()).getRemoteAddr());
        HttpSession session = (HttpSession)getExternalContext().getSession(false);
        gu.setSession(session.getId());
        persistUsage(gu);
      } catch  (FacadeException fe) {
        log.info("FacadeException during logging..."+fe.getMessage());
      }
    }
    public void persistUsage(GlossaryUsage gu) throws FacadeException {
        CatalogFacade catalogFacade = (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        catalogFacade.persistUsage(gu);
    }
    
    public List<StatisticFeedbackUsage> retrieveFeedbackUsage(Date startDate, Date endDate) throws FacadeException {
            List<StatisticFeedbackUsage> list = new ArrayList<StatisticFeedbackUsage>();
            CatalogFacade catalogFacade = 
                (CatalogFacade)getBean(Globals.CATALOG_FACADE);
            list = catalogFacade.findFeedbackUsage(startDate, endDate);

            return list;
    }
        
    public List<StatisticSessionLength> retrieveSessionLength(Date startDate, Date endDate) throws FacadeException {
        List<StatisticSessionLength> list = new ArrayList<StatisticSessionLength>();
        CatalogFacade catalogFacade = 
            (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        list = catalogFacade.findSessionLength(startDate, endDate);
        
        return list;
    }
    
    public List<StatisticSessionPerDay> retrieveSessionPerDay(Date startDate, Date endDate) throws FacadeException {
        List<StatisticSessionPerDay> list = new ArrayList<StatisticSessionPerDay>();
        CatalogFacade catalogFacade = 
            (CatalogFacade)getBean(Globals.CATALOG_FACADE);
        list = catalogFacade.findSessionPerDay(startDate, endDate);

        return list;
    }
}
