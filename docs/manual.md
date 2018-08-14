# User Manual for vArmyKnife 

> v2.1.83   \
> Compiled 

## QUICK START:

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

## RECOMMENDED ENVIRONMENT AND JAVA OPTIONS:

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

## COMPLETE DOCUMENTATION:

General command documentation can be found [*here*](docs/index.html).

Options and variables for the walkVcf command (which is the command that you will almost always use for most purposes)
can be found [*here*](walkVcf.html)

## INPUT COMMANDS AND OPTIONS:

All vArmyKnife executions follow the same basic syntax:

    varmyknife commandName parameters

For example:

    varmyknife walkVcf testInput.vcf.gz testOutput.vcf.gz

All the primary functions of vArmyKnife have been folded into the multiStepPipeline command,
so you're almost always going to use that command. The above invocation is the simplest
possible use of this tool. It simply reads in a VCF file (which can be gzipped, or not) and
copies it to a new place (which can also be gzipped, or not).

You can add java options prior to the "command" part, and vArmyKnife options after the command. 
So for example:

    varmyknife -Xmx5g multiStepPipeline --verbose testInput.vcf.gz testOutput.vcf.gz

Will increase the allowed memory to 5 gigabytes and turn on verbose reporting (making it tell you
more progress and debugging information).

In addition to the mandatory inputVcf and outputVcf parameters, multiStepPipeline
can take a large number of additional optional parameters that add functionality to the
run. These options should be placed AFTER the multiStepPipeline command but before the mandatory parameters. 
For example, if you simply want to take an input VCF and split all multiallelics:

    varmyknife -Xmx5g multiStepPipeline --splitMultiAllelics testInput.vcf.gz testOutput.vcf.gz

You can get a full list of all parameters using the command:

    varmyknife multiStepPipeline --help

## VCF Walkers:

The basic structure of the software was inspired by GATK. 
Different commands execute different "VCF walkers". 

A "walker" is just a modular sub-program that 
takes a VCF, does something to it, and passes it on. 

Unlike with GATK, however, my tool can "chain"
multiple walkers in sequence, allowing a large number of steps to be merged into one big step. 
This is very useful, because at least on CCAD and HELIX, the process of writing a VCF to disk and then 
reading it out again is very time consuming. In addition to reducing the number of steps required, 
using chained walkers greatly reduces the total runtime.

When using the "vArmyKnife multiStepPipeline" command, VCF walkers are added depending on 
what optional parameters you add. If all you include is the input and output files (which are required), 
then all it does is read out the VCF and then copy it over:

    varmyknife multiStepPipeline inputfile.vcf.gz outputfile.vcf.gz

But, if you add a GTF annotation file and a genome fasta file:

    varmyknife walkVcf --gtfFile myGtfFile.gtf.gz \
                       --genomeFA hg19.fa.gz \
                         testInput.vcf.gz \
                         testOutput.vcf.gz

Then it will add a "AddTxAnnoSVcfWalker" walker, which adds a wide array of transcript-related annotations, like 
transcript lists, mRNA changes, amino acid changes, variant type (LOF/splice-loss/etc), and so on.

If you also add a file that lists transcript names and the associated gene symbol, 
it will also include a gene list for each variant:

    varmyknife walkVcf --txToGene txToGene.txt \
                       --gtfFile myGtfFile.gtf.gz \
                       --genomeFA hg19.fa.gz \
                         testInput.vcf.gz \
                         testOutput.vcf.gz

If you also add a file that lists the canonical transcript for each gene, then it will add another walker
that generates similar annotation fields restricted to only canonical transcripts:

    varmyknife walkVcf --txToGene txToGene.txt \
                       --gtfFile myGtfFile.gtf.gz \
                       --genomeFA hg19.fa.gz \
                       --addCanonicalTags canonList.txt \
                         testInput.vcf.gz \
                         testOutput.vcf.gz

