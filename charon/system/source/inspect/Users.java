// Charon system Mike Smith 1999-2017
package inspect;

import java.io.*;

class Users
{
  public static AMap read(String base )
  {
    AMap map = new AMap();

    try
    {
      FileInputStream istream = new FileInputStream( base );
      InputStreamReader br    = new InputStreamReader( istream );
      BufferedReader re       = new BufferedReader( br );
      StreamTokenizer sto     = new StreamTokenizer( re );

      String course = "";
      String student= "";
      String userid = "";
      String studentNUM = "00000000";
      int    field  = 1;

      sto.nextToken();
      while ( sto.ttype != StreamTokenizer.TT_EOF )
      {
        switch ( sto.ttype )
        {
           case StreamTokenizer.TT_NUMBER :
             break;
           case '"' : case '\'' :
           case StreamTokenizer.TT_WORD :
             switch ( field )
             {
               case 1 : studentNUM = sto.sval;
                        break;
               case 2 : userid = sto.sval;
                        userid = userid.toLowerCase();
                        break;
               case 4 : course = sto.sval;
                        break;
               case 5 : student= sto.sval.trim().replace("_","-").replace(" ", "-");
                        break;
               case 6 : {
                          String tmp = sto.sval.trim().replace("_","-").replace( " ", "-");
                          student= student + "," + tmp;
                        }
                        break;
             }
             field++;
             break;
           case StreamTokenizer.TT_EOL :
             break;
           default :
             if ( (char) sto.ttype == '+' )
             {
               field = 1;
               map.put( userid, new Record( student, course, userid, studentNUM ) );
               //System.out.println( userid + " " + student + " " + course );
             }
        }
        sto.nextToken();
      }
      istream.close();
    }
    catch ( IOException e )
    {
      System.out.println("Fail : " + e.getMessage() );
    }
    return map;
  }
}
