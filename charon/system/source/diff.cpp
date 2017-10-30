// Run a program in a controlled environment
//
// (C) M.A.Smith University of Brighton
//
// Permission is granted to use this code
//   provided this declaration and copyright notice remains intact.
//
// 15 September 2011

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

//void execute( char user[],  char passwd[], char server[], char result[], char *env[] );

int main(int argc, char* argv[], char *env[] )
{
  if ( argc >= 1 )
  {
    int res = open( argv[3], O_WRONLY | O_CREAT, 0644 );

    char* args[] = { (char*) "/usr/bin/diff", (char*) "-Bw", 
                     argv[2], argv[1], 0 };
  
    if ( res >= 0 )
    {
      dup2( res, 1 );
      dup2( res, 2 );
    }
  
    pid_t pid = fork();
    if ( pid == 0 )
    {
      execve( args[0], args, env );           // prog
    } else {
      wait(0);
    }
    close( res ); 
  }
  return 0;
}
