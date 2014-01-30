package gov.idaho.sboe.jpa.beans;

/**
 * <p>
 * This entity bean represents a feedback item. Because all we do is insert records,
 * this is not a full-function, first-class JPA bean.  It is more of just a 
 * data structure POJO.
 * 
 * CREATE TABLE CCAT_GLOSSARY_USER_RATING
(
CCAT_GLOSSARY_USER_IP VARCHAR2(16) NOT NULL,
CCAT_GLOSSARY_USER_RATING VARCHAR2(1) NOT NULL,
CCAT_GLOSSARY_USER_COMMENT VARCHAR2(4000),
CCAT_GLOSSARY_USER_TIMESTAMP TIMESTAMP NOT NULL,
CCAT_GLOSSARY_USER_SESSION VARCHAR2(256)
)
;
 * </p>
 */
//@Entity
//@Table(name = "CCAT_GLOSSARY_USER_RATING")
public class GlossaryFeedback
//extends AbstractEntityBean implements Serializable, Comparable, Cloneable 
{
    public GlossaryFeedback() {
    }

    // the tab letter, search term, etc.
//    @Column(name = "CCAT_GLOSSARY_USER_RATING", nullable = false, length = 1)
    private String rating;

    // the tab letter, search term, etc.
//    @Column(name = "CCAT_GLOSSARY_USER_COMMENT", nullable = true, length = 4096)
    private String comment;

//    @Column(name = "CCAT_GLOSSARY_USER_IP", nullable = false, length = 16)
    private String ipAddress;

//    @Column(name = "CCAT_GLOSSARY_USER_SESSION", nullable = true, length = 256)
    private String session;

    public void setSession(String session) {
        this.session = session;
    }

    public String getSession() {
        return session;
    }
    
    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRating() {
        return rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
