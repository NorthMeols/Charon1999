// Charon system Mike Smith 1999-2017
package client;

import utils.*;
import structure.Message;
import structure.MessageType;
import structure.NAME;
import customise.VERSION;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class Client extends JFrame
{
  private static final long serialVersionUID = 2;
	
  private static final int W = 650;         // Width  of window
  private static final int H = 600;         // Height of window

  private String theMc       = VERSION.server; // Server IP
  private int    thePort     = VERSION.port;   // Port
  private String theCourse   = "NotKnown";  // Course
  private String theExercise = "";          // Exercise


  private NetReader theReader = null;       // Coms line reader
  private NetWriter theWriter = null;       // Coms line writer
  private Panel cardsStack;


  private TextArea   theProgress;            // TextArea  for progress list
  private TextAreaSS theInfo;                // TextArea  for Information
  private TextArea   theInfoD;               // TextArea  for Information (Detached)
  private TextArea   theSystem;              // TextArea  for System information

  private TextField  theC1Course;
  private TextField  theC1User;
  private TextField  theC1Passwd;
  private Button     theC1Login;

  private TextArea   theC2Program;
  private TextField  theC2Assignment;
  private JSpinner   theC2Replace;
  private Button     theC2Submit;
  private Button     theC2Detach;

  private TextArea   theC4Program;
  private TextField  theC4Assignment;
  private Button     theC4Submit;

  private final String B_LOGIN     = "Logon panel";
  private final String B_SUBMIT    = "Submit panel";
  private final String B_PROGRESS  = "Progress";
  private final String B_RETRIEVE  = "Retrieve";
  private final String B_DISCONNECT= "Logout";
  private final String B_CONNECT   = "Connect/Server";
  private final String B_SYSTEM    = "About";
  //-private String B_DOCK2     = "Dupe/UnDupe";
  //-private String B_UNDOCK2   = "Undock";

  private final String B_DOCK      = "Dock windows to web page";
  private final String B_UNDOCK    = "UnDock & expand windows";

  private final Color iBGColor = new Color( 255, 250, 223 ); // Input BG color
  private final Color oBGColor = new Color( 227, 243, 255 ); // Output BG color

  private final Color buttonBGColor = new Color( 195, 195, 215 );
  private final Color buttonFGColor = new Color( 0, 0, 0 );
  //private Color buttonBGColor = new Color( 0, 0, 128 );
  //private Color buttonFGColor = new Color( 255, 255, 255 );

  private ServerConnection theServerCon = null;

  private WatchDog theWD = null;
  private Semaphore limitOfSentMessages = new Semaphore(1);

  private Panel choice2;


  public static void main( String args[] )
  {
     (new Client()).startA( args );
  }

  public void startA( String args[] )
  {
    Debug.trace(1, "Application: constructor");
    Debug.state(1);
    int i = 0;
    while ( i < args.length )
    {
      switch ( args[i] )
      {
        case "-localhost" :
        case "--localhost" :
          theMc = "localhost"; break;
        case "-port" :
        case "--port" :
          thePort = VERSION.port;
          String port = "";
          try
          {
            if ( ++i >= args.length ) break;
            port = args[i].trim();
            thePort = Integer.parseInt( port );
          }
          catch ( NumberFormatException err )
          {
            Debug.trace( 1, "Application: port number " + port + 
                            " invalid using " +thePort );
          }
      }
      i++;
    }
    Debug.trace( 1, "Application: start V%s", VERSION.is );
    createGUI();
    Debug.trace( 1, "Application: MC=%s Port=%d Course=%s", theMc, thePort, theCourse );

    connectionOk();
    stopWatchDog();
    theWD = new WatchDog();
    theWD.start();
  }

  private Button MButton( String s )
  {
    Button b = new Button( s );
    b.setFont( new Font( "SansSerif", Font.BOLD, 12 ) );
    b.setBackground( buttonBGColor );
    b.setForeground( buttonFGColor );
    return b;
  }

  private TextField I_TextField( String str, int length )
  {
    TextField t = new TextField();
    t.setFont( new Font( "Serif", Font.BOLD, 12 ) );
    t.setBackground( iBGColor );
    t.setForeground( new Color( 20, 5, 96 ) );
    t.setColumns( length );
    t.setText( str );
    return t;
  }

  private JSpinner I_JSpinner( String values[] )
  {
    SpinnerCircularListModelC slm = new SpinnerCircularListModelC( values );
    return new JSpinner( slm );
  }

  private Label O_Label( String str )
  {
    Label t = new Label( str );
    t.setFont( new Font( "Serif", Font.BOLD, 12 ) );
    t.setBackground( Color.white );
    t.setForeground( new Color( 0, 128, 128 ) );
    return t;
  }

  private TextArea I_TextArea( String s, int rows, int columns )
  {
    TextArea t = new TextArea( s, rows, columns,
                               TextArea.SCROLLBARS_VERTICAL_ONLY );
    t.setFont( new Font( "MonoSpaced", Font.PLAIN, 12 ) );
    t.setBackground( iBGColor );
    t.setForeground( Color.black );
    t.setEditable( true );
    return t;
  }
/*
  private TextArea DI_TextArea( String s, int rows, int columns )
  {
    Frame x = new Frame();
    TextArea t = new TextArea( s, rows, columns,
                               TextArea.SCROLLBARS_VERTICAL_ONLY );
    t.setFont( new Font( "MonoSpaced", Font.PLAIN, 12 ) );
    t.setBackground( iBGColor );
    t.setForeground( Color.black );
    x.add(t);
    x.setVisible(true);
    return t;
  }
*/
  private TextAreaSS O_TextArea( String s, int rows, int columns )
  {
    TextAreaSS t = new TextAreaSS( s, rows, columns,
                               TextArea.SCROLLBARS_VERTICAL_ONLY );
    t.set( theInfoD );
    t.setFont( new Font( "MonoSpaced", Font.PLAIN, 12 ) );
    t.setBackground( oBGColor );
    t.setForeground( Color.black );
    t.setEditable( false );
    return t;
  }

  private static Frame progFrame = null;

  private void makeProgramPanel( String contents, String exercise, boolean floatFrame )
  {
    if ( floatFrame )
    {
      if ( progFrame != null ) return;
      progFrame = new Frame();
      progFrame.setTitle( "Program" );
      progFrame.setSize( 600, 700 );
      progFrame.setLocation( 600, 10 );
      progFrame.toFront();
      progFrame.setFocusable(true);
      progFrame.setLayout( new BorderLayout() );
      progFrame.requestFocus();
    } else {
      if ( progFrame != null ) progFrame.setVisible( false );
      progFrame = null;
    }

    choice2 = new Panel();
    choice2.setLayout( new BorderLayout() );
    theC2Program    = I_TextArea(contents,15,80);
    theC2Submit     = MButton("Submit exercise #");
    theC2Assignment = I_TextField( exercise, 4);
    //theC2Replace    = I_TextField("not replacing", 12);
    theC2Replace    = I_JSpinner( new String[]
                        { "Submit to charon  ", "Replace on charon" } );
    theC2Detach     = MButton(floatFrame ? B_DOCK : B_UNDOCK );
    Panel choice2s  = new Panel();
    choice2s.setLayout( new FlowLayout() );
    choice2s.add( theC2Detach );
    choice2s.add( theC2Submit );
    choice2s.add( theC2Assignment );
    //choice2s.add( O_Label( "to Charon" ) );
    choice2s.add( theC2Replace );
    // choice2s.add( O_Label( "any working solution" ) );
    choice2.add( choice2s,     BorderLayout.NORTH );
    choice2.add( theC2Program, BorderLayout.CENTER );
    ButtonPressed2  c2b = new ButtonPressed2();
    ButtonPressed2d c2d = new ButtonPressed2d();
    theC2Submit.addActionListener( c2b );
    theC2Detach.addActionListener( c2d );

    if ( floatFrame )
    {
      progFrame.add( choice2, BorderLayout.CENTER );
      progFrame.setVisible( true );
    } else {
      if ( cardsStack != null )
        cardsStack.add( B_SUBMIT,   choice2 );
    }
  }

  private static Frame resultsFrame = null;

  private void makeResultsFrame( String contents )
  {
    if ( resultsFrame != null ) return;
    resultsFrame = new Frame();
    resultsFrame.setTitle( "Results" );
    resultsFrame.setSize( 600, 700 );
    resultsFrame.setLocation( 0, 10 );
    resultsFrame.toFront();
    resultsFrame.setLayout( new BorderLayout() );
    theInfoD = O_TextArea( contents, 10,80 );
    resultsFrame.add( theInfoD, BorderLayout.CENTER );
    resultsFrame.setVisible( true );
    theInfo.set( theInfoD );
  }

  class WatchDog extends Thread
  {
    private static final int SECOND             = 1000;
    private static final int MINUTE             = 60 * SECOND;
    private static final int TIME_OUT_AFTER     = 15 * MINUTE;
    private static final int WATCHDOG_SLEEP_FOR = 30 * SECOND;
    private int       count = 0;
    private boolean   active = true;

    public void run()
    {
      while ( active )
      {
        if ( timedOut() )
        {
          if ( theServerCon != null )
          {
            Message info   = new Message();
            info.setType( MessageType.M_CLOSE );
            theServerCon = null;
            theInfo.append( "[C] Connection timed-out " + "\n" );
            if ( theWriter != null ) theWriter.put( info );
          }
         reset();
        }

        try
        {
          sleep( WATCHDOG_SLEEP_FOR );
          if ( !active ) return;
        }
        catch ( InterruptedException err )
        {
          Debug.trace( err, "sleep : " );
          count = TIME_OUT_AFTER;
        }
        inc();
      }
    }

    public synchronized void inc()
    {
      count++;
    }

    public synchronized void reset()
    {
      count= 0;
    }

    public synchronized boolean timedOut()
    {
      return count > TIME_OUT_AFTER/WATCHDOG_SLEEP_FOR;
    }

    public synchronized void terminate()
    {
      active = false;
    }
  }

  private class WindowClosed extends WindowAdapter
  {
    public void windowClosing( WindowEvent e)
    {
      try
      {
        Message info  = new Message();            // New message
        info.setType( MessageType.M_CLOSE );      //
        limitOfSentMessages.set(1);
        theServerCon = null;
        theWriter.put( info );                    // Send message to server
        Thread.sleep(1000);
        System.exit(0);
      }
      catch ( Exception err )
      {
        theInfo.append( "[C] Communications failure : " +
                         err.getMessage() + "\n" );
        theServerCon = null;
        System.exit(0);
      }
    }
  }

  public void createGUI()
  {
    Debug.trace( 2, "Application: init");
    //removeAll();
    setSize( W+0, H+20 );
    setBackground( Color.white );
    setFont( new Font( "SansSerif", Font.BOLD, 12 ) );

    Panel panel = new Panel();                  // For border layout
    panel.setSize(W, H);
    panel.setLayout( new BorderLayout() );      //
    panel.setBackground( Color.white );

    Panel  choiceBar  = new Panel();            // Choice BAR
    choiceBar.setLayout( new FlowLayout() );

    //- Button cbB0 = MButton( B_UNDOCK2 );
    Button cbB1 = MButton( B_LOGIN );
    Button cbB2 = MButton( B_SUBMIT );
    Button cbB3 = MButton( B_PROGRESS );
    Button cbB4 = MButton( B_RETRIEVE );
    Button cbB5 = MButton( B_DISCONNECT );
    Button cbB6 = MButton( B_CONNECT );
    Button cbB7 = MButton( B_SYSTEM );

    ChoiceAction ct = new ChoiceAction();     // Called on change
    //-cbB0.addActionListener( new ButtonPressed0() );
    cbB1.addActionListener( ct );
    cbB2.addActionListener( ct );
    cbB3.addActionListener( ct );
    cbB4.addActionListener( ct );
    cbB5.addActionListener( new ButtonPressed5() );
    cbB6.addActionListener( new ButtonPressed6() );
    cbB7.addActionListener( ct );

    //choiceBar.add( cbB0 );
    choiceBar.add( cbB1 );
    choiceBar.add( cbB2 );
    choiceBar.add( cbB3 );
    choiceBar.add( cbB4 );
//  choiceBar.add( O_Label    ( "Server" ) );
    choiceBar.add( cbB5 );
//  choiceBar.add( cbB6 );
    choiceBar.add( cbB7 );

    // Panel #1                                 // Password

    Panel choice1 = new Panel();
    choice1.setLayout( new FlowLayout() );

    theC1Course    = I_TextField( "", 10);
    theC1User      = I_TextField( "", 10);
    theC1Passwd    = I_TextField( "", 10);

    theC1Passwd.setEchoChar( '#' );
    theC1Login  = MButton("login");
    choice1.add( O_Label("Course") );
    choice1.add( theC1Course );
    choice1.add( O_Label("User") );
    choice1.add( theC1User );
    choice1.add( O_Label("Password") );
    choice1.add( theC1Passwd );
    choice1.add( theC1Login );
    ButtonPressed1 c1b = new ButtonPressed1();
    theC1Login.addActionListener( c1b );
    CharTyped passwdInput = new CharTyped();
    theC1Passwd.addKeyListener( passwdInput );
    theC1User.requestFocusInWindow();

    // Panel #2                                // Submit


    makeProgramPanel( "", theExercise, false );

    // Panel #3                                // Performance

    Panel choice3 = new Panel();
    choice3.setLayout( new BorderLayout() );
    theProgress  = O_TextArea ("",15,80 );
    choice3.add( theProgress, BorderLayout.SOUTH );
    Button theC3Progress = MButton("Show progress so far");

    Panel choice3s  = new Panel();
    choice3s.setLayout( new FlowLayout() );
    choice3s.add( O_Label( "Progress so far" ) );
    choice3s.add( theC3Progress );
    choice3.add( choice3s, BorderLayout.CENTER );
    theC3Progress.addActionListener( new ButtonPressed3() );

    // Panel #4                                // Retrieve

    Panel choice4 = new Panel();
    theC4Submit     = MButton("Retrieve exercise #");
    theC4Assignment = I_TextField(theExercise == null ? "1" : theExercise, 4);
    theC4Program    = O_TextArea ("",15,80);
    choice4.setLayout( new BorderLayout() );
    choice4.add( theC4Program, BorderLayout.SOUTH );
    Panel choice4s    = new Panel();
    choice4s.setLayout( new FlowLayout() );
    choice4s.add( theC4Submit );
    choice4s.add( theC4Assignment );
    choice4.add( choice4s, BorderLayout.NORTH );
    theC4Submit.addActionListener( new ButtonPressed4() );

     //  Panel #7                                // System

    Panel choice7 = new Panel();
    theSystem  = O_TextArea ("",15,80);
    Button theC7About   = MButton("About");
    //Button theC7Status  = MButton("Status");
    Button theC7Clear   = MButton("Clear");
    choice7.setLayout( new BorderLayout() );
    choice7.add( theSystem, BorderLayout.SOUTH );
    Panel choice7s    = new Panel();
    choice7s.setLayout( new FlowLayout() );
    choice7s.add( theC7About );
    //choice7s.add( theC7Status );
    choice7s.add( theC7Clear );
    choice7.add( choice7s, BorderLayout.NORTH );
    theC7About .addActionListener( new ButtonPressed7() );
    //theC7Status.addActionListener( new ButtonPressed8() );
    theC7Clear .addActionListener( new ButtonPressed9() );

                                                // Cards
    cardsStack = new Panel();
    cardsStack.setLayout( new CardLayout() );
    cardsStack.add( B_LOGIN,    choice1 );
    cardsStack.add( B_SUBMIT,   choice2 );
    cardsStack.add( B_PROGRESS, choice3 );
    cardsStack.add( B_RETRIEVE, choice4 );
    cardsStack.add( B_SYSTEM,   choice7 );
                                                // Overall
    theInfo = O_TextArea( "", 10,80 );
    panel.add( choiceBar,  BorderLayout.NORTH );
    panel.add( theInfo,    BorderLayout.CENTER );
    panel.add( cardsStack, BorderLayout.SOUTH );

    addWindowListener( new WindowClosed() );

    add( panel );
    setResizable( false );
    setVisible( true );
  }

  private boolean connectionOk()
  {
    if ( theServerCon == null )
    {
      theServerCon = new ServerConnection();
      if ( !theServerCon.ok() )
      {
         theServerCon = null;
         return false;
      }
      limitOfSentMessages.set(1);
      theServerCon.start();
    }
    return true;
  }

  private void stopWatchDog()
  {
    if ( theWD != null )
    {
      theWD.terminate();
      theWD = null;
    }
  }

  public void dispose()                       // When switched to
  {
    Debug.trace( 2, "Application: dispose");
  }

  public void stop()                          // When switched from
  {
    Debug.trace( 2, "Application: stop");
    stopWatchDog();
/*
    Message info   = new Message();           // New message
    info.setType( MessageType.M_CLOSE );      //
    theWriter.put( info );                    // Send message to server
*/
  }

  public void destroy()                       // When exited
  {
    Debug.trace( 2, "Application: destroy");
    if ( theServerCon != null )
    {
      Message info   = new Message();          // New message
      info.setType( MessageType.M_FINISH );    //
      theServerCon = null;
      theWriter.put( info );                   // Send message to server
    }
    stopWatchDog();
    (new ButtonPressed2d()).actionPerformed( new ActionEvent(new Object(),1,B_DOCK)  );
  }


  // When pull down menu selected

  private class ChoiceAction implements ActionListener
  {
    public synchronized void actionPerformed( ActionEvent evt )
    {
      CardLayout cl       = (CardLayout) cardsStack.getLayout();  // Layout
      String     selected = evt.getActionCommand();               // Selected

      if ( selected.equals( B_SUBMIT ) &&                         // Un-dock so no select
           progFrame != null  ) return;

      cl.show( cardsStack, selected );                           // Show
      if ( selected.equals( B_LOGIN ) )
      {
        if ( theServerCon == null )
        {
          theInfo.setText( "" );
          theInfo.append( "[C] Please login now" + "\n" );
          theInfo.append( "[C]  Type in your user name" + "\n" );
          theInfo.append( "[C]  Type in your password" + "\n" );
          theInfo.append( "[C] Then press button 'Login'" + "\n" );
	} else {
          theInfo.setText( "" );
          theInfo.append( "[C] Login page" + "\n" );
	}
      }
      if ( selected.equals( B_SUBMIT ) )
      {
        theInfo.setText( "" );
        theInfo.append( "[C] Paste program code into the window below" + "\n" );
	theInfo.append( "\n" );
        theInfo.append( "[C] Type in exercise number (input box following Submit button)" + "\n" );
        theInfo.append( "[C] Press button 'Submit exercise #'" + "\n" );
        theInfo.append( "\n" );
        theInfo.append( "[C] If you wish to replace a working solution" + "\n" );
        theInfo.append( "[C]    change 'Submit to charon' to " +
                                "'Replace on charon'" + "\n" );
      }
      if ( selected.equals( B_PROGRESS ) )
      {
        theInfo.setText( "" );
        theInfo.append( "[C] Press button 'Show progress so far' for details" + "\n" );
      }
      if ( selected.equals( B_RETRIEVE ) )
      {
        theInfo.setText( "" );
        theInfo.append( "[C] Select which exercise to retrieve" +
			 " (Type in exercise number)" + "\n" );
        theInfo.append( "[C] Press button 'Retrieve exercise #'" + "\n" );
      }
      if ( selected.equals( B_SYSTEM ) )
      {
        theInfo.setText( "" );
        theInfo.append( "[C] Shows state of System" + "\n" );
      }
    }
  }

  //- class ButtonPressed0 implements ActionListener
  //- {
  //-   public synchronized void actionPerformed( ActionEvent evt )
  //-   {
  //-     String contents = theInfo.getText();
  //-     if ( theInfoD == null )
  //-     {
  //-       makeResultsFrame( contents );
  //-     } else {
  //-       theInfoD.setVisible(false);
  //-       resultsFrame.setVisible(false);
  //-       resultsFrame = null;
  //-       theInfoD     = null;
  //-     }
  //-   }
  //- }


  public void processLogin()
  {
    Message data   = new Message();
    String  passwd = theC1Passwd.getText().trim();
    String  user   = theC1User.getText().trim();
    String  course = theC1Course.getText().trim().toLowerCase();
    theCourse = course;         // Seen by all as theCourse

    if ( user.equals( "" ) )
    {
      theInfo.append( "[C] You need to specify a user name"  + "\n");
      return;
    }

    if ( connectionOk() )
    {
      if ( ! limitOfSentMessages.dec() ) return;
      try
      {
          data.setType(MessageType.M_LOGIN);
          data.put( NAME.USER,     user );
          data.put( NAME.PASSWORD, UtString.urlEncode(passwd) );
          data.put( NAME.COURSE,   theCourse );
          theC1Passwd.setText( "" );
          theInfo.append( "[C] Logging [" + user + "] " +
                           "onto Charon server" + "\n" );
          if ( theWriter != null ) theWriter.put( data );
      }
      catch ( Exception err )
      {
        theInfo.append( "[C] Communications failure : " +
                         err.getMessage() + "\n" );
        theServerCon = null;
      }
    }
  }

  private class ButtonPressed1 implements ActionListener
  {
    public synchronized void actionPerformed( ActionEvent evt )
    {
      processLogin();
    }
  }

  private class CharTyped implements KeyListener
  {
    public void keyPressed( KeyEvent ke )
    {
      if ( ke.getKeyCode() == 10 )
        processLogin();
    }

    public void keyReleased( KeyEvent ke )
    {
    }
    public void keyTyped( KeyEvent ke )
    {
    }
  }

  private class ButtonPressed2 implements ActionListener
  {
    public synchronized void actionPerformed( ActionEvent evt )
    {
      if ( theServerCon == null )
      {
        theInfo.append( "[C] You need to logon to the server" + "\n" );
        return;
      }
      if ( theC2Program.getText().equals( "" ) )
      {
        theInfo.append( "[C] You need to paste a program into the window below"  + "\n");
        return;
      }
      if  ( ! UtString.stringOk( theC2Program.getText(), 3000, 100000 ) )
      {
        theInfo.append( "[C] Your program is far too long"  + "\n");
        return;
      }

      if ( ! limitOfSentMessages.dec() ) return;

      try
      {
        Message data = new Message();
        data.setType(MessageType.M_PROGRAM);
        data.put( NAME.PROGRAM,     theC2Program.getText()+"\n" );
        data.put( NAME.ASSIGNMENT,  theC2Assignment.getText().trim() );
        //data.put( NAME.REPLACE,     theC2Replace.getText().trim() );
        data.put( NAME.REPLACE,     ( (String) theC2Replace.getValue()).trim() );
        data.put( NAME.COURSE,      theCourse );
        theInfo.setText( "" );
        theInfo.append( "[C] Please wait for processing" + "\n" );
        theWriter.put( data );
      }
      catch ( Exception err )
      {
        theInfo.append( "[C] Communications failure : " +
                         err.getMessage() + "\n" );
        theServerCon = null;
      }
    }
  }

  class ButtonPressed2d implements ActionListener
  {
    public synchronized void actionPerformed( ActionEvent ae )
    {
      String actionIs = ae.getActionCommand();    // Button
      // Undock Windows
      if ( actionIs.equals( B_UNDOCK ) )
      {
        String contents = theInfo.getText();
        if ( theInfoD == null )
        {
          makeResultsFrame( contents );
        }

        String prog = theC2Program.getText();
	String exer = theC2Assignment.getText();
        makeProgramPanel( prog, exer, true );
        CardLayout cl = (CardLayout) cardsStack.getLayout();  // Layout
        cl.show( cardsStack, B_LOGIN );                       // Show

        contents = theInfo.getText();
        makeResultsFrame( contents );
      }

      // Dock windows

      if ( progFrame != null && actionIs.equals( B_DOCK ) )
      {
        String prog = theC2Program.getText();
	String exer = theC2Assignment.getText();
        makeProgramPanel( prog, exer, false );
        //theC2Program.setText( prog );
        CardLayout cl = (CardLayout) cardsStack.getLayout();  // Layout
        cl.show( cardsStack, B_SUBMIT );                      // Show
        theC2Program.requestFocus();

        if ( theInfoD != null )
        {
  	  theInfoD.setVisible(false);
  	  resultsFrame.setVisible(false);
  	  resultsFrame = null;
  	  theInfoD = null;
        }

      }
    }
  }

  private class ButtonPressed3 implements ActionListener
  {
    public synchronized void actionPerformed( ActionEvent evt )
    {
      if ( theServerCon == null )
      {
        theInfo.append( "[C] You need to logon to the server" + "\n" );
        return;
      }
      if ( ! limitOfSentMessages.dec() ) return;
      try
      {
        Message data = new Message();
        data.setType(MessageType.M_PROCESS);
        data.put( NAME.COURSE,      theCourse );
        theWriter.put( data );
      }
      catch ( Exception err )
      {
        theInfo.append( "[C] Communications failure : " +
                         err.getMessage() + "\n" );
        theServerCon = null;
      }
    }
  }


  private class ButtonPressed4 implements ActionListener
  {
    public synchronized void actionPerformed( ActionEvent evt )
    {
      if ( theServerCon == null )
      {
        theInfo.append( "[C] You need to logon to the server" + "\n" );
        return;
      }
      if ( ! limitOfSentMessages.dec() ) return;
      try
      {
        Message data = new Message();
        data.setType(MessageType.M_EXTRACT);
        Debug.trace( 2, "Retrieve : Exercise %s",
			theC4Assignment.getText().trim() );
        data.put( NAME.COURSE, theCourse );
        data.put( NAME.ASSIGNMENT, theC4Assignment.getText().trim() );
        theWriter.put( data );
      }
      catch ( Exception err )
      {
        theInfo.append( "[C] Communications failure : " +
                         err.getMessage() + "\n" );
        theServerCon = null;
      }
    }
  }


  private class ButtonPressed5 implements ActionListener
  {
    public synchronized void actionPerformed( ActionEvent evt )
    {
      if ( theServerCon == null )
      {
        theInfo.append( "[C] But you are logged out" + "\n" );
        return;
      }
      try
      {
        Message info  = new Message();            // New message
        info.setType( MessageType.M_CLOSE );      //
        limitOfSentMessages.set(1);
        theServerCon = null;
        theWriter.put( info );                    // Send message to server
        Thread.sleep(1000);
        System.exit(0);
      }
      catch ( Exception err )
      {
        theInfo.append( "[C] Communications failure : " +
                         err.getMessage() + "\n" );
        theServerCon = null;
      }
    }
  }


  private class ButtonPressed6 implements ActionListener
  {
    public synchronized void actionPerformed( ActionEvent evt )
    {
      connectionOk();
    }
  }


  // About
  private class ButtonPressed7 implements ActionListener
  {
    public synchronized void actionPerformed( ActionEvent evt )
    {
      if ( connectionOk() )
      {
       try
       {
         Message info   = new Message();           // New message
         info.setType( MessageType.M_INFO_1 );     //
         info.put( "Client", About.client() );
         theWriter.put( info );
         theSystem.setText("");
         theSystem.append("\n");
         limitOfSentMessages.set(1);
       }
       catch ( Exception err )
       {
         theInfo.append( "[C] Communications failure : " +
                          err.getMessage() + "\n" );
         theServerCon = null;
       }
      }
    }
  }

  // Status
  private class ButtonPressed8 implements ActionListener
  {
    public synchronized void actionPerformed( ActionEvent evt )
    {
      if ( connectionOk() )
      {
        try
        {
          Message info   = new Message();           // New message
          info.setType( MessageType.M_MODULE );     //
          info.put( "module", "Information" );
          theWriter.put( info );
          theSystem.setText("");
          limitOfSentMessages.set(1);
        }
        catch ( Exception err )
        {
          theInfo.append( "[C] Communications failure : " +
                           err.getMessage() + "\n" );
          theServerCon = null;
        }
      }
    }
  }

  // Clear
  private class ButtonPressed9 implements ActionListener
  {
    public synchronized void actionPerformed( ActionEvent evt )
    {
      theSystem.setText("");
    }
  }



  public void setInitialValues()
  {
    theExercise = "";
   
    Debug.trace( 1, "Application: MC=%s Port=%d Course=%s", theMc, thePort, theCourse );
  }

  public String setUpConnection()
  {
    try
    {
      theWriter = null;   theReader = null;

      SSLSocketFactory sf;
      SSLSocket socket;
      sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
      socket =    (SSLSocket) sf.createSocket( theMc, thePort );

      // Note no certificate check

      socket.setEnabledCipherSuites( MessageType.ENCRYPTION );

      //  Socket socket;
      //  socket = new Socket( theMc, thePort );        // Socket host.port

      theWriter = new NetWriter( socket );
      theReader = new NetReader( socket );

      try
      {
        Debug.trace( 1, "Protocol: %s, Cipher suite: %s",
                        socket.getSession().getProtocol(),
                        socket.getSession().getCipherSuite() );
      } catch ( Exception err )
      {
        Debug.trace( 1, "Can not get protocol/Cipher suite used"  );
      }
      Debug.trace( 2, "Application: setUpConnection ok" );
      return null;                                    // All ok
    }
    catch ( Exception err )
    {
      return "[C] Problem with network connection to the Charon system \n    [" +
             err.getMessage() + "]";
    }
  }

//  class Transaction implements TextListener
//  {
//    public void textValueChanged( TextEvent e )
//    {
//      String userInput = theProgram.getText();      // Users response
//      Debug.trace( 2, "userInput : " + userInput );
//    }
//  }
//
  class ServerConnection extends Thread
  {
    private boolean allOk = true;

    public ServerConnection()
    {
      String res = setUpConnection();                // Set up connection
      if ( res != null )
      {
        allOk = false;
        theInfo.append( res + "\n" );                // Failure
        return;
      }
      theInfo.append("[C] Connected to Charon server" +"\n");
    }

    public boolean ok()
    {
      return allOk;
    }

    public void run()
    {
      Debug.trace( 2, "Application: ServerConnection Run" );

      theInfo.setText( "" );
      theInfo.append( "[C] Client started V" + VERSION.is +
                      " : ** You need to login **" + "\n" );

      if ( !allOk ) return;

      Message info   = new Message();           // New message
      info.setType( MessageType.M_INFO );       // Ask for info from server
      theWD.reset();				// Reset Watch dog timer
      theWriter.put( info );                    // Send message to server

      loop: while ( true )
      {
        String error = "Error";
        Message display = theReader.get();

        if ( ! display.messageOK() )
        {
          theInfo.append(
           "[C]  FATAL: Communication corruption from the Charon server\n" +
           "[C]    You are probably using an old out of date Charon client\n" +
           "[C]    Use the latest Charon client\n"
          );
          continue;
        }

        final int type  = display.getType();
        Debug.trace( 2, "Message from server : %s", display.getTypeAsString() );

        if ( theWD != null ) theWD.reset();

        final long interval= display.responseTime();
        final long seconds = interval/1000;
        final long rest    = interval%1000;

        theInfo.append( "[C] Elapsed time to process request " +
                        seconds + "." + UtString.asMills( (int) rest ) +
                      " seconds" + "\n" );

        sw: switch( type )
        {
          case MessageType.M_LINK_FAILURE:
            theInfo.append( "[C] Connection terminated" +"\n" );
            break loop;
          case MessageType.M_CLOSE:
            theInfo.append( "[C] Connection closed"  + "\n");
	    if ( (display.get( "result" )).length() > 1 )
	      theInfo.append( display.get( "result" ) + "\n");
            break loop;
          case MessageType.M_UNKNOWN:
            theInfo.append( "[C] Unknown message"  + "\n");
            break loop;

          case MessageType.M_INFO:
            theInfo.setText( About.client() );
            theInfo.append( display.get("result") + "\n" );
//          Debug.trace( 3, "M_INFO : %s", display.get( "result") );
            break sw;

          case MessageType.M_SYSTEM:
            theSystem.append( display.get("result") + "\n" );
            Debug.trace( 3, "M_INFO_1 : %s", display.get( "result") );
            break sw;

          case MessageType.M_RESULT:
            theInfo.append( display.get("result") + "\n" );
            limitOfSentMessages.inc();
            break sw;

          case MessageType.M_PROGRESS:
            theProgress.setText( display.get("result") + "\n" );
            limitOfSentMessages.inc();
            break sw;

          case MessageType.M_EXTRACT:
            //theC4Program.setText( "" );
            error = display.get( "error" );
            if ( error.equals( "" ) )
              theC4Program.setText( display.get("result") );
            else
              theInfo.append( error + "\n" );
            limitOfSentMessages.inc();
            break sw;
/*
          case MessageType.M_MODULE:
            String tSystem = display.get("windowSystem");       // System window
            if ( ! tSystem.equals("") )
            {
              theSystem.append( tSystem.substring(1) + "\n" );
            }
            String tInfo = display.get("windowInfo");           // Information window
            if ( ! tInfo.equals("") )
            {
              theInfo.append( tInfo.substring(1) + "\n" );
            }
//          String tProgram = display.get("windowProgram");     // Program window
//          if ( ! tProgram.equals("") )
//          {
//            theInfo.append( tProgram.substring(1) + "\n" );
//          }
            String tProgress = display.get("windowProgress");   // Progress window
            if ( ! tProgress.equals("") )
            {
              theProgress.append( tProgress.substring(1) + "\n" );
            }
            break sw;
*/
          default :
            Debug.trace( 2, "Unknow message : [%s]", display.getTypeAsString() );

        }
      }
      theServerCon = null;
    }
  }
}
