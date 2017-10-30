// Charon system Mike Smith 1999-2017
package utils;

import server.Context;
import structure.Message;

import java.io.Serializable;

public interface ModuleInterface extends Serializable
{
  Message run(String name, Message mes, Context con);
}
