# User Manual for vArmyKnife 

> v2.2.305   \
> Compiled Mon Dec  2 11:32:33 EST 2019

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

    varmyknife -Xmx5g walkVcf --verbose testInput.vcf.gz testOutput.vcf.gz

Will increase the allowed memory to 5 gigabytes and turn on verbose reporting (making it tell you
more progress and debugging information).

In addition to the mandatory inputVcf and outputVcf parameters, multiStepPipeline
can take a large number of additional optional parameters that add functionality to the
run. These options should be placed AFTER the multiStepPipeline command but before the mandatory parameters. 
For example, if you simply want to take an input VCF and split all multiallelics:

    varmyknife -Xmx5g walkVcf --splitMultiAllelics testInput.vcf.gz testOutput.vcf.gz

You can get a full list of all parameters using the command:

    varmyknife walkVcf --help

## WHAT CAN VARMYKNIFE DO

vArmyKnife can do many different things. It is primarily designed to read and process [*VCF files*]().

The vast majority of functions can be carried out by the walkVcf command. This command uses the syntax:

    varmyknife walkVcf [options] input.vcf.gz output.vcf.gz

Optionally, the input or output vcfs can be replaced with "-" and the tool will use stdin and/or stdout. This can be used to read
VCF output from other tools without generating intermediate files, or to feed the output directly into other tools, or to generate
block VCFs using the tabix bgzip utility. For example:

    varmyknife walkVcf [options] input.vcf.gz - | bgzip > output.vcf.gz

### Basic Processing and Reorganization

 * Dropping tags, fields, or samples.
 * Splitting Multiallelics: Many tools do not properly deal with multiallelic variants and so it is often useful to split up the multiallelics into separate VCF lines.     
 * Left-Align and Trim: vArmyKnife contains the GATK 3.8 open software libraries, and can internally call the functons underlying the LeftAlignAndTrim function. Note that this function generally must be combined with the multiallelic split in order to fully left align and trim indels.





### Gene and Transcript Annotation

Transcript Effect Annotation: Like SnpEff, vArmyKnife generates automated prediction of transcript base changes. It outputs ths information organized in several different ways, intended to make it easier to parse, filter, and organize variants based on their type and effect.

SnpEff Processing: vArmyKnife also has several functions designed to parse and extract useful information from SnpEff-generated "ANN" fields. This often produces information similar to the vArmyKnife native automated transcript annotations. There are often minor differences caused by differences in definitions, as well as differences in the alignment of indels. This can serve as an alternative to the native transcript effect prediction.

### Comparing and Combining Multiple Caller VCFs

vArmyKnife contains tools for comparing and merging multiple VCFs for the same sample sets. This can be used to compare the results of different sequencing technologies or variant calling tools. It can also be used to generate "ensemble" callsets and final genotype calls based on the results of two or more variant callers. Because different variant callers often have different assumptions and different failure modes, the intersection between multiple callers often produces much lower false discovery rates than either caller alone (albeit at the cost of fewer true positives as well).

## How-To:

### Dropping Fields, samples, etc:

vArmyKnife can remove extranious INFO or GENOTYPE/FORMAT fields by either specifying the fields you want to keep, OR by specifying the fields that you want to drop. These can be specified using the parameters:

   --outputKeepInfoTags tag1,tag2,...
   --outputDropInfoTags tag1,tag2,...
   --outputKeepGenoTags tag1,tag2,...
   --outputDropGenoTags tag1,tag2,...

Individual samples can be dropped using the command:

   --inputKeepSamples samp1,samp2,...
     or
   --outputKeepSamples samp1,samp2,...

If the "outputKeepSamples" option is used, then the samples will be removed AFTER all other processing has taken place. If you want to perform all processing on the reduced sample set, use the inputKeepSamples option.

ALL sample/genotype-level data can be dropped from the file using the "--dropGenotypeData" flag.

### Merging variant data from separate chromosomes / spans:

vArmyKnife can seamlessly read data from multiple VCFs that contain variants from different chromosomes or loci. It is often useful to split up the genome into smaller parts for processing.

This can be done by either specifying a file containing a list of VCF files and using the --infileList flag. So for example if the file vcflist.txt contains a simple list of VCF file paths (one per line), then you would use the command:

    varmyknife walkVcf --infileList vcflist.txt merged.output.vcf.gz

Alternatively, if all VCFs have similar paths and names, you can specify a infix list file. For example, say we have N input VCFs which are each of the form "infile.chromName.vcf.gz", and we have a file with a list of the chromosomes named "chromList.txt". We could merge these VCF files into a single all-chromosome file using the command:

    varmyknife walkVcf --infileListInfix chromList.txt "infile.|.vcf.gz" output.allChrom.vcf.gz

Note that the infile parameter must be placed in quotes to prevent BASH from interpreting the bar character as a pipe.



### Splitting multiallelics:

vArmyKnife has two ways to do this without discarding information. In particular, there needs to be a way to indicate multiallelic alleles in the GT and AD tags. The default method is to use the star (asterisk) allele as a marker for an allele on one of the other VCF lines. For the AD tag, the read depth for all other alternative alleles will be summed up and listed for this "star" allele.

    varmyknife walkVcf --splitMultiAllelics input.vcf.gz output.vcf.gz
    #  or
    varmyknife walkVcf --splitMultiAllelicsNoStarAllele input.vcf.gz output.vcf.gz

