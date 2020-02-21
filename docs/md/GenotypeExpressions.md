
# BASIC SYNTAX:

    Filtering expressions are parsed as a series of logical filters 
    connected with AND, OR, NOT, and parentheses\. All expressions 
    must be separated with whitespace, though it does not matter 
    how much whitespace or what kind\. Alternatively, expressions 
    can be read directly from file by setting the expression to 
    EXPRESSIONFILE:filepath\.
    Logical filters are all of the format 
    FILTERNAME:PARAM1:PARAM2:etc\. Some filters have no parameters, 
    other filters can accept a variable number of parameters\. All 
    filters return TRUE or FALSE\. Filters can be inverted using 
    the NOT operator before the filter \(with whitespace in 
    between\)\. Unless indicated otherwise, elements are dropped 
    when the full expression returns FALSE\.

# 

    
    FALSE:
    Never pass
    GTAG\.SC\.altDepth\.gt:splitIdxTag:t:v
    PASS iff the tag t, which must be a 
    single\-caller\-AD\-style\-formatted field, has an observed 
    alt\-allele\-frequency greater than k\.
    GTAG\.SC\.altDepth\.lt:splitIdxTag:t:v
    PASS iff the tag t, which must be a 
    single\-caller\-AD\-style\-formatted field, has an observed 
    alt\-allele\-frequency greater than k\.
    GTAG\.SC\.altProportion\.gt:splitIdxTag:t:v
    PASS iff the tag t, which must be a 
    single\-caller\-AD\-style\-formatted field, has an observed 
    alt\-allele\-frequency greater than k\.
    GTAG\.SC\.altProportion\.lt:splitIdxTag:t:v
    PASS iff the tag t, which must be a 
    single\-caller\-AD\-style\-formatted field, has an observed 
    alt\-allele\-frequency greater than k\.
    GTAG\.altDepthForAlle\.gt:gt:ad:v
    PASS iff for AD\-style tag ad and GT\-style tag gt, the sample 
    is called as having an allele K while having less than v reads 
    covering said allele\.
    GTAG\.altProportion\.gt:t:k
    PASS iff the tag t, which must be a AD\-style\-formatted field, 
    has an observed alt\-allele\-frequency greater than k\.
    GTAG\.altProportion\.lt:t:k
    PASS iff the tag t, which must be a AD\-style\-formatted field, 
    has an observed alt\-allele\-frequency less than k\.
    GTAG\.eq:t:s
    PASS iff GT field t equals the string s\. DROP if tag t is not 
    present or set to missing\.
    GTAG\.ge:t:k
    PASS iff tag t is present and not set to missing, and is a 
    number greater than or equal to k\.
    GTAG\.gem:t:k
    PASS iff tag t is either not present, set to missing, or a 
    number greater than or equal to k\.
    GTAG\.gt:t:k
    PASS iff tag t is present and not set to missing, and is a 
    number greater than k\.
    GTAG\.gtm:t:k
    PASS iff tag t is either not present, set to missing, or a 
    number greater than k\.
    GTAG\.isAnyAlt:t
    PASS iff the tag t, which must be a genotype\-style\-formatted 
    field, is present and not set to missing and contains the alt 
    allele\.
    GTAG\.isCleanHet:t
    PASS iff the tag t, which must be a genotype\-style\-formatted 
    field, is present and not set to missing and is heterozygous 
    between the alt and reference allele\.
    GTAG\.isHet:t
    PASS iff the tag t, which must be a genotype\-style\-formatted 
    field, is present and not set to missing and is heterozygous\.
    GTAG\.isHomAlt:t
    PASS iff the tag t, which must be a genotype\-style\-formatted 
    field, is present and not set to missing and is 
    homozygous\-alt\.
    GTAG\.isHomRef:t
    PASS iff the tag t, which must be a genotype\-style\-formatted 
    field, is present and not set to missing and is 
    homozygous\-reference\.
    GTAG\.le:t:k
    PASS iff tag t is present and not set to missing, and is a 
    number less than or equal to k\.
    GTAG\.lem:t:k
    PASS iff tag t is either not present, set to missing, or a 
    number less than or equal to k\.
    GTAG\.lt:t:k
    PASS iff tag t is present and not set to missing, and is a 
    number less than k\.
    GTAG\.ltm:t:k
    PASS iff tag t is either not present, set to missing, or a 
    number less than k\.
    GTAG\.m:t:k
    PASS iff the GT field t is is not present or set to missing\.
    GTAG\.ne:t:k
    PASS iff GT field t does not equal the string s\. DROP if tag t 
    is not present or set to missing\.
    GTAG\.nm:t
    PASS iff the GT field t is present and not set to missing\.
    GTAGARRAY\.gt:t:i:k
    PASS iff the tag t is present and not set to missing, and is a 
    list with at least i elements, and the i\-th element of which 
    is greater than k\.
    GTAGARRAYSUM\.gt:t:k
    PASS iff the tag t is present and not set to missing, and is a 
    list of numbers the sum of which is greater than k\.
    SAMPGRP\.in:g
    PASS iff the sample is a member of group g\.
    TAGPAIR\.match:t1:t2
    PASS iff the two tags t1 and t2 are both present and not set to 
    missing, and are equal to one another\.
    TRUE:
    Always pass