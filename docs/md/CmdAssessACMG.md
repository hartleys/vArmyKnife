# vArmyKnife
> Version 3.2.44 (Updated Tue Jul  6 23:29:51 EDT 2021)

> ([back to main](../index.html)) ([back to java-utility help](index.html))

## Help for vArmyKnife command "CmdAssessACMG"

## USAGE:

GENERAL SYNTAX:

    varmyknife [java_options] --CMD CmdAssessACMG [options] variants.vcf outvcf


## DESCRIPTION:

BETA: This function consolidates information from a wide variety of different input files and attempts to calculate a subset of the ACMG guidelines criteria\. It then attempts to assign pathogenicity scores\. WARNING: THIS UTILITY IS AN UNTESTED BETA, AND MAY CONTAIN MAJOR FLAWS\. NOT FOR PRODUCTION USE\! USE AT YOUR OWN RISK\!

## REQUIRED ARGUMENTS:
#### variants.vcf:

> input VCF file. This file must have been processed by  (String)


#### outvcf:

> The output vcf file. (String)



## OPTIONAL ARGUMENTS:
### Annotation:
#### --tallyFile file.txt:

> Write a file with a table containing counts for all tallies, warnings and notices reported during the run. (String)

### Other Inputs:
#### --paramFile paramFile.txt:

> A file containing additional parameters and options. Each parameter must begin with a dash. Leading whitespace will be ignored, and newlines preceded by a backslash are similarly ignored. Lines that begin with a pound sign will be skipped. Trailing parameters (infile, outfile, etc) CANNOT be defined using a parameter file. (paramFile)

### Preprocessing:
#### --universalTagPrefix VAK\_:

> Set the universal tag prefix for all vArmyKnife INFO and FORMAT tags. By default it is VAK\_. Warning: if you change this at any point, then all subsequent vArmyKnife commands may need to be changed to match, as vArmyKnife sometimes expects its own tag formatting. (String)

### OTHER OPTIONS:
#### --chromList chr1,chr2,...:

> List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored. (CommaDelimitedListOfStrings)

#### --clinVarVcf clinVarVcf.vcf:

> Processed clinvar variant vcf file. This file must have been processed by the addTxInfoToVCF command. (String)

#### --txToGeneFile txToGene.txt:

> File containing the mapping of transcript names to gene symbols. This file must have 2 columns: the txID and the geneID. No header line. (String)

#### --canonicalTxFile knownCanonical.txt:

> A file containing a list of canonical transcript IDs. It must have a header line with a column labelled "transcript". The header line may begin with a #, or not. It can be compressed or in plaintext. (String)

#### --rmskFile rmsk.txt.gz:

> rmsk.txt.gz file, from UCSC. The only columns that matter are the 6th through 8th columns, which specify the chromosome, starts, and ends of each repetitive region (counting from 0, upper bound exclusive) (String)

#### --toleranceFile toleranceFile.txt:

> This file must contain three columns (labelled in a header line): geneID (gene symbol), LOFtolerant, and MIStolerant. Genes that are not included in this list will be assumed to be non-tolerant. (String)

#### --domainFile domainFile.txt:

> This file must contain at least four columns (labelled in a header line): chrom, start, end, and domainID. (String)

#### --pseudoGeneGTF pseudoGeneGTF.gtf.gz:

> Simple GTF file containing a list of pseudogenes. (String)

#### --inSilicoKeys dbNSFP\_MetaSVM\_pred:D:T:

> This must be a comma-delimited list (no spaces). Each element in the list must consist of 3 parts, seperated by colons (":"). The first part is the INFO key referring to the stored results of an in silico prediction algorithm. The 2nd is an operator, which can be either "eq", "ge", or "le". For "eq", the third and fourth columns can be "|"-delimited lists of values that will be interpreted as "damaging" or "benign" respectively. For the other two functions, the third and fourth columns are values referring to the thresholds for counting the variant as "damaging" or "benign" respectively. Higher is assumed to mean more damaging for "ge", and lower is more damaging for "le". This utility will calculate a "summary" statistic for each specified algorithm which lists the variant as either damaging, ambiguous, benign, or unknown. Variants listed as both damaging as benign will be listed as ambiguous, and variants that are listed as both damaging and unknown will be listed as damaging, and similar for benign. (String)

#### --hgmdPathogenicClasses DM,DM?:

>  (CommaDelimitedListOfStrings)

#### --inSilicoMergeMethod smart:

> Currently the only legal value is the default, "smart", or "intersection" (String)

#### --conservedElementFile conservedElementFile.txt:

> This file contains the spans for the conserved element regions found by GERP. This file must contain 3 columns (no header line): chrom, start end. (String)

#### --lowMapBed lowMapBed.bed.gz:

> This simple bed file should contain all spans with mappability less than 1. (String)

#### --ctrlAlleFreqKeys key1,key2,...:

> List of VCF INFO tags tcontaining the allele frequencies for the control datasets. (CommaDelimitedListOfStrings)

#### --BA1\_AF val:

> The allele frequency cutoff to assign BA1 (benign) status. (Double)

#### --PM2\_AF val:

> The allele frequency cutoff to assign PM2 (moderate pathogenic) status. (Double)

#### --groupFile groups.txt:

> File containing a group decoder. This is a simple 2-column file (no header line). The first column is the sample ID, the 2nd column is the group ID. (String)

#### --hgmdVarVcf HGMD.vcf.gz:

> File containing HGMD variants All variants will be assumed to be likely pathogenic. (String)

#### --superGroupList sup1,grpA,grpB,...;sup2,grpC,grpD,...:

> A list of top-level supergroups. Requires the --groupFile parameter to be set. (String)

#### --summaryOutputFile summaryOutputFile.txt:

> Optional summary output file. (String)

#### --infileList:

> Use this option if you want to provide input file(s) containing a list of input files rather than a single input file (flag)

#### --infileListInfix infileList.txt:

>  (String)

#### --newParser:

> The default parser. Adding this parameter has no effect but is available for backwards compatibility with previous versions. (flag)

#### --oldParser:

> Deprecated, may not support all features. Included for testing purposes. (flag)

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

## AUTHORS:

Stephen W\. Hartley, Ph\.D\. stephen\.hartley \(at nih dot gov\)

## LEGAL:

Written 2017\-2019 by Stephen Hartley, PhD  National Cancer Institute \(NCI\), Division of Cancer Epidemiology and Genetics \(DCEG\), Human Genetics Program As a work of the United States Government, this software package and all related documentation and information is in the public domain within the United States\. Additionally, the National Institutes of Health and the National Cancer Institute waives copyright and related rights in the work worldwide through the CC0 1\.0 Universal Public Domain Dedication \(which can be found at https://creativecommons\.org/publicdomain/zero/1\.0/\)\. Although all reasonable efforts have been taken to ensure the accuracy and reliability of the software and data, the National Human Genome Research Institute \(NHGRI\), the National Cancer Institute \(NCI\) and the U\.S\. Government does not and cannot warrant the performance or results that may be obtained by using this software or data\. NHGRI, NCI and the U\.S\. Government disclaims all warranties as to performance, merchantability or fitness for any particular purpose\. In work or products derived from this material, proper attribution of the authors as the source of the software or data may be made using "NCI Division of Cancer Epidemiology and Genetics, Human Genetics Program" as the citation\. This package uses \(but is not derived from\) several externally\-developed, open\-source libraries which have been distributed under various open\-source licenses\. vArmyKnife is distributed with compiled versions of these packages\. Additional License information can be accessed using the command:     vArmyKnife \-\-help LICENSES And can also found in the distributed source code in:     src/main/resources/library\.LICENSES\.txt

