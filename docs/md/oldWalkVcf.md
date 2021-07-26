# vArmyKnife
> Version 3.2.40 (Updated Wed May 26 09:14:51 EDT 2021)

> ([back to main](../index.html)) ([back to java-utility help](index.html))

## Help for vArmyKnife command "oldWalkVcf"

ALIASES: multiStepPipeline
## USAGE:

GENERAL SYNTAX:

    varmyknife [java_options] --CMD oldWalkVcf [options] infile.vcf.gz outfile.vcf.gz


## DESCRIPTION:

This utility performs a series of transformations on an input VCF file and adds an array of informative tags\.

## REQUIRED ARGUMENTS:
#### infile.vcf.gz:

> input VCF file. Can be gzipped or in plaintext. Can use dash - to read from STDIN. Note that multifile processing will obviously not be available in this mode. (String)


#### outfile.vcf.gz:

> The output file. Can be gzipped or in plaintext. Can use dash - to write to STDOUT Note that multifile processing will obviously not be available in this mode. (String)



## OPTIONAL ARGUMENTS:
### Input Parameters:
#### --infileList:

> If this option is set, then the infile parameter is a text file containing a list of input VCF files (one per line), rather than a simple path to a single VCF file. Multiple VCF files will be concatenated and used as input. Note that only the first file's headers will be used, and if any of the subsequent files have tags or fields that are not present in the first VCF file then errors may occur. Also note that if the VCF file includes sample genotypes then the samples MUST be in the same order. (flag)

#### --infileListInfix infileList.txt:

> If this command is included, then all input files are treated very differently. The input VCF file path (or multiple VCFs, if you are running a VCF merge of some sort) must contain a BAR character. The file path string will be split at the bar character and the string infixes from the supplied infileList.txt infix file will be inserted into the break point. This can be very useful for merging multiple chromosomes or genomic-region-split VCFs. (String)

#### --numLinesRead N:

> If this parameter is set, then the utility will stop after reading in N variant lines. Intended for testing purposes. (Int)

### Universal Parameters:
#### --genomeFA genome.fa.gz:

> The genome fasta file. Can be gzipped or in plaintext. (String)

### Preprocessing:
#### --splitMultiAllelics:

> If this flag is used, multiallelic variants will be split into multiple separate VCF lines. In order to preserve cross-allele genotypes and multiallelic FORMAT fields, the star allele will be used to indicate a different alt allele on a different variant line. The star-allele encoding can be deactivated using the --splitMultiAllelicsNoStarAllele option instead. (flag)

#### --splitMultiAllelicsNoStarAlle:

> If this flag is used, multiallelic variants will be split into multiple separate VCF lines. Two copies of the AD tag will be created, AD and AD\_multAlle. (flag)

#### --chromList chr1,chr2,...:

> List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes will be ignored. For a VCF that contains only one chromosome this option will improve runtime, since the utility will not have to load and process annotation data for the other chromosomes. (CommaDelimitedListOfStrings)

#### --extractInterval chr1:1000-5000:

> This tool will extract variants across the given genomic interval. All other loci will be discarded. Note: the VCF MUST be sorted! Furthermore: this extract will occur before any leftAlignAndTrim or similar preprocessing steps. (String)

#### --splitOutputByBed intervalBedFile.bed:

> If this option is set, the output will be split up into multiple VCF files based on the supplied BED file. An output VCF will be created for each line in the BED file. If the BED file has the 4th (optional) column, and if this 'name' column contains a unique name with no special characters then this name column will be used as the infix for all the output VCF filenames. If the BED file name column is missing, non-unique, or contains illegal characters then the files will simply be numbered. NOTE: If this option is used, then the 'outfile' parameter must be either a file prefix (rather than a full filename), or must be a file prefix and file suffix separated by a bar character. In other worse, rather than being 'outputFile.vcf.gz', it should be either just 'outputFile' or 'outputFile.|.vcf.gz'.  (String)

#### --universalTagPrefix VAK\_:

> Set the universal tag prefix for all vArmyKnife INFO and FORMAT tags. By default it is VAK\_. Warning: if you change this at any point, then all subsequent vArmyKnife commands may need to be changed to match, as vArmyKnife sometimes expects its own tag formatting. (String)

### Filtering, Variant-Level:
#### --keepVariantsExpression vcfLineFilterExpression:

> If this parameter is set, then VCF lines will be dropped if and only if they FAIL the provided vcf line filter expression. See the section on VCF line filtering below.  (String)

### Postprocessing:
#### --dropGenotypeData:

> If this flag is included, then ALL sample-level columns will be stripped from the output VCF. This greatly reduces the file size, and can be useful for making portable variant set VCFs. (flag)

#### --addDummyGenotypeColumn:

> If this flag is included, then the genotype data will be stripped and replaced with a dummy column with 1 sample and 1 het genotype on every line. (flag)

### Output Parameters:
#### --splitOutputByChrom:

> If this option is set, the output will be split up into parts by chromosome. NOTE: The outfile parameter must be either a file prefix (rather than a full filename), or must be a file prefix and file suffix separated by a bar character. In other worse, rather than being 'outputFile.vcf.gz', it should be either just 'outputFile' or 'outputFile.|.vcf.gz'.  (flag)

### Annotation:
#### --addAltSequence windowSize[:tagID]:

> Adds a field with the alt allele and [windowSize] base pairs of reference-genome flanking sequence to each side. If no tagID is specified, the tagID will be VAK\_altSeq[windowsSize]. Note: requires genomeFA be set! Note: this parameter can be set more than once (with different window sizes). (repeatable String)

#### --tagVariantsExpression newTagID|desc|variantExpression:

> If this parameter is set, then additional tags will be generated based on the given expression(s). The list of expressions must be comma delimited. Each element in the comma-delimited list must begin with the tag ID, then a bar-symbol, followed by the tag description, then a bar-symbol, and finally with the expression. The expressions are always booleans, and follow the same rules for VCF line filtering. See the section on VCF line filtering, below. (repeatable String)

#### --tagVariantsGtCountExpression newTagID|desc|genotypeExpression[|type]:

> If this parameter is set, then additional tags will be generated based on the given GENOTYPE expression(s). Each element in the comma-delimited list must begin with the tag ID, then a bar-symbol, followed by the tag description, then a bar-symbol, and finally with the genotype expression. The expressions are always booleans, and follow the same rules for VCF genotype-level filtering. See the section on VCF genotype filtering, below. Optionally, a fourth element in the list can define the type which can be either FRAC, PCT, or CT, which defines the output as either a fraction, a percentage, or a count. (repeatable String)

#### --variantMapFunction FunctionType|ID|param1=p1|param2=p2|...:

> TODO DESC (repeatable String)

#### --tallyFile file.txt:

> Write a file with a table containing counts for all tallies, warnings and notices reported during the run. (String)

### Other Inputs:
#### --paramFile paramFile.txt:

> A file containing additional parameters and options. Each parameter must begin with a dash. Leading whitespace will be ignored, and newlines preceded by a backslash are similarly ignored. Lines that begin with a pound sign will be skipped. Trailing parameters (infile, outfile, etc) CANNOT be defined using a parameter file. (paramFile)

### Sample Info:
#### --groupFile groups.txt:

> File containing a group decoder. This is a simple 2-column file (no header line). The first column is the sample ID, the 2nd column is the group ID. (String)

#### --superGroupList sup1,grpA,grpB,...;sup2,grpC,grpD,...:

> A list of top-level supergroups. Requires the --groupFile parameter to be set. (String)

