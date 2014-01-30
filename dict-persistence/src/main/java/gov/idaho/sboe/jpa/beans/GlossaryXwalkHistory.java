package gov.idaho.sboe.jpa.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import javax.persistence.*;

/**
 * <p>
 * This entity bean records changes to the {@link GlossaryXwalk} relationships.
 * </p>
 */
@NamedQueries( {
        @NamedQuery(name = "findGlossaryXwalkHistory", query = "select h from GlossaryXwalkHistory h where h.glossTypeParent = :glossTypeParent and h.itemNameParent = :itemNameParent and h.glossTypeChild = :glossTypeChild and h.itemNameChild = :itemNameChild"),
        @NamedQuery(name = "truncateGlossaryXwalkHistory", query = "delete from GlossaryXwalkHistory") })
@Entity
@Table(name = "GlossaryXwalkHistory")
public class GlossaryXwalkHistory extends AbstractEntityBean implements
        Serializable, Comparable, Cloneable {
        
    /**
     * <p>
     * Compares the state of this object with <code>obj</code>.
     * </p>
     * 
     * @param o
     *            object to compare against
     * @return returns a value greater than zero if this instance is greater
     *         than <code>obj</code>. A value less than zero if
     *         <code>obj</code> is less than this instance. A value of zero
     *         if the two objects have equal state.
     */
    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }
        if (!(o instanceof GlossaryXwalkHistory)) {
            return -1;
        }
        GlossaryXwalkHistory comp = (GlossaryXwalkHistory) o;
        int order = compareTo(this.histId, comp.histId);
        if (order != 0) {
            return order;
        }
        order = compareTo(this.glossTypeParent, comp.glossTypeParent);
        if (order != 0) {
            return order;
        }
        order = compareTo(this.itemNameParent, comp.itemNameParent);
        if (order != 0) {
            return order;
        }
        order = compareTo(this.glossTypeChild, comp.glossTypeChild);
        if (order != 0) {
            return order;
        }
        order = compareTo(this.itemNameChild, comp.itemNameChild);
        if (order != 0) {
            return order;
        }

        order = compareTo(this.createDate, comp.createDate);
        if (order != 0) {
            return order;
        }
        order = compareTo(this.createUserid, comp.createUserid);
        if (order != 0) {
            return order;
        }
        order = compareTo(this.deleteDate, comp.deleteDate);
        if (order != 0) {
            return order;
        }
        return compareTo(this.deleteUserid, comp.deleteUserid);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        GlossaryXwalkHistory clone = new GlossaryXwalkHistory();
        clone.histId = this.histId;
        clone.glossTypeParent = this.glossTypeParent;
        clone.itemNameParent = this.itemNameParent;
        clone.glossTypeChild = this.glossTypeChild;
        clone.itemNameChild = this.itemNameChild;
        clone.createDate = this.createDate;
        clone.createUserid = this.createUserid;
        clone.deleteDate = this.deleteDate;
        clone.deleteUserid = this.deleteUserid;
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
        if (!(obj instanceof GlossaryXwalkHistory)) {
            return false;
        }
        GlossaryXwalkHistory comp = (GlossaryXwalkHistory) obj;

        return compareTo(this.histId, comp.histId) == 0
                && compareTo(this.glossTypeParent, comp.glossTypeParent) == 0
                && compareTo(this.itemNameParent, comp.itemNameParent) == 0
                && compareTo(this.glossTypeChild, comp.glossTypeChild) == 0
                && compareTo(this.itemNameChild, comp.itemNameChild) == 0
                && compareTo(this.createDate, comp.createDate) == 0
                && compareTo(this.createUserid, comp.createUserid) == 0
                && compareTo(this.deleteDate, comp.deleteDate) == 0
                && compareTo(this.deleteUserid, comp.deleteUserid) == 0;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("histId: ").append(this.histId)
                .append(" glossTypeParent: ").append(this.glossTypeParent)
                .append(" itemNameParent: ").append(this.itemNameParent)
                .append(" glossTypeChild: ").append(this.glossTypeChild)
                .append(" itemNameChild: ").append(this.itemNameChild).append(
                        " createDate: ").append(this.createDate).append(
                        " createUserid: ").append(this.createUserid).append(
                        " deleteDate: ").append(this.deleteDate).append(
                        " deleteUserid: ").append(this.deleteUserid);

        return buff.toString();
    }

    @TableGenerator(table="KeySequence", name = "GlossaryXwalkHistorySeq", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "GlossaryXwalkHistorySeq")
    @Column(name = "Id", nullable = false)
    private BigDecimal histId;

    @Column(name = "ItemNameChild", nullable = false, length = 80)
    private String itemNameChild;

    @Column(name = "ItemNameParent", nullable = false, length = 80)
    private String itemNameParent;

    @Column(name = "DeleteUserId", nullable = true, length = 30)
    private String deleteUserid;

    @Column(name = "GlossTypeParent", nullable = false, length = 30)
    private String glossTypeParent;

    @Column(name = "DeleteDate", nullable = true)
    private Date deleteDate;

    @Column(name = "CreateUserId", nullable = false, length = 30)
    private String createUserid;

    @Column(name = "GlossTypeChild", nullable = false, length = 30)
    private String glossTypeChild;

    @Column(name = "CreateDate", nullable = false)
    private Date createDate;

    private static final long serialVersionUID = 1L;

    public GlossaryXwalkHistory() {
        super();
    }

    public BigDecimal getHistId() {
        return this.histId;
    }

    public void setHistId(BigDecimal ccatKey) {
        this.histId = ccatKey;
    }

    public String getItemNameChild() {
        return this.itemNameChild;
    }

    public void setItemNameChild(String ccatItemNameChild) {
        this.itemNameChild = ccatItemNameChild;
    }

    public String getItemNameParent() {
        return this.itemNameParent;
    }

    public void setItemNameParent(String ccatItemNameParent) {
        this.itemNameParent = ccatItemNameParent;
    }

    public String getDeleteUserid() {
        return this.deleteUserid;
    }

    public void setDeleteUserid(String ccatDeleteUserid) {
        this.deleteUserid = ccatDeleteUserid;
    }

    public String getGlossTypeParent() {
        return this.glossTypeParent;
    }

    public void setGlossTypeParent(String ccatGlossTypeParent) {
        this.glossTypeParent = ccatGlossTypeParent;
    }

    public Date getDeleteDate() {
        return this.deleteDate;
    }

    public void setDeleteDate(Date ccatDeleteDate) {
        this.deleteDate = ccatDeleteDate;
    }

    public String getCreateUserid() {
        return this.createUserid;
    }

    public void setCreateUserid(String ccatCreateUserid) {
        this.createUserid = ccatCreateUserid;
    }

    public String getGlossTypeChild() {
        return this.glossTypeChild;
    }

    public void setGlossTypeChild(String ccatGlossTypeChild) {
        this.glossTypeChild = ccatGlossTypeChild;
    }

    public Date getCreateDate() {
        return this.createDate;
    }

    public void setCreateDate(Date ccatCreateDate) {
        this.createDate = ccatCreateDate;
    }

}
