// Charon system Mike Smith 1999-2017
package inspect;

class Record
{
  private String  theRealName = "";
  private String  theCourse   = "";
  private String  theUID      = "xxx";
  private String  theSN       = "00000000";

  public Record( String aRealName, String aCourse )
  {
    theRealName = aRealName;
    theCourse    = aCourse;
  }

  public Record( String aRealName, String aCourse, String uid )
  {
    theRealName = aRealName;
    theCourse   = aCourse;
    theUID      = uid;
  }

  public Record( String aRealName, String aCourse, String uid, String sn )
  {
    theRealName = aRealName;
    theCourse   = aCourse;
    theUID      = uid;
    theSN       = sn;
  }

  public String name()
  {
    String tail = "," + theUID + "";
    int    tl   = tail.length();
    String name = theRealName + "______________________________________________";
    name = name.substring( 0, WORLD.NAME_LENGTH- WORLD.NAME_CHOP-tl);
    return name + tail;

    //return theRealName + "_[" + theUID + "]";
  }

  public String course()
  {
    return theCourse;
  }

  public String userID()
  {
    return theUID;
  }

  public String sn()
  {
    return theSN;
  }
}