### OTHER OPTIONS:
#### --verbose:

> Flag to indicate that debugging information and extra progress information should be sent to stderr. (flag)

#### --quiet:

> Flag to indicate that only errors and warnings should be sent to stderr. (flag)

#### --debugMode:

> Flag to indicate that much more debugging information should be sent to stderr. (flag)

#### --createRunningFile filename.txt:

> A file to create when this utility starts, to be deleted on a clean exit. The file WILL be deleted even if errors are caught. It will only remain if uncaught errors are thrown or if the process is killed externally. (String)

#### --successfulCompletionFile filename.txt:

> A file to create if and when this utility successfully completes without fatal errors. (String)


# VARIANT OPERATIONS

    
    Variant Operations or Variant Map Functions are a set of 
        sub-utilities that perform operations on a variant set one 
        variant at a time. When more than one function is specified 
        in a run, these functions are performed in the order that 
        they appear in the command line, after all other operations 
        have been carried out (excluding output ops).
    Basic Syntax:
        --FCN functionName|ID|param1=p1|param2=p2|...
    
    functionName: one of the functions listed below.
    ID: The ID for this particular operation run. This will be used 
        in warning/error messages and in the header metadata. It is 
        recommended that this ID be unique. In some functions this 
        ID is used to determine the INFO field names.
    param1,param2,...: Most functions take one or more parameters. 
        Parameters are specified with the format: param=value, 
        where param is the parameter ID listed in the documentation 
        below.

## Available Operations

    

##### General\-Purpose Tools



### addInfo

>  This is a set of functions that all take one or more input parameters and outputs one new INFO field\. The syntax is: \-\-fcn "addInfo|newTagName|fcn\(param1,param2,\.\.\.\)"\. Optionally you can add "|desc=tag description"\. There are numerous addInfo functions\. For more information, go to the section on addInfo Functions below, or use the help command: varmyknife help addInfo


    func: (String, required)
    desc: The description in the header line for the new INFO 
        field.(String, default=No desc provided)
###### Example 1:
    Make a new INFO field which is the maximum from several allele 
        frequencies (which are already in the file) Then make a 0/1 
        INFO field that is 1 if the max AF is less than 0.01. Note 
        the CONST:0 term, which allows you to include constant 
        values in these functions. In this case it makes it so that 
        if the AF is missing in all three populations, the maxAF 
        will be 0 rather than missing.
    varmyknife walkVcf \
    --fcn "addInfo|maxAF|MAX(CEU_AF,AFR_AF,JPT_AF,CONST:0)|\
    desc=The max allele frequency from CEU_AF, AFR_AF, or JPT_AF 
        (or zero if all are missing)."\
    --fcn "addInfo|isRare|EXPR(INFO.lt:maxAF:0.01)|\
    desc=Indicates whether the variant maxAF is less than 0.01."\
    infile.vcf.gz outfile.vcf.gz
###### End Example
###### Example 2:
    varmyknife walkVcf \
    --fcn "addInfo|CarryCt|SUM(hetCount,homAltCount)|\
    desc=The sum of the info tags: hetCount and homAltCount."\
    infile.vcf.gz outfile.vcf.gz
###### End Example

### addFormat

>  This is a set of functions that all take one or more input parameters and outputs one new FORMAT field\. The syntax is: \-\-fcn "addInfo|newTagName|fcn\(param1,param2,\.\.\.\)"\. Optionally you can add "|desc=tag description"\. There are numerous addInfo functions\. For more information, go to the section on addFormat Functions below, or use the help command: varmyknife help addFormat


    func: (String, required)
    desc: The description in the header line for the new INFO 
        field.(String, default=No desc provided)
###### Example 1:
    Make a new FORMAT field which is the maximum from several 
        allele frequencies (which are already in the file) Then 
        make a 0/1 INFO field that is 1 if the max AF is less than 
        0.01. Note the CONST:0 term, which allows you to include 
        constant values in these functions. In this case it makes 
        it so that if the AF is missing in all three populations, 
        the maxAF will be 0 rather than missing.
    varmyknife walkVcf \
    --fcn "addInfo|maxAF|MAX(CEU_AF,AFR_AF,JPT_AF,CONST:0)|\
    desc=The max allele frequency from CEU_AF, AFR_AF, or JPT_AF 
        (or zero if all are missing)."\
    --fcn "addInfo|isRare|EXPR(INFO.lt:maxAF:0.01)|\
    desc=Indicates whether the variant maxAF is less than 0.01."\
    infile.vcf.gz outfile.vcf.gz
###### End Example
###### Example 2:
    varmyknife walkVcf \
    --fcn "addInfo|CarryCt|SUM(hetCount,homAltCount)|\
    desc=The sum of the info tags: hetCount and homAltCount."\
    infile.vcf.gz outfile.vcf.gz
###### End Example

### sampleCounts

>  This function generates counts and frequencies for alt alleles, alt genotypes, missing genotypes, ref genotypes, and so on\. Note that there are several calc\- flags\. If none of these are included, then this function does nothing\.


    groupFile: A tab-delimited file containing sample ID's and a 
        list of group IDs for each sample. See the --groupFile 
        parameter of walkVcf.(String)
    inputGT: The input genotype FORMAT field.(String)
    noCountsCalc: (Flag)
    noFreqCalc: (Flag)
    noMissCalc: (Flag)
    noAlleCalc: (Flag)
    noHetHomCalc: (Flag)
    noMultiHetCalc: (Flag)
    expr: The variant expression, which is a true/false expression 
        using the variant expression syntax.(String)

### sampleLists

>  This function generates sample list fields which contain comma\-delimited lists of samples that are het or hom\-alt\.


    inputGT: The input genotype FORMAT field.(String)
    samplePrintLimit: (String)
    groupFile: A tab-delimited file containing sample ID's and a 
        list of group IDs for each sample. See the --groupFile 
        parameter of walkVcf.(String)
    expr: The variant expression, which is a true/false expression 
        using the variant expression syntax.(String)

### depthStats

>  This function calculates various statistics on total read depth and hetAB\.


    inputGT: The input genotype FORMAT field.(String)
    inputAD: (String)
    inputDP: (String)
    restrictToGroup: (String)
    groupFile: A tab-delimited file containing sample ID's and a 
        list of group IDs for each sample. See the --groupFile 
        parameter of walkVcf.(String)

### calcStats

>  This function combines the functions sampleCounts, sampleLists, and depthStats, performing all three\.


    groupFile: A tab-delimited file containing sample ID's and a 
        list of group IDs for each sample. See the --groupFile 
        parameter of walkVcf.(String)
    inputGT: The input genotype FORMAT field.(String)
    inputAD: (String)
    inputDP: ()
    noCountsCalc: (Flag)
    noFreqCalc: (Flag)
    noMissCalc: (Flag)
    noAlleCalc: (Flag)
    noHetHomCalc: (Flag)
    noMultiHetCalc: (Flag)
    samplePrintLimit: (String)
    noDepthStats: (Flag)
    noSampleLists: (Flag)
    noSampleCounts: (Flag)
    expr: The variant expression, which is a true/false expression 
        using the variant expression syntax.(String)

### addVariantIdx

>  This function adds a new INFO column with a unique numeric value for each line\. Optionally, you can add a prefix to each ID\.


    prefix: Prefix to prepend to the index field.(String)

### addVariantPosInfo

>  This function adds a new INFO field in the form: CHROM:START:REF>ALT\. This can be useful for checking the effects of functions that alter the variant columns\. For example, you can run this function before and after leftAlignAndTrim to see how a variant changes\.


    (This function takes no parameters)

