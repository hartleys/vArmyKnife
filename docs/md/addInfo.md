
# INFO TAG FUNCTIONS

    
    Info Tag Functions are simple modular functions that take one variant at a time and add a new 
        INFO field.
    Basic Syntax:
        --FCN addInfoTag|newTagID|FCN( param1, param2, etc. )
###### Example 1:
    Make a new INFO field which is the maximum from several allele frequencies (which are already in 
        the file) Then make a 0/1 INFO field that is 1 if the max AF is less than 0.01. Note the 
        CONST:0 term, which allows you to include constant values in these functions. In this case it 
        makes it so that if the AF is missing in all three populations, the maxAF will be 0 rather 
        than missing.
    varmyknife walkVcf \
    --fcn "addInfo|maxAF|MAX(CEU_AF,AFR_AF,JPT_AF,CONST:0)|\
    desc=The max allele frequency from CEU_AF, AFR_AF, or JPT_AF (or zero if all are missing)."\
    --fcn "addInfo|isRare|EXPR(INFO.lt:maxAF:0.01)"\
    infile.vcf.gz outfile.vcf.gz
###### End Example
###### Example 2:
    varmyknife walkVcf \
    --fcn "addInfo|CarryCt|SUM(hetCount,homAltCount)|\
    desc=The sum of the info tags: hetCount and homAltCount."\
    infile.vcf.gz outfile.vcf.gz
###### End Example

## Available Functions:

    

