
# VARIANT OPERATIONS

    
    Variant Operations or Variant Map Functions are a set of 
    sub\-utilities that perform operations on a variant set one 
    variant at a time\. When more than one function is specified in 
    a run, these functions are performed in the order that they 
    appear in the command line, after all other operations have 
    been carried out \(excluding output ops\)\.
    Basic Syntax:
        \-\-FCN functionName|ID|param1=p1|param2=p2|\.\.\.

## Available Functions:

    

### snpEffExtract

    
    
    annTag: A valid SnpSift command\(String, default=ANN\)
    bioTypeKeepList: \(String\)
    effectKeepList: \(String\)
    warningDropList: \(String\)
    geneListName: \(String\)
    geneList: \(String\)
    severityList: Must be a list of severity levels, listed as some 
    combination of effectseverity types delimited with slashes\. 
    Legal types are: HIGH, MODERATE, LOW, and MODIFIER, which are 
    standard SnpEFF effect types, and also: NS \(HIGH and 
    MODERATE\), NonNS \(LOW and MODIFIER\), and ANY \(any 
    type\)\.\(String, default=HIGH/MODERATE/LOW\)
    extractFields: This is a complex multi\-part field that allows 
    flexible extraction of information from SnpEff ANN tags\. This 
    field must be in the colon\-delimited format 
    tagInfix:ANN\_idx:description:severityList\[:noCollapse\]\. 
    severityList must be in the same format as the severityList 
    parameter above, but can override the default if desired\. 
    ANN\_idx must be a slash\-delimited list of field indices 
    counting from zero in the ANN tag\. The standard ANN field 
    indices are: 0:allele,1:effect,2:impact,3:geneName,4:geneID,5:t-
    xType,6:txID,7:txBiotype,8:rank,9:HGVS\.c,10:HGVS\.p,11:cDNApos-
    ition,12:cdsPosition,13:proteinPosition,14:distToFeature,15:war-
    nings,16:errors If multiple fields are selected then the output 
    fields will have the format first:second:third:etc\. For 
    example, to create two new fields containing a list of all 
    genes for which the current variant has HIGH and MODERATE 
    impact respectively, use the format: myNewField:4:my 
    description:HIGH/MODERATE\. This will generate two new fields: 
    myNewField\_HIGH and myNewField\_MODERATE\. Note that if this 
    function as a whole has a mapID set, then both field names will 
    be prefixed by that overall ID\.\(String\)
    geneNameIdx: \(String, default=4\)
    biotypeIdx: \(String, default=7\)
    warnIdx: \(String, default=15\)

### fixSwappedRefAlt

    
    \.\.\.
    genomeFA: \(String, required\)

### convertSampleNames

    
    This function converts the sample IDs of the VCF file according 
    to a decoder file that you supply\. The decoder will decode
    file: A tab delimited file with the from/to chromosome 
    names\.\(String, required\)
    columnNames: The column titles for the old chrom names and the 
    new chrom names, in that order\. If this parameter is used, the 
    decoder file must have a title line\.\(String\)
    columnIdx: The column number of the current chromosome names 
    then the new chromosome names, in that order\. Column indices 
    start counting from 0\. If you use this parameter to set the 
    columns, and if the file has a title line, then you should use 
    skipFirstRow or else it will be read in as if it were a 
    chromosome\.\(Integer\)
    skipFirstRow: If this parameter is set, then this tool will 
    skip the first line on the decoder file\. This is useful if you 
    are specifying the columns using column numbers but the file 
    also has a title line\.\(Flag\)

### tagBedFile

    
    This function takes a BED file \(which can be gzipped if 
    desired\) and creates a new INFO field based on whether the 
    variant locus overlaps with a genomic region in the BED file\. 
    The new field can be either an integer that is equal to 1 if 
    there is overlap and 0 otherwise \(which is the default 
    behavior\) Or, alternatively, it can copy in the title field 
    from the bed file\. NOTE: this function only uses the first 3 
    to 5 fields of the BED file, it does not implement the optional 
    fields 10\-12 which can specify intron/exon blocks\.
    file: \(String, required\)
    desc: \(String, default=No desc provided\)
    buffer: \(Integer, default=0\)
    style: \(String, default=\+\)

### checkReferenceMatch

    
    
    genomeFA: \(String, required\)

### keepVariants

    
    This function drops variants based on a given true/false 
    expression\.
    expr: \(String\)

