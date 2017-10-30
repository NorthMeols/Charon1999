#!bash

if [ ! "$BASH_VERSION" ]
then
    echo Shell not bash - re-execute script with bash
    exec /bin/bash "$0" "$@"
fi

function Message
{
  echo "-----------------------------------------------------------------------------------"
  printf "| %-80s|\n" "$@"
  echo "-----------------------------------------------------------------------------------"
}

Message  "V3.93 Backup and transfer backup to remote machine";

PATH="/opt/java/current/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games"

CI101="ci101 ci101-resit" 
CI101ee="ci101.ee"
CI228="ci228 ci228-resit"
CI229=""
CI283="ci283"
CI283="ci283 ci283-resit"
TEST="test"
HASTINGS="hastings"
HASTINGSee="hastings.ee"
HASTINGS=""
HASTINGSee=""

FILES="/home/mas/old_2012_end/"
FILES="/opt/charon/"

##################################################
# Set to yes if require backup                   #
#  to remote machines                            #
# Machine also must have IP  193.62.183.45       #
#  See check later in script
##################################################

export BACKUP=no
export BACKUP=yes   # Will fail if m/c is not 193.62.183.45

##################################################
# Do not change beyond this line                 #
##################################################

# m h dom mon dow user	command
# 55 *    * * *   root  bash /opt/charon/system/results/transfer.shh >> ~charon/log.txt
# 55 1,3,8,9,10,11,12,13,14,15,16,17,19,21,23    * * *   root    bash /opt/charon/system/results/transfer.sh >> ~charon/log.txt 2>&1
# #

export COMPRESS=bz2
export COMPRESS=7z

export ARC=tar.$COMPRESS


function dateStamp()
{
  echo "++ " `date` " :: " "$@"
}

function setTime()
{
  export DAY=`date "+%a"`
  export WEEK=`date "+%V"`
  export DAYNUM=`date "+%d"`
  export MONTH=`date "+%b"`
  export YEAR=`date "+%G"`
  export HOUR=`date "+%H"`
  export MIN=`date "+%M"`
  export SECOND=`date "+%S"`
  export WEEK_NO=$YEAR-WEEK-$WEEK
  export DAY_NAME=$DAY
}

setTime;

# export DAY_NAME=$YEAR-$MONTH-$DAY


cd /home/charon/backup

# at now + 2 hours < transfer

##################################################
# Start the processing                           #
##################################################

Message  "When:  $WEEK_NO  $DAY $YEAR/$MONTH/$DAYNUM  $HOUR.$MIN.$SECOND ";

##################################################
# Generate current state of completed work       #
#  for each student                              #
# But take out teachers                          #
##################################################

dateStamp " Start to process statistics for courses"

pushd /opt/charon/system/results >& /dev/null

BIN_DIR="/opt/charon/bin"
echo -n "Processing "
for CLASS in $CI101 $CI101ee $CI228 $CI229 $CI283 $HASTINGS $HASTINGSee $TEST
do
 if [ -d ../../students/$CLASS ]
 then
   #echo Processing $CLASS BIN_DIR = $BIN_DIR Dir = `pwd`
   echo -n $CLASS " "
   # Exclude those users who are not students
   EXCLUDE=",ab1184\|ss935\|maj27\|,vmk12\|,nb345\|,mas\|,na179\|,rs602\|,gs3\ \|,as781"
   EXCLUDE=",ab1184\|ss935\|maj27\|,vmk12\|,nb345\|,na179\|,rs602\|,gs3\ \|,as781"
   # Which of course does not exist
   EXCLUDE=",xyzzy\|"                                                                  

     java -jar $BIN_DIR/Inspect.jar R $CLASS base.db 4 -B$FILES  -S -L | sed -e "s/ *$//" | sort | \
     grep -v "$EXCLUDE" | \
     sort -n -b -k  4,4.0 -k 3,3.0  > $CLASS.completed.SN.txt

     java -jar $BIN_DIR/Inspect.jar R $CLASS base.db 4 -B$FILES -L | sed -e "s/ *$//" | sort | \
     grep -v "$EXCLUDE" > $CLASS.sorted.txt

   sort -b -k     2,2.0 -k 1,1.0  < $CLASS.sorted.txt > $CLASS.award.txt
   sort -n -b -k  3,3.0 -k 2,2.0  < $CLASS.sorted.txt > $CLASS.completed.txt
   sort -b -k     2,2.0 -k 3,3.0n < $CLASS.sorted.txt > $CLASS.completed.award.txt
   BASE=/opt/charon/students/$CLASS/
   if [ -f  $BASE/progress.sh ]
   then
     pushd $BASE >& /dev/null
     sh progress.sh > /opt/charon/system/results/$CLASS.global.txt
     popd >& /dev/null
   fi
 fi
