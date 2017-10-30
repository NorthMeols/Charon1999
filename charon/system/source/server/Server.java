// Charon system Mike Smith 1999-2017
package server;

import utils.Debug;
import utils.Semaphore;
import structure.MessageType;
import customise.VERSION;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.net.InetAddress;


public class Server
{
  private static Semaphore theStats;
  private static int    thePort = VERSION.port;
  private static String theBase = VERSION.sysroot;

  public static void main( String[] args )
  {
    Debug.state(2);                             // DEBUG upto 2
    theStats = new Semaphore(0);
    try
    {
      for ( int i = 0; i< args.length; i++ )
      {
        switch ( i )
        {
          case 0 :
            thePort = Integer.parseInt( args[0] );
	    break;
          case 1 :
            theBase = args[1];
	    break;
          default:
        }
      }
      process();
    }
    catch ( Exception e )
    {
      Debug.trace( 0, "Server parameters: Port=%d Base=%s", thePort, theBase );
      Debug.trace( e, "Error: " );
    }
    Debug.trace( 0, "Usage server port charonBase" );
  }

  private static void process()
  {
    //Utils.Debug.add( currentThread() );
    Debug.trace( 2, "Restarted server: Port=%5d Base=%s", thePort, theBase );
    WatchDog2 wd  = new WatchDog2();             // Watch dog timer
    wd.start();                                  // Start the timer
    SSLServerSocket socket = null;               // Server Socket
    try
    {
      SSLServerSocketFactory ssf;
      ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

      socket = (SSLServerSocket) ssf.createServerSocket(thePort);

      // Note anon authentication

      socket.setEnabledCipherSuites( MessageType.ENCRYPTION );

      //ServerSocket socket = new ServerSocket( thePort );
      while( true )
      {
        SSLSocket connection = (SSLSocket) socket.accept(); //  Wait for connection
        //Socket connection = socket.accept();              // Wait for connection

        InetAddress ia = connection.getInetAddress();
        String hostAddress = ia.getHostAddress();
        String hostName    = ia.getHostName();
//T     Utils.Debug.trace( 3, "Connection from address %s [%s]", hostAddress, hostName );

        TConnection thread =
          new TConnection(theBase, connection,  theStats, hostAddress );
        MonitorThreads.add( thread );
        thread.start();                          // Start thread
      }
    }
    catch ( Exception e )
    {
      // Should not fail
      // Will fail if serverport is already used (running program twice?)
      Debug.trace( 0, "MAJOR SYSTEM ERROR" );
      Debug.trace( e, "Exception is" );
      try
      {
        if ( socket != null ) socket.close();
      } catch ( Exception err2 )
      {
        Debug.trace( 0, "Can not close socket" );
      }
    }
  }

}
