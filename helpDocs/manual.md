# User Manual for vArmyKnife 

> v2.1.82   \
> Compiled 

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

## ADDITIONAL DOCUMENTATION:

General command documentation can be found [here](indexInputParams.html).

Options and variables for the multiStepPipeline command (which is the command that you almost always will use)
can be found [here](multiStepPipeline.html)

## INPUT COMMANDS AND OPTIONS:

All vArmyKnife executions follow the same basic syntax:

    varmyknife commandName parameters

For example:

    varmyknife multiStepPipeline testInput.vcf.gz testOutput.vcf.gz

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

    varmyknife multiStepPipeline --gtfFile myGtfFile.gtf.gz \
                              --genomeFA hg19.fa.gz \
                                testInput.vcf.gz \
                                testOutput.vcf.gz

Then it will add a "AddTxAnnoSVcfWalker" walker, which adds a wide array of transcript-related annotations, like 
transcript lists, mRNA changes, amino acid changes, variant type (LOF/splice-loss/etc), and so on. The above 
command is not the recommended way to do this, however. Reading raw GTF and fasta files is very time consuming, 
so I have created a special custom transcript sequence file format that contains the necessary extracted data. I
have generated these files for hg19 with the ensembl and UCSC GTF files. If you need to use a different annotation or
genome build, let me know and I can show you how they can be generated. Thus, the recommended way to use these 
transcript files is:

    varmyknife multiStepPipeline --inputSavedTxFile ucsc.txdata.txt.gz \
                                testInput.vcf.gz \
                                testOutput.vcf.gz



If you also add
a file that lists transcript names and the associated gene symbol, it will also include a gene list for each
variant:

    varmyknife multiStepPipeline --txToGene txToGene.txt \
                              --gtfFile myGtfFile.gtf.gz \
                              --genomeFA hg19.fa.gz \
                                testInput.vcf.gz \
                                testOutput.vcf.gz

If you also add a file that lists the canonical transcript for each gene, then it will add another walker
that generates similar annotation fields restricted to only canonical transcripts:

    varmyknife multiStepPipeline --txToGene txToGene.txt \
                              --inputSavedTxFile txdata.txt.gz \
                              --addCanonicalTags canonList.txt \
                                testInput.vcf.gz \
                                testOutput.vcf.gz

If you add the parameter --splitMultiAllelics, then it will also split any multiallelic variants:

    varmyknife multiStepPipeline --txToGene txToGene.txt \
                              --inputSavedTxFile txdata.txt.gz \
                              --addCanonicalTags canonList.txt \
                              --splitMultiAllelics \
                                testInput.vcf.gz \
                                testOutput.vcf.gz

If you also add the "--groupFile" parameter, specifying a file that lists all the samples along with
their sample group(s), then you will get various counts for each sample group:

    varmyknife multiStepPipeline --txToGene txToGene.txt \
                              --groupFile testGroups.txt \
                              --inputSavedTxFile txdata.txt.gz \
                              --addCanonicalTags canonList.txt \
                              --splitMultiAllelics \
                                testInput.vcf.gz \
                                testOutput.vcf.gz

Sometimes with all these parameters, the commands can get really long and complex. To help alleviate 
this, you can specify a paramFile:

    varmyknife multiStepPipeline --paramFile testParam.part001.txt \
                                testInput.vcf.gz \
                                testOutput.vcf.gz

    #testParam.part001.txt:
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

## PARALLELIZATION:

vArmyKnife has a variety of tools intended to allow easy parallelization.

VCF files from CGR are generally located in a build directory. Also contained in that directory is
a subdirectory containing the same data but split up into ~4100 parts. This allows one to easily perform
complex operations on these data in a fast and efficient manner.

But 4100 is a little too many for our purposes, especially since each job has a ~30gb memory footprint. 
So instead I generally split-merge these files into 218 parts. Most of these parts are simply composed of
a subset of the 4100 parts, although the members of the 4100 file list that contain the "border" between
two chromosomes are split up by chromosome.

This also reduces the memory footprint, since each job only needs to load the transcript data for one 
chromosome, rather than the whole genome.

This behavior is controlled with the "--infileListInfix" parameter and the "--chromList" parameter. 
The infileListInfix points to a file that just contains a list of "infix" strings. When the 
infileListInfix parameter is set, the input file must contain a bar-sign ("|").

It is worth noting that whenever you use special characters in linux, you have to make sure you put
quotes around it. It's almost always safest to put quotes around everything except commands.

