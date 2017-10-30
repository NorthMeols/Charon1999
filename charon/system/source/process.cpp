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

// Parameters
//     Files.F_PROCESS,             // 
//     sandbox,                     // Directory to do this in
//     data,                        // Program submitted
//     result,                      // Where to put results
//     ex_dir + Files.F_COMPILE,    // File to compile program
//     ex_dir + Files.F_RUN,        // File to run program
//     ex_dir + Files.F_DATA1,      // Data 1
//     ex_dir + Files.F_RESULT1,    // Results 1
//     ex_dir + Files.F_DATA2,      // Data 2
//     ex_dir + Files.F_RESULT2 };  // Results 2

// Format of charon system for BASE

//  programs/F_NAME                    -- Users program
//  results/F_NAME                     -- Result of processing
//  template/COURSE/EXERCISE/          -- How to compile, expected results
//                           compile
//                           run
//                           data1
//                           result1
//                           data2
//                           result2
//  
//  course/COURSE/USER/EXERCISE/      -- saved results 
//                              result
//
//  /sandbox/D_NAME                   -- Directory where program run

// File layout

#include <iostream>             // Normal I/O
#include <iomanip>              // Allows iomanipulators
#include <fstream>              // Fstream
#include <string>

#define SANDBOX   argv[1]
#define DATA      argv[2]
#define RESULT    argv[3]
#define COMPILE   argv[4]
#define RUN       argv[5]
#define COMPARE   argv[6]
#define DATA1     argv[7]
#define RESULT1   argv[8]
#define DATA2     argv[9]
#define RESULT2   argv[10]


void execute1(char *env[], char* compile, char* data, char *result);
void execute2(char *env[], char* program, char* param1, char* param2, char* result);

int main(  int argc,  char* argv[],  char * env[] )
{
  chdir( SANDBOX );                              // To sandbox

  execute1( env, COMPILE, DATA, RESULT );        // Compile program

  execute1( env, RUN, DATA1, "res1" );           // Run program
  execute1( env, RUN, DATA2, "res2" );           // Run program


  execute2( env, COMPARE, RESULT1, "res1", "r1");         // Compare
  execute2( env, COMPARE, RESULT2, "res2", "r2");         // Compare
}


void execute1( char *env[], char* program, char* param, char *result  )
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


void execute2(char *env[], char* program, char* param1, char* param2, char* result)
{
  char* argv[] = { program, param1, param2, 0 };
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


