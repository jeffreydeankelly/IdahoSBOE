package gov.idaho.sboe.jaas.dbloginmodule.DBProcLM;

import java.security.Principal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import javax.sql.DataSource;

//import oracle.jdbc.OracleTypes;

import gov.idaho.sboe.jaas.dbloginmodule.interfaces.DBLoginModule;
import gov.idaho.sboe.jaas.dbloginmodule.interfaces.DBLoginModuleEncodingInterface;
import gov.idaho.sboe.jaas.dbloginmodule.interfaces.LMLogger;
import gov.idaho.sboe.jaas.dbloginmodule.principals.DBRolePrincipal;
import gov.idaho.sboe.jaas.dbloginmodule.principals.DBUserPrincipal;
import gov.idaho.sboe.jaas.dbloginmodule.util.ScreenLoggerImpl;


public class DBProcOraDataSourceLoginModule implements DBLoginModule 
{
 
  // initial state
  protected Subject _subject;
  protected CallbackHandler _callbackHandler;
  protected Map _sharedState;
  
  /**
   * Options:
   * <ul>
   * <li>debug debug=true  indicates that LoginModule prints processing information </li>
   * <li>data_source_name  Name of the data source configured in the data-sources.xml file 
   * <li>logger_class      The class that implements the LMLogger interface to write 
   *                       loginformation for this Login Module. Additional options required
   *                       by a specific logging device must be provided with the LoginModule
   *                       configuration. 
   * 
   *                       The default logger is oracle.sample.dbloginmodule.util.ScreenLoggerImpl
   *                       that logs messages with System.out.println() </li>
   * <li>log_level         "AUTH" or "ALL" (default). determines the granularity of the log messages.
   *                       The default setting is all. Authentication only logs authentication related 
   *                       information</li>. Logs require debug=true to be written.</li>
   * <li>application_realm Application security realm for least privileged access. Use this option of your 
   *                       authentication scheme supports application specific authorization.</i>
   * </ul>
   */
  protected Map _options;
  
  // configuration options
  protected boolean _debug = false;
  protected String _data_source_name  ="";
  
  // username and password
  protected String _username;
  protected char[] _password;
  
  // authentication table details

  
  // debugging detail
  protected boolean _logAll  = true;
  protected boolean _logAuth = false;
  protected static final String LOGGING_PREFIX = "[DBProcDataSourceLoginModule]";
  // authentication
  boolean _logonSucceeded = false;
  boolean _commitSucceeded = false;
  
  protected Principal[] _authPrincipals;
  protected LMLogger _lmlogger;
  protected DBLoginModuleEncodingInterface _pwencoder;
  
  protected String _application_realm ="";
  protected String _loggerClass ="";
  
  // list of user Principals and database roles
  protected ArrayList _dbauth  = new ArrayList();
  
  public DBProcOraDataSourceLoginModule()
  { 
  }
  
  /**
   * 
   * @param subject <code>Subject</code> to be authenticated
   * @param cbh <code>CallbackHandler</code> for communication with end users
   * @param sharedState shared <code>LoginModule</code> state
   * @param options <code>Configurations</code> for this LoginModule.
   */
  public void initialize(Subject subject, CallbackHandler cbh, Map sharedState, Map options)
  {
    this._subject = subject;
    this._callbackHandler = cbh;
    this._sharedState=sharedState;
    this._options = options;
    
    
    // log all if no log_level option is found and if log_level is set to all
    _logAll  =  options.get("log_level")==null?true:"ALL".equalsIgnoreCase((String)options.get("log_level"))?true:false;
    
    // _logAuth always is the opposite of _logAll
    _logAuth = !_logAll;
    
    //get the options required to authenticate the user
    _debug = "TRUE".equalsIgnoreCase((String)options.get("debug"));
  
    _data_source_name= options.get("data_source_name")== null?"":(String)options.get("data_source_name");
     
    // Get logger class to write debug information too. The default is to write the 
    // logging output to screen. All logger classes must implement the LMLogger Interface
    _loggerClass = options.get("logger_class")!=null?(String) options.get("logger_class"):null;
    
    
    _application_realm = options.get("application_realm")!=null?(String) options.get("application_realm"):null;
    
    // instantiate the configured logger

    this.setLogger();
    
    // write options 
    log("option debug             = " + _debug, LOG_ALL);
    log("option data source name  = " + _data_source_name, LOG_ALL);
    log("option log level         = " + (_logAll?"log all":"log auth"),LOG_ALL);
    log("option logger class      = "+_loggerClass,LOG_ALL);
    log("option application_realm = "+ _application_realm,LOG_ALL);

  }
    