### markDup

>  This map function will detect duplicate variant lines and add two new INFO fields: mapID\_CT and mapID\_IDX\. The CT will indicate how many duplicates were found matching the current variant, and the IDX will number each duplicate with a unique identifier, counting from 0\. All nonduplicates will be marked with CT=1 and IDX=0\. VCF FILE MUST BE SORTED\!


    (This function takes no parameters)

##### Variant Formatting/Conversion



### fixFirstBaseMismatch

>  This utility will extend indels in which the first base is not a matching base\. Certain variant processing tools may use blanks to mark indels or may not begin combination insertion\-deletion variants with a matching base \(this latter case is technically legal VCF, but some tools may throw errors\)\. 


    windowSize: Sets the size of the sliding window used. Problems 
        may occur if you have variants longer than this window 
        size. Default is 200bp.(Int)
    genomeFA: The genome fasta file containing the reference 
        genome. This will be used by various functions that require 
        genomic information. Note that some functions that call the 
        GATK library will also require that the fasta be indexed. 
        Note: Chromosome names must match.(String, required)

### leftAlignAndTrim

>  This utility performs the exact same operations as GATK leftAlignAndTrim\. It trims excess bases and shifts ambiguously\-positioned indels to their leftmost possible position\. This can assist in ensuring that variants are consistantly represented which is critical in matching indels between files\. IMPORTANT: if there are multiallelic variants then they MUST be split apart before this step\. You can use the splitMultiAllelics function to do this\.


    windowSize: Sets the size of the sliding window used. Problems 
        may occur if you have variants longer than this window 
        size. Default is 200bp.(Int)
    genomeFA: The genome fasta file containing the reference 
        genome. This will be used by various functions that require 
        genomic information. Note that some functions that call the 
        GATK library will also require that the fasta be indexed. 
        Note: Chromosome names must match.(String, required)

### fixSwappedRefAlt

>  This utility searches for cases where the REF and ALT bases are swapped\.


    genomeFA: The genome fasta file containing the reference 
        genome. This will be used by various functions that require 
        genomic information. Note that some functions that call the 
        GATK library will also require that the fasta be indexed. 
        Note: Chromosome names must match.(String, required)

### splitMultiAllelics

>  This utility takes any multiallelic variables and splits them apart so that each line contains only one ALT allele\. There are two options for how this will be carried out\. The default creates several new FORMAT fields\. TODO explain more\! \.\.\.Thus after the split the multiallelics will have an ALT field of the form A,\* and the GT field and AD field will use this coding\. Thus if a sample has one of the other alt alleles then 


    useStarAlle: If this flag is used, the asterisk allele will be 
        used as a placeholder for all other alleles. See the 
        explanation above.(Flag)
    treatOtherAsRef:  (Flag)

### fixDotAltIndels

>  \.\.\.


    genomeFA: The genome fasta file containing the reference 
        genome. This will be used by various functions that require 
        genomic information. Note that some functions that call the 
        GATK library will also require that the fasta be indexed. 
        Note: Chromosome names must match.(String, required)

### addAltSequence

>  


    windowSize: The number of flanking bases to include on each 
        side of the alt sequence.(String, default=10)
    genomeFA: The genome fasta file containing the reference 
        genome. This will be used by various functions that require 
        genomic information. Note that some functions that call the 
        GATK library will also require that the fasta be indexed. 
        Note: Chromosome names must match.(String, required)

##### File/Database Annotation



### tagBedFile

>  This function takes a BED file \(which can be gzipped if desired\) and creates a new INFO field based on whether the variant locus overlaps with a genomic region in the BED file\. The new field can be either an integer that is equal to 1 if there is overlap and 0 otherwise \(which is the default behavior\) Or, alternatively, it can copy in the title field from the bed file\. NOTE: this function only uses the first 3 to 5 fields of the BED file, it does not implement the optional fields 10\-12 which can specify intron/exon blocks\.


    file: (String, required)
    desc: The description for the new INFO line.(String, default=No 
        desc provided)
    buffer: The additional buffer to add around each BED 
        element.(Integer, default=0)
    style: This determines the type of INFO tag. For +, the new tag 
        will be a dichotomous 0/1 numeric variable that will equal 
        1 if and only if the variant intersects with one or more 
        BED lines (including buffer, noted above). For - the 
        opposite is true. For LABEL, the new tag will be a String 
        variable with the title of the element(s) that intersect 
        with the variant, comma delimited. Note that for LABEL 
        style the BED file must have a 4th column.(String, 
        default=+)

### snpSiftAnno

>  This function runs a SnpSift anno command


    cmd: A valid SnpSift command(String)

### snpSiftAnnoMulti

>  This function runs several SnpSift anno commands in series\. This will be faster than several separate snpSiftAnno function calls\.


    cmds: A semicolon delimited list of valid snpSift 
        commands.(String)

### snpSiftDbnsfp

>  This function runs the SnpSift dbnsfp command


    cmd: A valid SnpSift command(String)

### snpEff

>  This function runs SnpEff by calling the SnpEff library internally\. It uses SnpEff version 4\.3t\.


    cmd: A valid SnpSift command.(String)
###### Example 1:
    varmyknife walkVcf
    --fcn "snpEff|mySnpEffRun_1|cmd=GRCh37.75 -noout -c 
        snpEff.config -v -noStats -lof -motif -nextprot"
    infile.vcf.gz outfile.vcf.gz
###### End Example

### snpEffExtract

>  


    annTag: A valid ANN formatted field, usually generated by 
        SNPeff.(String, default=ANN)
    bioTypeKeepList: A comma delimited list of the transcript 
        biotypes that you want to keep. All other biotypes will be 
        ignored.(String)
    effectKeepList: A comma delimited list of the effect types that 
        you want to keep. All other EFFECT values will be 
        ignored.(String)
    warningDropList: A comma delimited list of warnings. Any 
        entries that include a listed warning will be 
        ignored.(String)
    geneListName: (String)
    geneList: (String)
    severityList: Must be a list of severity levels, listed as some 
        combination of effectseverity types delimited with slashes. 
        Legal types are: HIGH, MODERATE, LOW, and MODIFIER, which 
        are standard SnpEFF effect types, and also: NS (HIGH and 
        MODERATE), NonNS (LOW and MODIFIER), and ANY (any 
        type).(String, default=HIGH/MODERATE/LOW)
    extractFields: This is a complex multi-part field that allows 
        flexible extraction of information from SnpEff ANN tags. 
        This field must be in the colon-delimited format 
        tagInfix:ANN_idx:description:severityList[:noCollapse]. 
        severityList must be in the same format as the severityList 
        parameter above, but can override the default if desired. 
        ANN_idx must be a slash-delimited list of field indices 
        counting from zero in the ANN tag. The standard ANN field 
        indices are: 0:allele,1:effect,2:impact,3:geneName,4:geneID-
        ,5:txType,6:txID,7:txBiotype,8:rank,9:HGVS.c,10:HGVS.p,11:c-
        DNAposition,12:cdsPosition,13:proteinPosition,14:distToFeat-
        ure,15:warnings,16:errors If multiple fields are selected 
        then the output fields will have the format 
        first:second:third:etc. For example, to create two new 
        fields containing a list of all genes for which the current 
        variant has HIGH and MODERATE impact respectively, use the 
        format: myNewField:4:my description:HIGH/MODERATE. This 
        will generate two new fields: myNewField_HIGH and 
        myNewField_MODERATE. Note that if this function as a whole 
        has a mapID set, then both field names will be prefixed by 
        that overall ID.(String)
    geneNameIdx: (String, default=4)
    biotypeIdx: (String, default=7)
    warnIdx: (String, default=15)

