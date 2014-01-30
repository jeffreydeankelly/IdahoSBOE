package gov.idaho.sboe.services;

/**
 * <p>
 * This is a checked Exception that will be raised from the service layer.
 * </p>
 */
public class FacadeException extends Exception {
    private static final long serialVersionUID = 2756204972237991936L;

    boolean showMessageToUser = false;

    /**
     * <p>Constructor requires exception <code>message</code>.</p>
     * @param message
     */
    public FacadeException(String message) {
       super(message); 
    }
    
    /**
     * @param message
     *            error message
     * @param originalException
     *            original caught exception
     */
    public FacadeException(String message, Exception originalException) {
        super(message, originalException);
    }

    public FacadeException(String message, Exception originalException, boolean hasUserMessage) {
        super(message, originalException);
        this.showMessageToUser = hasUserMessage;
    }

    public boolean isMessageForUser() {
        return showMessageToUser;
    }
}