  public boolean login() throws LoginException
  {
    log("login called on DBTableLoginModule",LOG_ALL);
    
    // set logon success to false
    _logonSucceeded=false;
    
    if (_callbackHandler == null)
    {
      log("Error: "+MESSAGE_KEY_NO_CALLBACK_HANDLER,LOG_ALL);
      throw new LoginException (LOGGING_PREFIX+"Error: "+MESSAGE_KEY_NO_CALLBACK_HANDLER);
   }
    
    Callback[] callbacks = new Callback[NUM_CALLBACKS];
    callbacks[USERNAME_CALLBACK_INDEX] = new NameCallback("Username");
    callbacks[PASSWORD_CALLBACK_INDEX] = new PasswordCallback("Password:",false);
    
    		try
    		{
          log("Calling callbackhandler ... ",LOG_ALL);
          
    			// Prompt the user for a _username and _password. In a web environment the
          // callback handler would get the username/password pair from the request 
          // object 
    			
          _callbackHandler.handle(callbacks);


    			_username = ((NameCallback)callbacks[USERNAME_CALLBACK_INDEX]).getName();
     			_password = ((PasswordCallback)callbacks[PASSWORD_CALLBACK_INDEX]).getPassword();
          
          log("Username returned by callback = "+_username,LOG_AUTH);
          
          _logonSucceeded = this.performDbAuthentication(_username, _password);
                        
          log("Logon Successful = "+_logonSucceeded,LOG_AUTH);
          return _logonSucceeded;

    		}
    		catch (java.io.IOException e)
    		{
          log("Login error: " +e.getMessage() + "\n" + e.toString(),LOG_AUTH);
    			// Log io exception for debug
    			throw new LoginException(LOGGING_PREFIX+"Login error: " +
    				e.getMessage() + "\n" + e.toString());
    		}
        
    		catch (UnsupportedCallbackException e)
    		{
    			// Log unsupported callback exception for debug

    			throw new LoginException(LOGGING_PREFIX+"Callback error: " +
    				e.getMessage() + "\n" + e.getCallback().toString() +
    				" not available\n");
    		}
  }

  public boolean commit() throws LoginException
  {
    //set commit success to false
    _commitSucceeded = false;
    log("Subject contains "+_subject.getPrincipals().size()+" Principals before auth", LOG_ALL);
    
    if(!_logonSucceeded)
      {
        log("Local LM commit failed because authentication failed",LOG_ALL);
        return false;
      }
      
    if (_subject.isReadOnly())
      {
        log("Subject is read only! Arrrrgh.",LOG_ALL);
        log("Local LM commit failed because Subject is read only",LOG_ALL);
        throw new LoginException("Subject is read only!");
      }
      
      // authenticated is if _authPrincipals is not null and the length is 
      // at least one
    if (_authPrincipals!= null && _authPrincipals.length>0)
      {
        for (int i = 0; i < _authPrincipals.length; i++) 
        {
         // add the Principals to the subject
          _subject.getPrincipals().add(_authPrincipals[i]);
        }
        _commitSucceeded = true;

        log("Local LM commit succeeded",LOG_AUTH);
        log("Subject contains "+_subject.getPrincipals().size()+" Principals after auth", LOG_ALL);
        
        log("Cleaning internal state!", LOG_ALL);
        cleanInternalState();
        
        return true; 
      }
    
    log("Cleaning internal state!", LOG_ALL);
    cleanInternalState();
    return false;
  }

