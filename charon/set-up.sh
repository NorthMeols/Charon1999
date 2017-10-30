#!/bin/bash

# --------------------------------------------------------
# Install the Charon system
# --------------------------------------------------------

if [ ! "$BASH_VERSION" ]
then
    echo Shell not bash
    exec /bin/bash "$0" "$@"
fi

CHARON_IS="charon:users"
CHARON_IS="charon:charon"
ROOT_IS="root:root"
JAVA_VERSION="current"
OS=P32
OS=LP64

# --------------------------------------------------------
# Do not change beyond here
# --------------------------------------------------------

if [ "$OS" = "LP64" ]
then
  export ARCH="64"
else
  export ARCH="32"
fi

# --------------------------------------------------------
# Check sh is linked to bash
# --------------------------------------------------------

# SH=`ls -l /bin/sh | awk '{ print $10}' `
# if [  "bash" != $SH ]; then
# # echo "ERROR - /bin/sh linked to" $SH "should be bash" " - $OS
#   if [ $FIRSTTIME"x" = "x" ]; then
#     export FIRSTTIME="NO"
#     bash set_up.sh
#     exit
#   fi
# fi

# --------------------------------------------------------
#              Functions used in shell script
# --------------------------------------------------------
# Set version of important libraries used in playpen
# These tend to change frequently
# --------------------------------------------------------

function findLatestLib()
{
  # echo "Trace findlatestLib $1 $2..$4 $3..$5"
  ((MAJOR=$2))
  ((MINOR=$3))
  ((MAJOR_TO=$4))
  ((MINOR_TO=$5))
  ((FOUND=0))
  if [ "$OS" = "LP64" ]
  then
    LIBROOT=/lib/x86_64-linux-gnu
  else
    LIBROOT=/lib/i386-linux-gnu
  fi
  for ((m=MAJOR; m<=MAJOR_TO; m++))
  {
    for ((s=0; s<=MINOR_TO; s++))
    {
       # echo Trace: $LIBROOT/$1.so.$m.$s
       if [ -e $LIBROOT/$1.so.$m.$s ]
       then
         export $1MM=$1.so.$m.$s
         export $1=$1.so.$m
         # TRACE
         echo Found $LIBROOT/$1.so.$m.$s
         ((FOUND=1))
         break
       fi
    }
    if [ $FOUND == 1 ]
    then
      break;
    fi
  }
  if [ $FOUND == 0 ]
  then
   echo LIBROOT $LIBROOT/$1 " DOES NOT EXIST"
   exit
  fi
}

function updateFile
{
  if [ -f $1 ]; then
    cp $1 $2
  fi
}

function Link
{
  ((FAIL=0))
  if [ -d $1 ]
  then
    cd $1
    ln -s $2 $3
    cd ..
  else
    ((FAIL++))
  fi
  if [ $FAIL -gt 0 ]
  then
    echo "ERROR Link Fail Dir $1  $2 -> $3"
  fi
}

function MakeDir
{
  for DIR in $*
  do
    if [ ! -d $DIR ]
    then
      mkdir $DIR
    fi
  done
}

function CopyDir
{
  if  test Z$5 ==  Z
  then 
    echo "Copying - cp -R " $*
  else
    echo "Copying - cp -R $1 $2 $3 $4 ... "
  fi
  cp -R $*
}

function cpL32
{
  echo "Add (32) lib $1 "
  cp $2 $3
}

function updateLib32
{
  if [ -f /lib/$1 ]; then
    cpL32 $1 /lib/$1 lib/$1
  elif [ -f /usr/lib/$1 ]; then
    cpL32 $1 /usr/lib/$1 lib/$1
  elif [ -f /lib/i386-linux-gnu/$1 ]; then
    cpL32 $1 /lib/i386-linux-gnu/$1 lib/$1
  elif [ -f /usr/lib/i386-linux-gnu/$1 ]; then
    cpL32 $1 /usr/lib/i386-linux-gnu/$1 lib/$1
  elif [ -f /lib/x86_64-linux-gnu/$1 ]; then
    cpL32 $1 /lib/x86_64-linux-gnu/$1 lib/$1
  else
    echo "ERROR - library " 32 $1 " does not exist"
  fi
}