### getLocusDepthFromWig

>  This utility takes a \.wig file \(aka a wiggle file\) and annotates each variant with the depth indicated in the wiggle file for the variant site\.


    wigFile: The input wiggle file.(String)
    desc: The description for the new INFO field, to be included in 
        the INFO line.(String)

### addDistToFeature

>  This utility takes a simple 2\-column text file\. the first column must be the chrom ID and the second column must be position\. A new integer info field will be added that is equal to the distance to the nearest position in the file\. If there is no listed position on the given chromosome then the info field will be missing\.


    file: The input text file. Must have 2 columns, chrom and 
        pos(String)
    desc: The description for the new INFO field, to be included in 
        the INFO line.(String)

##### Genotype Processing



### unPhaseAndSortGenotypes

>  This function removes phasing and sorts genotypes \(so that heterozygotes are always listed as 0/1 and never 1/0\)\.


    inputGT: The input/output genotype FORMAT field.(String)
    groupFile: A tab-delimited file containing sample ID's and a 
        list of group IDs for each sample. See the --groupFile 
        parameter of walkVcf.(String)

### genotypeFilter

>  This function filters a genotype field based on a given genotype expression\. The new filtered genotype can replace the GT field or can be set to a different field, so multiple filtering strategies can be included in a single VCF\.


    expr: A Genotype Expression, using the genotype expression 
        syntax.(String, required)
    desc: A description, to go in the new FORMAT fields.(String)
    filterTag: The name of a new FORMAT field, which will be a flag 
        equal to 1 if and only if the genotype passes the 
        filter.(String)
    outputGT: The output genotype FORMAT field. If this is the same 
        as the input genotype field then the genotype field will be 
        overwritten.(String)
    inputGT: The input genotype FORMAT field.(String)
    inputGtNewName: If this parameter is set, the input genotype 
        field will be copied to a new tag with this name before 
        filtering. This can be useful if overwriting the input 
        genotype field.()
    groupFile: A tab-delimited file containing sample ID's and a 
        list of group IDs for each sample. See the --groupFile 
        parameter of walkVcf.(String)

### genotypeSelect

>  This function selects between two different a genotype fields based on a given genotype expression\. The new composite genotype can replace either GT field or can be set to a different field, so multiple filtering strategies can be included in a single VCF\.


    expr: A Genotype Expression, using the genotype expression 
        syntax.(String, required)
    desc: A description, to go in the new FORMAT fields.(String)
    outputGT: The output genotype FORMAT field. If this is the same 
        as the input genotype field then the genotype field will be 
        overwritten.(String)
    inputGTifTRUE: The input genotype FORMAT field to be used if 
        the expression returns TRUE.(String, required)
    inputGTifFALSE: The input genotype FORMAT field to be used if 
        the expression returns FALSE.(String, required)
    missingString: The string to use when setting the variable to 
        missing.(String)
    groupFile: A tab-delimited file containing sample ID's and a 
        list of group IDs for each sample. See the --groupFile 
        parameter of walkVcf.(String)

##### Concordance Caller



### concordanceCaller

>  \.\.\.\.


    callerNames: Comma delimited list of caller IDs, used in the 
        callerSet INFO fields and the names of the output GT 
        fields. By default, callers will simply be named 
        C1,C2,...(String)
    priority: Comma delimited list of caller IDs. The list of 
        caller IDs in order of descending priority.(String)
    gtDecisionMethod: The merge rule for calculating 
        ensemble-merged GT and AD tags. Valid options are priority, 
        prioritySkipMissing, and majority_priorityOnTies. Default 
        is simple priority.(String)
    ignoreSampleIds: If this flag is set, then sample IDs will be 
        ignored and each VCF will be assumed to have the exact same 
        samples in the exact same order. Use at your own risk.(flag)
    ignoreSampleOrder: If this flag is set, then the sample IDs 
        will be used to match up the different VCFs, and the 
        samples may be in different orders in the different 
        files.(flag)

##### Filtering



### keepVariants

>  This function drops variants based on a given true/false expression\.


    expr: (String)

### extractRegion

>  This function extracts a single region from the VCF\. NOTE: the VCF MUST BE SORTED\!


    region: The genomic region to extract.(String, required)
    windowSize: The size of the window around the genomic region to 
        extract.(Int)

### dropNullVariants

>  This function drops all lines with no alt alleles \('\.' in the ALT column\), or lines where the ALT allele is identical to the REF\. Note: you must split multiallelics first\. See the 'splitMultiallelics' function\.


    (This function takes no parameters)

### dropSpanIndels

>  This function drops Spanning indel lines \('\*' alleles\)\. Note: you must split multiallelics first\!


    (This function takes no parameters)

### rmDup

>  This utility detects identical variant lines and deletes any excess beyond the first\. NOTE: VCF FILE MUST BE SORTED\!


    (This function takes no parameters)

### dropSymbolicAlleles

>  This utility strips all symbolic alleles\. See the VCF v4\.2 specification for more information on what those are and what they are used for\. Many older tools will return errors if fed symbolic alleles\.


    (This function takes no parameters)

### dropVariantsWithNs

>  This utility drops variants if they contain Ns in either the REF or ALT columns\.


    (This function takes no parameters)

##### Genomic Locus Annotation



### homopolymerRunStats

>  This tool adds several new INFO tags that indicate whether the variant is near a homopolymer run, and if so, whether it extends or truncates that run\.


    runSize: The number of repeated bases required to count as a 
        homopolymer run(String, required)
    genomeFA: The genome fasta file containing the reference 
        genome. This will be used by various functions that require 
        genomic information. Note that some functions that call the 
        GATK library will also require that the fasta be indexed. 
        Note: Chromosome names must match.(String, required)

### addContextBases

>  This function adds several new INFO fields which list the base pairs flanking the variant\.


    windowSize: The number of bases to include in the context 
        window(String, required)
    genomeFA: The genome fasta file containing the reference 
        genome. This will be used by various functions that require 
        genomic information. Note that some functions that call the 
        GATK library will also require that the fasta be indexed. 
        Note: Chromosome names must match.(String, required)

### addTrinucleotideComplexity

>  This function adds a new INFO field containing the trinucleotide complexity, defined as the sum of the squares of the proportions of each 3\-bp combination\.


    windowSize: The number of bases to include in the context 
        window(String, required)
    genomeFA: The genome fasta file containing the reference 
        genome. This will be used by various functions that require 
        genomic information. Note that some functions that call the 
        GATK library will also require that the fasta be indexed. 
        Note: Chromosome names must match.(String, required)

### gcContext

>  This function calculates the fraction of bases within k bases from the variant locus that are G or C\. This can be useful to identify high\-GC areas where variant calling and sequencing may be less accurate\.


    windowSize: The number of bases to include in the context 
        window for determining local gc content.(String, required)
    digits: Number of digits to round to.(String, default=4)
    genomeFA: The genome fasta file containing the reference 
        genome. This will be used by various functions that require 
        genomic information. Note that some functions that call the 
        GATK library will also require that the fasta be indexed. 
        Note: Chromosome names must match.(String, required)

### checkReferenceMatch

