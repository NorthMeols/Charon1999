// JCL_SAFE
//
// (C) M.A.Smith University of Brighton
//
// Permission is granted to use this code
//   provided this declaration and copyright notice remains intact.
//
// 9 August 1999
//  & Later

const int CPU_SECS = 3;        // Runs for CPU_SECS CPU seconds
const int REAL_TIME_SECS = 30; // Runs for REAL_TIME_SECS real time seconds
const int BASE_PID = 1200;     //      as PID BASE_PID .. 65354 
const int MAX_PID = 64000;     // 

// Run a command file but restrict resources it can use.
// Time CPU_SECS seconds
// 

// call JCL_safe sandbox program 1st_param output_to_file
//

#include <iostream>
#include <fstream>
#include <iomanip>

#include <sys/types.h>         // Types
#include <sys/stat.h>
#include <sys/wait.h>
#include <fcntl.h>

#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <ctype.h>

#include <unistd.h>             // fork exec
#include <sys/wait.h>           // wait
#include <signal.h>             // kill
#include <grp.h>                // segroups

#include <sys/resource.h>
#include <sys/time.h>
#include <unistd.h>

#include <pwd.h>
#include <time.h>
#include <errno.h>

#include <inttypes.h>
#include <math.h>

extern int errno;

void execute( char program[], char param[], char *env[]  );
void set_max( int resource, int too );
void timerInterrupt( int val );

// Arg 0 This program
//     1 Root of the playpen
//     2 Directory in the playpen to do this in
//     3 The file to run
//     4 File containing input data
//     5 File output from the run to go to here

int main(int argc, char* argv[], char *env[] )
{

  struct timespec theTime;
  clock_gettime( CLOCK_REALTIME, &theTime ); 

  srand( (int) (theTime.tv_nsec & 0xFFFFFFF) );
  int randomPID = rand()%(MAX_PID-BASE_PID)+BASE_PID;   // PID to run as

  // all because problem running >=2 programs` with 2+ threads as same uid

  if ( argc >= 5 )
  {
    int result = chdir( argv[1] );
    if ( result != 0 )
    {
      // Of course can never see this message
      std::cout << "-Security failure: Can not chdir to root of playpen" << argv[1] << "\n";
      return -1;
    }

    result = chroot( "." );                    // chroot jail argv[1]

    result = chdir( argv[2] );                 // Playpen
    chmod( argv[3], 0755 );                    // 'run' file owner must be able to overwrite

    // Security reason for putting here

    int ignore = unlink( argv[5] );
    int resFD = open( argv[5], O_WRONLY | O_CREAT, 0644 );  // Now record what happens
    ignore = chown( argv[5], 1000, 1000 );

    if ( resFD < 0 )
    {
      std::cout << "-Security failure: in jcl_safe :: Can not create results file"  << "\n";
      return -1;
    }

    if ( resFD >= 0 )
    {
      dup2( resFD, 1 );                           // stdout
      dup2( resFD, 2 );                           // stderr
    }
    umask( 0000 );                              // So can delete files

    gid_t groups[] = { (gid_t) randomPID };
    setgroups( 1, groups );                     // Only now a member of group randomPID

    result = setregid( randomPID, randomPID );
    if ( result == -1 )
    {
      std::cout << "-Security failure: in jcl_safe :: User gid can not be set " << randomPID << "\n";
      return -1;
    }

    result = setreuid( randomPID, randomPID );
    if ( result == -1 )
    {
      std::cout << "-Security failure: in jcl_safe :: User id can not be set " << randomPID << "\n";
      return -1;
    }

    if ( getuid() != randomPID )
    {
      std::cout << "Security failure: in jcl_safe :: User id wrong for process" << "\n";
      return -1;
    }

    if ( getgid() != randomPID )
    {
      std::cout << "-Security failure: in jcl_safe :: Group id wrong for process" << "\n";
      return -1;
    }

    execute( argv[3], argv[4], env );
    close( resFD ); close(1); close(2);
  }
  return 0;
}


void set_max_soft( int resource, int too )
{
  struct rlimit a_resource;
  getrlimit( (__rlimit_resource) resource,   &a_resource );   
  a_resource.rlim_cur = too;
  int res = setrlimit( (__rlimit_resource) resource,   &a_resource );
  if ( res < 0 )
    std::cout << "Resource " << resource << " failed to set " << "\n";
}

void set_max_hard_soft( int resource, int too )
{
  struct rlimit a_resource;
  getrlimit( (__rlimit_resource) resource,   &a_resource );   
  a_resource.rlim_cur = too;             // Soft limit
  a_resource.rlim_max = too;             // Hard limit
  int res = setrlimit( (__rlimit_resource) resource,   &a_resource );
  if ( res < 0 )
    std::cout << "Resource " << resource << " failed to set " << "\n";
}

pid_t pid = 0;

void execute( char program[], char param[], char *env[]  )
{
  char* argv[] = { program, param, 0 };

  pid = fork();
  if ( pid == 0 ) 
  {
    // We are the child so less freedom
    set_max_hard_soft( RLIMIT_CPU,    CPU_SECS+1 ); // CPU limit
    set_max_soft     ( RLIMIT_CPU,    CPU_SECS );   // So can get message out
    set_max_hard_soft( RLIMIT_FSIZE,  1024*64 );    // File size limit
    set_max_hard_soft( RLIMIT_NOFILE, 30 );         // No of files limit
    set_max_hard_soft( RLIMIT_CORE,   0 );          // No core
    set_max_hard_soft( RLIMIT_NPROC,  30 );         // No of sub processes

    int res = execve( program, argv, env );         // Give a restricted life
/*
    std::cout << "System Fail [jcl_safe.cpp]" << "\n" <<
                 "  execute(" << program << ",[" << param << "], env )" << " :: " <<
                 "    res = " << res << " errno = " << errno << "\n";
  
    char* ls = (char*) "/bin/ls";
    char* argv2[] = { ls, (char*)"-l", 0 };
    execve( ls, argv2, env );
*/
    // Should never get to here
    std::cout << "-FAILED to execute program under test" << "\n";
    std::cout << "        Maybee can not execute script file \"!#/bin/bash\" missing";
    return;
  }
  signal( 14, &timerInterrupt );
  alarm(REAL_TIME_SECS);
  int result;
  waitpid( pid, &result, 0 );
/**/
  if ( result != 0 )
  {
    int sig = WEXITSTATUS(result)%128;
    switch ( sig )
    {
       case  24: std::cout << "*** CPU time limit exceeded" << "\n";
                 std::cout << " Usual reason - your program is in an endless loop" << "\n";
                 break;
    }
    std::cout << "\n" << " Details" << "\n"
              << " Your program has received signal " << sig << "\n";
    std::cout << " WIFEXITED   -> " << WIFEXITED( result ) << "\n";
    std::cout << " WEXITSTATUS -> " << WEXITSTATUS( result) << "\n";
    std::cout << " WTERMSIG    -> " << WTERMSIG( result) << "\n";
    std::cout << " WSTOPSIG    -> " << WSTOPSIG( result) << "\n";
    std::cout << " WIFSIGNALED -> " << WIFSIGNALED( result ) << "\n";
  }
/**/
}

void timerInterrupt( int val )
{
  std::cout << "\n" << "Charon system: " << "\n"
                    << "Your program has been terminated" << "\n"
                    << "Real-time limit of " << REAL_TIME_SECS << " seconds exceeded" << "\n";
  kill( pid, 9 );
  exit(0);
}
