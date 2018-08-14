# vArmyKnife
> Version 2.1.83 (Updated Tue Aug 14 14:00:27 EDT 2018)

> ([back to main](../index.html)) ([back to java-utility help](index.html))

## Help for java command "CalcGenotypeStatTable"

## USAGE:

    varmyknife [java options] CalcGenotypeStatTable [options] variants.vcf tagFile.txt outfileprefix
      or 
    java -jar vArmyKnife.jar [java options] CalcGenotypeStatTable [options] variants.vcf tagFile.txt outfileprefix



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

#### --paramFile paramFile.txt:

> A file containing additional parameters and options. Each parameter must begin with a dash. Leading whitespace will be ignored, and newlines preceded by a backslash are similarly ignored. Lines that begin with a pound sign will be skipped. Trailing parameters (infile, outfile, etc) CANNOT be defined using a parameter file. (paramFile)

#### --debugMode:

> Flag to indicate that much more debugging information should be sent to stderr. (flag)

#### --createRunningFile filename.txt:

> A file to create when this utility starts, to be deleted on a clean exit. The file WILL be deleted even if errors are caught. It will only remain if uncaught errors are thrown or if the process is killed externally. (String)

#### --successfulCompletionFile filename.txt:

> A file to create if and when this utility successfully completes without fatal errors. (String)

## AUTHORS:

Stephen W\. Hartley, Ph\.D\. stephen\.hartley \(at nih dot gov\)

## LEGAL:

 This software is "United States Government Work" under the terms of the United States Copyright  Act\.  It was written as part of the authors' official duties for the United States Government and  thus cannot be copyrighted\.  This software is freely available to the public for use without a  copyright notice\.  Restrictions cannot be placed on its present or future use\.  Although all reasonable efforts have been taken to ensure the accuracy and reliability of the  software and data, the National Cancer Institute \(NCI\) and the U\.S\. Government  does not and cannot warrant the performance or results that may be obtained by using this software  or data\.  NCI and the U\.S\. Government disclaims all warranties as to performance, merchantability  or fitness for any particular purpose\.  In any work or product derived from this material, proper attribution of the authors as the source  of the software or data should be made, using "NCI, Division of Cancer Epidemiology and Genetics, Human Genetics Program" as the citation\.  NOTE: This package USES, but is not derived from, several externally\-developed libraries licensed under various licenses\.  For more information on the licenses of the contained libraries, use the command:   java \-jar thisjarfile\.jar help LICENSES

