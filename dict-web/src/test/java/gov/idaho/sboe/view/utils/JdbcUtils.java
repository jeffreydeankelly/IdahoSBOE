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
            "insert into CCAT_GLOSSARY" +
            "(ItemNarrative, EffDateStart, CreateDate, ItemName, GlossType, CreateUserId)" +
            " VALUES("+
                "'" + g.getItemNarrative() + "'," +
                " SYSDATE, " +
                " SYSDATE," +
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
            "insert into CCAT_GLOSSARY_XWALK" +
            "(CreateDate, CreateUserId, ItemNameParent, GlossTypeParent, ItemNameChild, GlossTypeChild)" +
            " VALUES("+
                " SYSDATE, 'SYSTEM', " +
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
            "delete from CCAT_GLOSSARY where ItemName='" + pk.getItemName()+
            "' and GlossType='" + pk.getGlossType() +
            "'").executeUpdate();
        updateCount = em.createNativeQuery(
            "delete from CCAT_GLOSSARY_XWALK where " +
                "(ItemNameParent='" + pk.getItemName()+ "' and GlossTypeParent='" + pk.getGlossType() + "') or " +
                "(ItemNameChild='" + pk.getItemName()+ "' and GlossTypeChild='" + pk.getGlossType() +"')").executeUpdate();
        em.getTransaction().commit();
    }

    public static List<Glossary.PK> existsParentReferences(EntityManager em, Glossary.PK pk) {
        List query = em.createNativeQuery(
            "select ItemNameChild,GlossTypeChild " + 
            "from CCAT_GLOSSARY_XWALK where ItemNameParent='" + pk.getItemName()+
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
            "from CCAT_GLOSSARY_XWALK " +
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
            "from CCAT_GLOSSARY where ItemName='" + pk.getItemName()+
            "' and GlossType='" + pk.getGlossType() + "'").
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
}
