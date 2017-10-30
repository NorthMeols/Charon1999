# ----------------------------------------------------------------

if [ ! "$BASH_VERSION" ]
then
    echo Shell not bash! re-executed with bash
    exec /bin/bash "$0" "$@"
fi

# ----------------------------------------------------------------

function CHMOD
{
  if [ -e $2 ]
  then
    chmod $@
  fi
}

function CP
{
  if [ -e $1 ]
  then
    cp $1 $2
  fi
}

function PROLOGUE
{
  export PREV_EXE=$1
  export EXERCISE=$2

  if [ ! -e $EXERCISE ]
  then
    mkdir $EXERCISE
  fi

  if [ $1 != $2 ]
  then
    CP $PREV_EXE/compile   $EXERCISE/compile
    CP $PREV_EXE/run       $EXERCISE/run
    CP $PREV_EXE/runPre    $EXERCISE/runPre
  fi
}

function EPILOGUE 
{
  if [ $EXERCISE != $PREV_EXE ]
  then
    CP $PREV_EXE/by	$EXERCISE/by
  fi
  CHMOD 744 $EXERCISE/by
  CHMOD 755 $EXERCISE/compile 
  CHMOD 755 $EXERCISE/run
  CHMOD 755 $EXERCISE/runPre
  ((count=1))
  while [ -f $EXERCISE/data$count ]
  do
    CHMOD 744 $EXERCISE/data$count   $EXERCISE/result$count
    CHMOD 755 $EXERCISE/script$count
    ((count++))
  done
}

# --------------------- Building example 1.1 --------------------- 
# A program to print Hello world
#  Note: PROLOGUE 1.1 1.1 will only create the directory 1.1
# ----------------------------------------------------------------

PROLOGUE 1.1 1.1

cat <<+END+ > $EXERCISE/compile
#!/bin/bash
chmod 755 .

cp /opt/charon/templates/test/BIO.class .
cat \$1 > Main.java
PATH="/opt/java/current/bin:$PATH"
export PATH
echo javac Main.java
echo "classes in application"
grep "class " Main.java
javac Main.java
+END+

cat <<+END+ > $EXERCISE/run
#!/bin/bash
PATH="/java/bin:$PATH"
if [ -f Main.class ]
then
  java Main < \$1 | cat -v | grep -v "#"
else
  echo "++ ERROR ++"
  echo "   Your program did not compile or create the file Main.class"
  echo "   Scroll back in this window to find more details"
fi
+END+

cat <<+END+ > $EXERCISE/data1
+END+

cat <<+END+ > $EXERCISE/result1
Hello Brighton
+END+

cat <<+END+ > $EXERCISE/by
21/10/2058 03:00
+END+

EPILOGUE

# --------------------- Building example 1.2 --------------------- 
# A program to input two numbers from stdin and print the sum
# ----------------------------------------------------------------

PROLOGUE 1.1 1.2

cat <<+END+ > $EXERCISE/data1
2
3
+END+

cat <<+END+ >$EXERCISE/result1
5
+END+

cat <<+END+ >$EXERCISE/data2
20
30
+END+

cat <<+END+ >$EXERCISE/result2
50
+END+

EPILOGUE

# --------------------- Building example 1.3 --------------------- 
# A program to copy the stdin to a file called file.txt
# ----------------------------------------------------------------

PROLOGUE 1.1 1.3

cat <<+END+ > $EXERCISE/runPre
#!/bin/bash
rm -f -r writable    >& /dev/null
if [ ! -d writable ]
then
  mkdir writable
  chmod 777 writable
fi
+END+

cat <<+END+ > $EXERCISE/run
#!/bin/bash
PATH=/java/bin:$PATH
cd writable

if [ -f ../Main.class ]; then
 export CLASSPATH=..:$CLASSPATH
 java  Main < ../\$1
 cat file.txt
else
  echo "++ ERROR ++"
  echo "   Your program did not compile or create the file Main.class"
  echo "   Scroll back in this window to find more details"
fi
+END+


cat <<+END+ > $EXERCISE/by
1/2/2058:08:09:10
+END+


cat <<+END+ > $EXERCISE/data1
Hello
+END+

cat <<+END+ > $EXERCISE/result1
Hello
+END+

cat <<+END+ > $EXERCISE/data2
World
+END+

cat <<+END+ > $EXERCISE/result2
World
+END+

EPILOGUE

# --------------------- Building example 1.4 --------------------- 
# A program to read the contents of the file secret.txt and write
# the contents minus the first and last printing characters to
# a newly created file called notSoSecret.txt
# ----------------------------------------------------------------

PROLOGUE 1.1 1.4

cat <<+END+ > $EXERCISE/runPre
#!/bin/bash
rm -f -r writable
if [ ! -d writable ]
then
  mkdir writable
  chmod 777 writable
fi
+END+

cat <<+END+ > $EXERCISE/run
#!/bin/bash
PATH=/java/bin:$PATH
cd writable
if [ -e ../Main.class ]; then
 export CLASSPATH=..:$CLASSPATH
 bash ../script                           ## Run specific script ##
 # Now we get a subdirectory ? containing jre_oracle_usage
 java Main < ../$1 
 cat notSoSecret.txt
else
  echo "++ ERROR ++"
  echo "   Your program did not compile or create the file Main.class"
  echo "   Scroll back in this window to find more details"
