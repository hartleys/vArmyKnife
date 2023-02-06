
> v3.3.43   \
> Compiled Tue Jan 24 11:11:49 EST 2023

# INTRODUCTION:

vArmyKnife is...

# INSTALLATION AND SETUP:

[You can download the most recent stable version of vArmyKnife here](https://github.com/hartleys/vArmyKnife/releases),
or you can use the [most recent experimental build here](https://github.com/hartleys/vArmyKnife/tarball/master).

Simply download the vArmyKnife.tar.gz file and extract it to your preferred location.

    tar xvzf varmyknife.tar.gz /my/install/directory/

If you are running either Linux or OSX, you can install the software onto your PATH using the command:

    export PATH=/my/install/directory/:$PATH

Then you can test vArmyKnife and see the basic syntax using either of the commands:

    varmyknife help
       or
    java -jar /my/install/directory/vArmyKnife.jar help

# RECOMMENDED ENVIRONMENT AND JAVA OPTIONS:

I have found that the following environment variables seem to improve stability and performance
when using vArmyKnife on a cluster or HPC-like environment.

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
to allocate some extra memory as needed. So for example, to set the max memory to 4 gigabytes:

    varmyknife -Xmx4g walkVcf inputVcf.vcf.gz outputVcf.vcf.gz

# How to use vArmyKnife:

General command documentation can be found [*here*](https://hartleys.github.io/vArmyKnife/).

## LEGAL:

Written 2017 by Stephen Hartley, PhD

National Cancer Institute (NCI), Division of Cancer Epidemiology and Genetics (DCEG), Human Genetics Program

vArmyKnife and all relevant documentation is "United States Government Work" under he terms of the United States Copyright Act. It was written as part of the authors' official duties for the United States Government and thus vArmyKnife cannot be copyrighted. This software is freely available to the public for use without a copyright notice. Restrictions cannot be placed on its present or future use.

Although all reasonable efforts have been taken to ensure the accuracy and reliability of the software and data, the National Human Genome Research Institute (NHGRI), the National Cancer Institute (NCI) and the U.S. Government does not and cannot warrant the performance or results that may be obtained by using this software or data. NHGRI, NCI and the U.S. Government disclaims all warranties as to performance, merchantability or fitness for any particular purpose.

In any work or product derived from this material, proper attribution of the authors as the source of the software or data should be made, using "NCI Division of Cancer Epidemiology and Genetics, Human Genetics Program" as the citation.

This package uses (but is not derived from) several externally-developed, open-source libraries which have been distributed under various open-source licenses. vArmyKnife is distributed packaged with these libraries included.

Additional License information can be accessed using the command:

    vArmyKnife help LICENSES

And can also found in the distributed source code in:

    src/main/resources/

