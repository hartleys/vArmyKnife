#!/bin/bash

#+SHORTDESC=""
#+ HELPTEXT=""$'\n'
#++HELPTEXT="More Help Text"$'\n'
#+ SYNTAXTEXT=""$'\n'
#+ PARAMS="--help: displays syntax and help info."$'\n'
#++PARAMS="--man: synonym for --help."$'\n'
#++PARAMS=""$'\n'
#+ VERSION="0.1.0"

if [ "$#" -gt 0 ]; then
  VERSION="$1"
else
  VERSION="0"
fi
shift

if [ "$VERSION" == "." ]; then
  echo "Autodetecting version";
  MAJORMINOR=$( ls -v ~/modules/vArmyKnife/ | tail -n1  | tr '.' $'\t' | cut -f1-2 | tr $'\t' '.')
  LEASTVER=$(( $( ls -v ~/modules/vArmyKnife/ | tail -n1  | tr '.' $'\t' | cut -f3 ) + 1 ))
  VERSION="${MAJORMINOR}.${LEASTVER}"
fi

echo "VERSION=$VERSION"
echo "VERSION=$VERSION" > buildScripts/env.version.txt
echo "VERSION=$VERSION" > buildScripts/env.options.txt

sleep 5;


MAKEPUBLIC="0"
MAKERELEASE="0"
MAKERELEASE_HELIX="0"
MAKEBETA_HELIX="0"
UPLOAD_HELIX="0"
REBUILD="1"
MAKEDOCS="1"


while [ "$#" -gt 0 ]; do
   if [ "$1" == "--pub" ]; then
     echo "Making public."
     MAKEPUBLIC="1"
   fi
   if [ "$1" == "--rel" ]; then
     echo "Making release."
     MAKEPUBLIC="1"
     MAKERELEASE="1"
   fi
   if [ "$1" == "--noRebuild" ]; then
     echo "skipping rebuild."
     REBUILD="0"
   fi
   if [ "$1" == "--noDocs" ]; then
     echo "skipping documentation build."
     MAKEDOCS="0"
   fi
   if [ "$1" == "--helixRel" ]; then
     MAKERELEASE_HELIX="1"
     MAKEBETA_HELIX="1"
   fi
   if [ "$1" == "--helixBeta" ]; then
     MAKEBETA_HELIX="1"
   fi
   if [ "$1" == "--helix" ]; then
     UPLOAD_HELIX="1"
   fi
   shift;
done

echo "MAKEPUBLIC=\"$MAKEPUBLIC\"" >> buildScripts/env.options.txt
echo "MAKERELEASE=\"$MAKERELEASE\"" >> buildScripts/env.options.txt
echo "REBUILD=\"$REBUILD\"" >> buildScripts/env.options.txt
echo "MAKEDOCS=\"$MAKEDOCS\"" >> buildScripts/env.options.txt
echo "UPLOAD_HELIX=\"$UPLOAD_HELIX\"" >> buildScripts/env.options.txt
echo "MAKEBETA_HELIX=\"$MAKEBETA_HELIX\"" >> buildScripts/env.options.txt
echo "MAKERELEASE_HELIX=\"$MAKERELEASE_HELIX\"" >> buildScripts/env.options.txt

cat buildScripts/env.options.txt
