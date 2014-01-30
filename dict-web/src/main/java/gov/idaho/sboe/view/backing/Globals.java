package gov.idaho.sboe.view.backing;

/**
 * <p>Class to hold constants used by the view tier.</p>
 */
public class Globals {

    /**
     * <p>Name of the registered managed bean that holds the current
     * working set used by the Catalog view.</p>
     */
    public static final String SCRATCH_PAD = "us$co$cde$ccat$ScratchPad";
    
    /**
     * <p>An application scoped managed bean that delegates
     * to the resource layer.</p>
     */
    public static final String DATA_ACCESS_HELPER = "us$co$cde$ccat$DataAccessHelper";


    /**
     * <p>The logical service layer that wrappers many of the calls to
     * the data access layer.</p>
     */
    public static final String CATALOG_FACADE = "us$co$cde$ccat$CatalogFacade";
    
    
    /**
     * <p>The locical service layer that handles the indexing and searching
     * for <code>Glossary</code> items.</p>
     */
    public static final String INDEXING_FACADE = "us$co$cde$ccat$IndexingFacade";
    
    
    /**
     * <p>JPA persistence unit bean kept in application scope.</p>
     */
    public static final String JPA_RESOURCE = "us$co$cde$ccat$JpaResource";
    
    
    /**
     * <p>The <code>User</code> entity bean for an authenticated internal 
     * staff user.</p>
     */
    public static final String AUTHENTICATED_USER = "us$co$cde$ccat$authenticatedUser";

    /**
     * <p>The key used to collect the viewId prior to redirecting to the 
     * login page.</p>
     */
    public static final String RETURN_VIEWID = "us$co$cde$ccat$ReturnViewId";
    
    /**
     * <p>The default viewId that successful login will be directed to if
     * the <code>RETURN_VIEW_ID</code> doesn't have a value.</p>
     */
    public static final String DEFAULT_RETURN_VIEWID = "/welcome.jspx";
    
    /**
     * <p>Managed bean name (Shale ViewController) for the exceptions page.</p>
     */
    public static final String SHOW_EXCEPTION = "showexception";
    
    /**
     * <p>The viewId of the login in page.  This is used for programmatic navigation
     * to the login page in the event that an operation is attempted and the user
     * has lost their session due to a timeout.</p>
     */
    public static final String LOGIN_VIEWID = "/login.jspx";
    
    /**
     * <p>The viewId of the feedback page.  This is used for programmatic navigation
     * to the feedback page.</p>
     */
    public static final String FEEDBACK_VIEWID = "/feedback.jspx";
    public static final String COLLECTIONS_VIEWID = "/collection.jspx";
    public static final String DATAELEMENTS_VIEWID = "/rolodex.jspx";
    
    /**
     * <p>Managed bean name of the security helper.</p>
     */
    public static final String SECURITY = "security";
    
    /**
     * <p>Managed bean name of the feedback helper.</p>
     */
    public static final String FEEDBACK = "feedback";
    
    /**
     * <p>These <code>GlossaryType</code>'s should only display
     * "Data Elements".  This is a visual filter.  There is nothing
     * in the <code>GlossaryXwalk</code> to prevent the relationship.</p>
     */
    public static final String[] CONTAINER_TYPES = {"Collection", "Business Rule", "Report"};
    
    /**
     * <p>The <code>GlossaryType</code> code for the "Data Element".</p>
     */
    public static final String DATA_ELEMENT_TYPE = "Data Element";
    
    /**
     * <p>The <code>GlossaryType</code> code for the "Business Rule".</p>
     */
    public static final String BUSINESS_RULE = "Business Rule";
   
    /**
     * <p>The <code>GlossaryType</code> code for the "Collection".</p>
     */
    public static final String COLLECTIONS_TYPE = "Collection";
    
    /**
     * <p>The <code>GlossaryType</code> code for the "Collection Rule".</p>
     */
    public static final String COLLECTIONS_RULE_TYPE = "Collection Rule";
    
    /**
     * <p>The <code>GlossaryType</code> code for the "Report".</p>
     */
    public static final String REPORT = "Report";
    
    /**
     * <p>The <code>PageId</code> code for the search function.</p>
     */
    public static final String PAGE_SEARCH = "S";
    /**
     * <p>The <code>PageId</code> code for the tab menu.</p>
     */
    public static final String PAGE_TAB = "T";
    /**
     * <p>The <code>PageId</code> code for the reference.</p>
     */
    public static final String PAGE_REFERENCE = "R";
    /**
     * <p>The <code>PageId</code> code for the clear function.</p>
     */
    public static final String PAGE_CLEAR = "C";

    public static final String ROLODEX_FILTER = "rolodexFilter";
}
