package gov.idaho.sboe.jpa.beans;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.*;

/**
 * <p>
 * This entity bean holds a glossary definition.  Each {@link Glossary} is
 * associated with a {@link GlossaryType}.  The {@link GlossaryXwalk} 
 * manages the hierarchical relationships between {@link Glossary} entities.
 * </p>
 */
@NamedQueries( {
        @NamedQuery(name = "findGlossaryChildren", query = "select new gov.idaho.sboe.jpa.beans.Glossary.PK(g.pk.glossType, g.pk.itemName)  from Glossary g, GlossaryXwalk x where g.pk.glossType = x.pk.glossTypeChild and g.pk.itemName = x.pk.itemNameChild and x.pk.glossTypeParent = :glossType and x.pk.itemNameParent = :itemName order by g.pk.itemName"),
        @NamedQuery(name = "findGlossaryParents", query = "select new gov.idaho.sboe.jpa.beans.Glossary.PK(g.pk.glossType, g.pk.itemName) from Glossary g, GlossaryXwalk x where g.pk.glossType = x.pk.glossTypeParent and g.pk.itemName = x.pk.itemNameParent and x.pk.glossTypeChild = :glossType and x.pk.itemNameChild = :itemName order by g.pk.itemName"),
        @NamedQuery(name = "truncateGlossary", query = "delete from Glossary"),
        @NamedQuery(name = "searchGlossaryRolodex", query = "select distinct new gov.idaho.sboe.jpa.beans.Glossary.PK(g.pk.glossType, g.pk.itemName) from Glossary g where g.pk.itemName like :startsWith order by g.pk.itemName"),
        @NamedQuery(name = "searchGlossaryAll", query = "select distinct new gov.idaho.sboe.jpa.beans.Glossary.PK(g.pk.glossType, g.pk.itemName) from Glossary g where g.pk.glossType = :type order by g.pk.itemName")

})
@Entity
@Table(name = "Glossary")
public class Glossary extends AbstractEntityBean implements Serializable,
        Comparable, Cloneable {

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(pk).append(" createDate: ").append(createDate).append(
                " createUserid: ").append(createUserid).append(" effDateEnd: ")
                .append(effDateEnd).append(" effDateStart: ").append(
                        effDateStart).append(" itemNarrative: ").append(
                        itemNarrative).append(" updateDate: ").append(
                        updateDate).append(" updateUserid: ").append(
                        updateUserid);

        return buff.toString();
    }

    /**
     * @return Returns another instance of this object the same state.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Glossary clone = new Glossary();
        clone.pk = (Glossary.PK) (pk != null ? pk.clone() : null);
        clone.createDate = (Date) (createDate != null ? createDate.clone()
                : null);
        clone.createUserid = (createUserid != null ? new String(
                createUserid) : null);
        clone.effDateEnd = (Date) (effDateEnd != null ? effDateEnd.clone()
                : null);
        clone.effDateStart = (Date) (effDateStart != null ? effDateStart
                .clone() : null);
        clone.itemNarrative = (itemNarrative != null ? new String(
                itemNarrative) : null);
        clone.updateDate = (Date) (updateDate != null ? updateDate.clone()
                : null);
        clone.updateUserid = (updateUserid != null ? new String(
                updateUserid) : null);

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
        if (!(obj instanceof Glossary)) {
            return false;
        }
        Glossary comp = (Glossary) obj;

        if (compareTo(this.pk, comp.pk) != 0) {
            return false;
        }
        if (compareTo(this.createDate, comp.createDate) != 0) {
            return false;
        }
        if (compareTo(this.createUserid, comp.createUserid) != 0) {
            return false;
        }
        if (compareTo(this.effDateEnd, comp.effDateEnd) != 0) {
            return false;
        }
        if (compareTo(this.effDateStart, comp.effDateStart) != 0) {
            return false;
        }
        if (compareTo(this.itemNarrative, comp.itemNarrative) != 0) {
            return false;
        }
        if (compareTo(this.updateDate, comp.updateDate) != 0) {
            return false;
        }
        if (compareTo(this.updateUserid, comp.updateUserid) != 0) {
            return false;
        }

        return true;
    }

    /**
     * <p>
     * Compares the state of this object with <code>obj</code>.
     * </p>
     * 
     * @param obj
     *            object to compare against
     * @return returns a value greater than zero if this instance is greater
     *         than <code>obj</code>. A value less than zero if
     *         <code>obj</code> is less than this instance. A value of zero
     *         if the two objects have equal state.
     */
    public int compareTo(Object obj) {
        int order = 0;

        if (obj == null) {
            return -1;
        }
        if (!(obj instanceof Glossary)) {
            return -1;
        }
        Glossary comp = (Glossary) obj;

        order = compareTo(this.pk, comp.pk);
        if (order != 0)
            return order;

        order = compareTo(this.createDate, comp.createDate);
        if (order != 0)
            return order;

        order = compareTo(this.createUserid, comp.createUserid);
        if (order != 0)
            return order;

        order = compareTo(this.effDateEnd, comp.effDateEnd);
        if (order != 0)
            return order;

        order = compareTo(this.effDateStart, comp.effDateStart);
        if (order != 0)
            return order;

        order = compareTo(this.itemNarrative, comp.itemNarrative);
        if (order != 0)
            return order;

        order = compareTo(this.updateDate, comp.updateDate);
        if (order != 0)
            return order;

        order = compareTo(this.updateUserid, comp.updateUserid);
        if (order != 0)
            return order;

        return order;
    }

    @EmbeddedId
    private Glossary.PK pk;

    @Column(name = "EffDateStart", nullable = false)
    private Date effDateStart;

    @Column(name = "CreateDate", nullable = false)
    private Date createDate;

    @Column(name = "EffDateEnd", nullable = true)
    private Date effDateEnd;

    @Column(name = "ItemNarrative", nullable = false, length = 4000)
    private String itemNarrative;

    @Column(name = "UpdateUserId", nullable = true, length = 30)
    private String updateUserid;

    @Column(name = "CreateUserId", nullable = false, length = 30)
    private String createUserid;

    @Column(name = "UpdateDate", nullable = true)
	private Date updateDate;

	private static final long serialVersionUID = 1L;

    public Glossary() {
        super();
    }

    public Glossary.PK getPk() {
        return this.pk;
    }

    public void setPk(Glossary.PK pk) {
        this.pk = pk;
    }

    public Date getEffDateStart() {
        return this.effDateStart;
    }

    public void setEffDateStart(Date ccatEffDateStart) {
        this.effDateStart = ccatEffDateStart;
    }

    public Date getCreateDate() {
        return this.createDate;
    }

    public void setCreateDate(Date ccatCreateDate) {
        this.createDate = ccatCreateDate;
    }

    public Date getEffDateEnd() {
        return this.effDateEnd;
    }

    public void setEffDateEnd(Date ccatEffDateEnd) {
        this.effDateEnd = ccatEffDateEnd;
    }

    public String getItemNarrative() {
        return this.itemNarrative;
    }

    public void setItemNarrative(String ccatItemNarrative) {
        this.itemNarrative = ccatItemNarrative;
    }

    public String getUpdateUserid() {
        return this.updateUserid;
    }

    public void setUpdateUserid(String ccatUpdateUserid) {
        this.updateUserid = ccatUpdateUserid;
    }

    public String getCreateUserid() {
        return this.createUserid;
    }

    public void setCreateUserid(String ccatCreateUserid) {
        this.createUserid = ccatCreateUserid;
    }

    public Date getUpdateDate() {
        return this.updateDate;
    }

    public void setUpdateDate(Date ccatUpdateDate) {
        this.updateDate = ccatUpdateDate;
    }

    @Embeddable
    public static class PK extends AbstractEntityBean implements Serializable,
            Comparable<PK>, Cloneable {
        @Column(name = "GlossType", nullable = false, length = 30)
        private String glossType;

        @Column(name = "ItemName", nullable = false, length = 80)
        private String itemName;

        private static final long serialVersionUID = 1L;

        public PK() {
            super();
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return new PK(glossType, itemName);
        }

        public PK(String ccatGlossType, String ccatItemName) {
            this.glossType = ccatGlossType;
            this.itemName = ccatItemName;
        }

        public String getGlossType() {
            return this.glossType;
        }

        public void setGlossType(String ccatGlossType) {
            this.glossType = ccatGlossType;
        }

        public String getItemName() {
            return this.itemName;
        }

        public void setItemName(String ccatItemName) {
            this.itemName = ccatItemName;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof PK)) {
                return false;
            }
            PK other = (PK) o;
            return ((compareTo(this.glossType, other.glossType) == 0) && (compareTo(
                    this.itemName, other.itemName) == 0));
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        public String toString() {
            StringBuffer buff = new StringBuffer();
            buff.append("glossType: ").append(glossType).append(" itemName: ")
                    .append(itemName);

            return buff.toString();
        }

        public int compareTo(PK other) {
            int order1 = glossType.compareTo(other.glossType);
            if (order1 != 0) {
                return order1;
            }

            return itemName.compareTo(other.itemName);
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
