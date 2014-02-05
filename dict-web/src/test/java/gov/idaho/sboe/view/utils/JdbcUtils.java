package gov.idaho.sboe.view.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.persistence.EntityManager;

import javax.persistence.Query;

import gov.idaho.sboe.jpa.beans.Glossary;
import gov.idaho.sboe.view.backing.Security;

public class JdbcUtils {

    public static int insertItem(EntityManager em, Glossary g, Security user) {
        em.getTransaction().begin();
        Query q = em.createNativeQuery(
            "insert into Glossary" +
            "(ItemNarrative, EffDateStart, CreateDate, ItemName, GlossType, CreateUserId)" +
            " VALUES("+
                "'" + g.getItemNarrative() + "'," +
                " GETDATE(), " +
                " GETDATE()," +
                "'" + g.getPk().getItemName() + "'," +
                "'" + g.getPk().getGlossType() + "'," +
                "'" + user.getUserId()+ "'" +
            ")");
        int updateCount = q.executeUpdate();
        em.getTransaction().commit();
        return updateCount;
    }


    public static int insertReference(EntityManager em, Glossary.PK pkP, Glossary.PK pkC) {
        em.getTransaction().begin();
        Query q = em.createNativeQuery(
            "insert into GlossaryXwalk" +
            "(CreateDate, CreateUserId, ItemNameParent, GlossTypeParent, ItemNameChild, GlossTypeChild)" +
            " VALUES("+
                " GETDATE(), 'SYSTEM', " +
                "'" + pkP.getItemName() + "'," +
                "'" + pkP.getGlossType() + "'," +
                "'" + pkC.getItemName() + "'," +
                "'" + pkC.getGlossType() + "'" +
            ")");
        int updateCount = q.executeUpdate();
        em.getTransaction().commit();
        return updateCount;
    }

    public static void deleteItemAndReferences(EntityManager em, Glossary.PK pk) {
        em.getTransaction().begin();
        int updateCount = em.createNativeQuery(
            "delete from Glossary where ItemName='" + pk.getItemName()+
            "' and GlossType='" + pk.getGlossType() +
            "'").executeUpdate();
        updateCount = em.createNativeQuery(
            "delete from GlossaryXwalk where " +
                "(ItemNameParent='" + pk.getItemName()+ "' and GlossTypeParent='" + pk.getGlossType() + "') or " +
                "(ItemNameChild='" + pk.getItemName()+ "' and GlossTypeChild='" + pk.getGlossType() +"')").executeUpdate();
        em.getTransaction().commit();
    }

    public static List<Glossary.PK> existsParentReferences(EntityManager em, Glossary.PK pk) {
        List query = em.createNativeQuery(
            "select ItemNameChild,GlossTypeChild " + 
            "from GlossaryXwalk where ItemNameParent='" + pk.getItemName()+
            "' and GlossTypeParent='" + pk.getGlossType() + "'").
            getResultList();
        if (query == null) return null;
        List<Glossary.PK> results = new ArrayList<Glossary.PK>();
        java.util.Iterator iter = query.iterator();
        while (iter.hasNext()) {
            Vector<String> item = (Vector<String>)iter.next();
            results.add(new Glossary.PK(item.get(1), item.get(0)));
        }
        return results;
    }

    public static boolean existsReference(EntityManager em, Glossary.PK pkP, Glossary.PK pkC) {
        List query = em.createNativeQuery(
            "select ItemNameChild,GlossTypeChild " + 
            "from GlossaryXwalk " +
            "where ItemNameParent='" + pkP.getItemName()+
            "' and GlossTypeParent='" + pkP.getGlossType() + "'" +
            "  and ItemNameChild='" + pkC.getItemName()+
            "' and GlossTypeChild='" + pkC.getGlossType() + "'").
           getResultList();
        if (query == null) return false;
        return query.size()>0;
    }

    public static List<Glossary.PK> existsItem(EntityManager em, Glossary.PK pk) {
        List query = em.createNativeQuery(
            "select ItemName,GlossType " + 
            "from Glossary where ItemName='" + pk.getItemName()+
            "' and GlossType='" + pk.getGlossType() + "'").
            getResultList();
        if (query == null) return null;
        List<Glossary.PK> results = new ArrayList<Glossary.PK>();
        java.util.Iterator iter = query.iterator();
        while (iter.hasNext()) {
        	/* jeffk - Receiving ClassCastException when casting the iter Object to a Vector	
        	Vector<String> item = (Vector<String>)iter.next();
            results.add(new Glossary.PK(item.get(1), item.get(0))); */
            Object[] item = (Object[])iter.next();
            results.add(new Glossary.PK(item[1].toString(), item[0].toString()));
        }
        return results;
    }
}
