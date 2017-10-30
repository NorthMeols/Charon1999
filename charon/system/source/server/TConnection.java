// Charon system Mike Smith 1999-2017
package server;

import customise.VERSION;
import utils.*;
import structure.*;

import java.net.Socket;

public  class TConnection extends Thread implements IdleThread
{
  //private static final int SECOND         = 1000;
  //private static final int MINUTE         = 60 * SECOND;
  //private static final int TIME_OUT_AFTER = 15 * MINUTE;
  private static final String MOTD = "motd.txt";

  private Socket theSocket;                   // Socket used
  private Semaphore theStats;
  private Context   theContext;
  private String    theBase;
  private String    theHostAddress = "";
  private String    theState = "***";
  private String    theUserIs = null;
  private long      theTimeLastAction = 0;
  private NetReader theIn = null;
  private NetWriter theOut = null;
  private boolean   theConnectionDead = false;

  private synchronized void setTime()
  {
    theTimeLastAction  = System.currentTimeMillis();   // Time of last action
  }

  public synchronized long idleFor()
  {
    if ( theTimeLastAction == 0 ) return 0;
    return System.currentTimeMillis() - theTimeLastAction;
  }

  public synchronized String state()
  {
    return "[" + ( theUserIs == null ? "NONE": theUserIs ) + "] " + theState;
  }

  public synchronized void die()
  {
    theConnectionDead = true;
  }

  public TConnection(String base, Socket s, Semaphore stats, String hostAddress  )
  {
//T Utils.Debug.trace( 3, "Constructor TConnection");
    theSocket          = s;
    theStats           = stats;
    theBase            = base;
    theHostAddress     = hostAddress;
  }

