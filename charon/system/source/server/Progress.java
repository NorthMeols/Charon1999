// Charon system Mike Smith 1999-2017
package server;

import utils.Debug;
import utils.UtFile;
import structure.Message;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Progress
{
  private Context theContext;

  public Progress( Context files )
  {
    theContext = files;
  }

  private String millsToDate( long time )
  {
    Date when     = new Date( time );

    DateFormat dtfUk =
      //DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.UK );
      DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.LONG, Locale.UK );

    return dtfUk.format( when );
  }

  private String make( String s, int len )
  {
    if ( s.length() > len )
      return s.substring( 0, len-1 );
    else
    {
      String spaces = "                         ";
      return (s + spaces).substring( 0, len-1 );
    }
  }

  public String getProgress(String user, String course, Message result )
  {
    String res        = "+" +
                        "[S] User " + user + " course [" + course +  "]\n" +
                        "Key \n" +
                        " Ex           - Exercise \n" +
                        " Completed    - Date & time the exercise was completed\n" +
                        " N            - You were the n'th to complete this exercise\n" +
                        " OK           - Exercise completed on time Yes/No\n" +
                        " Expected     - Date & time exercise expected to be completed by\n" +
                        "---------------------------------------------------------------\n" +
                        "Ex                Completed          N   OK Expected\n";

    int completed = 0;
    String[] files = UtFile.list( theContext.studentBase( "" ) );

//T DT.trace( "Files", files );

    for ( String file: files )
    {
      theContext.setExercise( file );
      String fOk = theContext.student(Context.F_OK);
      if ( UtFile.exists(fOk) )
      {                                                            // Completed
        completed++;
        result.put("completed", file );                        // BODGE
        String          whenStr = UtFile.fileToString( fOk );
        StringTokenizer st     = new StringTokenizer( whenStr );
        String          when   = "2000";
        String          rank   = "0";
        try
        {
          final int count = st.countTokens();
          if ( count >= 1 ) when = st.nextToken();
          if ( count >= 2 ) rank = st.nextToken().trim();
        }
        catch ( NoSuchElementException err )
        {
          Debug.trace( err, "Progress : getProgress " );
        }

        Date completedBy = UtFile.toDate( when );
        final long mills = completedBy.getTime();

        res += make(file,15) + " " + make( millsToDate(mills), 21 ) +
               "  " + make(rank, 4);

        boolean seen = false;

        if ( UtFile.exists( theContext.student(Context.F_BY_EXT) ) )
        {
          String  dateBy   = UtFile.fileToString(theContext.student(Context.F_BY_EXT) );
          Date    dueDate  = UtFile.toDate( dateBy );
          final int ok = completedBy.compareTo(dueDate);
          res += (ok <= 0 ? " Y  " : " No " ) +
                  make( millsToDate( dueDate.getTime() ), 21 );
          seen = true;
        }

        if ( !seen && UtFile.exists( theContext.template(Context.F_BY) ) )
        {
          String  dateBy   = UtFile.fileToString(theContext.template(Context.F_BY) );
          Date    dueDate  = UtFile.toDate( dateBy );
          final int ok = completedBy.compareTo(dueDate);
          res += (ok <= 0 ? " Y  " : " No " ) +
                  make( millsToDate( dueDate.getTime() ), 21 );
        }
        res += "\n";

      }
    }
    res += "---------------------------------------------------------------\n" +
           completed + " exercise(s) completed";
    return res;
  }
}
