# vArmyKnife
> Version 2.2.53 (Updated Thu Nov  8 13:58:02 EST 2018)

> ([back to main](../index.html)) ([back to java-utility help](index.html))

## Help for java command "walkVcf"

ALIASES: multiStepPipeline
## USAGE:

    varmyknife [java options] walkVcf [options] variants.vcf outfile.vcf.gz
      or 
    java -jar vArmyKnife.jar [java options] walkVcf [options] variants.vcf outfile.vcf.gz



## DESCRIPTION:

This utility performs a series of transformations on an input VCF file and adds an array of informative tags\.WARNING: THIS UTILITY IS AN UNTESTED BETA, AND MAY CONTAIN MAJOR FLAWS\. NOT FOR PRODUCTION USE\! USE AT YOUR OWN RISK\!

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

> If this parameter is set, then the utility will stop after reading in N variant lines. (Int)

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

> If this flag is used, then the RO/AO FORMAT fields used by certain callers (freebayes) will be copied over into a new AD tag. (flag)

#### --nonNullVariantsOnly:

> If this flag is used, only write variants that have non-null alt alleles. (flag)

#### --rmDup:

> If this flag is used, duplicate lines will be dropped. VCF must be sorted. (flag)

#### --chromList chr1,chr2,...:

> List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes will be ignored. For a VCF that contains only one chromosome this option will improve runtime, since the utility will not have to load and process annotation data for the other chromosomes. (CommaDelimitedListOfStrings)

#### --splitOutputByBed intervalBedFile.bed:

> If this option is set, the output will be split up into multiple VCF files based on the supplied BED file. An output VCF will be created for each line in the BED file. If the BED file has the 4th (optional) column, and if this 'name' column contains a unique name with no special characters then this name column will be used as the infix for all the output VCF filenames. If the BED file name column is missing, non-unique, or contains illegal characters then the files will simply be numbered. NOTE: If this option is used, then the 'outfile' parameter must be either a file prefix (rather than a full filename), or must be a file prefix and file suffix separated by a bar character. In other worse, rather than being 'outputFile.vcf.gz', it should be either just 'outputFile' or 'outputFile.|.vcf.gz'.  (String)

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

#### --inputDropInfoTags tag1,tag2,...:

> List of tags to DROP from the input file before processing. All other tags will be dropped before processing. This can be useful for updating a file with a new version or annotation, as it can be used to ensure a clean input. (CommaDelimitedListOfStrings)

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

#### --snpSiftAnnotate ...:

> Options for a SnpSift annotate run. Provide all the options for the standard run.  Do not include the annotate command itself or the input VCF. This will call the internal SnpSift annotate methods, not merely run an external instance of SnpSift.  (repeatable String)

#### --snpSiftDbnsfp ...:

> Options for a SnpSift Dbnsfp run. Provide all the options for the standard run. Do not include the Dbnsfp command itself or the input VCF. This will call the internal SnpSift annotate commands, not merely run an external instance of SnpSift.  (repeatable String)

#### --addBedTags TAGTITLE:bufferLen:filedesc:bedfile.bed,TAGTITLE2:bufferLen:filedesc2:bedfile2.bed.gz,...:

> List of tags and bed files that define said tags. For each tag, the variant will have a tag value of 1 iff the variant appears on the bed file region, and 0 otherwise. This should be a comma-delimited list consisting of one or more colon-delimited lists. Each element in the comma delimited list must have 4 colon-delimited sub-elements: the tag title (ie, the tag that will be added to the VCF file, the buffer distance, the tag description (for the VCF header), and the bedfile path. Bed files may be gzipped or zipped. (repeatable CommaDelimitedListOfStrings)

#### --addVariantPosInfo tagPrefix:

> Copies the chrom, start, ref, and alt columns into an info field. This can be useful for keeping track of variants before and after variant transformations such as leftAlignAndTrim or multiallelic splits. (String)

#### --addInfoVcfs vcfTitle:vcfFilename:List|of|tags:

> A comma delimited list with each element containing 3 colon-delimited parts consisting of the title, filename, and the desired tags for a secondary VCF file. The indicated tags from each VCF file will be copied over. (repeatable CommaDelimitedListOfStrings)

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

#### --ctrlAlleFreqKeys ...:

> A comma-delimited list of field IDs to use to calculate the maximum of several included allele frequency fields. Optionally, the list can be preceded by the output tag ID, followed by a colon. Can be specified more than once with different parameters and a different output tag ID. (repeatable String)

#### --tallyFile file.txt:

> Write a file with a table containing counts for all tallies, warnings and notices reported during the run. (String)

### Sample Stats:
#### --noGroupStats:

> Do NOT calculate group stats. Normally if you specify sample groups then group stats will automatically be calculated. (flag)

#### --groupFile groups.txt:

> File containing a group decoder. This is a simple 2-column file (no header line). The first column is the sample ID, the 2nd column is the group ID. (String)

#### --superGroupList sup1,grpA,grpB,...;sup2,grpC,grpD,...:

> A list of top-level supergroups. Requires the --groupFile parameter to be set. (String)

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

### Merge Multicaller VCFs:
#### --runEnsembleMerger:

> If this parameter is raised, then the input VCF should instead be formatted as a comma delimited list of N VCF files. Each of the N files will be run through an initial subset of the final VCF walkers including any of the following that are indicated by the other options: addVariantIdx,nonVariantFilter,chromosome converter,inputTag filters,addVariantPosInfo,splitMultiAllelics,leftAlignAndTrim, and convertROtoAD The variant data output stream from these walkers will be merged and final GT, AD, and GQ fields will be added if the requisite information is available. Final genotypes will be assigned by plurality rule if any genotype has a simple plurality of all nonmissing caller calls, and if no genotype has a plurality then the genotype will be chosen from the highest priority caller, chosen in the order they are named in the  (flag)

#### --ensembleGenotypeDecision priority:

> The merge rule for calculating ensemble-merged GT and AD tags. Valid options are priority, prioritySkipMissing, and majority\_priorityOnTies. Default is simple priority. (String)

#### --singleCallerVcfNames :

> This parameter should be a comma delimited list of names, with the same length as --singleCallerVcfs. These names will be used in the folder-over VCF tags. (CommaDelimitedListOfStrings)

#### --singleCallerPriority :

> This parameter should be a comma delimited list composed of a subset of the VCF names from the --singleCallerVcfNames parameter. Single callers can be left off this list and their calls will not be used to determine genotype, allele depth, and genotype quality information. Genotype calls are assigned using the most common call across all callers. Ties are broken using the priority list above, in order of highest priority (most trusted) to lowest priority (least trusted). (CommaDelimitedListOfStrings)

### Postprocessing:
#### --alphebetizeHeader:

> Alphebetizes the INFO and FORMAT header lines. (flag)

#### --convertToStandardVcf:

> Final output file will be a standard VCF that does not use the asterisk to indicate multiallelic variants. Some utilities do not implement the full VCF standard and may have trouble with asterisk alleles. (flag)

#### --dropGenotypeData:

> If this flag is included, then ALL sample-level columns will be stripped from the output VCF. This greatly reduces the file size, and can be useful for making portable variant set VCFs. (flag)

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

## VCF Line Filter Expressions



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

## AUTHORS:

Stephen W\. Hartley, Ph\.D\. stephen\.hartley \(at nih dot gov\)

## LEGAL:

 This software is "United States Government Work" under the terms of the United States Copyright  Act\.  It was written as part of the authors' official duties for the United States Government and  thus cannot be copyrighted\.  This software is freely available to the public for use without a  copyright notice\.  Restrictions cannot be placed on its present or future use\.  Although all reasonable efforts have been taken to ensure the accuracy and reliability of the  software and data, the National Cancer Institute \(NCI\) and the U\.S\. Government  does not and cannot warrant the performance or results that may be obtained by using this software  or data\.  NCI and the U\.S\. Government disclaims all warranties as to performance, merchantability  or fitness for any particular purpose\.  In any work or product derived from this material, proper attribution of the authors as the source  of the software or data should be made, using "NCI, Division of Cancer Epidemiology and Genetics, Human Genetics Program" as the citation\.  NOTE: This package USES, but is not derived from, several externally\-developed libraries licensed under various licenses\.  For more information on the licenses of the contained libraries, use the command:   java \-jar thisjarfile\.jar help LICENSES

