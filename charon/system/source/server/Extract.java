// Charon system Mike Smith 1999-2017
package server;

import utils.Debug;
import utils.UtFile;
import structure.Message;

public class Extract
{
  private Context theContext;

  public Extract( Context files )
  {
    theContext = files;
  }

  public void process(Message mes, String user, String course, String exercise )
  {
    theContext.setExercise( exercise );
/*
    if ( ! Utils.UtFile.exists( theContext.student(Server.Context.F_OK) ) )
    {
      mes.put( "error", "[S] But you have not completed " + course + "/" + exercise );
      mes.put( "result", "" );
      return;
    }
*/
    if ( UtFile.exists( theContext.student(Context.F_PROGRAM) ) )
    {
      Debug.trace( 2, "User: %s - program %s/%s retrieved", user, course, exercise );
      mes.put( "result", UtFile.fileToString( theContext.student(Context.F_PROGRAM) ) );
    } else

    if ( UtFile.exists( theContext.student(Context.F_LAST_FAIL) ) )
    {
      Debug.trace( 2, "User: %s - Non working program %s/%s retrieved", user, course, exercise );
      mes.put( "result", UtFile.fileToString( theContext.student(Context.F_LAST_FAIL) ) );
    } else {

      mes.put( "error", "[S] But you have not attempted " + course + "/" + exercise );
      mes.put( "result", "" );
    }
  }
}
