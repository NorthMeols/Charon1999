// Charon system Mike Smith 1999-2017
package inspect;

import utils.Debug;
import utils.UtFile;

import customise.VERSION;

import java.text.DateFormat;
import java.util.*;

class Inspect
{
  private static String theBase = "/opt/charon"; // Changed by -B
  private static boolean appendSN = false;       // Append student number
  private static boolean theActualLate = false;  // Those that are allowed to be late
  private static int theFwExercise = 3;
  private static AMap theMap  = null;
  private static MapCourse mapCourse = null;
  private static ArrayList<String> original = new ArrayList<>();
  private static ArrayList<String> added = new ArrayList<>();

  public static void main( String[] args )
  {
    mapCourse = new MapCourse();

    for ( String arg: args )
    {
      if ( arg.length() >= 1 && arg.charAt(0) == '-' )
        added.add( arg );
      else
        original.add( arg );
    }

    String argsN[] = new String[ original.size() ];

    argsN = original.toArray( argsN );

    for ( String a: added )
    {
      if ( a.length() >= 2 )
      {
        if ( a.startsWith( "-B" ) )
          theBase = a.substring( 2 );
        if ( a.startsWith( "-S" ) )
          appendSN = true;
        if ( a.startsWith( "-L" ) )
          theActualLate = true;
      }
    }


    Debug.state(1);
    if ( argsN.length <= 2 )
    {
      System.out.println("Usage: Inspect mode course base.db length");
      System.out.println("       Flags -S append student number" );
      System.out.println("       Flags -B<base> Base of system is <base> not /opt/charon" );
      System.out.printf ("       Version %s\n", VERSION.is );
    } else {
      if ( argsN.length >= 4 )
      {
        try
        {
          theFwExercise = Integer.parseInt( argsN[3] );
        }
        catch ( Exception err )
        {
          Debug.trace( 0, "theFwExercise" );
        }
      }
      if ( argsN.length >= 3 )
      {
        try
        {
          theMap = Users.read( argsN[2] );
        }
        catch ( Exception err )
        {
           Debug.trace( 0, "Failed to read from map" );
        }
      }
      if ( argsN[0].length() < 1 )
         argsN[0] = "C";
      switch ( argsN[0].charAt(0) )
      {
        case 'R' : process2( argsN[1] ); break;
        case 'X' : process3( argsN[1] ); break;
        default:
        case 'C' : process( argsN[1] ); break;
      }

    }
  }

  public static String millsToDate( String mills )
  {
    long time = 0;
    try
    {
       time = Long.parseLong( mills );
    }
    catch ( NumberFormatException err )
    {
      return "Not known [" + mills + "]";
    }

    Date when     = new Date( time );

    DateFormat dtfUk =
      DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.UK );

