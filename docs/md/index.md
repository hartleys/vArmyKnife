# vArmyKnife
> Version 2.2.314 (Updated Mon Jan 13 12:38:19 EST 2020)

> ([back to main](../index.html)) ([back to java-utility help](index.html))

## Help for vArmyKnife command "walkVcf"

ALIASES: multiStepPipeline
## USAGE:

GENERAL SYNTAX:

    varmyknife [java_options]  [options] infile outfile
    OR
    varmyknife [java_options]  [options] infile - > outfile
    OR, to use other secondary tools:
    varmyknife [java_options] --CMD commandName [options]


## DESCRIPTION:

This utility performs a series of transformations on an input VCF file and adds an array of informative tags\.

## REQUIRED ARGUMENTS:
#### variants.vcf:

> input VCF file. Can be gzipped or in plaintext. (String)


#### outfile.vcf.gz:

> The output file. Can be gzipped or in plaintext. (String)



## OPTIONAL ARGUMENTS:
### Input Parameters:
#### --infileList:

> If this option is set, then the input file is a text file containing a list of input VCF files (one per line), rather than a simple path to a single VCF file. Multiple VCF files will be concatenated and used as input. Note that only the first file's headers will be used, and if any of the subsequent files have tags or fields that are not present in the first VCF file then errors may occur. Also note that if the VCF file includes sample genotypes then the samples MUST be in the same order. (flag)

#### --infileListInfix infileList.txt:

> If this command is included, then all input files are treated very differently. The input VCF file path (or multiple VCFs, if you are running a VCF merge of some sort) must contain a BAR character. The file path string will be split at the bar character and the string infixes from the supplied infileList.txt infix file will be inserted into the break point. This can be very useful for merging multiple chromosomes or genomic-region-split VCFs. (String)

#### --numLinesRead N:

> If this parameter is set, then the utility will stop after reading in N variant lines. Intended for testing purposes. (Int)

#### --testRun:

> Only read the first 1000 lines. Equivalent to --numLinesRead 1000. Intended for testing purposes. (flag)

### Universal Parameters:
#### --genomeFA genome.fa.gz:

> The genome fasta file. Can be gzipped or in plaintext. (String)

### Preprocessing:
#### --splitMultiAllelics:

> If this flag is used, multiallelic variants will be split into multiple separate VCF lines. In order to preserve cross-allele genotypes and multiallelic FORMAT fields, the star allele will be used to indicate a different alt allele on a different variant line. The star-allele encoding can be deactivated using the --splitMultiAllelicsNoStarAllele option instead. (flag)

#### --splitMultiAllelicsNoStarAlle:

> If this flag is used, multiallelic variants will be split into multiple separate VCF lines. Two copies of the AD tag will be created, AD and AD\_multAlle. (flag)

#### --dropSpanIndels:

> Requires splitMultiAllelic. Drops spanning indels, which are marked with an asterisk allele. (flag)

#### --convertROAOtoAD:

> If this flag is used, then the RO/AO FORMAT fields used by certain callers (freebayes) will be merged and copied over into a new AD tag. (flag)

#### --nonNullVariantsOnly:

> If this flag is used, only write variants that have non-null alt alleles. (flag)

#### --rmDup:

> If this flag is used, duplicate lines will be dropped. VCF must be sorted. (flag)

#### --chromList chr1,chr2,...:

> List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes will be ignored. For a VCF that contains only one chromosome this option will improve runtime, since the utility will not have to load and process annotation data for the other chromosomes. (CommaDelimitedListOfStrings)

#### --extractInterval chr1:1000-5000:

> This tool will extract variants across the given genomic interval. All other loci will be discarded. Note: the VCF MUST be sorted! Furthermore: this extract will occur before any leftAlignAndTrim or similar preprocessing steps. (String)

#### --splitOutputByBed intervalBedFile.bed:

> If this option is set, the output will be split up into multiple VCF files based on the supplied BED file. An output VCF will be created for each line in the BED file. If the BED file has the 4th (optional) column, and if this 'name' column contains a unique name with no special characters then this name column will be used as the infix for all the output VCF filenames. If the BED file name column is missing, non-unique, or contains illegal characters then the files will simply be numbered. NOTE: If this option is used, then the 'outfile' parameter must be either a file prefix (rather than a full filename), or must be a file prefix and file suffix separated by a bar character. In other worse, rather than being 'outputFile.vcf.gz', it should be either just 'outputFile' or 'outputFile.|.vcf.gz'.  (String)

#### --fixSwappedRefAlt:

> Checks if the ref allele matches the alt allele. IF they don't match, it checks if the alt allele matches and switches the ref/alt if it does. Otherwise it simply adds a warning tag. (flag)

#### --leftAlignAndTrim:

> Left align and trim the primary input VCF using a modified and ported version of the GATK v3.8-2 LeftAlignAndTrim walker. (flag)

#### --leftAlignAndTrimWindow N:

> Set the window size used for left align and trim. Indels larger than this will not be left aligned. (Int)

#### --fixDotAltIndels:

> Some callers return variant lines that use a dot or dashin the alt column, especially if the VCFs were converted over from ANNOVAR files. technically per the VCF spec this should be interpreted as the absence of any variant alleles (an allele list of length 0). But some tools seem to sometimes use this encoding when they actually mean to indicate an indel. If you use this parameter, then dot-alt indels will be converted to proper form before any other processing. Note: the genomeFA parameter is required in order to use this option, as we need to be able to find the reference sequence for the previous base. (flag)

