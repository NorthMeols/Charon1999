echo "************************************************"
echo "* BUILD standalone Charon client               *"
echo "************************************************"

STOREPASS=`date "+%N%j%s"`
KEYNAME=`date "+KEYNAME"`
KEYPASS=`date "+%N%j%s"`
JAVA_FLAGS=-g:none

javac $JAVA_FLAGS Charon.java jc_useful.java Message.java

echo STOREPASS = $STOREPASS KEYPASS = $KEYPASS KEYNAME = $KEYNAME

jar cvfe CharonA.jar Charon *.class | grep -v deflated

keytool   -genkey -keyalg RSA -alias $KEYNAME        \
          -keystore _store                             \
          -storepass $STOREPASS -keypass $KEYPASS  \
	  -validity 360 -keysize 2048                  \
	  -dname "cn=MAS, OU=CEM, O=Computing, L=W424"

keytool   -selfcert -alias $KEYNAME -keystore _store \
	  -storepass $STOREPASS -keypass $KEYPASS  \
          -validity 9999

jarsigner -keystore _store \
	  -storepass $STOREPASS -keypass $KEYPASS  \
	  -signedjar Charon.jar CharonA.jar            \
	  $KEYNAME

jarsigner -verify -verbose Charon.jar

rm -f _store CharonA.jar

echo    "   <TT>"                                                 > charon.txt
echo -n "    <BR>md5&nbsp;&nbsp;&nbsp; "                          >> charon.txt
        md5sum Charon.jar | sed -e "s/Charon.jar//"               >> charon.txt
echo -n "    <BR>sha256 "                                         >> charon.txt
        sha256sum Charon.jar | sed -e "s/Charon.jar//"            >> charon.txt
echo "   </TT>"                                              	  >> charon.txt

cat charon.txt

cp Charon.jar /tmp
