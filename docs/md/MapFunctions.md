
# VARIANT OPERATIONS

    
    Variant Operations or Variant Map Functions are a set of sub-utilities that perform operations on 
        a variant set one variant at a time. When more than one function is specified in a run, these 
        functions are performed in the order that they appear in the command line, after all other 
        operations have been carried out (excluding output ops).
    Basic Syntax:
        --FCN functionName|ID|param1=p1|param2=p2|...
    
    functionName: one of the functions listed below.
    ID: The ID for this particular operation run. This will be used in warning/error messages and in 
        the header metadata. It is recommended that this ID be unique. In some functions this ID is 
        used to determine the INFO field names.
    param1,param2,...: Most functions take one or more parameters. Parameters are specified with the 
        format: param=value, where param is the parameter ID listed in the documentation below.

## Available Operations

    

##### Structural Variants



### annotateSVset

>  Requires that the inputs be structural variants annotated as per the VCF 4\.2 file specification\. Takes a second \(small\) SV set and add it to this SV file when matches are found\. Can be used to test true/false positives, compare methods, etc\. See also concordanceCallerSV which performs a similar function but is better suited for circumstances where you want to MERGE two SV sets, or if both SV sets are large\. 


    annofile: The file to annotate with. Must be relatively small, as all the data will be loaded 
        into memory.(String, required)
    copyOverInfoTags: A list of INFO tags from the annofile that you want copied over.(String)
    copyInfoPrefix: A prefix that will be prepended to every copied INFO tag in 
        copyOverInfoTags.(String)
    crossChromWin: For cross-chromosome SVs, the window within which to register a match.(Int)
    withinChromWin: For same-chromosome SVs (SVs where both ends of the breakend are on the same 
        chromosome), the window within which to register a match.(Int)
###### Example 1:
    This example annotates the current VCF file with the AC and AN fields from another VCF file. Note 
        that the annotation VCF file must be sorted and indexed using tabix.
    varmyknife walkVcf \
    --fcn "snpSiftAnno|gnomad|cmd=-info AC,AN -name GNOM_ /path/to/anno/file/gnomad.vcf.gz "\
    infile.vcf.gz outfile.vcf.gz
###### End Example

##### Structural Variant Tools



### dropInvalidSVBND

>  \.\.\.\.


    (This function takes no parameters)

### dropReverseSVbreakends

>  \.\.\.\.


    (This function takes no parameters)

### addReverseSVbreakends

>  \.\.\.\.


    (This function takes no parameters)

### convertSVtoBND

>  \.\.\.\.


    (This function takes no parameters)

##### General\-Purpose Tools



### addInfo

>  This is a set of functions that all take one or more input parameters and outputs one new INFO field\. The syntax is: \-\-fcn "addInfo|newTagName|fcn\(param1,param2,\.\.\.\)"\. Optionally you can add "|desc=tag description"\. There are numerous addInfo functions\. See the section in the help doc titled INFO TAG FUNCTIONS, or use the help command: varmyknife help addInfo


    func: (String, required)
    desc: The description in the header line for the new INFO field.(String, default=No desc 
        provided)
###### Example 1:
    Make a new INFO field which is the maximum from several allele frequencies (which are already in 
        the file) Then make a 0/1 INFO field that is 1 if the max AF is less than 0.01. Note the 
        CONST:0 term, which allows you to include constant values in these functions. In this case it 
        makes it so that if the AF is missing in all three populations, the maxAF will be 0 rather 
        than missing.
    varmyknife walkVcf \
    --fcn "addInfo|maxAF|MAX(CEU_AF,AFR_AF,JPT_AF,CONST:0)|\
    desc=The max allele frequency from CEU_AF, AFR_AF, or JPT_AF (or zero if all are missing)."\
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
    desc: The description in the header line for the new INFO field.(String, default=No desc 
        provided)
###### Example 1:
    This example makes a new FORMAT field which is the ratio between the coverage on the first ALT 
        allele and the total coverage across all alleles.
    varmyknife walkVcf \
    --fcn "addFormat|AlleleDepth_ALTALLE|extractIDX(AD,1)|\
    desc=The observed allele depth for the first alt allele."\
    --fcn "addFormat|AlleleDepth_TOTAL|SUM(AD)|\
    desc=The observed allele depth for the first alt allele."\
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


    groupFile: A tab-delimited file containing sample ID's and a list of group IDs for each sample. 
        See the --groupFile parameter of walkVcf.(String)
    inputGT: The input genotype FORMAT field.(String)
    noCountsCalc: If this is set, then no Ct fields will be generated.(Flag)
    noFreqCalc: If this is set, then no Freq fields will be generated.(Flag)
    noMissCalc: If this is set, then Ct and Freq fields will not be generated to count the number or 
        rate of missing genotypes.(Flag)
    noAlleCalc: If this is set, then Ct and Freq fields will not be generated to count allele counts 
        and frequencies.(Flag)
    noHetHomCalc: If this is set, then Ct and Freq fields will not be generated to count the number 
        of HomAlt and Het genotypes.(Flag)
    noMultiHetCalc: If this is set, then the number of multiallelic heterozygotes will not be 
        counted.(Flag)
    addOtherCountsCalc: If this is set, additional optional counts will be added.(Flag)
    expr: The variant expression, which is a true/false expression using the variant expression 
        syntax.(String)