#### --unPhaseAndSortGenotypes GT,GT\_PREFILT,etc:

> Replace phased genotypes with unphased and reorder genotypes so that heterozygote alleles are always written in ascending order. (CommaDelimitedListOfStrings)

#### --dropSymbolicAlleleLines:

> Drop all variant lines that contain symbolic alleles. If this flag is used with splitMultiAllelic, then the non-symbolic alleles of mixed-type variants will be preserved. (flag)

#### --dropVariantsWithNs:

> Drop any variants that contain Ns in the ref or alt allele. (flag)

#### --inputKeepSamples samp1,samp2,...:

> If this parameter is set, all samples other than the listed samples will be removed PRIOR to processing. (CommaDelimitedListOfStrings)

#### --inputKeepInfoTags tag1,tag2,...:

> List of tags to keep from the input file before processing. All other tags will be dropped before processing. This can be useful for updating a file with a new version or annotation, as it can be used to ensure a clean input. (CommaDelimitedListOfStrings)

#### --inputDropInfoTagsFromFile tag1,tag2,...:

> List of tags to DROP from the input file before processing. All other tags will be dropped before processing. This can be useful for updating a file with a new version or annotation, as it can be used to ensure a clean input. (String)

#### --inputKeepInfoTagsFromFile tag1,tag2,...:

> List of tags to keep from the input file before processing. All other tags will be dropped before processing. This can be useful for updating a file with a new version or annotation, as it can be used to ensure a clean input. (String)

#### --inputDropInfoTags tag1,tag2,...:

> List of tags to DROP from the input file before processing. All other tags will be dropped before processing. This can be useful for updating a file with a new version or annotation, as it can be used to ensure a clean input. (CommaDelimitedListOfStrings)

#### --inputSampleOrdering samp1,samp2,...:

> List of samples in the order you want them to be in prior to any analysis. If you are filtering down your sample list using inputKeepSamples or renaming your samples via sampleRenameFile, then the reordering happens AFTER the renaming. (CommaDelimitedListOfStrings)

#### --inputSampleOrderingAlphabetical:

> This option causes the samples to be reordered into alphabetical order. See --inputSampleOrdering for more info on how and when the reordering happens. (flag)

#### --inputChromDecoder chromDecoder.txt:

> Assigns a chromosome name decoder file. All chromosomes will be renamed based on this decoder. By default, the first column will be the FROM column and the second column will be the TO column. The FROM and TO column numbers can be changed with the --inputChromDecoderFromCol and --inputChromDecoderToCol parameters. These parameters can be used to translate chromosome names between the chr1,chr2,... and 1,2,... style chromosome name conventions. Any chrom names not found in this file will be left as is, throwing a warning. Note: the translation will take place BEFORE any other processing. (String)

#### --inputChromDecoderFromCol val:

> Define the column in the chromDecoder text file that contains the chrom names found in the original input VCF. (Int)

#### --inputChromDecoderToCol val:

> Define the column in the chromDecoder text file that contains the chrom names that are to be used in the new output VCF. (Int)

#### --universalTagPrefix VAK\_:

> Set the universal tag prefix for all vArmyKnife INFO and FORMAT tags. By default it is VAK\_. Warning: if you change this at any point, then all subsequent vArmyKnife commands may need to be changed to match, as vArmyKnife sometimes expects its own tag formatting. (String)

### Annotation:
#### --addLocalGcInfo tagPrefix,windowsize1|ws2|...,numDigits:

> Will add VCF INFO tags that indicate the GC fraction of flanking bases. Requires an indexed genome fasta to be set with the --genomeFA option. The parameter must be formatted in 3 comma-delimited parts: first the prefix to append to the INFO tags, then a bar-delimited list of window sizes, and then the number of digits to include in the output. (repeatable String)

#### --snpSiftAnnotate tagPrefix,snpSiftParameters:

> Options for a SnpSift annotate run. Provide all the options for the standard run.  Do not include the annotate command itself or the input VCF. This will call the internal SnpSift annotate methods, not merely run an external instance of SnpSift.  (repeatable String)

#### --snpSiftFilter filter Name|KEEP\_MATCH or DROP\_MATCH|SnpSift command:

> Options for a SnpSift annotate run. Filters based on the results of this run.  (repeatable String)

#### --snpEffAnnotate tagPrefix,snpEffParameters:

> Options for a SnpEff ann run. Provide all the options for the standard run.  Do not include the annotate command itself or the input VCF. This will call the internal SnpEff ann methods, not merely run an external instance of SnpEff.  (String)

#### --snpSiftDbnsfp ...:

> Options for a SnpSift Dbnsfp run. Provide all the options for the standard run. Do not include the Dbnsfp command itself or the input VCF. This will call the internal SnpSift annotate commands, not merely run an external instance of SnpSift.  (repeatable String)

#### --addBedTags TAGTITLE:bufferLen:filedesc:bedfile.bed,TAGTITLE2:bufferLen:filedesc2:bedfile2.bed.gz,...:

