
# BASIC SYNTAX:

    Filtering expressions are parsed as a series of logical filters 
        connected with AND, OR, NOT, and parentheses. All 
        expressions must be separated with whitespace, though it 
        does not matter how much whitespace or what kind. 
        Alternatively, expressions can be read directly from file 
        by setting the expression to EXPRESSIONFILE:filepath.
    Logical filters are all of the format 
        FILTERNAME:PARAM1:PARAM2:etc. Some filters have no 
        parameters, other filters can accept a variable number of 
        parameters. All filters return TRUE or FALSE. Filters can 
        be inverted using the NOT operator before the filter (with 
        whitespace in between). Unless indicated otherwise, 
        elements are dropped when the full expression returns FALSE.

# 

    
    FALSE:
    Never pass
    (Param Types: )
    GTAG.SC.altDepth.gt:splitIdxTag:t:v
    PASS iff the tag t, which must be a 
        single-caller-AD-style-formatted field, has an observed 
        alt-allele-frequency greater than k.
    (Param Types: )
    GTAG.SC.altDepth.lt:splitIdxTag:t:v
    PASS iff the tag t, which must be a 
        single-caller-AD-style-formatted field, has an observed 
        alt-allele-frequency greater than k.
    (Param Types: )
    GTAG.SC.altProportion.gt:splitIdxTag:t:v
    PASS iff the tag t, which must be a 
        single-caller-AD-style-formatted field, has an observed 
        alt-allele-frequency greater than k.
    (Param Types: )
    GTAG.SC.altProportion.lt:splitIdxTag:t:v
    PASS iff the tag t, which must be a 
        single-caller-AD-style-formatted field, has an observed 
        alt-allele-frequency greater than k.
    (Param Types: )
    GTAG.altDepthForAlle.gt:gt:ad:v
    PASS iff for AD-style tag ad and GT-style tag gt, the sample is 
        called as having an allele K while having less than v reads 
        covering said allele.
    (Param Types: )
    GTAG.altProportion.gt:t:k
    PASS iff the tag t, which must be a AD-style-formatted field, 
        has an observed alt-allele-frequency greater than k.
    (Param Types: )
    GTAG.altProportion.lt:t:k
    PASS iff the tag t, which must be a AD-style-formatted field, 
        has an observed alt-allele-frequency less than k.
    (Param Types: )
    GTAG.eq:t:s
    PASS iff GT field t equals the string s. DROP if tag t is not 
        present or set to missing.
    (Param Types: )
    GTAG.ge:t:k
    PASS iff tag t is present and not set to missing, and is a 
        number greater than or equal to k.
    (Param Types: )
    GTAG.gem:t:k
    PASS iff tag t is either not present, set to missing, or a 
        number greater than or equal to k.
    (Param Types: )
    GTAG.gt:t:k
    PASS iff tag t is present and not set to missing, and is a 
        number greater than k.
    (Param Types: )
    GTAG.gtm:t:k
    PASS iff tag t is either not present, set to missing, or a 
        number greater than k.
    (Param Types: )
    GTAG.isAnyAlt:t
    PASS iff the tag t, which must be a genotype-style-formatted 
        field, is present and not set to missing and contains the 
        alt allele.
    (Param Types: )
    GTAG.isCleanHet:t
    PASS iff the tag t, which must be a genotype-style-formatted 
        field, is present and not set to missing and is 
        heterozygous between the alt and reference allele.
    (Param Types: )
    GTAG.isHet:t
    PASS iff the tag t, which must be a genotype-style-formatted 
        field, is present and not set to missing and is 
        heterozygous.
    (Param Types: )
    GTAG.isHomAlt:t
    PASS iff the tag t, which must be a genotype-style-formatted 
        field, is present and not set to missing and is 
        homozygous-alt.
    (Param Types: )
    GTAG.isHomRef:t
    PASS iff the tag t, which must be a genotype-style-formatted 
        field, is present and not set to missing and is 
        homozygous-reference.
    (Param Types: )
    GTAG.le:t:k
    PASS iff tag t is present and not set to missing, and is a 
        number less than or equal to k.
    (Param Types: )
    GTAG.lem:t:k
    PASS iff tag t is either not present, set to missing, or a 
        number less than or equal to k.
    (Param Types: )
    GTAG.lt:t:k
    PASS iff tag t is present and not set to missing, and is a 
        number less than k.
    (Param Types: )
    GTAG.ltm:t:k
    PASS iff tag t is either not present, set to missing, or a 
        number less than k.
    (Param Types: )
    GTAG.m:t:k
    PASS iff the GT field t is is not present or set to missing.
    (Param Types: )
    GTAG.ne:t:k
    PASS iff GT field t does not equal the string s. DROP if tag t 
        is not present or set to missing.
    (Param Types: )
    GTAG.nm:t
    PASS iff the GT field t is present and not set to missing.
    (Param Types: )
    GTAGARRAY.gt:t:i:k
    PASS iff the tag t is present and not set to missing, and is a 
        list with at least i elements, and the i-th element of 
        which is greater than k.
    (Param Types: )
    GTAGARRAYSUM.gt:t:k
    PASS iff the tag t is present and not set to missing, and is a 
        list of numbers the sum of which is greater than k.
    (Param Types: )
    SAMPGRP.in:g
    PASS iff the sample is a member of group g.
    (Param Types: )
    TAGPAIR.match:t1:t2
    PASS iff the two tags t1 and t2 are both present and not set to 
        missing, and are equal to one another.
    (Param Types: )
    TRUE:
    Always pass
    (Param Types: )