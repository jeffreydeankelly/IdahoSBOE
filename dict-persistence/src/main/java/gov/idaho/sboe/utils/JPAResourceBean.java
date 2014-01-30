package gov.idaho.sboe.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import gov.idaho.sboe.services.CatalogFacade;

/**
 * <p>This is a utility wrapper class around some of the JPA
 * API.  It provides simple bean methods for getting at the
 * EntityManagerFactor and EntityManager.  It is designed to be
 * a managed bean.  Since it is bean friendly, we can us the 
 * property setter injection of the managed bean facility to
 * inject a persistence unit into a service facade that is
 * also a managed bean.</p>
 */
public class JPAResourceBean {

    /**
     * <p>
     * Java log utility class.
     * </p>
     */
    private static Logger log = Logger.getLogger(JPAResourceBean.class
            .getName());

    /**
     * <p>
     * Utility to load messages from a resource bundle.
     * </p>
     */
    private static final Messages messages = new Messages(
            "gov.idaho.sboe.tools.Bundle", JPAResourceBean.class
                    .getClassLoader());

    /**
     * <p>Default constructor.</p>
     */
    public JPAResourceBean() {

    }

    /**
     * @param persistenceUnitName name of the target persistence unit
     */
    public JPAResourceBean(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    /**
     * <p>A cached instance of the JPA persistence unit.</p>
     */
    private EntityManagerFactory emf = null;

    /**
     * <p>The name of the persistence unit that is registered in the
     * "/META-INF/persistence.xml".</p>
     */
    private String persistenceUnitName = "default";

    /**
     * <p>Custom JPA connection properties used when creating a
     * <code>EntityManagerFactory</code>.</p>
     */
    private Map<String, String> properties = new TreeMap<String, String>();

    /**
     * <p>
     * The name of a properties file that should be loaded from the class path.
     * These values will override the values in the persistence.xml.
     * </p>
     */
    private String propertiesFilename = null;

    /**
     * @return the JPA connection properties 
     */
    public Map getProperties() {
        return properties;
    }

    /**
     * @param properties sets the JPA connection properties
     */
    public void setProperties(Map<String, String> properties) {

        if (this.properties != null) {
            this.properties.putAll(properties);
        } else {
            this.properties = properties;
        }
        
        this.isPropertiesLoaded = false;
    }


    /**
     * <p>Internal toggle flag to indicate an external properties file
     * has been loaded.</p>
     */
    private boolean isPropertiesLoaded = false;
    
    /**
     * <p>Loads the <code>propertiesFiilename</code> into the <code>properties</code>
     * collection.  It trys to load from the class path first and then from the file
     * system.  Once loaded, a flag <code>isPropertiesLoaded</code> is set to 
     * indicate that the file has been loaded.  Setting the propertiesFilename
     * or the properties attributes will revert the internal flag so that it will 
     * be loaded again the next call to <code>getFactory</code>.
     * </p>
     */
    private void mergeProperties() {

        // try to load an external properties file
        if (propertiesFilename != null && !isPropertiesLoaded) {

            // check the classpath
            URL url = this.getClass().getClassLoader().getResource(
                    propertiesFilename);
            if (url == null) {
                // try file system
                try {
                    url = new URL("file:///" + propertiesFilename);
                } catch (Exception e) {

                }
            }

            if (url != null) {
                Properties externalProps = new Properties();
                InputStream in = null;
                try {
                    in = url.openStream();
                    externalProps.load(in);
                    if (this.properties == null) {
                        this.properties = new TreeMap<String, String>();
                    }
                    for (Map.Entry e : externalProps.entrySet()) {
                        properties.put((String) e.getKey(), (String) e
                                .getValue());
                    }

                } catch (IOException e) {
                    log.severe(messages.getMessage(
                            "jpaloadproperties.exception", new Object[] {
                                    propertiesFilename, e.toString() }));
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }

    }

    /**
     * <p>Uses the <code>persistenceUnitName</code> and the 
     * <code>properties</code> to establish a JPA persistence
     * unit.</p>
     *
     * @return JPA persistence unit
     */
    public EntityManagerFactory getFactory() {

        mergeProperties();
        
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory(persistenceUnitName,
                    properties);
        }

        return emf;
    }

    /**
     * @param persistenceUnitName name used to establish a <code>EntityManagerFactory</code>
     */
    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    /**
     * @return name used to establish a <code>EntityManagerFactory</code>
     */
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    /**
     * <p>Invokes the <code>close</code> method on the <code>EntityManagerFactory</code>
     */
    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }

    /**
     * @return JPA persistence context established from the <code>factory</code>
     */
    public EntityManager getEntityManager() {
        return getFactory().createEntityManager();
    }

    /**
     * @return name of a properties file that can be loaded from the classpath
     * or the file system.  It is used to establish a <code>EntityManagerFactory</code>.
     */
    public String getPropertiesFilename() {
        return propertiesFilename;
    }

    /**
     * @param propertiesFilename name of a properties file that can be loaded from the classpath
     * or the file system.  It is used to establish a <code>EntityManagerFactory</code>.
     */
    public void setPropertiesFilename(String propertiesFilename) {
        this.propertiesFilename = propertiesFilename;
        this.isPropertiesLoaded = false;
    }

    private JPACacheManager bank = null;
    
    public void initCache(CatalogFacade catalogFacade) {
        if (bank == null) {
            bank = new JPACacheManager();
            bank.init(catalogFacade);
        }
    }
    
    public JPACacheManager getCacheManager(CatalogFacade catalogFacade) {
        initCache(catalogFacade);
        return bank;
    }
}
