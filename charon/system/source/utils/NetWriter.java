// Charon system Mike Smith 1999-2017
package utils;

import utils.Debug;
import structure.Message;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetWriter
{
  private ObjectOutputStream theOut;             // Input

  public NetWriter( Socket soc )
  {
    try
    {
      theOut = null;
      theOut =  new ObjectOutputStream( soc.getOutputStream() );
    }
    catch( Exception err )
    {
      String add = "Unknown";
      try
      {
        add = soc.getInetAddress().getHostAddress();
      } catch ( Exception e )
      {
        Debug.trace( 0, "Failed Utils.NetWriter: soc.getInetAddress().getHostAddress()" );
      }
      Debug.trace( err, "Utils.NetWriter (open): [" +  add  + "] " );
    }
  }

  public synchronized boolean put( Message data )
  {
    if ( theOut == null )
    {
      Debug.trace( 1, "Utils.NetWriter (put): " + "Stream null" );
      return false;
    }
    try
    {
//T   Utils.Debug.trace( 3, "Sending message " + data.getTypeAsString() );
      theOut.writeObject( data );
      theOut.flush();
      return true;
    }
    catch ( Exception  err )
    {
      Debug.trace( err, "Utils.NetWriter.put : "  );
    }
    return false;
  }

  public synchronized void close()
  {
    try
    {
      if ( theOut != null ) theOut.close();
    }
    catch ( Exception err )
    {
      Debug.trace( err, "Utils.NetWriter.close : " );
    }
  }
}
