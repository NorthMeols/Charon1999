// Charon system Mike Smith 1999-2017
package utils;

public class Sleep
{
  public static void seconds(int number)
  {
    try
    {
      Thread.sleep(number*1000);
    } catch ( InterruptedException e )
    {
      Debug.trace( 1, "Sleep: seconds " + e.getMessage() );
    }
  }
}
