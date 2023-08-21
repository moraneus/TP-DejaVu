```     
████████╗██████╗       ██████╗ ███████╗     ██╗ █████╗ ██╗   ██╗██╗   ██╗
╚══██╔══╝██╔══██╗      ██╔══██╗██╔════╝     ██║██╔══██╗██║   ██║██║   ██║
   ██║   ██████╔╝█████╗██║  ██║█████╗       ██║███████║██║   ██║██║   ██║
   ██║   ██╔═══╝ ╚════╝██║  ██║██╔══╝  ██   ██║██╔══██║╚██╗ ██╔╝██║   ██║
   ██║   ██║           ██████╔╝███████╗╚█████╔╝██║  ██║ ╚████╔╝ ╚██████╔╝
   ╚═╝   ╚═╝           ╚═════╝ ╚══════╝ ╚════╝ ╚═╝  ╚═╝  ╚═══╝   ╚═════╝ 
                                                                                                                                                         
First-order past time LTL with recursive rules, time and pre-evaluation!
Version 4.0, August - 2023
```

  
## Overview

[DejaVu](https://github.com/havelund/dejavu) is a program written in Scala for monitoring event streams (traces) against temporal logic formulas. 
The main formulas are written in a first-order past-time linear temporal logic, with the addition of macros and recursive rules. The logic also supports reasoning about time.
DejaVu contributors are [Klaus Havelund](http://www.havelund.com), [Doron Peled](http://u.cs.biu.ac.il/~doronp) and [Dogan Ulus](https://www.linkedin.com/in/doganulus).
TP-DejaVu is an extension of DejaVu that supports pre-evaluation.
More details about DejaVu and how it operates can be found [here](https://github.com/havelund/dejavu).

## Installing DejaVu:

The directly ``out`` contains files and directories useful for installing and running DejaVu:

* dejavu                          : script to run the system
* artifacts                       : contain the iPRV-dejavu jar file
* papers                          : a directory containing papers published about DejaVu
* examples                        : an example directory containing properties and logs

DejaVu is implemented in Scala. In this version of TP-DejaVu we used Scala 2.11.12.

1. Install the Scala programming language if not already installed (https://www.scala-lang.org/download)
2. Place the files ``dejavu`` and ``dejavu.jar`` mentioned above in some directory **DIR** (standing for the total path to this directory).
3. cd to  **DIR** and make the script executable:

        chmod +x dejavu
     
4. Preferably define an alias in your shell profile to the dejavu script so it can be called from anywhere:

        alias dejavu=DIR/dejavu
        
## Running TP-DejaVu

The script is applied as follows:

    dejavu (--specfile=<filename>) [--prefile=<filename>] (--logfile=<filename>) [--bits=numOfBits] 
           [--mode=(debug | profile)] [--clear=(0 | 1)]

    Options:
        -s, --specfile          the path to a file containing the specification document. This is a mandatory field
        -p, --prefile           the path to a file containing the pre specification document.
        -l, --logfile           the path to a file containing the log in CSV format to be analyzed.
        -b, --bits              number indicating how many bits should be assigned to each variable in the BDD representation. If nothing is specified, the default value is 20 bits.
        -m, --mode              specifies output modes. by default no one is active.
        -c, --clear             indicating whether to clear generated files and folder. value of '1' is for cleaning.

#### Some Execution Examples

* Execute `TP-DejaVu`, allocating `10` bits to each variable.
  ```bash
  ./dejavu --specfile=/path/to/specfile --logfile=/path/to/logfile --bits=10
   ```
* Execute `TP-DejaVu`, allocating `7` bits to each variable, and ensure the clearing of generated files and folders.
  ```bash
  ./dejavu -s=/path/to/specfile --logfile=/path/to/logfile -b=7 --clear=1
  ```

* Execute `TP-DejaVu` in `debug` mode, where certain parameters like `bits` assume default values.
  ```bash
  ./dejavu --specfile=/path/to/specfile -l=/path/to/logfile --mode=debug
  ```

* Initiate the `TP-DejaVu` process, allocating `5` bits to each variable, and set the pre-evaluation process.
  ```bash
  ./dejavu --specfile=/path/to/specfile --prefile=/path/to/specfile --logfile=/path/to/logfile --bits=5
  ```
* Initiate the `TP-DejaVu` process, allocating `5` bits to each variable, and set the pre-evaluation process. 
  ```bash
  ./dejavu -s=/path/to/specfile -p=/path/to/prefile -l=/path/to/logfile -b=5
  ```
  
The `--specfile=<filename>` is the **main** temporal specification that the trace must satisfy, 
which is referred to as the **Specification Document File**. See the explanation of the specification language below.

The `--prefile=<filename>` is the **pre** specification that makes some pre-evaluation on the incoming events. 
Each calculated event, eventually produce a new event for the main property, based on the results of this pre-evaluation step.
See the explanation of the pre specification language below.

**The log file** (``--logfile=<filename>``) should be in comma separated value format (CSV): http://edoceo.com/utilitas/csv-file-format. For example, a file of
the form:

    list,chair,500
    bid,chair,700
    bid,chair,650
    sell,chair

with **no leading spaces** would mean the four events:

    list(chair,500)
    bid(chair,700)
    bid(chair,650)
    sell(chair)

**The bits per variable** (``--bits=numOfBits``):
Indicates how many bits are assigned to each variable in the BDDs. 
This parameter is optional with the default value being 20. If the number is too low an error message will be issued 
during analysis as explained below. A too high number can have impact on the efficiency of the algorithm. Note that the 
number of values representable by N bits is 2^N, so one in general does not need very large numbers.  

The algorithm/implementation will perform garbage collection on allocated
BDDs, re-using BDDs that are no longer needed for checking the property, depending on the form of the formula.

**Debugging** (`--mode=debug`): 
Typically, a low number of bits (e.g., `3`) is chosen for 
debugging purposes. The result is a debugging output that displays the progress of formula 
evaluation for each event and the progress of the prediction, if activated. The output includes 
BDD graphs that can be visualized with GraphViz (http://www.graphviz.org).

**Cleanup of Results and Created Files** (`--clear=1`): 
This flag indicates whether the files and folders generated during the process should 
be deleted. A value of `1` signifies deletion of the files, while the default value, `0`, 
implies that the files will be preserved in the `output` folder. The generated files typically 
include `TraceMonitor.scala`, `ast.dot`, and `dejavu-results`.

## The TP-DejaVu Pre Specification Logic

### The DejaVu Pre-Evaluation Grammar

The grammar rules are outlined as follows:

    <doc> ::= <form>
    
    <form>            ::= [<initiate>] <newline> <on> <newline> <output>
    <newline> ::= \n
    
    <param>           ::= <var> | <const>
    <varType>         ::= <str> | <int> | <double> | <float> | <bool>
    <eventName>       ::= <const>
    <arithmeticop>    ::= '+' | '-' | '/' | '%' | '*' | '^^'
    <assignmentop>    ::= '=' | '+=' | '-=' | '*=' | '/=' | '%='
    <binop>           ::= '->' | '||' | '&&' | '<->' | '^'
    <unop>            ::= '!' | '@'
    <relop>           ::= '<' | '<=' | '==' | '>' | '>=' | '!='
    <functions>       ::= (in | notin) '(' <param> ',' ... ',' <param> ')' 
                         | lte '(' <condition>, <param>, <param> ')' 
                         | abs '(' (<int> | <float> | <double>) ')'
    <initiate>        ::= initiate <newline> <varDefinition> = <const> <newline> ... <varDefinition> = <const> <newline>
    <on>              ::= on <eventName> '(' <varDefinition>, ... , <varDefinition> ')' <newline> <varDefinition> <assignmentop> <expr> <newline> ... <varDefinition> <assignmentop> <expr> <newline>
    <output>          ::= <eventName> '(' <param> ',' ... ',' <param> ')' | 'skip()'
    <varDefinition>   ::= <var> : <varType>

    <expr> ::= 'true'
    | 'false'
    | <param>
    | <expr> <arithmeticop> <expr>
    | <expr> <assignmentop> <expr>
    | <expr> <relop> <expr>
    | <expr> <binop> <expr>
    | <unop> <expr>
    | <functions>

### Operator Meanings by Type

- **Arithmetic Operators (`<arithmeticop>`)**:
  These operate on numeric types like `<int>`, `<float>`, and `<double>`. Examples include addition (`+`) and multiplication (`*`).

- **Assignment Operators (`<assignmentop>`)**:
  These typically modify the value of a variable. For instance, `+=` adds the right value to the left variable and updates the left variable with the result.

- **Binary Operators (`<binop>`)**:
  Binary operations act on two operands. The implication (`->`) and logical "and" (`&&`) are examples.

- **Unary Operators (`<unop>`)**:
  These operate on a single operand. Examples include the negation (`!`) which flips the truth value of a boolean expression.

- **Relational Operators (`<relop>`)**:
  These compare two values. For instance, `<` checks if the left value is less than the right.

- **Functions (`<functions>`)**:
  These are more complex operations. For instance, the `in` function checks for membership in a list, while `abs` returns the absolute value of a number.

### Operators meaning supported by the Pre-Evaluation DejaVu

The different operators `op` and their behaviors on the types `X`, `Y`, and `Z` they act upon are as follows:

    X + Y              : Addition of X and Y
    X - Y              : Subtraction of X from Y
    X * Y              : Multiplication of X and Y
    X / Y              : Division of X by Y
    X % Y              : Modulus (remainder) of X divided by Y

    X == Y             : Equality check between X and Y
    X != Y             : Inequality check between X and Y
    X > Y              : Greater than check of X compared to Y
    X < Y              : Less than check of X compared to Y
    X >= Y             : Greater than or equal to check of X compared to Y
    X <= Y             : Less than or equal to check of X compared to Y
    
    Z = X              : Simple assignment of X to Z
    Z += X             : Add X to Z and assign the result to Z
    Z -= X             : Subtract X from Z and assign the result to Z
    Z *= X             : Multiply Z by X and assign the result to Z
    Z /= X             : Divide Z by X and assign the result to Z
    Z %= X             : Take modulus of Z by X and assign the result to Z

    X && Y             : Logical AND between X and Y
    X || Y             : Logical OR between X and Y
    !X                 : Logical NOT of X
    X ^ Y              : Logical XOR between X and Y

    X.in([a, b, c])    : Checks if X exists in the list [a, b, c]
    X.notin([a, b, c]) : Checks if X does not exist in the list [a, b, c]
    abs(X)             : Absolute value of X

    X -> Y             : Logical implication. If X then Y
    X <-> Y            : Logical biconditional. True only if X and Y have the same boolean value
    X ^^ Y             : Raises X to the power of Y

## Examples Of DejaVu Pre-Evaluation Properties

### Ex1
//TODO: Gives an example....


### Ex2
//TODO: Gives an example....


## Enhancing DejaVu for 2 Phases Evaluation

We have extended DejaVu to include the capability of 2 phase evaluation..... 


## Modifying the DejaVu Runtime Verification Tool

This part provides a brief guide on how to modify the DejaVu Runtime Verification (RV) tool. 
The primary functions of DejaVu are found in three main source files: [Verify.scala](https://github.com/moraneus/TP-DejaVu/blob/master/src/main/scala/dejavu/Verify.scala), [Monitor.scala](https://github.com/moraneus/TP-DejaVu/blob/master/src/main/scala/dejavu/Monitor.scala), and [Ast.scala](https://github.com/moraneus/TP-DejaVu/blob/master/src/main/scala/dejavu/Ast.scala).

1. The `Verify.scala` file manages the creation process of the `TraceMonitor.scala` file. 
It generates this monitor based on the user-provided specifications. Additionally, 
it is responsible for executing the resulting `TraceMonitor.scala`.

2. The `Monitor.scala` file contains common monitoring code used across all properties. 
Importantly, `Monitor.scala` should always be identical to `Monitor.txt` to ensure consistency.

3. The `Ast.scala` file has a crucial role in parsing the spec file provided by the user. 
It generates the appropriate functions to be run on the `TraceMonitor.scala`.

### Guide to Modifying

When `TraceMonitor.scala` is created during the generation process, it uses code templates found in the aforementioned files. 
If you want to modify the final output of `TraceMonitor.scala`, you'll need to make changes in these source files. 
This often involves altering the existing code templates or adding new ones as per your needs.

### Build a New DejaVu.jar Artifact in IntelliJ

After making modifications to the DejaVu Runtime Verification (RV) tool, you may want to create a new `dejavu.jar` artifact. Here are the steps to take this using IntelliJ:

#### Pre-requisites

Before you begin, please ensure you have Scala version 2.11.x installed on your computer. DejaVu was developed on Scala version 2.11.12, so using a different minor version may cause unexpected issues.

#### Steps

1. **Open Project in IntelliJ**: Navigate to your project directory and open the project in IntelliJ.

2. **Setup Scala SDK**: Go to `File -> Project Structure -> Global Libraries`, then add the Scala 2.11.12 SDK.

3. **Create Artifact Configuration**: Go to `File -> Project Structure -> Artifacts`, click on the `+` button, and select `JAR -> From modules with dependencies`.

4. **Select Module and Main Class**: A new window will appear. Select the main module and the main class for the JAR artifact ([Verify.scala](https://github.com/moraneus/iPRV-DejaVu/blob/master/src/main/scala/dejavu/Verify.scala)).

5. **Finalize Artifact Configuration**: IntelliJ will automatically select the output directory for the artifact (it can be modified by you). Click `OK` to save the configuration.

6. **Build Artifact**: Go to `Build -> Build Artifacts`, select `dejavu`, and then select `Build`.

The `dejavu.jar` file will be created in the output directory you specified in the artifact configuration.

#### ⚠️ Important Note on Scala Versions

DejaVu is based on an older version of Scala (2.11.12). If you're using a newer version of Scala, you might encounter compatibility issues. Therefore, it's crucial to ensure you have Scala 2.11.x installed and selected as the SDK in IntelliJ before you proceed with the artifact creation.


## Contributors - For Original DejaVu
* [Klaus Havelund](http://www.havelund.com), Jet Propulsion Laboratory/NASA, USA
* [Doron Peled](http://u.cs.biu.ac.il/~doronp), Bar Ilan University, Israel
* [Dogan Ulus](https://www.linkedin.com/in/doganulus), Boston University, USA

## Contributors - For TP-DejaVu
* [Doron Peled](http://u.cs.biu.ac.il/~doronp), Bar Ilan University, Israel
* [Moran Omer](https://github.com/moraneus), Bar Ilan University, Israel
* Panagiotis Katsaros
* Tasos Temperekidis