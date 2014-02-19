package gov.idaho.sboe.jaas.dbloginmodule.util;

import gov.idaho.sboe.jaas.dbloginmodule.interfaces.DBLoginModuleEncodingInterface;

public class DBLoginModuleCearTextEncoder implements DBLoginModuleEncodingInterface{
  
  public DBLoginModuleCearTextEncoder()
  {
  }
  
  public String getKeyDigestString (String message, String key) {
    
    //perform no encryption
    return message;
  }    
}