ParserToolsTmp
==============
version: 0.2.0

In progress parser tools built atop [JTextParser] (https://github.com/TeamworkGuy2/JTextParser), [Jackson] (https://github.com/FasterXML/jackson-core/) (core, databind, annotations) and half a dozen other utility libraries. 

####The goal:
* A competent source code parser that can turn C#, Java, or JavaScript/TypeScript code into a simple AST like structure ('competent' meaning this project aims to support common use cases, not every syntatic feature of the supported languages). 
* A 'code first' parser aimed at manipulating the resulting AST and writing it back as source code.  With the goal of allowing simple language constructs like interfaces and data models to be transpiled to different languages. 

####Not-goals: 
* NOT to create another compiler for C#, Java, or JS/TS. This project's parser expects valid code as input, the few error messages that are present are NOT design to highlight syntax errors in the input. 
* NOT to create a valid AST for each supported language. Rather an AST like structure that supports the lowest common denominator between the targeted languages. This means many AST compromises are inevitable due to differences in language specs. 