> List of tags and bed files that define said tags. For each tag, the variant will have a tag value of 1 iff the variant appears on the bed file region, and 0 otherwise. This should be a comma-delimited list consisting of one or more colon-delimited lists. Each element in the comma delimited list must have 4 colon-delimited sub-elements: the tag title (ie, the tag that will be added to the VCF file, the buffer distance, the tag description (for the VCF header), and the bedfile path. Bed files may be gzipped or zipped. (repeatable CommaDelimitedListOfStrings)

#### --addContextBases windowSize[:tagInfix]:

> Adds fields containing the sequence flanking the variant with the assigned windowsize. Note: requires genomeFA be set! (repeatable String)

#### --addAltSequence windowSize[:tagID]:

> Adds a field with the alt allele and [windowSize] base pairs of reference-genome flanking sequence to each side. If no tagID is specified, the tagID will be VAK\_altSeq[windowsSize]. Note: requires genomeFA be set! Note: this parameter can be set more than once (with different window sizes). (repeatable String)

#### --homopolymerRunStats tagID|bedfile.bed|hrunThreshold:

> Adds a new tag that indicates when a variant adds to or deletes from a homopolymer run. Requires a homopolymer run bed file. (String)

#### --addVariantPosInfo tagPrefix:

> Copies the chrom, start, ref, and alt columns into an info field. This can be useful for keeping track of variants before and after variant transformations such as leftAlignAndTrim or multiallelic splits. (String)

#### --addInfoVcfs vcfTitle:vcfFilename:List|of|tags:

> A comma delimited list with each element containing 3 colon-delimited parts consisting of the title, filename, and the desired tags for a secondary VCF file. The indicated tags from each VCF file will be copied over. Deprecated: we recommend using the snpEffAnnotate function instead. (repeatable CommaDelimitedListOfStrings)

#### --thirdAlleleChar tag:

> Some users may not prefer the use of the star allele to indicate an other allele in multiallelic variants. If this tag is raised, then the star allele will be removed from multiallelics. In addition, in the GT field all other-allele For multiallelic-split variants, this defines the character used for the 'other' allele. If this is used, the original version will be copied as a backup. (String)

#### --addVariantIdx tag:

> . (String)

#### --tagVariantsExpression newTagID|desc|variantExpression:

> If this parameter is set, then additional tags will be generated based on the given expression(s). The list of expressions must be comma delimited. Each element in the comma-delimited list must begin with the tag ID, then a bar-symbol, followed by the tag description, then a bar-symbol, and finally with the expression. The expressions are always booleans, and follow the same rules for VCF line filtering. See the section on VCF line filtering, below. (repeatable String)

#### --duplicatesTag duplicateTagPrefix:

> If this parameter is set, duplicates will be detected and two new tags will be added: duplicateTagPrefix\_CT and duplicateTagPrefix\_IDX. The CT will indicate how many duplicates were found matching the current variant, and the IDX will number each duplicate with a unique identifier, counting from 0. All nonduplicates will be marked with CT=1 and IDX=0. (String)

#### --tagVariantsGtCountExpression newTagID|desc|genotypeExpression[|type]:

> If this parameter is set, then additional tags will be generated based on the given GENOTYPE expression(s). Each element in the comma-delimited list must begin with the tag ID, then a bar-symbol, followed by the tag description, then a bar-symbol, and finally with the genotype expression. The expressions are always booleans, and follow the same rules for VCF genotype-level filtering. See the section on VCF genotype filtering, below. Optionally, a fourth element in the list can define the type which can be either FRAC, PCT, or CT, which defines the output as either a fraction, a percentage, or a count. (repeatable String)

#### --tagVariantsFunction newTagID|desc|funcName|param1,param2,...:

>  (repeatable String)

#### --variantMapFunction FunctionType|ID|param1=p1|param2=p2|...:

> TODO DESC (repeatable String)

#### --ctrlAlleFreqKeys ...:

> A comma-delimited list of field IDs to use to calculate the maximum of several included allele frequency fields. Optionally, the list can be preceded by the output tag ID, followed by a colon. Can be specified more than once with different parameters and a different output tag ID. (repeatable String)

#### --tallyFile file.txt:

> Write a file with a table containing counts for all tallies, warnings and notices reported during the run. (String)

### Sample Stats:
#### --groupFile groups.txt:

> File containing a group decoder. This is a simple 2-column file (no header line). The first column is the sample ID, the 2nd column is the group ID. (String)

#### --superGroupList sup1,grpA,grpB,...;sup2,grpC,grpD,...:

> A list of top-level supergroups. Requires the --groupFile parameter to be set. (String)

#### --addSampCountWithMultVector tagID|desc|sampMultFile.txt:

> Beta feature: not for general use. (repeatable String)

#### --addSampTag N:

> If this parameter is set, then a tags will be added with sample IDs that possess alt genotypes. Samples will only be printed if the number of samples that possess a given genotype are less than N.  (String)

#### --calcStatGtTag GT:

> The genotype tag used to calculate stats like het count, Het-AD-balance, etc. (String)

### Transcript Annotation:
#### --inputSavedTxFile txdata.data.txt.gz:

> Loads a saved TXdata file in order to add transcript annotation. To generate TX annotation, either this parameter OR the --genomeFA parameter must be set. Using this file will be much faster than regenerating the tx data from the gtf/fasta. this TXdata file must be generated using the GenerateTranscriptAnnotation command (String)

#### --txTypes protein\_coding,...:

> List of transcript biotypes to include. Only works if biotype info is available in the TX annotation. (CommaDelimitedListOfStrings)

#### --gtfFile gtfFile.gtf.gz:

> A gene annotation GTF file. Can be gzipped or in plaintext. (String)

#### --addCanonicalTags knownCanonical.txt:

> Supply a list of canonical transcripts, add tags that indicate canonical-transcript-only variant info. (String)

#### --txToGeneFile txToGene.txt:

> File containing the mapping of transcript names to gene symbols. This file must have 2 columns: the txID and the geneID. No header line. (String)

### SnpEff Annotation Processing:
#### --annTag ANN:

> SnpEff ANN tag for info extract (String)

#### --snpEffTagPrefix ANNEX\_:

> Prefix for SnpEff extracted info tags (String)

#### --snpEffBiotypeKeepList ANN:

> todo desc (CommaDelimitedListOfStrings)

#### --snpEffEffectKeepList ANN:

> todo desc (CommaDelimitedListOfStrings)

#### --snpEffKeepIdx 0,1,2,...:

> todo desc (CommaDelimitedListOfStrings)

#### --snpEffBiotypeIdx 7:

> The index of the field in the annotation column that contains the biotype. (Int)

#### --snpEffWarnIdx 7:

> The index of the field in the annotation column that contains warnings or flags. (Int)

#### --snpEffFieldLen 16:

> The the length of an annotation entry. (Int)

#### --snpEffFields Allele,Consequence,...:

> The names of the fields in the annotation entries. (CommaDelimitedListOfStrings)

#### --snpEffGeneListTagInfix myGeneListAbbrev:

> A short abbreviation for your selected snpeff gene list, for use in the INFO tag IDs. (String)

### Filtering, Genotype-Level:
#### --genoFilter :

> A genotype filter expression. See the help section on genotype filtering expressions, below. (String)

#### --filterTag :

> This sets the field ID for a new FORMAT field that will be 0 when the --genoFilter PASSES and 1 otherwise. (String)

#### --filterInputGtTag :

> Requires that --genoFilter be set. This parameter indicates which FORMAT (genotype) field should be used as the raw genotype for the purposes of genotype filtering via --genoFilter. By default the genotype filter will take the GT field and copy it to GT\_PREFILT and then replace GT with the post-filtering version. (String)

#### --filterOutputGtTag :

> Requires that --genoFilter be set. This parameter indicates which FORMAT field should be used to store the post-filtered genotypes. Note that genotypes that fail the --genoFilter filters will be set to missing. By default the GT field will be overwritten with post-filtered genotypes. (String)

#### --unfilteredGtTag :

> This parameter indicates where the raw genotypes should be copied before filtering the genotypes with the --genoFilter expression. (String)

### Filtering, Variant-Level:
#### --keepVariantsExpression vcfLineFilterExpression:

> If this parameter is set, then VCF lines will be dropped if and only if they FAIL the provided vcf line filter expression. See the section on VCF line filtering below.  (String)

#### --keepVariantsExpressionPrefilter vcfLineFilterExpression:

> If this parameter is set, then VCF lines will be parsed in if and only if they pass the given line filter expression. Note that this will be performed BEFORE other processing, so all referenced tags must be in the input VCF.See the section on VCF line filtering below.  (String)

### Merge Multicaller VCFs:
#### --concordanceCaller:

> This parameter is used to run the ConcordanceCaller utility, which is designed to merge the variant calls from multiple variant callers on the same sample set. If this parameter is raised, then the input VCF should instead be formatted as a comma delimited list of N VCF files. Each of the N files will be run through an initial subset of the final VCF walkers including any of the following that are indicated by the other options: addVariantIdx,nonVariantFilter,chromosome converter,inputTag filters,addVariantPosInfo,splitMultiAllelics,leftAlignAndTrim, and convertROtoAD The variant data output stream from these walkers will be merged and final GT, AD, and GQ fields will be added if the requisite information is available. Final genotypes will be assigned by plurality rule if any genotype has a simple plurality of all nonmissing caller calls, and if no genotype has a plurality then the genotype will be chosen from the highest priority caller, chosen in the order they are named in the  (flag)

#### --concordanceCallerNames vcfName1,vcfName2,...:

> This parameter should be a comma delimited list of names, with the same length as --singleCallerVcfs. These names will be used in the folder-over VCF tags. (CommaDelimitedListOfStrings)

#### --concordanceCallerPriority vcfName1,vcfName2,...:

> This parameter should be a comma delimited list composed of the VCF names from the --singleCallerVcfNames parameter (or a subset of that list). Single callers can be left off this list and their calls will not be used to determine genotype, allele depth, and genotype quality information, although their info fields will still be merged in, and they will still appear in the callerSet field. By default: genotype calls are assigned using the most common call across all callers. Ties are broken using the priority list above, in order of highest priority (most trusted) to lowest priority (least trusted). This behavior can be altered using the --ensembleGenotypeDecision parameter. (CommaDelimitedListOfStrings)

#### --concordanceCallerIgnoreSampleIds:

> This option is only used by the ConcordanceCaller variant caller merge utility. If this parameter is raised, then the sample ID's listed in the last header line will be ignored. Samples MUST be in the same order in each input VCF file! Without this option, this utility will halt with an error message if the sample IDs do not match. Note that if the sample IDs are NOT in the same order, but you want to link them up by name, then instead use the --ccAllowSampleOrderDiff option. (flag)

#### --concordanceCallerIgnoreSampleOrder:

> This option is only used by the ConcordanceCaller variant caller merge utility. If this parameter is raised, then the caller will match up the samples by sample ID, rather than requiring that the samples all be in the same order in all of the input single-caller VCFs. Without this option, this utility will halt with an error message if the sample IDs do not match and are not in the same order. Note that this will STILL crash if all the files do not have the same set of samples. If the VCFs do not all have the same samples, you can have the utility ignore the non-overlapping samples by providing the intersection sample set in the --inputKeepSamples parameter. (flag)

#### --concordanceCallerGtMethod priority:

> The merge rule for calculating ensemble-merged GT and AD tags. Valid options are priority, prioritySkipMissing, and majority\_priorityOnTies. Default is simple priority. (String)

### Postprocessing:
#### --alphebetizeHeader:

> Alphebetizes the INFO and FORMAT header lines. (flag)

#### --convertToStandardVcf:

> Final output file will be a standard VCF that does not use the asterisk to indicate multiallelic variants. Some utilities do not implement the full VCF standard and may have trouble with asterisk alleles. (flag)

#### --dropGenotypeData:

> If this flag is included, then ALL sample-level columns will be stripped from the output VCF. This greatly reduces the file size, and can be useful for making portable variant set VCFs. (flag)

#### --addDummyGenotypeColumn:

> If this flag is included, then the genotype data will be stripped and replaced with a dummy column with 1 sample and 1 het genotype on every line. (flag)

#### --outputKeepInfoTags tag1,tag2,...:

> List of tags to include in the final output (repeatable CommaDelimitedListOfStrings)

#### --outputDropInfoTags tag1,tag2,...:

> List of tags to drop from the final output (repeatable CommaDelimitedListOfStrings)

#### --outputKeepGenoTags tag1,tag2,...:

> List of tags to include in the final output (repeatable CommaDelimitedListOfStrings)

#### --outputDropGenoTags tag1,tag2,...:

> List of tags to drop from the final output (repeatable CommaDelimitedListOfStrings)

#### --outputKeepSamples samp1,samp2,...:

> List of samples to include in the final output (repeatable CommaDelimitedListOfStrings)

### Output Parameters:
#### --splitOutputByChrom:

> If this option is set, the output will be split up into parts by chromosome. NOTE: The outfile parameter must be either a file prefix (rather than a full filename), or must be a file prefix and file suffix separated by a bar character. In other worse, rather than being 'outputFile.vcf.gz', it should be either just 'outputFile' or 'outputFile.|.vcf.gz'.  (flag)

### OTHER OPTIONS:
#### --snpEffWarningDropList ANN:

> todo desc (CommaDelimitedListOfStrings)

#### --verbose:

> Flag to indicate that debugging information and extra progress information should be sent to stderr. (flag)

#### --quiet:

> Flag to indicate that only errors and warnings should be sent to stderr. (flag)

#### --paramFile paramFile.txt:

> A file containing additional parameters and options. Each parameter must begin with a dash. Leading whitespace will be ignored, and newlines preceded by a backslash are similarly ignored. Lines that begin with a pound sign will be skipped. Trailing parameters (infile, outfile, etc) CANNOT be defined using a parameter file. (paramFile)

#### --debugMode:

> Flag to indicate that much more debugging information should be sent to stderr. (flag)

#### --createRunningFile filename.txt:

> A file to create when this utility starts, to be deleted on a clean exit. The file WILL be deleted even if errors are caught. It will only remain if uncaught errors are thrown or if the process is killed externally. (String)

#### --successfulCompletionFile filename.txt:

> A file to create if and when this utility successfully completes without fatal errors. (String)


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

    
    
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
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

### convertSampleNames

    
    \.\.\.\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
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
    skipFirstRow: \(Flag\)

### tagBedFile

    
    \.\.\.\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    file: \(String, required\)
    desc: \(String, default=No desc provided\)
    buffer: \(Integer, default=0\)
    style: \(String, default=\+\)

### checkReferenceMatch

    
    
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    genomeFA: \(String, required\)

### keepVariants

    
    This function drops variants based on a given true/false 
    expression\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    expr: \(String\)

### depthStats

    
    This function calculates various statistics on total read depth 
    and hetAB\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
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
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    cmd: A valid SnpSift command\(String\)

### calcBurdenMatrix

    
    \.\.\.\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
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

### filterTags

    
    This function can be used to remove unwanted INFO or FORMAT 
    fields, or remove unwanted samples\. This can substantially 
    reduce file sizes\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    FORMAT\.keep: \(String\)
    FORMAT\.drop: \(String\)
    INFO\.keep: \(String\)
    INFO\.drop: \(String\)
    SAMPLES\.keep: \(String\)
    SAMPLES\.drop: \(String\)

### gcContext

    
    
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    windowSize: The number of bases to include in the context 
    window for determining local gc content\.\(String, required\)
    digits: Number of digits to round to\.\(String, default=4\)
    genomeFA: \(String, required\)

### addInfoTag

    
    This is a set of functions that all take one or more input 
    parameters and outputs one new INFO field\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    func: \(String, required\)
    desc: \(String, default=No desc provided\)
    digits: \(Integer\)
    params: \(String\)

### tagVariantsExpression

    
    This function takes a variant expression and creates a new INFO 
    field that is 1 if and only if that expression returns TRUE, 
    and 0 otherwise\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    expr: The variant expression, which is a true/false expression 
    using the variant expression syntax\.\(String, required\)
    desc: A description, to go in the info field 
    description\.\(String, default=No desc provided\)

### sanitize

    
    
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)

