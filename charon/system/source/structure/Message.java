// Charon system Mike Smith 1999-2017
package structure;

import utils.Debug;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class Message implements Serializable
{
  private static final long serialVersionUID = 2L;
  private static final String TYPE_OF_MESSAGE = "\u7329";
  private static final String MAP  =        "";        // Name of map
  private static final String MESSAGE_ACT = "\uF01D";  // Actual Creation Time
  private static final String MESSAGE_ICT = "\uAF18";  // Instigator Creation time
  private static final int    INITIAL_SEED= 0xB7CE;    // Initial Seed
  private static final String MAGIC_KEY   = "\uF4B7";  // Key name
  private static final String MAGIC_TEXT  = "ChArOn";  // Check compatibility

  private static final int SIZE_MAP       = 47;        // Size of map
  private static final int MAX_START      = 41;        // Max start in map

  private HashMap<String,Object> message;

  public Message()
  {
    Long time = getTimeInMills();
    Random ran = new Random();
 
    char mapS[] = new char[ SIZE_MAP ];
    int start = ran.nextInt() >>> 3;
    for ( int i=0; i<SIZE_MAP; i++ )
    {
      int  index  = (start+i)%SIZE_MAP;
      mapS[index] = (char) (ran.nextInt() ^ (ran.nextInt()>>(i%7) ) );
    }

    String map = new String( mapS );
    message = new HashMap<>(24);
    message.put( MAP, map );
    message.put( TYPE_OF_MESSAGE, MessageType.M_UNKNOWN );
    put( "result", "-" );
    message.put( MESSAGE_ACT, time );
    message.put( MESSAGE_ICT, time );
    this.put( MAGIC_KEY, MAGIC_TEXT );
//  Utils.Debug.trace( 0, "structure.Message() act %d\n", time );
  }

  // Used to make return message
  public Message( Message originalMessage )
  {
    this();
    Long act = (Long) originalMessage.getObject( MESSAGE_ACT );
    message.put( MESSAGE_ACT, act );
    message.put( MESSAGE_ICT, act );
//  Utils.Debug.trace( 0, "structure.Message( message ) ict %d\n", act.longValue() );
  }

  public void makeReturnMessage()
  {
    Long act   = (Long) message.get( MESSAGE_ACT );
    message.put( MESSAGE_ICT, act );
//  Utils.Debug.trace( 0, "structure.Message:makeReturnMessage ict/act %d\n", act.longValue() );
  }

  public boolean messageOK()
  {
    String checkMessage = get( MAGIC_KEY );
//  Utils.Debug.trace( 0, "MAGIC: [%s] [%s]", MAGIC_TEXT, checkMessage );
    return checkMessage.equals( MAGIC_TEXT );
  }

  public long responseTime()
  {
    Long ict = (Long) message.get( MESSAGE_ICT );
    //Long act = (Long) message.get( MESSAGE_ACT );
    long created = 0;
    if ( ict != null )
      created = ict;
    else
      Debug.trace( 2, "structure.Message.responseTime() is null" );

    long now = (new Date()).getTime();
//  Utils.Debug.trace(0, "responseTime: ict %d act %d now %d %d\n", created, act.longValue(), now, now-created );
    return now - created;
  }

  private Long getTimeInMills()
  {
    return new Date().getTime();
  }


  private String encodeK( String key )
  {
    return encode( INITIAL_SEED, key );
  }

  private String decodeK( String str )
  {
    return decode( INITIAL_SEED, str );
  }

  private String encode( final int iSeed, String str )
  {
    String map    = get( MAP );
    if ( map == null ) return "";
    final int len = str.length();
    StringBuilder encode = new StringBuilder( len );
    int start     = iSeed % MAX_START;
    int p         = 0;     // Char to encode

    int seed = iSeed;
    out:
    while ( true )
    {
      for ( int i=start; i<SIZE_MAP; i++ )
      {
        int l1 = map.charAt( i );
        for ( int j=i+1; j<SIZE_MAP; j++ )
        {
          int l2 = l1 ^ map.charAt(j);
          for ( int k=j+1; k<SIZE_MAP; k++ )
          {
            int l3 = l2 ^ map.charAt(k);
            for ( int l=k+1; l<SIZE_MAP; l++ )
            {
              if ( p >= len ) break out;
              int disguise = str.charAt(p);
              int l4 = l3 ^ map.charAt( l ) ^ disguise;
              encode.append( (char) ( seed ^ l4 ) );
              seed = (char) ( disguise ^ ( ( seed  * disguise ) >> (l%5) ) );
              p++;
            }
          }
        }
      }
      start = 0;
    }
    return new String( encode );
  }


  private String decode( final int iSeed, String str )
  {
    String map    = get( MAP );
    if ( map == null ) return "";
    final int len = str.length();
    StringBuilder encode = new StringBuilder( len );
    int start     = iSeed % MAX_START;
    int p = 0;

    int seed = iSeed;
    out:
    while ( true )
    {
      for ( int i=start; i<SIZE_MAP; i++ )
      {
        int l1 = map.charAt( i );
        for ( int j=i+1; j<SIZE_MAP; j++ )
        {
          int l2 = l1 ^ map.charAt(j);
          for ( int k=j+1; k<SIZE_MAP; k++ )
          {
            int l3 = l2 ^ map.charAt(k);
            for ( int l=k+1; l<SIZE_MAP; l++ )
            {
              if ( p >= len ) break out;
              int l4 = l3 ^ map.charAt( l ) ^ str.charAt(p);
              int plain = l4 ^ seed;
              encode.append( (char) ( plain  ) );
              seed = (char) ( plain ^  ( seed * plain ) >> (l%5) );
              p++;
            }
          }
        }
      }
      start = 0;
    }
    return new String( encode );
  }

  public void setType(int type)
  {
    message.remove( TYPE_OF_MESSAGE );
    message.put( TYPE_OF_MESSAGE, type );
    //Long ct = (Long) getObject( MESSAGE_ACT );   // Creation time
    //Long time = getTimeInMills();                // Current time
    //message.put( MESSAGE_ACT, time );            // Turned round message
    //message.put( MESSAGE_ICT, ct );              // Instigator
  }

  public int getType()
  {
    return (Integer) message.get( TYPE_OF_MESSAGE );
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
      int seed = INITIAL_SEED;
      for ( int i=0; i<key.length(); i++ )
      {
        int k = key.charAt(i);
        seed = seed ^ ( ( k * seed) >> (i%5) );
      } 
      seed = seed & 0xFFFF;
//    Utils.Debug.trace( 0, "Seed = %04X", seed );
//    Utils.Debug.trace( 0, "put key = %s", key );
      message.put( encodeK(key), encode(seed, data) );
    }
  }

  public String get( String key )
  {
    if ( key.equals( MAP ) )
    {
       String map = message.containsKey( MAP ) ? (String) message.get( MAP ) : "";
       if ( map.length() == SIZE_MAP )
         return map;
       else
         return null;
    } else {
      String eKey = encodeK( key );
//    Utils.Debug.trace( 0, "get key = %s", key );
      if ( message.containsKey( eKey ) )
      {
        int seed = INITIAL_SEED;
        for ( int i=0; i<key.length(); i++ )
        {
          int k = key.charAt(i);
          seed = seed ^ ( (k * seed) >> (i%5) );
        }
        seed = seed & 0xFFFF;
//      Utils.Debug.trace( 0, "Seed = %04X", seed );
        String res = (String) message.get( eKey );
        return decode( seed, res );
      } else {
//      Utils.Debug.trace( 0, "key = %s [No data associated]", key );
        return "";
        //return "[E] No data associated with key [" + key + "]";
      }
    }
  }

/*
  public String getEn( String key )
  {
    if ( key.equals( MAP ) )
    {
       return message.containsKey( MAP ) ? (String) message.get( MAP ) : "";
    } else {
      String eKey = encodeK( key );
//    Utils.Debug.trace( 0, "get key = %s", key );
      if ( message.containsKey( eKey ) )
      {
        String res = (String) message.get( eKey );
        return res;
      } else {
//      Utils.Debug.trace( 0, "key = %s [No data associated]", key );
        return "";
        //return "[E] No data associated with key [" + key + "]";
      }
    }
  }
*/

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
}
