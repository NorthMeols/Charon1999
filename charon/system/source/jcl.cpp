// JCL
//
// (C) M.A.Smith University of Brighton
//
// Permission is granted to use this code
//   provided this declaration and copyright notice remains intact.
//
// 9 August 1999

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

void execute( char *env[], char* program, char* param, char *result  );

// Parameters
// 0 : This program
// 1 : Directory in which to change too.
// 2 : Shell script to run
// 3 : File to write output to (replaces stdout/ stderr )

int main(int argc, char* argv[], char *env[] )
{
  if ( argc >= 3 )
  {
    int result = chdir( argv[1] );                 // To sandbox [Usually]
    if ( argc == 4 )
    {
      // No output file requested so discard output
      execute( env, argv[2], argv[3], (char*) "/dev/null" );
    } else {
      execute( env, argv[2], argv[3], argv[4] );
    }
  }
  return 0;
}

void execute( char *env[], char* program, char* param, char *result  )
{
  char* argv[] = { program, param, 0 };
  int res = open( result, O_WRONLY | O_APPEND | O_CREAT, 0644 );

  if ( res >= 0 )
  {
    dup2( res, 1 );
    dup2( res, 2 );
  }

  pid_t pid = fork();
  if ( pid == 0 )
  {
    umask( 0000 );                          // So can delete files
    execve( program, argv, env );           // prog
  } else {
    wait(0);
  }
}
