# vArmyKnife
> Version 2.1.83 (Updated Tue Aug 14 14:00:27 EDT 2018)

> ([back to main](../index.html)) ([back to java-utility help](index.html))

## Help for java command "ConvertAminoRangeToGenoRange"

## USAGE:

    varmyknife [java options] ConvertAminoRangeToGenoRange [options] infile.txt txdataFile.txt.gz outfile
      or 
    java -jar vArmyKnife.jar [java options] ConvertAminoRangeToGenoRange [options] infile.txt txdataFile.txt.gz outfile



## DESCRIPTION:

 UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY\! DO NOT USE\!

## REQUIRED ARGUMENTS:
#### infile.txt:

> infile (String)


#### txdataFile.txt.gz:

> txdataFile (String)


#### outfile:

> The output file. (String)



## OPTIONAL ARGUMENTS:
#### --txCol 0:

>  (Int)

#### --startCol 1:

>  (Int)

#### --endCol 2:

>  (Int)

#### --badSpanFile file.txt:

> ... (String)

#### --unkTxFile file.txt:

> ... (String)

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