>  This function compares the REF column to the genomic reference and makes sure that they actually match\. If mismatches are found, a warning will be thrown\. In addition, a new INFO field will be added to the VCF that will be a simple integer field that will equal 1 if and only if the REF matches the reference, and 0 otherwise\.


    genomeFA: The genome fasta file containing the reference 
        genome. This will be used by various functions that require 
        genomic information. Note that some functions that call the 
        GATK library will also require that the fasta be indexed. 
        Note: Chromosome names must match.(String, required)

##### Data/Table Extraction



### tally

>  This is a set of functions that takes various counts and totals across the whole VCF\.


    func: (String, required)

### calculateMatchMatrix

>  \.\.\.\.


    file: (String, required)
    gtTag: The genotype FORMAT field.(String, default=GT)
    matchCutoff: matches below this threshold will not be written 
        to file.(Float, default=0.5)

### calcBurdenCounts

>  This function generates the \.


    geneTag: This is the INFO tag that indicates the geneID. It can 
        be a comma-delimited list.(String, required)
    expr: This is a true/false variant expression. Variants will 
        only be counted towards a burden test if they pass this 
        expression. This can be used to generate several burden 
        count tables with different filtering strategies in a 
        single run.(String)
    sampleSet: This is a list of samples to include. Samples not on 
        this list will be ignored.(String)
    group: This is a sample group to include. Samples that are not 
        in this sample group will be ignored. Note that this 
        requires the groupFile variable to be set.(String)
    inputGT: This is the FORMAT column to use as the genotype 
        column.(String)
    groupFile: A tab-delimited file containing sample ID's and a 
        list of group IDs for each sample. See the --groupFile 
        parameter of walkVcf.(String)
    countFileID: If multiple output count files are desired, you 
        can specify which functions output to which count file 
        using this parameter. Note that each file must be created 
        using a --burdenCountsFile parameter, with the form 
        fileID:/path/to/file.txt(String)

### calcBurdenCountsByGroups

>  


    geneTag: This is the INFO tag that indicates the geneID. It can 
        be a comma-delimited list.(String, required)
    groups: This is a comma delimited list of sample groups, taken 
        from the groupFile.(String, required)
    expr: This is a true/false variant expression. Variants will 
        only be counted towards a burden test if they pass this 
        expression. This can be used to generate several burden 
        count tables with different filtering strategies in a 
        single run.(String)
    sampleSet: This is a list of samples to include. Samples not on 
        this list will be ignored.(String)
    inputGT: This is the FORMAT column to use as the genotype 
        column.(String)
    groupFile: A tab-delimited file containing sample ID's and a 
        list of group IDs for each sample. See the --groupFile 
        parameter of walkVcf.(String)
    countFileID: If multiple output count files are desired, you 
        can specify which functions output to which count file 
        using this parameter. Note that each file must be created 
        using a --burdenCountsFile parameter, with the form 
        fileID:/path/to/file.txt(String)

### calcBurdenMatrix

>  \.\.\.\.


    geneTag: (String, required)
    expr: (String)
    sampleSet: (String)
    group: (String)
    inputGT: (String)
    geneList: (String)
    geneListFile: (String)
    printFullGeneList: (flag)
    pathwayList: (String)
    groupFile: A tab-delimited file containing sample ID's and a 
        list of group IDs for each sample. See the --groupFile 
        parameter of walkVcf.(String)
    outfile: The output matrix file path.(String, required)

##### File Formatting/Conversion



### convertSampleNames

>  This function converts the sample IDs of the VCF file according to a decoder file that you supply\. Your decoder should be a tab\-delimited text file with at least 2 columns\. One column should specify the FROM sample names as they currently appear in the VCF, and one should specify the new sample names you want them converted TO\. You must specify which of these columns is which using either the columnNames or columnIdx parameters\. 


    file: A tab delimited file with the from/to chromosome 
        names.(String, required)
    columnNames: The column titles for the old chrom names and the 
        new chrom names, in that order. If this parameter is used, 
        the decoder file must have a title line.(String)
    columnIdx: The column number of the current chromosome names 
        then the new chromosome names, in that order. Column 
        indices start counting from 0. If you use this parameter to 
        set the columns, and if the file has a title line, then you 
        should use skipFirstRow or else it will be read in as if it 
        were a chromosome.(Integer)
    skipFirstRow: If this parameter is set, then this tool will 
        skip the first line on the decoder file. This is useful if 
        you are specifying the columns using column numbers but the 
        file also has a title line.(Flag)

### convertChromNames

>  This function takes a file and translates chromosome names into a different format\. This is most often used to convert between the chr1,chr2,\.\.\. format and the 1,2,\.\.\. format\.


    file: A tab delimited file with the from/to chromosome 
        names.(String, required)
    columnNames: The column titles for the old chrom names and the 
        new chrom names, in that order. If this parameter is used, 
        the decoder file must have a title line.(String)
    columnIdx: The column number of the current chromosome names 
        then the new chromosome names, in that order. Column 
        indices start counting from 0. If you use this parameter to 
        set the columns, and if the file has a title line, then you 
        should use skipFirstRow or else it will be read in as if it 
        were a chromosome.(Integer)
    skipFirstRow: If this parameter is set, then this tool will 
        skip the first line on the decoder file. This is useful if 
        you are specifying the columns using column numbers but the 
        file also has a title line.(Flag)

### sampleReorder

>  This function allows you to reorder the sample columns in your VCF\. Set ONE of the parameters below to specify the desired ordering\.


    sampleOrdering: A simple list of all the samples, in the 
        desired order.(String)
    sampleOrderingFile: A file containing one sampleID per line. 
        The samples will be reordered to match the order found in 
        the file.(String, required)
    alphabetical: If this flag is set, then the samples will be 
        reordered alphabetically.(Flag)

### filterTags

>  This function can be used to remove unwanted INFO or FORMAT fields, or remove unwanted samples\. This can substantially reduce file sizes\.


    FORMAT.keep: (String)
    FORMAT.drop: (String)
    INFO.keep: (String)
    INFO.drop: (String)
    SAMPLES.keep: (String)
    SAMPLES.drop: (String)

### sanitize

>  This function strips out additional optional fields in the INFO lines which are technically valid according to the VCF specification, but that will be rejected by certain applications including GATK\.


    (This function takes no parameters)

### convertToStdVcf

>  Certain utilities \(eg GATK\) do not allow certain optional features of the VCFv4\.2 format standard \(For example: additional tag\-pairs in the INFO or FORMAT header lines\)\. This function strips out this additional metadata\.


    (This function takes no parameters)

### dropGenotypeData

>  This utility drops the entire genotype table, including all columns from the FORMAT column on\.


    (This function takes no parameters)

### addDummyGenotypeColumn

>  This utility adds a new genotype column and FORMAT column, containing a simple GT field that is always 0/1\. Some utilities will refuse to process files without genotype data or will ignore VCF lines with no alt genotypes\.


    (This function takes no parameters)

### copyColumnToInfo

>  This utility copies the contents of one of the VCF columns to a new INFO field\.


    columnID: (String, required)

# INFO TAG FUNCTIONS

    
    Info Tag Functions are simple modular functions that take one 
        variant at a time and add a new INFO field.
    Basic Syntax:
    --FCN addInfoTag|newTagID|fcn=infoTagFunction|params=p1,p2,... 
        

## Available Functions:

    

### LN\(x\)

    
    Input should be a numeric INFO field. output will be the 
        natural log of that field.
    x (INT|FLOAT|INFO:Int|INFO:Float) 

