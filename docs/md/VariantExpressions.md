
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
    \(Param Types: string\)
    ALT\.isOneOf:k1:k2:\.\.\.
    PASS iff the first ALT allele is one of k1,k2,\.\.\.
    \(Param Types: string:\.\.\.\)
    ALT\.len\.eq:k
    PASS iff the first ALT allele is of length k\.
    \(Param Types: int\)
    ALT\.len\.gt:k
    PASS iff the first ALT allele is of length gt k\.
    \(Param Types: int\)
    AnyGtNonRef:gtTag
    
    \(Param Types: geno\)
    AnyGtPass:simpleGtFiltExpression:k1:\.\.\.
    PASS iff any one of the samples pass the supplied GT filter\.
    \(Param Types: geno:String:\.\.\.\)
    FALSE:
    Never pass
    \(Param Types: \)
    FILTER\.eq:k
    PASS iff the FILTER column is equal to k\.
    \(Param Types: String\)
    FILTER\.ne:k
    PASS iff the FILTER column is not equal to k\.
    \(Param Types: String\)
    GENO\.MAFgt:t:k
    PASS iff the genotype\-field tag t is a 
    genotype\-style\-formatted field and the minor allele frequency 
    is greater than k\.
    \(Param Types: geno\)
    GENO\.MAFlt:t:k
    PASS iff the genotype\-field tag t is a 
    genotype\-style\-formatted field and the minor allele frequency 
    is less than k\.
    \(Param Types: geno:number\)
    GENO\.hasTagPairGtStyleMismatch:t1:t2
    PASS iff the genotype\-field t1 and t2 are both found on a 
    given line and have at least 1 sample where both tags are not 
    set to missing but they do not have the same value\.
    \(Param Types: geno:geno\)
    GENO\.hasTagPairMismatch:t1:t2
    PASS iff the genotype\-field t1 and t2 are both found on a 
    given line but are not always equal for all samples\.
    \(Param Types: geno:geno\)
    GTAG\.any\.gt:gtTag:k
    PASS iff any one of the samples have a value for their 
    genotype\-tag entry greater than k\.
    \(Param Types: geno:number:\.\.\.\)
    INFO\.any\.gt:t:k
    PASS iff INFO field t is nonmissing and less than or equal to 
    k\.
    \(Param Types: info:number\)
    INFO\.any\.lt:t:k
    PASS iff INFO field t is nonmissing and less than or equal to 
    k\.
    \(Param Types: info:number\)
    INFO\.eq:t:k
    PASS iff INFO field t is nonmissing and equal to k\.
    \(Param Types: info:string\)
    INFO\.ge:t:k
    PASS iff INFO field t is nonmissing and greater than or equal 
    to k\.
    \(Param Types: info:number\)
    INFO\.gem:t:k
    PASS iff INFO field t is missing or greater than or equal to k\.
    \(Param Types: info:number\)
    INFO\.gt:t:k
    PASS iff INFO field t is nonmissing and greater than k\.
    \(Param Types: info:number\)
    INFO\.gtm:t:k
    PASS iff INFO field t is missing or greater than k\.
    \(Param Types: info:number\)
    INFO\.in:t:k
    PASS iff INFO field t is a comma delimited list that contains 
    string k\.
    \(Param Types: info:string\)
    INFO\.inAny:t:k
    PASS if INFO field t is a list delimited with commas and bars, 
    and contains string k\.
    \(Param Types: info:string\)
    INFO\.inAnyOf:t:k1:k2:\.\.\.
    PASS iff INFO field t is a list delimited with commas and bars, 
    and contains any of the parameters k1,k2,\.\.\.
    \(Param Types: info:string:\.\.\.\)
    INFO\.inAnyOfN:t:k1:k2:\.\.\.
    PASS iff INFO field t is a list delimited with commas, bars, 
    slashes, OR COLONS, and contains any of the parameters 
    k1,k2,\.\.\.
    \(Param Types: info:string:\.\.\.\)
    INFO\.inAnyOfND:t:k1:k2:\.\.\.
    PASS iff INFO field t is a list delimited with commas, bars, 
    slashes, colons, or dashes, and contains any of the parameters 
    k1,k2,\.\.\.
    \(Param Types: info:string:\.\.\.\)
    INFO\.le:t:k
    PASS iff INFO field t is nonmissing and less than or equal to 
    k\.
    \(Param Types: info:number\)
    INFO\.lem:t:k
    PASS iff INFO field t is missing or less than or equal to k
    \(Param Types: info:number\)
    INFO\.len\.eq:t:k
    PASS iff INFO field t is nonmissing and has length equal to k\.
    \(Param Types: info:int\)
    INFO\.len\.gt:t:k
    PASS iff INFO field t is nonmissing and has length greater than 
    k\.
    \(Param Types: info:int\)
    INFO\.len\.lt:t:k
    PASS iff INFO field t is nonmissing and has length less than k\.
    \(Param Types: info:int\)
    INFO\.lt:t:k
    PASS iff INFO field t is nonmissing and less than k\.
    \(Param Types: info:number\)
    INFO\.ltm:t:k
    PASS iff INFO field t is missing or less than k\.
    \(Param Types: info:number\)
    INFO\.m:t
    PASS iff INFO field t is missing\.
    \(Param Types: info\)
    INFO\.mempty:t
    PASS iff INFO field t is missing or less than or equal to k
    \(Param Types: info\)
    INFO\.ne:t:k
    PASS iff INFO field t is either missing or not equal to k\.
    \(Param Types: info:string\)
    INFO\.nm:t
    PASS iff INFO field t is nonmissing\.
    \(Param Types: info\)
    INFO\.notIn:t:k
    PASS iff INFO field t is missing or is a comma delimited list 
    that does NOT contain string k\.
    \(Param Types: info:string\)
    INFO\.notInAny:t:k
    PASS if INFO field t is a list delimited with commas and bars, 
    and does not contain string k\.
    \(Param Types: info:string\)
    INFO\.subsetOf:t:k1:k2:\.\.\.
    PASS iff INFO field t is a comma delimited list and is a subset 
    of k1,k2,etc
    \(Param Types: info:string:\.\.\.\)
    INFO\.subsetOfFileList:t:f
    PASS iff INFO field t is a comma delimited list and is a subset 
    of the list contained in file f
    \(Param Types: info:infile\)
    INFO\.tagsDiff:t1:t2
    PASS iff the INFO\-field t1 and t2 are different, including 
    when one is missing and the other is not\.
    \(Param Types: info:info\)
    INFO\.tagsMismatch:t1:t2
    PASS iff the INFO\-field t1 and t2 are both found on a given 
    line but are not equal\.
    \(Param Types: info:info\)
    QUAL\.gt:k
    PASS iff the QUAL column is greater than k\.
    \(Param Types: String\)
    QUAL\.gtm:k
    PASS iff the QUAL column is greater than k, OR qual is missing\.
    \(Param Types: String\)
    REF\.eq:k
    PASS iff the REF allele equals k\.
    \(Param Types: string\)
    REF\.isOneOf:k1:k2:\.\.\.
    PASS iff the REF allele is one of k1,k2,\.\.\.
    \(Param Types: string:\.\.\.\)
    REF\.len\.eq:k
    PASS iff the REF allele is of length k\.
    \(Param Types: int\)
    REF\.len\.gt:k
    PASS iff the REF allele is of length gt k\.
    \(Param Types: int\)
    TRUE:
    Always pass
    \(Param Types: \)
    isSNV:
    PASS iff the variant is an SNV\.
    \(Param Types: \)
    simpleSNV:
    PASS iff the variant is a biallelic SNV\.
    \(Param Types: \)