echo -n "Do you wish to use LDP authentication yes/no: "
read inputLine
echo "Previously"
ls -l login.sh
if [ X$inputLine = "Xno" ]
then
  rm -f login.sh
  ln  -s login_fake.sh login.sh
  ls -l login.sh
  echo Set to no password
elif [ X$inputLine = "Xyes" ]
then
  rm -f login.sh
  ln -s login_ldap5.sh login.sh 
  echo Set to require password
  ls -l login.sh
else
  echo Usage: setPassword
fi

