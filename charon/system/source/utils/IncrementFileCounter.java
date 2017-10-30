// Charon system Mike Smith 1999-2017
package utils;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Implemented as a separate class because of static synchronization
 */

public class IncrementFileCounter
{
  public synchronized static int file( String fileName )
  {
    if ( !UtFile.exists( fileName ) )
    {
      UtFile.stringToFile( fileName, "0" );
    }
    String          contents = UtFile.fileToString( fileName );
    StringTokenizer st       = new StringTokenizer( contents );
    String num = "0";
    try
    {
      num = st.nextToken();
    }
    catch ( NoSuchElementException err )
    {
      Debug.trace( err, "Utils.UtFile.increment 1: " );
    }
    int attempt = 0;
    try
    {
      attempt = Integer.parseInt( num );
      attempt++;
      boolean worked = UtFile.saveToFile( fileName, "" + attempt );
      if ( !worked )
      {
        Debug.trace( 0, "Fail IncrementFileCounter %s by %d ", fileName, attempt );
      }
    }
    catch ( NumberFormatException err )
    {
      Debug.trace( err, "Utils.UtFile.increment 2: " );
    }
    return attempt;
  }
}
