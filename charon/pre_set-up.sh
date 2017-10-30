# ----------------------------------------------------------------------------------
# pre setup 
# Change some system files
# ----------------------------------------------------------------------------------
set -e

export OS=64

if [ ! "$BASH_VERSION" ]
then
    echo Shell not bash! re-executed with bash
    exec /bin/bash "$0" "$@"
fi


function Message
{
  echo "-----------------------------------------------------------------------------------"
  printf "| %-80s|\n" "$@"
  echo "-----------------------------------------------------------------------------------"
}

# ----------------------------------------------------------------------------------
# pre setup 
# Simple check that the Charon system has been unpacked here
# In now ways exhaustive
# ----------------------------------------------------------------------------------

Message "Check Charon files are installed"

if [ ! -e /opt/charon ]
then
  echo "Strange: Have you unpacked the charon system"
  echo "  Directory /opt/charon missing"
  exit
fi

if [ ! -e /opt/charon/scripts ]
then
  echo "Strange: Have you unpacked the charon system"
  echo "  Directory /opt/charon/scripts missing"
  exit
fi

function CHECKINSTALLED
{
  result=`dpkg -s $1 | grep "ok installed" | awk '{ print \$3 }'`
  echo Check if package $1 has been installed
  if [ X$result != Xok ]
  then
    echo Package $1 not installed
    echo "Rerun and answer Y to install request"
    echo "$0" will exit NOW!
    exit
  else
    echo "  $1 has been installed"
  fi
}


Message "Install required software"

if [ ! -e /opt/charon/SOFTWARE_INSTALLED ]
then
  echo "----------------------------------------------------"
  echo "Answer Y to the request to install software"
  echo "  If you get the error message"
  echo "  Could not get lock /var/lib/dpkg/lock - open (11: Resource temporarily unavailable)"
  echo "  Retry after important software has been installed automatically by the system"
  echo "----------------------------------------------------"
  
  apt-get install g++ apache2 perl libnet-ldap-perl shellinabox make

  CHECKINSTALLED g++ 
  CHECKINSTALLED apache2 
  CHECKINSTALLED perl 
  CHECKINSTALLED libnet-ldap-perl 
  CHECKINSTALLED shellinabox
  CHECKINSTALLED make
  
  echo "" > /opt/charon/SOFTWARE_INSTALLED
fi



cd /opt/charon/scripts

if [ -f /etc/rc.local.bak ]
then
  Message "Already run pre-setup"                              \
          "If you need to rerun delete /etc/rc.local.bak"      \
          "Think about this"
  exit
fi

# if [ -e /var/lib/dpkg/lock ]
# then
#   if [ -s /var/lib/dpkg/lock ]
#   then
#     echo "----------------------------------------------"
#     echo "Lock /var/lib/dpkg/lock claimed"
#     echo "fuser /var/lib/dpkg/lock"
#     echo "Will give the process using the lock"
#     echo "Try again later"
#     echo "----------------------------------------------"
#     exit
#   fi
# fi

# ----------------------------------------------------------------------------------
# Under systemd /etc/rc.local is not automatically created
# So create /etc/rc.local if not there
#    This will then be backed up to /etc/rc.local.bak & recreated
# ----------------------------------------------------------------------------------

Message "Fix rc.local environment fstab crontab: All in /etc"

if [ ! -f /etc/rc.local ]
then
  cp etc/rc.local /etc/rc.local 
  chmod +x /etc/rc.local
  # systemctl enable rc-local
  echo "Created /etc/rc.local (Must be executable)"
fi


if [ ! -f /etc/rc.local.bak ]
then
  cp             /etc/rc.local    /etc/rc.local.bak
  cp              etc/rc.local    /etc/rc.local
  chmod 755      /etc/rc.local
  echo "Modified /etc/rc.local"
fi

if [ ! -e /etc/environment.bak ]
then
  cp             /etc/environment /etc/environment.bak
  cp              etc/environment /etc/environment
  chmod 644      /etc/environment
  echo "Modified /etc/environment"
fi

if [ ! -e /etc/fstab.bak ]
then
  cp /etc/fstab /etc/fstab.bak
  awk -i inplace 'BEGINFILE{print "proc /proc proc defaults,hidepid=2 0 0"} !/\/proc/ {print;}' /etc/fstab
  echo "Modified /etc/fstab (/proc entry added)"
fi

if [ -e /etc/default/shellinabox ]
then
  sed -i 's/=4200/=8080/' /etc/default/shellinabox
  echo "Modified /etc/defaults/shellinabox"
fi

if [ ! -e /etc/crontab.bak ]
then
  cp /etc/crontab /etc/crontab.bak
  cat <<+END+  >> /etc/crontab
55 *    * * *   root    bash /opt/charon/system/results/transfer.sh >> ~charon/log.txt 2>&1
+END+
  chmod 644 /etc/crontab /etc/crontab.bak
  echo "Modified /etc/crontab"
fi

cd /opt/charon

Message " Now reboot the system - required"     \
        "  Then run set-up.sh"                  \
        "  To install the Charon system"

Message "END of script"

# ----------------------------------------------------------------------------------
# End of script
# ----------------------------------------------------------------------------------
