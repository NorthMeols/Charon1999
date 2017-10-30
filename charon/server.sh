BIN=/opt/charon/bin

ps axl | grep java

# ulimit -s 2048
# export J2SE_PREEMPTCLOSE=1
# export LD_ASSUME_KERNEL=2.2.5
# export PATH

echo "STARTING the charon system " `date`

if [ ! -e $BIN/jcl_safe ]; then
  echo "You have not run set_up.sh"
  exit
fi

if [ ! -u $BIN/jcl_safe ]; then
  echo "May not be running as root"
  exit
fi

if [ ! -d /opt/charon/playpen/sandbox ]; then
  echo "/opt/charon/playpen/sandbox must exist"
  echo "You may not have run set_up.sh"
  exit
fi

if [ ! -e /opt/charon/playpen/proc/version ]; then
  mount  -n -t proc defaults,hidepid=2 playpen/proc
fi

if [ ! -e /opt/charon/playpen/proc/version ]; then
  echo "/opt/charon/playpen/proc/version must exist"
  exit
fi

cd $BIN
pwd
ls -l Server*
su charon <<+END+
 echo java -server -d64 -jar Server.jar 50000  /opt/charon/
 java -server -d64 -jar Server.jar 50000  /opt/charon/
+END+

# ----- END OF SCRIPT

