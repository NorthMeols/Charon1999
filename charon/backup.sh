#!/bin/bash

if [ ! "$BASH_VERSION" ]
then
    echo Shell not bash
    exec /bin/bash "$0" "$@"
fi

function setBase
{
  if [ -d /cygdrive/d/ ]
  then
    export BASE=/cygdrive/d/
  elif test -d /dosd/
  then
    export BASE=/dosd/
  elif [ -d /media/D_DRIVE/ ]
  then
    export BASE=/media/D_DRIVE/
  elif [ -d /media/sf_D_DRIVE/ ]
  then
    export BASE=/media/sf_D_DRIVE/
  else
    echo "ERROR - can not find backup directory"
    exit 1
  fi
}

setBase ;
cd /opt/charon/jcl
echo "yes" | sh setPassword.sh

cd /opt/charon

sh clean.sh
tar cvf /tmp/charon.tar *   >& /dev/null

# -------------------------------------------------------------------

#########################################
# Do not change beyond this line        #
#########################################
export DAY=`date "+%d"`
export MONTH=`date "+%m"`
export YEAR=`date "+%G"`

export PREFIX=$YEAR-$MONTH-$DAY
echo $PREFIX

cd /tmp

# 7z a -t7z -mx9 -mfb=256
7z a -t7z -mx=9 -md256m -mfb=256 charon.tar.7z charon.tar


if [ -d /media/sf_E_DRIVE ]
then
  cp /tmp/charon.tar.7z /media/sf_E_DRIVE/backup/charon-$PREFIX.7z
  cp /tmp/charon.tar.7z /media/sf_E_DRIVE/00\ DropBox/Dropbox/work/charon-$PREFIX.7z
  ls -l  /media/sf_E_DRIVE/backup/charon-$PREFIX.7z
fi