### snpEff

    
    This function runs SnpEff
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    cmd: A valid SnpSift command\(String\)

### tagVariantsFunction

    
    This is a set of functions that all take one or more input 
    parameters and outputs one new INFO field\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    func: \(String, required\)
    desc: \(String, default=No desc provided\)
    digits: \(Integer\)
    params: \(String\)

### snpEffExtractField

    
    
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    tagPrefix: \(String, required\)
    outputTagPrefix: \(String\)
    columns: \(String\)
    desc: \(String\)
    collapseUniques: \(Flag\)
    tagSet: \(String\)

### snpSiftAnno

    
    This function runs a SnpSift anno command
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    cmd: A valid SnpSift command\(String\)

### unPhaseAndSortGenotypes

    
    This function removes phasing and sorts genotypes\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    inputGT: The input/output genotype FORMAT field\.\(String\)
    groupFile: A tab\-delimited file containing sample ID's and a 
    list of group IDs for each sample\. See the \-\-groupFile 
    parameter of walkVcf\.\(String\)
    superGroupList: See the \-\-superGroupList parameter of 
    walkVcf\.\(String\)

### calcStats

    
    This function combines the functions sampleCounts, sampleLists, 
    and depthStats, performing all three\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
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

    
    
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    geneTag: \(String, required\)
    snpEffAnnField: NOT IMPLEMENTED\(String\)
    snpEffGeneField: NOT IMPLEMENTED\(String\)
    snpEffVariantTypes: NOT IMPLEMENTED\(String\)
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

