package gov.idaho.sboe.jaas.dbloginmodule.util;

import gov.idaho.sboe.jaas.dbloginmodule.interfaces.DBLoginModule;
import gov.idaho.sboe.jaas.dbloginmodule.interfaces.LMLogger;
 

public class ScreenLoggerImpl implements LMLogger 
{
  private boolean _internalState = false;
  DBLoginModule _lmToLog;
  
  public ScreenLoggerImpl()
  {
  }
  
  public ScreenLoggerImpl(DBLoginModule dbLoginModule)
  {
    _lmToLog=dbLoginModule;
  }

  public boolean print(String s)
  {
    System.out.println(s);
    return true;
  }
  
  public void close()
  {
    //nothing to close
  }
  
  public void setLoginModule(DBLoginModule dbml)
  {
    _lmToLog = dbml;
  }
  
  public boolean hasInternalState()
  {
    return _internalState;
  }
  
}