### MULT\(x,y\)

    
    Input should be a pair of info fields and/or numeric constants 
        (which must be specified as CONST:n). Output field will be 
        the product of the two inputs. Missing INFO fields will be 
        treated as ZEROS unless all params are INFO fields and all 
        are missing, in which case the output will be missing. 
        Output field type will be an integer if all inputs are 
        integers and otherwise a float.
    x (INT|FLOAT|INFO:Int|INFO:Float) 
    y (INT|FLOAT|INFO:Int|INFO:Float) 

### DIFF\(x,y\)

    
    Input should be a pair of info fields and/or numeric constants 
        (which must be specified as CONST:n). Output field will be 
        the difference of the two inputs (ie x - y). Missing INFO 
        fields will be treated as ZEROS unless all params are INFO 
        fields and all are missing, in which case the output will 
        be missing. Output field type will be an integer if all 
        inputs are integers and otherwise a float.
    x (INT|FLOAT|INFO:Int|INFO:Float) 
    y (INT|FLOAT|INFO:Int|INFO:Float) 

### SUM\(x\.\.\.\)

    
    Input should be a set of info tags and/or numeric constants 
        (which must be specified as CONST:n). Output field will be 
        the sum of the inputs. Missing INFO fields will be treated 
        as zeros unless all params are INFO fields and all are 
        missing, in which case the output will be missing. Output 
        field type will be an integer if all inputs are integers 
        and otherwise a float.
    x... (INT|FLOAT|INFO:Int|INFO:Float) 

### FLAGSET\(x\.\.\.\)

    
    Input should be a set of infoFields and optionally a name, with 
        the format tagID:name or just tagID. If names are omitted, 
        then the name will be equal to the tagID. Output field will 
        be the set of names for which the respective info field is 
        equal to 1. Any value other than 1, including missing 
        fields, will be treated as 0.
    x... (INFO:Int) 

### LEN\(x\)

    
    The new field will be an integer field equal to the length of 
        the input field. Will be missing if the input field is 
        missing.
    x (INFO:String,INFO:Int,INFO:Float) 

### RANDFLAG\(x,seed\)

    
    
    x (FLOAT) 
    seed (INT) 

### DIV\(x,y\)

    
    Input should be a pair of info fields and/or numeric constants 
        (which must be specified as CONST:n). Output field will be 
        the product of the two inputs. Missing INFO fields will be 
        treated as ZEROS unless all params are INFO fields and all 
        are missing, in which case the output will be missing. 
        Output field type will be a float.
    x (INT|FLOAT|INFO:Int|INFO:Float) 
    y (INT|FLOAT|INFO:Int|INFO:Float) 

### MIN\(x\.\.\.\)

    
    
    x... (INT|FLOAT|INFO:Int|INFO:Float) 

### CONVERT\.TO\.INT\(x,defaultValue\)

    
    Input should be an INFO field. Converts field to a Integer.
    x (INFO:String) 
    defaultValue (Optional) (Int) 

### CONVERT\.TO\.FLOAT\(x,defaultValue\)

    
    Input should be an INFO field. Converts to a numeric float. If 
        no defaultValue is supplied then non-floats will be 
        dropped. Note that NaN and Inf will be dropped / replaced 
        with the default.
    x (INFO:String) 
    defaultValue (Optional) (Float) 

### EXPR\(expr\)

    
    The new field will be an integer field which will be equal to 1 
        if and only if the expression is TRUE, and 0 otherwise. See 
        the expression format definition for more information on 
        how the logical expression syntax works.
    expr (STRING) 

### DECODE\(x,decoder\)

    
    
    x (INFO:String) 
    decoder (FILE:String) 

### CONCAT\(x\.\.\.\)

    
    This simple function concatenates the values of the input 
        parameters. Input parameters can be any combination of INFO 
        fields or constant strings.
    x... (String|INFO:String) 

### LOG10\(x\)

    
    Input should be a numeric INFO field. output will be the log10 
        of that field.
    x (INT|FLOAT|INFO:Int|INFO:Float) 

### PRODUCT\.ARRAY\(x\.\.\.\)

    
    Input should be a set of info fields and/or numeric constants 
        (which must be specified as CONST:n). Output field will be 
        the product of the inputs. Missing INFO fields will be 
        treated as ones unless all params are INFO fields and all 
        are missing, in which case the output will be missing. 
        Output field type will be an integer if all inputs are 
        integers and otherwise a float.
    x... (INT|FLOAT|INFO:Int|INFO:Float) 

### SETS\.DIFF\(x,y\)

    
    Input should be a pair of sets that are either INFO fields 
        specified as INFO:tagID, text files specified as 
        FILE:fileName, or a constant set delimited with 
        colons.Output field will be a comma delimited string 
        containing the elements in the first set with the second 
        set subtracted out.
    x 
        (String|INFO:String|INT|FLOAT|INFO:Int|INFO:Float|FILE:Stri-
        ng)
    y 
        (String|INFO:String|INT|FLOAT|INFO:Int|INFO:Float|FILE:Stri-
        ng)

### SWITCH\.EXPR\(expr,A,B\)

    
    Switches between two options depending on a logical expression. 
        The 'expr' expression parameter must be formatted like 
        standard variant-level expressions. The A and B parameters 
        can each be either a constant or an INFO field. The output 
        field will be equal to A if the logical expression is TRUE, 
        and otherwise will be B.
    expr (STRING) 
    A (INFO:Int|INFO:Float|INFO:String|Int|Float|String) 
    B (Optional) (INFO:Int|INFO:Float|INFO:String|Int|Float|String) 
        

### GT\.EXPR\(gtExpr,varExpr\)

    
    The new field will be an integer field which will be equal to 
        the number of samples that satisfy a given genotype-level 
        expression. See the expression format definition for more 
        information on how the logical expression syntax works. You 
        can also specify a variant-level expression which, if 
        false, will return missing.
    gtExpr (Optional) (STRING) 
    varExpr (Optional) (STRING) 

### MAX\(x\.\.\.\)

    
    
    x... (INT|FLOAT|INFO:Int|INFO:Float) 

### PICK\.RANDOM\(seed,x,y\.\.\.\)

    
    The first parameter must be either '.' or a supplied random 
        seed for the random number generator. You can then provide 
        either a single additional parameter and the output field 
        will be a randomly picked element from that parameter. In 
        this case the output will be chosen from this one input 
        parameter (which is assumed to be a list of some sort), 
        which can be a string constant list delimited with colons 
        and beginning with CONST:, an INFO field, or a text file 
        specified as FILE:filename. Alternately: you can provide 
        several additional parameters, in which case it will select 
        randomly from the set of parameters.
    seed (String) 
    x (String|INFO:String|FILE:String) 
    y... (Optional) (String|INFO:String) 

### SETS\.UNION\(x\.\.\.\)

    
    The new field will be equal to the union of the inputs. Inputs 
        can either be INFO fields specified with 'INFO:tagName', 
        can point to a text file with 'FILE:filename', or can be 
        constants (delimited with colons). The output will be the 
        union of the given parameters, in alphabetical order.
    x... (String|INFO:String|FILE:String) 

### COPY\(oldField\)

    
    
    oldField (INFO:INT|INFO:Float|INFO:String) 

### SETS\.INTERSECT\(x\.\.\.\)

    
    Input should be a pair of sets that are either INFO fields 
        specified as INFO:tagID, text files specified as 
        FILE:fileName, or a constant set delimited with colons. 
        Output field will be a comma delimited string containing 
        the intersect between the supplied sets.
    x... (String|INFO:String|FILE:String) 

### CONST\(x\)

    
    Input should be a simple string of characters
    x (STRING) # VCF Line Filter Expressions



