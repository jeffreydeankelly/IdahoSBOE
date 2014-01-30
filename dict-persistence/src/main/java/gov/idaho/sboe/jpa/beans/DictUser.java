package gov.idaho.sboe.jpa.beans;

import java.io.Serializable;
import javax.persistence.*;

/**
 * <p>
 * This entity bean represents an authenticated interanl staff member. Internal
 * users can make changes to the glossary.
 * </p>
 */
@NamedQueries( { @NamedQuery(name = "truncateUser", query = "delete from DictUser") })
@Entity
@Table(name = "DictUser")
public class DictUser extends AbstractEntityBean implements Serializable,
        Comparable, Cloneable {
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DictUser clone = new DictUser();
        clone.userid = this.userid;
        clone.userName = this.userName;
        clone.userPassword = this.userPassword;
        clone.userDesc = this.userDesc;

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
        if (!(obj instanceof DictUser)) {
            return false;
        }
        DictUser comp = (DictUser) obj;
        if (compareTo(this.userid, comp.userid) != 0) {
            return false;
        }

        if (compareTo(this.userName, comp.userName) != 0) {
            return false;
        }
        
        if (compareTo(this.userName, comp.userName) != 0) {
            return false;
        }
        if (compareTo(this.userPassword, comp.userPassword) != 0) {
            return false;
        }
         
        return (compareTo(this.userDesc, comp.userDesc) == 0);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("userid: ").append(this.userid).append(" userName: ")
                .append(this.userName).append(" userPassword: ").append(
                        this.userPassword).append(" userDesc: ").append(
                        this.userDesc);

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
        if (!(o instanceof DictUser)) {
            return -1;
        }
        DictUser comp = (DictUser) o;

        int order = compareTo(this.userid, comp.userid);
        if (order != 0) {
            return order;
        }
        order = compareTo(this.userName, comp.userName);
        if (order != 0) {
            return order;
        }
        order = compareTo(this.userPassword, comp.userPassword);
        if (order != 0) {
            return order;
        }
        return compareTo(this.userDesc, comp.userDesc);
    }

    @Id
    @Column(name = "DictUserId", nullable = false, length = 30)
    private String userid;

    @Column(name = "DictUserPassword", nullable = false, length = 15)
    private String userPassword;

    @Column(name = "DictUserDesc", nullable = true, length = 400)
    private String userDesc;

    @Column(name = "DictUserName", nullable = false, length = 50)
    private String userName;

    private static final long serialVersionUID = 1L;

    public DictUser() {
        super();
    }

    public String getUserid() {
        return this.userid;
    }

    public void setUserid(String ccatUserid) {
        this.userid = ccatUserid;
    }

    public String getUserPassword() {
        return this.userPassword;
    }

    public void setUserPassword(String ccatUserPassword) {
        this.userPassword = ccatUserPassword;
    }

    public String getUserDesc() {
        return this.userDesc;
    }

    public void setUserDesc(String ccatUserDesc) {
        this.userDesc = ccatUserDesc;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String ccatUserName) {
        this.userName = ccatUserName;
    }

}
