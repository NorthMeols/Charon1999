// Charon system Mike Smith 1999-2017
import java.applet.Applet;

public class LightweightCharonClient extends Applet {
  private boolean    connected = false;
  private String     host      = "localhost";
  private CharonLink charon    = null;
  private String     report    = "";
  private boolean    status    = false;
  
  public void init () {
    host   = getCodeBase().getHost();
    charon = new CharonLink();
  }
  
  public void destroy () {
    if (charon != null) {
      charon.disconnect();
      System.out.println ("Applet destroyed.\n");
    }
  }

  public boolean connect () {
    try {
      charon.doConnect(host,2000);
      connected = true;
    }
    catch (Exception e) {
      report = e.getMessage();
      connected = false;
    }
    return connected;
  }
  
  public boolean login (String user, String password) {
    try {
      charon.doLogin(user,password);
      Message msg = charon.getMessage();
      status = msg.get("status").charAt(0) == '+';
      report = "Login: " + msg.get("result");
      System.out.println(report + "\n");
      return status;
    }
    catch (Exception e) {
      report = "Login: " + e.getMessage();
      System.out.println(report + "\n");
      return false;
    }
  }
  
  public boolean submit (String course, String exercise, String text) {
    try {
      charon.doSubmit(text,exercise,course.toLowerCase());
      Message msg = charon.getMessage();
      status = msg.get("status").charAt(0) == '+';
      report = "Submission: " + msg.get("result");
      System.out.println(report + "\n");
      return status;
    }
    catch (Exception e) {
      report = "Submission: " + e.getMessage();
      System.out.println(report + "\n");
      return false;
    }
  }
  
  public void close () {
    charon.doClose();
      System.out.println("Close\n");
  }
  
  public String response () {
    String s = report;
    report = "";
    return s;
  }
}