### sampleLists

>  This function generates sample list fields which contain comma\-delimited lists of samples that are het or hom\-alt\.


    inputGT: The input genotype FORMAT field.(String)
    samplePrintLimit: (String)
    groupFile: A tab-delimited file containing sample ID's and a list of group IDs for each sample. 
        See the --groupFile parameter of walkVcf.(String)
    expr: The variant expression, which is a true/false expression using the variant expression 
        syntax.(String)

### depthStats

>  This function calculates various statistics on total read depth and hetAB\.


    inputGT: The input genotype FORMAT field.(String)
    inputAD: The input allele depth or AD field.(String)
    inputDP: The input total depth or DP field.(String)
    restrictToGroup: If this variable is set, then all stats will be restricted to the given sample 
        subgroup. Note that sample group information must be supplied either for this function or 
        globally using the --groupFile parameter.(String)
    groupFile: A tab-delimited file containing sample ID's and a list of group IDs for each sample. 
        See the --groupFile parameter of walkVcf.(String)

### calcStats

>  This function combines the functions sampleCounts, sampleLists, and depthStats, performing all three\.


    groupFile: A tab-delimited file containing sample ID's and a list of group IDs for each sample. 
        See the --groupFile parameter of walkVcf.(String)
    inputGT: The input genotype FORMAT field.(String)
    inputAD: The input allele depth or AD field.(String)
    inputDP: The input total depth or DP field.(String)
    noCountsCalc: If this is set, then no Ct fields will be generated.(Flag)
    noFreqCalc: If this is set, then no Freq fields will be generated.(Flag)
    noMissCalc: If this is set, then Ct and Freq fields will not be generated to count the number or 
        rate of missing genotypes.(Flag)
    noAlleCalc: If this is set, then Ct and Freq fields will not be generated to count allele counts 
        and frequencies.(Flag)
    noHetHomCalc: If this is set, then Ct and Freq fields will not be generated to count the number 
        of HomAlt and Het genotypes.(Flag)
    noMultiHetCalc: If this is set, then the number of multiallelic heterozygotes will not be 
        counted.(Flag)
    addOtherCountsCalc: If this is set, additional optional counts will be added.(Flag)
    samplePrintLimit: This limits the number of samples that will be listed in the SAMPLIST fields. 
        This can be useful to reduce file sizes and prevent problems when importing into excel due to 
        overly long fields.(String)
    noDepthStats: If this is set, depth statistic fields (including total depth, depth quantiles, and 
        hetAB stats) will not be created.(Flag)
    noSampleLists: If this is set, then SAMPLIST fields will not be generated.(Flag)
    noSampleCounts: If this is set, then sample count and frequency fields will not be 
        generated.(Flag)
    expr: The variant expression, which is a true/false expression using the variant expression 
        syntax.(String)

### addVariantIdx

>  This function adds a new INFO column with a unique numeric value for each line\. Optionally, you can add a prefix to each ID\.


    prefix: Prefix to prepend to the index field.(String)

### addVariantPosInfo

>  This function adds a new INFO field in the form: CHROM:START:REF>ALT\. This can be useful for checking the effects of functions that alter the variant columns\. For example, you can run this function before and after leftAlignAndTrim to see how a variant changes\.


    (This function takes no parameters)

### splitMultiNucleotideVariants

>  This function splits multinucleotide variants into separate SNVs\.This only modifies biallelic variants \(or split multiallelics\) in which the REF and ALT are the same length and that length is greater than 1\.


    biallelicOnly: If this flag is used, only biallelic variants will be split, not larger 
        variants.(flag)
    sortBufferWindowSize: (String, default=5000)

### markDup

>  This map function will detect duplicate variant lines and add two new INFO fields: mapID\_CT and mapID\_IDX\. The CT will indicate how many duplicates were found matching the current variant, and the IDX will number each duplicate with a unique identifier, counting from 0\. All nonduplicates will be marked with CT=1 and IDX=0\. VCF FILE MUST BE SORTED\!


    (This function takes no parameters)

### longInfoVcfToStandardFormatVcf

>  This map function is intended to convert a VCF that does not have a format field but  instead has duplicate lines when a variant appears in two samples\. One of the INFO fields must be a sample ID\. this function will copy the INFO fields to the genotype columns, using the sampField parameter to determine the sample column\.


    sampField: Indicates which INFO field contains the VCF line's sample ID.(String)
    sampList: The list of samples. Lines in which the infoField field does not match one of these 
        sample IDs will be ignored.(String)

### mergeDup