### dropNullVariants

    
    

### fixFirstBaseMismatch

    
    \.\.\.
    windowSize: Sets the size of the sliding window used\. Problems 
    may occur if you have variants longer than this window size\. 
    Default is 200bp\.\(Int\)

### depthStats

    
    This function calculates various statistics on total read depth 
    and hetAB\.
    inputGT: The input genotype FORMAT field\.\(String\)
    inputAD: \(String\)
    inputDP: \(String\)
    restrictToGroup: \(String\)
    groupFile: A tab\-delimited file containing sample ID's and a 
    list of group IDs for each sample\. See the \-\-groupFile 
    parameter of walkVcf\.\(String\)
    superGroupList: See the \-\-superGroupList parameter of 
    walkVcf\.\(String\)

### snpSiftDbnsfp

    
    This function runs the SnpSift dbnsfp command
    cmd: A valid SnpSift command\(String\)

### calcBurdenMatrix

    
    \.\.\.\.
    geneTag: \(String, required\)
    expr: \(String\)
    sampleSet: \(String\)
    group: \(String\)
    inputGT: \(String\)
    geneList: \(String\)
    geneListFile: \(String\)
    printFullGeneList: \(flag\)
    pathwayList: \(String\)
    groupFile: A tab\-delimited file containing sample ID's and a 
    list of group IDs for each sample\. See the \-\-groupFile 
    parameter of walkVcf\.\(String\)
    superGroupList: See the \-\-superGroupList parameter of 
    walkVcf\.\(String\)
    outfile: The output matrix file path\.\(String, required\)

### splitMultiAllelics

    
    \.\.\.

### filterTags

    
    This function can be used to remove unwanted INFO or FORMAT 
    fields, or remove unwanted samples\. This can substantially 
    reduce file sizes\.
    FORMAT\.keep: \(String\)
    FORMAT\.drop: \(String\)
    INFO\.keep: \(String\)
    INFO\.drop: \(String\)
    SAMPLES\.keep: \(String\)
    SAMPLES\.drop: \(String\)

### gcContext

    
    
    windowSize: The number of bases to include in the context 
    window for determining local gc content\.\(String, required\)
    digits: Number of digits to round to\.\(String, default=4\)
    genomeFA: \(String, required\)

### addInfoTag

    
    This is a set of functions that all take one or more input 
    parameters and outputs one new INFO field\.
    func: \(String, required\)
    desc: The description in the header line for the new INFO 
    field\.\(String, default=No desc provided\)
    digits: For floating point values, the number of digits to 
    include after the decimal\(Integer\)

### tagVariantsExpression

    
    This function takes a variant expression and creates a new INFO 
    field that is 1 if and only if that expression returns TRUE, 
    and 0 otherwise\.
    expr: The variant expression, which is a true/false expression 
    using the variant expression syntax\.\(String, required\)
    desc: A description, to go in the info field 
    description\.\(String, default=No desc provided\)

### sanitize

    
    

### snpEff

    
    This function runs SnpEff
    cmd: A valid SnpSift command\(String\)

### getLocusDepthFromWig

    
    \.\.\.
    wigfile: \(String\)
    desc: \(String\)

### tagVariantsFunction

    
    This is a set of functions that all take one or more input 
    parameters and outputs one new INFO field\.
    func: \(String, required\)
    desc: \(String, default=No desc provided\)
    digits: \(Integer\)
    params: \(String\)

### convertToStdVcf

    
    Certain utilities \(eg GATK\) do not allow certain optional 
    features of the VCFv4\.2 format standard \(For example: 
    additional tag\-pairs in the INFO or FORMAT header lines\)\. 
    This function strips out this additional metadata\.

### snpEffExtractField

    
    
    tagPrefix: \(String, required\)
    outputTagPrefix: \(String\)
    columns: \(String\)
    desc: \(String\)
    collapseUniques: \(Flag\)
    tagSet: \(String\)

### snpSiftAnno

    
    This function runs a SnpSift anno command
    cmd: A valid SnpSift command\(String\)

### unPhaseAndSortGenotypes

    
    This function removes phasing and sorts genotypes\.
    inputGT: The input/output genotype FORMAT field\.\(String\)
    groupFile: A tab\-delimited file containing sample ID's and a 
    list of group IDs for each sample\. See the \-\-groupFile 
    parameter of walkVcf\.\(String\)
    superGroupList: See the \-\-superGroupList parameter of 
    walkVcf\.\(String\)

