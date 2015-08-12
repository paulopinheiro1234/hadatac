#!/bin/bash

clear

IN=$1/in/ccsv/*.ccsv
PROC=$1/proc/
OUT=$1/out/

for f in $IN
do
  xbase=${f##*/}
  xfext=${xbase##*.}
  xpref=${xbase%.*}
  echo "Processing $f file..."
#  java -jar hadatac-loader.jar -i $f -m CCSV | split -a 3 -d -l 10000000 - $OUT$xbase.csv.
  java -jar hadatac-loader.jar -i $f -m CCSV
#  java -jar ccsv-loader.jar -pv -i $f
done
