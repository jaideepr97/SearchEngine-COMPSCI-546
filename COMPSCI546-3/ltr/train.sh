RANKLIB_JAR='ltr/ranklib-2.9.jar'

for i in `seq 1 10`; do
  fold=`printf %02d $i`
  java -jar ${RANKLIB_JAR} -train ltr/fold${fold}.train -test ltr/fold${fold}.test -missingZero -metric2t ndcg@5 -ranker 8 -frate 1.0 -bag 30 -srate 0.1 -leaf 8 "$@" | tee ltr/fold${fold}.out
done

echo "Done: "
# average in AWK:
tail -n1 ltr/fold*.out | grep 'test data' | awk 'BEGIN {x=0} {x += $5} END {print x/NR}'