### Gene and Transcript Annotation:

todo!

### Annotating and Filtering Variants with Boolean Expressions:

vArmyKnife includes an advanced set of parameters that allow boolean expressions to be used to either annotate or filter variants. Expressions can be combined or modified using "AND", "OR", and "NOT", as well as parentheses. vArmyKnife does not currently respect order of operations, and parentheses are recommended to eliminate possible ambiguity. The full list of base expressions are listed in the walkVcf help page.

An additional INFO field can be added using the "--tagVariantsExpression" parameter. This parameter should be a Bar-delimited list of the desired tag ID, a description of the tag, then the expression itself. This will create a new INFO field with a value of either 0 or 1.

For example, to create a new INFO tag that will be equal to 1 if and only if the exac allele frequency (stored in an already-present info field named "ExAC_AF") is less than 0.01, OR that the exac allele frequency field is missing (which in this case would indicate that the variant was not found in ExAC), we would use the command:

    varmyknife walkVcf \
               --tagVariantsExpression "ExAC_LOW_AF|This field is equal \
    to 1 if and only if the variant is missing from ExAC or is at an AF \
    less than 0.01| INFO.lt:ExAC_AF:0.01 OR INFO.m:ExAC_AF" \
               input.vcf.gz output.vcf.gz

Note that the base expressions "INFO.lt" and "INFO.m" are functions used to indicate whether an INFO field is less than a given value or an INFO field is missing, respectively. All the base expressions are described at length in the walkVcf help page.

The same expression syntax can also be used to remove variants using the "--dropVariantsExpression" parameter. This parameter value should simply be the variant expression. So for example, to remove all variants that do not have an ExAC allele frequency less than 0.01:

    varmyknife walkVcf \
               --tagVariantsExpression "INFO.lt:ExAC_AF:0.01 OR INFO.m:ExAC_AF" \
               input.vcf.gz output.vcf.gz

### Filtering genotypes with Boolean Expressions:

Similar expressions can be used to "filter" individual sample genotypes by setting them to "missing". 

TODO: write more here.

### Misc other functions:

The GC content of the region flanking each variant can be added as a new INFO tag using the --addLocalGcInfo command.

    varmyknife walkVcf \
               --addLocalGcInfo "GCCONTENT_,100,4" \
               --genomeFA genome.fa \
               input.vcf.gz output.vcf.gz

The "100" indicates that we want to count the GC content in 100bp windows from the variant, and the "4" indicates that we want the result rounded to 4 digits of precision. Multiple windows can also be specified, delimited with bar characters.

Intersection with a supplied BED file can be added as a new INFO field using the "--addBedTags" parameter. The parameter value must be formatted as a colon-delimited list composed of the field name, the bp window size, whether you want 1 to indicate ON the bed file ("+") or OFF the bed file ("-"), a description of the field to go in the INFO line, and the bed file path. For example:

    varmyknife walkVcf \
               --addBedtags "RepeatMasker:5:+:Variant is on a RepeatMasker region:rmsk.bed" \
               input.vcf.gz output.vcf.gz

The variant specification can be copied to an INFO field using the command "--addVariantPosInfo". This creates a new INFO field containing a comma delimited list of CHROM, POS, REF, and ALT columns. This can be useful for mapping back and forth between non-left-aligned and left-aligned variant files, or for examining the other alternative alleles for multiallelic variants. Also, a unique variant ID number can be added as an INFO field using the "--addVariantIdx" parameter. For example:

    varmyknife walkVcf \
               --addVariantPosInfo varInfo \
               --addVariantidx varIdx \
               input.vcf.gz output.vcf.gz

vArmyKnife has a special parameter for taking the maximum of several simple numeric INFO fields. This is primarily used to take the maximum observed allele frequency across several different databases or populations. So for example if our VCF already contains INFO fields indicating allele frequencies from ExAC, ESP, and thousand genomes, listed as ExAC_AF, ESP_AF, and KG_AF, the maximum observed allele frequency can be generated and written as a new INFO field called MAX_AF using the command:

    varmyknife walkVcf \
               --ctrlAlleFreqKeys "MAX_AF:ExAC_AF,ESP_AF,KG_AF"
               input.vcf.gz output.vcf.gz

## VCF Walkers:

The basic structure of the software was inspired by GATK. 
Different commands execute different "VCF walkers". 

A "walker" is just a modular sub-program that takes a VCF, does something to it, and passes it on. 

The VCF format requires substantial processing in order to convert a variant line into useful data structures. 
Thus, reading a variant from a VCF line and converting it back into a VCF line takes substantial overhead. 
vArmyKnife can "chain" multiple walkers in sequence, allowing a large number of steps to be merged into one big step. 
This is even faster than chaining multiple commands in a file pipe, since the variant does not need to be converted to and from plain text format. In addition to reducing the number of steps required, 
using chained walkers greatly reduces the total runtime.

When using the "vArmyKnife walkVcf" command, VCF walkers are added depending on 
what optional parameters you add. If all you include is the input and output files (which are required), 
then all it does is read out the VCF and then copy it over:

    varmyknife walkVcf inputfile.vcf.gz outputfile.vcf.gz

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

