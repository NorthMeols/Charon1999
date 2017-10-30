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

int main(int argc, char* argv[], char *env[] )
{
  if ( argc >= 2 )
  {
    chdir( argv[1] );
    execute( env, argv[2], argv[3], argv[4] );
  } else {
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
    execve( program, argv, env );           // prog
  } else {
    wait(0);
  }
}