function cpL64
{
  echo "Add (64) lib $1 "
  cp $2 $3
}

function updateLib64
{
  if [ -f /lib/x86_64-linux-gnu/$1 ]
  then
    cpL64 $1 /lib/x86_64-linux-gnu/$1 lib/x86_64-linux-gnu/
  elif [ -f /usr/lib/x86_64-linux-gnu/$1 ]
  then
    cpL64 $1 /usr/lib/x86_64-linux-gnu/$1 lib/x86_64-linux-gnu/
  else
    echo "ERROR - library 64 " $1 " does not exist"
  fi
}

function MakeLink
{
  if [ ! -f $1 ]
  then
   echo "WARNING MakeLink Source $1 does not exist"
  elif [ -f $2 ]
  then
   echo ln $1 $2
   echo "WARNING MakeLink Target $2 exists"
  else  
    ln $1 $2
  fi
}

function updateLibLink
{
  # echo updateLibLink $1 $2
  updateLib$ARCH $1
  if [ -f lib/$1 ]; then
    if [ -f $2 ]; then
     echo " Library $2 no need for link"
    else
      cd lib
      ln -s $1 $2
      cd ..
    fi
  fi
}

function add
{
  echo -n "Sandbox programs: "
  for i in $*
  do
    ((UPDATE=0))
    if test -e /bin/$i 
    then
      cp /bin/$i bin
      ((UPDATE=1))
    elif test -e /usr/bin/$i
    then
      cp /usr/bin/$i bin
      ((UPDATE=1))
    fi
    if [ $UPDATE == 1 ]
    then
      echo -n  " "$i
      chown root:root  bin/$i
      chmod 755        bin/$i
    fi
  done
  echo ""
}

function Message
{
  echo "-----------------------------------------------------------------------------------"
  printf "| %-80s|\n" "$@"
  echo "-----------------------------------------------------------------------------------"
}

# --------------------------------------------------------
# Find root web-server and where Java is installed
# --------------------------------------------------------

WEBROOT=""
if [ -d /var/www/ ]; then
  WEBROOT="/var/www/"
fi
if [ -d /var/www/html ]; then
  WEBROOT="/var/www/html"
fi
if [ -d /srv/www/htdocs ]; then
  WEBROOT="/srv/www/htdocs"
fi

JAVA_JVM=""
JAVA_FLA=""
if [ -f /opt/java/$JAVA_VERSION/bin/java ]; then
  JAVA_JVM=/opt/java/$JAVA_VERSION
  JAVA_FLA="Oracle"
fi

if [ ! -n $WEBROOT""  ]; then
  echo "ERROR - Do not know where webroot is"
  echo "Have you run pre-set-up.sh"
  exit
fi

if [ ! -n $JAVA_JVM""  ]; then
  echo "ERROR - Do not know where Java root is"
  echo "ERROR - Java not installed"
  exit
fi

Message "Has pre-setup.sh been run?"  "Check if g++, apache2 have been installed" 

if [ ! -e /usr/bin/g++  ]; then
  echo "ERROR - Need to install g++" 
  echo "Have you run pre-set-up.sh
  exit
fi

if [ ! -e /usr/sbin/apache2  ]; then
  echo "ERROR - Need to install apache2 web server" 
  echo "Have you run pre-set-up.sh
  exit
fi

Message "Looks ok, however very simple check" 

# --------------------------------------------------------
# Print basic information about the system
# --------------------------------------------------------

IP=`ifconfig | grep "inet " |  grep -v '127.0.0.1' | awk '{ print $2 }' `

HOST=`hostname`

WHEN=`date "+%d %b %Y %H:%M"`

Message "All about the system"

function About()
{
 WHEN=`date "+%d %b %Y %H:%M"`

 echo "Time is        " $WHEN
 echo "Default sh is  " $SH
 echo "Web root is at " $WEBROOT
 echo "root           " $ROOT_IS
 echo "charon         " $CHARON_IS
 echo "Java root      " $JAVA_JVM - $JAVA_FLA
 echo "Hostname       " $HOST
 echo "IP             " $IP
 echo "OS             " $OS
}

About;