  public void run()                                 // Execution
  {
    try
    {
//T   Utils.Debug.trace( 3, "Thread started" );
      theSocket.setSoTimeout( VERSION.TimeOut);    // Max time wait
      setTime();                                    // Set time of last action

      theUserIs  = null;                            // No user yet
      theStats.inc();                               // 1 extra getProgress
      theState = "Started  receive loop";
      Info information = new Info( theStats );
//T   Utils.Debug.trace( 3, "S %s %s", theHostAddress, information.stats() );
//T   Utils.Debug.trace( 3, "Server.Server getProgress running" );
      theOut = new NetWriter( theSocket );
//T   Utils.Debug.trace( 3, "Net writer setup");
      theIn  = new NetReader( theSocket );
//T   Utils.Debug.trace( 3, "Net reader setup");

      theContext = new Context( theBase );

      loop: while ( true )
      {
         theContext = new Context( theBase );
//T      Utils.Debug.trace( 3, "Waiting for client message");
         setTime();                                // Set time of last action

         Message action = theIn.get();             // From Client
         if ( theConnectionDead ) break loop;      // Killed

         String course = action.get(NAME.COURSE).toLowerCase();

         Message result = new Message( action );

         final int actionType = action.getType();
//T      Utils.Debug.trace( 3, "structure.Message : %s", structure.MessageType.what( action.getType() ) );

         // Messages where you do not need to be logged in
         // Utils.Sleep.seconds(2);

         switch ( actionType )
         {
           case  MessageType.M_LINK_FAILURE:
           case  MessageType.M_FINISH:
             break loop;
           case  MessageType.M_CLOSE:
             result.setType( MessageType.M_CLOSE );
             result.makeReturnMessage();
             theOut.put( result );                       // Send to client
             break loop;

           case  MessageType.M_INFO:
             String motd = "NO message of the day\n";
             if ( UtFile.exists( theContext.baseOfSystem(MOTD) ) )
               motd = UtFile.fileToString( theContext.baseOfSystem(MOTD) );
             result.setType( MessageType.M_INFO );
             result.put( "result", action.get( "Client" ) +
                                   information.aboutMax() + motd  );
             theOut.put( result );                       // Send to client
             continue loop;

           case  MessageType.M_INFO_1:
             result.setType( MessageType.M_SYSTEM );
             result.put( "result", action.get( "Client" ) +
                                   information.aboutMax() );
             theOut.put( result );                       // Send to client
             continue loop;

           case  MessageType.M_INFO_2:
             result.setType( MessageType.M_SYSTEM );
             result.put( "result", "None" );
             theOut.put( result );                   // Send to client
             continue loop;

           case  MessageType.M_LOGIN:
             String lmotd    = "";
             String user     = action.get(NAME.USER).toLowerCase().trim();
             String passwd   = action.get(NAME.PASSWORD);
             theContext      = new Context( theBase );
             theContext.setStudent( user );
             theContext.setCourse( course );
             Login lo      = new Login( theContext );
             String res    = lo.verify( user, passwd, course, theHostAddress, information );
             passwd = null;  action = null;           // Just to be safe
             if ( res.charAt(0) ==  '+'  )
             {
               theUserIs = user;
               if ( UtFile.exists( theContext.templateCourseBase(MOTD) ) )
               {
                 lmotd = UtFile.fileToString( theContext.templateCourseBase(MOTD) );
               } else {
                 if ( UtFile.existsDir( theContext.templateCourseBase("") ) )
                 {
                   lmotd = "";
                 } else {
                   lmotd = String.format( "[S] WARNING Course [%s] is not recognised\n", course );
                 }
               }
//T            Utils.Debug.trace( 9, "User: %s - Loged in ", theUserIs );
               result.put("loggedIn", "yes" );
               result.put("courseMotd", lmotd );
             } else {
               // Failed to login
               result.put("loggedIn", "no" );
               theUserIs  = null;
               theContext = new Context( theBase );   // Just in case
               Sleep.seconds(5);
             }
             result.setType( MessageType.M_RESULT );
             result.put("status", res.substring(0,1) );
             result.put("result", res.substring(1) + "\n" + lmotd );
             theOut.put( result );                    // Send to client
             continue loop;
/*
           case structure.MessageType.M_MODULE:
             Object main    = new Object();
             String course = action.get(structure.NAME.COURSE).toLowerCase();
             String failure = null;
             try
             {
                Class cl = Class.forName(action.get("module"));
                main = cl.newInstance();
                theContext.setStudent( theUserIs );
                theContext.setCourse( course );
                Utils.ModuleInterface mi = (Utils.ModuleInterface) main;

                structure.Message mes = mi.run(theUserIs, action, theContext);
                theOut.put( mes );
             }
             catch ( ClassNotFoundException e )
             {
               failure = "Class not found: " + e.getMessage();
             }
             catch ( InstantiationException e )
             {
               failure = "Cannot instantiate class: " + e.getMessage();
             }
             catch ( IllegalAccessException e )
             {
               failure = "Illegal access: " + e.getMessage();
             }
             catch ( Exception e )
             {
               failure = "Exception: " + e.getMessage();
             }
             if ( failure != null )
             {
                action.put( "windowInfo", "-[S] Failed - " + failure );
                theOut.put( action );
             }
             continue loop;
 */
         }

         if ( theUserIs == null )
         {
           result.setType( MessageType.M_RESULT );
           result.put( "result", "[S] But you are not logged in");
           theOut.put( result );                       // Send to client
           continue loop;
         }

         // Messages where you do need to be logged in

         switch( actionType )
         {
           case  MessageType.M_PROGRAM:

             String resOfRun = "-[E] default";

             String program    = action.get(NAME.PROGRAM);
             //String assignment = action.get(structure.NAME.ASSIGNMENT);
             String course1 = action.get(NAME.COURSE).toLowerCase();

             if ( UtString.stringOk( program, 3000, 100000 ) )
             {
               theContext.setStudent( theUserIs );
               theContext.setCourse( course1 );
               Program p = new Program( theContext );
               resOfRun = p.process( theUserIs, action ) + "    ";

             } else {
               resOfRun = "-[S] Server.Program too long";
             }

             result.setType( MessageType.M_RESULT );
             result.put("result", resOfRun.substring(1) );
             result.put("status", resOfRun.substring(0,1) );
             theOut.put( result );                       // Send to client
             continue loop;

           case  MessageType.M_EXTRACT:
             //int fail =1/0;
             String eAssignment = action.get(NAME.ASSIGNMENT);
             String eCourse     = action.get(NAME.COURSE).toLowerCase();

             theContext.setStudent( theUserIs );
             theContext.setCourse( eCourse );

             Extract ex = new Extract( theContext );
             ex.process( result, theUserIs, eCourse, eAssignment );
             result.setType( MessageType.M_EXTRACT );
             theOut.put( result );                       // Send to client
             continue loop;

           case  MessageType.M_PROCESS:
             Progress pr = new Progress( theContext );
             theContext.setStudent( theUserIs );
             theContext.setCourse( action.get(NAME.COURSE).toLowerCase() );
             String resProgress =
		    pr.getProgress( theUserIs, action.get(NAME.COURSE).toLowerCase(), result ) + "  ";
             result.setType( MessageType.M_PROGRESS );
             result.put("result", resProgress.substring(1) );
             result.put("status", resProgress.substring(0,1) );
             theOut.put( result );                       // Send to client
             //continue loop;
         }
      }

      //String who = theUserIs==null ? "???" : theUserIs;
      // Did not login so why record
      if ( theUserIs != null )
        Debug.trace( 2, "User: %s - Logged out %s",
                        theUserIs, information.stats() );
      try
      {
        theIn.close();                                    // Close Read
        theOut.close();                                   // Close Write
      } catch ( Exception e )
      {
        // Ignore as usually result of other error on stream
      }

//T   Utils.Debug.trace( 3, "End of clients thread" );
    }
    catch ( Exception err )
    {
      Debug.trace( err,  "Exception in client thread: " );
//      err.printStackTrace();
      Message sorry = new Message();
      sorry.setType( MessageType.M_CLOSE );
      sorry.makeReturnMessage();
      sorry.put( "result", "[S] SYSTEM FAILURE You have been Logged out\n    " +
                           "Can you mail all of this text to mas@brighton.ac.uk\n    " +
	                    Debug.trace( err ) );
      theOut.put( sorry );                             // Send to client
      // int crash =1/0;
      try
      {
         if ( theIn != null ) theIn.close();
         if ( theOut!= null ) theOut.close();
      } catch ( Exception e )
      {
        Debug.trace( 0, "Can not close socket" );
      }
    }
    finally
    {
      theStats.dec();
      MonitorThreads.remove( (IdleThread) currentThread() );
    }
  }
}
