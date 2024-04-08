SCALA_LIB=/Users/xak/Downloads/scala-2.11.8/lib/scala-library.jar
DEJAVU_LIB=/Users/xak/workspace/scala/TP-DejaVu/dir/tpdejavu.jar
DEJAVU_HOME=/Users/xak/workspace/scala/TP-DejaVu/

alias dejavu=$DEJAVU_HOME/dir/dejavu
dejavu -s=spec.qtl -p=spec.pqtl --jar --online
jruby -J-cp "$SCALA_LIB:$DEJAVU_LIB:." master.rb