### homopolymerRunStats

    
    
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    runSize: The number of repeated bases required to count as a 
    homopolymer run\(String, required\)
    genomeFA: \(String, required\)

### addContextBases

    
    
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    windowSize: The number of bases to include in the context 
    window\(String, required\)
    genomeFA: \(String, required\)

### convertChromNames

    
    \.\.\.\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
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
    skipFirstRow: \(Flag\)

### addVariantIdx

    
    
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    windowSize: The number of bases to include in the context 
    window for determining local gc content\.\(String, required\)
    genomeFA: \(String, required\)

### sampleLists

    
    This function generates sample list fields which contain 
    comma\-delimited lists of samples that are het or hom\-alt\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    inputGT: The input genotype FORMAT field\.\(String\)
    samplePrintLimit: \(String\)
    groupFile: A tab\-delimited file containing sample ID's and a 
    list of group IDs for each sample\. See the \-\-groupFile 
    parameter of walkVcf\.\(String\)
    superGroupList: See the \-\-superGroupList parameter of 
    walkVcf\.\(String\)
    expr: The variant expression, which is a true/false expression 
    using the variant expression syntax\.\(String\)

### calcBurdenCountsByGroups

    
    
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
    geneTag: \(String, required\)
    groups: \(String, required\)
    snpEffAnnField: NOT IMPLEMENTED\(String\)
    snpEffGeneField: NOT IMPLEMENTED\(String\)
    snpEffVariantTypes: NOT IMPLEMENTED\(String\)
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

