# User Manual for vArmyKnife 

> v3.3.149   \
> Compiled Thu Feb 20 10:42:15 EST 2025

TODO: write more!

The creator and maintainer of this package can be contacted at stephen.hartley (at) nih.gov

## QUICK START:

The pipeline is already installed on HELIX and CCAD.

The following script will load all recommended utility modules on either CCAD or HELIX/BIOWULF:

    #on CCAD:
    source <( /mnt/nfs/gigantor/ifs/Shared/hartleys/software/shareMisc/getCurrentLocation -m -s -d )
    
    #on HELIX:
    source <( /data/hartleys/pub/software/shareMisc/getCurrentLocation -m -s -d )

We also recommend using a few additional java options, which prevent certain (rare) java errors:

    export _JAVA_OPTIONS="-Xms1g -XX:ParallelGCThreads=1 -Xss512m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled"
    export MALLOC_ARENA_MAX=1

## Loading the pipeline manually:

The pipeline is already installed on HELIX and CCAD.

This tool is loaded using modules. If for some bizarre reason module isn't active on your console, 
you may need to use the following command before doing anything:

    source /etc/profile.d/modules.sh

You will also need to load java version 1.8.0_50 or higher. Fortunetely CCAD, Helix, and
TREK all have java preinstalled:

    #On CCAD:
    module load jdk/1.8.0_111
    #On Helix:
    module load java/1.8.0_92
    #On TREK:
    module load lang/Java/1.8.0_60

Then you can load vArmyKnife itself using the commands:

    #On CCAD:
    module use /mnt/nfs/gigantor/ifs/Shared/hartleys/modules 
    #On Helix, use:
    module use /data/hartleys/pub/modules
    #Then:
    module load vArmyKnife

You can test the installation using the command:

    varmyknife help

You can also load and use the BETA version of vArmyKnife, which may have additional new features but has not 
finished undergoing testing. The beta version is more likely to contain undiscovered bugs. The beta version
can be found here:

    #On CCAD:
    module use /mnt/nfs/gigantor/ifs/Shared/hartleys/modulesBeta
    module load vArmyKnifeBeta
    
    #On Helix:
    module use /data/hartleys/pub/modulesBeta/
    module load vArmyKnifeBeta

## RECOMMENDED ENVIRONMENT AND JAVA OPTIONS:

It is recommended that you also use the following environment variables whenever you 
use vArmyKnife. On some of the clusters I use this seems to prevent certain rare errors.

    export _JAVA_OPTIONS="-Xms1g -XX:ParallelGCThreads=1 -Xss512m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled"
    export MALLOC_ARENA_MAX=1

Also, you're usually going to want to set the maximum memory. How much memory is required 
will depend on the type of job. The more large annotation files that need to be loaded,
the more memory will be required. Note however that if you restrict to only one chromosome,
most of the annotation files will only be loaded for that chromosome, greatly reducing the
memory footprint.

You can set the memory usage by adding "-Xmx4g" just after "varmyknife" when you invoke vArmyKnife,
changing "4g" to however many gigabytes you think you will need. Note that if java starts to 
run low on memory it will slow down considerably as it tries to conserve. It's usually best 
to allocate some extra memory as needed.

