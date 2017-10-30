// Charon system Mike Smith 1999-2017
package server;

import utils.Debug;

public class WatchDog2 extends Thread
{
  private static final int SECOND         = 1000;
  private static final int SLEEP_FOR      = 10 * SECOND;

  public void run()
  {
    while ( true )
    {
      MonitorThreads.killIdle();
      try
      {
        sleep( SLEEP_FOR );
      } catch ( InterruptedException err )
      {
        Debug.trace( err, "WatchDog2.sleep : " );
      }
    }
  }
}