# --------------------------------------------------------
# Needed by web server
# --------------------------------------------------------

if [ ! -d /home/charon ]; then
  if [ -f /usr/sbin/useradd ]; then
    echo "User charon created"
    useradd -m charon
    # usermod -s charon /bin/false
    usermod -L charon
  else
    echo "ERROR - Need to add user charon to system"
    exit
  fi
fi

# --------------------------------------------------------
# For testing
# echo "charon:one1nine9five5two2" | chpasswd
# --------------------------------------------------------
tr -dc A-Za-z0-9 < /dev/urandom | head -c50 | sed s/^/charon:/ | chpasswd


# --------------------------------------------------------
# Check if some important directories exist
# --------------------------------------------------------

#if [ ! -f /usr/bin/jikes ]; then
#  echo "ERROR - /usr/bin/jikes must exist"
#  exit
#fi

if [ ! -d /opt/charon ]; then
  echo "ERROR - /opt/charon must exist"
  exit
fi

if [ ! -d /opt/charon/system ]; then
  echo "ERROR - /opt/charon/system must exist"
  exit
fi

if [ ! -d /opt/charon/system/source ]; then
  echo "ERROR - /opt/charon/system/source must exist"
  exit
fi

if [ ! -d /opt/charon/bin ]; then
  mkdir /opt/charon/bin
fi

if [ ! -d /opt/charon/results ]; then
  mkdir /opt/charon/results
  chmod 700 /opt/charon/results
fi

if [ ! -d $WEBROOT/charon ]; then
  mkdir $WEBROOT/charon
  chmod 755 $WEBROOT/charon
fi

# --------------------------------------------------------
# Remove proc file system if already exists
# ---------------------------------------------------------

Message "Clean up previous environment"

cd /opt/charon

if [ -e playpen/proc/version ]
then
   # All ready mounted so umount
   echo "unmount playpen/proc"
   umount playpen/proc
fi

cd playpen

if [ -e proc ] 
then
     rm -f -r proc
fi

cd /opt/charon

if [ ! -f system/source/Makefile ]
then
  echo "ERROR - Missing file"
  echo "ERROR - /opt/charon/system/source/Makefile must exist"
  exit
fi

# --------------------------------------------------------
# Start installation
# ---------------------------------------------------------


MakeDir students playpen

cd playpen
rm -f -r bin lib lib64 etc usr dev java sandbox
MakeDir sandbox
cd ..

# --------------------------------------------------------
# Build system
# ---------------------------------------------------------


cd system/source
make clean

Message "Build the Charon system"
make install

Message  "Start installation" "Find latest version of libraries"

findLatestLib libhistory  6 0  10 10
findLatestLib libreadline 6 0  10 10
findLatestLib libncurses  5 0  10 10


Message "Finished build of system" "Now fix ownership of files" "Create /proc dir"

# --------------------------------------------------------
# Change ownership of all files to charon
# ---------------------------------------------------------

cd /opt/charon
chown -R $CHARON_IS * 

# --------------------------------------------------------
# Mount proc file system in playpen
# ---------------------------------------------------------

cd playpen

mkdir proc
chown $ROOT_IS proc
chmod 755 proc

#	mount -n -t proc remount,hidepid=2 proc
#	mount -n -t proc proc
#	mount  -o -n -t proc remount,hidepid=2 proc
#	mount --bind /proc proc

echo "Mount the proc file system"
mount  -n -t proc defaults,hidepid=2 proc

if [ ! -e proc/version ]; then
	echo "proc filesystem not mounted"
        exit
fi

# --------------------------------------------------------
# Do not allow access to information that could be dangerous
# ---------------------------------------------------------

# chmod 000 proc/net/* proc/net
# chmod 000 proc/partitions
# chmod 000 proc/timer*

# --------------------------------------------------------
# Create restricted environment
# ---------------------------------------------------------

cd /opt/charon/playpen

rm -f -r bin lib etc usr dev java lib64
MakeDir bin lib etc usr dev java usr/bin usr/lib lib/i386-linux-gnu lib/x86_64-linux-gnu/
if [ $OS == "LP64" ]
then
  MakeDir lib64
fi

