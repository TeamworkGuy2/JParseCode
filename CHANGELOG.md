# Change Log
All notable changes to this project will be documented in this file.
This project does its best to adhere to [Semantic Versioning](http://semver.org/).


--------
###[0.10.1](N/A) - 2016-05-13
####Changed
* Merged DocumentFragmentRef with DocumentFragmentText
* Added some documentation
* Fixed some code warnings
* Moved full class parsing tests to twg2.parser.codeParser.test

####Fixed
* Bug in annotation named parameter parsing when annotation only had one parameter
* Bug in C# property parsing not supporting field modifiers (i.e. 'private', 'protected', ...)

####Removed
* DocumentFragmentRef (merged with DocumentFragmentText)


--------
###[0.10.0](https://github.com/TeamworkGuy2/JParserTools/commit/3e8a324ccada6af273339e6f29ae569795e3abcd) - 2016-04-12
####Added
* Added better annotation parsing, including support for negative numbers as arguments
* Added CodeFragment which extends 'DocumentFragmentText<CodeFragmentType>' so don't have to keep typing that every time, updated most of the code to use CodeFragment
* Added OperatorUtil and Operator (with C# and Java implementation enums) similar to the existing Keyword enums

####Changed
* Refactored how we use EnumSubSet and enum sub-categorization
   * Added CodeFragmentEnumSubSet with is() and parse() methods which accept CodeFragment nodes (we were starting to duplicate this parsing code in Keyword and the new Operator class, so moved it to a reusable class)
   * Removed KeywordUtil isXyzKeyword() and parseXyzKeyword() methods in favor of methods that return CodeFragmentEnumSubSet instances for each of the keyword categories (i.e. 'blockModifiers()' or 'operators()')
* Moved twg2.parser.codeParser extractor classes (i.e. AccessModifierExtractor or BlockExtractor) to new twg2.parser.codeParser.extractors package

####Fixed
* ParserWorkflow now generates and groups all results by destination file before writing (previously a writer was opened in overwrite mode for each destination group, thereby overwriting data written to the same file by a previous destination group during the same program execution)


--------
###[0.9.0](https://github.com/TeamworkGuy2/JParserTools/commit/679778bafd13a413854bd169cabe747b12bbc894) - 2016-03-20
####Added
* Added commented parsing for comments attached to methods and fields (future TODO: add comment parsing for comments attached to classes and namespaces)

####Changed
* Renamed intermAst packages to 'twg2.ast.interm'
* Renamed most AST classes, removed 'interm' from the name
* Moved type resolution out of AST classes into new 'twg2.parser.resolver' classes (i.e. ClassSigResolver, FieldSigResolver, etc.)
* Created 'twg2.parser.language' package for code language management classes


--------
###[0.8.0](https://github.com/TeamworkGuy2/JParserTools/commit/32ee2a5ec5c218d3f90d1438f893a86e34b9c716) - 2016-02-28
####Changed
Move from assuming that conditions can list the initial chars that match them (CharParser.WithMarks.getMatchFirstChars()) to CharParserMatchable and new getFirstCharMatcher() method which allows for a flexible definition of matching first chars
* Moved/renamed ParserWorkFlow SourceInfo and LoadResult \(renamed to SourceFiles) nested classes and ParserMain.getFilesByExtension() to [JFileIo] (https://github.com/TeamworkGuy2/JFileIo) library
* Moved twg2.parser.output JsonWrite and JsonWritable to JFileIO project's twg2.io.write package
* Renamed ParserMain -> ParserMisc
* Implemented new CharParserMatchable with getFirstCharMatcher() methods in place of old CharParser.WithMarks interface from JTextParser
* Updated to use latest version of JFileIo and JTextParser


--------
###[0.7.0](https://github.com/TeamworkGuy2/JParserTools/commit/218036c37673615e6bced0eecfb8a9b7d6eb7808) - 2016-02-24
####Changed
* Updated to latest version of JTextParser and JStreamish
* Switched from StringLineSupplier for reading lines from a source string to CharLineSupplier (slightly less garbage generated due to less conversion between strings and char arrays)
* By default, annotation arguments map is include in toJson() output even if empty
* ITrackSearchService.cs test file was using '\r' for newlines, replaced with '\n'
* Moved twg2.parser.test package to separate test directory

####Fixed
* Fixed toJson() not formatting generic types correctly


--------
###[0.6.0](https://github.com/TeamworkGuy2/JParserTools/commit/5ae0793feb0475654bbdf835ef5d350e91cdd438) - 2016-02-21
####Added
* Added numeric literal parsing \(i.e. '23' or '1.5f')
* Added field and method access modifier parsing \(i.e. public, static, synchronized, volatile)

####Changed
* Fields now write their annotations when toJson\() is called
* Changed JsonWrite method names to help differentiate their purposes
* Update to use latest version of multiple libraries, including: JFileIo, JStreamish, JTextParser, and JParserDataTypeLike


--------
###[0.5.0](https://github.com/TeamworkGuy2/JParserTools/commit/eea353c111f789b315ab5471661c6a305c0701d2) - 2016-02-09
####Added
* Added array type parsing, \(i.e. 'int\[]\[]')
* Added some more tests

#### Changed
* Moved twg2.parser.codeParser.eclipseProject to another project
* Moved none JUnit experiment files to 'miscellaneous' package


--------
###[0.4.0](https://github.com/TeamworkGuy2/JParserTools/commit/ab23d86656221e6b1a540d7129446b08c808aca4) - 2016-01-16
####Changed
* Moved twg2.parser.condition.AstParser -> twg2.parser.baseAst.AstParser
* Modified CodeLanguageOptions, so implementation class is a sub-class and CodeLanguageOptions contains only static fields and methods

####Removed
* Removed unused twg2.parser.documentParser.block package

####Fixed
* Fixed a regression in C# method signature parsing


--------
###[0.3.0](https://github.com/TeamworkGuy2/JParserTools/commit/0b7128980ba31623d17f85d9f10bd4d99bd1288e) - 2016-01-16
####Added
* __Java parsing support and tests.__
* Added Keyword interface for generic language keyword operations such as isKeyword(), isBlockModifierKeyword(), isDataTypeKeyword(), etc.

####Changed
* Made interm parsing more generic, added a bunch of parser creator functions to AstExtractor.  Converted some of the C# interm parsers into more generic parsers to be used by C# and Java (see BaseFieldExtractor, BaseMethodExtractor, BaseMethodParametersParser)
* Renamed getFullyQualifyingName() methods to getFullName(), (note: this may change again in future, possibly to getFqName())
* Moved and renamed ParserCondition, Precondition -> ParserFactory, TokenParserCondition -> TokenParser, CharParserCondition -> CharParser, and ParserStartChars to the [JTextParser] (https://github.com/TeamworkGuy2/JTextParser) project
* Simplified CharConditions and StringConditions sub-class names (i.e. StringConditions.StringStartFilter -> StringConditions.Start)


--------
###[0.2.1](https://github.com/TeamworkGuy2/JParserTools/commit/a33f37ad6a116e7e697498af88327dfaa46709a0) - 2016-01-13
####Added
* Simple C# parse example with resulting JSON.  Added and refactored some test cases.


--------
###[0.2.0](https://github.com/TeamworkGuy2/JParserTools/commit/5b692bc3476ca94c6dedb5b6424d1319fcad2057) - 2016-01-12
####Added
* __Finished command line interface (CLI) argument parsing for ['sources', 'destinations', 'log'] and ParserWorkflow.__

####Changed
* Simplified CodeFileSrc generic signature. Added interfaces and setup entire parsing process to be generic (added CodeLanguage.getExtractor() and AstExtractor interface).
* Refactored C# classes to support this more generic approach.
* Added Simple and Resolved sub-classes of ProjectClassSet. Renamed CsMain -> ParserMain.
* Added some additional EclipseClasspathFile/Utils methods for finding project dependencies.

####Removed
* Removed IntermClass.getBlockTree(), it should be tracked higher up in the parsing process.