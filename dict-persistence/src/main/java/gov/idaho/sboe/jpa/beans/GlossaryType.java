package gov.idaho.sboe.jpa.beans;

import java.io.Serializable;
import javax.persistence.*;

/**
 * <p>
 * This entity bean defines a catagory grouping for a {@link Glossary} entry.
 * </p>
 */
@NamedQueries( {
        @NamedQuery(name = "findAllGlossaryType", query = "select t from GlossaryType t order by t.glossType"),
        @NamedQuery(name = "truncateGlossaryType", query = "delete from GlossaryType") })
@Entity
@Table(name = "GlossaryType")
public class GlossaryType extends AbstractEntityBean implements Serializable,
        Comparable, Cloneable {

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
        if (!(o instanceof GlossaryType)) {
            return -1;
        }

        GlossaryType comp = (GlossaryType) o;
        int order = compareTo(this.glossType, comp.glossType);
        if (order != 0) {
            return order;
        }
        return compareTo(this.glossDesc, comp.glossDesc);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        GlossaryType clone = new GlossaryType();
        clone.glossType = this.glossType;
        clone.glossDesc = this.glossDesc;

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
        if (!(obj instanceof GlossaryType)) {
            return false;
        }

        GlossaryType comp = (GlossaryType) obj;

        return compareTo(this.glossType, comp.glossType) == 0
                && compareTo(this.glossDesc, comp.glossDesc) == 0;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("glossType: ").append(glossType).append(" glossDesc: ")
                .append(glossDesc);
        return buff.toString();
    }

    @Id
    @Column(name = "GlossType", nullable = false, length = 30)
    private String glossType;

    @Column(name = "GlossDesc", nullable = false, length = 400)
    private String glossDesc;

    @Column(name = "GlossVisible", nullable = false, length = 1)
    private String glossVisible;

    private static final long serialVersionUID = 1L;

    public GlossaryType() {
        super();
    }

    public String getGlossType() {
        return this.glossType;
    }

    public void setGlossType(String ccatGlossType) {
        this.glossType = ccatGlossType;
    }

    public String getGlossDesc() {
        return this.glossDesc;
    }

    public void setGlossDesc(String ccatGlossDesc) {
        this.glossDesc = ccatGlossDesc;
    }

    public String getGlossVisible() {
        return this.glossVisible;
    }
    
    public boolean isGlossVisible() {
        return this.glossVisible.toUpperCase().equals("Y");
    }

    public void setGlossVisible(String ccatGlossVisible) {
        this.glossVisible = ccatGlossVisible;
    }

}
