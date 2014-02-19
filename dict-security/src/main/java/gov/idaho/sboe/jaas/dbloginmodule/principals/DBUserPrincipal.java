package gov.idaho.sboe.jaas.dbloginmodule.principals;

import gov.idaho.sboe.jaas.dbloginmodule.interfaces.UserPrincipal;


public class DBUserPrincipal implements UserPrincipal 
{
  private String _name;
  
  public DBUserPrincipal()
  {
  }
  
  public DBUserPrincipal(String name)
  {
    this._name=name;
  }

  public boolean equals(Object another)
  {
    if (another == null)
       return false;

    if (this == another)
       return true;
 
    if (another instanceof DBUserPrincipal) {
       if (((DBUserPrincipal) another).getName().equals(_name))
        return true;
       else
        return false;
    }
    else 
        return false;
  }

  public String toString()
  {
    return "DBUserPrincipal: " + _name;
  }

  public int hashCode()
  {
    return _name.hashCode();
  }

  public String getName()
  {
    return _name;
  }
}