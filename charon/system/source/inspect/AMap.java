// Charon system Mike Smith 1999-2017
package inspect;

import java.util.HashMap;

class AMap
{
  private HashMap<String,Record> theData = new HashMap<>();

  public void put( String key, Record data )
  {
    theData.put( key, data );
  }

  public Record get(String key )
  {
    if ( theData.containsKey( key ) )
    {
      return theData.get( key );
    } else {
      return new Record( key, "????" );
    }
  }
}