    return dtfUk.format( when );
  }

  public static Date millsToDateO( String mills )
  {
    long time = 0;
    try
    {
       time = Long.parseLong( mills );
    }
    catch ( NumberFormatException err )
    {
      Debug.trace( 0, "Method millToDate: " + mills );
    }

    return new Date( time );
  }

  private static String make( String s, int len )
  {
    if ( s.length() > len )
      return s.substring( 0, len );
    else
    {
      String spaces = "                                                                 ";
      return s + spaces.substring( 0, len-s.length() );
    }
  }

  private static String make( String s, int len, String adjust )
  {
    if ( s.length() > len )
      return s.substring( 0, len );
    else
    {
      String spaces = "                                                                 ";
      return spaces.substring( 0, len-s.length()+1 ) + s + " " ;
    }
  }

  private static void process( String course )
  {
    //final int inspect.WORLD.NAME_LENGTH = 32;

    String[] exercises = UtFile.list( theBase + "/" + "templates" + "/" + course  );

    String[] students = UtFile.list( theBase + "/" + "students" + "/" + course );

    if ( appendSN )
      System.out.print( "00 SN #  " );
    System.out.print(make("0Student 0000", WORLD.NAME_LENGTH) );
    System.out.print( make( "", theFwExercise+1 ) );
    for ( String exercise: exercises  )
    {
      if ( UtFile.existsDir(  theBase + "/" + "templates" + "/" + course + "/" + exercise ) )
      {
        System.out.print( make( exercise, theFwExercise ) );
      }
    }
    System.out.println();

    for ( String student: students )
    {
      if ( theMap == null )
      {
         System.out.print( make( student, WORLD.NAME_LENGTH ) );
      } else {
         Record iStudent = theMap.get( student.toLowerCase() );
         String name = (iStudent.name()).replace( ' ', '_' );
         String sCourse = ( iStudent.course() + "        "  ).substring(3,7) ;
         sCourse = mapCourse.get( sCourse );
         if ( appendSN )
           System.out.print( make( iStudent.sn(), 8 ) + " " );
         System.out.print( make( ( make( name, WORLD.NAME_LENGTH- WORLD.NAME_CHOP ) + " " +
                           make( sCourse, 5 ) + " "), WORLD.NAME_LENGTH)  );
      }
      String resLine = "";
      int    fin      = 0;
      String userRoot = theBase + "/" + "students" + "/" + course + "/" + student + "/";

      for ( String exercise: exercises )
      {
        if ( ! UtFile.existsDir(  theBase + "/" + "templates" + "/" + course + "/" + exercise ) ) continue;
        if ( UtFile.existsDir( userRoot + exercise ) )
        {
          if ( UtFile.exists( userRoot + exercise + "/" + "ok" ) )
          {
              resLine += make("OK",theFwExercise);
              fin++;
          } else {
              if ( UtFile.exists( userRoot + exercise + "/" + "LastFail" ) )
                resLine += make("a",theFwExercise);         // Has extension and a LastFail
              else
                resLine += make(".",theFwExercise);         // Has extension but no attempt
          }
        } else {
          resLine += make(".",theFwExercise);
        }
      }
      System.out.println( make( ""+fin, theFwExercise ) + " " + resLine );
    }
  }


  private static String getDate( String file )
  {
    String          whenStr = UtFile.fileToString( file );              //
    StringTokenizer st     = new StringTokenizer( whenStr );
    String          when   = "2000";
    try
    {
      final int count = st.countTokens();
      if ( count >= 1 ) when = st.nextToken();
    }
    catch ( NoSuchElementException err )
    {
      Debug.trace( err, "Server.Server.Progress : getDate " );
    }
    return when;
  }

  private static void process2( String course )
  {
    //final int inspect.WORLD.NAME_LENGTH = 32;
    String[] exercises = UtFile.list( theBase + "/" + "templates" + "/" + course  );

    String[] students = UtFile.list( theBase + "/" + "students" + "/" + course );

    if ( appendSN )
      System.out.print( "00 SN #  " );
    System.out.print(make("0Student 0000", WORLD.NAME_LENGTH) );
    System.out.print(make("C", theFwExercise-1));
    System.out.print(make("L", theFwExercise-1));
    if ( theActualLate )
      System.out.print(make("C40", theFwExercise));  // Capped at 40%
    else
      System.out.print(make("TLD", theFwExercise));  // Total Late Days
    for ( int i=0; i<exercises.length; i++ )
    {
      //if ( exercises[i].length() <=theFwExercise )
      if ( UtFile.existsDir(  theBase + "/" + "templates" + "/" + course + "/" + exercises[i] ) )
      {
        System.out.print( make( exercises[i], theFwExercise ) );
      }
    }
    System.out.println();

    for ( int i=0; i<students.length; i++ )
    {
      if ( ! UtFile.existsDir( theBase + "/" + "students" + "/" + course + "/" + students[i] ) ) continue;
      if ( theMap == null )
      {
         System.out.print( make( students[i], WORLD.NAME_LENGTH ) );
      } else {
         Record student = theMap.get( students[i] );
         String name = (student.name()).replace( ' ', '_' );
         String sCourse = ( student.course() + "        "  ).substring(3,7) ;
         sCourse = mapCourse.get( sCourse );
         if ( appendSN )
           System.out.print( make( student.sn(), 8)  + " " );
         System.out.print( make( ( make( name, WORLD.NAME_LENGTH- WORLD.NAME_CHOP ) + " " +
                                         make( sCourse, 5 ) + " "), WORLD.NAME_LENGTH)  );
      }
      String resLine = "";
      int    tFinished  = 0;
      int    tExLate   = 0;
      int    tDaysLate = 0;
      for ( int j=0; j<exercises.length; j++ )
      {
        if ( ! UtFile.existsDir(  theBase + "/" + "templates" + "/" + course + "/" + exercises[j] ) ) continue;
        String userRoot = theBase + "/" + "students" + "/" + course + "/" + students[i] + "/";
        if ( UtFile.existsDir( userRoot + exercises[j] ) )
        {

          String okFile = userRoot + exercises[j] + "/" + "ok";
          if ( UtFile.exists( okFile ) )                           // program works
          {
             String when = getDate( okFile );
             tFinished++;                                          // Finished exercise
             String exBy    = theBase + "/" + "templates" + "/" + course + "/" + exercises[j] + "/" + "by";
             String exByExt = userRoot + exercises[j] + "/" + "byExt";

             String dateBy = null;                                 // When should exercise be done by

             if ( UtFile.exists( exByExt ) )
             {
               dateBy = UtFile.fileToString( exByExt );            // Special date (Set in student dir)
             } else
             if ( UtFile.exists( exBy ) )
             {
               dateBy = UtFile.fileToString( exBy );               // Regular extension date
             }

             if ( dateBy != null )
             {
               Date  co       = UtFile.toDate( when );
               Date  by       = UtFile.toDate( dateBy );
               final long mCo = co.getTime();                          // Completed
               final long mBy = by.getTime();                          // Expected completion


               if ( mCo <= mBy )
               {
                 resLine += make("OK", theFwExercise );                // Ok by date
               } else {
                 long daysLate = (mCo-mBy)/(1000 * 60 * 60 * 24);
                 long pdayLate = (mCo-mBy)%(1000 * 60 * 60 * 24);
                 if ( pdayLate != 0 ) daysLate++;                      // Treat partial day as day

                 if ( theActualLate )
                 {
                   tExLate++;
                   if ( daysLate <= 14 )
                     tDaysLate++;            // Can have 40% of mark
                 } else {
                   tDaysLate += daysLate;    // Used as total
                   tExLate++;
                 }
                 resLine +=  make( ("" + daysLate).trim(), theFwExercise );
               }
             } else {
                 resLine += make("ok", theFwExercise );                             // No by date set
             }
          } else {                                                                  // Server.Program does not work
            if ( UtFile.exists( userRoot + exercises[j] + "/" + "LastFail" ) )
              resLine += make("a",theFwExercise);         // Has extension and a LastFail
            else
              resLine += make("e",theFwExercise);         // Has extension but no attempt
          }
        } else {
          resLine += make(".",theFwExercise);
        }
      }
      System.out.println( make( "" + tFinished, theFwExercise-1 )  +
                          make( "" + tExLate,   theFwExercise-1 )  +
                          make( "" + tDaysLate, theFwExercise-0 ) + resLine );
    }
  }


  private static void process3( String course )
  {

    String exercise = theBase + "/" + "templates" + "/" + course  + "/" ;
    String student  = theBase + "/" + "students" + "/" + course + "/";

    String[] students  = UtFile.list( student );
    String[] exercises = UtFile.list( exercise  );

    for ( int ex=0; ex < exercises.length; ex++)
    {
      System.out.println(": +++++++ " + exercises[ex]);
      for ( int i=0; i < students.length; i++ )
      {
        for ( int j=i; j < students.length; j++ )
        {
          if ( i == j ) continue;
          String file1 = student + students[i] + "/" + exercises[ex] + "/program";
          String file2 = student + students[j] + "/" + exercises[ex] + "/program";
          if ( UtFile.exists( file1 )  && UtFile.exists( file2) )
            System.out.println( "sh X " + file1 + "  " + file2 );
        }
      }
    }
  }

}
