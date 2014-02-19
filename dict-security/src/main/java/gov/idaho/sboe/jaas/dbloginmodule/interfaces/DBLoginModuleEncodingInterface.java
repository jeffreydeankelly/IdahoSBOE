package gov.idaho.sboe.jaas.dbloginmodule.interfaces;

public interface DBLoginModuleEncodingInterface
{
  String getKeyDigestString(String message, String key);
}
