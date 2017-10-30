// Charon system Mike Smith 1999-2017
package server;

import utils.Debug;
import customise.VERSION;

import java.util.ArrayList;

public class MonitorThreads
{
  private static ArrayList<IdleThread> threads = new ArrayList<>(20);

  public static synchronized void add( IdleThread aThread )
  {
    threads.add( aThread );
//T Utils.Debug.trace( 3, "add " + threads.size() );
  }

  public static synchronized void remove( IdleThread aThread )
  {
    if ( threads.contains( aThread ) )
    {
      threads.remove( aThread );
    } else {
      Debug.trace( 2, "Utils.Debug: removing non-existent Thread" );
    }
  }

  public static synchronized String about()
  {
//T Utils.Debug.trace( 3, "about " + threads.size() );
    String mes = "";
    for ( int i=0; i<threads.size(); i++ )
    {
       mes += i + ": " + threads.get(i).state() + "\n";
    }
    return mes;
  }

  public static synchronized void killIdle()
  {
    for ( int i=0; i<threads.size(); i++ )
    {
      final long seconds = threads.get(i).idleFor() / 1000;

      if (seconds > VERSION.TimeOut)   // Idle for more than 15 minutes then kill
      {
        IdleThread idle = threads.get(i);
        String mes = "" +  i + ": " + idle.state();
        ((Thread)idle).interrupt();                                // Kill thread
        remove( idle );                                            // So will not try again
        idle.die();                                                // Should not be required
        Debug.trace( 2, "Idle user terminated " + mes );
      }
    }
  }
}
