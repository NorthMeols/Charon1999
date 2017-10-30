// Charon system Mike Smith 1999-2017
package server;

import utils.Semaphore;
import customise.VERSION;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

public class Info
{
  private Runtime    theRT;
  private Semaphore theStats;
  private Properties theProp;

  public Info( Semaphore aStat )
  {
    theRT    = Runtime.getRuntime();
    theProp  = System.getProperties();
    theStats = aStat;
  }

  public String stats()
  {
    final double MB = (1024.0*1024.0);
    double freeMB   = theRT.freeMemory()/MB;
    double totalMB  = theRT.totalMemory()/MB;
    double maxMB    = theRT.maxMemory()/MB;
    return String.format( "Mem: Max/Total/Free %4.0f %4.0f %4.0f MB Users=%1d",
                          maxMB, totalMB, freeMB, theStats.value() );
  }

  public String about()
  {
    long   time = System.currentTimeMillis();
    Date when     = new Date( time );
    DateFormat dtfUk =
      DateFormat.getDateTimeInstance( DateFormat.FULL, DateFormat.FULL, Locale.UK );

    return "[S] Version V" + VERSION.is + " " + stats() + "\n" +
           "    " + dtfUk.format( when );
  }

  public String aboutMax()
  {
    return about() + "\n" +
           "    Java: version = " + theProp.getProperty( "java.version" ) +
           ", class version = "   + theProp.getProperty( "java.class.version" ) + "\n" +
           String.format( "    %s-%s-%s - CPU's [%d] Threads [%d]\n",
                          theProp.getProperty( "os.name" ),
                          theProp.getProperty( "os.version" ),
                          theProp.getProperty( "os.arch" ),
                          theRT.availableProcessors(),
                          Thread.activeCount() );
//         "    O/S: name = "     + theProp.getProperty( "os.name" ) +
//         ", version  = "        + theProp.getProperty( "os.version" ) +
//         ", arch = "            + theProp.getProperty( "os.arch" ) + "\n" +
//         "    Thread(s) = " + Thread.activeCount() +
//         ", CPU(s) = " + theRT.availableProcessors() +"\n";
           //Server.MonitorThreads.about();
  }
}
