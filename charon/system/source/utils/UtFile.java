// Charon system Mike Smith 1999-2017
package utils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class UtFile
{
  /**
   * Return true if file exists
   * @param path To file
   * @return true if file exists else false
   */

  public static boolean exists( String path )
  {
    try
    {
      File in = new File( path );
//T   Utils.Debug.trace( 3, "Exists : " + path + (in.isFile()? " T " : " F " ) );
      return in.isFile();
    }
    catch ( NullPointerException err )
    {
      return false;
    }
    catch (SecurityException err )
    {
      Debug.trace( err, "UtFile.isFile : " );
      return false;
    }
  }

  /**
   * Return true of Directory exists
   * @param path To directory
   * @return true if directory exists else false
   */

  public static boolean existsDir( String path )
  {
    try
    {
      File in = new File( path );
//T   Utils.Debug.trace( 3, "ExistsDir : " + path + (in.isFile()? " T " : " F " ) );
      return in.isDirectory();
    }
    catch ( NullPointerException err )
    {
      return false;
    }
    catch (SecurityException err )
    {
      Debug.trace( err, "UtFile.isDirectory " );
      return false;
    }
  }

  /**
   * Create a symbolic link to a file
   * Fake till SE 7 available on Ubuntu
   * @param actualFile Actual file
   * @param target     Symbolic link created
   * @return true      If all ok
   */

  public static boolean createSymLink( String actualFile, String target )
  {
    return UtFile.copyFromTo( actualFile, target );
/*
    try
    {
       File theLink   = new File( link );
       File theTarget = new File( target );
       File.createSymbolicLink(theLink, theTarget);
    }
    catch ( NullPointerException | IOException | SecurityException | UnsupportedOperationException err )
    {
      Utils.Debug.trace( err, "UtFile.createSymbolicLink " );
      return false;
    }
*/
  }

  /**
   * Create a new file containing the String contents.
   * Directories will be made, if required
   * @param path To file
   * @param contents To write to file
   * @return true if ok else false
   */

  public static boolean stringToFile( String path, String contents )
  {
     try
     {
       Files.write( Paths.get( path ), contents.getBytes() );
     }
     catch ( Exception err )
     {
       Debug.trace( 0, "UtFile.stringToFile : -> %s\n%s",
                             path, err.getMessage() );
       return false;
     }
     return true;
   }


  /**
   * Append to a file the String contents.
   * @param path To file
   * @param contents To write to file
   * @return true if ok else false
   */
  public static boolean append( String path, String contents )
  {
    try
    {
      File fPath  = new File( path );
      File dir    = new File( fPath.getParent() );
      boolean worked = dir.mkdirs();            // Make directories to file
      if ( ! worked )
      {
        Debug.trace( 0, "UtFile:append can not create dir %s", dir.getAbsolutePath() );
      }

      FileOutputStream ostream = new FileOutputStream( path, true );
      PrintWriter pw           = new PrintWriter( ostream );

      pw.print( contents );
      pw.flush(); pw.close(); ostream.close();
      return true;
    }
    catch ( NullPointerException | IOException | SecurityException err )
    {
      Debug.trace( err, "UtFile.append " );
      return false;
    }
  }

  /**
   * Make a new directory
   * @param path To directory
   * @return true if ok else false
   */

  public static boolean mkdir( String path )
  {
    try
    {
      File in = new File( path );
      return in.mkdir();
    }
    catch (SecurityException err )
    {
      Debug.trace( err, "UtFile.mkdir " );
      return false;
    }
  }

  /**
   * Delete file
   * @param path To file
   * @return true if ok else false
   */

  public static boolean delete( String path )
  {
    try
    {
      File in = new File( path );
      return in.delete();
    }
    catch (SecurityException err )
    {
      Debug.trace( err, "UtFile.delete " );
    }
    return false;
  }

  /**
   * Return number of characters in file contents
   * @param path File
   * @return length of file (contents)
   */

  public static long length( String path )
  {
    try
    {
      File in = new File( path );
      return in.length();
    }
    catch (SecurityException err )
    {
      Debug.trace( err, "UtFile.length " );
    }
    return -1;
  }


  /**
   * Recursively remove files from directory. So be careful.
   * @param dir To directory
   */

  public static void removeDir( String dir )
  {
    String[] dirList = UtFile.list( dir );
//T Utils.Debug.trace( 3, "removeDir  : " + dir );
    for ( String fileToDelete: dirList )
    {
      File curFile = new File( dir + fileToDelete );
      if ( curFile.isDirectory() )
      {
        removeDir( dir + fileToDelete + File.separatorChar );
      } else {
        UtFile.delete( dir + fileToDelete );
//T     Utils.Debug.trace( 3, "removeFile : " + dir + filesToDelete[i] );
      }
    }
    UtFile.delete(dir);
  }

  /**
   * Return a list of files in the directory (Sorted).
   * @param path To directory
   * @return array of strings
   */

  public static String[] list( String path )
  {
    try
    {
      File in = new File( path );
      String [] files = in.list();

      if ( files == null )
      {
        return new String[0];
      } else {
        Arrays.sort( files );
        return files;
      }
    }
    catch (SecurityException err )
    {
      Debug.trace( err, "UtFile.list : " );
      return new String[0];
    }
  }

  /**
   * Write String to  file
   * return +/- dependant on success
   * @param file To save too
   * @param data To save to file
   * @return "+" if ok else "-"
   */

  public static boolean saveToFile( String file, String data )
  {
    try
    {
      FileOutputStream ostream = new FileOutputStream( file );
      PrintWriter pw           = new PrintWriter( ostream );

      pw.print( data );
      pw.flush();  pw.close(); ostream.close();
      return true;
    }
    catch ( IOException e )
    {
      Debug.trace( 0, "Utfile.save File=\"%s\" %s", file, e.getMessage() );
      return false;
    }
  }

  /**
   * Return contents of file as String
   * @param file To read contenst from
   * @return contents as a string
   */

  public static String fileToString( String file )
  {
    try
    {
      return new String( Files.readAllBytes( Paths.get( file ) ) );
    }
    catch ( Exception err )
    {
      Debug.trace( 1, "UtFile.fileToBytes : " + err.getMessage() );
      return "";
    }
  }



  /**
   * Copy from fileFrom to fileTo
   * @param fileFrom To copy from
   * @param fileTo To copy too
   * @return "+" if ok else "-"
   */

  public static boolean copyFromTo( String fileFrom, String fileTo )
  {
    try
    {
        FileChannel fromC = new FileInputStream( fileFrom ).getChannel();
        FileChannel toC   = new FileOutputStream( fileTo  ).getChannel();
        toC.transferFrom(fromC, 0, fromC.size());
        fromC.close();   toC.close();
    }
    catch ( Exception err )
    {
      Debug.trace( 1, "UtFile.copy : %s -> %s", fileFrom, fileTo );
      return false;
    }
    return true;
  }

  /**
   * Increment number in file.
   * Synchronization done in static class Utils.IncrementFileCounter
   * @param name File containing integer
   * @return new contents
   */

  public static int increment( String name )
  {
    return IncrementFileCounter.file( name );
  }

  /**
   * Convert string to int
   * Failure causes a value of 0 to be returned
   * @param number Contained in a string
   * @return The 'number' as an integer
   */

  private static int toInt( String number )
  {
    int res = 0;
    try
    {
      res = Integer.parseInt( number.trim() );
    }
    catch ( NumberFormatException err )
    {
        Debug.trace( 0, "Failed toInt: " + number );
    }{
    }
    return res;
  }

  /**
   * Convert String to Date
   * Format day:month:year hour:minute:second
   * @param start String containing date
   * @return Instance of Date
   */

  public static Date toDate(String start )
  {
    int year=1970, month=1, day=1, hour=0, minute=0, second=0;
    GregorianCalendar date =
      new GregorianCalendar( year, month, day, hour, minute );

    StringTokenizer st     = new StringTokenizer( start, "/: " );
    try
    {
      final int count = st.countTokens();
      for ( int i=1; i<=count; i++ )
      {
        String token = st.nextToken();
        switch ( i )
        {
          case 1 : day    = toInt( token );
                   break;
          case 2 : month  = toInt( token )-1;
                   break;
          case 3 : year   = toInt( token );
                   break;
          case 4 : hour   = toInt( token );
                   break;
          case 5 : minute = toInt( token );
                   break;
          case 6 : second = toInt( token );
                   break;
        }
      }
      date = new GregorianCalendar( year, month, day, hour, minute, second );
    }
    catch ( NoSuchElementException err )
    {
      Debug.trace( err, "Server.Server.Progress.getProgress : " );
    }
    return date.getTime();
  }

}
