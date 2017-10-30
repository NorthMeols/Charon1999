// Charon system Mike Smith 1999-2017
package server;

import utils.Debug;
import utils.UtString;
import structure.NAME;
import utils.UtFile;
import structure.Message;

import java.io.IOException;

public class Program
{
  private Runtime theRuntime;
  private Context theContext;

  public Program( Context files )
  {
    theRuntime = Runtime.getRuntime();
    theContext   = files;
  }

  private String execute( String stage, String[] cmd )
  {
    try
    {
      Process pro = theRuntime.exec( cmd );
      pro.waitFor();
      //pro.destroy();
      return "+";
    }
    catch ( InterruptedException | IOException err )
    {
      Debug.trace( err, "Server.Program.execute" );
      return "-[S] " + stage + " : " + Debug.trace( err );
    }
  }

  public String process( String user, Message action )
  {
    String program   = action.get(NAME.PROGRAM);
    String exercise  = action.get(NAME.ASSIGNMENT);
    String course    = action.get(NAME.COURSE).toLowerCase();

    theContext.setExercise( exercise );

    boolean replace      = ( action.get( NAME.REPLACE ) )
                           .toLowerCase().startsWith( "replace" );
    boolean allReadyDone = UtFile.exists( theContext.student(Context.F_OK) );

    if ( allReadyDone && !replace )
    {
      return "+[S] But you have already completed " +
                   course + "/" + exercise + "\n" +
              "    change 'Submit to charon' to " +
                                 "'Replace on charon'" + "\n";
    }

    UtFile.mkdir( theContext.playpen("") );

    boolean worked = UtFile.saveToFile( theContext.playpen(Context.F_PROGRAM), program );
    if ( !worked )
    {
     UtFile.removeDir( theContext.playpen( "" ) );
     return "-[S] System Error in getProgress : Failed to save program";
    }

    // Check if exercise is on the server

    if ( ! ( UtFile.exists( theContext.template(Context.F_COMPILE) ) &&
             UtFile.exists( theContext.template(Context.F_RUN) ) &&
             UtFile.exists( theContext.template(Context.F_DATA + 1) ) &&
             UtFile.exists( theContext.template(Context.F_RESULT + 1) ) ) )
    {
      UtFile.removeDir( theContext.playpen( "" ) );
      Debug.trace( 2, "User: %s - program %s/%s exercise not present", user, course, exercise );
      return "-[S] Exercise [" + exercise + "] for course [" + course + "] not present";
    }

    //---------------------------------------------------------------------------------
    // COMPILE the users program //////////////////////////////////////////////////////
    //         Run as user charon                                                     /
    //---------------------------------------------------------------------------------

    String[] compile = {
                         theContext.program(Context.P_JCL),       // JCL
                         theContext.playpen(""),                  // Directory to do this in
                         theContext.template(Context.F_COMPILE),  // File to compile program
                         theContext.playpen(Context.F_PROGRAM),   // Server.Program
                         theContext.playpen(Context.F_COMP_OUT)   // Result of compile
                       };

//T DT.trace( "compile", compile );

    String res = execute( "compile", compile );              // Compile program
    if ( res.length() <= 0 ) res = "-No result produced compile";
    if ( res.charAt(0) == '-' )
    {
      UtFile.removeDir( theContext.playpen( "" ) );
      return res;
    }

    String compileRes = UtFile.fileToString( theContext.playpen(Context.F_COMP_OUT) );
//T Utils.Debug.trace( 3, "Answer : %s", compileRes );

    // TEST the program, by running it with several different datasets //////////////////////

    int dataSet = 1;

    while ( true )
    {
      String fData     = theContext.template(Context.F_DATA   + dataSet);  // Test Data
      String fExpected = theContext.template(Context.F_RESULT + dataSet);  // Expected output
      String fScript   = theContext.template(Context.F_SCRIPT + dataSet ); // Extra Script that may be run from run script
      String fRun      = theContext.template(Context.F_RUN  );             // Script to run to execute users program
      String fRunPre   = theContext.template(Context.F_RUN_PRE  );         // Script to run before execute (as Charon)
      String fActual   = theContext.playpen (Context.F_ACTUAL + dataSet);  // Actual result
      String fCompare  = theContext.playpen (Context.F_COMPARE+ dataSet);  // Compare result
      String fCopyData = theContext.playpen (Context.F_DATA+ dataSet);     // Data in playpen
      String fDiff     = theContext.playpen (Context.F_DIFF);              // Diff result

      if ( ! ( UtFile.exists( fExpected ) &&
               UtFile.exists( fData )      ) ) break;                      // No more test data

      worked  = UtFile.copyFromTo( fData, fCopyData );                     // Copy to playpen
      if ( ! worked )
      {
    	  Debug.trace( 0, "Server.Program.getProgress : copy Fail %s -> %s", fData, fCopyData );
      }

      //---------------------------------------------------------------------------------
      // Copy the extra script to the playpen if it exists //////////////////////////////
      // Will be executed by the run script (Writer of run script puts in request) //////
      //---------------------------------------------------------------------------------

      if ( UtFile.exists( fScript ) )
      {
        String fCopyScript   = theContext.playpen(Context.F_SCRIPT );     // The Script(n) shell script
        worked = UtFile.copyFromTo( fScript, fCopyScript );               // Copy to playpen
        if ( ! worked )
        {
    	  Debug.trace( 0, "Server.Program.getProgress : copy Fail %s -> %s", fScript, fCopyScript );
        }
      }

      //---------------------------------------------------------------------------------
      // Execute the PRE RUN Script if it exists as charon //////////////////////////////
      //---------------------------------------------------------------------------------

      if ( UtFile.exists( fRunPre ) )
      {
        String[] preRun = {
                            theContext.program(Context.P_JCL),       // JCL
                            theContext.playpen(""),                  // Directory to do this in
                            theContext.template(Context.F_RUN_PRE),  // File (name) containing pre run actions
			    "/dev/null"
                          };

        //T DT.trace( "pre run script", compile );

        res = execute( "preRun", preRun );                          // Execute pre run
        if ( res.length() <= 0 ) res = "-No result produced compile";
        if ( res.charAt(0) == '-' )
        {
          UtFile.removeDir( theContext.playpen( "" ) );
          return res;
        }
      }

      //---------------------------------------------------------------------------------
      // Execute the run script as a Random user  ///////////////////////////////////////
      //---------------------------------------------------------------------------------

      String fCopyRun = theContext.playpen(Context.F_RUN);            // The run shell script
      worked = UtFile.copyFromTo( fRun, fCopyRun );                   // Copy to playpen
      if ( ! worked )
      {
    	  Debug.trace( 0, "Server.Program.getProgress : copy Fail %s -> %s", fRun, fCopyRun );
      }

//T   Utils.Debug.trace( 3, "From D   : %s", Utils.UtFile.fileToString( fData ) );      // Utils.Debug
//T   Utils.Debug.trace( 3, "To   D   : %s", Utils.UtFile.fileToString( fCopyData ) );  // Utils.Debug
//T   Utils.Debug.trace( 3, "From R   : %s", Utils.UtFile.fileToString( fRun ) );       // Utils.Debug
//T   Utils.Debug.trace( 3, "To   R   : %s", Utils.UtFile.fileToString( fCopyRun ) );   // Utils.Debug

      String[] run = {
                       theContext.program(Context.P_JCL_SAFE), // Server.Program to run "shell script for run"
                       theContext.playpenRoot(""),             // Root of playpen
                       theContext.playpenRel(""),              // Directory to do this in
                       Context.F_RUN,                          // File to run program
                       Context.F_DATA + dataSet,               // DATA 1
                       Context.F_ACTUAL + dataSet              // Result of run
                     };

//T   DT.trace( "run" + dataSet, run );

      res = execute( "Run" + dataSet, run );           // Run of program
      if ( res.length() <= 0 ) res = "-No result produced run";
      if ( res.charAt(0) == '-' )
      {
        UtFile.removeDir( theContext.playpen( "" ) );
        return res;                                     // Failed to run
      }

      String resOfRun = UtFile.fileToString( fActual );
//T   Utils.Debug.trace( 3, "Answer : %s", resOfRun);

      //---------------------------------------------------------------------------------
      // COMPARE run output with expected output ////////////////////////////////////////
      //         use cmp
      //---------------------------------------------------------------------------------

      String[] compare = {
                           theContext.program(Context.P_COMPARE), // File to compare program
                           theContext.playpen(""),                // Directory to do this in
                           fExpected,                             // Expected
                           fActual,                               // Actual
                           fCompare                               // Result of compare
                         };

//T   DT.trace( "compare" + dataSet, compare );

      res = execute( "compare" + dataSet, compare );   // Compare program
      if ( res.length() <= 0 ) res = "-No result produced running compare";
      if ( res.charAt(0) == '-' )
      {
        UtFile.removeDir( theContext.playpen( "" ) );
        return res;
      }

      String answerCmp = UtFile.fileToString( fCompare );
//T   Utils.Debug.trace( 3, "Expected : %s", Utils.UtFile.fileToString( fExpected ) );
//T   Utils.Debug.trace( 3, "Answer   : %s",  answerCmp);

      //---------------------------------------------------------------------------------
      // DIFF    ////////////////////////////////////////////////////////////////////////
      //         Ok, it did not match so give user indication of where differences are //
      //---------------------------------------------------------------------------------

      UtFile.stringToFile( fDiff, "Contents" );
      String[] diff = {
                           theContext.program(Context.P_DIFF),    // File to compare program
                           fActual,                               // Actual result
                           fExpected,                             // Expected result
                           fDiff                                  // Result of diff
                         };

//T   DT.trace( "diff" + dataSet, diff );

      String diffRes = execute( Context.P_DIFF, diff );
      if ( diffRes.length()>0 )
      {
        if ( diffRes.charAt(0) == '-' )
          Debug.trace( 1, "Execute: Failed to run diff" );
      }

      //diffRes = UtFile.fileToString( fDiff );

      //---------------------------------------------------------------------------------
      // REPORT  back to user what went wrong                                          /
      //         Users have to work this out from actual vs. expected output            /
      //         Where would the challenge be if it said                                /
      //           On line(s) ... you should have put ....                              /
      //---------------------------------------------------------------------------------

      if ( answerCmp.length() <= 0 ) answerCmp = "-No result produced from compare";
      if ( answerCmp.charAt(0) == '-' )
      {
        Debug.trace( 2, "User: %s - program %s/%s did not work", user, course, exercise );
        String failProg =  theContext.student( Context.F_BAD + UtString.timeNowRev() ) ;
        UtFile.stringToFile( failProg, program );
        String inputData = UtFile.fileToString( fData );       // As a string Input Data
        if ( inputData.length() > 1 )
        {
           if ( inputData.charAt(0) == '#' )                   // First ch # so hide
             inputData = "";
        }

        String ans1 =  "-" +
	       "Using DataSet " + dataSet + "\n" +
               "-#### << Compilation output >> ------------------------------------------\n" +
               compileRes +
               "-------------------------------------------------------------------------\n\n" +

               "-#### << Data used as input was >> --------------------------------------\n" +
               inputData  +
               "-------------------------------------------------------------------------\n\n" +

               "-#### << Expected answer was >> -----------------------------------------\n" +
               UtFile.fileToString( fExpected )  +
               "-------------------------------------------------------------------------\n\n" +

               "-#### << Your answer [Excluding lines containing a #] >> ----------------\n" +
               resOfRun +
               "-------------------------------------------------------------------------\n\n" +

               "-#### << Differences between expected (<) your answer (>) >> ------------\n" +
               UtFile.fileToString( fDiff ) +
               "-------------------------------------------------------------------------\n\n" +
               "[S] Sorry exercise " + course + "/" + exercise + " was not correct.\n";

        //String progPrev = fileToString( theContext.playpen(Server.Context.F_SUBMITTED) );
        //Utils.UtFile.create( theContext.student(Server.Context.F_LAST_FAIL), progPrev );
        UtFile.createSymLink( failProg, theContext.student(Context.F_LAST_FAIL) );

        UtFile.removeDir( theContext.playpen( "" ) );

        if ( replace && allReadyDone )
        {
          ans1 += "    Your previous correct attempt will not be replaced.\n" +
                  "    Check the above output for why this attempt failed.";
        } else {
          ans1 += "    Check the above output for why this attempt failed.";
        }
        return ans1;
      }

      // Worked so far
      // Utils.Debug.trace( 2, dataSet + ": " + fActual );
      // Utils.UtFile.delete( fActual  ); Utils.UtFile.delete( fCompare  ); Utils.UtFile.delete( fCopyData );
      dataSet++;
    }

    // WORKED  ///////////////////////////////////////////////////////////
    //


    int rank = 0;
/*
    if ( replace && allReadyDone )
    {
      // Better to put the original time stamp from program on file
      String progPrev = UtFile.fileToString( theContext.student(Context.F_PROGRAM) );
      UtFile.stringToFile( theContext.student(Context.F_PROGRAM + "_"  + UtString.timeNowRev() ), progPrev );
    }
*/
    String when = UtString.timeNowRev();
    if ( ! allReadyDone )
    {
      rank = UtFile.increment( theContext.template(Context.F_RANK) );
      UtFile.stringToFile( theContext.student(Context.F_OK),
                     UtString.timeNow() + " " +
                     rank + " " + user + " " + course + " " + exercise + "\n"
                   );
    }


    // Save as dated working program
    worked = UtFile.saveToFile( theContext.student(Context.F_PROGRAM)+ "_" + when, program );
    if ( !worked )
    {
      UtFile.removeDir( theContext.playpen( "" ) );
      return "-[S] System Error in Program.getProgress: Failed to save users program [M1]";
    }

    // Save as current working program sym link ?
    worked =  UtFile.saveToFile( theContext.student(Context.F_PROGRAM), program );
    if ( !worked )
    {
      UtFile.removeDir( theContext.playpen( "" ) );
      return "-[S] System Error in Program.getProgress: Failed to save users program [M2]";
    }

    UtFile.removeDir( theContext.playpen( "" ) );
    Debug.trace( 2, "User: %s - program %s/%s worked %s",
                     user, course, exercise,
		             ( allReadyDone ? " All ready done replaces": "" )
               );

    String ans2 =  "+" +
           "----------Compilation output--------------------------------------------\n" +
           compileRes +
           "------------------------------------------------------------------------\n" +
           "[S] Well done! exercise: " + course + "/" + exercise + " is correct\n";

    if ( replace && allReadyDone )
    {
      ans2 += "    and replaces your previous solution.\n" +
              "    Your original submission date has not been changed.";
    } else {
      ans2 += "    and you were #" + rank + " to complete this exercise.";
    }
    return ans2;
  }

}
