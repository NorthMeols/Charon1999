// Charon system Mike Smith 1999-2017
package utils;

public class Semaphore
{
  private int theCount = 0;

  public Semaphore( int aValue )
  {
    theCount = aValue;
  }

  public synchronized void set(int aValue)
  {
    theCount = aValue;
  }

  public synchronized int value()
  {
    return theCount;
  }

  public synchronized void inc()
  {
//T Utils.Debug.trace( 3, "Utils.Semaphore : inc : " + theCount );
    theCount++;
  }

  public synchronized boolean dec()
  {
//T Utils.Debug.trace( 3, "Utils.Semaphore : dec : " + theCount );
    if ( theCount <= 0 )
    {
      return false;
    } else {
      theCount--;
      return true;
    }
  }
}
