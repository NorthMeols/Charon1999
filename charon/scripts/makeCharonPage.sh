# run in /var/wwww
echo "------------------------------------------"
echo "Make  pages to access charon  "
echo "------------------------------------------"
cat <<+END+ > _prototype.html
<!doctype html public "-//IETF//DTD HTML//EN">
<HTML>
  <HEAD>
  <TITLE>Course material: University of Brighton</TITLE>
  <LINK TITLE="Course material: University of Brighton"
        HREF="Mike Smith University of Brighton" >
</HEAD>

<!-- -------------------------------------------------- -->

<BODY TEXT="#101060" BGCOLOR=white LINK="DarkBlue" VLINK="DarkBlue" ALINK=olive>

<IMG SRC="../charon/charon.png" ALT="Charon">

<P>
The Charon system is used to automatically record and judge your
programming coursework. 
<UL>
  <LI>
  <FONT COLOR="blue">
   All transactions to this system are recorded for later analysis.
  </FONT>
  <LI><B>Details about the Charon system are
      <A HREF="../charon/charon.htm">here</A> and specifically include
      details about what to look for if your program does not work</B>.
  <LI>Details of how to use an appletviewer to connect to Charon are
      <A HREF="alternative.htm">here</A>.
  <LI><FONT COLOR=RED><B>NEW:</B></FONT>
       You will now need to click <B>run</B> to the security warning:
       <BR>
       "The applications digital signature cannot be verified. 
       Do you want to run the application".
       <BR>See FAQ
  <LI>Use the UnDock button to be able to re-size the results and submission window.
</UL>
<P>

<FONT SIZE=+3 COLOR="red"><B>MESSAGE1</B></FONT>
<BR>
<APPLET CODEBASE="http://charon.it.brighton.ac.uk/charon" 
        ARCHIVE="jc_lib.jar"
        CODE=jc_web_client.class
        WIDTH=720 HEIGHT=500>
  <PARAM NAME=course VALUE=COURSENAME>
  <PARAM NAME=port   VALUE=50000>
</APPLET>
<BR>

<HR>
<FONT COLOR="#808080">
&#169; M.A.Smith University of Brighton.
Created August 1999 last modified June 2013.
<BR>
 <I>
  Comments, suggestions, etc.
  
  M.A.Smith at brighton dot ac dot uk
  </A>
 </I>
 *
 <A HREF="http://puck.it.brighton.ac.uk/~mas" TARGET="_top">[Home page]</A>
</FONT>

</BODY>
</HTML>
+END+
#
function makePage
{
  export COURSE=$1
  export MESSAGE1=$2
  echo "Making /var/www/$COURSE" - $MESSAGE1
  sed -e "s/COURSENAME/$COURSE/" < _prototype.html > _1
  sed -e "s/MESSAGE1/$MESSAGE1/" < _1 > $COURSE.html
  mv $COURSE.html /var/www/
  rm _1
}

makePage ci101.2014 "CI101 2014-5" ;
makePage ci228.2014 "CI228 2014-5" ;
makePage ci229.2014 "CI283 2014-5" ;

rm _prototype.html
