package gov.idaho.sboe.jpa.beans;

/**
 * <p>
 * This entity bean represents a page hit.  Because all we do is insert records,
 * this is not a full-function, first-class JPA bean.  It is more of just a 
 * data structure POJO.
 * 
 * HttpSession session = (HttpSession)context.getExternalContext().getSession(false);
 *  session.getId()
 *  CREATE TABLE GlossaryUsageStatistics
(
GlossaryUsagePage VARCHAR2(1) NOT NULL,
GlossaryUsageExtra VARCHAR2(200),
GlossaryUsageIP VARCHAR2(16) NOT NULL,
GlossaryUsageSession VARCHAR2(256) NOT NULL,
GlossaryUsageTimestamp TIMESTAMP NOT NULL
)
;

 * </p>
 */

//Entity
//Table(name = "GlossaryUsageStatistics")
public class GlossaryUsage 
//extends AbstractEntityBean implements Serializable,Comparable, Cloneable 
{
    public GlossaryUsage() {
    }

    // 'H','S','T','L'...
//    @Column(name = "CCAT_USAGE_PAGE", nullable = false, length = 1)
    private String pageId;

     // the tab letter, search term, etc.
//    @Column(name = "CCAT_USAGE_EXTRA", nullable = false, length = 200)
    private String pageExtra;

//    @Column(name = "CCAT_USAGE_IP", nullable = false, length = 16)
    private String ipAddress;

//    @Column(name = "CCAT_USAGE_SESSION", nullable = false, length = 256)
    private String session;

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageExtra(String pageExtra) {
        this.pageExtra = pageExtra;
    }

    public String getPageExtra() {
        return pageExtra;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getSession() {
        return session;
    }
}
