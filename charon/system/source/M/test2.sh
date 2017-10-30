javac Debug.java Main2.java Message.java MessageType.java
java Main > log
sort -u < log > log2
wc log*

javac s10_histogram_02.java
sed -e "s/^...//" < log > log3

java Main < log3

# rm log log2 log3