### calcStats

    
    This function combines the functions sampleCounts, sampleLists, 
    and depthStats, performing all three\.
    groupFile: A tab\-delimited file containing sample ID's and a 
    list of group IDs for each sample\. See the \-\-groupFile 
    parameter of walkVcf\.\(String\)
    superGroupList: See the \-\-superGroupList parameter of 
    walkVcf\.\(String\)
    inputGT: The input genotype FORMAT field\.\(String\)
    inputAD: \(String\)
    inputDP: \(\)
    noCountsCalc: \(Flag\)
    noFreqCalc: \(Flag\)
    noMissCalc: \(Flag\)
    noAlleCalc: \(Flag\)
    noHetHomCalc: \(Flag\)
    noMultiHetCalc: \(Flag\)
    samplePrintLimit: \(String\)
    noDepthStats: \(Flag\)
    noSampleLists: \(Flag\)
    noSampleCounts: \(Flag\)
    expr: The variant expression, which is a true/false expression 
    using the variant expression syntax\.\(String\)

### calcBurdenCounts

    
    
    geneTag: \(String, required\)
    expr: \(String\)
    sampleSet: \(String\)
    group: \(String\)
    inputGT: \(String\)
    groupFile: A tab\-delimited file containing sample ID's and a 
    list of group IDs for each sample\. See the \-\-groupFile 
    parameter of walkVcf\.\(String\)
    superGroupList: See the \-\-superGroupList parameter of 
    walkVcf\.\(String\)
    countFileID: If multiple output count files are desired, you 
    can specify which functions output to which count file using 
    this parameter\. Note that each file must be created using a 
    \-\-burdenCountsFile parameter, with the form 
    fileID:/path/to/file\.txt\(String\)

### markDup

    
    This map function will detect duplicate variant lines and add 
    two new INFO fields: mapID\_CT and mapID\_IDX\. The CT will 
    indicate how many duplicates were found matching the current 
    variant, and the IDX will number each duplicate with a unique 
    identifier, counting from 0\. All nonduplicates will be marked 
    with CT=1 and IDX=0\.

### homopolymerRunStats

    
    
    runSize: The number of repeated bases required to count as a 
    homopolymer run\(String, required\)
    genomeFA: \(String, required\)

### addDummyGenotypeColumn

    
    

### concordanceCaller

    
    \.\.\.\.
    callerNames: Comma delimited list of caller IDs, used in the 
    callerSet INFO fields and the names of the output GT fields\. 
    By default, callers will simply be named C1,C2,\.\.\.\(String\)
    priority: Comma delimited list of caller IDs\. The list of 
    caller IDs in order of descending priority\.\(String\)
    gtDecisionMethod: The merge rule for calculating 
    ensemble\-merged GT and AD tags\. Valid options are priority, 
    prioritySkipMissing, and majority\_priorityOnTies\. Default is 
    simple priority\.\(String\)
    ignoreSampleIds: If this flag is set, then sample IDs will be 
    ignored and each VCF will be assumed to have the exact same 
    samples in the exact same order\. Use at your own risk\.\(flag\)
    ignoreSampleOrder: If this flag is set, then the sample IDs 
    will be used to match up the different VCFs, and the samples 
    may be in different orders in the different files\.\(flag\)

### addContextBases

    
    
    windowSize: The number of bases to include in the context 
    window\(String, required\)
    genomeFA: \(String, required\)

### convertChromNames

    
    \.\.\.\.
    file: A tab delimited file with the from/to chromosome 
    names\.\(String, required\)
    columnNames: The column titles for the old chrom names and the 
    new chrom names, in that order\. If this parameter is used, the 
    decoder file must have a title line\.\(String\)
    columnIdx: The column number of the current chromosome names 
    then the new chromosome names, in that order\. Column indices 
    start counting from 0\. If you use this parameter to set the 
    columns, and if the file has a title line, then you should use 
    skipFirstRow or else it will be read in as if it were a 
    chromosome\.\(Integer\)
    skipFirstRow: If this parameter is set, then this tool will 
    skip the first line on the decoder file\. This is useful if you 
    are specifying the columns using column numbers but the file 
    also has a title line\.\(Flag\)

### addVariantIdx

    
    
    prefix: Prefix to prepend to the index field\.\(String\)

### dropSpanIndels

    
    