  /**
   * Housekeeping when authentication is aborted because a required LoginModule 
   * failed
   * 
   * @return true of false based on whether or not the previous authentication
   * was successful and the changes have been submitted to the subject
   * @throws LoginException 
   */
  public boolean abort() throws LoginException
  {
    log("Abort called on LoginModule",LOG_ALL);
    
    if (!_logonSucceeded)
    {
      cleanInternalState();
      return false;
    }
    else if(_logonSucceeded && !_commitSucceeded)
    {
      _logonSucceeded = false;
      cleanInternalState();
    }
    else
    {
      removePrincipalsFromSubjectAndCleanUp();
      _commitSucceeded = false;
      _logonSucceeded = false;
    }
    return true;
  }

  public boolean logout() throws LoginException
  {
    _logonSucceeded=false;
    _commitSucceeded=false;
    removePrincipalsFromSubjectAndCleanUp();
  
    return true;
  }

  public Subject getSubject()
  {
    return _subject;
  }

  public Map getOptions()
  {
    return _options;
  }

  public Map getSharedState()
  {
    return _sharedState;
  }

  public String getUsername()
  {
    return _username;
  }

  public String getConnectInformation()
  {
    return _data_source_name;
  }

  public boolean setLogger()
  {
    // check options if logger class is specified
    String errmsg="";

    if (_loggerClass != null && _loggerClass.length()>0)
    { 

      try 
      {       
        this._lmlogger = (LMLogger) Class.forName(_loggerClass).newInstance();
        this._lmlogger.setLoginModule(this);
        return true;
      } catch (Exception ex) 
      {
        errmsg="Cannot instantiate logger "+_loggerClass+". Use screen as default log output!";
      }
    }
      // if no logger class is specified or logger class couldn't be found
      // use default ScreenLogger
      
      this._lmlogger = (LMLogger) new ScreenLoggerImpl(this);

      if (errmsg.length()>0)
      {
        // print error message if specified logger class couldn't be printed.
        log(errmsg,LOG_ALL);
      }
      return true;
  }
  
  /**
   * @param prt statement to print
   * @param level log level LOG_ALL or LOG_AUTH
   */
  public void log(String prt, int level)
  {
    // logging requires debug flag to be set to true
    if (_debug && _logAll)
      _lmlogger.print(LOGGING_PREFIX + " " + prt + "\n");
      
      
    // print only authentication login  
    else if (_debug && _logAuth && level == LOG_AUTH)
    {
      _lmlogger.print(LOGGING_PREFIX + " " + prt + "\n");
    }
    else 
    {
      // log nothing
    }
    /**
   * 
   * @param username 
   * @param password 
   * @return true if authentication succeeded, false if failed
   */
  }
  
