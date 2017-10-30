#include <stdio.h>

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

/*
int get_ch( FILE* fd )
{
  static int lastChar = 0;
  int c = getc( fd );
  while ( c <= ' '  && c >= 0  )  // Ignore non printing characters and white space characters
  {
    c = getc( fd );
  }
  //if ( c >= 'A' && c <= 'Z' ) c = c -'A' + 'a';
  return c;
}
*/

/*
 * Take multiple NL's as 1 NL, Ignore 1st NL
 * Ignore spaces and non printing characters
 */

int get_ch1( FILE* fd )
{
  static int lastCh, firstNL = true;
  int c = getc( fd );
  while ( true )
  {
    if ( c == '\r' ) c = '\n';                   // treat \r as \n
    if ( c == '\n' && firstNL )                  // Ignore first NL
    {
       c = getc( fd ); lastCh = '\n'; firstNL = false;
    }
    if ( c == '\n' && lastCh == '\n' )          // Eat multiple NL's
    {
       c = getc( fd ); continue;
    }
    if (  (c != '\n') && (c <= ' ' && c >= 0) )  // Ignore space non printing
    {
       c = getc( fd ); continue;
    }
    break;                                       // Ok character
  }
  firstNL = false; lastCh = c; return c;
}

int get_ch2( FILE* fd )
{
  static int lastCh, firstNL = true;
  int c = getc( fd );
  while ( true )
  {
    if ( c == '\r' ) c = '\n';                   // treat \r as \n
    if ( c == '\n' && firstNL )                  // Ignore first NL
    {
       c = getc( fd ); lastCh = '\n'; firstNL = false;
    }
    if ( c == '\n' && lastCh == '\n' )          // Eat multiple NL's
    {
       c = getc( fd ); continue;
    }
    if (  (c != '\n') && (c <= ' ' && c >= 0) )  // Ignore space non printing
    {
       c = getc( fd ); continue;
    }
    break;                                       // Ok character
  }
  firstNL = false; lastCh = c; return c;
}

/* Done twice as too horrible as a single function passing parameters */

int main( int argc,  char* argv[],  char* env[] )
{
  if ( argc >=3 )
  {
    int result = chdir( argv[1] );                              // Sandbox directory
    FILE* fd1 = fopen( argv[2], "r" );
    FILE* fd2 = fopen( argv[3], "r" );
    if ( fd1 == NULL || fd2 == NULL )
    {
      puts( "-System error can not open files to compare results");
      return -1;
    }

    int res = open( argv[4], O_WRONLY | O_APPEND | O_CREAT, 0644 );
    if ( res >= 0 )
    {
      dup2( res, 1 );
      dup2( res, 2 );
    }

    // Compare files
    int file1_FNL, file1_LCH;
    int file2_FNL, file2_LCH;

    int ch1 = get_ch1( fd1 );
    int ch2 = get_ch2( fd2 );
    while ( ch1 == ch2 )
    {
      if ( ch1 == -1 ) break;     
      ch1 = get_ch1( fd1 );
      ch2 = get_ch2( fd2 );
    }

    if ( ch1 == -1  && ch2 == -1 )
    {
       puts( "+" );
       return 0;
    } 
    printf( "- Not equal Expected %c found %c\n", ch1, ch2 );
    return -1;
  }
  puts("-");
}
