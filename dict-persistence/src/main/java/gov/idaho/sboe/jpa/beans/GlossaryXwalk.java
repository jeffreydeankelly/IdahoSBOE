package gov.idaho.sboe.jpa.beans;

import java.io.Serializable;
import java.sql.Date;
import javax.persistence.*;

/**
 * <p>
 * This entity bean defines the hierarchical relationships between
 * {@link Glossary} definitions.
 * </p>
 */
@NamedQueries( {
        @NamedQuery(name = "findGlossaryXwalkForParent", query = "select x from GlossaryXwalk x where x.pk.glossTypeParent = :glossType and x.pk.itemNameParent = :itemName order by x.pk.itemNameParent"),
        @NamedQuery(name = "findGlossaryXwalkForChild", query = "select x from GlossaryXwalk x where x.pk.glossTypeChild = :glossType and x.pk.itemNameChild = :itemName order by x.pk.itemNameChild"),
        @NamedQuery(name = "loadGlossaryXwalk", query = "select x from GlossaryXwalk x"),
//        @NamedQuery(name = "loadGlossaryXwalkType", query = "select x from GlossaryXwalk x where x.pk.glossTypeParent = :glossType order by x.pk.itemNameParent"),
        @NamedQuery(name = "loadGlossaryXwalkType", 
            query = "select " +
            "new gov.idaho.sboe.jpa.beans.GlossaryXRef(" +
                "x.pk.glossTypeParent, x.pk.itemNameParent, x.pk.glossTypeChild, x.pk.itemNameChild) " +
            "from GlossaryXwalk x where x.pk.glossTypeParent = :glossType order by x.pk.itemNameParent"),
        @NamedQuery(name = "truncateGlossaryXwalk", query = "delete from GlossaryXwalk") })
