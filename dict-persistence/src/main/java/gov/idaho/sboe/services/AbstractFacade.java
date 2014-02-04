package gov.idaho.sboe.services;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;

import gov.idaho.sboe.utils.Messages;

/**
 * <p>The base class for creating a simple service facade that is 
 * not an enterprise bean.   The entity manager is not set using
 * annotation injection but setter injection within the managed 
 * bean facility.  
 * <br/>
 * <br/>
 * The {@link gov.idaho.sboe.utils.JPAResourceBean} is a utility 
 * class that manages the <code>persistenceUnit</code>.
 * </p>
 */
public class AbstractFacade {

    /**
     * <p>Java log utility class.</p>
     */
    private static Logger log = Logger.getLogger(AbstractFacade.class.getName());

    /**
     * A system date getter is created to allow one for production using SqlServer,
     * and another using just Java libraries so that getting a SQLException on
     * the bad SqlServer query (when we're not using SqlServer for testing) doesn't
     * hose the transaction.
     */
    private static SystemDateGetter dateGetter = null;
    
    public static void setSystemDateGetter(SystemDateGetter dateGetter) {
        AbstractFacade.dateGetter = dateGetter;
    }

    public static java.sql.Date getSystemDate(EntityManager em) throws FacadeException {
        if (dateGetter == null) {
            setSystemDateGetter(new SystemDateGetter.FromSqlServer());
        }
        return dateGetter.getSystemDate(em);
    }

    /**
     * <p>Message resources for this class.</p>
     */
    private static Messages messages = new Messages("gov.idaho.sboe.services.Bundle", AbstractFacade.class.getClassLoader());

    public static Messages getMessages() {
        return messages;
    }
    
    /**
     * <p>The factory that hides the JPA provide.</p>
     */
    private EntityManagerFactory persistenceUnit = null;

    /**
     * @return the persistentUnit used by the service facade.
     */
    public EntityManagerFactory getPersistenceUnit() {
        return persistenceUnit;
    }

    /**
     * @param persistenceUnit sets the JPA factory used by the service.
     */
    public void setPersistenceUnit(EntityManagerFactory persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }

    
    /**
     * <p>Uses the JPA annotations to perform some simple edits on required
     * fields and length.</p>
     *
     * @param bean JPA entity bean
     * @throws ValidationException contains the detail property exceptions
     */
    public void validate(Object bean) throws ValidationException {
        ValidationException ve = new ValidationException();
       
        validate(bean, ve);
        
        if (ve.getDetailMessages().size() > 0) {
            throw ve;
        }
    }

    /**
     * <p>Recursively inspects the <code>bean</code> for any JPA 
     * <code>Column</code>, <code>Id</code> or <code>EmbeddedId</code>
     * annotations that can be used to validate property values before
     * invoking the <code>persist</code>.</p> 
     * @param bean object having JPA annotated properties
     * @param ve collection of messages for each property
     */
    private void validate(Object bean, ValidationException ve) {
        try {            
            for (Field field: bean.getClass().getDeclaredFields()) {
                Id id = field.getAnnotation(Id.class);
                Column column = field.getAnnotation(Column.class);

                field.setAccessible(true);
                Object value = field.get(bean);
                if (column != null && id == null) {
                    if (!column.nullable() && value == null) {

                        ve.setPropertyMessage(field.getName(), 
                                messages.getMessage("validate.required", new Object[] {field.getName()}));
                    } else if (value instanceof String && column.length() > 0) {
                        int valueLen = ((String) value).length(); 
                        if (valueLen > column.length()) {
                            ve.setPropertyMessage(field.getName(), 
                                    messages.getMessage("validate.maxlength", 
                                            new Object[] {field.getName(), column.length(), valueLen - column.length()}));                            
                        }
                    }
                }

                EmbeddedId embededId = field.getAnnotation(EmbeddedId.class);
                if (embededId != null && value != null) {
                    field.setAccessible(true);
                    validate(value, ve); 
                }

            }

        } catch (Exception e) {
            log.severe(e.toString());  
        }

    }
}
