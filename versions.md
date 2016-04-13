--------
####0.10.0
date: 2016-4-12

commit: ?

* Added better annotation parsing, including support for negative numbers as arguments
* Added CodeFragment which extends 'DocumentFragmentText<CodeFragmentType>' so don't have to keep typing that every time, updated most of the code to use CodeFragment
* Added OperatorUtil and Operator (with C# and Java implementation enums) similar to the existing Keyword enums
* Refactored how we use EnumSubSet and enum sub-categorization
   * Added CodeFragmentEnumSubSet with is() and parse() methods which accept CodeFragment nodes (we were starting to duplicate this parsing code in Keyword and the new Operator class, so moved it to a reusable class)
   * Removed KeywordUtil isXyzKeyword() and parseXyzKeyword() methods in favor of methods that return CodeFragmentEnumSubSet instances for each of the keyword categories (i.e. 'blockModifiers()' or 'operators()')
* Moved twg2.parser.codeParser extractor classes (i.e. AccessModifierExtractor or BlockExtractor) to new twg2.parser.codeParser.extractors package


--------
####0.9.0
date: 2016-3-20

commit: 679778bafd13a413854bd169cabe747b12bbc894

* Added commented parsing for comments attached to methods and fields (future TODO: add comment parsing for comments attached to classes and namespaces)
* Renamed intermAst packages to 'twg2.ast.interm'
* Removed 'interm' from most AST class names
* Moved type resolution out of AST classes into new 'twg2.parser.resolver' classes (i.e. ClassSigResolver, FieldSigResolver, etc.)
* Created 'twg2.parser.language' package for code language management classes


--------
####0.8.0
date: 2016-2-28

commit: 32ee2a5ec5c218d3f90d1438f893a86e34b9c716

Move from assuming that conditions can list the initial chars that match them (CharParser.WithMarks.getMatchFirstChars()) to CharParserMatchable and new getFirstCharMatcher() method which allows for a flexible definition of matching first chars
* Moved/renamed ParserWorkFlow SourceInfo and LoadResult \(renamed to SourceFiles) nested classes and ParserMain.getFilesByExtension() to [JFileIo] (https://github.com/TeamworkGuy2/JFileIo) library
* Moved twg2.parser.output JsonWrite and JsonWritable to JFileIO project's twg2.io.write package
* Renamed ParserMain -> ParserMisc
* Implemented new CharParserMatchable with getFirstCharMatcher() methods in place of old CharParser.WithMarks interface from JTextParser
* Updated to use latest version of JFileIo and JTextParser


--------
####0.7.0
date: 2016-2-24

commit: 218036c37673615e6bced0eecfb8a9b7d6eb7808

* Updated to latest version of JTextParser and JStreamish
* Switched from StringLineSupplier for reading lines from a source string to CharLineSupplier (slightly less garbage generated due to less conversion between strings and char arrays)
* Fixed toJson() not formatting generic types correctly
* By default, annotation arguments map is include in toJson() output even if empty
* ITrackSearchService.cs test file was using '\r' for newlines, replaced with '\n'
* Moved twg2.parser.test package to separate test directory


--------
####0.6.0
date: 2016-2-21

commit: 5ae0793feb0475654bbdf835ef5d350e91cdd438

* Added numeric literal parsing \(i.e. '23' or '1.5f')
* Added field and method access modifier parsing \(i.e. public, static, synchronized, volatile)
* Fields now writing their annotations when toJson\() is called
* Changed JsonWrite method names to help differentiate their purposes
* Update to use latest version of multiple libraries, including: JFileIo, JStreamish, JTextParser, and JParserDataTypeLike


--------
####0.5.0
date: 2016-2-9

commit: eea353c111f789b315ab5471661c6a305c0701d2

* Added array type parsing, \(i.e. 'int\[]\[]')
* Moved twg2.parser.codeParser.eclipseProject to another project
* Added some more tests and moved none JUnit experiment files to 'miscellaneous' package


--------
####0.4.0
date: 2016-1-16

commit: ab23d86656221e6b1a540d7129446b08c808aca4

* Fixed a regression in C# method signature parsing
* Removed unused twg2.parser.documentParser.block package
* Moved twg2.parser.condition.AstParser -> twg2.parser.baseAst.AstParser
* Modified CodeLanguageOptions, so implementation class is a sub-class and CodeLanguageOptions contains only static fields and methods


--------
####0.3.0
date: 2016-1-16

commit: 0b7128980ba31623d17f85d9f10bd4d99bd1288e

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