For example, say you have a group of BAM files named testInput.chr16.1.vcf.gz to testInput.chr16.10.vcf.gz.
You can concatenate these BAM files by creating a file fileParts.txt that just contains one number of each line
from 1-10, then running:

    varmyknife multiStepPipeline --paramFile testParam.part001.txt \
                              --infileListInfix fileParts.txt \
                              --chromList "chr16" \
                              "testInput.chr16.|.vcf.gz" \
                              "testOutput.vcf.gz"

If your VCF was created by CGR, then the build is already split into many parts (usually ~4100 parts). I find it most efficient
to partially merge them and then run separate jobs on each larger VCF. I'll go into greater depth on how to do this on a later date.

WRITE MORE HERE!


## MERGING MULTIPLE CALLER VCFS:



## OUTPUT DOCUMENTATION:

TODO: write more!

## EXAMPLE PARAMFILES:

The kitchen sink run:

    #These options get added to the pipeline run using the option --paramFile thisfile.txt
    #Leading whitespace is ignored, and newlines that come after a backslash are ignored.
    #Note: TRAILING whitespace is NOT ignored. This is important because some parameters require whitespace
    #(Also: Any line that starts with a # is ignored.)
    #Simple internal string variables can be set using the syntax: VARNAME=VARSTRING
    #  and can be referenced using ${VARNAME}. Note, unlike bash, the brackets are REQUIRED!
    #  Note: Variable declarations can reference other previously-declared variables.
    #Note that variables are specific to only THIS param file, and will NOT carry over into
    #  the other parameters declared in the command line.
    #
    #
    #Resource Directory on CCAD:
    RESOURCEDIR=/mnt/nfs/gigantor/ifs/Shared/hartleys/resources/
    #Resource Directory of HELIX. Uncomment if you are on HELIX:
    #RESOURCEDIR=/data/hartleys/pub/resources/
    #
    #Minor utilities and options:
    --splitAlleleGroupCounts
    --splitMultiAllelics
    --alphebetizeHeader
    --geneVariantsOnly
    --nonNullVariantsOnly
    
    #Perform genotype filtering.
    #   Require GQ >= 20 and AD > 10.
    --genoFilter            GTAG.gt:hc_GQ:19 AND GTAGARRAYSUM.gt:hc_AD:10 AND TAGPAIR.match:GT:hc_GT_FIX
    --unfilteredGtTag       GT_PREFILT
    --filterTag             SWH_FILTER_GT
    --superGroupList        CASE,CCSS;CTRL,ACS,PLCO
    
    #Add transcript and gene info:
    --inputSavedTxFile	${RESOURCEDIR}/annotation.ucsc/txdata.ucsc.hg19.txt.gz
    --txToGeneFile		${RESOURCEDIR}/KnownGeneToMaps/knownToSymbol.txt
    --addCanonicalTags	${RESOURCEDIR}/KnownGeneToMaps/knownCanonicalRefSeq.txt
    
    #Add a bunch of locus-based variables based on whether the variant locus is on a region 
    #   specified by some bed files.
    --addBedTags SWH_locusIsNearRmsk_5:5:+:Variant locus is within 5bp of a repeatmasker masked region:\
                        ${RESOURCEDIR}/ucsc/rmsk.bed.gz,\
                 SWH_locusIsNearHPR_5:5:+:Variant locus is within 5bp of a ${HPRLEN}bp homopolymer run:\
                        ${RESOURCEDIR}/homopolymer/homopolymer.rep5.bed.gz,\
                 SWH_locusIsOnTarget_5:5:+:Variant locus is within 5bp of an exome targeted region:\
                        ${RESOURCEDIR}/captureBeds/120430_HG19_ExomeV3_UTR_EZ_HX1_capture.bed,\
                 SWH_locusIsNearSimpleRepeat_5:5:+:Variant locus is within 5bp of a simple tandem repeat with score 500:\
                        ${RESOURCEDIR}/ucsc/simpleRepeat.score500.bed.gz,\
                 SWH_locusConserved:0:+:Variant locus is within a highly conserved region:\
                        ${RESOURCEDIR}/gerp/significantConservedElements.txt,\
                 SWH_locusIsMappable:0:-:Variant locus is in a mappable region:\
                        ${RESOURCEDIR}/ucsc/wgEncodeCrgMapabilityAlign100mer.lowMappabilityRegions.bed.gz,\
                 SWH_locusIsOnPseudogene:0:+:Variant locus is on a pseudogene:\
                        ${RESOURCEDIR}/gencodePseudogenes/gencode.v19.2wayconspseudos.gtf.gz,\
                 SWH_locusIsOnDomain:0:+:Variant locus is on a known domain in swissProt:\
                        ${RESOURCEDIR}/ucsc/unipDomain.swissProt.bed.gz
    #Some options, like addBedTags, can be specified all at once as a comma-delimited list, or can be
    #   simply invoked more than once to add to the list.
    --addBedTags SWH_locusIsGiabHiConf:0:+:\
                        Locus is on giab high confidence region:\
                        ${RESOURCEDIR}/giab/unionV2.19.fixed.bed

    #The following parameters are used by the ACMG pathogenicity caller,
    #   which also does a number of secondary things like
    #   clinvar/hgmd matching and summarizing the in silico calls.
    --clinVarVcf          ${RESOURCEDIR}/annotation.ucsc/ClinVar.annoTX.hg19.vcf.gz
    --hgmdVarVcf          ${RESOURCEDIR}/annotation.ucsc/HGMD2016.4.annoTX.hg19.vcf.gz
    --toleranceFile 	  ${RESOURCEDIR}/tolerance/tolerance.simple.txt
    --ctrlAlleFreqKeys    1KG_EUR_AF,ESP_EA_AF,ExAC_NFE
    --locusRepetitiveTag  SWH_locusIsNearRmsk_5
    --locusDomainTag      SWH_locusIsOnDomain
    --locusConservedTag   SWH_locusConserved
    --locusMappableTag    SWH_locusIsMappable
    --locusPseudoTag      SWH_locusIsOnPseudogene
    --inSilicoKeys        SWH_DBNSFP_MetaSVM_pred:eq:D:T,SWH_REVEL_SCORE:ge:0.5:0.2
    
    #Add several tags that include sample IDs for samples that have variant genotypes.
    #  if a groupFile is specified elsewhere, then it will also make sublists
    #  containing just the samples in each sample group.
    --addSampTag 30
    
    #Variables that only work on our datasets:
        #Add heterozygote depth stats.
        --addDepthStats hc_AD,SWH_split_alleIdx
        
    #Match this VCF up with other external VCFs and copy over some tags:
    --addInfoVcfs SWH_DBNSFP:\
                  ${RESOURCEDIR}/dbNSFP/v2.9/dbNSFP.v2.9.fc.vcf.gz:\
                  SIFT_score|SIFT_pred|Polyphen2_HDIV_score|Polyphen2_HDIV_pred|Polyphen2_HVAR_score|Polyphen2_HVAR_pred|LRT_score|LRT_pred|MutationTaster_score|MutationTaster_pred|MutationAssessor_score|MutationAssessor_pred|FATHMM_score|FATHMM_pred|MetaSVM_score|MetaSVM_pred|MetaLR_score|MetaLR_pred|PROVEAN_score|PROVEAN_pred|CADD_raw|CADD_raw_rankscore|CADD_phred
    #Do the same thing for REVEL calls:
    --addInfoVcfs SWH:${RESOURCEDIR}/revel/revel_all_chromosomes.vcf.gz:REVEL_SCORE
    
    #Add a bunch of boolean fields based on boolean expressions:
    --tagVariantsExpression isSNV|Equal to 1 iff the variant is a simple SNV|isSNV
    --tagVariantsExpression SWH_GATK_QC_PASS|\
                            Equal to 1 iff the variant passes all the standard GATK filters. whcih are as follows. \
                                      For SNVs: DP gt 5. QD gt 2. MQ gt 40. FS lt 60. MQRankSum gt -12.5. ReadPosRankSum gt -8. SOR lt 3. \
                                      And for indels. QD gt 2. FS lt 200. ReadPosRankSum gt -20 and SOR lt 10.|\
                            (isSNV AND INFO.gt:SWH_hc_DP:5 AND \
                                       INFO.gt:SWH_hc_QD:2 AND \
                                       INFO.gt:SWH_hc_MQ:40 AND \
                                       INFO.lt:SWH_hc_FS:60 AND \
                                       INFO.gt:SWH_hc_MQRankSum:-12.5 \
                                       AND INFO.gt:SWH_hc_ReadPosRankSum:-8 \
                                       AND INFO.lt:SWH_hc_SOR:3 ) OR \
                            ( ( NOT isSNV ) AND \
                                       INFO.gt:SWH_hc_QD:2 AND \
                                       INFO.lt:SWH_hc_FS:200 AND \
                                       INFO.gt:SWH_hc_ReadPosRankSum:-20 AND \
                                       INFO.lt:SWH_hc_SOR:10 \
                             )
    --tagVariantsExpression SWH_isLongIndel|\
                            Equal to 1 iff the variant is an indel of length 6 or greater.| \
                            (NOT isSNV) AND (REF.len.gt:5 OR ALT.len.gt:5)
    --tagVariantsExpression SWH_passQC_AF|\
                            Equal to 1 iff the variant has an allele frequency less than 0.001 in the external controls|\
                            INFO.ltm:SWH_ACMG_ctrlAFMAX:0.001
    --tagVariantsExpression SWH_passQC_AlleFrac|\
                            Equal to 1 iff the variant has a total allele depth ratio of greater than 0.2 in the het samples| \
                            INFO.gtm:SWH_STAT_AD_HET_FRAC:0.2
    --tagVariantsExpression SWH_passQC_MissRate|\
                            Equal to 1 iff the variant has a missing-genotype rate less than 0.25| \
                            INFO.lt:SWH_CT_MisFrq_GRP_CCSS:0.25
    --tagVariantsExpression SWH_passQC_locus|\
                            Equal to 1 iff the variant is not on or within 5bp from low mappability regions or HPR regions or Rmsk regions or off target regions or simple repeat regions or on a pseudogene| \
                            NOT ( INFO.eq:SWH_locusIsMappable:1 AND INFO.eq:SWH_locusIsNearHPR_5:1 OR INFO.eq:SWH_locusIsNearRmsk_5:1 OR INFO.eq:SWH_locusIsNearSimpleRepeat_5:1 OR INFO.eq:SWH_locusIsOnPseudogene:1 )
    --tagVariantsExpression SWH_passQC|\
                            Equal to 1 iff the variant passes all quality and frequency filters.|\
                            FILTER.eq:. AND INFO.eq:SWH_passQC_AF:1 AND INFO.eq:SWH_passQC_AlleFrac:1 AND INFO.eq:SWH_passQC_MissRate:1 AND INFO.eq:SWH_GATK_QC_PASS:1 AND INFO.eq:SWH_isLongIndel:0 AND INFO.eq:SWH_passQC_locus:0
    --tagVariantsExpression SWH_notLongIndel|not long indel|INFO.eq:SWH_isLongIndel:0
    --tagVariantsExpression SWH_passMisFreq|Mis freq ok|INFO.lt:SWH_CT_MisFreq_GRP_CTRL:0.2
    --tagVariantsExpression SWH_passHetFrac|Het frac ok| INFO.m:SWH_STAT_AD_HET_FRAC OR ( INFO.lt:SWH_STAT_AD_HET_FRAC:0.75 AND INFO.gt:SWH_STAT_AD_HET_FRAC:0.25 )
    --tagVariantsExpression SWH_locusNotNearHPR_5|not near hpr|INFO.eq:SWH_locusIsNearHPR_5:0
    --tagVariantsExpression SWH_locusNotNearRmsk_5|not near hpr|INFO.eq:SWH_locusIsNearRmsk_5:0
    --tagVariantsExpression SWH_locusNotNearSimpleRepeat_5|not near hpr|INFO.eq:SWH_locusIsNearSimpleRepeat_5:0
    --tagVariantsExpression SWH_locusNotOnPseudogene|not near hpr|INFO.eq:SWH_locusIsOnPseudogene:0
    --tagVariantsExpression CScorePass|pass CS|FILTER.eq:.
    --tagVariantsExpression SWH_passAllQC|pass all|INFO.eq:CScorePass:1 AND INFO.eq:SWH_notLongIndel:1 AND INFO.eq:SWH_passMisFreq:1 AND INFO.eq:SWH_passHetFrac:1 AND INFO.eq:SWH_passQC_locus:1 AND INFO.eq:SWH_GATK_QC_PASS:1


## LEGAL:
This software is "United States Government Work" under the terms of the United 
States Copyright Act. It was written as part of the authors’ official duties 
for the United States Government and thus QoRT Package User Manual cannot be 
copyrighted. This software is freely available to the public for use without a 
copyright notice. Restrictions cannot be placed on its present or future use.

Although all reasonable efforts have been taken to ensure the accuracy and 
reliability of the software and data, the National Human Genome Research 
Institute (NHGRI) and the U.S. Government does not and cannot warrant the 
performance or results that may be obtained by using this software or data. 
NHGRI and the U.S. Government disclaims all warranties as to performance, 
merchantability or fitness for any particular purpose.

In any work or product derived from this material, proper attribution of the 
authors as the source of the software or data should be made, using "NHGRI 
Genome Technology Branch" as the citation.

