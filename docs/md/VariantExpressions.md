    Variant expressions are logical expressions that are performed at the variant level. They are 
        used by several parts of vArmyKnife, usually when filtering or differentiating variants based 
        on it's properties/stats. For any given variant, a variant expression will return either TRUE 
        or FALSE. Variant expressions are parsed as a series of logical functions connected with AND, 
        OR, NOT, and parentheses. All expressions MUST be separated with whitespace, though it does 
        not matter how much whitespace or what kind. Alternatively, expressions can be read directly 
        from file by setting the expression toEXPRESSIONFILE:filepath.
    Variant Expression functions are all of the format FILTERNAME:PARAM1:PARAM2:etc. Some filters 
        have no parameters; other filters can accept a variable number of parameters. All expression 
        functions return TRUE or FALSE. Filters can be inverted using the NOT operator before the 
        filter (with whitespace in between).

# 

    
    ALT.eq(k)
    TRUE iff the first ALT allele equals k.
    (Param Types: string)
    ALT.isOneOf(k1,k2,...)
    TRUE iff the first ALT allele is one of k1,k2,...
    (Param Types: string,...)
    ALT.len.eq(k)
    TRUE iff the first ALT allele is of length k.
    (Param Types: int)
    ALT.len.gt(k)
    TRUE iff the first ALT allele is of length gt k.
    (Param Types: int)
    AnyGtNonRef(gtTag)
    TRUE iff gtTag has an alt allele for any sample. gtTag must be a GT-formatted genotype field.
    (Param Types: geno)
    AnyGtPass(simpleGtFiltExpression,k1,...)
    TRUE iff any one of the samples pass the supplied GT filter.
    (Param Types: String,String,...)
    CHROM.inAnyOf(chrX,...)
    TRUE iff the variant is one one of the given chromosomes
    (Param Types: String,String,...)
    FALSE()
    Never TRUE
    (Param Types: )
    FILTER.eq(k)
    TRUE iff the FILTER column is equal to k.
    (Param Types: String)
    FILTER.ne(k)
    TRUE iff the FILTER column is not equal to k.
    (Param Types: String)
    GENO.MAFgt(t,k)
    TRUE iff the genotype-field tag t is a genotype-style-formatted field and the minor allele 
        frequency is greater than k.
    (Param Types: geno)
    GENO.MAFlt(t,k)
    TRUE iff the genotype-field tag t is a genotype-style-formatted field and the minor allele 
        frequency is less than k.
    (Param Types: geno,number)
    GENO.hasTagPairGtStyleMismatch(t1,t2)
    TRUE iff the genotype-field t1 and t2 are both found on a given line and have at least 1 sample 
        where both tags are not set to missing but they do not have the same value.
    (Param Types: geno,geno)
    GENO.hasTagPairMismatch(t1,t2)
    TRUE iff the genotype-field t1 and t2 are both found on a given line but are not always equal for 
        all samples.
    (Param Types: geno,geno)
    GTAG.any.gt(gtTag,k)
    TRUE iff any one of the samples have a value for their genotype-tag entry greater than k.
    (Param Types: geno,number,...)
    INFO.any.gt(t,k)
    TRUE iff INFO field t is nonmissing and less than or equal to k.
    (Param Types: info,number)
    INFO.any.lt(t,k)
    TRUE iff INFO field t is nonmissing and less than or equal to k.
    (Param Types: info,number)
    INFO.eq(t,k)
    TRUE iff INFO field t is nonmissing and equal to k.
    (Param Types: info,string)
    INFO.ge(t,k)
    TRUE iff INFO field t is nonmissing and greater than or equal to k.
    (Param Types: info,number)
    INFO.gem(t,k)
    TRUE iff INFO field t is missing or greater than or equal to k.
    (Param Types: info,number)
    INFO.gt(t,k)
    TRUE iff INFO field t is nonmissing and greater than k.
    (Param Types: info,number)
    INFO.gtm(t,k)
    TRUE iff INFO field t is missing or greater than k.
    (Param Types: info,number)
    INFO.in(t,k)
    TRUE iff INFO field t is a comma delimited list that contains string k.
    (Param Types: info,string)
    INFO.inAny(t,k)
    TRUE if INFO field t is a list delimited with commas and bars, and contains string k.
    (Param Types: info,string)
    INFO.inAnyOf(t,k1,k2,...)
    TRUE iff INFO field t is a list delimited with commas and bars, and contains any of the 
        parameters k1,k2,...
    (Param Types: info,string,...)
    INFO.inAnyOfN(t,k1,k2,...)
    TRUE iff INFO field t is a list delimited with commas, bars, slashes, OR COLONS, and contains any 
        of the parameters k1,k2,...
    (Param Types: info,string,...)
    INFO.inAnyOfND(t,k1,k2,...)
    TRUE iff INFO field t is a list delimited with commas, bars, slashes, colons, or dashes, and 
        contains any of the parameters k1,k2,...
    (Param Types: info,string,...)
    INFO.le(t,k)
    TRUE iff INFO field t is nonmissing and less than or equal to k.
    (Param Types: info,number)
    INFO.lem(t,k)
    TRUE iff INFO field t is missing or less than or equal to k
    (Param Types: info,number)
    INFO.len.eq(t,k)
    TRUE iff INFO field t is nonmissing and has length equal to k.
    (Param Types: info,int)
    INFO.len.gt(t,k)
    TRUE iff INFO field t is nonmissing and has length greater than k.
    (Param Types: info,int)
    INFO.len.lt(t,k)
    TRUE iff INFO field t is nonmissing and has length less than k.
    (Param Types: info,int)
    INFO.lt(t,k)
    TRUE iff INFO field t is nonmissing and less than k.
    (Param Types: info,number)
    INFO.ltm(t,k)
    TRUE iff INFO field t is missing or less than k.
    (Param Types: info,number)
    INFO.m(t)
    TRUE iff INFO field t is missing.
    (Param Types: info)
    INFO.mempty(t)
    TRUE iff INFO field t is missing or less than or equal to k
    (Param Types: info)
    INFO.ne(t,k)
    TRUE iff INFO field t is either missing or not equal to k.
    (Param Types: info,string)
    INFO.nm(t)
    TRUE iff INFO field t is nonmissing.
    (Param Types: info)
    INFO.notIn(t,k)
    TRUE iff INFO field t is missing or is a comma delimited list that does NOT contain string k.
    (Param Types: info,string)
    INFO.notInAny(t,k)
    TRUE if INFO field t is a list delimited with commas and bars, and does not contain string k.
    (Param Types: info,string)
    INFO.subsetOf(t,k1,k2,...)
    TRUE iff INFO field t is a comma delimited list and is a subset of k1,k2,etc
    (Param Types: info,string,...)
    INFO.subsetOfFileList(t,f)
    TRUE iff INFO field t is a comma delimited list and is a subset of the list contained in file f
    (Param Types: info,infile)
    INFO.tagsDiff(t1,t2)
    TRUE iff the INFO-field t1 and t2 are different, including when one is missing and the other is 
        not.
    (Param Types: info,info)
    INFO.tagsMismatch(t1,t2)
    TRUE iff the INFO-field t1 and t2 are both found on a given line but are not equal.
    (Param Types: info,info)
    LOCUS.eq(chrom,pos)
    TRUE if the variant is at the given chromosome and position
    (Param Types: String,number)
    LOCUS.near(k,chrom,pos)
    TRUE if the variant is within k bases from the given chromosome and position
    (Param Types: number,String,number)
    LOCUS.range(chrom,from,to)
    TRUE if the variant is at the given chromosome and between the given positions (0-based)
    (Param Types: String,number)
    POS.gt(pos)
    TRUE iff the variant is at a position greater than the given position
    (Param Types: number)
    POS.inAnyOf(pos1,...)
    TRUE iff the variant is at one of the given positions
    (Param Types: String,String,...)
    QUAL.gt(k)
    TRUE iff the QUAL column is greater than k.
    (Param Types: String)
    QUAL.gtm(k)
    TRUE iff the QUAL column is greater than k, OR qual is missing.
    (Param Types: String)
    REF.eq(k)
    TRUE iff the REF allele equals k.
    (Param Types: string)
    REF.isOneOf(k1,k2,...)
    TRUE iff the REF allele is one of k1,k2,...
    (Param Types: string,...)
    REF.len.eq(k)
    TRUE iff the REF allele is of length k.
    (Param Types: int)
    REF.len.gt(k)
    TRUE iff the REF allele is of length gt k.
    (Param Types: int)
    TRUE()
    Always TRUE
    (Param Types: )
    allelesHaveNoNs()
    FALSE iff the variant has unknown bases, ie N, in the ALT or REF alleles.
    (Param Types: )
    isSNV()
    TRUE iff the variant is an SNV.
    (Param Types: )
    isVariant()
    FALSE iff the variant has no alt alleles, or if the only alt allele is exactly equal to the ref 
        allele.
    (Param Types: )
    simpleSNV()
    TRUE iff the variant is a biallelic SNV.
    (Param Types: )