# --------------------------------------------------------
# Set access for important charon directories
# ---------------------------------------------------------
Message "Set permissions for directories"

cd /opt/charon

chmod 750 scripts
chmod 440 scripts/*.sh
chmod 440 *.sh

chmod 440 00READ.ME backup.sh scripts/clean.sh 
chmod 750 jcl
chmod 500 server.sh set-up.sh pre_set-up.sh

chmod 755 /opt /opt/charon
chmod 750 bin
chmod 700 system system/source system/results results

chmod 750 students templates
chmod 755 playpen
chown $ROOT_IS playpen
chmod 751 playpen/sandbox

cd playpen
chmod 755 bin lib etc usr dev java usr/bin usr/lib lib/i386-linux-gnu lib/x86_64-linux-gnu/
if [ -e lib64 ]
then
  chmod 755 lib64
fi

# --------------------------------------------------------
# Standard libraries
# ---------------------------------------------------------

cd /opt/charon/playpen

Message "Sandbox - Create standard libraries"

echo OS = $OS ARCH = $ARCH
if [ "$OS" == "LP64" ]
then
  updateLib$ARCH 	ld-linux-x86-64.so.2
else
  updateLib$ARCH 	libc.so.6 
fi
updateLib$ARCH 	libc.so.6 
updateLib$ARCH 	libpam.so.0
updateLib$ARCH 	libpam_misc.so.0
updateLib$ARCH 	libselinux.so.1
updateLib$ARCH	libtinfo.so.5
#--
updateLib$ARCH 	libacl.so.1
updateLib$ARCH 	libattr.so.1
updateLib$ARCH 	librt.so.1
#--

if [ "$OS" = "LP64" ]
then
  echo lib64/ld-linux-x86-64.so.2
  MakeLink  lib/x86_64-linux-gnu/ld-linux-x86-64.so.2 lib64/ld-linux-x86-64.so.2
fi

if [ "$OS" = "P32" ]
then
  updateLib$ARCH  ld-linux.so.2
  updateLib$ARCH  libpthread.so.0
fi

updateLibLink  $libhistoryMM	$libhistory
updateLibLink  $libreadlineMM 	$libreadline
updateLibLink  $libncursesMM	$libncurses


# --------------------------------------------------------
# Required for c++
# ---------------------------------------------------------

Message "Sandbox - Create standard libraries for C++"

updateLib$ARCH 	libstdc++.so.6
updateLib$ARCH 	libgcc_s.so.1

# --------------------------------------------------------
# Shell commands in playpen
# ---------------------------------------------------------

Message "Sandbox - Populate playpen with shell tools"

add bash basename cat chown chmod cp 
add dirname echo find grep 
add ls mkdir mv pwd rm rmdir sed test awk
add id
add uname head cut expr
add date # Used for testing so comment out

Link bin bash sh

# ---------------------------------------------------------
# Required for Ada
# ---------------------------------------------------------

if [  -e /usr/lib/libgnat-3.13p.so.1 ]; then
	cp /usr/lib/libgnat-3.13p.so.1		lib
fi


#
# ---------------------------------------------------------
# Required for Java
# ---------------------------------------------------------

Message "Sandbox - Create standard environment for Java"


updateFile /usr/X11R6/lib/libX11.so.6		lib
#
mknod dev/null c 1 3
chmod 766 dev/null
#
updateLib$ARCH	libpthread.so.0
updateLib$ARCH	libdl.so.2
updateLib$ARCH	libm.so.6
updateLib$ARCH  libz.so.1    ## NEW
updateLib64	libpcre.so.3
# updateLib	libtermcap.so.2

# ---------------------------------------------------------
# Copy Java files
# ---------------------------------------------------------

function cpThisFile
{
  if [ -e $1 ]; then
    if [ ! -d $2 ]; then 
      rm -f $2
    fi
    cp $1 $2
  else
    echo "ERROR - File " $1 " does not exist"
  fi
}


Message "Copy java jdk to sandbox"

if [ $JAVA_FLA == "Oracle" ]
then
  echo "Java Oracle"
  CopyDir $JAVA_JVM/* java
else
  echo "ERROR Do not know how to setup Java system"
fi

# ---------------------------------------------------------
# Required for Java since 1.4.2
# ---------------------------------------------------------

if [ ! -d etc ]; then
  : mkdir etc
fi

if [ ! -d proc ]; then
  mkdir proc
  chmod 755 proc
fi

# ---------------------------------------------------------
# ---------- end install for java  ----------------------
# ---------------------------------------------------------

cd ..

# ---------------------------------------------------------
# Set up jar file and first class in web directory
# ---------------------------------------------------------

Message "Set up environment for web server"

# cpThisFile /opt/charon/bin/jc_lib.jar 		$WEBROOT/charon 

# chown $ROOT_IS  /opt/charon/bin/runas
# chmod 6755      /opt/charon/bin/runas
chown $ROOT_IS  /opt/charon/bin/jcl_safe 
chmod 6755      /opt/charon/bin/jcl_safe 

# cpThisFile /opt/charon/bin/jcl_safe		$WEBROOT/charon 
# cpThisFile /opt/charon/bin/runas		$WEBROOT/charon 
# chown $ROOT_IS  $WEBROOT/charon/jcl_safe $WEBROOT/charon/runas
# chmod 6755      $WEBROOT/charon/jcl_safe $WEBROOT/charon/runas

# ---------------------------------------------------------
# Charon front page
# ---------------------------------------------------------

cat <<+END+ > $WEBROOT/index.html
<HTML>
<HEAD>
   <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
   <TITLE>Charon</TITLE>
</HEAD>
<BODY TEXT="#101060" BGCOLOR=white LINK="blue" VLINK="blue" ALINK=lightblue>
<P><A NAME="TITLE"></A>

<H2>Delivered by the web-server on $HOST ($IP)</H2>

<TABLE BGCOLOR="blue" >
 <TR>
  <TD><B><FONT SIZE="+2" COLOR="white">
    Charon</FONT></B>
 </TR>
</TABLE>

<FONT SIZE=+2>
<OL>
  <LI>
    The aged ferryman (the son of Erebus and Nyx) who (in Greek mythology) 
    conveys the souls of the newly dead across the river Acheron to the gates 
    of the underworld (Hades). 
    He would only allow on his boat those who had received the rites of burial 
    and whose journey had been pre-paid with a coin left under their tongue. 
    Those who could not pay had to wander the banks of Acheron for 100 years.
  <BR>
  <BR>
  <LI>
    The largest moon of the dwarf planet Pluto. 
    The other known moons of Pluto are Nix, Hydra, Kerberos and Styx.
    Charon has a mean diameter of 1213 miles and orbits Pluto at a 
    mean distance of 12,160 miles in approximately 6.4 days. 
    Charon was discovered in 1978 by James Christy 
    and is named after [1] 
    but also because the first syllable is the nickname of his wife, Charlene.
    <P>
    To an observer (in the appropriate hemisphere) on the surface of Pluto,
    Charon would remain fixed in the sky.
    <P>
    On the 24th July 2015 NASA's interplanetary space probe New Horizons flew
    past Pluto at 36,373 mph.
  <P>
</OL>
</FONT>

</BODY>
</HTML>
+END+

if [ ! -d $WEBROOT/charon ]
then
  mkdir $WEBROOT/charon
fi
cp $WEBROOT/index.html $WEBROOT/charon/index.html

# ---------------------------------------------------------
# Set up web pages to test charon
# ---------------------------------------------------------

MODULE=test
PORT=50000

cat <<+END+ > $WEBROOT/charon.html
<HTML>
<HEAD>
   <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
   <TITLE>Charon test</TITLE>
</HEAD>
<BODY TEXT="#101060" BGCOLOR=white LINK="blue" VLINK="blue" ALINK=lightblue>
<P><A NAME="TITLE"></A>

<TABLE BGCOLOR="darkblue" >
 <TR>
  <TD><B><FONT SIZE="+2" COLOR="white">
    Created $WHEN Charon test on $HOST ip=$IP</FONT></B>
 </TR>
</TABLE>


<H2>Introduction</H2>
Submit your answers to the following exercises to the Charon applet at:
<BR>
<A HREF="charonappletlocal.html">http://localhost/charonapplet.html</A>.
or 
<A HREF="charonapplet.html">http://$IP/charonapplet.html</A>.
<P>

<UL>
  <LI>Exercise 1
  <BR>Write a program in Ada to print Hello world onto the screen.
  <P>
  <FONT COLOR="BLUE">Hint:</FONT><P>
  <PRE>
    with Ada.Text_Io; use Ada.Text_Io;
    procedure main is
    begin
      put("In Ada95 Hello world"); new_line;
    end main;
  </PRE>
 <P>
  <LI>Exercise 2
  <BR>Write a program in C++ to print Hello world onto the screen.
  <P>
  <FONT COLOR="BLUE">Hint:</FONT><P>
  <PRE>
    #include &lt;iostream&gt;

    int main()
    {
      std::cout &lt;&lt; "In C++ Hello world" &lt;&lt; "\n";
    }
  </PRE>
 <P>
  <LI>Exercise 3
  <BR>Write a program in Java to print Hello world onto the screen.
  <P>
  <FONT COLOR="BLUE">Hint:</FONT><P>
  <PRE>
    class Main
    {
      public static void main( String args[] )
      {
        System.out.println("In Java Hello world");
      }
    }
  </PRE>
</UL>

<P><TABLE WIDTH="100%" ALIGN=CENTER><TR> <TD BGCOLOR="darkblue"><FONT SIZE=-3>&nbsp;</FONT> </TD> </TABLE>
<!------------------------------------------------------------------------------->

<HR>
</BODY>
</HTML>

+END+



cat <<+END+ > $WEBROOT/charonappletlocal.html
<!doctype html public "-//IETF//DTD HTML//EN">
<HTML>
 <HEAD>
  <TITLE>Charon applet</TITLE>
 </HEAD>

<!-- -------------------------------------------------- -->

<BODY TEXT="#101060" BGCOLOR=white LINK=blue VLINK=blue ALINK=lightblue>

 <B>
  <FONT COLOR="blue">
   You can access the Charon system (on localhost) by using either:
  </B>
  <TABLE CELLPADDING=2>
   <TR>
    <TD>A WEB browser</TD>
    <TD>Browse to the URL:<BR>
        <TT>http://localhost/charon.html</TT>
    </TD>
   </TR>
   <TR>
    <TD>An appletviewer</TD>
    <TD>At a command prompt type:<BR>
        <TT>appletviewer http://localhost/charon.html</TT>
   </TD>
   </TR>
  </TABLE>
  </FONT>
 <P>
 <TT><B>$HOST's IP address is $IP, Module=$MODULE, Port=$PORT</B></TT>
 <P>
 <APPLET CODEBASE="http://localhost/charon" 
         ARCHIVE="jc_lib.jar"
         CODE="jc_web_client.class"
         WIDTH="700"
         HEIGHT="500">

  <PARAM NAME="course" VALUE=$MODULE>
  <PARAM NAME="port"   VALUE=$PORT>

 </APPLET>

</BODY>

</HTML>
+END+
sed -e  s/localhost/$IP/ < $WEBROOT/charonappletlocal.html > $WEBROOT/charonapplet.html

# ----------------------------------------------------------------------------------

for COURSE in ci101_2013 java ci101_2013_resit ci228_2013 ci228_resit_2013 ci101.2014 ci228.2014
do
  sed -e "s/=test/=$COURSE/" \
      -e "s/charon.html/$COURSE.html/" < $WEBROOT/charonapplet.html > $WEBROOT/$COURSE.html
done

# --------------------------------------------------------
# The java applications to access the system make visible
# --------------------------------------------------------

MakeDir $WEBROOT/charon

cp /opt/charon/bin/Charon.jar			$WEBROOT/charon/
cp /opt/charon/backup/Charon2Launcher.jar	$WEBROOT/charon/

chmod 755 $WEBROOT/charon/Charon.jar
chmod 755 $WEBROOT/charon/Charon2Launcher.jar

# --------------------------------------------------------
# List important facts
# --------------------------------------------------------

Message "All about the system"

About;

Message "Now reboot the system - required" "So that the latest version of the server will start"

Message "END of script"

# ----------------------------------------------------------------------------------
# End of script
# ----------------------------------------------------------------------------------
