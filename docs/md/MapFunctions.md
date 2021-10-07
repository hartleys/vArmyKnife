
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

>  This is a set of functions that all take one or more input parameters and outputs one new INFO field\. The syntax is: \-\-fcn "addInfo|newTagName|fcn\(param1,param2,\.\.\.\)"\. Optionally you can add "|desc=tag description"\. There are numerous addInfo functions\. See the section in the help doc titled INFO TAG FUNCTIONS, or use the help command: varmyknife help addInfo


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
    --fcn "addInfo|isRare|EXPR(INFO.lt:maxAF:0.01)"\
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

>  This utility creates a matrix file with 


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