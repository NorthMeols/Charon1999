// Charon system Mike Smith 1999-2017
package utils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class UtString
{

  public static boolean stringOk( String str, int maxLines, int maxLength )
  {
    return str.length() <=  maxLength;
  }

  public static String urlEncode2( String str )
  {
    StringBuilder res = new StringBuilder();
    for ( int i=0; i<str.length(); i++ )
    {
      switch ( str.charAt(i) )
      {
        case ' ' : res.append( "%22" ); break;
        case ':' : res.append( "%3A" ); break;
        case '@' : res.append( "%40" ); break;
        default  : res.append( str.charAt(i) ); break;
      }
    }
    return new String( res );
  }

  public static String urlEncode( String str )
  {
/*
    String res = "";
    String hex = "0123456789ABCDEF";
    return str;
    for ( int i=0; i<str.length(); i++ )
    {
      byte ch = (byte) str.charAt(i);
      res = res + "%" + hex.charAt( (ch >> 4)&0x0F  ) +
                        hex.charAt( ch&0x0F );
    }

*/
    return str;
  }

  public static String millsToDate( long  time )
  {
    Date   when     = new Date( time );
    DateFormat dtfUk =
      DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.UK );
    return dtfUk.format( when );
  }

  private static String m2d( int no )
  {
    if ( no <= 9 )
      return "0" + no;
    else
      return "" + no;
  }

  public static String asMills( int mills )
  {
    if ( mills <=   9 ) return "00" + mills;
    if ( mills <=  99 ) return "0" + mills;
    return "" + mills;
  }

  public static String timeNow()
  {
    Date   when     = new Date();
    Calendar cal = new GregorianCalendar();
    cal.setTime( when );

    StringBuilder res = new StringBuilder(30);
    res.append( m2d( cal.get( Calendar.DAY_OF_MONTH ) ) ); res.append( '/' );
    res.append( m2d( cal.get( Calendar.MONTH ) +1 ) );     res.append( '/' );
    res.append( cal.get( Calendar.YEAR )  );               res.append( ':' );
    res.append( m2d( cal.get( Calendar.HOUR_OF_DAY ) ) );  res.append( ':' );
    res.append( m2d( cal.get( Calendar.MINUTE ) ) );       res.append( ':' );
    res.append( m2d( cal.get( Calendar.SECOND ) ) );
    return new String( res );
  }

  public static String timeNowRev()
  {
    Date  when          = new Date();
    final long inMills  = when.getTime();
    final int  mills    = (int) ( inMills % 1000 );
    Calendar cal = new GregorianCalendar();

    cal.setTime( when );
    StringBuilder res = new StringBuilder(30);
    res.append( cal.get( Calendar.YEAR )  );               res.append( '_' );
    res.append( m2d( cal.get( Calendar.MONTH ) +1 ) );     res.append( '_' );
    res.append( m2d( cal.get( Calendar.DAY_OF_MONTH ) ) ); res.append( '_' );
    res.append( m2d( cal.get( Calendar.HOUR_OF_DAY ) ) );  res.append( '_' );
    res.append( m2d( cal.get( Calendar.MINUTE ) ) );       res.append( '_' );
    res.append( m2d( cal.get( Calendar.SECOND ) ) );       res.append( '.' );
    res.append( asMills( mills)  );
    return new String( res );
  }

  public static String getTimeInMills()
  {
    Date now = new Date();
    return "" + now.getTime();
  }

}