### LN\(x\)

    
    Input should be a numeric INFO field. output will be the natural log of that field.
    x (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 

### MULT\(x,y\)

    
    Input should be a pair of info fields and/or numeric constants (which must be specified as 
        CONST:n). Output field will be the product of the two inputs. Missing INFO fields will be 
        treated as ZEROS unless all params are INFO fields and all are missing, in which case the 
        output will be missing. Output field type will be an integer if all inputs are integers and 
        otherwise a float.
    x (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 
    y (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 

### TO\.UPPER\.CASE\(x\)

    
    Input should be an INFO field. All alphabetic characters in the field will be converted to Upper 
        case.
    x (INFO:String) 

### DIFF\(x,y\)

    
    Input should be a pair of info fields and/or numeric constants (which must be specified as 
        CONST:n). Output field will be the difference of the two inputs (ie x - y). Missing INFO 
        fields will be treated as ZEROS unless all params are INFO fields and all are missing, in 
        which case the output will be missing. Output field type will be an integer if all inputs are 
        integers and otherwise a float.
    x (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 
    y (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 

### SUM\(x\.\.\.\)

    
    Input should be a set of info tags and/or numeric constants (which must be specified as CONST:n). 
        Output field will be the sum of the inputs. Missing INFO fields will be treated as zeros 
        unless all params are INFO fields and all are missing, in which case the output will be 
        missing. Output field type will be an integer if all inputs are integers and otherwise a 
        float.
    x... (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 

### FLAGSET\(x\.\.\.\)

    
    Input should be a set of infoFields and optionally a name, with the format tagID:name or just 
        tagID. If names are omitted, then the name will be equal to the tagID. Output field will be 
        the set of names for which the respective info field is equal to 1. Any value other than 1, 
        including missing fields, will be treated as 0.
    x... (INFO:Int) 

### LEN\(x\)

    
    The new field will be an integer field equal to the length of the input field. Will be missing if 
        the input field is missing.
    x (INFO:String|INFO:Int|INFO:Float) 

### RANDFLAG\(x,seed\)

    
    
    x (FLOAT) 
    seed (INT) 

### SETS\.KEEP\.ELEMENTS\.THAT\.CONTAIN\(info,str\)

    
    First parameter should be an INFO field, second parameter is a string. Any elements in the INFO 
        field that contain the string will be dropped. Does not do pattern matching, simple 
        replacement.
    info (INFO:String) 
    str (CONST:String) 

### TO\.LOWER\.CASE\(x\)

    
    Input should be an INFO field. All alphabetic characters in the field will be converted to Lower 
        case.
    x (INFO:String) 

### DIV\(x,y\)

    
    Input should be a pair of info fields and/or numeric constants (which must be specified as 
        CONST:n). Output field will be the product of the two inputs. Missing INFO fields will be 
        treated as ZEROS unless all params are INFO fields and all are missing, in which case the 
        output will be missing. Output field type will be a float.
    x (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 
    y (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 

### MIN\(x\.\.\.\)

    
    
    x... (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 

### SETS\.DROP\.ELEMENTS\.THAT\.CONTAIN\(info,str\)

    
    First parameter should be an INFO field, second parameter is a string. Any elements in the INFO 
        field that contain the string will be dropped. Does not do pattern matching, simple 
        replacement.
    info (INFO:String) 
    str (CONST:String) 

### CONVERT\.TO\.INT\(x,defaultValue\)

    
    Input should be an INFO field, usually of type String. Converts field to a Integer. By default 
        failed conversions will simply be left out. if the defaultValue option is included, then 
        failed conversions will be set to the defaultValue.
    x (INFO:String) 
    defaultValue (Optional) (CONST:Int) 

### CONVERT\.TO\.FLOAT\(x,defaultValue\)

    
    Input should be an INFO field. Converts to a numeric float. If no defaultValue is supplied then 
        non-floats will be dropped. Note that NaN and Inf will be dropped / replaced with the default.
    x (INFO:String) 
    defaultValue (Optional) (CONST:Float) 

### STRING\.REPLACE\(old,new,info\)

    
    Simple string replacement. First parameter should be the old string, second parameter the 
        replacement string, and the third parameter an INFO field. Any time the old string appears in 
        the INFO field it will be replaced by the new string. Does not do pattern matching, simple 
        replacement.
    old (CONST:String) 
    new (CONST:String) 
    info (INFO:String) 

### EXPR\(expr\)

    
    The new field will be an integer field which will be equal to 1 if and only if the expression is 
        TRUE, and 0 otherwise. See the expression format definition for more information on how the 
        logical expression syntax works.
    expr (STRING) 

### DECODE\(x,decoder\)

    
    Decodes an INFO field. Decoder must be a simple 2-column tab-delimited file with the old ID 
        first. Any time an element in the INFO field x matches an element in the first column of the 
        text file, it will be swapped with the corresponding entry in the second column of the text 
        file. Elements that do not match any element in the first column will be unchanged.
    x (INFO:String) 
    decoder (FILE:String) 

### STRING\.REPLACE\.WITHCOMMA\(old,info\)

    
    Simple string replacement. First parameter should be the old string, second parameter an INFO 
        field. Any time the old string appears in the INFO field it will be replaced by a comma. Does 
        not do pattern matching, simple replacement.
    old (CONST:String) 
    info (INFO:String) 

### CONCAT\(x\.\.\.\)

    
    This simple function concatenates the values of the input parameters. Input parameters can be any 
        combination of INFO fields or constant strings.
    x... (INFO:String|CONST:String) 

### SUM\.GENO\(x\)

    
    Input should be a genotype field. Output field will be the sum of the given genotype field or 
        fields. If the field is missing across all samples, the INFO field will also be missing, 
        otherwise missing values will be treated as zeros. Output field type will be an integer if 
        the inputs is an integer field and otherwise a float.
    x (GENO:Int|GENO:Float) 

### LOG10\(x\)

    
    Input should be a numeric INFO field. output will be the log10 of that field.
    x (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 

### PRODUCT\.ARRAY\(x\.\.\.\)

    
    Input should be a set of info fields and/or numeric constants (which must be specified as 
        CONST:n). Output field will be the product of the inputs. Missing INFO fields will be treated 
        as ones unless all params are INFO fields and all are missing, in which case the output will 
        be missing. Output field type will be an integer if all inputs are integers and otherwise a 
        float.
    x... (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 

### SETS\.DIFF\(x,y\)

    
    Input should be a pair of sets that are either INFO fields specified as INFO:tagID, text files 
        specified as FILE:fileName, or a constant set delimited with colons.Output field will be a 
        comma delimited string containing the elements in the first set with the second set 
        subtracted out.
    x (INFO:String|INFO:Int|INFO:Float|FILE:String|CONST:String|CONST:Int|CONST:Float) 
    y (INFO:String|INFO:Int|INFO:Float|FILE:String|CONST:String|CONST:Int|CONST:Float) 

### blanksToDots\(info\)

    
    If a field is left blank, this will properly replace the blank with a period, which is the proper 
        missing value symbol
    info (INFO:String) 

### SWITCH\.EXPR\(expr,A,B\)

    
    Switches between two options depending on a logical expression. The 'expr' expression parameter 
        must be formatted like standard variant-level expressions. The A and B parameters can each be 
        either a constant or an INFO field. The output field will be equal to A if the logical 
        expression is TRUE, and otherwise will be B.
    expr (STRING) 
    A (INFO:Int|INFO:Float|INFO:String|CONST:Int|CONST:Float|CONST:String) 
    B (Optional) (INFO:Int|INFO:Float|INFO:String|CONST:Int|CONST:Float|CONST:String) 

### GT\.EXPR\(gtExpr,varExpr\)

    
    The new field will be an integer field which will be equal to the number of samples that satisfy 
        a given genotype-level expression. See the expression format definition for more information 
        on how the logical expression syntax works. You can also specify a variant-level expression 
        which, if false, will return missing.
    gtExpr (Optional) (STRING) 
    varExpr (Optional) (STRING) 

### COLLATE\(inputDelimName,outputDelimOuter,outputDelimInner,x\.\.\.\)

    
    This takes multiple ordered info fields and collates them. The new output INFO field will be a 
        list of lists. The first list in the list of lists will be composed of The first element of 
        the first field, the first element of the second field, the first element of the third field, 
        and so on. The second list in the list of lists will be composed of the second element of the 
        first field, the second element of the second field, and so on. Delimiters in the input lists 
        as well as the two delimiters used in the output can have the following names: colon, comma, 
        bar, slash, period, or ampersand. Note that the input delim can be a slash-delimited list of 
        delimiter names.
    inputDelimName (CONST:String) 
    outputDelimOuter (CONST:String) 
    outputDelimInner (CONST:String) 
    x... (INFO:String) 

### MAX\(x\.\.\.\)

    
    
    x... (INFO:Int|INFO:Float|CONST:Int|CONST:Float) 

### PICK\.RANDOM\(seed,x,y\.\.\.\)

    
    The first parameter must be either '.' or a supplied random seed for the random number generator. 
        You can then provide either a single additional parameter and the output field will be a 
        randomly picked element from that parameter. In this case the output will be chosen from this 
        one input parameter (which is assumed to be a list of some sort), which can be a string 
        constant list delimited with colons and beginning with CONST:, an INFO field, or a text file 
        specified as FILE:filename. Alternately: you can provide several additional parameters, in 
        which case it will select randomly from the set of parameters.
    seed (String) 
    x (INFO:String|FILE:String|CONST:String) 
    y... (Optional) (INFO:String|CONST:String) 

### SETS\.UNION\(x\.\.\.\)

    
    The new field will be equal to the union of the inputs. Inputs can either be INFO fields 
        specified with 'INFO:tagName', can point to a text file with 'FILE:filename', or can be 
        constants (delimited with colons). The output will be the union of the given parameters, in 
        alphabetical order.
    x... (String|INFO:String|FILE:String) 

### COPY\(oldField\)

    
    
    oldField (INFO:INT|INFO:Float|INFO:String) 

### CONVERT\.FLAG\.TO\.BOOLEAN\(x\)

    
    Input should be a single INFO field of type Flag
    x (CONST:String) 

### SETS\.INTERSECT\(x\.\.\.\)

    
    Input should be a pair of sets that are either INFO fields specified as INFO:tagID, text files 
        specified as FILE:fileName, or a constant set delimited with colons. Output field will be a 
        comma delimited string containing the intersect between the supplied sets.
    x... (INFO:String|FILE:String|CONST:String) 

### CONST\(x\)

    
    Input should be a simple string of characters
    x (CONST:String) 