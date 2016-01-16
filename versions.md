--------
####0.3.0
date: 2016-1-16

commit: ?

* __Added Java parsing support and tests.__
* Added Keyword interface for generic language keyword operations such as isKeyword(), isBlockModifierKeyword(), isDataTypeKeyword(), etc.
* Made interm parsing more generic, added a bunch of parser creator functions to AstExtractor.  Converted some of the C# interm parsers into more generic parsers to be used by C# and Java (see BaseFieldExtractor, BaseMethodExtractor, BaseMethodParametersParser)
* Renamed getFullyQualifyingName() methods to getFullName(), (note: this may change again in future, possibly to getFqName())
* Moved and renamed ParserCondition, Precondition -> ParserFactory, TokenParserCondition -> TokenParser, CharParserCondition -> CharParser, and ParserStartChars to the [JTextParser] (https://github.com/TeamworkGuy2/JTextParser) project
* Simplified CharConditions and StringConditions sub-class names (i.e. StringConditions.StringStartFilter -> StringConditions.Start)


--------
####0.2.1
date: 2016-1-13

commit: a33f37ad6a116e7e697498af88327dfaa46709a0

* Added simple C# parse example with resulting JSON.  Added and refactored some test cases.


--------
####0.2.0:
date: 2016-1-12

commit: 5b692bc3476ca94c6dedb5b6424d1319fcad2057

* __Finished command line interface (CLI) argument parsing for ['sources', 'destinations', 'log'] and ParserWorkflow.__
* Simplified CodeFileSrc generic signature. Added interfaces and setup entire parsing process to be generic (added CodeLanguage.getExtractor() and AstExtractor interface).
* Refactored C# classes to support this more generic approach.
* Removed IntermClass.getBlockTree(), it should be tracked higher up in the parsing process.
* Added Simple and Resolved sub-classes of ProjectClassSet. Renamed CsMain -> ParserMain.
* Added some additional EclipseClasspathFile/Utils methods for finding project dependencies.