done
echo ""


##################################################
# awk script  to produce statistics file         #
##################################################

dateStamp " Finished generating list of files"

cat <<+END+ > _tmp

# ------------------------------------------------------------------
# The actual awk script to produce statistics
#  Need to change some parts if change the number of exercises in 
#  a course, or add a new course
# ------------------------------------------------------------------

BEGIN { 
}

{
  # col 1 = name, col 2 = award, col 3 = completed, col 4 = , col 5 = 
  # col 6 - 28 students progress per exercise
  award=\$2;
  finished=\$3;

  
  completed[finished]++;       # number completing n exercises

  # exercises after col 5
  for ( i=6; i<=NF; i++ )
  {
    if ( match( \$i, "OK") )
    {
      worked[award][i]++;
    }

    if ( match( \$i, "[1-9][0-9]*" ) )
    {
      lateWorked[award][i]++;
      #printf("%s %s late %s\n", \$1, \$2, \$i ); 
      firstExMax[c]++;
    }

    if ( match( \$i, "a" ) )
    {
      attempted[award][i]++;
    }
  }
  studentNum[award]++;
}

# ------------------------------------------------------------------
# Number completing exercise by award
# ------------------------------------------------------------------

function underline( len )
{
  printf("-------" );
  for ( i=1; i<=(len); i++ )
  {
    printf("----");
  }
  printf("\n");
}


# ------------------------------------------------------------------
# Exercises completed by students on a specific course
# ------------------------------------------------------------------

function questionsCompleted( number )
{
  printf("Questions # completed : ", number );
  for ( i=0; i<=number; i++ )
  {
    printf( " %4d", i );
  }
  print "";


  printf("# completed           : " );
  for ( i=0; i<=number; i++ )
  {
    printf( " %4d", completed[i] );
  }
  print "";

    
  printf("As a percentage       : " );
  for ( i=0; i<=number; i++ )
  {
    printValue( totalStudents, completed[i] );
  }
  print "";

  for ( i=number; i>=0; i-- )
  {
   aCompleted[i] = completed[i]+aCompleted[i+1];
  }

  printf("Cumulative percentage : " );
  for ( i=0; i<=number; i++ )
  {
    printValue( totalStudents, aCompleted[i] );
  }
  print "";

}

function printValue( total, value )
{
  if ( total == 0 ) 
    printf( "  0.0" );
  else
    if ( total == value )
      printf( "  100" );
    else
      printf( "%5.1f", (value*100.0)/total );
} 

# ------------------------------------------------------------------
# Exercises completed by students on a specific course
# ------------------------------------------------------------------

