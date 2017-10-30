// Run a program in a controlled environment
//
// (C) M.A.Smith University of Brighton
//
// Permission is granted to use this code
//   provided this declaration and copyright notice remains intact.
//
// 5 August 1999

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

void execute( char user[],  char passwd[], char server[], 
              char file1[], char file2[], char result[], char *env[] );

int main(int argc, char* argv[], char *env[] )
{
  if ( argc >= 7 )
  {
    chdir( argv[1] );
    int res = open( argv[7], O_WRONLY | O_APPEND | O_CREAT, 0644 );
  
    if ( res >= 0 )
    {
      dup2( res, 1 );
      dup2( res, 2 );
    }
    execute( argv[2], argv[3], argv[4], argv[5], argv[6], argv[7], env );
  } else {
    cout << "no" << "\n";
  }
  return 0;
}


void execute( char user[],  char passwd[], char server[], 
              char file1[], char file2[], char result[], char *env[] )
{
  char buffer[1000]; buffer[0] = '\0';

  strcat( buffer, "wget ftp://" );             // File 1
  strcat( buffer, user );
  strcat( buffer, ":" );
  strcat( buffer, passwd );
  strcat( buffer, "@" );
  strcat( buffer, server );
  strcat( buffer, "/" );
  int last = strlen( buffer );
  strcat( buffer, file1 );

  system( buffer );                           // wget File 1

  buffer[last] = '\0';
  strcat( buffer, file2 );

  system( buffer );                           // wget File 2

}
