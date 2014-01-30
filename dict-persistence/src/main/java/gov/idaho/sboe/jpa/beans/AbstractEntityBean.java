package gov.idaho.sboe.jpa.beans;

/**
 * <p>
 * Base entity class that has some utility methods used by JPA entity beans.
 * </p>
 * 
 */
public abstract class AbstractEntityBean implements Cloneable {

    /**
     * @return a new instance with the same state
     */
     public abstract Object clone() throws CloneNotSupportedException;

    /**
     * <p>
     * Utility method used to compare two objects implementing the
     * <code>Comparable</code> interface.
     * </p>
     * 
     * @param o1
     *            first objectcompare
     * @param o2
     *            second object to compare
     * @return returns 0 if the two are equal. If <code>o1</code> &lt;
     *         <code>o2</code> return -1; o; otherwise; return a value of 1.
     */
    @SuppressWarnings("unchecked")
    protected int compareTo(Comparable o1, Comparable o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null && o2 != null) {
            return -1;
        }
        if (o1 != null && o2 == null) {
            return 1;
        }

        if (o1.getClass() != o1.getClass()) {
            return -1;
        }

        return o1.compareTo(o2);
    }

}
