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

    for (char  aChar=start; aChar <= start+10; aChar++ )
    {
      for ( String message: msgs )
      {
        System.out.println( message );
        String key = "" + aChar; //  + "Key" + aChar;
        x.put( key, message );

        String dec = x.get( key );
        if ( ! dec.equals( message ) )
        {
           System.out.printf("Failed\n%s\n%s\n", message, dec );
        }
        String eMessage = x.getEn( key );
        for ( int i = 0; i<eMessage.length(); i++ )
        {
           System.out.printf( "%04X ", (short) eMessage.charAt(i) );
        }
        System.out.println();
      }
    }
  }

}

