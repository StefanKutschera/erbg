#!/bin/sh
if [ $# -eq 0 ]
  then
    echo "\tusage: executeable.sh <filename>\n" 
    echo "Just copies the input file to ~/install/erbg_java_standalone"
    return
fi
cp $1 ~/install/erbg_java_standalone