Variant expressions are logical expressions that are performed at 
the variant level\. They are used by several parts of vArmyKnife, 
usually when filtering or differentiating variants based on it's 
properties/stats\. For any given variant, a variant expression will 
return either TRUEor FALSE\. Variant expressions are parsed as a 
series of logical functionsconnected with AND, OR, NOT, and 
parentheses\. All expressions MUSTbe separated with whitespace, 
though it does not matter how muchwhitespace or what kind\. 
Alternatively, expressions can be readdirectly from file by setting 
the expression toEXPRESSIONFILE:filepath\.

Variant Expression functions are all of the 
formatFILTERNAME:PARAM1:PARAM2:etc\. Some filters have no 
parameters; other filters can accept a variable number of 
parameters\. Allexpression functions return TRUE or FALSE\. Filters 
can be inverted using theNOT operator before the filter \(with 
whitespace in between\)\.
### True/False Functions:

#### ALT\.eq:k

> TRUE iff the first ALT allele equals k\.

#### ALT\.isOneOf:k1:k2:...

> TRUE iff the first ALT allele is one of k1,k2,\.\.\.

#### ALT\.len\.eq:k

> TRUE iff the first ALT allele is of length k\.

#### ALT\.len\.gt:k

> TRUE iff the first ALT allele is of length gt k\.

#### AnyGtNonRef:gtTag

> TRUE iff gtTag has an alt allele for any sample\. gtTag must be a GT\-formatted genotype field\.

#### AnyGtPass:simpleGtFiltExpression:k1:...

> TRUE iff any one of the samples pass the supplied GT filter\.

#### CHROM\.inAnyOf:chrX:...

> TRUE iff the variant is one one of the given chromosomes

#### FALSE:

> Never TRUE

#### FILTER\.eq:k

> TRUE iff the FILTER column is equal to k\.

#### FILTER\.ne:k

> TRUE iff the FILTER column is not equal to k\.

#### GENO\.MAFgt:t:k

> TRUE iff the genotype\-field tag t is a genotype\-style\-formatted field and the minor allele frequency is greater than k\.

#### GENO\.MAFlt:t:k

> TRUE iff the genotype\-field tag t is a genotype\-style\-formatted field and the minor allele frequency is less than k\.

#### GENO\.hasTagPairGtStyleMismatch:t1:t2

> TRUE iff the genotype\-field t1 and t2 are both found on a given line and have at least 1 sample where both tags are not set to missing but they do not have the same value\.

#### GENO\.hasTagPairMismatch:t1:t2

> TRUE iff the genotype\-field t1 and t2 are both found on a given line but are not always equal for all samples\.

#### GTAG\.any\.gt:gtTag:k

> TRUE iff any one of the samples have a value for their genotype\-tag entry greater than k\.

#### INFO\.any\.gt:t:k

> TRUE iff INFO field t is nonmissing and less than or equal to k\.

#### INFO\.any\.lt:t:k

> TRUE iff INFO field t is nonmissing and less than or equal to k\.

#### INFO\.eq:t:k

> TRUE iff INFO field t is nonmissing and equal to k\.

#### INFO\.ge:t:k

> TRUE iff INFO field t is nonmissing and greater than or equal to k\.

#### INFO\.gem:t:k

> TRUE iff INFO field t is missing or greater than or equal to k\.

#### INFO\.gt:t:k

> TRUE iff INFO field t is nonmissing and greater than k\.

#### INFO\.gtm:t:k

> TRUE iff INFO field t is missing or greater than k\.

#### INFO\.in:t:k

> TRUE iff INFO field t is a comma delimited list that contains string k\.

#### INFO\.inAny:t:k

> TRUE if INFO field t is a list delimited with commas and bars, and contains string k\.

#### INFO\.inAnyOf:t:k1:k2:...

> TRUE iff INFO field t is a list delimited with commas and bars, and contains any of the parameters k1,k2,\.\.\.

#### INFO\.inAnyOfN:t:k1:k2:...

> TRUE iff INFO field t is a list delimited with commas, bars, slashes, OR COLONS, and contains any of the parameters k1,k2,\.\.\.

#### INFO\.inAnyOfND:t:k1:k2:...

> TRUE iff INFO field t is a list delimited with commas, bars, slashes, colons, or dashes, and contains any of the parameters k1,k2,\.\.\.

#### INFO\.le:t:k

> TRUE iff INFO field t is nonmissing and less than or equal to k\.

#### INFO\.lem:t:k

> TRUE iff INFO field t is missing or less than or equal to k

#### INFO\.len\.eq:t:k

> TRUE iff INFO field t is nonmissing and has length equal to k\.

#### INFO\.len\.gt:t:k

> TRUE iff INFO field t is nonmissing and has length greater than k\.

#### INFO\.len\.lt:t:k

> TRUE iff INFO field t is nonmissing and has length less than k\.

#### INFO\.lt:t:k

> TRUE iff INFO field t is nonmissing and less than k\.

#### INFO\.ltm:t:k

> TRUE iff INFO field t is missing or less than k\.

#### INFO\.m:t

> TRUE iff INFO field t is missing\.

#### INFO\.mempty:t

> TRUE iff INFO field t is missing or less than or equal to k

#### INFO\.ne:t:k

> TRUE iff INFO field t is either missing or not equal to k\.

#### INFO\.nm:t

> TRUE iff INFO field t is nonmissing\.

#### INFO\.notIn:t:k

> TRUE iff INFO field t is missing or is a comma delimited list that does NOT contain string k\.

#### INFO\.notInAny:t:k

> TRUE if INFO field t is a list delimited with commas and bars, and does not contain string k\.

#### INFO\.subsetOf:t:k1:k2:...

> TRUE iff INFO field t is a comma delimited list and is a subset of k1,k2,etc

#### INFO\.subsetOfFileList:t:f

> TRUE iff INFO field t is a comma delimited list and is a subset of the list contained in file f

#### INFO\.tagsDiff:t1:t2

> TRUE iff the INFO\-field t1 and t2 are different, including when one is missing and the other is not\.

#### INFO\.tagsMismatch:t1:t2

> TRUE iff the INFO\-field t1 and t2 are both found on a given line but are not equal\.

#### LOCUS\.eq:chrom:pos

> TRUE if the variant is at the given chromosome and position

#### LOCUS\.near:k:chrom:pos

> TRUE if the variant is within k bases from the given chromosome and position

#### LOCUS\.range:chrom:from:to

> TRUE if the variant is at the given chromosome and between the given positions \(0\-based\)

#### POS\.gt:pos

> TRUE iff the variant is at a position greater than the given position

#### POS\.inAnyOf:pos1:...

> TRUE iff the variant is at one of the given positions

#### QUAL\.gt:k

> TRUE iff the QUAL column is greater than k\.

#### QUAL\.gtm:k

> TRUE iff the QUAL column is greater than k, OR qual is missing\.

#### REF\.eq:k

> TRUE iff the REF allele equals k\.

#### REF\.isOneOf:k1:k2:...

> TRUE iff the REF allele is one of k1,k2,\.\.\.

#### REF\.len\.eq:k

> TRUE iff the REF allele is of length k\.

#### REF\.len\.gt:k

> TRUE iff the REF allele is of length gt k\.

#### TRUE:

> Always TRUE

#### allelesHaveNoNs:

> FALSE iff the variant has unknown bases, ie N, in the ALT or REF alleles\.

#### isSNV:

> TRUE iff the variant is an SNV\.

#### isVariant:

> FALSE iff the variant has no alt alleles, or if the only alt allele is exactly equal to the ref allele\.

