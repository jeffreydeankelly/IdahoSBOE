package gov.idaho.sboe.jaas.dbloginmodule.interfaces;

import java.security.Principal;
import java.util.Map;
import javax.security.auth.Subject; 
import javax.security.auth.spi.LoginModule;


public interface DBLoginModule extends LoginModule 
{
  
  // prefix to be used when writing log messages. Can be overridden
  // in LM implementation
  static final String LOGGING_PREFIX="[DBLoginModule]";
  
  // callback handler settings
  static final int NUM_CALLBACKS = 2;
  static final int USERNAME_CALLBACK_INDEX = 0;
  static final int PASSWORD_CALLBACK_INDEX = 1;
  
  // logging levels
  public final static int LOG_ALL =  0;
  public final static int LOG_AUTH = 1;
  
  // the message keys

  public final String MESSAGE_KEY_NO_CALLBACK_HANDLER = "Missing CallbackHandler";

  /**
   * 
   * @return javax.security.auth.Subject
   */
  Subject getSubject();
  /**
   * 
   * @return java.util.Map
   */
  Map getOptions();
  /**
   * 
   * @return java.util.Map
   */
  Map getSharedState();
  /**
   * 
   * @return String
   */
  String getUsername();
  /**
   * 
   * @return String
   */
  String getConnectInformation();
  /**
   * 
   * @return boolean Method that instatiates the logger class that is provided 
   * in the option of the LoginModule configuration. All logger classes must 
   * implement the LMLogger interface. The configuration is performed via the 
   * "logger_class" option in the LM configurations
   */
  boolean setLogger();
  
  /**
   * Method that is called within a program to perform the logging action. Having 
   * this method being separate from the setLogger() method allows to add application
   * (LM) specific actions to be performed if required. The log method also evaluates
   * the log level for printing.
   * 
   * @param prt message that should be logged
   * @param level DBLoginModuleConstants.LOG_AUTH or LOG_ALL
   */
  void log(String prt, int level);
}