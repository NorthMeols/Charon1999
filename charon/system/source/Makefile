# Makefile for charon system
# Do not use -O flag to Java (Resultant code will not run)

#-------------------------------------------------------------------
BINDIR=/var/www/html/charon
BINDIR=/opt/charon/bin

CPP= g++
CPPFLAGS= -O3 

JAVA= javac -Xlint:unchecked 
JAVA= javac

JAVA_FLAGS=-g:none
JAVA_FLAGS=-g -Xlint:deprecation
JAVA_FLAGS_CLIENT=-g:none -Xlint:deprecation

#-------------------------------------------------------------------
#KEYPASS   = mpdge9d61jx74hw4cv1
#STOREPASS = wj9x6hqisz92l57f2ma
#KEYNAME   = kdy8t2dswunzyt541o9
#
#STOREPASS := $(shell date "+%N%s" )
#KEYNAME   := $(shell date "+%N%s" )
#KEYPASS   := $(shell date "+%N%s" )
#
#STOREPASS := $(shell tr -dc A-Za-z0-9 < /dev/urandom | head -c50 )
#KEYNAME   := $(shell tr -dc A-Za-z0-9 < /dev/urandom | head -c50 )
#KEYPASS   := $(shell tr -dc A-Za-z0-9 < /dev/urandom | head -c50 )
#
#KEYPASS   = $(STOREPASS)

all :			clean							\
			Charon.jar						\
			Server.jar						\
			Inspect.jar						\
			compare login jcl jcl_safe diff 			

jcl :			jcl.cpp
			$(CPP) $(CPPFLAGS) -o jcl jcl.cpp

jcl_safe :		jcl_safe.cpp
			$(CPP) $(CPPFLAGS) -o jcl_safe jcl_safe.cpp

compare :		compare.cpp
			$(CPP) $(CPPFLAGS) -o compare compare.cpp

diff :			diff.cpp
			$(CPP) $(CPPFLAGS) -o diff diff.cpp

login :			login.cpp
			$(CPP) $(CPPFLAGS) -o login login.cpp

# ---------------------------------------------------------------

Charon.jar :		client/Client.java
			rm -f *.class */*.class
			javac $(JAVA_FLAGS_CLIENT) client/Client.java

			echo Manifest-Version: 1.0				> Manifest.txt
			echo Created-By: MAS					>> Manifest.txt
			echo Main-Class: client.Client           		>> Manifest.txt
			echo Application-Name: Charon           		>> Manifest.txt
			echo Codebase: charon.it.brighton.ac.uk, localhost  	>> Manifest.txt

			jar cfm Charon.jar Manifest.txt				\
				client/*.class 					\
				structure/*.class 				\
				utils/*.class 					\
				customise/*.class

#-------------------------------------------------------------------


Server.jar :		server/Server.java
			rm -f */*.class
			javac $(JAVA_FLAGS) server/Server.java

			echo Manifest-Version: 1.0				> Manifest.txt
			echo Created-By: MAS					>> Manifest.txt
			echo Main-Class: server.Server           		>> Manifest.txt
			echo Application-Name: Server           		>> Manifest.txt
			echo Permissions: sandbox       			>> Manifest.txt
			echo Codebase: charon.it.brighton.ac.uk, localhost  	>> Manifest.txt

			jar cfm Server.jar Manifest.txt				\
				server/*.class 					\
				structure/*.class 				\
				utils/*.class 					\
				customise/*.class

#-------------------------------------------------------------------


Inspect.jar :		inspect/Inspect.java
			rm -f */*.class
			javac $(JAVA_FLAGS) inspect/Inspect.java

			echo Manifest-Version: 1.0				> Manifest.txt
			echo Created-By: MAS					>> Manifest.txt
			echo Application-Name: Inspect           		>> Manifest.txt
			echo Main-Class: inspect.Inspect           		>> Manifest.txt
			echo Permissions: sandbox       			>> Manifest.txt
			echo Codebase: charon.it.brighton.ac.uk, localhost  	>> Manifest.txt

			jar cfm Inspect.jar Manifest.txt			\
				inspect/*.class 				\
				utils/*.class					\
				customise/*.class


#-------------------------------------------------------------------
			
clean :
			rm -f *.class */*.class jcl *.jar jcl_safe compare login runas diff
			rm -f Manifest.txt
			rm -f -r html


install : 		all
			cp Charon.jar		$(BINDIR)
			cp Server.jar		$(BINDIR)
			cp Inspect.jar		$(BINDIR)
			cp jcl jcl_safe		$(BINDIR)
			cp compare diff		$(BINDIR)
			cp login		$(BINDIR)
			chown root:root		$(BINDIR)/jcl_safe
			chmod 6755		$(BINDIR)/jcl_safe
doc:
			echo "Making doc"
			javadoc -sourcepath $(WIN_SDK1)\\src.zip \
			-group Catalogue Catalogue \
			-header "<FONT color="teal">Charon</FONT>" \
			-author -windowtitle "Charon" \
			-use -splitindex -d html \
			-package *.java 