### sampleCounts

    
    This function generates counts and frequencies for alt alleles, 
    alt genotypes, missing genotypes, ref genotypes, and so on\. 
    Note that there are several calc\- flags\. If none of these are 
    included, then this function does nothing\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
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

### genotypeFilter

    
    This function filters a genotype field based on a given 
    genotype expression\. The new filtered genotype can replace the 
    GT field or can be set to a different field, so multiple 
    filtering strategies can be included in a single VCF\.
    mapType: The function to run\.\(String, required\)
    mapID: The ID to use for this operation\. This ID will also be 
    used in the output tag IDs generated by this 
    operation\.\(String, required\)
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
    walkVcf\.\(String\)## VCF Line Filter Expressions



### BASIC SYNTAX:

Filtering expressions are parsed as a series of logical filters 
connected with AND, OR, NOT, and parentheses\. All expressions must 
be separated with whitespace, though it does not matter how much 
whitespace or what kind\. Alternatively, expressions can be read 
directly from file by setting the expression to 
EXPRESSIONFILE:filepath\.

Logical filters are all of the format 
FILTERNAME:PARAM1:PARAM2:etc\. Some filters have no parameters, 
other filters can accept a variable number of parameters\. All 
filters return TRUE or FALSE\. Filters can be inverted using the 
NOT operator before the filter \(with whitespace in between\)\. 
Unless indicated otherwise, elements are dropped when the full 
expression returns FALSE\.
### Filter Functions:

#### ALT\.eq:k

> PASS iff the first ALT allele equals k\.

#### ALT\.isOneOf:k1:k2:...

> PASS iff the first ALT allele is one of k1,k2,\.\.\.

#### ALT\.len\.eq:k

> PASS iff the first ALT allele is of length k\.

#### ALT\.len\.gt:k

> PASS iff the first ALT allele is of length gt k\.

#### AnyGtNonRef:gtTag

> 

#### AnyGtPass:simpleGtFiltExpression:k1:...

