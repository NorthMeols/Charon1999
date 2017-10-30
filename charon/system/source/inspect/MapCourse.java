// Charon system Mike Smith 1999-2017
package inspect;

import java.util.HashMap;

class MapCourse
{
  private HashMap<String,String> theData = new HashMap<>();

  public MapCourse()
  {
    theData.put( "3585", "SE" );
    theData.put( "3589", "CS" );
    theData.put( "4316", "CSG" );
    theData.put( "3754", "BIS" );
    theData.put( "4407", "BCS" );
    theData.put( "4408", "EC" );
    theData.put( "3759", "DMD" );
    theData.put( "4314", "DM" );  // Check
    theData.put( "4048", "MATH" );
    theData.put( "4597", "CS-M" );
    theData.put( "4598", "SE-M" );
    theData.put( "4599", "BCS-M" );
    theData.put( "7218", "CSyC" );
    theData.put( "3757", "Compt" );
    theData.put( "7526", "DM" );
    theData.put( "3930", "IC" );
    theData.put( "5088", "DGP" );
    theData.put( "7207", "CSN" );


  }

  public void put( String key, String data )
  {
    theData.put( key, data );
  }

  public String get( String key ) {
    return theData.getOrDefault(key, key);  // If key does not exist in map return key
  }
}