>  Merges duplicated lines\. NOTE: REQUIRES THE VCF TO BE SORTED\. NOTE: DOES NOT WORK ON GENOTYPES\.NOTE: Splitting multiallelics and left\-align\-and\-trim are also require for it to work properly with multiallelics and indels respectively\.


    (This function takes no parameters)

##### Variant Formatting/Conversion



### fixFirstBaseMismatch

>  This utility will extend indels in which the first base is not a matching base\. Certain variant processing tools may use blanks to mark indels or may not begin combination insertion\-deletion variants with a matching base \(this latter case is technically legal VCF, but some tools may throw errors\)\. 


    windowSize: Sets the size of the sliding window used. Problems may occur if you have variants 
        longer than this window size. Default is 200bp.(Int)
    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

### addFirstBaseWhenMissing

>  Intended for converting other formats into VCFs\. 


    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

### leftAlignAndTrim

>  This utility performs the exact same operations as GATK leftAlignAndTrim\. It trims excess bases and shifts ambiguously\-positioned indels to their leftmost possible position\. This can assist in ensuring that variants are consistantly represented which is critical in matching indels between files\. IMPORTANT: if there are multiallelic variants then they MUST be split apart before this step\. You can use the splitMultiAllelics function to do this\.


    windowSize: Sets the size of the sliding window used. Problems may occur if you have variants 
        longer than this window size. Default is 200bp.(Int)
    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

### fixSwappedRefAlt

>  This utility searches for cases where the REF and ALT bases are swapped\.


    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

### splitMultiAllelics

>  This utility takes any multiallelic variables and splits them apart so that each line contains only one ALT allele\. There are two options for how this will be carried out\. The default creates several new FORMAT fields\. TODO explain more\! \.\.\.Thus after the split the multiallelics will have an ALT field of the form A,\* and the GT field and AD field will use this coding\. Thus if a sample has one of the other alt alleles then 


    useStarAlle: If this flag is used, the asterisk allele will be used as a placeholder for all 
        other alleles. See the explanation above.(Flag)
    treatOtherAsRef:  (Flag)

### fixDotAltIndels

>  \.\.\.


    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

### addAltSequence

>  


    windowSize: The number of flanking bases to include on each side of the alt sequence.(String, 
        default=10)
    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

##### File/Database Annotation



### tagBedFile

>  This function takes a BED file \(which can be gzipped if desired\) and creates a new INFO field based on whether the variant locus overlaps with a genomic region in the BED file\. The new field can be either an integer that is equal to 1 if there is overlap and 0 otherwise \(which is the default behavior\) Or, alternatively, it can copy in the title field from the bed file\. NOTE: this function only uses the first 3 to 5 fields of the BED file, it does not implement the optional fields 10\-12 which can specify intron/exon blocks\.


    file: (String, required)
    desc: The description for the new INFO line.(String, default=No desc provided)
    buffer: The additional buffer to add around each BED element.(Integer, default=0)
    style: This determines the type of INFO tag. For +, the new tag will be a dichotomous 0/1 numeric 
        variable that will equal 1 if and only if the variant intersects with one or more BED lines 
        (including buffer, noted above). For - the opposite is true. For LABEL, the new tag will be a 
        String variable with the title of the element(s) that intersect with the variant, comma 
        delimited. Note that for LABEL style the BED file must have a 4th column.(String, default=+)

### snpSiftAnno

>  This function runs a SnpSift anno command\. SnpSift's java library has been packaged internally within vArmyKnife and is called directly, producing results identical to a separate snpSift command\.


    cmd: A valid SnpSift command. In general you should specify the -info and -name options followed 
        by a VCF file to annotate with. (String)
###### Example 1:
    This example annotates the current VCF file with the AC and AN fields from another VCF file. Note 
        that the annotation VCF file must be sorted and indexed using tabix.
    varmyknife walkVcf \
    --fcn "snpSiftAnno|gnomad|cmd=-info AC,AN -name GNOM_ /path/to/anno/file/gnomad.vcf.gz "\
    infile.vcf.gz outfile.vcf.gz
###### End Example

### snpSiftAnnoMulti

>  This function runs several snpSiftAnno commands one after another\. See the help for the snpSiftAnno function above\. This will be faster than several separate snpSiftAnno function calls\. It uses SnpEff/Sift version 4\.3t\.


    cmds: A semicolon delimited list of valid snpSift commands.(String)

### snpSiftDbnsfp

>  This function runs the SnpSift dbnsfp command\. SnpSift's java library has been packaged internally within vArmyKnife and is called directly, producing results identical to a separate snpSift command\. It uses SnpEff/Sift version 4\.3t\.


    cmd: A valid SnpSift command(String)
###### Example 1:
    This example annotates the current VCF file with a DBNSFP database that has been downloaded and 
        prepared by SNPSIFT.
    varmyknife walkVcf \
    --fcn "snpSiftDbnsfp|dbnsfp|cmd=-f genename,cds_strand,refcodon -db 
        /path/to/db/dbNSFP/v3.5a/dbNSFP3.5a_hg19_sorted.txt.gz "\
    infile.vcf.gz outfile.vcf.gz
