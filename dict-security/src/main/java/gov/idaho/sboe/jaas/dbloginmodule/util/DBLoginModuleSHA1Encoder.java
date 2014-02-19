package gov.idaho.sboe.jaas.dbloginmodule.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import gov.idaho.sboe.jaas.dbloginmodule.interfaces.DBLoginModuleEncodingInterface;
import sun.misc.BASE64Encoder;

public class DBLoginModuleSHA1Encoder implements DBLoginModuleEncodingInterface{
  
  public DBLoginModuleSHA1Encoder()
  {
  }
  
  public String getKeyDigestString (String message, String key) {
    try {
      String pwCompareStr = "";
      byte[] messageByte = message.getBytes();
      // of no key is provided, the message string gets encrypted with itself
      
      byte[] keyByte = (key != null && key.length()>0)? key.getBytes():message.getBytes();
      
      // get SHA1 instance       
      MessageDigest sha1 = MessageDigest.getInstance("SHA1");        
      sha1.update(messageByte);
      byte[] digestByte = sha1.digest(keyByte);
              
      // base 64 encoding
      BASE64Encoder b64Encoder = new BASE64Encoder();
      pwCompareStr = (b64Encoder.encode(digestByte));
      
      return pwCompareStr;
      } 
      
      catch (NoSuchAlgorithmException e) {
      }
    return null;
  }
}