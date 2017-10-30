// Charon system Mike Smith 1999-2017
import java.net.*;

public class CharonLink {

  private String the_mc     = "localhost";    // Server
  private int    the_port   = 2000;           // Port

  private NetReader the_reader = null;       // Coms line reader
  private NetWriter the_writer = null;       // Coms line writer
  
  public boolean isConnected() {
    return (the_reader != null && the_writer != null);
  }
  
  public void doLogin (String user, String passwd) throws Exception {
    Message data   = new Message();
    try {
      data.setType(MessageType.M_LOGIN);
      data.put( "user",     user );
      data.put( "password", UtString.urlEncode(passwd) );
      the_writer.put( data );
    }
    catch ( Exception err )
    {
      disconnect();
      throw err;
    }
  }

  public void doSubmit (String program, String assignment, String course) throws Exception {
    try
    {
      Message data = new Message();
      data.setType(MessageType.M_PROGRAM);
      data.put( "program", program);
      data.put( "assignment", assignment);
      data.put( "course", course );
      the_writer.put( data );
    }
    catch ( Exception err )
    {
      disconnect();
      throw err;
    }
}
  
  public void doClose() {
    Message info = new Message();             // New message
    info.setType( MessageType.M_CLOSE );      //
    the_writer.put( info );                   // Send message to server
    disconnect();
  }
  
  public void doFinish() {
    Message info = new Message();           // New message
    info.setType( MessageType.M_FINISH );     //
    the_writer.put( info );                   // Send message to server
    disconnect();
  }
  
  public void doProcess(String course) throws Exception {
    try {
      Message data = new Message();
      data.setType(MessageType.M_PROCESS);
      data.put( "course", course);
      the_writer.put( data );
    }
    catch ( Exception err )
    {
      disconnect();
      throw err;
    }
  }
  
  public void doExtract(String course, String assignment) throws Exception {
    try {
      Message data = new Message();
      data.setType(MessageType.M_EXTRACT);
      data.put( "course", course);
      data.put( "assignment", assignment);
      the_writer.put( data );
    }
    catch ( Exception err )
    {
      disconnect();
      throw err;
    }
  }
  
  public void doConnect(String mc, int port) throws Exception {
    Socket socket;
    socket = new Socket( mc, port );        // Socket host.port
    the_writer = new NetWriter( socket );
    the_reader = new NetReader( socket );
  }
  
  public void doInfo() {
    try {
      Message info   = new Message();           // New message
      info.setType( MessageType.M_INFO );  //
      the_writer.put( info );                   // Send message to server
    }
    catch ( Exception err )
    {
      disconnect();
    }
  }
  
  public void doInfo1() throws Exception {
    try {
      Message info   = new Message();           // New message
      info.setType( MessageType.M_INFO_1 );  //
      the_writer.put( info );                   // Send message to server
    }
    catch ( Exception err )
    {
      disconnect();
      throw err;
    }
  }
  
  public void doInfo2() throws Exception {
    try {
      Message info   = new Message();           // New message
      info.setType( MessageType.M_INFO_2 );     //
      the_writer.put( info );                   // Send message to server
    }
    catch ( Exception err )
    {
      disconnect();
      throw err;
    }
  }
  
  public Message getMessage() {
    return the_reader.get();
  }
  
  public void disconnect()
  {
    the_writer = null;
    the_reader = null;
  }
}
