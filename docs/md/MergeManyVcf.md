# vArmyKnife
> Version 2.2.318 (Updated Wed Jan 15 15:25:17 EST 2020)

> ([back to main](../index.html)) ([back to java-utility help](index.html))

## Help for vArmyKnife command "MergeManyVcf"

## USAGE:

GENERAL SYNTAX:

    varmyknife [java_options] --CMD MergeManyVcf [options]


## DESCRIPTION:

 UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY\! DO NOT USE\!

## REQUIRED ARGUMENTS:
#### infilePrefix:

> Input file prefix (String)


#### infileSuffix:

> Input file suffix (String)


#### outvcf:

> The output vcf file. (String)



## OPTIONAL ARGUMENTS:
### Annotation:
#### --tallyFile file.txt:

> Write a file with a table containing counts for all tallies, warnings and notices reported during the run. (String)

### Preprocessing:
#### --universalTagPrefix VAK\_:

> Set the universal tag prefix for all vArmyKnife INFO and FORMAT tags. By default it is VAK\_. Warning: if you change this at any point, then all subsequent vArmyKnife commands may need to be changed to match, as vArmyKnife sometimes expects its own tag formatting. (String)

### OTHER OPTIONS:
#### --sumInfoFields tag1,tag2,tag3,...:

>  (CommaDelimitedListOfStrings)

#### --splitInfoFields tag1,tag2,tag3,...:

>  (CommaDelimitedListOfStrings)

#### --firstInfoFields tag1,tag2,tag3,...:

>  (CommaDelimitedListOfStrings)

#### --gtInfoFields tag1,tag2,tag3,...:

>  (CommaDelimitedListOfStrings)

#### --chromList chromList.txt:

>  (String)

#### --genomeFA genome.fa:

>  (String)

#### --leftAlignAndTrimWindow 200:

>  (Int)

#### --infixList infix1,infix2,...:

>  (CommaDelimitedListOfStrings)

#### --infixFile infixlist.txt:

>  (String)

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

Written 2017\-2019 by Stephen Hartley, PhD  National Cancer Institute \(NCI\), Division of Cancer Epidemiology and Genetics \(DCEG\), Human Genetics Program As a work of the United States Government, this software package and all related documentation and information is in the public domain within the United States\. Additionally, the National Institutes of Health and the National Cancer Institute waives copyright and related rights in the work worldwide through the CC0 1\.0 Universal Public Domain Dedication \(which can be found at https://creativecommons\.org/publicdomain/zero/1\.0/\)\. Although all reasonable efforts have been taken to ensure the accuracy and reliability of the software and data, the National Human Genome Research Institute \(NHGRI\), the National Cancer Institute \(NCI\) and the U\.S\. Government does not and cannot warrant the performance or results that may be obtained by using this software or data\. NHGRI, NCI and the U\.S\. Government disclaims all warranties as to performance, merchantability or fitness for any particular purpose\. In work or products derived from this material, proper attribution of the authors as the source of the software or data may be made using "NCI Division of Cancer Epidemiology and Genetics, Human Genetics Program" as the citation\. This package uses \(but is not derived from\) several externally\-developed, open\-source libraries which have been distributed under various open\-source licenses\. vArmyKnife is distributed with compiled versions of these packages\. Additional License information can be accessed using the command:     vArmyKnife \-\-help LICENSES And can also found in the distributed source code in:     src/main/resources/library\.LICENSES\.txt
