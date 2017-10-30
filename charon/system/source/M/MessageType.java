// Charon system Mike Smith 1999-2017
class MessageType
{
  public static final int M_PROGRAM      = 1;
  public static final int M_LOGIN        = 2;
  public static final int M_PROCESS      = 3;
  public static final int M_RESULT       = 4;
  public static final int M_PROGRESS     = 5;
  public static final int M_UNKNOWN      = 6;
  public static final int M_LINK_FAILURE = 7;
  public static final int M_INFO         = 8;
  public static final int M_INFO_1       = 9;
  public static final int M_INFO_2       = 15;
  public static final int M_FINISH       = 10;
  public static final int M_CLOSE        = 11;
  public static final int M_EXTRACT      = 12;
  public static final int M_SYSTEM       = 14;
  public static final int M_MODULE       = 100;

  public static String what( int type )
  {
    switch( type )
    {
      case M_PROGRAM       : return "M_PROGRAM      ";
      case M_LOGIN         : return "M_LOGIN        ";
      case M_PROCESS       : return "M_PROCESS      ";
      case M_RESULT        : return "M_RESULT       ";
      case M_PROGRESS      : return "M_PROGRESS     ";
      case M_UNKNOWN       : return "M_UNKNOWN      ";
      case M_LINK_FAILURE  : return "M_LINK_FAILURE ";
      case M_INFO          : return "M_INFO         ";
      case M_INFO_1        : return "M_INFO_1       ";
      case M_INFO_2        : return "M_INFO_2       ";
      case M_FINISH        : return "M_FINISH       ";
      case M_CLOSE         : return "M_CLOSE        ";
      case M_EXTRACT       : return "M_EXTRACT      ";
      case M_SYSTEM        : return "M_SYSTEM       ";
      case M_MODULE        : return "M_MODULE       ";
    }
    return "???";
  }

   //public static final String[] ENCRYPTION = { "SSL_DH_anon_WITH_RC4_128_MD5" };
   //public static final String[] ENCRYPTION = { "SSL_RSA_WITH_RC4_128_SHA" };
   //public static final String[] ENCRYPTION = { "TLS_DH_anon_WITH_AES_256_CBC_SHA" };
   //public static final String[] ENCRYPTION = { "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA" };
   //public static final String[] ENCRYPTION = { "TLS_DH_anon_WITH_AES_256_CBC_SHA", "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA" };
     public static final String[] ENCRYPTION = { "TLS_DH_anon_WITH_AES_128_CBC_SHA" };
   //public static final String[] ENCRYPTION = { "TLS_DH_anon_WITH_AES_256_CBC_SHA", "TLS_DH_anon_WITH_AES_128_CBC_SHA" };

}
