# vArmyKnife
> Version 3.3.90 (Updated Mon Jul  3 09:37:25 EDT 2023)

> ([back to main](../index.html)) ([back to java-utility help](index.html))

## Help for vArmyKnife command "CalcGenotypeStatTable"

## USAGE:

GENERAL SYNTAX:

    varmyknife [java_options] --CMD CalcGenotypeStatTable [options] variants.vcf tagFile.txt outfileprefix


## DESCRIPTION:

This takes a stat table for genotype statistics \(such as GQ or AD\)\. Warning: does not function on phased genotypes\! UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY\! DO NOT USE\!

## REQUIRED ARGUMENTS:
#### variants.vcf:

> input VCF file. Can be gzipped or in plaintext. (String)


#### tagFile.txt:

> Can be gzipped or in plaintext. This is a special text file that specifies which genotype tags to examine and what stats to collect. It must be a tab-delimited file with a variable number of rows. The first row must be the line type, which is either "TAG" or "PAIR". For TAG lines: the second column is the title to be used for the tag. The third column is the tag ID as it appears in the VCF file. The fourth column is the format or function to collect. Options are: Int (the tag is a simple Int), sumInt (the tag is a series of Ints, collect the sum), get0 (the tag is a series of ints, collect the first value), get1 (the tag is a series of ints, collect the second value). The fifth should specify the counting bins to use, as a comma-delimited list of underscore-delimited numbers. The bins are specified as lower-bound-inclusive. PAIR rows should just have 2 additional columns: one specifying one of the tag titles that appears in one of the TAG lines in this file. This will cause the utility to count a crosswise table comparing the two specified stats across the specified windows. (String)


#### outfileprefix:

> The output file. (String)



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

#### --GenoTag GT:

> The tag used to indicate genotype. (String)

#### --infileList:

> If this option is used, then instead of a single input file the input file(s) will be assumed to be a file containing a list of input files to parse in order. If multiple VCF files are specified, the vcf lines will be concatenated and the header will be taken from the first file. (flag)

#### --byBaseSwap:

> Placeholder: NOT YET IMPLEMENTED! (flag)

#### --subFilterExpressionSets :

> Placeholder: NOT YET IMPLEMENTED! (String)

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

