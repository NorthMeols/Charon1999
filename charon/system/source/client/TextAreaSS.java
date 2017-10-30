// Charon system Mike Smith 1999-2017
package client;

import java.awt.*;

public class TextAreaSS extends TextArea
{
  public TextAreaSS( String str, int rows, int cols, int scrollBars )
  {
    super( str, rows, cols, scrollBars );
  }

  private TextArea theInfoD = null;

  public void set( TextArea ta )
  {
    theInfoD = ta;
  }

  public void append( String str )
  {
    super.append( str );
    if ( theInfoD != null ) theInfoD.append( str );
  }

  public void setText( String str )
  {
    super.setText( str );
    if ( theInfoD != null ) theInfoD.setText( str );
  }
}
