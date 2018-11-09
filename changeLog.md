# CHANGE LOG

### v2.2.53: 

* Changed SWH prefix to an assignable prefix. the default is VAK_, for vArmyKnife. Can be changed with the --universalTagPrefix parameter
* Added execution time stats to the successfulCompletionFile. Allows easy calculation of runtime.

### v2.2.44: 

* added --duplicatesTag option. Counts duplicates and adds counts and indices.
* Added several more tagVariantsExpression modes and documented them: GTTAGCOPY, GTFILT, GTrecodeMultAlle, GTTAGANDCOUNT

### v2.2.39: 2018-10-26

* Added tallyFile option. Will make a table with counts for warnings, notices, and tallies

### v2.2.36: 2018-10-24

* Added more documentation/help text
* Fixed leftAlignAndTrim to ignore fasta upper/lower case status.

### v2.2.34: 2018-10-18

* Fixed dropSpanIndels option
* Changed SnpSift parser to extract MODIFIER variant types and include them with LOW. You can specify the subtypes using the usual params.

### v2.2.31: 2018-10-10

* experimented with keepInfo and keepFormat regexes. Function is not yet externally accessable.
* added an option to tagVariantsExpression to add a FORMAT field based on a genotype expression, rather than an INFO field.
* added --thirdAlleleChar option to be used with --convertToStandardVcf or alone. Removes the star-allele multiallelic convention
and maps other alt alleles to the supplied string. Recommended values are "." and "0".
* Added --dropSpanIndels option
* Added tally function. Counts various stats and events.

### v2.2.28: 2018-10-2

* Improvements to VcfGtExpressionTag walker

### v2.2.24: 2018-09-28

* Fixed makeFirstBaseMatch option
* Changed convertToStandardVcf option to edit genotypes better and to avoid tag name collisions
* Improvements to VcfGtExpressionTag walker

### v2.2.21: 2018-09-24

* Added --makeFirstBaseMatch option, which adjusts indels so that the first base matches. This is necessary to make LAT work
properly on some oddly-formatted multiallelic variants from FreeBayes
* Worked on alpha version of amino acid matcher

### v2.2.19: 2018-09-21

* Bugfixes.
* Worked on alpha version of amino acid matcher

### v2.2.16: 2018-08-31

* Added BinaryMonoToList argument type
* Added additional tagVariantsExpression modes (allowing genotype-level expressions)
* Shifted convertToStandardVcf walker to later in the pipeline
