#!/bin/bash

#source /etc/profile.d/modules.sh
#module use /home/hartleys/modules
#module load java/1.8.0_112
#module load sbt

source env.ccad.txt

#+SHORTDESC=""
#+ HELPTEXT=""$'\n'
#++HELPTEXT="More Help Text"$'\n'
#+ SYNTAXTEXT=""$'\n'
#+ PARAMS="--help: displays syntax and help info."$'\n'
#++PARAMS="--man: synonym for --help."$'\n'
#++PARAMS=""$'\n'
#+ VERSION="0.1.0"

source setOptions.bash

cat env.options.txt


echo "echo \"VERSION=$VERSION\"" >> env.version.txt

#INSTDIR="/DCEG/Projects/CCSS/steve/software-big/annoJar-beta/"
#ESCAPED_INSTDIR="\/DCEG\/Projects\/CCSS\/steve\/software-big\/annoJar-beta\/"
#MODULEDIR="/home/hartleys/modules/"
#INSTDIR2="/DCEG/Projects/CCSS/steve/software-big/annoJar/"
#ESCAPED_INSTDIR2="\/DCEG\/Projects\/CCSS\/steve\/software-big\/annoJar\/"
#MODULEDIR2="/DCEG/Projects/CCSS/steve/modules/"
#MODULEREL="???"
#SCALAVER=2.12

OUTDIR="${INSTDIR}/$VERSION"
echo "OUTDIR=$OUTDIR"
echo "OUTDIR=$OUTDIR" >> env.options.txt


echo "------------------------------------------------------------------"

INITDIR=$(pwd -P);
echo "INITDIR=$INITDIR"
echo "INITDIR=$INITDIR" >> env.options.txt

SHAREMISCDIR=/mnt/nfs/gigantor/ifs/Shared/hartleys/software/
SHAREMISCNAME=shareMisc/

if [ "$REBUILD" == "1" ]; then
   #export _JAVA_OPTIONS="-XX:ParallelGCThreads=1 -Xmx4g -Xms1g"
   #export MALLOC_ARENA_MAX=1
   #export SBT_OPTS="-Xmx1536M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Xss2M  -Duser.timezone=GMT"
   
   export FULLDATE=$(date)
   export FULLDATESEC=$(date --date="$FULLDATE" +%s)
   export DATE=$(date +"%Y-%m-%d" --date="$FULLDATE")
   export DATETEXT=$(date +"%B %d, %Y" --date="$FULLDATE")
   
   echo "FULLDATE=$FULLDATE";
   echo "FULLDATESEC=$FULLDATESEC";
   
   echo "VERSION=\"$VERSION\"" > env.build.txt
   echo "FULLDATE=\"$FULLDATE\"" >> env.build.txt
   echo "FULLDATESEC=\"$FULLDATESEC\"" >> env.build.txt
   echo "DATE=\"$DATE\"" >> env.build.txt
   echo "DATETEXT=\"$DATETEXT\"" >> env.build.txt
   cat env.build.txt >> env.options.txt
   
   if [ "$VERSION" != "0" ]
   then
     echo "Setting version $VERSION"
     QORTS_VERS_LINE="  val UTIL_VERSION = \"$VERSION\"; \\/\\/ REPLACE_THIS_QORTS_VERSION_VARIABLE_WITH_VERSION_NUMBER          (note this exact text is used in a search-and-replace. Do not change it.)"
     sed -i -e "s/^.*REPLACE_THIS_QORTS_VERSION_VARIABLE_WITH_VERSION_NUMBER.*/$QORTS_VERS_LINE/g" ./src/main/scala/runner/runner.scala
     echo $VERSION > ver.txt;
   else
     echo "No version set."
   fi
   
   QORTS_DATE_LINE="  val UTIL_COMPILE_DATE = \"$FULLDATE\"; \\/\\/ REPLACE_THIS_QORTS_DATE_VARIABLE_WITH_DATE          (note this exact text is used in a search-and-replace. Do not change it.)"
   QORTS_TIME_LINE="  val UTIL_COMPILE_TIME : Long = $FULLDATESEC; \\/\\/ REPLACE_THIS_QORTS_DATE_VARIABLE_WITH_TIME          (note this exact text is used in a search-and-replace. Do not change it.)"
   sed -i -e "s/^.*REPLACE_THIS_QORTS_DATE_VARIABLE_WITH_DATE.*/$QORTS_DATE_LINE/g" src/main/scala/runner/runner.scala
   sed -i -e "s/^.*REPLACE_THIS_QORTS_DATE_VARIABLE_WITH_TIME.*/$QORTS_TIME_LINE/g" src/main/scala/runner/runner.scala
   
   
   
   echo "which sbt: $( which sbt )"
   
   sbt < sbtAssemblyCommand.txt 2>&1 | egrep -v "Merging '(scala|chemcomp|org/apache|org/biojava|org/biojava3|org/snpeff|org/broad|META-INF)/"
   #sbt < sbtAssemblyCommand.txt > /dev/null 2>&1
   
   tar -zcvf src.tar.gz src/ > /dev/null
 
   mkdir -p $OUTDIR;
   cp env.build.txt $OUTDIR
   cp env.build.txt $INSTDIR
   cp src.tar.gz $OUTDIR
   cp target/scala-${SCALAVER}/vArmyKnife.jar ${OUTDIR};
   echo "copied jar and src."

   cd $SHAREMISCDIR
   tar -zcvf $INITDIR/shareMisc.tar.gz $SHAREMISCNAME > /dev/null
   cp $INITDIR/shareMisc.tar.gz $OUTDIR
   cd ${INITDIR}

else
   echo "SKIPPING COMPILE"
fi

echo "------------------------------------------------------------------"

