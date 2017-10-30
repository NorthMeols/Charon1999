// Charon system Mike Smith 1999-2017
package server;

import java.io.File;
import java.util.UUID;

public class Context
{
                                                    // Programs
  public static String P_COMPARE   =  "compare";
  public static String P_JCL       =  "jcl";
  public static String P_JCL_SAFE  =  "jcl_safe";
  public static String P_LOGIN     =  "login";
  public static String P_XXX       =  "diff";
  public static String P_DIFF      =  "diff";

                                                    // File names
  public static String F_ACTUAL    =  "actual";
  public static String F_BY        =  "by";
  public static String F_BY_EXT    =  "byExt";      // Optional
  public static String F_COMPARE   =  "compare";
  public static String F_DIFF      =  "diff";
  public static String F_COMPILE   =  "compile";
  public static String F_COMP_OUT  =  "resOfComp";
  public static String F_DATA      =  "data";
  public static String F_PROGRAM   =  "program";
  public static String F_LAST_FAIL =  "LastFail";
  public static String F_BAD       =  "Bad_";
  public static String F_RANK      =  "completed";
  public static String F_RESULT    =  "result";
  public static String F_RUN       =  "run";
  public static String F_RUN_PRE   =  "runPre";    // Optional
  public static String F_SCRIPT    =  "script";    // Optional

  public static String F_OK        =  "ok";
  //public static String F_FTP_FILE1 =  ".cshrc";
  //public static String F_FTP_FILE2 =  ".bashrc";

  private String theFBase          = "-base-";
  private String theProgram        = "-bin-";
  private String thePlaypen        = "-playpen-";
  private String thePlayPenBase    = "-playpen-";
  private String thePlaypenRel     = "-playpen-";
  private String theCourseDir      = "-template-";
  private String theStudentDir     = "-student-";
  private String theStudentBaseDir = "-student-";
  private String theExerciseDir    = "-student-";
  private String theTemplatesBase  = "-templates-";
  private String theStudent        = "-student-";
  private char   theFsc            = '/';


  public Context( String base )
  {
    theFsc            = File.separatorChar;
    theFBase          = base;
    theProgram        = theFBase + "bin" + theFsc;
    theCourseDir      = "";
    theStudentDir     = "";
    theStudentBaseDir = "";
    theStudent        = "";
    theExerciseDir    = "-";
  }

  public void setStudent( String aStudent )
  {
    thePlayPenBase = theFBase + "playpen" + theFsc ;
    thePlaypenRel  = "sandbox" + theFsc + "D_" + aStudent + "_" +
                      UUID.randomUUID() + theFsc;
    thePlaypen     = thePlayPenBase + thePlaypenRel;
    theStudent     = aStudent;
  }

  public void setCourse( String course )
  {
    theTemplatesBase  = theFBase + "templates" + theFsc;
    theCourseDir      = theTemplatesBase + course + theFsc;
    theStudentBaseDir = theFBase + "students"  + theFsc + course + theFsc +
                           theStudent + theFsc;
  }

  public void setExercise( String exercise )
  {
    theExerciseDir= theCourseDir  + exercise + theFsc;
    theStudentDir = theStudentBaseDir + exercise + theFsc;
  }

  public String baseOfSystem( String file )
  {
    return theFBase + file;
    //return "/opt/charon/" + file;
  }

  public String program( String aProgram )
  {
    return theProgram + aProgram;
  }

  public String playpen( String file )
  {
    return thePlaypen + file;
  }

  public String playpenRel( String file )
  {
    return thePlaypenRel + file;
  }

  public String playpenRoot( String file )
  {
    return thePlayPenBase + file;
  }

  public String student( String file )
  {
    return theStudentDir + file;
  }

  public String studentBase( String file )
  {
    return theStudentBaseDir + file;
  }

  public String template( String file )
  {
    return theExerciseDir + file;
  }

  public String templateCourseBase( String file )
  {
    return theCourseDir + file;
  }
}
