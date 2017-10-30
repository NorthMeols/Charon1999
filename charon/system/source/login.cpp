// Run a program in a controlled environment
//
// (C) M.A.Smith University of Brighton
//
// Permission is granted to use this code
//   provided this declaration and copyright notice remains intact.
//
// 15 September 2000

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

void execute( char user[],  char passwd[], char server[], char result[], char *env[] );

int main(int argc, char* argv[], char *env[] )
{
  if ( argc >= 5 )
  {
    int result = chdir( argv[1] );
    int res = open( argv[5], O_WRONLY | O_APPEND | O_CREAT, 0644 );

    char* args[] = { (char*)"/bin/bash", (char*)"/opt/charon/jcl/login.sh", 
                     argv[2], argv[3], argv[4], argv[5], 0 };
  
    if ( res >= 0 )
    {
      dup2( res, 1 );
      dup2( res, 2 );
    }
  
    pid_t pid = fork();
    if ( pid == 0 )
    {
      execve( "/bin/bash", args, env );           // prog
    } else {
      wait(0);
    }
  }
  return 0;
}
