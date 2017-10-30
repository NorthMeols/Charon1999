// Charon system Mike Smith 1999-2017
package server;

import utils.Debug;
import utils.UtFile;

import java.io.IOException;

public  class Login
{
  private Runtime theRuntime;
  private Context theContext;

  public Login( Context files)
  {
    theRuntime  = Runtime.getRuntime();
    theContext = files;
  }

  private String execute( String stage, String[] cmd )
  {
    try
    {
      Process pro = theRuntime.exec( cmd );
      pro.waitFor();
      //pro.destroy();
      return "+[S] " + stage ;
    }
    catch ( InterruptedException | IOException err )
    {
      Debug.trace( err, "Login.execute" );
      return "-[S] " + stage + " : " + Debug.trace( err );
    }
  }

  public String verify( String user, String passwd, String course,
                        String hostAddress, Info info )
  {
    if ( user.contains( "/" ) )
      return "-[S] *** User name cannot contain a / character";

//T   if ( passwordServer.equals("null") )
//T      return "+[S] NO CHECKS regarded as being logged in as [" + user + "]";


   if ( ! UtFile.mkdir( theContext.playpen( "" ) ) )
   {
     Debug.trace( 1, "MAJOR SYSTEM ERROR failed to create directory in playpen partition full/permissions");
     return "-[S] *** SYSTEM ERROR failed to create directory in playpen";
   }

    String[] args = {
                         theContext.program(Context.P_LOGIN),   // Server.Program
                         theContext.playpen( "" ),              // Directory to do this in
                         user,                                  // User
                         passwd,                                // Password
                         "mas",                                 // Unused
                         theContext.playpen(Context.F_RESULT)   // Output
                       };

//T DT.trace( "verify", args, 3 );

    String res = execute( "verify", args );              // Check login
    if ( res.charAt(0) == '-' )
    {
      UtFile.removeDir( theContext.playpen( "" ) );
      return res;
    }

    String loginOut = UtFile.fileToString( theContext.playpen(Context.F_RESULT) );
//T Utils.Debug.trace( 3, "Answer : " + loginOut );
    if ( loginOut.length() < 1 )
    {
       loginOut = "-User name and/or password invalid";
    }

    final boolean canLogin =  loginOut.charAt(0) == '+';

    UtFile.removeDir( theContext.playpen( "" ) );
    if ( canLogin )
    {
      Debug.trace( 2, "User: %s - Login from [%s] %s", user, hostAddress, info.stats()  );
      return "+[S] WELCOME " +  user +  " taking course " + course +
             ".\n    You are now logged onto the charon server.";
    }
    Debug.trace( 2, "User: %s - Failed to login %s from [%s]",
                    user, loginOut.substring(1).replace('\n', ' '),
                    hostAddress );
    return "-[S] *** YOU ARE NOT LOGGED ONTO THE CHARON SYSTEM ***\n" +
           "        " + loginOut.substring(1);
  }

//-  public String verify8( String user, String passwd, String passwordServer )
//-  {
//-    if ( passwordServer.equals("null") )
//-      return "+[S] NO CHECKS regarded as being logged in as [" + user + "]";
//-
//-    Utils.UtFile.mkdir( theContext.playpen( "" ) );
//-
//-    String args[] = {
//-                         theContext.program(Server.Context.P_LOGIN),   // Server.Program
//-                         theContext.playpen( "" ),              // Directory to do this in
//-                         user,                                   // User
//-                         passwd,                                 // Password
//-                         passwordServer,                        // Password server
//-                         Server.Context.F_FTP_FILE1,                    // File to try and get 1
//-                         Server.Context.F_FTP_FILE2,                    // File to try and get 2
//-                         theContext.playpen(Server.Context.F_RESULT)   // Output
//-                       };
//-
//-    DT.trace( "verify", args, 3 );
//-
//-    String res = execute( "verify", args );              // Check login
//-    if ( res.charAt(0) == '-' )
//-    {
//-      Utils.UtFile.removeDir( theContext.playpen( "" ) );
//-      return res;
//-    }
//-
//-    String loginOut = Utils.UtFile.fileToString( theContext.playpen(Server.Context.F_RESULT) );
//-    Utils.Debug.trace( 3, "Answer : " + loginOut );
//-
//-    boolean canLogin =  Utils.UtFile.exists( theContext.playpen(Server.Context.F_FTP_FILE1) ) ||
//-                         Utils.UtFile.exists( theContext.playpen(Server.Context.F_FTP_FILE2) ) ;
//-
//-    Utils.UtFile.removeDir( theContext.playpen( "" ) );
//-    if ( canLogin )
//-    {
//-      Utils.Debug.trace( 2, "User: " + user + " - Logged in <1>" );
//-      return "+[S] You are now logged into the charon server as [" + user + "]";
//-    }
//-    Utils.Debug.trace( 2, "User: " + user + " - Failed to login" );
//-    return "-[S] *** Password invalid for user [" + user + "] you are not logged in";
//-  }
}
