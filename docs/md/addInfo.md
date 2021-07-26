
# INFO TAG FUNCTIONS

    
    Info Tag Functions are simple modular functions that take one 
        variant at a time and add a new INFO field.
    Basic Syntax:
    --FCN addInfoTag|newTagID|fcn=infoTagFunction|params=p1,p2,... 
        

## Available Functions:

    

### LN\(x\)

    
    Input should be a numeric INFO field. output will be the 
        natural log of that field.
    x (INT|FLOAT|INFO:Int|INFO:Float) 

### MULT\(x,y\)

    
    Input should be a pair of info fields and/or numeric constants 
        (which must be specified as CONST:n). Output field will be 
        the product of the two inputs. Missing INFO fields will be 
        treated as ZEROS unless all params are INFO fields and all 
        are missing, in which case the output will be missing. 
        Output field type will be an integer if all inputs are 
        integers and otherwise a float.
    x (INT|FLOAT|INFO:Int|INFO:Float) 
    y (INT|FLOAT|INFO:Int|INFO:Float) 

### DIFF\(x,y\)

    
    Input should be a pair of info fields and/or numeric constants 
        (which must be specified as CONST:n). Output field will be 
        the difference of the two inputs (ie x - y). Missing INFO 
        fields will be treated as ZEROS unless all params are INFO 
        fields and all are missing, in which case the output will 
        be missing. Output field type will be an integer if all 
        inputs are integers and otherwise a float.
    x (INT|FLOAT|INFO:Int|INFO:Float) 
    y (INT|FLOAT|INFO:Int|INFO:Float) 

### SUM\(x\.\.\.\)

    
    Input should be a set of info tags and/or numeric constants 
        (which must be specified as CONST:n). Output field will be 
        the sum of the inputs. Missing INFO fields will be treated 
        as zeros unless all params are INFO fields and all are 
        missing, in which case the output will be missing. Output 
        field type will be an integer if all inputs are integers 
        and otherwise a float.
    x... (INT|FLOAT|INFO:Int|INFO:Float) 

### FLAGSET\(x\.\.\.\)

    
    Input should be a set of infoFields and optionally a name, with 
        the format tagID:name or just tagID. If names are omitted, 
        then the name will be equal to the tagID. Output field will 
        be the set of names for which the respective info field is 
        equal to 1. Any value other than 1, including missing 
        fields, will be treated as 0.
    x... (INFO:Int) 

### LEN\(x\)

    
    The new field will be an integer field equal to the length of 
        the input field. Will be missing if the input field is 
        missing.
    x (INFO:String,INFO:Int,INFO:Float) 

### RANDFLAG\(x,seed\)

    
    
    x (FLOAT) 
    seed (INT) 

### DIV\(x,y\)

    
    Input should be a pair of info fields and/or numeric constants 
        (which must be specified as CONST:n). Output field will be 
        the product of the two inputs. Missing INFO fields will be 
        treated as ZEROS unless all params are INFO fields and all 
        are missing, in which case the output will be missing. 
        Output field type will be a float.
    x (INT|FLOAT|INFO:Int|INFO:Float) 
    y (INT|FLOAT|INFO:Int|INFO:Float) 

### MIN\(x\.\.\.\)

    
    
    x... (INT|FLOAT|INFO:Int|INFO:Float) 

### CONVERT\.TO\.INT\(x,defaultValue\)

    
    Input should be an INFO field. Converts field to a Integer.
    x (INFO:String) 
    defaultValue (Optional) (Int) 

### CONVERT\.TO\.FLOAT\(x,defaultValue\)

    
    Input should be an INFO field. Converts to a numeric float. If 
        no defaultValue is supplied then non-floats will be 
        dropped. Note that NaN and Inf will be dropped / replaced 
        with the default.
    x (INFO:String) 
    defaultValue (Optional) (Float) 

### EXPR\(expr\)

    
    The new field will be an integer field which will be equal to 1 
        if and only if the expression is TRUE, and 0 otherwise. See 
        the expression format definition for more information on 
        how the logical expression syntax works.
    expr (STRING) 

