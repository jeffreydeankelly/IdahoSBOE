package gov.idaho.sboe.jaas.dbloginmodule.principals;

import gov.idaho.sboe.jaas.dbloginmodule.interfaces.RolePrincipal;


public class DBRolePrincipal implements RolePrincipal 
{
 private String _name;
 
 public DBRolePrincipal()
 {
 }
 
 public DBRolePrincipal(String name)
 {
   this._name = name;
 }

 public boolean equals(Object another)
 {
   if (another == null)
      return false;

   if (this == another)
      return true;

   if (another instanceof DBRolePrincipal) {
      if (((DBRolePrincipal) another).getName().equals(_name))
       return true;
      else
       return false;
   }
   else 
       return false;
 }

 public String toString()
 {
   return "DBRolePrincipal: " + _name;
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