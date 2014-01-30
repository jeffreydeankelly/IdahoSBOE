package gov.idaho.sboe.jpa.beans;

/**
 * This is an unmapped class simply for convenience in use in CatalogDataAccessHelper
 * where we want a class to put in a list.  GlossaryXwalk would be ok if I could
 * figure out how to get JPA to build embedded Glossary.PKs as part of the
 * GlossaryXwalk.PK
 */
public class GlossaryXRef 
implements Comparable<GlossaryXRef>, Cloneable {
    public GlossaryXRef() {
    }

    private Glossary.PK parent;
    private Glossary.PK child;

    public GlossaryXRef(String ccatGlossType, String ccatItemName, String childGlossType, String childItemName) {
        super();
        parent = new Glossary.PK(ccatGlossType, ccatItemName);
        child = new Glossary.PK(childGlossType, childItemName);
    }
    public int compareTo(GlossaryXRef comp) {
        if (comp == null) {
            return -1;
        }
        int order = getParent().compareTo(comp.getParent());
         if (order != 0) {
             return order;
         }
        return getChild().compareTo(comp.getChild());
    }
    public Object clone() throws CloneNotSupportedException {
        GlossaryXRef clone = new GlossaryXRef();
        clone.parent = (Glossary.PK)getParent().clone();
        clone.child = (Glossary.PK)getChild().clone();
        return clone;
    }
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof GlossaryXRef)) {
            return false;
        }
        GlossaryXRef other = (GlossaryXRef) o;
        return parent.compareTo(other.parent) == 0 &&
            child.compareTo(other.child) == 0;
    }
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Parent: ").append(parent.toString()).
            append("Child: ").append(child.toString());
        return buff.toString();
    }

    public Glossary.PK getParent() {
        return parent;
    }

    public Glossary.PK getChild() {
        return child;
    }
}
