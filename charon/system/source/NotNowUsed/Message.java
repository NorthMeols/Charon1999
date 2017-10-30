// Charon system Mike Smith 1999-2017
import java.io.*;
import java.util.*;

public class Message implements Serializable
{
  private static final long serialVersionUID = 2L;
  private static final String TYPE_OF_MESSAGE = "\u7329";
  private static final String MAP  = "";               // Name of map
  private static final String MESSAGE_ACT = "\uFACE";  // Actual Creation Time
  private static final String MESSAGE_ICT = "\uABCD";  // Instigator Creation time

  private static final int SIZE    = 48;         // Size of map

  private HashMap<String,Object> message;

  public Message()
  {
    Random ran = new Random();
    StringBuilder mapSB = new StringBuilder(SIZE+1);
    for ( int i=0; i<SIZE+1; i++ )
    {
      mapSB.append( (char) ran.nextInt() );
    }
    String map = new String( mapSB );
    message = new HashMap<String,Object>(24);
    message.put( MAP, map );
    message.put( TYPE_OF_MESSAGE, new Integer(MessageType.M_UNKNOWN) );
    put( "result", "-" );
    Long time = getTimeInMills();
    message.put( MESSAGE_ACT, time );
    message.put( MESSAGE_ICT, new Long(0) );
  }

  // Used to make return message
  public Message( Message originalMessage )
  {
    this();
    Long ct = (Long) originalMessage.getObject( MESSAGE_ACT );
    message.put( MESSAGE_ICT, ct );
  }

  public void makeReturnMessage()
  {
    Long ct   = (Long) message.get( MESSAGE_ACT );
    message.put( MESSAGE_ICT, ct );
  }

  private String encode( String str )
  {
    String map    = (String) get( MAP );
    final int len = str.length();
    StringBuilder encode = new StringBuilder( len );
    int start = len % (SIZE-5);
    int p = 0;
    int preEncrypt = map.charAt( SIZE );

    out:
    while ( true )
    {
      for ( int i=start; i<SIZE; i++ )
      {
        int l1 = map.charAt( i );
        for ( int j=i+1; j<SIZE; j++ )
        {
          int l2 = l1 ^ map.charAt(j);
          for ( int k=j+1; k<SIZE; k++ )
          {
            if ( p >= len ) { start = 0; break out; };
            int toEncode = str.charAt(p);
            int l3 = l2 ^ map.charAt( k ) ^ preEncrypt ^ toEncode;
            encode.append( (char) l3 );
            preEncrypt = l3;
            p++;
          }
        }
      }
    }
    return new String( encode );
  }


  private String decode( String str )
  {
    String map    = (String) get( MAP );
    final int len = str.length();
    StringBuilder encode = new StringBuilder( len );
    int start = len % (SIZE-5);
    int p = 0;
    int preEncrypt = map.charAt( SIZE );

    out:
    while ( true )
    {
      for ( int i=start; i<SIZE; i++ )
      {
        int l1 = map.charAt( i );
        for ( int j=i+1; j<SIZE; j++ )
        {
          int l2 = l1 ^ map.charAt(j);
          for ( int k=j+1; k<SIZE; k++ )
          {
            if ( p >= len ) { start = 0; break out; };
            int toDecode = str.charAt(p);
            int l3 = l2 ^ map.charAt( k ) ^ preEncrypt ^ toDecode;
            char decrypted = (char) ( l3 );
            preEncrypt = toDecode;
            encode.append( decrypted );
            p++;
          }
        }
      }
    }
    return new String( encode );
    //return encode( str );
  }

  public void setType(int type)
  {
    message.remove( TYPE_OF_MESSAGE );
    message.put( TYPE_OF_MESSAGE, new Integer(type) );
    Long ct = (Long) getObject( MESSAGE_ACT );   // Creation time
    Long time = getTimeInMills();
    message.put( MESSAGE_ACT, time );            // Turned round message
    message.put( MESSAGE_ICT, ct );              // Instigator
  }

  public int getType()
  {
    return ( (Integer) message.get( TYPE_OF_MESSAGE ) ).intValue();
  }

  public String getTypeAsString()
  {
    return MessageType.what( getType() );
  }

  public void put( String key, String data )
  {
    if ( key.equals( MAP ) )
    {
      message.put( key, data );
    } else {
      message.put( encode(key), encode(data) );
    }
  }

  public String get( String key )
  {
    if ( key.equals( MAP ) )
    {
       return message.containsKey( MAP ) ? (String) message.get( MAP ) : "";
    } else {
      String eKey = encode(key);
      if ( message.containsKey( eKey ) )
      {
        String res = (String) message.get( eKey );
        return decode( res );
      } else {
	    return "";
        //return "-[E] No data associated with key [" + key + "]";
      }
    }
  }

  public void putObject( String key, Object data )
  {
    message.put( key, data );
  }

  public Object getObject( String key )
  {
    return message.get( key );
  }

  public String toString()
  {
    return message.toString();
  }

  public long responceTime()
  {
    Long ct = (Long) message.get( MESSAGE_ICT );
    long created = 0;
    if ( ct == null )
      Debug.trace( 2, "Message.responceTime() is null" );
    else
      created = ct.longValue();
    long now = (new Date()).getTime();
    return now - created;
  }

  public Long getTimeInMills()
  {
    return new Long( (new Date()).getTime() );
  }
}
