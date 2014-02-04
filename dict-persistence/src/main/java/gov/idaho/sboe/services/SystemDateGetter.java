package gov.idaho.sboe.services;

import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

public abstract class SystemDateGetter {
    private static Logger log = Logger.getLogger(SystemDateGetter.class.getName());

    /**
     * @param em
     * @return
     * @throws FacadeException
     */
    public abstract java.sql.Date getSystemDate(EntityManager em) throws FacadeException;
   
    public static class FromOracle extends SystemDateGetter {
        /**
         * <p>Returns the current Database's system date.</p>
         * @param em persistence context
         * @return system date
         */
        public java.sql.Date getSystemDate(EntityManager em) throws FacadeException {
            java.sql.Date now = null;
            try {
               Vector results = (Vector) em.createNativeQuery("select CURRENT_DATE from DUAL").getSingleResult();
               Object d = results.firstElement();
               if (d instanceof java.sql.Date) {
                   now = (java.sql.Date) d;
               } else if (d instanceof java.sql.Timestamp) {
                   now = new java.sql.Date(((java.sql.Timestamp) d).getTime());
               }
            } catch (Exception e) {
               log.warning(AbstractFacade.getMessages().getMessage("systemDate.exception"));
            }
            return now;
        }
    }
    
    public static class FromSqlServer extends SystemDateGetter {
        /**
         * <p>Returns the current Database's system date.</p>
         * @param em persistence context
         * @return system date
         */
        public java.sql.Date getSystemDate(EntityManager em) throws FacadeException {
            java.sql.Date now = null;
            try {
//               Vector results = (Vector) em.createNativeQuery("select CONVERT (date, GETDATE())").getSingleResult();
//               Object d = results.firstElement();
            	Object d = em.createNativeQuery("select CONVERT (date, GETDATE())").getSingleResult();
               if (d instanceof java.sql.Date) {
                   now = (java.sql.Date) d;
               } else if (d instanceof java.sql.Timestamp) {
                   now = new java.sql.Date(((java.sql.Timestamp) d).getTime());
               }
            } catch (Exception e) {
               log.warning(AbstractFacade.getMessages().getMessage("systemDate.exception"));
            }
            return now;
        }
    }
    
    public static class FromJava extends SystemDateGetter {
        /**
         * <p>Returns the current Database's system date.</p>
         * @param em persistence context
         * @return system date
         */
        public java.sql.Date getSystemDate(EntityManager em) throws FacadeException {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new java.util.Date());
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return new java.sql.Date(cal.getTime().getTime());
        }
    }

}
