// Charon system Mike Smith 1999-2017
class Main
{
  public static void main( String args[] )
  {
    Message x = new Message();
    String msgs[] = {
//        "ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789 abcdefghijklmnopqrstuvwxyz",
//        "0123456789",
//        "cur027",
//        "ABC",
//        "ACCD",
//        "ADCDE",
//        "AECDEF",
        "aaaaaaaaaaaaaaaaaaa"
    };

    char start = (char) 0x3476;
    int LENGTH=65535;
    //int LENGTH=1000;

    for (char  aChar=start; aChar < start+1; aChar++ )
    {
      for ( String message: msgs )
      {
        StringBuilder text = new StringBuilder();
        for ( int i=0; i<LENGTH; i++ )
          text.append('a');
        //System.out.println( message );
        String key = "" + aChar; //  + "Key" + aChar;
        String sText = new String( text );
        x.put( key, sText );

        String dec = x.get( key );
        if ( ! dec.equals( sText ) )
        {
           System.out.printf("Failed\n%s\n%s\n", message, dec );
        }
        String eMessage = x.getEn( key );
        for ( int i = 0; i<eMessage.length(); i++ )
        {
           //System.out.printf( "%04X ", (short) eMessage.charAt(i) );
           System.out.printf( "%04X\n", (short) eMessage.charAt(i) );
        }
        System.out.println();
      }
    }
  }

}

