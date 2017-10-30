// Charon system Mike Smith 1999-2017
package utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class Debug
{
  private static int theTraceLevel = 3;

  public synchronized static void state(int level)
  {
    theTraceLevel = level;
  }

  public static void trace( String str )
  {
     Debug.trace( 2, str );
  }

  public static void trace( int level, String str )
  {

    if ( level <= theTraceLevel )
    {
      Date now       = new Date();                   // Time now
      DateFormat df  = DateFormat.getDateTimeInstance( DateFormat.MEDIUM,
                                                       DateFormat.MEDIUM,
                                                       Locale.UK );
      String message = df.format( now ) + " : " + str;
      synchronized ( Debug.class )
      {
        System.out.println( message );
      }
    }
  }

  public static void trace( int level, String fmt, Object... params )
  {
    if ( level <= theTraceLevel )
    {
      synchronized ( Debug.class )
      {
        try
        {
          String message = String.format( fmt, params );
          trace( level, message );
        } catch ( Exception err )
        {
          System.out.printf( "Exception trace: %2d %s\n", level, fmt );
        }
      }
    }
  }


  public static void trace( Exception err, String str )
  {
    Date now       = new Date();                   // Time now
    DateFormat df  = DateFormat.getDateTimeInstance( DateFormat.MEDIUM,
                                                     DateFormat.MEDIUM,
                                                     Locale.UK );
    String message = df.format( now ) + " : " +
                      str + " : - " + Debug.trace( err );
    synchronized ( Debug.class )
    {
      System.out.println( message );
    }
  }

  public static String trace( Exception err )
  {
    StringBuilder message = new StringBuilder( err.getLocalizedMessage() + " [\n" );
    StackTraceElement[] st = err.getStackTrace();
    for ( StackTraceElement ste: st )
    {
     message.append( "    " );
     message.append( String.format( "%-25.25s ",    ste.getFileName()  ) );
     message.append( String.format( "Line %5d   ",  ste.getLineNumber()  ) );
     message.append( ste.getClassName()  );
     message.append(  ".");
     message.append( ste.getMethodName() );
     message.append( "\n" );
    }
    message.append( "    ]\n" );
    return new String(message);
  }

}
