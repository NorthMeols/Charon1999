// Charon system Mike Smith 1999-2017
import java.io.IOException;

class Main
{
  public static void main( String args[] ) 
  {
    System.out.println("#Histogram program");
    Histogram hist = new Histogram();

    try
    {
      int ch = System.in.read();
      while ( ch != -1 )
      {
         hist.add( (char) ch );
         ch = System.in.read();
      }
    }
    catch ( IOException e )
    {
      System.out.println("Error on input" );
    }
    
    hist.printF();
    hist.scale(10);
    System.out.print( hist.getHist() );
  }
}

/**
 * Create a histogram of the frequency of lowercase letters in the form
 * <PRE>
 *  10 |     *                      | 
 *   9 |     *                      | 
 *   8 |     *                      | 
 *   7 |     *              *       | 
 *   6 | *   *   *    *    **       | 
 *   5 | *   *   *    **  ***       | 
 *   4 | *   *   *    **  ***       | 
 *   3 | * ***  **  * **  ***       | 
 *   2 | * **** **  ***** ****      | 
 *   1 | *********  ***** ********  | 
 *       ++++++++++++++++++++++++++
 *       abcdefghijklmnopqrstuvwxyz
 * </PRE>
 */

class Histogram
{
  private int frequency[] = new int[36];

  /**
  * Add a character to the histogram
  * @param c Character to be included in the histogram
  */
  
  public void add( char c )
  {
    if ( c >= 'A' && c <= 'Z' )
      c = (char) (c - 'A' + 'a');
    if ( c >= 'a' && c <= 'z' )
      frequency[ (int) ( c-'a' ) ]++;
    if ( c >= '0' && c <= '9' )
      frequency[ (int) (c -'0'+ 26) ]++;
  }

  /**
   * Add the characters of a string to the histogram
   * @param str String to be included in the histogram
   */

  public void add( String str )
  {
    for ( int i=0; i<str.length(); i++ )
    {
      char c = str.charAt( i );
      add( c );
    }
  }

  public void printF()
  { int min =0, max = 0; 
    for ( int i=0; i<36; i++ )
    {
      if (  frequency[i] != 0 )
      {
        if (  i <= 25 )
          System.out.printf( "%c frequency = %6d\n", (char)(i+'a'), frequency[i] );
        if (  i >= 26 )
          System.out.printf( "%c frequency = %6d\n", (char)(i-26+'0'), frequency[i] );

        if ( frequency[i] > max ) max = frequency[i]; 
        if ( min == 0 ) min = frequency[i];
        if ( frequency[i] < min ) min = frequency[i]; 
      }
    }
    System.out.printf("Min = %d, Max = %d, range = %8.2f\n", min, max, ((double)(max-min))/max*100.0 );
  }
  
  private int theHeight = 10;

  /**
   * Scale the histogram to be n units high
   * @param n The height of the histogram
   */

  public void scale( int n )
  {
    theHeight = n;
    int highValue = 0;
    for ( int i=0; i<36; i++ )
    {
      int cur = frequency[i];
      if ( highValue < cur ) highValue = cur;
    }
    if ( highValue > 0 ) 
    {
      for ( int i=0; i<36; i++ )
      {
        double cur = frequency[i];
        double scale = cur/highValue*theHeight;
        frequency[i] = (int) (scale+0.5);
      }
    }
  }

  /**
   * Returns the histogram as a string so may be 'displayed'
   * @return Returns the histogram as a string
   */

  public String getHist()
  {
    String res = "";
    for ( int i=theHeight; i>=1; i-- ) 
    {
      res += String.format("%2d | ", i );
      for ( int c=0; c< 36; c++ ) {
        res += frequency[c] >= i ? "*" : " ";
      }
      res += " | \n";
    }
    res += "     ";
    for ( int i=0; i<36; i++ ) res += "+";
    res += "\n     ";
    for ( int i=0; i<26; i++ )
      res += (char) ( 'a' + i );
    for ( int i=0; i<10; i++ )
      res += (char) ( '0' + i );
    res += "\n";
    return res;
  }
}
