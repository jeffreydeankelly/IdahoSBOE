package gov.idaho.sboe.jaas.dbloginmodule.interfaces;

public interface LMLogger 
{
  /**
   * 
   * @param s the string that should be logged 
   * @return boolean expression when log could be written
   */
  boolean print(String s);
  
  /**
   * Method called when LoginModule closes for the logger to do the clean up
   */
  void close();
  
  /**
   * @param dblm pass a reference of teh LoginModule to the logger so that the 
   * information in the LM can be used by the logger
   */
  void setLoginModule(DBLoginModule dblm);
  
  /**
   * 
   * @return true of there is any internal state like an open file or a 
   * database connection.
   */
  boolean hasInternalState();
}