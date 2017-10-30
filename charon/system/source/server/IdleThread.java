// Charon system Mike Smith 1999-2017
package server;

public interface IdleThread
{
  long idleFor();               // Milliseconds idle for
  String state();               // What doing
  void die();                   // Kill by other means
}
