ParserToolsTmp
==============

In progress parser tools built atop [JTextParser] (https://github.com/TeamworkGuy2/JTextParser) and half a dozen other utility libraries. 

####The goal:
* A competent source code parser that can turn C#, Java, or JavaScript/TypeScript code into a simple AST like structure. 
* A 'code first' parser aimed at manipulating the resulting AST and writing it back as source code.  With the goal of allow very simple language constructs like interfaces to be transpiled to different languages. 

####Not-goals: 
* To create another compiler for C#, Java, or JS/TS, this project's parser expects valid code as input, error messages are not implemented to help highlight syntax errors in the input. 
* To create a valid AST for each supported language, rather a AST like structure that supports the lowest common denominator between the targeted languages. This means many AST compromises from language specs are inevitable. 