cp env.*.txt $OUTDIR
cp env.*.txt $INSTDIR

sed "s/REPLACEWITHPATH/${ESCAPED_INSTDIR}${VERSION}\//g" ${SOURCEDIR}/moduleTemplate.txt > ${MODULEDIR}/vArmyKnife/$VERSION

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

echo "------------------------------------------------------------------"


mkdir -p ${OUTDIR}/md
mkdir -p ${OUTDIR}/docs
mkdir -p ${OUTDIR}/bin

cp varmyknife ${OUTDIR}/bin

chmod a+x ${OUTDIR}/bin/*

if [ "$MAKEDOCS" == "1" ]; then
  cd $OUTDIR
  cp -R $INITDIR/buildScripts/docs/stylesheets $OUTDIR/docs/stylesheets
  echo "creating MD files"
  cd md
  echo "pwd=$(pwd)"
  java -Xmx1G -jar $VARMYKNIFE_JAR --help generateMarkdownPages
  if [ "$?" -eq "0" ]; then
    echo "docs written successfully"
  else
    echo "documentation build FAILED"
    exit 10
  fi
  
  echo "done with MD files"
  
  echo "creating html..."
  ls *.md | sed -e 's/\.md//' > temp.txt
  
  while read line
  do
     perl /home/hartleys/software/markdown/Markdown.pl $line.md > ../docs/nocss.$line.html
     cat $INITDIR/buildScripts/docs/stylesheets/template.simple.start.html ../docs/nocss.$line.html $INITDIR/buildScripts/docs/stylesheets/template.simple.end.html > ../docs/$line.html
     rm ../docs/nocss.$line.html
  done < temp.txt
  rm temp.txt

  echo "done with html docs"
  
  cd $OUTDIR
  cat ${INITDIR}/helpDocs/index.md | sed "s/CURRENT_PIPELINE_VERSION_NUMBER/$VERSION/g" | sed "s/CURRENT_PIPELINE_VERSION_FULLDATE/$FULLDATE/g" > index.md
  cat ${INITDIR}/helpDocs/faq.md | sed "s/CURRENT_PIPELINE_VERSION_NUMBER/$VERSION/g" | sed "s/CURRENT_PIPELINE_VERSION_FULLDATE/$FULLDATE/g" > faq.md
  cat ${INITDIR}/helpDocs/pipelineUserManual.md | sed "s/CURRENT_PIPELINE_VERSION_NUMBER/$VERSION/g" | sed "s/CURRENT_PIPELINE_VERSION_FULLDATE/$FULLDATE/g" > manual.md

  for line in index faq manual
  do
     perl /home/hartleys/software/markdown/Markdown.pl $line.md > nocss.$line.html
     cat $INITDIR/buildScripts/docs/stylesheets/template.start.html nocss.$line.html $INITDIR/buildScripts/docs/stylesheets/template.end.html > $line.html
     rm nocss.$line.html
  done
  
  echo "done with html"
  
  cp $INITDIR/buildScripts/stylesheets ./
  cp $INITDIR/buildScripts/docStylesheets ./
  
  cd $INITDIR
else
   echo "SKIPPING DOC BUILD"
fi

echo "------------------------------------------------------------------"

cd $INITDIR

##########################
#Loading public location:

if [ "$MAKEPUBLIC" == "1" ]; then
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
  cp -R ${OUTDIR}/docs ${OUTDIR2}
  cp -R ${OUTDIR}/stylesheets ${OUTDIR2}
  cp -R ${OUTDIR}/docStylesheets ${OUTDIR2}
  cp -R ${OUTDIR}/bin ${OUTDIR2}
  rm $OUTDIR2/bin/vArmyKnife.jar

  cp $OUTDIR/src.tar.gz ${OUTDIR2}
  cp $OUTDIR/*.jar ${OUTDIR2}
  
  if [ ! -d "${OUTDIR2}" ]; then
    exit 3
  fi
  cp $INITDIR/shareMisc.tar.gz $OUTDIR2

  cd ${OUTDIR2}

  if [ "$?" -eq "0" ]; then
    #echo "dir change success (p1)"
    TESTCURRDIR=$(pwd)
    TESTCURRDIRPREFIX=$( pwd | grep -c "^/mnt/nfs/gigantor/ifs/Shared/hartleys/software/vArmyKnife/" )
    if [ "$TESTCURRDIRPREFIX" == "1" ]; then
      echo "dir change success (p2)"
      find . * -type d -exec chmod 1755 {} \;
      find . -type f -exec chmod 644 {} \;
      chmod 755 bin/*
    else
      echo "ERROR: failed to change directory"
      exit 2
    fi
    cd ../

  else
    echo "ERROR: failed to change directory"
    exit 1
  fi
  


echo '#%Module'"
set ModulesVersion $VERSION" > ${MODULEDIR2}/.version

  #find . * -type d -exec chmod 1755 {} \;
  #find . -type f -exec chmod 644 {} \;
  cd $INITDIR
fi


echo "------------------------------------------------------------------"


if [ "$MAKERELEASE" == "1" ]; then
  echo "Making release..."
  cp ${MODULEDIR2}/$VERSION ${MODULEREL}/$VERSION
echo '#%Module'"
set ModulesVersion $VERSION" > ${MODULEREL}/.version

  cp -R ${OUTDIR}/html/* $HELPDIR

fi

echo "------------------------------------------------------------------"

if [ "$UPLOAD_HELIX" == "1" ]; then
  echo "Uploading to HELIX..."
  source pubToHelix.bash
fi

echo "---------------------------DONE-----------------------------------"
echo "$VERSION "
echo "module unload vArmyKnife "
echo "module unload vArmyKnifeBeta "
echo "module load vArmyKnife/$VERSION "

echo ""
