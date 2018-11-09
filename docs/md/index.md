# vArmyKnife
> Version2.2.53 (Updated Thu Nov  8 13:58:02 EST 2018)

> ([back to help base](../index.html))

## General Help

## DESCRIPTION:



NOTE: if you run into OutOfMemoryExceptions, try adding the java options: "-Xmx8G"

## GENERAL SYNTAX:

    java [java_options] -jar vArmyKnife.jar COMMAND [options]

     or
    varmyknife [java_options]  COMMAND [options]
## COMMANDS:
### [walkVcf](walkVcf.html)

> This utility performs a series of transformations on an input VCF file and adds an array of informative tags.WARNING: THIS UTILITY IS AN UNTESTED BETA, AND MAY CONTAIN MAJOR FLAWS. NOT FOR PRODUCTION USE! USE AT YOUR OWN RISK!

### [GenerateTranscriptAnnotation](GenerateTranscriptAnnotation.html)

> This utility ... WARNING: THIS UTILITY IS AN UNTESTED BETA, AND MAY CONTAIN MAJOR FLAWS. NOT FOR PRODUCTION USE! USE AT YOUR OWN RISK!

### [VcfToMatrix](VcfToMatrix.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

## EXPERIMENTAL COMMANDS: 
These commands have not been fully tested and are not for production use. Documentation may be incomplete or nonexistant. Some are nonfunctional, or have been subsumed into other commands, or were only intended for internal testing purposes. Use at your own risk!
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

### [ConvertChromNames](ConvertChromNames.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

### [buildSummaryTracks](buildSummaryTracks.html)

> UNTESTED ALPHA: FOR DEVELOPMENT/TESTING PURPOSES ONLY! DO NOT USE!

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

    This software is "United States Government Work" under the 
    terms of the United States Copyright Act. It was written as 
    part of the authors' official duties for the United States 
    Government and thus cannot be copyrighted. This software is 
    freely available to the public for use without a copyright 
    notice. Restrictions cannot be placed on its present or future 
    use.
    Although all reasonable efforts have been taken to ensure the 
    accuracy and reliability of the software and data, the National 
    Cancer Institute (NCI) and the U.S. Government does not and 
    cannot warrant the performance or results that may be obtained 
    by using this software or data. NCI and the U.S. Government 
    disclaims all warranties as to performance, merchantability or 
    fitness for any particular purpose.
    In any work or product derived from this material, proper 
    attribution of the authors as the source of the software or 
    data should be made, using "NCI, Division of Cancer 
    Epidemiology and Genetics, Human Genetics Program" as the 
    citation.
    NOTE: This package USES, but is not derived from, several 
    externally-developed libraries licensed under various licenses.
    For more information on the licenses of the contained 
    libraries, use the command:
     java -jar thisjarfile.jar help LICENSES
