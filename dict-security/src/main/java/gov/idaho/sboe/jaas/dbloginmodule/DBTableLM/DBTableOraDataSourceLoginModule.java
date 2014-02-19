package gov.idaho.sboe.jaas.dbloginmodule.DBTableLM;

/*
 * @(#)DBTableOraDataSourceLoginModule.java  1.0 March 2005
 *
 * Copyright (c) 2001-2005 by Oracle. All Rights Reserved.
 * 
 * Oracle grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Oracle.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. ORACLE AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL ORACLE OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF ORACLE HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

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

import gov.idaho.sboe.jaas.dbloginmodule.interfaces.DBLoginModule;
import gov.idaho.sboe.jaas.dbloginmodule.interfaces.DBLoginModuleEncodingInterface;
import gov.idaho.sboe.jaas.dbloginmodule.interfaces.LMLogger;
import gov.idaho.sboe.jaas.dbloginmodule.principals.DBRolePrincipal;
import gov.idaho.sboe.jaas.dbloginmodule.principals.DBUserPrincipal;
import gov.idaho.sboe.jaas.dbloginmodule.util.ScreenLoggerImpl;


public class DBTableOraDataSourceLoginModule implements DBLoginModule 
{
 
  // initial state
  protected Subject _subject;
  protected CallbackHandler _callbackHandler;
  protected Map _sharedState;
  
  /**
   * Options:
   * <ul>
   * <li>debug debug=true  indicates that LoginModule prints processing information </li>
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
   * <li>data_source_name  Name of the data source configured in the data-sources.xml file
   * <li>user_table        Name of the user table</li>
   * <li>roles_table       Name of the roles table</li>
   * <li>username_column   User table column to obtain the username </li>
   * <li>password_column   User table column to obtain the password </li>
   * <i>pw_encoding_class  Class that implements the DBLoginModuleEncodingInterface interface. Use this option if the 
   *                       user password in the underlying table is encrypted. The  default implementation is clear text.
   *                       An alternative implementation using SAH1 is provided in: oracle.sample.dbloginmodule.util.DBLoginModuleSHA1Encoder </li>                  
   * <li>pw_key</li>       The key string used to encrypt the password. Note that the provided example in DBLoginModuleSHA1Encoder encrypts the password 
   *                       with itself if no password key string is provided</li>
   * <li>roles_column      Roles table column to read user roles from </li>
   * <li>realm_column      Column that holds the application realm. This value is optional</i>
   * <li>application_realm Application realm for least privileged access. Optional attribute </i> 
   * <li>user_pk_column    Primary key column of the user table</li>
   * <li>roles_fk_column   Foreign key column of the roles table</li>
   * 
   * </ul>
   */
  protected Map _options;
  
  // configuration options
  protected boolean _debug = false;
  
  // username and password
  protected String _username;
  protected char[] _password;
  
  // authentication table details
  
  protected String _data_source_name  ="";
  protected String _user_table        ="";
  protected String _roles_table       ="";
  protected String _username_column   ="";
  protected String _password_column   ="";
  protected String _roles_column      ="";
  protected String _pw_encoding_class  ="";
  protected String _pw_key="";
  protected String _user_pk_column    ="";
  protected String _roles_fk_column   ="";
  protected String _realm_column      ="";
  protected String _application_realm ="";
  
  // debugging detail
  protected boolean _logAll  = true;
  protected boolean _logAuth = false;
  protected static final String LOGGING_PREFIX = "[DBTableOraDatasourceLoginModule]";
  // authentication
  boolean _logonSucceeded = false;
  boolean _commitSucceeded = false;
  
  protected Principal[] _authPrincipals;
  protected LMLogger _lmlogger;
  protected DBLoginModuleEncodingInterface _pwencoder;
  
  protected String _loggerClass ="";
  
  // list of user Principals and database roles
  protected ArrayList _dbauth  = new ArrayList();
  
  public DBTableOraDataSourceLoginModule()
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
      
    // Get logger class to write debug information too. The default is to write the 
    // logging output to screen. All logger classes must implement the LMLogger Interface
    _loggerClass = options.get("logger_class")!=null?(String) options.get("logger_class"):null;
    
    _data_source_name  = options.get("data_source_name")!=null?(String) options.get("data_source_name"):null;
    _user_table        = options.get("user_table")!=null?(String) options.get("user_table"):null;
    _roles_table       = options.get("roles_table")!=null?(String) options.get("roles_table"):null;
    _username_column   = options.get("username_column")!=null?(String) options.get("username_column"):null;
    _password_column   = options.get("password_column")!=null?(String) options.get("password_column"):null;
    _pw_encoding_class = options.get("pw_encoding_class")!=null?(String) options.get("pw_encoding_class"):null;    
    _pw_key            = options.get("pw_key")!=null?(String) options.get("pw_key"):null;    
    _roles_column      = options.get("roles_column")!=null?(String) options.get("roles_column"):null;
    _user_pk_column    = options.get("user_pk_column")!=null?(String) options.get("user_pk_column"):null;
    _roles_fk_column   = options.get("roles_fk_column")!=null?(String) options.get("roles_fk_column"):null;
    _realm_column      = options.get("realm_column")!=null?(String) options.get("realm_column"):null;
    _application_realm = options.get("application_realm")!=null?(String) options.get("application_realm"):null;
    // instantiate the configured logger

    this.setLogger();
    
    // write options 
    log("option debug         = " + _debug, LOG_ALL);
    log("option log level     = " + (_logAll?"log all":"log auth"),LOG_ALL);
    log("option logger class  = "+_loggerClass,LOG_ALL);
    log("option data_source_name = "+ _data_source_name,LOG_ALL);    
    log("option user table    = "+ _user_table,LOG_ALL);    
    log("option roles table   = "+ _roles_table,LOG_ALL);    
    log("option username column = "+ _username_column,LOG_ALL);    
    log("option password column = "+ _password_column,LOG_ALL);    
    log("option roles column    = "+_roles_column,LOG_ALL);    
    log("option user pk column  = "+ _user_pk_column,LOG_ALL);    
    log("option roles fk column = "+_roles_fk_column,LOG_ALL); 
    log("option password encoding class = "+_pw_encoding_class,LOG_ALL);
    log("option realm_column  = "+ _realm_column,LOG_ALL);
    log("option application_realm  = "+ _application_realm,LOG_ALL);
      
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
    
    try 
    {
      Context ic = new InitialContext();
      DataSource dataSource = (DataSource) ic.lookup(_data_source_name);

      String _userPk = "";
      
      // get connection from data-sources.xml
      conn = dataSource.getConnection();
            
      // make sure dbroles and dbauth are empty before assigning new 
      // values
            
      _dbauth = new ArrayList();
      
      // SQL INJECTION DETECTION
      // Detect and report SQL injection attempts. The following code logs all attempts to enter
      // SQL commands like create, drop, update, delete, insert, or, hexadecimal encoding etc. 
      // and immediately returns false
      
      Pattern p = Pattern.compile("(?i)\\bselect|create|insert|delete|drop|update|or|%.\\d\\b");
      Matcher m = p.matcher(username);
      
      if (m.find())
      {
        //someone tries to break into this application and should be tracked back
        log("SQL Injection attempt detected: username was altered to include SQL keyword: "+m.group(),LOG_AUTH);
        return false;
      }
    
      // get user information
      
      String userSQL= " select "+_user_pk_column+","+_password_column+" from "+_user_table+" where "+_username_column+"= (?)";
    
      log("User query string: "+userSQL,LOG_ALL);
      PreparedStatement userStmt = conn.prepareStatement(userSQL);
      userStmt.setString(1,username);  
      ResultSet userResultSet = userStmt.executeQuery();
      
      userResultSet.next();
      
      // get userid
      _userPk = userResultSet.getString(_user_pk_column);
      
      log("User primary key value found = "+_userPk,LOG_AUTH); 
    
      // get user password
      String storedPassword = userResultSet.getString(_password_column);
    
      // encrypt the provided password so it matches the staored password
      // in the user database
      
      if (_pw_encoding_class == null)
      {
        // of no encoding class was provided, use the clear text version
        _pw_encoding_class = "oracle.sample.dbloginmodule.util.DBLoginModuleCearTextEncoder";
      }
      
      try 
      {
        _pwencoder = (DBLoginModuleEncodingInterface)Class.forName(_pw_encoding_class).newInstance();
      } 
      
      catch (ClassNotFoundException cnfe) 
      {
        log("No class found for name "+_loggerClass+". Please check JAAS configuration option",LOG_ALL); 
        return false;
      }      
      catch (IllegalAccessException iae) 
      {
        log("Illegal access exception for class name "+_loggerClass+". Please check JAAS configuration option",LOG_ALL); 
        return false;
      }
      catch (InstantiationException ie) 
      {
        log("Cannot instantiate class for name "+_loggerClass+". Please check JAAS configuration option",LOG_ALL); 
        return false;
      }
  
      String encPassword = _pwencoder.getKeyDigestString(new String(password),_pw_key);
      log("Password encoded by: "+_pw_encoding_class,LOG_ALL); 
      
      // if the stored password matches the provided password, the user is 
      // authenticated      
      
      if (!storedPassword.equals(encPassword))
      {
        log("User not authenticated: username or password mismatch",LOG_AUTH);  
        return false;
      }
      else
      {
        log("User "+username+" authenticated successfully",LOG_AUTH); 
      }


      String rolesSqL = "";
      
      if((_application_realm != null) && (_realm_column != null))
      {
        // get roles from application realm
        rolesSqL = "select "+_roles_column+" from "+_roles_table +" where "+_roles_fk_column+"= (?) and "+_realm_column+" = '"+_application_realm+"'";
      }
      else{
        // get all distinct roles
        rolesSqL = "select "+_roles_column+" from "+_roles_table +" where "+_roles_fk_column+"= (?)";
      }
      log("Roles query string: "+rolesSqL,LOG_ALL);       
      
      PreparedStatement rolesStmt = conn.prepareStatement(rolesSqL);
      rolesStmt.setString(1,_userPk);
      
      ResultSet rolesResultSet = rolesStmt.executeQuery();
        
      // add username principal
            
     log("DBUser Principal Name: "+username,LOG_AUTH);
      _dbauth.add(new DBUserPrincipal(username));
            
            
      while(rolesResultSet.next())
      {
         String roleName  = rolesResultSet.getString(_roles_column);
         log("DBRole Principal Name: "+roleName,LOG_AUTH);
            
         DBRolePrincipal dbRolePrincipal = new DBRolePrincipal(roleName);
         _dbauth.add(dbRolePrincipal);
      }
            
      // set principals in LoginModule
      _authPrincipals=(Principal[]) _dbauth.toArray(new Principal[_dbauth.size()]);
      return true;
  }
          
   catch(SQLException sqle)
   {
    	log("User "+ username+" not authenticated: username or password mismatch",LOG_AUTH);
      log("Error: "+sqle.getMessage(),LOG_AUTH);
            
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
    	if (conn != null)
      {
    			try
    				{
    					if( !conn.isClosed() )
    					{
    							conn.close();
    					}
    				}
    			catch(SQLException e)
          {
    				//ignore it
          }
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