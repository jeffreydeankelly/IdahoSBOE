package gov.idaho.sboe.jpa.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.*;
/**
 * <p>
 * This entity bean records changes to a {@link Glossary} entity bean.
 * </p>
 */
@NamedQueries( {
        @NamedQuery(name = "findHistoryForGlossary", query = "select h from GlossaryHistory h where h.glossType = :glossType and h.itemName = :itemName order by h.histId desc"),
        @NamedQuery(name = "truncateGlossaryHistory", query = "delete from GlossaryHistory") })
@Entity
@Table(name = "GlossaryHistory")
public class GlossaryHistory extends AbstractEntityBean implements
        Serializable, Comparable, Cloneable {

    @Override
    public Object clone() throws CloneNotSupportedException {
        GlossaryHistory clone = new GlossaryHistory();
        clone.createDate = this.createDate;
        clone.createUserid = this.createUserid;
        clone.deleteDate = this.deleteDate;
        clone.deleteUserid = this.deleteUserid;
        clone.updateDate = this.updateDate;
        clone.updateUserid = this.updateUserid;
        clone.glossType = this.glossType;
        clone.itemName = this.itemName;
        clone.itemNarrative = this.itemNarrative;
        clone.histId = this.histId;

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
        if (!(obj instanceof GlossaryHistory)) {
            return false;
        }

        GlossaryHistory comp = (GlossaryHistory) obj;
        if (compareTo(this.createDate, comp.createDate) != 0) {
            return false;
        }
        if (compareTo(this.createUserid, comp.createUserid) != 0) {
            return false;
        }
        if (compareTo(this.deleteDate, comp.deleteDate) != 0) {
            return false;
        }
        if (compareTo(this.deleteUserid, comp.deleteUserid) != 0) {
            return false;
        }
        if (compareTo(this.updateDate, comp.updateDate) != 0) {
            return false;
        }
        if (compareTo(this.updateUserid, comp.updateUserid) != 0) {
            return false;
        }
        if (compareTo(this.glossType, comp.glossType) != 0) {
            return false;
        }
        if (compareTo(this.itemName, comp.itemName) != 0) {
            return false;
        }
        if (compareTo(this.itemNarrative, comp.itemNarrative) != 0) {
            return false;
        }
        if (compareTo(this.histId, comp.histId) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("histId: ").append(histId).append(" createDate: ").append(
                createDate).append(" createUserid: ").append(createUserid)
                .append(" deleteDate: ").append(deleteDate).append(
                        " deleteUserid: ").append(deleteUserid).append(
                        " updateDate: ").append(updateDate).append(
                        " updateUserid: ").append(updateUserid).append(
                        " glossType: ").append(glossType).append(" itemName: ")
                .append(itemName).append(" itemNarrative: ").append(
                        itemNarrative);

        return buff.toString();
    }

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
        if (!(o instanceof GlossaryHistory)) {
            return -1;
        }
        GlossaryHistory comp = (GlossaryHistory) o;

        Comparable[] obj1props = { this.histId, this.createDate,
                this.createUserid, this.deleteDate, this.deleteUserid,
                this.updateDate, this.updateUserid, this.glossType,
                this.itemName, this.itemNarrative };
        Comparable[] obj2props = { comp.histId, comp.createDate,
                comp.createUserid, comp.deleteDate, comp.deleteUserid,
                comp.updateDate, comp.updateUserid, comp.glossType,
                comp.itemName, comp.itemNarrative };

        for (int i = 0; i < obj1props.length; i++) {
            int order = compareTo(obj1props[i], obj2props[i]);
            if (order != 0) {
                return order;
            }
        }

        return 0;
    }

    @TableGenerator(name="GlossaryHistorySeq",table="KeySequence",pkColumnName="SEQ_NAME",valueColumnName="SEQ_COUNT",pkColumnValue="GlossaryHistorySeq",allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "GlossaryHistorySeq")
    @Column(name = "Id", nullable = false, updatable = false)
    private BigDecimal histId;

    @Column(name = "CreateUserId", nullable = false, length = 30)
    private String createUserid;

    @Column(name = "EffDateEnd", nullable = true)
    private Date effDateEnd;

    @Column(name = "ItemNarrative", nullable = false, length = 4000)
    private String itemNarrative;

    @Column(name = "UpdateUserId", nullable = true, length = 30)
    private String updateUserid;

    @Column(name = "GlossType", nullable = false, length = 30)
    private String glossType;

    @Column(name = "DeleteUserId", nullable = true, length = 30)
    private String deleteUserid;

    @Column(name = "UpdateDate", nullable = true)
    private Date updateDate;

    @Column(name = "DeleteDate", nullable = true)
    private Date deleteDate;

    @Column(name = "EffDateStart", nullable = false)
    private Date effDateStart;

    @Column(name = "CreateDate", nullable = false)
    private Date createDate;

    @Column(name = "ItemName", nullable = false, length = 80)
    private String itemName;

    private static final long serialVersionUID = 1L;

    public GlossaryHistory() {
        super();
    }

    public BigDecimal getHistId() {
        return this.histId;
    }

    public void setHistId(BigDecimal ccatKey) {
        this.histId = ccatKey;
    }

    public String getCreateUserid() {
        return this.createUserid;
    }

    public void setCreateUserid(String ccatCreateUserid) {
        this.createUserid = ccatCreateUserid;
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

    public String getGlossType() {
        return this.glossType;
    }

    public void setGlossType(String ccatGlossType) {
        this.glossType = ccatGlossType;
    }

    public String getDeleteUserid() {
        return this.deleteUserid;
    }

    public void setDeleteUserid(String ccatDeleteUserid) {
        this.deleteUserid = ccatDeleteUserid;
    }

    public Date getUpdateDate() {
        return this.updateDate;
    }

    public void setUpdateDate(Date ccatUpdateDate) {
        this.updateDate = ccatUpdateDate;
    }

    public Date getDeleteDate() {
        return this.deleteDate;
    }

    public void setDeleteDate(Date ccatDeleteDate) {
        this.deleteDate = ccatDeleteDate;
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

    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String ccatItemName) {
        this.itemName = ccatItemName;
    }

}