function byAwardCompleted( title, startCol, endCol )
{
  if ( NUMBER < endCol ) exit;
  print "";
  print "Percentage of exercises in " title " completed on time for a specific course";

  printf("%6s  %-10s     Number\n", "%", "Award"  );
  printf("-------------------------------\n" );

  for ( c in worked )
  {
    for ( s=6+startCol-1; s<6+endCol; s++ )
    {
      wk[s] = 0;  # Worked per course
      at[s] = 0;
      lw[s] = 0;
    }

    for ( s=6+startCol-1; s<6+endCol; s++ )
    {
      wk[s] += worked[c][s];
      at[s] += attempted[c][s];
      lw[s] += lateWorked[c][s];
    }
    
    total=0;
    for ( s=6+startCol-1; s<6+endCol; s++ )
    {
     total += wk[s];
    }

    numOnAward = studentNum[c];
    per = (total * 100.0)/(numOnAward*(endCol-startCol+1));
    unsorted[c]=sprintf("%6.1f  %-10s %3d Student%s", per, c, numOnAward, numOnAward==1?"":"s" );
#   printf("%-5s | %d\n", c, per );
  }

  asort( unsorted, sorted )

  for ( i=1; i in sorted; i++ )
    printf( "%s\n", sorted[i] );

# ------------------------------------------------------------------
# Overall completion rate
# ------------------------------------------------------------------

  printf("-------------------------------\n" );

  for ( s=6+startCol-1; s<6+endCol; s++ )
  {
    wk[s] = 0;
    at[s] = 0;
    lw[s] = 0;
  }

  for ( c in worked )
  {
    for ( s=6+startCol-1; s<6+endCol; s++ )
    {
      wk[s] += worked[c][s];
      at[s] += attempted[c][s];
      lw[s] += lateWorked[c][s];
    }
  }

  total=0;
  for ( s=6+startCol-1; s<6+endCol; s++ )
  {
    total += wk[s];
  }

  # printf("total = %d totalStudents = %d \n", total, totalStudents );

  if ( totalStudents < 1 )
    per = 0.0;
  else
    per = (total * 100.0)/(totalStudents*(endCol-startCol+1));

  printf("%6.1f  %-10s %3d\n", per, "Overall", totalStudents  );
  printf("-------------------------------\n" );
  print "";
  print "";
}

# ------------------------------------------------------------------
# Now process data
# ------------------------------------------------------------------

END { 
  totalStudents=0;
  for ( c in studentNum )
  {
    totalStudents += studentNum[c];
  }

  if ( totalStudents == 0 ) totalStudents = 0;  # Now OK DELETE

  print "";
  print "COURSE" " - Number completing each exercise by award"
  print "        " "EXERCISES"

  underline( NUMBER );

  for ( c in worked )
  {
    printf( "%-5s |", c );
    for ( s=6; s<6+NUMBER; s++ )
    {
      printf( "%4d", worked[c][s]+lateWorked[c][s] );
      wk[s] += worked[c][s];
      lw[s] += lateWorked[c][s];
      at[s] += attempted[c][s];
    }
    printf( "\n" );
  }

  underline( NUMBER );

  printf( "%-6.6s|", "Worked" );
  for ( s=6; s<6+NUMBER; s++ )
  {
    printf( "%4d", wk[s] );
  }
  printf( "\n" );

  printf( "%-6.6s|", "Late Worked" );
  for ( s=6; s<6+NUMBER; s++ )
  {
    printf( "%4d", lw[s] );
  }
  printf( "\n" );

  printf( "%-6.6s|", "Attempted" );
  for ( s=6; s<6+NUMBER; s++ )
  {
    printf( "%4d", at[s] );
  }
  printf( "\n" );

  printf( "%-6.6s|", "Engaged" );
  for ( s=6; s<6+NUMBER; s++ )
  {
    printf( "%4d", wk[s] + lw[s] + at[s] );
    wk[s] = 0; at[s] = 0; lw[s] = 0;
  }
  printf( "\n" );

  underline( NUMBER );

# ------------------------------------------------------------------
# As above but as a percentage
# ------------------------------------------------------------------

  print "";
  print "COURSE" " - Percentages completing each exercise by award"
  print "        " "EXERCISES"

  underline( NUMBER );

  for ( c in worked )
  {
    printf( "%-5s |", c );
    max = totalStudents;
    max = 1.0 * max;
    for ( s=6; s<6+NUMBER; s++ )
    {
      numOnAward = studentNum[c];
      printf( "%4.0f", (worked[c][s]+lateWorked[c][s])*100.0/numOnAward );
      wk[s] += worked[c][s];
      lw[s] += lateWorked[c][s];
      at[s] += attempted[c][s];
      
    }
    printf( "\n" );
  }

  underline( NUMBER );

  printf( "%-6.6s|", "Worked" );
  max = totalStudents;
  max = 1.0 * max;
  for ( s=6; s<6+NUMBER; s++ )
  {
    if ( totalStudents < 1 )
      printf( "%4.0f", 0 );
    else
      printf( "%4.0f", wk[s]*100.0/totalStudents );
  }
  printf( "\n" );

  printf( "%-6.6s|", "Late Worked" );
  for ( s=6; s<6+NUMBER; s++ )
  {
    if ( totalStudents < 1 )
      printf( "%4.0f", 0 );
    else
      printf( "%4.0f", lw[s]*100.0/totalStudents );
  }
  printf( "\n" );

  printf( "%-6.6s|", "Attempted" );
  for ( s=6; s<6+NUMBER; s++ )
  {
    if ( totalStudents < 1 )
      printf( "%4.0f", 0 );
    else
      printf( "%4.0f", at[s]*100.0/totalStudents );
  }
  printf( "\n" );

  printf( "%-6.6s|", "Engaged" );
  for ( s=6; s<6+NUMBER; s++ )
  {
    if ( totalStudents < 1 )
      printf( "%4.0f", 0 );
    else
      printf( "%4.0f", (at[s]+wk[s]+lw[s])*100.0/totalStudents );
  }
  printf( "\n" );

  underline( NUMBER );

# ------------------------------------------------------------------
# Completion work per exercise
#  Now this must be changed if you change the number of exercises or
#  add a new course.
# ------------------------------------------------------------------

  if (NUMBER == 21 )
  {
    byAwardCompleted( "group 1", 1, 5 );      # Group 1
    byAwardCompleted( "group 2", 6, 10 );     # Group 2
    byAwardCompleted( "group 3", 11, 15 );    # Group 3
    byAwardCompleted( "SEM 1", 1, 15 );       # All of semester 1
  
  
    byAwardCompleted( "group 4", 16, 18 );    # Group 4
    byAwardCompleted( "group 5", 19, 20 );    # Group 5
    byAwardCompleted( "group 6", 21, 21 );    # Group 6
  
    byAwardCompleted( "SEM 1/2", 1, 21 );     # All of semester 1/2

    questionsCompleted( 21 );
  }

  if ( NUMBER == 3 )
  {
    byAwardCompleted( "group 1", 1, 2 );      # Group 1
    questionsCompleted( 3 );
  }

  if ( NUMBER == 5 )
  {
    byAwardCompleted( "group 1", 1, 5 );      # Group 1
    questionsCompleted( 5 );
  }

  if ( NUMBER == 4 )
  {
    byAwardCompleted( "group 1", 1, 4 );      # Group 1
    questionsCompleted( 4 );
  }

  if ( NUMBER == 6 )
  {
    byAwardCompleted( "group 1", 1, 4 );      # Group 1
    byAwardCompleted( "group 2", 5, 6 );      # Group 2
    questionsCompleted( 6 );
  }
}

