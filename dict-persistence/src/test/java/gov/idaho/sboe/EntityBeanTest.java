package gov.idaho.sboe;

import java.math.BigDecimal;

import java.util.List;

import junit.framework.TestCase;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.jpa.beans.GlossaryHistory;
import gov.idaho.sboe.jpa.beans.GlossaryType;
import gov.idaho.sboe.jpa.beans.GlossaryXwalk;
import gov.idaho.sboe.jpa.beans.GlossaryXwalkHistory;
import gov.idaho.sboe.jpa.beans.DictUser;
import gov.idaho.sboe.services.CatalogFacade;
import gov.idaho.sboe.services.ValidationException;


/**
 * <p>
 * Tests validation, comparable, comparator and clone implementations.
 * </p>
 */
public class EntityBeanTest extends TestCase {

    public void testValidateGlossary() {
        CatalogFacade catalogFacade = new CatalogFacade();

        Glossary g = new Glossary();
        g.setPk(new Glossary.PK());
        try {
            catalogFacade.validate(g);
            fail("didn't detect Empty Glossary");
        } catch (ValidationException e) {
            List<String> msgs = e.getPropertyMessages("itemName");
            assertNotNull("msgs itemName", msgs);
        }

        Glossary.PK pk = new Glossary.PK();
        pk.setGlossType("01234567890123456789012345678901234567890123456789");
        try {
            catalogFacade.validate(pk);
            fail("didn't catch glossType too large");
        } catch (ValidationException e) {
            List<String> msgs = e.getPropertyMessages("glossType");
            assertNotNull("glossType too large", msgs);
            assertEquals(1, msgs.size());
            assertEquals(
                    "glossType excedes the maximum length of 30 character(s) by 20.",
                    msgs.get(0));
        }

    }

