find . -name "completed" -print
echo -n "THINK Do you realy want to do this yes/no: "
read inputLine
if [ "X"$inputLine = "Xyes" ]
then
  find . -name "completed" -exec rm -f {} ";"
fi
