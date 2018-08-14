#!/bin/bash

#scp -r hartleys@helix.nih.gov:/home/hartleys/UTILS/ScrapTesting/src /home/hartleys/UTILS/ScrapTesting/src
source env.version.txt

#scp -r /DCEG/Projects/CCSS/steve/software-big/annoJar-beta/ hartleys@helix.nih.gov:/home/hartleys/UTILS/ScrapTesting/
#scp /home/hartleys/UTILS/ScrapTesting/env.version.txt  hartleys@helix.nih.gov:/home/hartleys/modules-inst/annoJar/

scp -r /DCEG/Projects/CCSS/steve/software-big/vArmyKnife/$VERSION  hartleys@helix.nih.gov:/home/hartleys/modules-inst/vArmyKnife/
scp /DCEG/Projects/CCSS/steve/software-big/vArmyKnife/env.*.txt  hartleys@helix.nih.gov:/home/hartleys/modules-inst/vArmyKnife/

cat pubToHelix.helper.bash | ssh hartleys@helix.nih.gov



