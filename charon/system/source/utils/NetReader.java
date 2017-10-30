// Charon system Mike Smith 1999-2017
package utils;

import structure.Message;
import structure.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class NetReader
{
  private ObjectInputStream theIn;             // Input

  public NetReader( Socket soc )
  {
    try
    {
      theIn = null;
      theIn = new ObjectInputStream( soc.getInputStream() );
    }
    catch( Exception err )
    {
      String add = "Unknown";
      try
      {
        add = soc.getInetAddress().getHostAddress();
      } catch ( Exception e )
      {
        Debug.trace( 0, "Failed NetReader: soc.getInetAddress().getHostAddress()" );
      }

      Debug.trace( err, "NetReader (open): [" +  add  + "] " );
    }
  }

  public synchronized int available()
  {
    int bytes = 0;
    try
    {
      bytes = theIn.available();
//T   Utils.Debug.trace( 3, "NetReader: available " + bytes );
    }
    catch ( IOException err )
    {
      Debug.trace( err, "NetReader (available:IO): " );

    }
    return bytes;
  }

  public synchronized Message get()
  {
    Message data = new Message();
    data.setType( MessageType.M_LINK_FAILURE );       // If failure
    if ( theIn == null ) return data;                 // Should not occur
    try
    {
//T   Utils.Debug.trace( 3, "NetReader: waiting on get");
      Object read = theIn.readObject();
      if ( read != null )
      {
        data = (Message) read;
//T     Utils.Debug.trace( 3, "NetReader: message is  : "+ data.getTypeAsString() );
        return data;
      }
      return data;
    }
    catch ( Exception err )
    {
//T   Utils.Debug.trace( err, "NetReader.get(): " );        // Connection broken
    }
    return data;
  }

  public synchronized void close()
  {
    try
    {
      if ( theIn != null ) theIn.close();
    }
    catch ( Exception err )
    {
      Debug.trace( err, "NetReader.close : " );
    }
  }
}