# ------------------------------------------------------------------
# END
# ------------------------------------------------------------------

+END+

##################################################
# Calculate percentages completing               #
##################################################


EXERCISES="1.1 1.2 1.3 1.4 1.5 2.1 2.2 2.3 2.4 2.5 3.1 3.2 3.3 3.4 3.5 4.1 4.2 4.3 5.1 5.2 6.1"
NUMBER="21"

for COURSE in $CI101 $HASTINGS
do
 if [ -f $COURSE.award.txt ]
 then
  sed -e "s/COURSE/$COURSE/"                         \
      -e "s/NUMBER/$NUMBER/"                         \
      -e "s/EXERCISES/$EXERCISES/" < _tmp > _tmp1    
  echo "Week:" $WEEK "Day:" $DAY "Date:" $YEAR-$MONTH-$DAYNUM "Time:" $HOUR.$MIN > $COURSE.stats.txt
  cat $COURSE.award.txt | sed -e "/0000/d" | awk -f _tmp1 >> $COURSE.stats.txt
 fi
done


EXERCISES="1.1 2.1 3.1"
NUMBER="3"

for COURSE in $CI101ee $HASTINGSee
do
 if [ -f $COURSE.award.txt ]
 then
  sed -e "s/COURSE/$COURSE/"                         \
      -e "s/NUMBER/$NUMBER/"                         \
      -e "s/EXERCISES/$EXERCISES/" < _tmp > _tmp1 
  echo "Week:" $WEEK "Day:" $DAY "Date:" $YEAR-$MONTH-$DAYNUM "Time:" $HOUR.$MIN > $COURSE.stats.txt
  cat $COURSE.award.txt | grep -v "0000" | awk -f _tmp1 >> $COURSE.stats.txt
 fi