If you add the parameter --splitMultiAllelics, then it will also split any multiallelic variants:

    varmyknife walkVcf --txToGene txToGene.txt \
                       --gtfFile myGtfFile.gtf.gz \
                       --genomeFA hg19.fa.gz \
                       --addCanonicalTags canonList.txt \
                       --splitMultiAllelics \
                         testInput.vcf.gz \
                         testOutput.vcf.gz

If you also add the "--groupFile" parameter, specifying a file that lists all the samples along with
their sample group(s), then you will get various counts for each sample group:

    varmyknife walkVcf --txToGene txToGene.txt \
                       --gtfFile myGtfFile.gtf.gz \
                       --genomeFA hg19.fa.gz \
                       --addCanonicalTags canonList.txt \
                       --splitMultiAllelics \
                       --groupFile testGroups.txt \
                         testInput.vcf.gz \
                         testOutput.vcf.gz

Sometimes with all these parameters, the commands can get really long and complex. To help alleviate 
this, you can specify a paramFile:

    varmyknife walkVcf --paramFile testParam.part001.txt \
                                testInput.vcf.gz \
                                testOutput.vcf.gz

With the paramFile "testParam.part001.txt" that looks like this:

    --txToGene  txToGene.txt 
    --groupFile testGroups.txt 
    --inputSavedTxFile txdata.txt.gz
    --addCanonicalTags canonList.txt
    --splitMultiAllelics

## Param files: advanced

The paramFile has a number of extra functions intended make it easier to use.
First of all, all leading whitespace on any line is automatically removed.
Secondly, any lines in a paramFile that (after trimming leading whitespace) begin with a "#", are ignored.
In addition, if lines end with backslashes, they are combined with the next line (with leading whitespace removed).

So, for example, this is a legal paramfile:

    # Tally groups:
    --groupFile            testGroups.txt 
    # Add transcript stuff:
        --txToGene         ucsc/txToGene.txt 
        --inputSavedTxFile ucsc/txdata.txt.gz
        --addCanonicalTags ucsc/canonList.txt
    #Extra stuff:
        --splitMultiAllelics
    #Add repeatmasker mask:
        --addBedTags SWH_locusIsNearRmsk_5:\
                     5:+:\
                     Variant locus is within 5bp of a repeatmasker masked region:\
                     rmsk.bed.gz,\

Finally, you can also set internal variables that will be expanded out whenever they are invoked.
Variables are created using the syntax "varname=varText", and are invoked using the syntax "${varname}".

So, for example, this is a legal paramfile:

    # Set to annotation directory:
    RESOURCE_DIR=/set/me/to/resource/dir/
    # Now, instead of having to change every variable's base directory, 
    #      you only need to change the line above.
    #      Note that advanced bash substitutions are NOT supported.
    # You can also set further variables that call previous variables:
    UCSCDATA_DIR=${RESOURCE_DIR}ucsc/
    # Tally groups:
    --groupFile            ${RESOURCE_DIR}testGroups.txt 
    # Add transcript stuff:
        --txToGene         ${UCSCDATA_DIR}txToGene.txt 
        --inputSavedTxFile ${UCSCDATA_DIR}txdata.txt.gz
        --addCanonicalTags ${UCSCDATA_DIR}canonList.txt
    #Extra stuff:
        --splitMultiAllelics
    #Add repeatmasker mask:
        --addBedTags SWH_locusIsNearRmsk_5:\
                     5:+:\
                     Variant locus is within 5bp of a repeatmasker masked region:\
                     ${UCSCDATA_DIR}rmsk.bed.gz

Note that these internal variables are ONLY used INSIDE the paramfile. Any invocations in the parameters
outside the paramfile will NOT be substituted out (or might even get substituted out by the command line itself,
if you're using bash).

## MERGING MULTIPLE CALLER VCFS:

TODO: write more!

## OUTPUT DOCUMENTATION:

TODO: write more!

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