> PASS iff any one of the samples pass the supplied GT filter\.

#### CHROM\.inAnyOf:chrX:...

> True iff the variant is one one of the given chromosomes

#### FALSE:

> Never pass

#### FILTER\.eq:k

> PASS iff the FILTER column is equal to k\.

#### FILTER\.ne:k

> PASS iff the FILTER column is not equal to k\.

#### GENO\.MAFgt:t:k

> PASS iff the genotype\-field tag t is a genotype\-style\-formatted field and the minor allele frequency is greater than k\.

#### GENO\.MAFlt:t:k

> PASS iff the genotype\-field tag t is a genotype\-style\-formatted field and the minor allele frequency is less than k\.

#### GENO\.hasTagPairGtStyleMismatch:t1:t2

> PASS iff the genotype\-field t1 and t2 are both found on a given line and have at least 1 sample where both tags are not set to missing but they do not have the same value\.

#### GENO\.hasTagPairMismatch:t1:t2

> PASS iff the genotype\-field t1 and t2 are both found on a given line but are not always equal for all samples\.

#### GTAG\.any\.gt:gtTag:k

> PASS iff any one of the samples have a value for their genotype\-tag entry greater than k\.

#### INFO\.any\.gt:t:k

> PASS iff INFO field t is nonmissing and less than or equal to k\.

#### INFO\.any\.lt:t:k

> PASS iff INFO field t is nonmissing and less than or equal to k\.

#### INFO\.eq:t:k

> PASS iff INFO field t is nonmissing and equal to k\.

#### INFO\.ge:t:k

> PASS iff INFO field t is nonmissing and greater than or equal to k\.

#### INFO\.gem:t:k

> PASS iff INFO field t is missing or greater than or equal to k\.

#### INFO\.gt:t:k

> PASS iff INFO field t is nonmissing and greater than k\.

#### INFO\.gtm:t:k

> PASS iff INFO field t is missing or greater than k\.

#### INFO\.in:t:k

> PASS iff INFO field t is a comma delimited list that contains string k\.

#### INFO\.inAny:t:k

> PASS if INFO field t is a list delimited with commas and bars, and contains string k\.

#### INFO\.inAnyOf:t:k1:k2:...

> PASS iff INFO field t is a list delimited with commas and bars, and contains any of the parameters k1,k2,\.\.\.

#### INFO\.inAnyOfN:t:k1:k2:...

> PASS iff INFO field t is a list delimited with commas, bars, slashes, OR COLONS, and contains any of the parameters k1,k2,\.\.\.

#### INFO\.inAnyOfND:t:k1:k2:...

> PASS iff INFO field t is a list delimited with commas, bars, slashes, colons, or dashes, and contains any of the parameters k1,k2,\.\.\.

#### INFO\.le:t:k

> PASS iff INFO field t is nonmissing and less than or equal to k\.

#### INFO\.lem:t:k

> PASS iff INFO field t is missing or less than or equal to k

#### INFO\.len\.eq:t:k

> PASS iff INFO field t is nonmissing and has length equal to k\.

#### INFO\.len\.gt:t:k

> PASS iff INFO field t is nonmissing and has length greater than k\.

#### INFO\.len\.lt:t:k

> PASS iff INFO field t is nonmissing and has length less than k\.

#### INFO\.lt:t:k

> PASS iff INFO field t is nonmissing and less than k\.

#### INFO\.ltm:t:k

> PASS iff INFO field t is missing or less than k\.

#### INFO\.m:t

> PASS iff INFO field t is missing\.

#### INFO\.mempty:t

> PASS iff INFO field t is missing or less than or equal to k

#### INFO\.ne:t:k

> PASS iff INFO field t is either missing or not equal to k\.

#### INFO\.nm:t

> PASS iff INFO field t is nonmissing\.

#### INFO\.notIn:t:k

> PASS iff INFO field t is missing or is a comma delimited list that does NOT contain string k\.

#### INFO\.notInAny:t:k

> PASS if INFO field t is a list delimited with commas and bars, and does not contain string k\.

#### INFO\.subsetOf:t:k1:k2:...

> PASS iff INFO field t is a comma delimited list and is a subset of k1,k2,etc

#### INFO\.subsetOfFileList:t:f

> PASS iff INFO field t is a comma delimited list and is a subset of the list contained in file f

#### INFO\.tagsDiff:t1:t2

> PASS iff the INFO\-field t1 and t2 are different, including when one is missing and the other is not\.

#### INFO\.tagsMismatch:t1:t2

> PASS iff the INFO\-field t1 and t2 are both found on a given line but are not equal\.

#### LOCUS\.eq:chrom:pos

> True if the variant is at the given chromosome and position

#### LOCUS\.range:chrom:from:to

> True if the variant is at the given chromosome and between the given positions \(0\-based\)

#### POS\.gt:pos

> True iff the variant is at a position greater than the given position

#### POS\.inAnyOf:pos1:...

> True iff the variant is at one of the given positions

#### QUAL\.gt:k

> PASS iff the QUAL column is greater than k\.

#### QUAL\.gtm:k

> PASS iff the QUAL column is greater than k, OR qual is missing\.

#### REF\.eq:k

> PASS iff the REF allele equals k\.

#### REF\.isOneOf:k1:k2:...

> PASS iff the REF allele is one of k1,k2,\.\.\.

#### REF\.len\.eq:k