done


EXERCISES="1.1 1.2 1.3 1.4 1.5"
NUMBER="5"

for COURSE in $CI228
do
 if [ -f $COURSE.award.txt ]
 then
  sed -e "s/COURSE/$COURSE/"                         \
      -e "s/NUMBER/$NUMBER/"                         \
      -e "s/EXERCISES/$EXERCISES/" < _tmp > _tmp1 
  echo "Week:" $WEEK "Day:" $DAY "Date:" $YEAR-$MONTH-$DAYNUM "Time:" $HOUR.$MIN > $COURSE.stats.txt
  cat $COURSE.award.txt | grep -v "0000" | awk -f _tmp1 >> $COURSE.stats.txt
 fi
done

EXERCISES="1.1 1.2 1.3 2.1"
NUMBER="4"

for COURSE in $CI283
do
 if [ -f $COURSE.award.txt ]
 then
  sed -e "s/COURSE/$COURSE/"                         \
      -e "s/NUMBER/$NUMBER/"                         \
      -e "s/EXERCISES/$EXERCISES/" < _tmp > _tmp1 
  echo "Week:" $WEEK "Day:" $DAY "Date:" $YEAR-$MONTH-$DAYNUM "Time:" $HOUR.$MIN > $COURSE.stats.txt
  cat $COURSE.award.txt | grep -v "0000" | awk -f _tmp1 >> $COURSE.stats.txt
 fi
done

EXERCISES="1.1 1.2 1.3 1.4 1.5 2.1 2.2 2.3 2.4 2.5 3.1 3.2 3.3 3.4 3.5 4.1 4.2 4.3 5.1 5.2 6.1"
EXERCISES="1.1 1.2 1.3 1.4 about"
NUMBER="5"

for COURSE in $TEST
do
 if [ -f $COURSE.award.txt ]
 then
  sed -e "s/COURSE/$COURSE/"                         \
      -e "s/NUMBER/$NUMBER/"                         \
      -e "s/EXERCISES/$EXERCISES/" < _tmp > _tmp1 
  echo "Week:" $WEEK "Day:" $DAY "Date:" $YEAR-$MONTH-$DAYNUM "Time:" $HOUR.$MIN > $COURSE.stats.txt
  cat $COURSE.award.txt | grep -v "0000" | awk -f _tmp1 >> $COURSE.stats.txt
 fi
done


rm -f _tmp _tmp1

popd >& /dev/null

##################################################
# Tell other about results                       #
##################################################

cd /opt/charon/system/results

dateStamp " Copy to selected users statistics"

function tellOthers
{
  WHO=$1
  if  [ -e /home/$WHO ]
  then
    SUBDIR=$2
    NAME=$3
    for F in 00HowToRead.txt $NAME.award.txt $NAME.completed.award.txt \
             $NAME.completed.SN.txt $NAME.completed.txt $NAME.sorted.txt $NAME.stats.txt
    do
      safeCopy $F
    done
  fi
}


function safeCopyX
{
  # Prevent evil
  FILEcp=$1
  START=/home/$WHO
  rm -f START/$SUBDIR/$FILEcp
  if [ -e $START/$FILEcp ]
  then
    echo "Not copied file exists " $START/$FILEcp
  else
    cp $FILEcp $START/$SUBDIR/$FILEcp
    chown $WHO:$WHO $START/$SUBDIR/$FILEcp
  fi
}


function safeCopy
{
  # Prevent evil
  FILEcp=$1
  # echo "SafeCopy " $WHO $SUBDIR $NAME $FILEcp
  START=/home/$WHO
  TARGET=$START/$SUBDIR/$FILEcp
  if [ -h $TARGET ]
  then
    echo "File $TARGET is a symbolic link"
  else
    if [ -e $TARGET ]
    then 
      if [ "$(stat -c %h -- "$TARGET")" -gt 1 ]
      then
       echo "File $TARGET is a hard link"
       return
      fi
    fi
    rm -f $TARGET
    if [ -e $FILEcp ]
    then
      cp $FILEcp $TARGET
      chown $WHO:$WHO $TARGET
    fi
  fi
}

