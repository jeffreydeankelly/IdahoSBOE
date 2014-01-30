package gov.idaho.sboe.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import gov.idaho.sboe.utils.Messages;

public class ValidationException extends FacadeException {

    private static final long serialVersionUID = -7549375469548144162L;
    
    /**
     * <p>Message resources for this class.</p>
     */
    private static Messages messages = new Messages("gov.idaho.sboe.services.Bundle", ValidationException.class.getClassLoader());

    
    /**
     * <p>Constructor requires exception <code>message</code>.</p>
     */
    public ValidationException() {
       super(messages.getMessage("validationException.message")); 
    }
    

    /**
     * <p>Captures the detail messages per property.  The first key to the 
     * Map is the propertyName.  The value is a collection of messages.</p>
     */
    private Map<String, List<String>> detailMessages = new TreeMap<String, List<String>>();
    
    
    /**
     * <p>Returns the messages associated with a property name.</p>
     * @param propertyName data element
     * @return a List of messages for the propery name or <code>null</code>
     */
    public List<String> getPropertyMessages(String propertyName) {
       List<String> propertyMessages = detailMessages.get(propertyName);
       return propertyMessages;
    }
    
    /**
     * <p>Adds a <code>message</code> to the collection by 
     * <code>propertyName</code>.</p>
     * @param propertyName data element
     * @param message validation message 
     */
    public void setPropertyMessage(String propertyName, String message) {
        List<String> propertyMessages = detailMessages.get(propertyName);
        if (propertyMessages == null) {
            propertyMessages = new ArrayList<String>();
            detailMessages.put(propertyName, propertyMessages);
        }
        
        propertyMessages.add(message);
    
    }

    
    /**
     * <p>Adds <code>messages</code> to the collection by 
     * <code>propertyName</code>.</p>
     * @param propertyName data element
     * @param messages validation messages for the data element
     */
    public void setPropertyMessage(String propertyName, List<String> messages) {
        List<String> propertyMessages = detailMessages.get(propertyName);
        if (propertyMessages == null) {
            propertyMessages = new ArrayList<String>();
            detailMessages.put(propertyName, propertyMessages);
        }
        
        propertyMessages.addAll(messages);
    
    }

    /**
     * @return data element message collection organized
     */
    public Map<String, List<String>> getDetailMessages() {
        return detailMessages;
    }

    /**
     * @param detailMessages sets the data element message collection
     */
    public void setDetailMessages(Map<String, List<String>> detailMessages) {
        this.detailMessages = detailMessages;
    }


    /**
     * @return message includes the property <code>detailMessages</code>.
     */
    @Override
    public String getMessage() {
        StringBuffer buff = new StringBuffer();
        buff.append(super.getMessage());
     
        if (detailMessages != null) {
            buff.append(":\n");
            for (Map.Entry<String, List<String>> e: detailMessages.entrySet()) {
               for (String msg: e.getValue()) {
                   buff.append("\t").append(msg).append("\n");
               }
            }
        }
        
        return buff.toString();
    }

}