###### End Example

### snpEff

>  This function runs SnpEff by calling the SnpEff library internally\. It uses SnpEff version 4\.3t\.


    cmd: A valid SnpSift command.(String)
###### Example 1:
    varmyknife walkVcf
    --fcn "snpEff|mySnpEffRun_1|cmd=GRCh37.75 -noout -c snpEff.config -v -noStats -lof -motif 
        -nextprot"
    infile.vcf.gz outfile.vcf.gz
###### End Example

### snpEffExtract

>  This utility is designed to extract information from ANN fields generated by either SNPEFF or VEP\. While the ANN field contains a large amount of information, the structure and organization of this field is often difficult to parse and operate on\. ANN fields are structured as a series of entries each specifying a gene, transcript, and effect\. For example, if a variant causes a missense change in transcript A and causes a splicing change in transcript B then there would be two entries, one for each effect\. This function can extract whatever specific information desired from the ANN\-formatted field and filters, collates, and reorganizes it in a way that is easier to use\. The various keep and drop lists allow you to extract or ignore ANN entries based on biotype, effect, or warnings\. The geneList function allows you to specify a gene list, any entries that do not pertain to a gene on that list will be ignored\. This can allow you to \(for example\) create a new field that lists the effects only for the desired gene\(s\)\. This can avoid certain common errors\. For example: where one extracts a gene list and then extracts all Loss\-of\-Function variants and ends up with a number of variants that have synonymous effect or simply nearby a gene of interest but are also loss\-of\-function for some other gene which is NOT a gene of interest\.


    annTag: A valid ANN formatted field, usually generated by SNPeff.(String, default=ANN)
    bioTypeKeepList: A comma delimited list of the transcript biotypes that you want to keep. All 
        other biotypes will be ignored.(String)
    effectKeepList: A comma delimited list of the effect types that you want to keep. All other 
        EFFECT values will be ignored.(String)
    warningDropList: A comma delimited list of warnings. Any entries that include a listed warning 
        will be ignored.(String)
    geneList: If this variant is set, then all operations will be restricted to only the ANN entries 
        that correspond to any gene in the supplied list of genes. This can be useful for extracting 
        only the effect on specific genes of interest.(String)
    geneListName: The name you want to give to the given gene list. Should only be used in 
        conjunction with the geneList option.(String)
    severityList: Must be a list of severity levels, listed as some combination of effectseverity 
        types delimited with slashes. Legal types are: HIGH, MODERATE, LOW, and MODIFIER, which are 
        standard SnpEFF effect types, and also: NS (HIGH and MODERATE), NonNS (LOW and MODIFIER), and 
        ANY (any type).(String, default=HIGH/MODERATE/LOW)
    extractFields: This is a complex multi-part field that allows flexible extraction of information 
        from SnpEff ANN tags. This field must be in the colon-delimited format 
        tagInfix:ANN_idx:description:severityList[:noCollapse]. severityList must be in the same 
        format as the severityList parameter above, but can override the default if desired. ANN_idx 
        must be a slash-delimited list of field indices counting from zero in the ANN tag. The 
        standard ANN field indices are: 0:allele,1:effect,2:impact,3:geneName,4:geneID,5:txType,6:txI-
        D,7:txBiotype,8:rank,9:HGVS.c,10:HGVS.p,11:cDNAposition,12:cdsPosition,13:proteinPosition,14:-
        distToFeature,15:warnings,16:errors If multiple fields are selected then the output fields 
        will have the format first:second:third:etc. For example, to create two new fields containing 
        a list of all genes for which the current variant has HIGH and MODERATE impact respectively, 
        use the format: myNewField:4:my description:HIGH/MODERATE. This will generate two new fields: 
        myNewField_HIGH and myNewField_MODERATE. Note that if this function as a whole has a mapID 
        set, then both field names will be prefixed by that overall ID.(String)
    geneNameIdx: This sets the index of the geneName to be used for collating information by gene. By 
        default this is set to 4 which as per the ANN field specification is the "geneID" (usually 
        the ensembl ID). If desired this can be set to 3 which is the "common gene name" (usually the 
        gene symbol), or even 6 which is the transcriptID (which will cause this tool to perform all 
        operations on the transcript level rather than the gene level. This variable can also be 
        useful if you have a nonstandard ANN-style input.(String, default=4)
    biotypeIdx: This sets the ANN field index for the warning field. As per the specification of the 
        ANN field this should always be set to the default (7), but for certain older versions it may 
        be different.(String, default=7)
    warnIdx: This sets the ANN field index for the warning field. As per the specification of the ANN 
        field this should always be set to the default (15), but for certain older versions it may be 
        different.(String, default=15)
###### Example 1:
    The following command will extract various useful information from the ANN field. Entries that do 
        not refer to protein coding transcripts will be ignored. Entries that refer to effects that 
        are not included in the provided effectKeepList will be ignored. Any entries with the listed 
        warnings will be ignored. Several types of output fields will be generated for various effect 
        severities.
    GENEEFFECT: This will be a list of geneName:Effect pairs that pass the above filters.
    ENSGID: This will be a list of ensemble gene ID's from any entries that pass the above filters.
    ENSTID: This will be a list of ensemble transcript ID's from any entries that pass the above 
        filters.
    TXCHANGE: These will be a list of TXID:HGVSc transcript change pairs that pass the above filters.
    FIRSTEFFECT: Like the GENEEFFECT fields, this will be a list of geneName:Effect pairs that pass 
        the above filters, but if multiple effects are listed it will only list the first effect.
    
    New fields will be created for each of the above and each of the listed effect severities. For 
        example:
    ANNEX_GENEEFFECT_HIGH: This will list only gene:effect pairs that pass the above filters and have 
        severity HIGH.
    ANNEX_GENEEFFECT_MODERATE: This will list only gene:effect pairs that pass the above filters and 
        have severity MODERATE.
    ANNEX_GENEEFFECT_NS: This will list only gene:effect pairs that pass the above filters and have 
        severity HIGH or MODERATE.
    and so on...
    
    varmyknife walkVcf
    --fcn "snpEffExtract|ANNEX|annTag=ANN|\
    bioTypeKeepList=protein_coding\
    effectKeepList=\
    coding_sequence_variant,inframe_insertion,disruptive_inframe_insertion,\
    conservative_inframe_insertion,inframe_deletion,disruptive_inframe_deletion,\
    conservative_inframe_insertion,exon_variant,exon_loss_variant,duplication,\
    inversion,frameshift_variant,missense_variant,start_retained_variant,\
    stop_retained_variant,initiator_codon_variant,rare_amino_acid_variant,\
    splice_acceptor_variant,splice_donor_variant,stop_lost,start_lost,stop_gained,\
    synonymous_variant,start_retained,splice_region_variant,\\n 
        5_prime_UTR_premature,start_codon_gain_variant,\
    3_prime_UTR_truncation,5_prime_UTR_truncation,\
    3_prime_UTR_variant,5_prime_UTR_variant,\
    intron_variant,conserved_intron_variant\
    warningDropList=WARNING_TRANSCRIPT_INCOMPLETE,WARNING_TRANSCRIPT_MULTIPLE_STOP_CODONS,\
    WARNING_TRANSCRIPT_NO_START_CODON,WARNING_TRANSCRIPT_NO_STOP_CODON\
    extractFields=GENEEFFECT:3/FIRSTEFFECT:set of gene-effect pairs:HIGH/MODERATE/NS:.,\
    ENSGID:4:Gene ensemble ID:HIGH/NS:.,\
    ENSTID:6:Affected Transcript ID:HIGH/NS:.,\
    TXCHANGE:6/9:TranscriptID and HGVSc change:HIGH/NS:.,\
    FIRSTEFFECT:FIRSTEFFECT:Extracted First Effect:HIGH/MODERATE/LOW/NS/ANY:.|\
    geneNameIdx=3\
    severityList=HIGH/MODERATE/NS/LOW"\
    infile.vcf.gz outfile.vcf.gz
###### End Example

### getLocusDepthFromWig

>  This utility takes a \.wig file \(aka a wiggle file\) and annotates each variant with the depth indicated in the wiggle file for the variant site\.


    wigFile: The input wiggle file.(String)
    desc: The description for the new INFO field, to be included in the INFO line.(String)

### addDistToFeature

>  This utility takes a simple 2\-column text file\. the first column must be the chrom ID and the second column must be position\. A new integer info field will be added that is equal to the distance to the nearest position in the file\. If there is no listed position on the given chromosome then the info field will be missing\.


    file: The input text file. Must have 2 columns, chrom and pos(String)
    desc: The description for the new INFO field, to be included in the INFO line.(String)

##### Genotype Processing



### unPhaseAndSortGenotypes

>  This function removes phasing and sorts genotypes \(so that heterozygotes are always listed as 0/1 and never 1/0\)\.


    inputGT: The input/output genotype FORMAT field.(String)
    groupFile: A tab-delimited file containing sample ID's and a list of group IDs for each sample. 
        See the --groupFile parameter of walkVcf.(String)

### genotypeFilter

>  This function filters a genotype field based on a given genotype expression\. The new filtered genotype can replace the GT field or can be set to a different field, so multiple filtering strategies can be included in a single VCF\.


    expr: A Genotype Expression, using the genotype expression syntax.(String, required)
    desc: A description, to go in the new FORMAT fields.(String)
    filterTag: The name of a new FORMAT field, which will be a flag equal to 1 if and only if the 
        genotype passes the filter.(String)
    outputGT: The output genotype FORMAT field. If this is the same as the input genotype field then 
        the genotype field will be overwritten.(String)
    inputGT: The input genotype FORMAT field.(String)
    inputGtNewName: If this parameter is set, the input genotype field will be copied to a new tag 
        with this name before filtering. This can be useful if overwriting the input genotype field.()
    groupFile: A tab-delimited file containing sample ID's and a list of group IDs for each sample. 
        See the --groupFile parameter of walkVcf.(String)

### genotypeSelect

>  This function selects between two different a genotype fields based on a given genotype expression\. The new composite genotype can replace either GT field or can be set to a different field, so multiple filtering strategies can be included in a single VCF\.


    expr: A Genotype Expression, using the genotype expression syntax.(String, required)
    desc: A description, to go in the new FORMAT fields.(String)
    outputGT: The output genotype FORMAT field. If this is the same as the input genotype field then 
        the genotype field will be overwritten.(String)
    inputGTifTRUE: The input genotype FORMAT field to be used if the expression returns TRUE.(String, 
        required)
    inputGTifFALSE: The input genotype FORMAT field to be used if the expression returns 
        FALSE.(String, required)
    missingString: The string to use when setting the variable to missing.(String)
    groupFile: A tab-delimited file containing sample ID's and a list of group IDs for each sample. 
        See the --groupFile parameter of walkVcf.(String)

##### Concordance Caller



### concordanceCaller

>  \.\.\.\.


    callerNames: Comma delimited list of caller IDs, used in the callerSet INFO fields and the names 
        of the output GT fields. By default, callers will simply be named C1,C2,...(String)
    priority: Comma delimited list of caller IDs. The list of caller IDs in order of descending 
        priority.(String)
    gtDecisionMethod: The merge rule for calculating ensemble-merged GT and AD tags. Valid options 
        are priority, prioritySkipMissing, and majority_priorityOnTies. Default is simple 
        priority.(String)
    ignoreSampleIds: If this flag is set, then sample IDs will be ignored and each VCF will be 
        assumed to have the exact same samples in the exact same order. Use at your own risk.(flag)
    ignoreSampleOrder: If this flag is set, then the sample IDs will be used to match up the 
        different VCFs, and the samples may be in different orders in the different files.(flag)

### concordanceCallerSV

>  \.\.\.\.


    callerNames: Comma delimited list of caller IDs, used in the callerSet INFO fields and the names 
        of the output GT fields. By default, callers will simply be named C1,C2,...(String)
    ignoreSampleIds: If this flag is set, then sample IDs will be ignored and each VCF will be 
        assumed to have the exact same samples in the exact same order, regardless of how they are 
        labelled. The sample labels from the first caller will be used for the output.(flag)
    withinChromWindow: Sets the size of the window around each SV endpoint within which near-similar 
        SVs will be merged. This window only applies to SVs where both endpoints are on the same 
        chromosome.(Int, default=500)
    crossChromWindow: Sets the size of the window around each SV endpoint within which near-similar 
        SVs will be merged. This window only applies to SVs where the endpoints are on DIFFERENT 
        chromosomes.(Int, default=500)

##### Filtering



### keepVariants

>  This function drops variants based on a given true/false expression\.


    expr: A variant-level expression. See the HELP section on Variant-Level Logical 
        Expressions.(String, required)

### extractRegion

>  This function extracts a single region from the VCF\. NOTE: the VCF MUST BE SORTED\!


    region: The genomic region to extract.(String, required)
    windowSize: The size of the window around the genomic region to extract.(Int)

### dropNullVariants

>  This drops variants if they appear beyond the endpoint of the genome builds chromosome\. Certain tools will occasionally create variants like this and they will crash many other functions like left\-align\-and\-trim or GC\-content calculations, etc\.


    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

### dropVariantsBeyondChromEnd

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

### dropInvalidAlleleLines

>  This utility strips all symbolic alleles\. See the VCF v4\.2 specification for more information on the definition of valid allele columns\. Some tools return invalid alleles, and many tools will crash when fed such data\.


    (This function takes no parameters)

### dropVariantsWithNs

>  This utility drops variants if they contain Ns in either the REF or ALT columns\.


    (This function takes no parameters)

##### Genomic Locus Annotation



### homopolymerRunStats

>  This tool adds several new INFO tags that indicate whether the variant is near a homopolymer run, and if so, whether it extends or truncates that run\.


    runSize: The number of repeated bases required to count as a homopolymer run(String, required)
    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

### addContextBases

>  This function adds several new INFO fields which list the base pairs flanking the variant\.


    windowSize: The number of bases to include in the context window(String, required)
    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

### addTrinucleotideComplexity

>  This function adds a new INFO field containing the trinucleotide complexity for the given genomic window around the variant locus, defined as the sum of the squares of the proportions of each 3\-bp combination\.


    windowSize: The number of bases to include in the context window(String, required)
    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

### gcContext

>  This function calculates the fraction of bases within k bases from the variant locus that are G or C\. This can be useful to identify high\-GC areas where variant calling and sequencing may be less accurate\.


    windowSize: The number of bases to include in the context window for determining local gc 
        content.(String, required)
    digits: Number of digits to round to.(String, default=4)
    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

### checkReferenceMatch

>  This function compares the REF column to the genomic reference and makes sure that they actually match\. If mismatches are found, a warning will be thrown\. In addition, a new INFO field will be added to the VCF that will be a simple integer field that will equal 1 if and only if the REF matches the reference, and 0 otherwise\.


    genomeFA: The genome fasta file containing the reference genome. This will be used by various 
        functions that require genomic information. Note that some functions that call the GATK 
        library will also require that the fasta be indexed. Note: Chromosome names must 
        match.(String, required)

##### Data/Table Extraction



### tally

>  This is a set of functions that takes various stats from each variant and sums them up across the whole VCF\. These functions DO NOT change the VCF itself, they simply emit meta information about the VCF\. See the help section on TALLY FUNCTIONS\.


    func: (String, required)

### calculateMatchMatrix

>  \.\.\.\.


    file: (String, required)
    gtTag: The genotype FORMAT field.(String, default=GT)
    matchCutoff: matches below this threshold will not be written to file.(Float, default=0.5)

### extractFormatMatrix

>  This utility will create a matrix file with information extracted from the FORMAT fields of the VCF\. There are two optional formats: standard and longForm\. Standard format will have each variant output a row, the first columns will be Chrom/pos/id/ref/alt \(unless noVarInfo is used, in which case these will be ommitted\), followed by the info fields listed in the infoFields param, followed by one cell for each sample in the VCF, containing a bar\-delimited list of the genotype fields listed in the gtTag parameter\. If the longForm option is used, instead each variant will print its own line for each sample\. Note that this method is generally easier to manipulate for large sample sets, but will generally result in a much larger file since the variant data is repeated many times\.


    file: (String, required)
    gtTag: A comma delimited list of the desired FORMAT fields. If more than one is specified then 
        all will be included in the matrix. If written in standard format, each matrix cell will 
        contain the given fields delimited with bars. If in longform format (see the longForm flag 
        below) then these fields will be tab delimited.(String, default=GT)
    infoFields: Comma delimited list of info fields to include after the CHROM/POS/ID/REF/ALT in the 
        output matrix.(String, default=.)
    longForm: If this flag is used, matrix will be printed in 'long form' in which each element in 
        the matrix gets its own entire line.(Flag)
    noVarInfo: If this flag is used, the variant info CHROM/POS/ID/REF/ALT is omitted from each 
        line.(Flag)

### calcBurdenCounts

>  This function generates the \.


    geneTag: This is the INFO tag that indicates the geneID. It can be a comma-delimited 
        list.(String, required)
    expr: This is a true/false variant expression. Variants will only be counted towards a burden 
        test if they pass this expression. This can be used to generate several burden count tables 
        with different filtering strategies in a single run.(String)
    sampleSet: This is a list of samples to include. Samples not on this list will be 
        ignored.(String)
    group: This is a sample group to include. Samples that are not in this sample group will be 
        ignored. Note that this requires the groupFile variable to be set.(String)
    inputGT: This is the FORMAT column to use as the genotype column.(String)
    groupFile: A tab-delimited file containing sample ID's and a list of group IDs for each sample. 
        See the --groupFile parameter of walkVcf.(String)
    countFileID: If multiple output count files are desired, you can specify which functions output 
        to which count file using this parameter. Note that each file must be created using a 
        --burdenCountsFile parameter, with the form fileID:/path/to/file.txt(String)

### calcBurdenCountsByGroups

>  


    geneTag: This is the INFO tag that indicates the geneID. It can be a comma-delimited 
        list.(String, required)
    groups: This is a comma delimited list of sample groups, taken from the groupFile.(String, 
        required)
    expr: This is a true/false variant expression. Variants will only be counted towards a burden 
        test if they pass this expression. This can be used to generate several burden count tables 
        with different filtering strategies in a single run.(String)
    sampleSet: This is a list of samples to include. Samples not on this list will be 
        ignored.(String)
    inputGT: This is the FORMAT column to use as the genotype column.(String)
    groupFile: A tab-delimited file containing sample ID's and a list of group IDs for each sample. 
        See the --groupFile parameter of walkVcf.(String)
    countFileID: If multiple output count files are desired, you can specify which functions output 
        to which count file using this parameter. Note that each file must be created using a 
        --burdenCountsFile parameter, with the form fileID:/path/to/file.txt(String)

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
    groupFile: A tab-delimited file containing sample ID's and a list of group IDs for each sample. 
        See the --groupFile parameter of walkVcf.(String)
    outfile: The output matrix file path.(String, required)

##### File Formatting/Conversion



### convertSampleNames

>  This function converts the sample IDs of the VCF file according to a decoder file that you supply\. Your decoder should be a tab\-delimited text file with at least 2 columns\. One column should specify the FROM sample names as they currently appear in the VCF, and one should specify the new sample names you want them converted TO\. You must specify which of these columns is which using either the columnNames or columnIdx parameters\. 


    file: A tab delimited file with the from/to chromosome names.(String, required)
    columnNames: The column titles for the old chrom names and the new chrom names, in that order. If 
        this parameter is used, the decoder file must have a title line.(String)
    columnIdx: The column number of the current chromosome names then the new chromosome names, in 
        that order. Column indices start counting from 0. If you use this parameter to set the 
        columns, and if the file has a title line, then you should use skipFirstRow or else it will 
        be read in as if it were a chromosome.(Integer)
    skipFirstRow: If this parameter is set, then this tool will skip the first line on the decoder 
        file. This is useful if you are specifying the columns using column numbers but the file also 
        has a title line.(Flag)

### convertChromNames

>  This function takes a file and translates chromosome names into a different format\. This is most often used to convert between the chr1,chr2,\.\.\. format and the 1,2,\.\.\. format\.


    file: A tab delimited file with the from/to chromosome names.(String, required)
    columnNames: The column titles for the old chrom names and the new chrom names, in that order. If 
        this parameter is used, the decoder file must have a title line.(String)
    columnIdx: The column number of the current chromosome names then the new chromosome names, in 
        that order. Column indices start counting from 0. If you use this parameter to set the 
        columns, and if the file has a title line, then you should use skipFirstRow or else it will 
        be read in as if it were a chromosome.(Integer)
    skipFirstRow: If this parameter is set, then this tool will skip the first line on the decoder 
        file. This is useful if you are specifying the columns using column numbers but the file also 
        has a title line.(Flag)

### sampleReorder

>  This function allows you to reorder the sample columns in your VCF\. Set ONE of the parameters below to specify the desired ordering\.


    sampleOrdering: A simple list of all the samples, in the desired order.(String)
    sampleOrderingFile: A file containing one sampleID per line. The samples will be reordered to 
        match the order found in the file.(String)
    alphabetical: If this flag is set, then the samples will be reordered alphabetically.(Flag)

### addHeaderLine

>  This function allows you to add any header line, changing nothing else\.


    headerLine: A header line.(String, required)

### removeUnannotatedFields

>  This function removes and INFO or FORMAT fields that appear in the VCF lines but are not in the VCF header\.


    (This function takes no parameters)

### filterTags

>  This function can be used to remove unwanted INFO or FORMAT fields, or remove unwanted samples\. This can substantially reduce file sizes\.


    FORMAT.keep: If this is set, then ALL format fields EXCEPT the ones listed here will be 
        dropped.(String)
    FORMAT.drop: IF this is set, then the listed format fields will be dropped.(String)
    INFO.keep: If this is set, then ALL info fields EXCEPT the ones listed here will be 
        dropped.(String)
    INFO.drop: If this is set, then the listed info fields will be dropped.(String)
    SAMPLES.keep: IF this is set, then ALL samples EXCEPT the ones listed here will be 
        dropped.(String)
    SAMPLES.drop: If this is set, the listed samples will be dropped.(String)
    INFO.rename: This is used to rename INFO fields. This should be set to a comma-delimited list of 
        FROM:TO pairs, with each pair separated with a colon.(String)
    FORMAT.rename: This is used to rename FORMAT fields. This should be set to a comma-delimited list 
        of FROM:TO pairs, with each pair separated with a colon.(String)

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


    sampID: The number of flanking bases to include on each side of the alt sequence.(String)

### copyColumnToInfo

>  This utility copies the contents of one of the VCF columns to a new INFO field\. Note that some columns allow characters that are not allowed in INFO fields, such as equal signs\. Any illegal characters will be automatically replaced with underscores\.


    columnID: (String, required)

### copyInfoToColumn

>  This utility copies the contents of an INFO field to one of the the other VCF columns\.


    infoColumn: (String, required)
    columnID: (String, required)

### copyInfoToGeno

>  This utility copies the contents of one of the INFO fields into the genotype level\.


    info: (String, required)

### copyAllInfoToGeno

>  This utility copies the contents of ALL info fields plus the FILTER column into the genotype FORMAT columns\. This can be useful for preserving sample\-level information stored in the INFO column of a single\-sample VCF prior to merging across multiple samples\.


    (This function takes no parameters)

### fixInfoFieldMetadata

>  This function swaps out fields from an INFO header line, allowing you to change the Number, desc, etc\. This can be useful when a field has invalid metadata, or for adding descriptions and documentation to your fields\.


    field: (String, required)
    Type: (String)
    Number: (String)
    desc: (String)
    removeMeta: (Flag)

### fixFormatFieldMetadata

>  This function swaps out fields from an INFO header line, allowing you to change the Number, desc, etc\. This can be useful when a field has invalid metadata, or for adding descriptions and documentation to your fields\.


    field: (String, required)
    Type: (String)
    Number: (String)
    desc: (String)
    removeMeta: (Flag)

### mergeSamplesIntoSingleColumn

>  This utility copies multiple samples into a single merged sample\.


    suffixes: Must be a comma delimited list with a short name for each sample column.(String, 
        required)
    sampID: The name for the new sample column(String, required)