### DECODE\(x,decoder\)

    
    
    x (INFO:String) 
    decoder (FILE:String) 

### CONCAT\(x\.\.\.\)

    
    This simple function concatenates the values of the input 
        parameters. Input parameters can be any combination of INFO 
        fields or constant strings.
    x... (String|INFO:String) 

### LOG10\(x\)

    
    Input should be a numeric INFO field. output will be the log10 
        of that field.
    x (INT|FLOAT|INFO:Int|INFO:Float) 

### PRODUCT\.ARRAY\(x\.\.\.\)

    
    Input should be a set of info fields and/or numeric constants 
        (which must be specified as CONST:n). Output field will be 
        the product of the inputs. Missing INFO fields will be 
        treated as ones unless all params are INFO fields and all 
        are missing, in which case the output will be missing. 
        Output field type will be an integer if all inputs are 
        integers and otherwise a float.
    x... (INT|FLOAT|INFO:Int|INFO:Float) 

### SETS\.DIFF\(x,y\)

    
    Input should be a pair of sets that are either INFO fields 
        specified as INFO:tagID, text files specified as 
        FILE:fileName, or a constant set delimited with 
        colons.Output field will be a comma delimited string 
        containing the elements in the first set with the second 
        set subtracted out.
    x 
        (String|INFO:String|INT|FLOAT|INFO:Int|INFO:Float|FILE:Stri-
        ng)
    y 
        (String|INFO:String|INT|FLOAT|INFO:Int|INFO:Float|FILE:Stri-
        ng)

### SWITCH\.EXPR\(expr,A,B\)

    
    Switches between two options depending on a logical expression. 
        The 'expr' expression parameter must be formatted like 
        standard variant-level expressions. The A and B parameters 
        can each be either a constant or an INFO field. The output 
        field will be equal to A if the logical expression is TRUE, 
        and otherwise will be B.
    expr (STRING) 
    A (INFO:Int|INFO:Float|INFO:String|Int|Float|String) 
    B (Optional) (INFO:Int|INFO:Float|INFO:String|Int|Float|String) 
        

### GT\.EXPR\(gtExpr,varExpr\)

    
    The new field will be an integer field which will be equal to 
        the number of samples that satisfy a given genotype-level 
        expression. See the expression format definition for more 
        information on how the logical expression syntax works. You 
        can also specify a variant-level expression which, if 
        false, will return missing.
    gtExpr (Optional) (STRING) 
    varExpr (Optional) (STRING) 

### MAX\(x\.\.\.\)

    
    
    x... (INT|FLOAT|INFO:Int|INFO:Float) 

### PICK\.RANDOM\(seed,x,y\.\.\.\)

    
    The first parameter must be either '.' or a supplied random 
        seed for the random number generator. You can then provide 
        either a single additional parameter and the output field 
        will be a randomly picked element from that parameter. In 
        this case the output will be chosen from this one input 
        parameter (which is assumed to be a list of some sort), 
        which can be a string constant list delimited with colons 
        and beginning with CONST:, an INFO field, or a text file 
        specified as FILE:filename. Alternately: you can provide 
        several additional parameters, in which case it will select 
        randomly from the set of parameters.
    seed (String) 
    x (String|INFO:String|FILE:String) 
    y... (Optional) (String|INFO:String) 

### SETS\.UNION\(x\.\.\.\)

    
    The new field will be equal to the union of the inputs. Inputs 
        can either be INFO fields specified with 'INFO:tagName', 
        can point to a text file with 'FILE:filename', or can be 
        constants (delimited with colons). The output will be the 
        union of the given parameters, in alphabetical order.
    x... (String|INFO:String|FILE:String) 

### COPY\(oldField\)

    
    
    oldField (INFO:INT|INFO:Float|INFO:String) 

### SETS\.INTERSECT\(x\.\.\.\)

    
    Input should be a pair of sets that are either INFO fields 
        specified as INFO:tagID, text files specified as 
        FILE:fileName, or a constant set delimited with colons. 
        Output field will be a comma delimited string containing 
        the intersect between the supplied sets.
    x... (String|INFO:String|FILE:String) 