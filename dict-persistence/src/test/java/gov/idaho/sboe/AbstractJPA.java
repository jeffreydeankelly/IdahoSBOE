package gov.idaho.sboe;

import junit.framework.TestCase;

import gov.idaho.sboe.services.AbstractFacade;
import gov.idaho.sboe.services.SystemDateGetter;
import gov.idaho.sboe.utils.JPAResourceBean;

/**
 * <p>This is an abstract class that JPA related test cases should
 * extend.  It creates a resource bean that that gives access
 * to a JPA persistence context and persistence unit.</p> 
 */
public class AbstractJPA extends TestCase {

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

    /**
     * <p>Creates a {@link us.co.cde.jpa.utils.JPAResourceBean} using
     * the <code>persistenceUnitName</code>.  The default persistence
     * unit name is "default".</p>
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        String unitName = persistenceUnitName;
        if (unitName == null) {
            unitName = "default";
        }

        if (resourceBean == null) {
            resourceBean = new JPAResourceBean(unitName);
        }
        AbstractFacade.setSystemDateGetter(new SystemDateGetter.FromJava());
    }
    
    /**
      * <p>Closes the JPA resources held by <code>resourceBean</code>.</p>
      */
    @Override
     public void tearDown() throws Exception {
         if (resourceBean != null) {
            resourceBean.close();
            resourceBean = null;
         }
         super.tearDown();
     }

}
