// JCL_SAFE
//
// (C) M.A.Smith University of Brighton
//
// Permission is granted to use this code
//   provided this declaration and copyright notice remains intact.
//
// 9 August 1999

const int CPU_SECS = 2;        // Runs for CPU_SECS cpu seconds
const int REAL_TIME_SECS = 30; // Runs for REAL_TIME_SECS real time seconds
const int BASE_PID = 1100;     //      as PID BASE_PID .. 65354 

// Run a command file but restrict resources it can use.
// Time CPU_SECS seconds
// 

// call jcl_safe sandbox program 1st_param output_to_file
//

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

#include <sys/resource.h>
#include <sys/time.h>
#include <unistd.h>

#include <pwd.h>
#include <time.h>
#include <errno.h>

extern int errno;

void execute( char program[], char param[], char *env[]  );
void set_max( int resource, int too );
void timerInterrupt( int val );

int main(int argc, char* argv[], char *env[] )
{
  int RandomPID = 64534;                       // PID to run as
  int pid = (int)(getpid()%(64534-BASE_PID));  // 
  RandomPID = RandomPID - pid;                 // Randomize PID

  // all because problem running >=2 programs` with 2+ threads as same uid

  if ( argc >= 5 )
  {
    int result = chdir( argv[1] );
    result = chroot( "." );
    result = chdir( argv[2] );                 // To sandbox


    chmod( argv[3], 0555 );
    setgid( RandomPID );
    setuid( RandomPID );

    if ( geteuid() != RandomPID )
    {
      std::cout << "Security failure: User id wrong for process" << "\n";
      return -1;
    }

    if ( getegid() != RandomPID )
    {
      std::cout << "Security failure: Group id wrong for process" << "\n";
      return -1;
    }

    int res = open( argv[5], O_WRONLY | O_CREAT, 0644 );
  
    if ( res >= 0 )
    {
      dup2( res, 1 );                           // stdout
      dup2( res, 2 );                           // stderr
    }
    umask( 0000 );                              // So can delete files
    execute( argv[3], argv[4], env );
  }
  return 0;
}


void set_max( int resource, int too )
{
  struct rlimit a_resource;
  getrlimit( (__rlimit_resource) resource,   &a_resource );   
  a_resource.rlim_cur = too;
  int res = setrlimit( (__rlimit_resource) resource,   &a_resource );
  if ( res < 0 )
    std::cout << "Resource " << resource << " failed to set " << "\n";
}

void set_max_all( int resource, int too )
{
  struct rlimit a_resource;
  getrlimit( (__rlimit_resource) resource,   &a_resource );   
  a_resource.rlim_cur = too-1;           // Soft limit
  a_resource.rlim_max = too;             // Hard limit
  int res = setrlimit( (__rlimit_resource) resource,   &a_resource );
  if ( res < 0 )
    std::cout << "Resource " << resource << " failed to set " << "\n";
}

pid_t pid = 0;

void execute( char program[], char param[], char *env[]  )
{
  char* argv[] = { program, param, 0 };
  //alarm( CPU_SECS );                       // Safety

  set_max_all( RLIMIT_CPU,    CPU_SECS+1 ); // CPU limit
  set_max    ( RLIMIT_FSIZE,  1024*64 );    // File size limit
  set_max    ( RLIMIT_NOFILE, 30 );         // No of files limit
  set_max    ( RLIMIT_CORE,   0 );          // No core
  set_max    ( RLIMIT_NPROC,  20 );         // No of sub processes

  pid = fork();
  if ( pid == 0 )
  {
    int res = execve( program, argv, env );    // Give a restricted life
/*
    std::cout << "System Fail [jcl_safe.cpp]" << "\n" <<
                 "  execute(" << program << ",[" << param << "], env )" << " :: " <<
                 "    res = " << res << " errno = " << errno << "\n";
  
    char* ls = (char*) "/bin/ls";
    char* argv2[] = { ls, (char*)"-l", 0 };
    execve( ls, argv2, env );
*/
  }
  signal( 14, &timerInterrupt );
  alarm(REAL_TIME_SECS);
  int result;
  waitpid( pid, &result, 0 );
/*
  if ( result != 0 )
  {
    std::cout << "\n" << "Charon system: " << "\n"
              << "Your program has received signal "
              << WEXITSTATUS(result)%128 << "\n";
    std::cout << "WIFEXITED   -> " << WIFEXITED( result ) << "\n";
    std::cout << "WEXITSTATUS -> " << WEXITSTATUS( result ) << "\n";
    std::cout << "WIFSIGNALED -> " << WIFSIGNALED( result ) << "\n";
    std::cout << "WTERMSIG    -> " << WTERMSIG( result ) << "\n";
    if ( WEXITSTATUS(result)%128 == 9 )
    {
       std::cout << "Signal 9 is the kill signal" << "\n"
                 << "Most likely your program ran out of time as it is looping" << "\n";
    }
  }
*/
}

void timerInterrupt( int val )
{
  std::cout << "\n" << "Charon system: " << "\n"
                    << "Your program has been terminated" << "\n"
                    << "Real-time limit of " << REAL_TIME_SECS << " seconds exceeded" << "\n";
  kill( pid, 9 );
  exit(0);
}