> PASS iff the REF allele is of length k\.

#### REF\.len\.gt:k

> PASS iff the REF allele is of length gt k\.

#### TRUE:

> Always pass

#### isSNV:

> PASS iff the variant is an SNV\.

#### simpleSNV:

> PASS iff the variant is a biallelic SNV\.

## Genotype Filter Expressions



### BASIC SYNTAX:

Filtering expressions are parsed as a series of logical filters 
connected with AND, OR, NOT, and parentheses\. All expressions must 
be separated with whitespace, though it does not matter how much 
whitespace or what kind\. Alternatively, expressions can be read 
directly from file by setting the expression to 
EXPRESSIONFILE:filepath\.

Logical filters are all of the format 
FILTERNAME:PARAM1:PARAM2:etc\. Some filters have no parameters, 
other filters can accept a variable number of parameters\. All 
filters return TRUE or FALSE\. Filters can be inverted using the 
NOT operator before the filter \(with whitespace in between\)\. 
Unless indicated otherwise, elements are dropped when the full 
expression returns FALSE\.
### Filter Functions:

#### FALSE:

> Never pass

#### GTAG\.SC\.altDepth\.gt:splitIdxTag:t:v

> PASS iff the tag t, which must be a single\-caller\-AD\-style\-formatted field, has an observed alt\-allele\-frequency greater than k\.

#### GTAG\.SC\.altDepth\.lt:splitIdxTag:t:v

> PASS iff the tag t, which must be a single\-caller\-AD\-style\-formatted field, has an observed alt\-allele\-frequency greater than k\.

#### GTAG\.SC\.altProportion\.gt:splitIdxTag:t:v

> PASS iff the tag t, which must be a single\-caller\-AD\-style\-formatted field, has an observed alt\-allele\-frequency greater than k\.

#### GTAG\.SC\.altProportion\.lt:splitIdxTag:t:v

> PASS iff the tag t, which must be a single\-caller\-AD\-style\-formatted field, has an observed alt\-allele\-frequency greater than k\.

#### GTAG\.altDepthForAlle\.gt:gt:ad:v

> PASS iff for AD\-style tag ad and GT\-style tag gt, the sample is called as having an allele K while having less than v reads covering said allele\.

#### GTAG\.altProportion\.gt:t:k

> PASS iff the tag t, which must be a AD\-style\-formatted field, has an observed alt\-allele\-frequency greater than k\.

#### GTAG\.altProportion\.lt:t:k

> PASS iff the tag t, which must be a AD\-style\-formatted field, has an observed alt\-allele\-frequency less than k\.

#### GTAG\.eq:t:s

> PASS iff GT field t equals the string s\. DROP if tag t is not present or set to missing\.

#### GTAG\.ge:t:k

> PASS iff tag t is present and not set to missing, and is a number greater than or equal to k\.

#### GTAG\.gem:t:k

> PASS iff tag t is either not present, set to missing, or a number greater than or equal to k\.

#### GTAG\.gt:t:k

> PASS iff tag t is present and not set to missing, and is a number greater than k\.

#### GTAG\.gtm:t:k

> PASS iff tag t is either not present, set to missing, or a number greater than k\.

#### GTAG\.isAnyAlt:t

> PASS iff the tag t, which must be a genotype\-style\-formatted field, is present and not set to missing and contains the alt allele\.

#### GTAG\.isCleanHet:t

> PASS iff the tag t, which must be a genotype\-style\-formatted field, is present and not set to missing and is heterozygous between the alt and reference allele\.

#### GTAG\.isHet:t

> PASS iff the tag t, which must be a genotype\-style\-formatted field, is present and not set to missing and is heterozygous\.

#### GTAG\.isHomAlt:t

> PASS iff the tag t, which must be a genotype\-style\-formatted field, is present and not set to missing and is homozygous\-alt\.

#### GTAG\.isHomRef:t

> PASS iff the tag t, which must be a genotype\-style\-formatted field, is present and not set to missing and is homozygous\-reference\.

#### GTAG\.le:t:k

> PASS iff tag t is present and not set to missing, and is a number less than or equal to k\.

#### GTAG\.lem:t:k

> PASS iff tag t is either not present, set to missing, or a number less than or equal to k\.

#### GTAG\.lt:t:k

> PASS iff tag t is present and not set to missing, and is a number less than k\.

#### GTAG\.ltm:t:k

> PASS iff tag t is either not present, set to missing, or a number less than k\.

#### GTAG\.m:t:k

> PASS iff the GT field t is is not present or set to missing\.

#### GTAG\.ne:t:k

> PASS iff GT field t does not equal the string s\. DROP if tag t is not present or set to missing\.

#### GTAG\.nm:t

> PASS iff the GT field t is present and not set to missing\.

#### GTAGARRAY\.gt:t:i:k

> PASS iff the tag t is present and not set to missing, and is a list with at least i elements, and the i\-th element of which is greater than k\.

#### GTAGARRAYSUM\.gt:t:k

> PASS iff the tag t is present and not set to missing, and is a list of numbers the sum of which is greater than k\.

#### SAMPGRP\.in:g

> PASS iff the sample is a member of group g\.

#### TAGPAIR\.match:t1:t2

> PASS iff the two tags t1 and t2 are both present and not set to missing, and are equal to one another\.

#### TRUE:

> Always pass

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