@Entity
@Table(name = "GlossaryXwalk")
public class GlossaryXwalk extends AbstractEntityBean implements Serializable,
        Comparable<GlossaryXwalk>, Cloneable {
    @EmbeddedId
    private GlossaryXwalk.PK pk;

    /**
     * <p>
     * Compares the state of this object with <code>obj</code>.
     * </p>
     * 
     * @param comp
     *            object to compare against
     * @return returns a value greater than zero if this instance is greater
     *         than <code>obj</code>. A value less than zero if
     *         <code>obj</code> is less than this instance. A value of zero
     *         if the two objects have equal state.
     */
    public int compareTo(GlossaryXwalk comp) {
        if (comp == null) {
            return -1;
        }
        int order = compareTo(this.createDate, comp.createDate);
        if (order != 0) {
            return order;
        }
        return compareTo(this.createUserid, comp.createUserid);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        GlossaryXwalk clone = new GlossaryXwalk();
        clone.setPk((PK) (pk != null ? pk.clone() : null));
        clone.createDate = this.createDate;
        clone.createUserid = this.getCreateUserid();
        return clone;
    }

    /**
     * @param obj
     *            object to compare against
     * @return if the <code>obj</code> has the same state as this object,
     *         returns <code>true</code>; otherwise, returns
     *         <code>false</code>
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GlossaryXwalk)) {
            return false;
        }
        GlossaryXwalk comp = (GlossaryXwalk) obj;

        return compareTo(this.pk, comp.pk) == 0
                && compareTo(this.createDate, comp.createDate) == 0
                && compareTo(this.createUserid, comp.createUserid) == 0;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(pk).append(" createDate: ").append(createDate).append(
                " createUserid: ").append(createUserid);

        return buff.toString();
    }

    @Column(name = "CreateUserId", nullable = false, length = 30)
    private String createUserid;

    @Column(name = "CreateDate", nullable = false)
    private Date createDate;

    private static final long serialVersionUID = 1L;

    public GlossaryXwalk() {
        super();
    }

    public GlossaryXwalk.PK getPk() {
        return this.pk;
    }

    public void setPk(GlossaryXwalk.PK pk) {
        this.pk = pk;
    }

    public String getCreateUserid() {
        return this.createUserid;
    }

    public void setCreateUserid(String ccatCreateUserid) {
        this.createUserid = ccatCreateUserid;
    }

    public Date getCreateDate() {
        return this.createDate;
    }

    public void setCreateDate(Date ccatCreateDate) {
        this.createDate = ccatCreateDate;
    }

    @Embeddable
    public static class PK extends AbstractEntityBean implements Serializable,
            Comparable<PK>, Cloneable {
        @Column(name = "ItemNameParent", nullable = false, length = 80)
        private String itemNameParent;

        @Column(name = "ItemNameChild", nullable = false, length = 80)
        private String itemNameChild;

        @Column(name = "GlossTypeParent", nullable = false, length = 30)
        private String glossTypeParent;

        @Column(name = "GlossTypeChild", nullable = false, length = 30)
        private String glossTypeChild;

        private static final long serialVersionUID = 1L;

        public PK() {
            super();
        }

        public PK(String GlossType, String ItemName, String childGlossType, String childItemName) {
            super();
            this.glossTypeParent = GlossType;
            this.itemNameParent = ItemName;
            this.glossTypeChild = childGlossType;
            this.itemNameChild = childItemName;
        }

        /**
         * <p>
         * Compares the state of this object with <code>obj</code>.
         * </p>
         * 
         * @param comp
         *            object to compare against
         * @return returns a value greater than zero if this instance is greater
         *         than <code>obj</code>. A value less than zero if
         *         <code>obj</code> is less than this instance. A value of zero
         *         if the two objects have equal state.
         */
        public int compareTo(PK comp) {
            int order = glossTypeParent.compareTo(comp.glossTypeParent);
            if (order != 0) {
                return order;
            }
            order = itemNameParent.compareTo(comp.itemNameParent);
            if (order != 0) {
                return order;
            }
            order = glossTypeChild.compareTo(comp.glossTypeChild);
            if (order != 0) {
                return order;
            }
            return itemNameChild.compareTo(comp.itemNameChild);
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            PK clone = new PK();
            clone.glossTypeParent = this.glossTypeParent;
            clone.itemNameParent = this.itemNameParent;
            clone.glossTypeChild = this.glossTypeChild;
            clone.itemNameChild = this.itemNameChild;

            return clone;
        }

        public String getItemNameParent() {
            return this.itemNameParent;
        }

        public void setItemNameParent(String ccatItemNameParent) {
            this.itemNameParent = ccatItemNameParent;
        }

        public String getItemNameChild() {
            return this.itemNameChild;
        }

        public void setItemNameChild(String ccatItemNameChild) {
            this.itemNameChild = ccatItemNameChild;
        }

        public String getGlossTypeParent() {
            return this.glossTypeParent;
        }

        public void setGlossTypeParent(String ccatGlossTypeParent) {
            this.glossTypeParent = ccatGlossTypeParent;
        }

        public String getGlossTypeChild() {
            return this.glossTypeChild;
        }

        public void setGlossTypeChild(String ccatGlossTypeChild) {
            this.glossTypeChild = ccatGlossTypeChild;
        }

        /**
         * @param o
         *            object to compare against
         * @return if the <code>obj</code> has the same state as this object,
         *         returns <code>true</code>; otherwise, returns
         *         <code>false</code>
         */
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof PK)) {
                return false;
            }
            PK other = (PK) o;
            return compareTo(this.itemNameParent, other.itemNameParent) == 0
                    && compareTo(this.itemNameChild, other.itemNameChild) == 0
                    && compareTo(this.glossTypeParent, other.glossTypeParent) == 0
                    && compareTo(this.glossTypeChild, other.glossTypeChild) == 0;
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public String toString() {
            StringBuffer buff = new StringBuffer();
            buff.append("glossTypeParent: ").append(glossTypeParent).append(
                    " itemNameParent: ").append(itemNameParent).append(
                    " glossTypeChild: ").append(glossTypeChild).append(
                    " itemNameChild: ").append(itemNameChild);

            return buff.toString();
        }
    }

}