##################################################
# Now do the telling od staff users of charon    #
#  of course they must have a user id on Charon  #
##################################################

tellOthers mas   stats   ci101
tellOthers mas   stats   ci101-resit
tellOthers mas   stats   ci101.ee
tellOthers mas   stats   ci228
tellOthers mas   stats   ci228-resit
tellOthers mas   stats   ci283
tellOthers mas   stats   ci283-resit
tellOthers mas   stats   test

# tellOthers mas   stats   ci101.dr
# tellOthers mas   stats   ci228.dr
# tellOthers mas   stats   hastings
# tellOthers mas   stats   hastings.ee

# tellOthers ajt15 ""      hastings
# tellOthers ajt15 ""      hastings.ee

tellOthers gs3 "" ci101
tellOthers gs3 "" ci101.ee
tellOthers gs3 "" ci283
tellOthers gs3 "" ci283-resit

tellOthers ab1184 "" ci101
tellOthers ab1184 "" ci101.ee

tellOthers rs602 "" ci101
tellOthers rs602 "" ci101-resit
tellOthers rs602 "" ci101.ee

tellOthers maj27 "" ci101
tellOthers maj27 "" ci101.ee

tellOthers jb    ""  ci283
tellOthers jb    ""  ci283-resit

# tellOthers jkck10    ""      hastings
# tellOthers jkck10    ""      hastings.ee
# tellOthers jkck10    ""      ci101.dr

##################################################
# Now backup important files                     #
##################################################

dateStamp " Create backup of files"

cd /opt/charon

export LIST="students templates jcl scripts log.txt *.sh"
export LIST=$LIST" /home/charon/log.txt"
export LIST=$LIST" system/results/*.sh"
export LIST=$LIST" system/results/*.txt"
export LIST=$LIST" system/source/*/*.java "
export LIST=$LIST" system/source/*.cpp system/source/Makefile"

echo $LIST

rm -f ~charon/backup/backup_$DAY_NAME.$ARC

if [ $COMPRESS = 7z ]
then
   tar cf - $LIST |
       7z a -t7z -mx9 -mfb=256 -si ~charon/backup/backup_$DAY_NAME.$ARC 
elif [ $COMPRESS = bz2 ]
then
  tar cvfj                         ~charon/backup/backup_$DAY_NAME.$ARC $LIST
else
  tar cvfj                         ~charon/backup/backup_$DAY_NAME.$ARC $LIST
fi

cd ~charon/backup
export WEEK_NO=$YEAR-WEEK-$WEEK

##################################################
# Make sure all backup locations exist           #
##################################################

function makeDirIfDoesNotExist
{
  ROOT=$1
  DIR=$2
  if [ ! -d $ROOT/$DIR ]
  then     mkdir $ROOT$DIR
    chmod 755 $ROOT$DIR
  fi
}

makeDirIfDoesNotExist   /var/www/html/ .facts
makeDirIfDoesNotExist   /var/www/html/.facts/ xyzzy
makeDirIfDoesNotExist   /var/www/html/.facts/ stats

##################################################
# Now send to remote machine                     #
#  MUST be able to authenticate user mas on      #
#  remote  machine using scp                     #
# Of course could be any user, but will need to  #
#  change scrip below                            #
#  lines sudo -u mas scp ...                     #
#  in function send                              #
##################################################

dateStamp "Copy backup to /home/charon/backup local machime"

function makeCharon()
{
  chown charon:charon    $1
  chmod 644              $1
}


function makeMas()
{
  chown mas:mas          $1
  chmod 644              $1
}

cp backup_$DAY_NAME.$ARC backup_$WEEK_NO.$ARC
cp backup_$DAY_NAME.$ARC today_$HOUR.$MIN.$ARC
cp backup_$DAY_NAME.$ARC today_$HOUR.$MIN.$ARC
cp backup_$DAY_NAME.$ARC day_$DAYNUM.$ARC

##################################################
# Check if require backup                        #
#  Will not need for testing                     #
# Backup will backup to many places              #
#  Some of which must allow an auto login        #
#  setup with                                    #
##################################################

if [ "X"$BACKUP = "X"no ]
then
  exit
  Message "Exit as BACKUP not set to yes";