#### simpleSNV:

> TRUE iff the variant is a biallelic SNV\.

# Genotype Filter Expressions



Variant expressions are logical expressions that are performed at 
the variant level\. They are used by several parts of vArmyKnife, 
usually when filtering or differentiating variants based on it's 
properties/stats\. For any given variant, a variant expression will 
return either TRUEor FALSE\. Variant expressions are parsed as a 
series of logical functionsconnected with AND, OR, NOT, and 
parentheses\. All expressions MUSTbe separated with whitespace, 
though it does not matter how muchwhitespace or what kind\. 
Alternatively, expressions can be readdirectly from file by setting 
the expression toEXPRESSIONFILE:filepath\.

Variant Expression functions are all of the 
formatFILTERNAME:PARAM1:PARAM2:etc\. Some filters have no 
parameters; other filters can accept a variable number of 
parameters\. Allexpression functions return TRUE or FALSE\. Filters 
can be inverted using theNOT operator before the filter \(with 
whitespace in between\)\.
### True/False Functions:

#### FALSE:

> Never pass

#### GTAG\.SC\.altDepth\.gt:splitIdxTag:t:v

> TRUE iff the tag t, which must be a single\-caller\-AD\-style\-formatted field, has an observed alt\-allele\-frequency greater than k\.

#### GTAG\.SC\.altDepth\.lt:splitIdxTag:t:v

> TRUE iff the tag t, which must be a single\-caller\-AD\-style\-formatted field, has an observed alt\-allele\-frequency greater than k\.

#### GTAG\.SC\.altProportion\.gt:splitIdxTag:t:v

> TRUE iff the tag t, which must be a single\-caller\-AD\-style\-formatted field, has an observed alt\-allele\-frequency greater than k\.

#### GTAG\.SC\.altProportion\.lt:splitIdxTag:t:v

> TRUE iff the tag t, which must be a single\-caller\-AD\-style\-formatted field, has an observed alt\-allele\-frequency greater than k\.

#### GTAG\.altDepthForAlle\.gt:gt:ad:v

> TRUE iff for AD\-style tag ad and GT\-style tag gt, the sample is called as having an allele K while having less than v reads covering said allele\.

#### GTAG\.altProportion\.gt:t:k

> TRUE iff the tag t, which must be a AD\-style\-formatted field, has an observed alt\-allele\-frequency greater than k\.

#### GTAG\.altProportion\.lt:t:k

> TRUE iff the tag t, which must be a AD\-style\-formatted field, has an observed alt\-allele\-frequency less than k\.

#### GTAG\.eq:t:s

> TRUE iff GT field t equals the string s\. DROP if tag t is not present or set to missing\.

#### GTAG\.ge:t:k

> TRUE iff tag t is present and not set to missing, and is a number greater than or equal to k\.

#### GTAG\.gem:t:k

> TRUE iff tag t is either not present, set to missing, or a number greater than or equal to k\.

#### GTAG\.gt:t:k

> TRUE iff tag t is present and not set to missing, and is a number greater than k\.

#### GTAG\.gtm:t:k

> TRUE iff tag t is either not present, set to missing, or a number greater than k\.

#### GTAG\.isAnyAlt:t

> TRUE iff the tag t, which must be a genotype\-style\-formatted field, is present and not set to missing and contains the alt allele\.

#### GTAG\.isCleanHet:t

> TRUE iff the tag t, which must be a genotype\-style\-formatted field, is present and not set to missing and is heterozygous between the alt and reference allele\.

#### GTAG\.isHet:t

> TRUE iff the tag t, which must be a genotype\-style\-formatted field, is present and not set to missing and is heterozygous\.

#### GTAG\.isHomAlt:t

> TRUE iff the tag t, which must be a genotype\-style\-formatted field, is present and not set to missing and is homozygous\-alt\.

#### GTAG\.isHomRef:t

> TRUE iff the tag t, which must be a genotype\-style\-formatted field, is present and not set to missing and is homozygous\-reference\.

#### GTAG\.le:t:k

> TRUE iff tag t is present and not set to missing, and is a number less than or equal to k\.

#### GTAG\.lem:t:k

> TRUE iff tag t is either not present, set to missing, or a number less than or equal to k\.

#### GTAG\.lt:t:k

> TRUE iff tag t is present and not set to missing, and is a number less than k\.

#### GTAG\.ltm:t:k

> TRUE iff tag t is either not present, set to missing, or a number less than k\.

#### GTAG\.m:t:k

> TRUE iff the GT field t is is not present or set to missing\.

#### GTAG\.ne:t:k

> TRUE iff GT field t does not equal the string s\. DROP if tag t is not present or set to missing\.

#### GTAG\.nm:t

> TRUE iff the GT field t is present and not set to missing\.

#### GTAGARRAY\.gt:t:i:k

> TRUE iff the tag t is present and not set to missing, and is a list with at least i elements, and the i\-th element of which is greater than k\.

#### GTAGARRAY\.lt:t:i:k

> TRUE iff the tag t is present and not set to missing, and is a list with at least i elements, and the i\-th element of which is less than k\.

#### GTAGARRAYSUM\.gt:t:k

> TRUE iff the tag t is present and not set to missing, and is a list of numbers the sum of which is greater than k\.

#### SAMPGRP\.in:g

> TRUE iff the sample is a member of group g\.

#### TAGPAIR\.match:t1:t2

> TRUE iff the two tags t1 and t2 are both present and not set to missing, and are equal to one another\.

#### TRUE:

> Always pass

#### VAREXPR:g

> TRUE iff simple variant\-level expression passes\.

        ## Secondary Commands:
        In addition to the standard command which parses a VCF or 
        variant table, vArmyKnife includes a few ancillary tools 
        which perform other tasks.
        These tools can be invoked with the command:
            varmyknife --CMD commandName [options]
        For more information see the [secondary command 
        page](secondaryCommands.html), or use the command:
            varmyknife --help CMD
        For a listing of all secondary commands, use the command: 
            varmyknife --help secondaryCommands## AUTHORS:

Stephen W\. Hartley, Ph\.D\. stephen\.hartley \(at nih dot gov\)

## LEGAL:

Written 2017\-2019 by Stephen Hartley, PhD  National Cancer Institute \(NCI\), Division of Cancer Epidemiology and Genetics \(DCEG\), Human Genetics Program As a work of the United States Government, this software package and all related documentation and information is in the public domain within the United States\. Additionally, the National Institutes of Health and the National Cancer Institute waives copyright and related rights in the work worldwide through the CC0 1\.0 Universal Public Domain Dedication \(which can be found at https://creativecommons\.org/publicdomain/zero/1\.0/\)\. Although all reasonable efforts have been taken to ensure the accuracy and reliability of the software and data, the National Human Genome Research Institute \(NHGRI\), the National Cancer Institute \(NCI\) and the U\.S\. Government does not and cannot warrant the performance or results that may be obtained by using this software or data\. NHGRI, NCI and the U\.S\. Government disclaims all warranties as to performance, merchantability or fitness for any particular purpose\. In work or products derived from this material, proper attribution of the authors as the source of the software or data may be made using "NCI Division of Cancer Epidemiology and Genetics, Human Genetics Program" as the citation\. This package uses \(but is not derived from\) several externally\-developed, open\-source libraries which have been distributed under various open\-source licenses\. vArmyKnife is distributed with compiled versions of these packages\. Additional License information can be accessed using the command:     vArmyKnife \-\-help LICENSES And can also found in the distributed source code in:     src/main/resources/library\.LICENSES\.txt

