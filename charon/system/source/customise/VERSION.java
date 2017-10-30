// Charon system Mike Smith 1999-2017
package customise;

public class VERSION
{
  public final static String is      = "2017-10-24";         // Date
  public final static String server  = "193.62.183.45";      // IP of server
  public final static int    port    = 50000;                // Port used by client/server to communicate
  public final static String sysroot = "/opt/charon/";       // Root of Charon system

  private static final int SECOND    = 1000;
  private static final int MINUTE    = 60 * SECOND;
  public  static final int TimeOut   = MINUTE*16;         // Logout the user if no interaction in TimeOut milliseconds

  public  static final boolean DEBUG = false;             // Extra debug information
}