### addAltSequence

    
    
    windowSize: The number of flanking bases to include on each 
    side of the alt sequence\.\(String, default=10\)
    genomeFA: \(String, required\)

### sampleLists

    
    This function generates sample list fields which contain 
    comma\-delimited lists of samples that are het or hom\-alt\.
    inputGT: The input genotype FORMAT field\.\(String\)
    samplePrintLimit: \(String\)
    groupFile: A tab\-delimited file containing sample ID's and a 
    list of group IDs for each sample\. See the \-\-groupFile 
    parameter of walkVcf\.\(String\)
    superGroupList: See the \-\-superGroupList parameter of 
    walkVcf\.\(String\)
    expr: The variant expression, which is a true/false expression 
    using the variant expression syntax\.\(String\)

### fixDotAltIndels

    
    \.\.\.

### dropGenotypeData

    
    

### calcBurdenCountsByGroups

    
    
    geneTag: \(String, required\)
    groups: \(String, required\)
    expr: \(String\)
    sampleSet: \(String\)
    inputGT: \(String\)
    groupFile: A tab\-delimited file containing sample ID's and a 
    list of group IDs for each sample\. See the \-\-groupFile 
    parameter of walkVcf\.\(String\)
    superGroupList: See the \-\-superGroupList parameter of 
    walkVcf\.\(String\)
    countFileID: If multiple output count files are desired, you 
    can specify which functions output to which count file using 
    this parameter\. Note that each file must be created using a 
    \-\-burdenCountsFile parameter, with the form 
    fileID:/path/to/file\.txt\(String\)

### rmDup

    
    \.\.\.

### leftAlignAndTrim

    
    \.\.\.
    windowSize: Sets the size of the sliding window used\. Problems 
    may occur if you have variants longer than this window size\. 
    Default is 200bp\.\(Int\)
    genomeFA: \(String, required\)

### sampleCounts

    
    This function generates counts and frequencies for alt alleles, 
    alt genotypes, missing genotypes, ref genotypes, and so on\. 
    Note that there are several calc\- flags\. If none of these are 
    included, then this function does nothing\.
    groupFile: A tab\-delimited file containing sample ID's and a 
    list of group IDs for each sample\. See the \-\-groupFile 
    parameter of walkVcf\.\(String\)
    superGroupList: See the \-\-superGroupList parameter of 
    walkVcf\.\(String\)
    inputGT: The input genotype FORMAT field\.\(String\)
    noCountsCalc: \(Flag\)
    noFreqCalc: \(Flag\)
    noMissCalc: \(Flag\)
    noAlleCalc: \(Flag\)
    noHetHomCalc: \(Flag\)
    noMultiHetCalc: \(Flag\)

### dropSymbolicAlleles

    
    

### sampleReorder

    
    This function allows you to reorder the sample columns in your 
    VCF\. Set ONE of the parameters below to specify the desired 
    ordering\.
    sampleOrdering: A simple list of all the samples, in the 
    desired order\.\(String\)
    sampleOrderingFile: A file containing one sampleID per line\. 
    The samples will be reordered to match the order found in the 
    file\.\(String, required\)
    alphabetical: If this flag is set, then the samples will be 
    reordered alphabetically\.\(Flag\)

### genotypeFilter

    
    This function filters a genotype field based on a given 
    genotype expression\. The new filtered genotype can replace the 
    GT field or can be set to a different field, so multiple 
    filtering strategies can be included in a single VCF\.
    expr: A Genotype Expression, using the genotype expression 
    syntax\.\(String, required\)
    desc: A description, to go in the new FORMAT fields\.\(String\)
    filterTag: The name of a new FORMAT field, which will be a flag 
    equal to 1 if and only if the genotype passes the 
    filter\.\(String\)
    outputGT: The output genotype FORMAT field\. If this is the 
    same as the input genotype field then the genotype field will 
    be overwritten\.\(String\)
    inputGT: The input genotype FORMAT field\.\(String\)
    inputGtNewName: If this parameter is set, the input genotype 
    field will be copied to a new tag with this name before 
    filtering\. This can be useful if overwriting the input 
    genotype field\.\(\)
    groupFile: A tab\-delimited file containing sample ID's and a 
    list of group IDs for each sample\. See the \-\-groupFile 
    parameter of walkVcf\.\(String\)
    superGroupList: See the \-\-superGroupList parameter of 
    walkVcf\.\(String\)