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
TP-DEJAVU is an enhanced version of the DEJAVU tool, designed to manage two-phase Runtime Verification (RV) processing. 
In the first phase, which is implemented in Scala, operational RV is carried out, allowing for arithmetic, string, and Boolean manipulations. 
This phase leverages a straightforward syntax that facilitates the updating of summary variables. 
The second phase, based on the DEJAVU tool, and performs monitoring against a first-order specification.
More details about DejaVu and how it operates can be found [here](https://github.com/havelund/dejavu).

## Installing DejaVu:

The directly ``out`` contains files and directories useful for installing and running DejaVu:

* examples                        : An example directory containing properties and logs (DejaVu + TP-DejaVu)
* dir                             : Script to run the system and tpdejavu.jar file

DejaVu is implemented in Scala. In this version of TP-DejaVu we used Scala 2.11.12.

1. Install the Scala programming language if not already installed (https://www.scala-lang.org/download)
2. Place the files ``dejavu`` and ``tpdejavu.jar`` mentioned above in some directory **DIR** (standing for the total path to this directory).
3. cd to  **DIR** and make the script executable:

        chmod +x dejavu
     
4. Preferably define an alias in your shell profile to the dejavu script so it can be called from anywhere:

        alias dejavu=DIR/dejavu
        
## Running TP-DejaVu

The script is applied as follows:

    Usage:
    dejavu --specfile=<filename> --logfile=<filename> [OPTIONS]
    
    Options:
    -s, --specfile=<filename>       Path to the specification document. (Mandatory)
    -p, --prefile=<filename>        Path to the pre-specification document. (Optional)
    -l, --logfile=<filename>        Path to the CSV log file to be analyzed. (Mandatory)
    -b, --bits=<numOfBits>          Number of bits for each variable in the BDD representation. (Default: 20 bits)
    -m, --mode=(debug|profile)      Set the output mode. (Default: None)
    -st, --stat=(true|false)        Print violations if set to true. (Optional)
    -c, --clear=(0|1)               Clear generated files and folders. Set to '1' for cleaning. (Optional)
    
    Examples:
    dejavu --specfile=spec.txt --logfile=log.csv
    dejavu --specfile=spec.txt --logfile=log.csv --bits=16 --mode=debug --stat=true --clear=1


#### Some Execution Examples

* Execute `TP-DejaVu`, allocating `10` bits to each variable (In this example we just execute DejaVu).
  ```bash
  ./dejavu --specfile=/path/to/specfile --logfile=/path/to/logfile --bits=10
   ```
* Execute `TP-DejaVu`, allocating `7` bits to each variable, and ensure the clearing of generated files and folders (In this example we just execute DejaVu).
  ```bash
  ./dejavu -s=/path/to/specfile --logfile=/path/to/logfile -b=7 --clear=1
  ```

* Execute `TP-DejaVu` in `debug` mode, where certain parameters like `bits` assume default values (In this example we just execute DejaVu).
  ```bash
  ./dejavu --specfile=/path/to/specfile -l=/path/to/logfile --mode=debug
  ```
* Initiate the `TP-DejaVu` process, allocating `20` bits to each variable, and set the pre-evaluation process in the compiled monitor, but without trace validation.
  ```bash
  ./dejavu --specfile=/path/to/specfile --prefile=/path/to/specfile --bits=20
  
* Initiate the `TP-DejaVu` process, allocating `5` bits to each variable, and set the pre-evaluation process.
  ```bash
  ./dejavu --specfile=/path/to/specfile --prefile=/path/to/specfile --logfile=/path/to/logfile --bits=5
  ```
* Initiate the `TP-DejaVu` process, allocating `5` bits to each variable, and set the pre-evaluation process. 
  ```bash
  ./dejavu -s=/path/to/specfile -p=/path/to/prefile -l=/path/to/logfile -b=5
  ```

* Same as previous, but now without printing violation messages. This relevant for anyone who want to get only the summary.
  ```bash
  ./dejavu -s=/path/to/specfile -p=/path/to/prefile -l=/path/to/logfile -b=5, --stat=false
  ```
  
The `--specfile=<filename>` is the **declarative** first-order specification that the trace must satisfy. 
See the explanation of the specification language in [DejaVu](https://github.com/havelund/dejavu).

The `--prefile=<filename>` is the **operational** specification that makes some pre-evaluation on the incoming events. 
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

## TP-DejaVu Operational Specification Logic

### TP-DejaVu Grammar

The grammar rules are outlined as follows:

    <specification>           ::= <initiate_section>? <update_section>*

    <initiate_section>        ::= "initiate" <assignment>+
    <update_section>          ::= "on" <predicate> <assignment>* <output_statement>?
    
    <predicate>               ::= <predicate_name> "(" <arg_type_list> ")"
    <arg_type_list>           ::= <arg_type> ("," <arg_type>)*
    <arg_type>                ::= <variable_name> ":" <data_type>
    <predicate_name>          ::= [a-zA-Z_][a-zA-Z0-9_]*
    <variable_name>           ::= [a-zA-Z_][a-zA-Z0-9_]*
    
    <assignment>              ::= <variable_name> ":=" <expression>
    
    <expression>              ::= <arithmetic_expression>
                                | <boolean_expression>
                                | <string_expression>
                                | "@" <variable_name>
                                | "ite" "(" <boolean_expression> "," <expression> "," <expression> ")"
    
    <in_function>             ::= <expression> "in" <item_list>
    <item_list>               ::= "[" <expression_list> "]"
    <expression_list>         ::= <expression> ("," <expression>)*
    
    <arithmetic_expression>   ::= <term> <arithmetic_operator> <term>
                                | <term>
    <term>                    ::= <factor>
                                | <abs_function>
                                | <number_value>
                                | <variable_name>

    <factor>                  ::= "(" <arithmetic_expression> ")"
    <abs_function>            ::= "abs(" <term> ")"
    
    <boolean_expression>      ::= <arithmetic_expression> <comparison_operator> <arithmetic_expression>
                                | <logical_expression>
                                | <in_function>

    <boolean_term>            ::= <boolean_value> | <variable_name>
    <logical_expression>      ::= <boolean_term> <logical_operator> <boolean_term>
                                | <unary_logical_operator> <boolean_expression>
    
    <string_expression>       ::= <string_operation> | <string_term>
    <string_operation>        ::= <string_term> "+" <string_term>
                                | <string_term> ".substring(" <integer> "," <integer> ")"
                                | <string_term> ".length()"  
                                | <string_term> ".indexOf(" <string_value> ")"
                                | <string_term> "==" <string_term>
                                | <string_term> "!=" <string_term>

    <string_term>             ::= <string_value> | <variable_name>
    
    <output_statement>        ::= "output" <predicate_name> "(" <arg_list> ")"
    <arg_list>                ::= <argument> ("," <argument>)*
    <argument>                ::= <value> | <variable_name>
    
    <data_type>               ::= "int" | "bool" | "string" | "float" | "double"
    
    <value>                   ::= <number_value> | <boolean_value> | <string_value>
    <number_value>            ::= <integer> | <float> | <double>
    <integer>                 ::= [0-9]+
    <boolean_value>           ::= "true" | "false"
    <string_value>            ::= "\"" .* "\""
    <float>                   ::= [0-9]+ "." [0-9]+ ?[fF]
    <double>                  ::= [0-9]+ "." [0-9]+
    
    <logical_operator>        ::= "&&" | "||" | "->" | "<->" | "^"
    <unary_logical_operator>  ::= "!"
    <comparison_operator>     ::= ">" | "<" | ">=" | "<=" | "==" | "!="
    <arithmetic_operator>     ::= "+" | "-" | "*" | "/" | "%" | "^^"


### Operator Meanings by Type

- **Arithmetic Operators (`<arithmetic_operator>`)**:
  These operate on numeric types like `<integer>`, `<float>`, and `<double>`. Examples include addition (`+`) and multiplication (`*`).

- **Logical Operators (`<logical_operator>`)**:
  Binary operations act on two operands. The implication (`->`) and logical "and" (`&&`) for examples.

- **Unary Operators (`<unary_logical_operator>`)**:
  These operate on a single operand. Examples include the negation (`!`) which flips the truth value of a boolean expression.

- **Relational Operators (`<comparison_operator>`)**:
  These compare two values. For instance, `<` checks if the left value is less than the right.

### Operators meaning supported by the TP-DejaVu

The different operators `op` and their behaviors on the variables `X` and `Y`:

    X + Y              : Addition of X and Y
    X - Y              : Subtraction of X from Y
    X * Y              : Multiplication of X and Y
    X / Y              : Division of X by Y
    X % Y              : Modulus (remainder) of X divided by Y
    X ^^ Y             : Raises X to the power of Y

    X == Y             : Equality check between X and Y
    X != Y             : Inequality check between X and Y
    X > Y              : Greater than check of X compared to Y
    X < Y              : Less than check of X compared to Y
    X >= Y             : Greater than or equal to check of X compared to Y
    X <= Y             : Less than or equal to check of X compared to Y

    X && Y             : Logical AND between X and Y
    X || Y             : Logical OR between X and Y
    !X                 : Logical NOT of X
    X ^ Y              : Logical XOR between X and Y

    X in [a, b, c]     : Checks if X exists in the list [a, b, c]
    abs(X)             : Absolute value of X

    X -> Y             : Logical implication. If X then Y
    X <-> Y            : Logical biconditional. True only if X and Y have the same boolean value


## Usage Examples Of TP-DejaVu

In this section we present an examples that use TP-DejaVu properties, and their corresponding DejaVu equivalents.
We also provide experimental results for comparing the efficiency of properties expressed for TP-DejaVu and for DejaVu.
Our benchmarks exclusively concentrated on assessing time and memory consumption during the evaluation phase, without the compilation time.
Executions where the evaluation process exceeded 1000 seconds are marked with the symbol ∞.

### Example 1

#### Dejavu
```
forall x . ((p(x) & x > 7) -> exists y . P q(x, y))
```

#### TP-DejaVu

```
on p(x: int)
  in_bound: bool := x > 7
  output p(x, in_bound)
  

forall x . (p(x, "true") -> exists y . q(x,y))  
```

#### Comparative test

<table style="font-size: smaller; width: 100%; text-align: center;">
    <thead>
        <tr>
            <th>Property</th>
            <th>Method</th>
            <th>Trace 10K</th>
            <th>Trace 100K</th>
            <th>Trace 500K</th>
            <th>Trace 1M</th>
            <th>Trace 5M</th>
        </tr>
    </thead>
    <tbody>
        <!-- Ex 1 -->
        <tr>
            <td rowspan="2">Ex 1</td>
            <td><strong>DejaVu</strong></td>
            <td>0.64s<br>125.26MB</td>
            <td>1.31s<br>335.52MB</td>
            <td>4.76s<br>1.10GB</td>
            <td>8.85s<br>1.88GB</td>
            <td>185.65s<br>3.59GB</td>
        </tr>
        <tr>
            <td><strong>TP-DejaVu</strong></td>
            <td>0.54s<br>129.74MB</td>
            <td>0.96s<br>311.24MB</td>
            <td>3.25s<br>858.30MB</td>
            <td>5.44s<br>1.21GB</td>
            <td>41.18s<br>5.70GB</td>
        </tr>
</table>

### How to Execute the Example

1. Ensure you have the following files in your local environment:
  - `dejavu`
  - `tpdejavu.jar`

2. Clone the [experiment directory](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example1) and place the above files inside it.

3. Run the following command:

```bash
./dejavu --specfile=spec_modified.qtl --logfile=log_10K.csv --bits=20 --prefile=spec.pqtl
```

### Required Files for Execution:

- [`spec.pqtl`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example1/spec.pqtl)
  - **Description**: The operational spec file.

- [`spec_modified.qtl`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example1/spec_modified.qtl)
  - **Description**: The declarative spec file.

- [`log_10K.csv`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example1/log_10K.csv)
  - **Description**: The trace file containing 10K events.

### Example 2
#### Dejavu
```
forall x . forall y . ((p(x) & @q(y) & x < y) -> P r(x, y))
```

#### TP-DejaVu

```
initiate
  prev_q: bool := false

on p(x: int)
  x_lt_y: bool := x < y
  prev_q: bool := false
  output p(x, x_lt_y)

on q(y: int)
  prev_q: bool := true
  output q(y)
  

forall x . forall y . ((p(x, "true") & @q(y)) -> P r(x, y))
```

#### Comparative test

<table style="font-size: smaller; width: 100%; text-align: center;">
    <thead>
        <tr>
            <th>Property</th>
            <th>Method</th>
            <th>Trace 10K</th>
            <th>Trace 100K</th>
            <th>Trace 500K</th>
            <th>Trace 1M</th>
            <th>Trace 5M</th>
        </tr>
    </thead>
    <tbody>
        <!-- Ex 2 -->
        <tr>
            <td rowspan="2">Ex 2</td>
            <td><strong>DejaVu</strong></td>
            <td>∞</td>
            <td>∞</td>
            <td>∞</td>
            <td>∞</td>
            <td>∞</td>
        </tr>
        <tr>
            <td><strong>TP-DejaVu</strong></td>
            <td>0.56s<br>135.18MB</td>
            <td>1.12s<br>308.72MB</td>
            <td>4.12s<br>1.17GB</td>
            <td>7.54s<br>1.78GB</td>
            <td>56.78s<br>3.55GB</td>
        </tr>
</table>

### How to Execute the Example

1. Ensure you have the following files in your local environment:
- `dejavu`
- `tpdejavu.jar`

2. Clone the [experiment directory](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example2) and place the above files inside it.

3. Run the following command:

```bash
./dejavu --specfile=spec_modified.qtl --logfile=log_1M.csv --bits=20 --prefile=spec.pqtl
```

### Required Files for Execution:

- [`spec.pqtl`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example2/spec.pqtl)
  - **Description**: The operational spec file.

- [`spec_modified.qtl`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example2/spec_modified.qtl)
  - **Description**: The declarative spec file.

- [`log_1M.csv`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example2/log_1M.csv)
  - **Description**: The trace file containing 1M events.

### Example 3
#### Dejavu
```
forall x . ( p(x) -> (forall y . ( @P q(y) -> x > y) & exists z . @  P q(z) ) )
```

#### TP-DejaVu

```
initiate
    MaxY: int := -1
    
on p(x: int)
   xGTy: bool := x > MaxY
   output p(x, xGTy)

on q(y: int)
   NewMaxY: bool := @MaxY < y
   MaxY: int := ite(NewMaxY, y, @MaxY)
   output q(y)
  

forall x . forall z . (p(x, z) -> (p(x, "true") & exists y . @ P q(y)))
```

#### Comparative test

<table style="font-size: smaller; width: 100%; text-align: center;">
    <thead>
        <tr>
            <th>Property</th>
            <th>Method</th>
            <th>Trace 10K</th>
            <th>Trace 100K</th>
            <th>Trace 500K</th>
            <th>Trace 1M</th>
            <th>Trace 5M</th>
        </tr>
    </thead>
    <tbody>
        <!-- Ex 3 -->
        <tr>
            <td rowspan="2">Ex 3</td>
            <td><strong>DejaVu</strong></td>
            <td>∞</td>
            <td>∞</td>
            <td>∞</td>
            <td>∞</td>
            <td>∞</td>
        </tr>
        <tr>
            <td><strong>TP-DejaVu</strong></td>
            <td>0.64s<br>119.85MB</td>
            <td>0.90s<br>326.38MB</td>
            <td>2.39s<br>738.78MB</td>
            <td>4.08s<br>1.11GB</td>
            <td>21.30s<br>3.43GB</td>
        </tr>
</table>

### How to Execute the Example

1. Ensure you have the following files in your local environment:
- `dejavu`
- `tpdejavu.jar`

2. Clone the [experiment directory](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example3) and place the above files inside it.

3. Run the following command:

```bash
./dejavu --specfile=spec_modified.qtl --logfile=log_100K.csv --bits=20 --prefile=spec.pqtl
```

### Required Files for Execution:

- [`spec.pqtl`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example3/spec.pqtl)
  - **Description**: The operational spec file.

- [`spec_modified.qtl`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example3/spec_modified.qtl)
  - **Description**: The declarative spec file.

- [`log_100K.csv`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example3/log_100K.csv)
  - **Description**: The trace file containing 100K events.

### Example 4
#### Dejavu
```
forall x . ((p(x) & x >= 0 & x <= 100) -> exists y . ( q(x, y) & ((y >= 0 & y <= 100) | (y <= 0 & y >= -100))))
```

#### TP-DejaVu

```
on p(x: int)
    x_in_bound: bool := (x >= 0) && (x <= 100)
    output p(x, x_in_bound)

on q(x: int, y: int)
    y_in_bound: bool := (abs(y) >= 0) && (abs(y) <= 100)
    output q(x, y, y_in_bound)
  

forall x . ((p(x, "true") -> exists y . q(x, y, "true")))
```

#### Comparative test

<table style="font-size: smaller; width: 100%; text-align: center;">
    <thead>
        <tr>
            <th>Property</th>
            <th>Method</th>
            <th>Trace 10K</th>
            <th>Trace 100K</th>
            <th>Trace 500K</th>
            <th>Trace 1M</th>
            <th>Trace 5M</th>
        </tr>
    </thead>
    <tbody>
        <!-- Ex 4 -->
        <tr>
            <td rowspan="2">Ex 4</td>
            <td><strong>DejaVu</strong></td>
            <td>0.52s<br>114.17MB</td>
            <td>0.75s<br>216.72MB</td>
            <td>1.29s<br>355.31MB</td>
            <td>2.05s<br>374.48MB</td>
            <td>13.01s<br>2.01GB</td>
        </tr>
        <tr>
            <td><strong>TP-DejaVu</strong></td>
            <td>0.46s<br>97.29MB</td>
            <td>0.62s<br>145.55MB</td>
            <td>0.98s<br>231.71MB</td>
            <td>1.21s<br>289.05MB</td>
            <td>5.07s<br>230.99MB</td>
        </tr>
</table>

### How to Execute the Example

1. Ensure you have the following files in your local environment:
- `dejavu`
- `tpdejavu.jar`

2. Clone the [experiment directory](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example4) and place the above files inside it.

3. Run the following command:

```bash
./dejavu --specfile=spec_modified.qtl --logfile=log_500K.csv --bits=20 --prefile=spec.pqtl
```

### Required Files for Execution:

- [`spec.pqtl`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example1/spec.pqtl)
  - **Description**: The operational spec file.

- [`spec_modified.qtl`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example1/spec_modified.qtl)
  - **Description**: The declarative spec file.

- [`log_500K.csv`](https://github.com/moraneus/TP-DejaVu/blob/master/src/test/scala/experiments/tp/example1/log_500K.csv)
  - **Description**: The trace file containing 500K events.


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

### Build a New `tpdejaVu.jar` Artifact in IntelliJ

After making modifications to the DejaVu Runtime Verification (RV) tool, you may want to create a new `tpdejavu.jar` artifact. Here are the steps to take this using IntelliJ:

#### Pre-requisites

Before you begin, please ensure you have Scala version 2.11.x installed on your computer. DejaVu was developed on Scala version 2.11.12, so using a different minor version may cause unexpected issues.

#### Steps

1. **Open Project in IntelliJ**: Navigate to your project directory and open the project in IntelliJ.

2. **Setup Scala SDK**: Go to `File -> Project Structure -> Global Libraries`, then add the Scala 2.11.12 SDK.

3. **Create Artifact Configuration**: Go to `File -> Project Structure -> Artifacts`, click on the `+` button, and select `JAR -> From modules with dependencies`.

4. **Select Module and Main Class**: A new window will appear. Select the main module and the main class for the JAR artifact ([Verify.scala](https://github.com/moraneus/iPRV-DejaVu/blob/master/src/main/scala/dejavu/Verify.scala)).

5. **Finalize Artifact Configuration**: IntelliJ will automatically select the output directory for the artifact (it can be modified by you). Click `OK` to save the configuration.

6. **Build Artifact**: Go to `Build -> Build Artifacts`, select `dejavu`, and then select `Build`.

The `tpdejavu.jar` file will be created in the output directory you specified in the artifact configuration.

#### ⚠️ Important Note on Scala Versions

DejaVu is based on an older version of Scala (2.11.12). If you're using a newer version of Scala, you might encounter compatibility issues. Therefore, it's crucial to ensure you have Scala 2.11.x installed and selected as the SDK in IntelliJ before you proceed with the artifact creation.

## Contributors - For TP-DejaVu (Ordered by last name):
* [Klaus Havelund](http://www.havelund.com), Jet Propulsion Laboratory/NASA, USA
* [Panagiotis Katsaros](https://depend.csd.auth.gr/users/katsaros), Aristotle University of Thessaloniki, Greece
* [Moran Omer](https://github.com/moraneus), Bar Ilan University, Israel
* [Doron Peled](http://u.cs.biu.ac.il/~doronp), Bar Ilan University, Israel
* [Anastasios Temperekidis](https://github.com/tasosxak), Aristotle University of Thessaloniki, Greece