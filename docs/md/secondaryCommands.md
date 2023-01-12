# vArmyKnife
> Version3.3.38 (Updated Thu Jan 12 11:49:33 EST 2023)

> ([back to help base](docs/secondaryCommands.html))

## General Help

## DESCRIPTION:



NOTE: if you run into OutOfMemoryExceptions, try adding the java options: "-Xmx8G"

## GENERAL SYNTAX:

    varmyknife [java_options]  [options] infile outfile
    OR
    varmyknife [java_options]  [options] infile - > outfile
    OR
    varmyknife [java_options] --CMD commandName [options]
## COMMANDS:
### [walkVcf](index.html)

> This utility performs a series of transformations on an input VCF file and adds an array of informative tags.

### [VcfToMatrix](VcfToMatrix.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [splitExomeIntoParts](splitExomeIntoParts.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

## EXPERIMENTAL COMMANDS: 
These commands have not been fully tested and are not for production use. Documentation may be incomplete or nonexistant. Some are nonfunctional, or have been subsumed into other commands, or were only intended for internal testing purposes. Use at your own risk!
### [GenerateTranscriptAnnotation](GenerateTranscriptAnnotation.html)

> This utility ... WARNING: THIS UTILITY IS AN UNTESTED BETA, AND MAY CONTAIN MAJOR FLAWS. NOT FOR PRODUCTION USE! USE AT YOUR OWN RISK!

### [filterGenotypesByStat](filterGenotypesByStat.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [CreateVariantSampleTableV2](CreateVariantSampleTableV2.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [RemoveUnwantedFields](RemoveUnwantedFields.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [FilterVCF](FilterVCF.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [legacyCanonInfo](legacyCanonInfo.html)

>  UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [CodingCoverageStats](CodingCoverageStats.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [addGroupSummaries](addGroupSummaries.html)

>  UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [addTxBed](addTxBed.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [CmdZip](CmdZip.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [runVcfTest](runVcfTest.html)

> Test utility.

### [legacyRedoDbnsfp](legacyRedoDbnsfp.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [legacyAddDomainInfo](legacyAddDomainInfo.html)

>  UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [testTXSeqUtil](testTXSeqUtil.html)

> Test utility.

### [CmdAssessACMG](CmdAssessACMG.html)

> BETA: This function consolidates information from a wide variety of different input files and attempts to calculate a subset of the ACMG guidelines criteria. It then attempts to assign pathogenicity scores. WARNING: THIS UTILITY IS AN UNTESTED BETA, AND MAY CONTAIN MAJOR FLAWS. NOT FOR PRODUCTION USE! USE AT YOUR OWN RISK!

### [legacySplit](legacySplit.html)

>  UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [GerpTrackToSpans](GerpTrackToSpans.html)

>  UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [ConvertToStandardVcf](ConvertToStandardVcf.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [MergeManyVcf](MergeManyVcf.html)

>  UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [ConvertChromNames](ConvertChromNames.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [buildSummaryTracks](buildSummaryTracks.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [oldWalkVcf](oldWalkVcf.html)

> This utility performs a series of transformations on an input VCF file and adds an array of informative tags.

### [legacyTxAnno](legacyTxAnno.html)

> This utility adds an array of new VCF tags with information about the transcriptional changes caused by each variant. WARNING: THIS UTILITY IS AN UNTESTED BETA, AND MAY CONTAIN MAJOR FLAWS. NOT FOR PRODUCTION USE! USE AT YOUR OWN RISK!

### [ConvertAminoRangeToGenoRange](ConvertAminoRangeToGenoRange.html)

>  UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [CalcVariantCountSummary](CalcVariantCountSummary.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [CalcGenotypeStatTable](CalcGenotypeStatTable.html)

> This takes a stat table for genotype statistics (such as GQ or AD). Warning: does not function on phased genotypes! UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [fixVcfInfoWhitespace](fixVcfInfoWhitespace.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [SimSeqError](SimSeqError.html)

>  UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [compareVcfs](compareVcfs.html)

> Compares two different VCFs containing different builds with overlapping sample sets.UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [VcfParserTest](VcfParserTest.html)

> This utility adds an array of new VCF tags with information about the transcriptional changes caused by each variant. WARNING: THIS UTILITY IS AN UNTESTED BETA, AND MAY CONTAIN MAJOR FLAWS. NOT FOR PRODUCTION USE! USE AT YOUR OWN RISK!

### [getHomopolymerBed](getHomopolymerBed.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [HelloWorld](HelloWorld.html)

> Test utility that just says hello world.

### [RecodeClinVarCLN](RecodeClinVarCLN.html)

>  UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [CreateVariantSampleTable](CreateVariantSampleTable.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [ibdSimulator](ibdSimulator.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [extractSingletons](extractSingletons.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

## AUTHORS:

Stephen W. Hartley, Ph.D. stephen.hartley (at nih dot gov)
## LEGAL:

    Written 2017-2019 by Stephen Hartley, PhD 
    National Cancer Institute (NCI), Division of Cancer 
    Epidemiology and Genetics (DCEG), Human Genetics Program
    As a work of the United States Government, this software 
    package and all related documentation and information is in the 
    public domain within the United States. Additionally, the 
    National Institutes of Health and the National Cancer Institute 
    waives copyright and related rights in the work worldwide 
    through the CC0 1.0 Universal Public Domain Dedication (which 
    can be found at 
    https://creativecommons.org/publicdomain/zero/1.0/).
    Although all reasonable efforts have been taken to ensure the 
    accuracy and reliability of the software and data, the National 
    Human Genome Research Institute (NHGRI), the National Cancer 
    Institute (NCI) and the U.S. Government does not and cannot 
    warrant the performance or results that may be obtained by 
    using this software or data. NHGRI, NCI and the U.S. Government 
    disclaims all warranties as to performance, merchantability or 
    fitness for any particular purpose.
    In work or products derived from this material, proper 
    attribution of the authors as the source of the software or 
    data may be made using "NCI Division of Cancer Epidemiology and 
    Genetics, Human Genetics Program" as the citation.
    This package uses (but is not derived from) several 
    externally-developed, open-source libraries which have been 
    distributed under various open-source licenses. vArmyKnife is 
    distributed with compiled versions of these packages.
    Additional License information can be accessed using the 
    command:
        vArmyKnife --help LICENSES
    And can also found in the distributed source code in:
        src/main/resources/library.LICENSES.txt
