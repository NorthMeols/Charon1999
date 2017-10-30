// Start Charon
//
// (C) M.A.Smith University of Brighton
//
// Permission is granted to use this code
//   provided this declaration and copyright notice remains intact.
//
// 9 August 1999

#include <string>

#include <iostream>
#include <fstream>
#include <iomanip>

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <ctype.h>

#include <unistd.h>             // fork exec
#include <sys/wait.h>           // wait
#include <signal.h>             // kill
#include <pwd.h>

int main(int argc, char* argv[], char *env[] )
{
  if ( argc < 3 )
  {
    std::cerr << "Usage: runas user command arg(s)\n";
    exit(-1);
  }

  struct passwd *pw = getpwnam( argv[1] );
  if ( pw == NULL )
  {
    std::cerr << "Can not set to " + std::string(argv[1]) + "\n" ;
    exit(-1);
  }

  if ( pw->pw_uid < 500 )
  {
    std::cerr << "It would be too dangerous, sorry\n";
    exit(-1);
  }

  if ( std::string(argv[1]) != "charon" )
  {
    std::cerr << "It would be too dangerous, sorry [charon]\n";
    exit(-1);
  }
    
  int result = 0;
  result = setgid( pw->pw_gid );
  if ( result != 0 )
    std::cerr << "Failed to set GID (" << pw->pw_gid << ")\n";
  result = setuid( pw->pw_uid );
  if ( result != 0 )
    std::cerr << "Failed to set UID (" << pw->pw_uid << ")\n";

  std::string s;
  for ( int i=2; i<argc; i++ )
  {
    if ( i != 2 )
      s = s + " " + argv[i];
    else
      s = s + argv[i];
  }
  std::cout << "executing [" << s << "] as " << argv[1] << "\n";
  result = system( s.c_str() );


}