fi


makeCharon  backup_$DAY_NAME.$ARC
makeCharon  backup_$WEEK_NO.$ARC
makeCharon  today_$HOUR.$MIN.$ARC
makeCharon  day_$DAYNUM.$ARC

ls -l today_$HOUR.$MIN.$ARC

dateStamp "Copy backup to remote machines"

##################################################
# You can never have too many backup's           #
#  Old computer saying                           #
##################################################

function send
{
  # Send to a remote m/c
  dateStamp "Backup to " $1
  sudo -u mas scp backup_$DAY_NAME.$ARC 	$1
  sudo -u mas scp backup_$WEEK_NO.$ARC 		$1
  sudo -u mas scp today_$HOUR.$MIN.$ARC 	$1
  sudo -u mas scp day_$DAYNUM.$ARC 	        $1
}
  
function sendL
{
  # send to other places on charon and mounted file systems
  LAST_RESORT=/var/www/html/.facts/xyzzy/
  if [ -d $LAST_RESORT ]
  then
    dateStamp "Backup to: Charon web server"
    cp backup_$WEEK_NO.$ARC	$LAST_RESORT
    #makeMas                    $LAST_RESORT/backup_$WEEK_NO.$ARC
  fi

  LAST_RESORT=/mnt/web/old/xyzzy/
  if [ -d $LAST_RESORT ]
  then
    dateStamp  "Backup to: CEM web server"
    cp backup_$WEEK_NO.$ARC	$LAST_RESORT
    #makeMas                    $LAST_RESORT/backup_$WEEK_NO.$ARC
  fi

  LAST_RESORT=/mnt/staff/backupCharon/
  if [ -d $LAST_RESORT ]
  then
    dateStamp "Backup to: mas's h: drive"
    cp backup_$WEEK_NO.$ARC	$LAST_RESORT
    cp today_$HOUR.$MIN.$ARC 	$LAST_RESORT
    cp backup_$DAY_NAME.$ARC	$LAST_RESORT
    cp day_$DAYNUM.$ARC	        $LAST_RESORT
  fi
}
  
function sendM
{
   # Another place on Charon
   dateStamp "Stats to web"
   STATS=stats.7z
   LAST_RESORT=/var/www/html/.facts/stats/
   cd /opt/charon/system/results
   rm -f $STATS
   tar cf - ci101* 00HowToRead.txt | 7z a -t7z -mx9 -mfb=256 -si -pxyzzy $STATS
   cp $STATS     $LAST_RESORT/$STATS
   chown charon:charon $LAST_RESORT/$STATS
   rm -f $STATS
}

function copy
{
  # Another mounted file system
  cp backup_$DAY_NAME.$ARC 	/mnt/staff/$1
  cp backup_$WEEK_NO.$ARC	/mnt/staff/$1
  cp today_$HOUR.$MIN.$ARC 	/mnt/staff/$1
}

# wakeonlan -i 193.62.183.141 00:1C:C0:2E:84:13
# sleep 10

sendL   
sendM 

##################################################
# do not externally backup if not charon         #
##################################################

IP=`hostname -i`
IP=`ifconfig | grep "inet " |  grep -v '127.0.0.1' | awk '{ print $2 }' `

if [ ! X$IP = X"193.62.183.45" ]
then
  #####################################################
  # Not on IP address 193.62.183.45 so not Charon     #
  #####################################################
  Message "Exit as machine not 193.62.183.45 so not Charon" \
          "No backup to external machines performed";
  exit
fi

##################################################
# External backup of files                       #
##################################################


send	mas@193.62.188.113:charon 
send	mas@cem-staff.brighton.ac.uk:charon 
# send	mas@193.62.183.219:charon

dateStamp " Finished backup"

setTime;
echo "---------------------------------------------------"
echo "When: " $WEEK_NO  $DAY $YEAR/$MONTH/$DAYNUM  $HOUR.$MIN.$SECOND
echo "---------------------------------------------------"

sleep 10

#---------------------------------------------------------------------
#  7z a -t7z -mx9 -mfb=256 ~charon/backup/backup_$DAY_NAME.$ARC $LIST
#----------------------------------------------------------------------
