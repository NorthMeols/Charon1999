cd /opt/charon

if [ -e playpen/proc/version ]; then
  umount playpen/proc
fi

if [ -e playpen/proc/version ]; then
  echo "proc filesystem still mounted"
  exit
fi

cd /opt/charon

rm -f -r log log.txt
rm -f -r playpen/sandbox/*
rm -f -r playpen/*
rm -f -r bin/*
rm -f -r SOFTWARE_INSTALLED
rm -f -r /opt/charon/system/results/ci*.txt /opt/charon/system/results/test*.txt
rm -f -r profile

find . -name "completed" -exec rm -f -r {} ";"

cd system/source
make clean
cd ../..
