// Charon system Mike Smith 1999-2017
package utils;

import customise.VERSION;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

public class About
{
  private static Runtime    theRT   = Runtime.getRuntime();
  private static Properties theProp; // = System.getProperties();
  private static boolean    ok      = true;

  static
  {
    try
    {
      theProp = System.getProperties();
    }
    catch ( Exception e )
    {
      ok = false;
    }
  }

  private  static String statsMemory()
  {
    final double MB = (1024.0*1024.0);
    double freeMB   = theRT.freeMemory()/MB;
    double totalMB  = theRT.totalMemory()/MB;
    double maxMB    = theRT.maxMemory()/MB;
    return String.format( "Mem: Max/Total/Free %3.0f %4.3f %4.3f MB",
                          maxMB, totalMB, freeMB );
  }

  private static String statsVersionMemoryTime()
  {
    long   time = System.currentTimeMillis();
    Date when     = new Date( time );
    DateFormat dtfUk =
      DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.FULL, Locale.UK );

    return "[C] Version V" + VERSION.is +
           "    " + statsMemory() + "\n" +
           "    " + dtfUk.format( when );
  }

  public static String client()
  {
    if ( ok )
    {
      return statsVersionMemoryTime() + "\n" +
        "    Java: version = " + theProp.getProperty( "java.version" ) +
        ", class version = "   + theProp.getProperty( "java.class.version" ) + "\n" +
           String.format( "    %s-%s-%s CPU's [%d] Threads [%d]\n",
                          theProp.getProperty( "os.name" ),
                          theProp.getProperty( "os.version" ),
                          theProp.getProperty( "os.arch" ),
                          theRT.availableProcessors(),
                          Thread.activeCount() );
    } else {
     return statsVersionMemoryTime() + "\n" +
           String.format( "    CPU's (%d) Th (%d)\n",
                          theRT.availableProcessors(),
                          Thread.activeCount() ) +
            "    Not possible to get further details about the environment\n";
    }
  }
}
