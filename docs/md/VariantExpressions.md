
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

    
    ALT\.eq:k
    PASS iff the first ALT allele equals k\.
    ALT\.isOneOf:k1:k2:\.\.\.
    PASS iff the first ALT allele is one of k1,k2,\.\.\.
    ALT\.len\.eq:k
    PASS iff the first ALT allele is of length k\.
    ALT\.len\.gt:k
    PASS iff the first ALT allele is of length gt k\.
    AnyGtNonRef:gtTag
    
    AnyGtPass:simpleGtFiltExpression:k1:\.\.\.
    PASS iff any one of the samples pass the supplied GT filter\.
    FALSE:
    Never pass
    FILTER\.eq:k
    PASS iff the FILTER column is equal to k\.
    FILTER\.ne:k
    PASS iff the FILTER column is not equal to k\.
    GENO\.MAFgt:t:k
    PASS iff the genotype\-field tag t is a 
    genotype\-style\-formatted field and the minor allele frequency 
    is greater than k\.
    GENO\.MAFlt:t:k
    PASS iff the genotype\-field tag t is a 
    genotype\-style\-formatted field and the minor allele frequency 
    is less than k\.
    GENO\.hasTagPairGtStyleMismatch:t1:t2
    PASS iff the genotype\-field t1 and t2 are both found on a 
    given line and have at least 1 sample where both tags are not 
    set to missing but they do not have the same value\.
    GENO\.hasTagPairMismatch:t1:t2
    PASS iff the genotype\-field t1 and t2 are both found on a 
    given line but are not always equal for all samples\.
    GTAG\.any\.gt:gtTag:k
    PASS iff any one of the samples have a value for their 
    genotype\-tag entry greater than k\.
    INFO\.any\.gt:t:k
    PASS iff INFO field t is nonmissing and less than or equal to 
    k\.
    INFO\.any\.lt:t:k
    PASS iff INFO field t is nonmissing and less than or equal to 
    k\.
    INFO\.eq:t:k
    PASS iff INFO field t is nonmissing and equal to k\.
    INFO\.ge:t:k
    PASS iff INFO field t is nonmissing and greater than or equal 
    to k\.
    INFO\.gem:t:k
    PASS iff INFO field t is missing or greater than or equal to k\.
    INFO\.gt:t:k
    PASS iff INFO field t is nonmissing and greater than k\.
    INFO\.gtm:t:k
    PASS iff INFO field t is missing or greater than k\.
    INFO\.in:t:k
    PASS iff INFO field t is a comma delimited list that contains 
    string k\.
    INFO\.inAny:t:k
    PASS if INFO field t is a list delimited with commas and bars, 
    and contains string k\.
    INFO\.inAnyOf:t:k1:k2:\.\.\.
    PASS iff INFO field t is a list delimited with commas and bars, 
    and contains any of the parameters k1,k2,\.\.\.
    INFO\.inAnyOfN:t:k1:k2:\.\.\.
    PASS iff INFO field t is a list delimited with commas, bars, 
    slashes, OR COLONS, and contains any of the parameters 
    k1,k2,\.\.\.
    INFO\.inAnyOfND:t:k1:k2:\.\.\.
    PASS iff INFO field t is a list delimited with commas, bars, 
    slashes, colons, or dashes, and contains any of the parameters 
    k1,k2,\.\.\.
    INFO\.le:t:k
    PASS iff INFO field t is nonmissing and less than or equal to 
    k\.
    INFO\.lem:t:k
    PASS iff INFO field t is missing or less than or equal to k
    INFO\.len\.eq:t:k
    PASS iff INFO field t is nonmissing and has length equal to k\.
    INFO\.len\.gt:t:k
    PASS iff INFO field t is nonmissing and has length greater than 
    k\.
    INFO\.len\.lt:t:k
    PASS iff INFO field t is nonmissing and has length less than k\.
    INFO\.lt:t:k
    PASS iff INFO field t is nonmissing and less than k\.
    INFO\.ltm:t:k
    PASS iff INFO field t is missing or less than k\.
    INFO\.m:t
    PASS iff INFO field t is missing\.
    INFO\.mempty:t
    PASS iff INFO field t is missing or less than or equal to k
    INFO\.ne:t:k
    PASS iff INFO field t is either missing or not equal to k\.
    INFO\.nm:t
    PASS iff INFO field t is nonmissing\.
    INFO\.notIn:t:k
    PASS iff INFO field t is missing or is a comma delimited list 
    that does NOT contain string k\.
    INFO\.notInAny:t:k
    PASS if INFO field t is a list delimited with commas and bars, 
    and does not contain string k\.
    INFO\.subsetOf:t:k1:k2:\.\.\.
    PASS iff INFO field t is a comma delimited list and is a subset 
    of k1,k2,etc
    INFO\.subsetOfFileList:t:f
    PASS iff INFO field t is a comma delimited list and is a subset 
    of the list contained in file f
    INFO\.tagsDiff:t1:t2
    PASS iff the INFO\-field t1 and t2 are different, including 
    when one is missing and the other is not\.
    INFO\.tagsMismatch:t1:t2
    PASS iff the INFO\-field t1 and t2 are both found on a given 
    line but are not equal\.
    QUAL\.gt:k
    PASS iff the QUAL column is greater than k\.
    QUAL\.gtm:k
    PASS iff the QUAL column is greater than k, OR qual is missing\.
    REF\.eq:k
    PASS iff the REF allele equals k\.
    REF\.isOneOf:k1:k2:\.\.\.
    PASS iff the REF allele is one of k1,k2,\.\.\.
    REF\.len\.eq:k
    PASS iff the REF allele is of length k\.
    REF\.len\.gt:k
    PASS iff the REF allele is of length gt k\.
    TRUE:
    Always pass
    isSNV:
    PASS iff the variant is an SNV\.
    simpleSNV:
    PASS iff the variant is a biallelic SNV\.