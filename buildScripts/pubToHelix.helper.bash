#!/bin/bash


cd /home/hartleys/modules-inst/vArmyKnife/

echo ".............................................................."

echo "Starting module generation..."

source env.helix.txt
source env.options.txt

cat env.helix.txt
cat env.options.txt

OUTDIR="${INSTDIR}/$VERSION"

echo "VERSION=$VERSION"

sed "s/REPLACEWITHPATH/${ESCAPED_INSTDIR}${VERSION}\//g" ${SOURCEDIR}/moduleTemplate.txt > ${MODULEDIR}/vArmyKnife/$VERSION

chmod a+x ${INSTDIR}/${VERSION}/bin/*

echo ".............................................................."

echo "generated modules. Test loading"
module use ${MODULEDIR}
module unload vArmyKnife
module load vArmyKnife/$VERSION
if [ "$?" -eq "0" ]; then
  echo "loaded successfully"
else
  echo "module load FAILED"
  exit 10
fi
echo "Loaded modules:"
module list

echo ".............................................................."

INITDIR=$(pwd);

if [ "$MAKEBETA_HELIX" == "1" ]; then

  echo "making public version..."
  OUTDIR2="${INSTDIR2}/$VERSION"
  echo "INSTDIR2=$INSTDIR2"
  echo "OUTDIR2=$OUTDIR2"
  echo "MODULEDIR2=$MODULEDIR2"
  
  sed "s/REPLACEWITHPATH/${ESCAPED_INSTDIR2}${VERSION}\//g" ${SOURCEDIR}/moduleTemplate.txt > ${MODULEDIR2}/$VERSION

echo '#%Module'"
set ModulesVersion $VERSION" > ${MODULEDIR2}/.version
  
  mkdir -p ${OUTDIR2}
  chmod 1755 ${OUTDIR2}
  cp -R ${OUTDIR}/md ${OUTDIR2}
  cp -R ${OUTDIR}/html ${OUTDIR2}
  cp -R ${OUTDIR}/bin ${OUTDIR2}
  cp $OUTDIR/src.tar.gz ${OUTDIR2}
  cp $OUTDIR/*.jar ${OUTDIR2}
  if [ ! -d "${OUTDIR2}" ]; then
    echo "FAILED to change directory 3"
    exit 3
  fi
  
  cd ${OUTDIR2}
  if [ "$?" -eq "0" ]; then
    #echo "dir change success (p1)"
    TESTCURRDIR=$(pwd)
    TESTCURRDIRPREFIX=$( pwd | grep -c "^/data/hartleys/pub/software/vArmyKnife/" )
    if [ "$TESTCURRDIRPREFIX" == "1" ]; then
      echo "dir change success (p2) to dir \"$OUTDIR2\""
      find . * -type d -exec chmod 1755 {} \;
      find . -type f -exec chmod 644 {} \;
      chmod a+x bin/*
    else
      echo "FAILED to change directory 2"
      exit 2
    fi
  else
    echo "FAILED to change directory 1"
    exit 1
  fi
  #find . * -type d -exec chmod 1755 {} \;
  #find . -type f -exec chmod 644 {} \;
  cd $INITDIR
fi

echo ".............................................................."


if [ "$MAKERELEASE_HELIX" == "1" ]; then
  echo "MODULEREL=$MODULEREL"

  cp ${MODULEDIR2}/$VERSION ${MODULEREL}/$VERSION

echo '#%Module'"
set ModulesVersion $VERSION" > ${MODULEREL}/.version

fi

echo ".............................................................."

#cd $INSTDIR2
#perl /home/hartleys/software/markdown/Markdown.pl annoJarHelp.md > nocss.annoJarHelp.html
#cat $INITDIR/buildScripts/stylesheets/template.simple.start.html ../html/nocss.$line.html $INITDIR/buildScripts/stylesheets/template.simple.end.html > ../html/index.html

cd $INITDIR

echo "...........................DONE_ON_HELIX......................"