fi
+END+

# - - - - - - - - - - - - - - - - - - - - - - - - - -

cat <<+END+ > $EXERCISE/by
1/2/2058:08:09:10
+END+

cat <<+END+ > $EXERCISE/script1
echo [Hello out there] > secret.txt
+END+

cat <<+END+ > $EXERCISE/data1
+END+

cat <<+END+ > $EXERCISE/result1
Hello out there
+END+

# - - - - - - - - - - - - - - - - - - - - - - - - - -

cat <<+END+ > $EXERCISE/script2
echo [A simple message] > secret.txt
+END+

cat <<+END+ > $EXERCISE/data2
+END+

cat <<+END+ > $EXERCISE/result2
A simple message
+END+

EPILOGUE

# --------------------- Building example 2.1 --------------------- 

PROLOGUE 1.1 2.1

cat <<+END+ > $EXERCISE/compile
#!/bin/bash
# Compile the users Ada program
chmod 777 .
cp \$1 main.ada
echo gnatchop -w main.ada
gnatchop -w main.ada
echo gnatmake -o a.out main
: gnatmake -o a.out main -largs -static
gnatmake -o a.out main 
+END+

cat <<+END+ > $EXERCISE/run
#!/bin/bash
# if no a.out file then the program did not compile
if [ -f a.out ]; then
 ./a.out < \$1
else
  echo "+++ Your program did not compile +++"
  echo "    Scroll back in window to find compilation errors ---"
fi
+END+

cat <<+END+ > $EXERCISE/by
1/2/2020:08:09:10
+END+

cat <<+END+ > $EXERCISE/data1
+END+

cat <<+END+ > $EXERCISE/result1
Hello world
+END+

EPILOGUE

# --------------------- Building example 2.2 --------------------- 

PROLOGUE 1.1 2.1

cat <<+END+ > $EXERCISE/compile
#!/bin/bash
chmod 777 .
# Compile the users C++ program
cp \$1 main.cpp
echo g++ main.cpp
g++ main.cpp
+END+

cat <<+END+ > $EXERCISE/run
#!/bin/bash
if [ -f a.out ]; then
 ./a.out < \$1
else
  echo "+++ Your program did not compile +++"
  echo "    Scroll back in window to find compilation errors ---"
fi
+END+

cat <<+END+ > $EXERCISE/data1
+END+

cat <<+END+ > $EXERCISE/result1
Hello world
+END+

EPILOGUE

# --------------------- Building example 2.3 --------------------- 

PROLOGUE 1.1 2.3

cat <<+END+ > $EXERCISE/compile
#!/bin/bash
chmod 777 .
cp \$1 main.java
echo javac main.java
echo "classes in application"
grep "class " main.java
javac  main.java
+END+


cat <<+END+ > $EXERCISE/run
#!/bin/bash
PATH=/java/bin:$PATH
if [ -f Main.class ]; then
 java  Main < \$1
else
  echo "++ ERROR ++"
  echo "   Your program did not compile or create the file Main.class"
  echo "   Scroll back in this window to find more details"
fi
+END+

cat <<+END+ > $EXERCISE/data1
+END+

cat <<+END+ > $EXERCISE/result1
Hello world
+END+

EPILOGUE

# --------------------- Building example 2.4 --------------------- 

PROLOGUE 2.3 2.4

cat <<+END+ > $EXERCISE/data1
+END+

cat <<+END+ > $EXERCISE/result1
Hello world
+END+

EPILOGUE

# --------------------- Building example about --------------------- 

PROLOGUE 1.1 about

cat <<+END+ > $EXERCISE/compile
#!/bin/bash
 echo "+++Compile action"
 chmod 755 .
+END+


cat <<+END+ > $EXERCISE/runPre
#!/bin/bash
 if [ ! -d writable ]
 then
   mkdir writable
   chmod 777 writable
 fi
+END+

cat <<+END+ > $EXERCISE/run
#!/bin/bash
function Message
{
  echo "-----------------------------------------------------------------------------------"
  printf "| %-80s|\n" "\$@"
  echo "-----------------------------------------------------------------------------------"
}

 Message "Executing run script change to directory / "
 pushd /
 cd /
 echo "ls  -l /"
 ls -l /
 echo "ls  -l /sandbox"
 ls -l /sandbox
 

 Message  "popd to directory containing scripts not writable"
 popd
 
 ls -ld .
 echo "NotPossible" >ShouldNotBeAbleToCreate
 Message "In directory:  \`pwd\` "
 ls -la --time-style=full-iso 
 Message "Change to writable"
 cd writable
 echo "In directory: " \`pwd\`
 echo "" > CreatedByMe
 chmod 777 CreatedByMe
 ls -la --time-style=full-iso 

 Message "I can see processes:"
 ls -C -d /proc/[01-9][0-9]*
 
 Message "My limits are"
 cat /proc/self/limits
 
 Message "My ID is"
 echo  "   " \`id\`
 cd ..

+END+

 echo "-----------------------------------------------------------"

chmod 775 $EXERCISE/run

cat <<+END+ > $EXERCISE/script1
 echo "+++Script action"
 cd writable
 echo "" > "CreatedScript"
 ls -C
+END+


cat <<+END+ > $EXERCISE/data1
+END+

cat <<+END+ > $EXERCISE/result1
+END+

EPILOGUE

# --------------------------------------------------------