   /**
   * 
   * @param username 
   * @param password 
   * @return true if authentication succeeded, false if failed
   */
  protected boolean performDbAuthentication(String username, char[] password)
  {
    ArrayList _dbauth; 
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rolesResultSet = null;
    
    try{
          
      
      Context ic = new InitialContext();
      DataSource dataSource = (DataSource) ic.lookup(_data_source_name);
      // get connection from data-sources.xml
      conn = dataSource.getConnection();
      
      // make sure dbroles and dbauth are empty before assigning new 
      // values
            
      _dbauth = new ArrayList();
      
      // SQL INJECTION DETECTION
      // Detect and report SQL injection attempts. The following code logs all attempts to enter
      // SQL commands like create, drop, update, delete, insert, or, hexadecimal encoding etc. 
      // and immediately returns false
      
      Pattern p = Pattern.compile("(?i)\\bselect|create|insert|delete|drop|update|%.\\d\\b");
      Matcher m = p.matcher(username);
      
      if (m.find())
      {
        //someone tries to break into this application and should be tracked back
        log("SQL Injection attempt detected: username was altered to include SQL keyword: "+m.group(),LOG_AUTH);
        return false;
      }
          
      String sql =  "select distinct ccat_user_desc "
                  + " from ccat_users "
                  + " where ccat_userid = ? and ccat_user_password = ?";
      
      stmt = conn.prepareStatement(sql);
      stmt.setString(1,username);
      stmt.setString(2,new String(password));
      rolesResultSet = stmt.executeQuery();
      log("DBUser Principal Name: "+username,LOG_AUTH);
      _dbauth.add(new DBUserPrincipal(username));

  
      while( rolesResultSet.next())
      {
         String roleName  = rolesResultSet.getString(1);
         log("DBRole Principal Name: "+roleName,LOG_AUTH);
            
         DBRolePrincipal dbRolePrincipal = new DBRolePrincipal(roleName);
         _dbauth.add(dbRolePrincipal);
      }
    
      // set principals in LoginModule
      _authPrincipals=(Principal[]) _dbauth.toArray(new Principal[_dbauth.size()]);
      return _dbauth.size()>1;
  }
          
   catch(SQLException sqle)
   {
    	log("User "+ username+" not authenticated: username or password mismatch",LOG_AUTH);
      
      // though this section is logging, we have an interest in not to expose any
      // highly sensitive information in the log files. 
      if(sqle.getMessage().indexOf("ORA-01403")< 0){
        
        if (sqle.getMessage().indexOf("ORA-06510") < 0)
        {
          //ORA-01403 is shown if user cannot be found. We don't want to 
          // show this information to a possible hacker and suppress it
          log("Error: "+sqle.getMessage(),LOG_AUTH);
        }
        else
        {
          // The error message ORA-06510 states that an unhandled exception was 
          // rasied in the PLSQL function. The message would show name and the 
          // line of the PLSQL procedure where this problem showed in. This also 
          // is information we don't want to expose here and this we suppress it.
          
          // Do nothing !
        }
        
      }      
       return false;
   }
   catch (NamingException ne)
   {
      log("User "+ username+" not authenticated: datasource name could not be resolved",LOG_AUTH);
      log("Error: "+ne.getMessage(),LOG_AUTH);      
      return false;
   }
      finally
      {
    	    try
    	    {
    	        if (rolesResultSet != null)
    	            rolesResultSet.close();
    	     
    	        if (stmt != null)
    	          stmt.close();
    	     
    	         if (conn != null)
    	           conn.close();
    	    }
    	    catch(SQLException e)
    	    {
    	           //ignore it
    	    }
     }   
  }
  
  /**
   * Cleans the internal state held for the username and the password. The password
   * is emptied character by character and then deleted from memory
   */
  protected void cleanInternalState()
  {
    _username=null;
    
    if(_password != null)
    {
      for (int i = 0; i < _password.length; i++) 
      {
       // make sure nothing remains in any memory dump
        _password[i]=' ';
      }
      _password=null;
    }
    
    // close all sources that are used for logging of open
    if (this._lmlogger!= null && this._lmlogger.hasInternalState())
    {
      this._lmlogger.close();
    }
  }
  
  /**
   * Called in case the overall JAAS authentication failed or logout
   * is called. This method removes all instances of DBRolePrincipal and 
   * DBUserPrincipal from the subject
   */
  protected void removePrincipalsFromSubjectAndCleanUp()
  {
    cleanInternalState();
    
    // only remove the principals added by this Loginmodule
    // DBUserPrincipal and DBRolePrincipal contained in the 
    // package oracle.sample.dbloginmodule.principals should 
    // only be added by this LoginModule
    
    DBRolePrincipal dbroles = new DBRolePrincipal();
    Set dbrolesSet = _subject.getPrincipals(dbroles.getClass());
    DBUserPrincipal dbuser = new DBUserPrincipal();
    Set dbuserSet = _subject.getPrincipals(dbuser.getClass());
    
    for (int i = 0; i < dbuserSet.size(); i++) 
    {
      _subject.getPrincipals().remove(dbrolesSet);
      _subject.getPrincipals().remove(dbuserSet);
    }
  }
}