# vArmyKnife
> Version 3.2.30 (Updated Tue May 18 15:48:03 EDT 2021)

> ([back to main](../index.html)) ([back to java-utility help](index.html))

## Help for vArmyKnife command "addTxInfoToVCF"

## USAGE:

GENERAL SYNTAX:

    varmyknife [java_options] --CMD addTxInfoToVCF [options] variants.vcf gtffile.gtf.gz outfile.vcf.gz


## DESCRIPTION:

This utility adds an array of new VCF tags with information about the transcriptional changes caused by each variant\. WARNING: THIS UTILITY IS AN UNTESTED BETA, AND MAY CONTAIN MAJOR FLAWS\. NOT FOR PRODUCTION USE\! USE AT YOUR OWN RISK\!

## REQUIRED ARGUMENTS:
#### variants.vcf:

> input VCF file. Can be gzipped or in plaintext. (String)


#### gtffile.gtf.gz:

> A gene annotation GTF file. Can be gzipped or in plaintext. (String)


#### outfile.vcf.gz:

> The output file. Can be gzipped or in plaintext. (String)



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
#### --inputSavedTxFile txdata.data.txt.gz:

> Loads a saved TXdata file. Either this parameter OR the --genomeFA parameter must be set. Using this file will be much faster than regenerating the tx data from the gtf/fasta. (String)

#### --genomeFA genome.fa.gz:

> The genome fasta file. Can be gzipped or in plaintext. Either this parameter OR the --inputSavedTxFile parameter must be set! (String)

#### --summaryFile filename.txt:

> An optional extra output file that contains debugging information. (String)

#### --cdsRegionContainsStop:

> Use this flag if the input GTF annotation file includes the STOP codon in the CDS region. Depending on the source of the annotation file, some GTF files include the STOP codon, some omit it. The UCSC knowngenes annotation file does NOT include CDS regions. (flag)

#### --addSummaryCLNSIG:

> Special-purpose flag for use with specialized ClinVar VCFs. NOT FOR GENERAL USE! (flag)

#### --addCanonicalTags knownCanonical.txt:

> Supply a list of canonical transcripts, add tags that indicate canonical-transcript-only variant info. (String)

#### --splitMultiAllelics:

> If this flag is used, multiallelic variants will be split into multiple separate VCF lines. (flag)

#### --geneVariantsOnly:

> If this flag is used, only variants that fall on or near known genes will be written. (flag)

#### --nonNullVariantsOnly:

> If this flag is used, only write variants that have non-null alt alleles. (flag)

#### --addBedTags TAGTITLE:filedesc:bedfile.bed,TAGTITLE2:filedesc2:bedfile2.bed:

> List of tags and bed files that define said tags. For each tag, the variant will have a tag value of 1 iff the variant appears on the bed file region, and 0 otherwise. (CommaDelimitedListOfStrings)

#### --txInfoFile txInfoFile.txt:

> Outputs an optional debugging file. (String)

#### --outputSavedTxFile txdata.data.txt.gz:

> Creates a saved TXdata file, for faster loading in future runs. This contains metadata about each transcript in a machine-readable format. (String)

#### --txToGeneFile txToGene.txt:

> File containing the mapping of transcript names to gene symbols. This file must have 2 columns: the txID and the geneID. No header line. (String)

#### --groupFile groups.txt:

> File containing a group decoder. This is a simple 2-column file (no header line). The first column is the sample ID, the 2nd column is the group ID. (String)

#### --superGroupList sup1,grpA,grpB,...;sup2,grpC,grpD,...:

> A list of top-level supergroups. Requires the --groupFile parameter to be set. (String)

#### --chromList chr1,chr2,...:

> List of chromosomes. If supplied, then all analysis will be restricted to these chromosomes. All other chromosomes wil be ignored. (CommaDelimitedListOfStrings)

#### --infileList:

> Use this option if you want to provide input file(s) containing a list of input files rather than a single input file (flag)

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