    /**
     * <p>Simple test to make sure the clone method is implemented 
     * correctly.</p>
     * @throws Exception error running test
     */
    public void testGlossaryClone() throws Exception {
        java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());
        
        
        Glossary g1 = new Glossary();
        g1.setPk(new Glossary.PK("Container", "folder"));
        g1.setCreateDate(now);
        g1.setCreateUserid("userId");
        g1.setEffDateEnd(now);
        g1.setEffDateStart(now);
        g1.setItemNarrative("narrative");
        g1.setUpdateDate(now);
        g1.setUpdateUserid("userId");
       
        
        Glossary g2 = (Glossary) g1.clone();
        assertEquals(g1.getPk(), g2.getPk());
        assertEquals(g1.getCreateDate(), g2.getCreateDate());
        assertEquals(g1.getEffDateEnd(), g2.getEffDateEnd());
        assertEquals(g1.getEffDateStart(), g2.getEffDateStart());
        assertEquals(g1.getItemNarrative(), g2.getItemNarrative());
        assertEquals(g1.getUpdateDate(), g2.getUpdateDate());
        assertEquals(g1.getUpdateUserid(), g2.getUpdateUserid());
          
    }

    /**
     * <p>Test the equals implementation.</p>
     *
     * @throws Exception error running test
     */
    public void testGlossaryEquals() throws Exception {
        java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());
             
        Glossary g1 = new Glossary();
        g1.setPk(new Glossary.PK("Container", "folder"));
        g1.setCreateDate(now);
        g1.setCreateUserid("userId");
        g1.setEffDateEnd(now);
        g1.setEffDateStart(now);
        g1.setItemNarrative("narrative");
        g1.setUpdateDate(now);
        g1.setUpdateUserid("userId");
       
        Glossary g2 = new Glossary();
        g2.setPk(new Glossary.PK("Container", "folder"));
        g2.setCreateDate(now);
        g2.setCreateUserid("userId");
        g2.setEffDateEnd(now);
        g2.setEffDateStart(now);
        g2.setItemNarrative("narrative");
        g2.setUpdateDate(now);
        g2.setUpdateUserid("userId");

        assertTrue(g1.equals(g2));
        
        assertFalse(g1.equals(null));
        assertFalse(g1.equals(new Glossary()));
        
        g2.getPk().setGlossType("Data Element");
        assertFalse(g1.equals(g2));

        g1.getPk().setGlossType("Data Element");
        assertTrue(g2.equals(g1));
        
    }

    /**
     * <p>Verify the compareTo method is implemented correcly.</p>
     * @throws Exception error running test
     */
    public void testGlossaryCompareTo() throws Exception {
        java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());
        
        Glossary g1 = new Glossary();
        g1.setPk(new Glossary.PK("Container", "folder"));
        g1.setCreateDate(now);
        g1.setCreateUserid("userId");
        g1.setEffDateEnd(now);
        g1.setEffDateStart(now);
        g1.setItemNarrative("narrative");
        g1.setUpdateDate(now);
        g1.setUpdateUserid("userId");
       
        Glossary g2 = new Glossary();
        g2.setPk(new Glossary.PK("Container", "folder"));
        g2.setCreateDate(now);
        g2.setCreateUserid("userId");
        g2.setEffDateEnd(now);
        g2.setEffDateStart(now);
        g2.setItemNarrative("narrative");
        g2.setUpdateDate(now);
        g2.setUpdateUserid("userId");
   
        assertEquals(0, g1.compareTo(g2));
        
        assertTrue(g1.compareTo(null) < 0);
        assertTrue(g1.compareTo(new String()) < 0);

        g1.setCreateUserid("zzzzzzzzzzzzzzzz");
        assertTrue(g1.compareTo(g2) > 0);
        
    }
    
    /**
     * <p>Test a few methods on the GlossaryHistory entity bean.</p>
     * @throws Exception error running test
     */
    public void testGlossaryHistory() throws Exception {
        java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());
        
        GlossaryHistory g1 = new GlossaryHistory();
        g1.setHistId(new BigDecimal(1));
        g1.setGlossType("Container");
        g1.setItemName("Student");
        g1.setItemNarrative("testing 123");
        g1.setCreateUserid("test user");
        g1.setCreateDate(now);
        
        GlossaryHistory g2 = (GlossaryHistory) g1.clone();
        
        assertTrue(g1.equals(g2));
        assertTrue(g1.compareTo(g2) == 0);
        
        assertEquals(g1.toString(), g2.toString());
        assertEquals(g1.hashCode(), g2.hashCode());
        
        g1.setDeleteUserid("test user");
        
        assertFalse(g1.equals(g2));
        assertFalse(g1.compareTo(g2) == 0);
        
        assertNotSame(g1.toString(), g2.toString());
        assertNotSame(g1.hashCode(), g2.hashCode());   
    }

    /**
     * <p>Test a few methods on the GlossaryType entity bean.</p>
     * @throws Exception error running test
     */
    public void testGlossaryType() throws Exception {

        GlossaryType g1 = new GlossaryType();
        g1.setGlossType("Collection");
        g1.setGlossDesc("Collection");
        

        GlossaryType g2 = (GlossaryType) g1.clone();
        
        assertTrue(g1.equals(g2));
        assertTrue(g1.compareTo(g2) == 0);
        
        assertEquals(g1.toString(), g2.toString());
        assertEquals(g1.hashCode(), g2.hashCode());
        
        g1.setGlossType("test2");

        assertFalse(g1.equals(g2));
        assertFalse(g1.compareTo(g2) == 0);
        
        assertNotSame(g1.toString(), g2.toString());
        assertNotSame(g1.hashCode(), g2.hashCode());
        
    }

    /**
     * <p>Test a few methods on the GlossaryXwalk entity bean.</p>
     * @throws Exception error running test
     */
    public void testGlossaryXwalk() throws Exception {
        java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());

        GlossaryXwalk g1 = new GlossaryXwalk();
        g1.setPk(new GlossaryXwalk.PK());
        g1.getPk().setGlossTypeParent("Container");
        g1.getPk().setItemNameParent("Test Container");
        g1.getPk().setGlossTypeChild("Data Element");
        g1.getPk().setItemNameChild("Test Element");
        
        g1.setCreateDate(now);
        g1.setCreateUserid("test userid");
        
        GlossaryXwalk g2 = (GlossaryXwalk) g1.clone();
        
        assertTrue(g1.equals(g2));
        assertTrue(g1.compareTo(g2) == 0);
        
        assertEquals(g1.toString(), g2.toString());
        assertEquals(g1.hashCode(), g2.hashCode());
        
        g1.setCreateUserid("test4");

        assertFalse(g1.equals(g2));
        assertFalse(g1.compareTo(g2) == 0);
        
        assertNotSame(g1.toString(), g2.toString());
        assertNotSame(g1.hashCode(), g2.hashCode());
        
    }

    /**
     * <p>Test a few methods on the GlossaryXwalkHistory entity bean.</p>
     * @throws Exception error running test
     */
    public void testGlossaryXwalkHistory() throws Exception {
        java.sql.Date now = new java.sql.Date(new java.util.Date().getTime());

        GlossaryXwalkHistory g1 = new GlossaryXwalkHistory();
        g1.setGlossTypeParent("Container");
        g1.setItemNameParent("Test Container");
        g1.setGlossTypeChild("Data Element");
        g1.setItemNameChild("Test Element");
        g1.setHistId(new BigDecimal(1));
        
        g1.setCreateDate(now);
        g1.setCreateUserid("test userid");
        g1.setDeleteDate(now);
        g1.setDeleteUserid("delete user");
        
        GlossaryXwalkHistory g2 = (GlossaryXwalkHistory) g1.clone();
        
        assertTrue(g1.equals(g2));
        assertTrue(g1.compareTo(g2) == 0);
        
        assertEquals(g1.toString(), g2.toString());
        assertEquals(g1.hashCode(), g2.hashCode());
        
        g1.setCreateUserid("test4");

        assertFalse(g1.equals(g2));
        assertFalse(g1.compareTo(g2) == 0);
        
        assertNotSame(g1.toString(), g2.toString());
        assertNotSame(g1.hashCode(), g2.hashCode());

        
    }

    /**
     * <p>Test a few methods on the User entity bean.</p>
     * @throws Exception error running test
     */
    public void testUser() throws Exception {

        DictUser g1 = new DictUser();
        g1.setUserid("test");
        g1.setUserDesc("test desc");
        g1.setUserName("test person");
        
        DictUser g2 = (DictUser) g1.clone();
        
        assertTrue(g1.equals(g2));
        assertTrue(g1.compareTo(g2) == 0);
        
        assertEquals(g1.toString(), g2.toString());
        assertEquals(g1.hashCode(), g2.hashCode());
        
        g1.setUserid("test1");

        assertFalse(g1.equals(g2));
        assertFalse(g1.compareTo(g2) == 0);
        
        assertNotSame(g1.toString(), g2.toString());
        assertNotSame(g1.hashCode(), g2.hashCode());

        
    }

    
}
