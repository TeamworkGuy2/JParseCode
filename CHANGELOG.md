# Change Log
All notable changes to this project will be documented in this file.
This project does its best to adhere to [Semantic Versioning](http://semver.org/).


--------
###[0.13.0](N/A) - 2016-10-26
#### Added
* Added ParameterSig and ParameterSigResolved 'parameterModifiers' field
* Added KeywordUtil parameterModifiers() and isParameterModifier()

#### Changed
__Parameter modifier parsing support__ (i.e. 'final' in Java or 'params' in C#):
* MethodParametersParser.extractParamsFromSignature() to support parameter modifiers
* Renamed CodeTokenizerBuilder addConstParser() -> addParser()
* Added FieldDef.initializerToJson() 'preClosingComma' parameter
* Updated dependencies, specifically jfile-io to 0.7.0 which no longer contains JsonWrite, so added json-stringify library for new equivalent JsonStringify class and updated related code
* Updated toJson() methods to use JsonStringify.inst:
  * FieldDef
  * FieldDefResolved
  * FieldSig
  * FieldSigResolver
  * MethodSig
  * ParameterSig
  * ParameterSigResolved


--------
###[0.12.1](https://github.com/TeamworkGuy2/JParseCode/commit/a0ef508705fb685798e8762fbc08cd5a92eff273) - 2016-10-02
#### Changed
* Updated dependencies, specifically jfile-io to 0.7.0 which no longer contains JsonWrite, so added json-stringify library for new equivalent JsonStringify class and updated related code


--------
###[0.12.0](https://github.com/TeamworkGuy2/JParseCode/commit/0ca793a0dd5f2d41f629e7f133dbd3bf2a2c4adb) - 2016-09-13
#### Added
* PerformanceTrackers, ParseTimes, TokenizeStepDetails in new twg2.parser.codeParser.tools.performance package - used for tracking performance

#### Changed
* biggest change is switching from jtext-parser's TextIteratorParser (previously: TextParserImpl) to TextCharsParser and files are read as char[] and stored in ParseInput and CodeFileSrc as char[] with offset and length, this will hopefully provide a small performance boost since each file's contents is copied one less time (no more new String(...) copy) and TextCharsParser is designed to take a char[] without any data copying
* second large change is moving the parsing process toward a clearly defined two step process, the first step is called 'tokenization' and the second is called 'parsing/extracting'
  * file tokenization logic has been split up.  Cs and Java FileTokenizer classes now return CodeTokenizerBuilder instances and CodeTokenizerBuilder contains all the generic logic for running the tokenization process
* updated to new latest dependencies, especially jtext-parser
* moved CodeFragment, DocumentFragment, and DocumentFragmentText from package twg2.parser.documentParser -> twg2.parser.fragment
* moved CodeFileParsed, CodeFileSource, ParseInput, and ParserWorkflow from package twg2.parser.codeParser -> twg2.parser.workflow
* moved/renamed twg2.parser.documentParser.DocumentParser -> twg2.parser.tokenizers.CodeTokenizerBuilder
* moved IsParentChild from package twg2.parser.documentParser -> twg2.parser.tokenizers
* CommentAndWhitespaceExtractor now drops the last trailing newlines from the comment strings
* updated a number of unit tests


--------
###[0.11.0](https://github.com/TeamworkGuy2/JParseCode/commit/3029d0d08bda6cc308d3732eb09eb971fd0e6030) - 2016-09-06
#### Added
* __basic C# and Java enum parsing__
  * Added twg2.ast.interm.field FieldDef and FieldDefResolved to represent enum members (TODO could use some clarification/refactoring)
  * Added CsEnumMemberExtractor and JavaEnumMemberExtractor
* Moved duplicate code from class that implemented AstParser into new AstParserReusableBase and AstMemberInClassParserReusable abstract classes (I know the names are a little awkward, suggestions are welcome)
 * Added C# and Java unit tests for enum parsing
 * Moved duplicate source code parsing logic for tests into new CodeFileAndAst class

#### Changed
* moved and renamed several packages and class names
  * twg2.parser.baseAst and sub-packages split and moved to twg2.parser.codeParser and sub-packages
    * twg2.parser.baseAst.tools -> twg2.parser.fragment
    * twg2.parser.baseAst.CompoundBlock -> twg2.parser.codeParser.BlockType
  * CsClassParser -> CsClassTokenizer
  * JavaClassParser -> JavaClassTokenizer
  * twg2.parser.codeParser.parsers -> twg2.parser.tokenizers and *Parser class name prefix changed to *Tokenizer
* AccessModifierExtractor renamed readAccessModifier() -> parseAccessModifier() and readAccessModifierFromIter() -> readAccessModifiers()
* CsBlock and JavaBlock renamed fromKeyword() -> parseKeyword() and tryFromKeyword() -> tryParseKeyword()
* TypeSig renamed nested classes:
  * Simple -> TypeSigSimple
  * SimpleBaseImpl -> TypeSigSimpleBase
  * SimpleGenericImpl -> TypeSigSimpleGeneric
  * ResolvedBaseImpl -> TypeSigResolvedBase
  * ResolvedGenericImpl -> TypeSigResolvedGeneric


--------
###[0.10.8](https://github.com/TeamworkGuy2/JParseCode/commit/a89e38e23341bb999ec136fc61212c8271ad4332) - 2016-09-02
#### Changed
* Updated dependency, switched jparser-data-type-like (now deprecated/removed) to jparse-primitive which is a separate project containing just the primitive parsing code from jparser-data-type-like
* Renamed project from JParserTools -> JParseCode
* Moved plugin-js/ -> plugin/node-js/


--------
###[0.10.7](https://github.com/TeamworkGuy2/JParseCode/commit/ee313a3fe1bfe4e4be59c85b5997c5b13c26d1c8) - 2016-08-28
#### Added
* Added jdate-times dependency (since dependent date/time code was moved from jdata-util and jparser-data-type-like to jdate-times)

#### Changed
* Updated dependencies to latest version


--------
###[0.10.6](https://github.com/TeamworkGuy2/JParseCode/commit/d1099ba9ef35ff0109c2c923044efe2219bd061d) - 2016-08-27
#### Changed
* Fixed version numbers and jackson-* dependency names in package-lib.json


--------
###[0.10.5](https://github.com/TeamworkGuy2/JParseCode/commit/d2efb99774df457392525dbdf4341c438fb20160) - 2016-08-21
#### Changed
* Added JCollectionBuilders and JTuples dependencies
* Updated jcollection-util to latest 0.7.x version (removed twg2.collections.builder and twg2.collections.tuple)


--------
###[0.10.4](https://github.com/TeamworkGuy2/JParseCode/commit/5a3686f828a91eac9896755fb8f6ee8d888b3ca7) - 2016-08-18
#### Changed
* Updated jdata-util to latest 0.3.x version (EnumUtil renamed ErrorUtil, TimeUnitUtil package name changed)
* Fixed compiled jar path and name


--------
###[0.10.3](https://github.com/TeamworkGuy2/JParseCode/commit/5e2e75da19451f6e99427405b6b04f844fb260de) - 2016-08-07
#### Changed
* Updated jcollection-util to latest 0.5.x version
* Updated jfile-io to latest 0.6.x version (SourceInfo renamed to DirectorySearchInfo)
* Updated jtext-util to latest 0.10.x version (some classes moved to new twg2.text.stringSearch package)
* Added jcollection-interfaces dependency


--------
###[0.10.2](https://github.com/TeamworkGuy2/JParseCode/commit/719509161f795fbafc56c8beefd51562103b6cb7) - 2016-06-21
#### Added
* plugin-js to help generate CLI strings from TypeScript/Javascript projects, with Node.js in mind
* Readme section about the CLI


--------
###[0.10.1](https://github.com/TeamworkGuy2/JParseCode/commit/5cdf7fabab17d8d9d8037c83c29047979a6438e7) - 2016-05-13
#### Changed
* Merged DocumentFragmentRef with DocumentFragmentText
* Added some documentation
* Fixed some code warnings
* Moved full class parsing tests to twg2.parser.codeParser.test

#### Fixed
* Bug in annotation named parameter parsing when annotation only had one parameter
* Bug in C# property parsing not supporting field modifiers (i.e. 'private', 'protected', ...)

#### Removed
* DocumentFragmentRef (merged with DocumentFragmentText)


--------
###[0.10.0](https://github.com/TeamworkGuy2/JParseCode/commit/3e8a324ccada6af273339e6f29ae569795e3abcd) - 2016-04-12
#### Added
* Added better annotation parsing, including support for negative numbers as arguments
* Added CodeFragment which extends 'DocumentFragmentText<CodeFragmentType>' so don't have to keep typing that every time, updated most of the code to use CodeFragment
* Added OperatorUtil and Operator (with C# and Java implementation enums) similar to the existing Keyword enums

#### Changed
* Refactored how we use EnumSubSet and enum sub-categorization
   * Added CodeFragmentEnumSubSet with is() and parse() methods which accept CodeFragment nodes (we were starting to duplicate this parsing code in Keyword and the new Operator class, so moved it to a reusable class)
   * Removed KeywordUtil isXyzKeyword() and parseXyzKeyword() methods in favor of methods that return CodeFragmentEnumSubSet instances for each of the keyword categories (i.e. 'blockModifiers()' or 'operators()')
* Moved twg2.parser.codeParser extractor classes (i.e. AccessModifierExtractor or BlockExtractor) to new twg2.parser.codeParser.extractors package

#### Fixed
* ParserWorkflow now generates and groups all results by destination file before writing (previously a writer was opened in overwrite mode for each destination group, thereby overwriting data written to the same file by a previous destination group during the same program execution)


--------
###[0.9.0](https://github.com/TeamworkGuy2/JParseCode/commit/679778bafd13a413854bd169cabe747b12bbc894) - 2016-03-20
#### Added
* Added commented parsing for comments attached to methods and fields (future TODO: add comment parsing for comments attached to classes and namespaces)

#### Changed
* Renamed intermAst packages to 'twg2.ast.interm'
* Renamed most AST classes, removed 'interm' from the name
* Moved type resolution out of AST classes into new 'twg2.parser.resolver' classes (i.e. ClassSigResolver, FieldSigResolver, etc.)
* Created 'twg2.parser.language' package for code language management classes


--------
###[0.8.0](https://github.com/TeamworkGuy2/JParseCode/commit/32ee2a5ec5c218d3f90d1438f893a86e34b9c716) - 2016-02-28
#### Changed
Move from assuming that conditions can list the initial chars that match them (CharParser.WithMarks.getMatchFirstChars()) to CharParserMatchable and new getFirstCharMatcher() method which allows for a flexible definition of matching first chars
* Moved/renamed ParserWorkFlow SourceInfo and LoadResult \(renamed to SourceFiles) nested classes and ParserMain.getFilesByExtension() to [JFileIo] (https://github.com/TeamworkGuy2/JFileIo) library
* Moved twg2.parser.output JsonWrite and JsonWritable to JFileIO project's twg2.io.write package
* Renamed ParserMain -> ParserMisc
* Implemented new CharParserMatchable with getFirstCharMatcher() methods in place of old CharParser.WithMarks interface from JTextParser
* Updated to use latest version of JFileIo and JTextParser


--------
###[0.7.0](https://github.com/TeamworkGuy2/JParseCode/commit/218036c37673615e6bced0eecfb8a9b7d6eb7808) - 2016-02-24
#### Changed
* Updated to latest version of JTextParser and JStreamish
* Switched from StringLineSupplier for reading lines from a source string to CharLineSupplier (slightly less garbage generated due to less conversion between strings and char arrays)
* By default, annotation arguments map is include in toJson() output even if empty
* ITrackSearchService.cs test file was using '\r' for newlines, replaced with '\n'
* Moved twg2.parser.test package to separate test directory

#### Fixed
* Fixed toJson() not formatting generic types correctly


--------
###[0.6.0](https://github.com/TeamworkGuy2/JParseCode/commit/5ae0793feb0475654bbdf835ef5d350e91cdd438) - 2016-02-21
#### Added
* Added numeric literal parsing \(i.e. '23' or '1.5f')
* Added field and method access modifier parsing \(i.e. public, static, synchronized, volatile)

#### Changed
* Fields now write their annotations when toJson\() is called
* Changed JsonWrite method names to help differentiate their purposes
* Update to use latest version of multiple libraries, including: JFileIo, JStreamish, JTextParser, and JParserDataTypeLike


--------
###[0.5.0](https://github.com/TeamworkGuy2/JParseCode/commit/eea353c111f789b315ab5471661c6a305c0701d2) - 2016-02-09
#### Added
* Added array type parsing, \(i.e. 'int\[]\[]')
* Added some more tests

#### Changed
* Moved twg2.parser.codeParser.eclipseProject to another project
* Moved none JUnit experiment files to 'miscellaneous' package


--------
###[0.4.0](https://github.com/TeamworkGuy2/JParseCode/commit/ab23d86656221e6b1a540d7129446b08c808aca4) - 2016-01-16
#### Changed
* Moved twg2.parser.condition.AstParser -> twg2.parser.baseAst.AstParser
* Modified CodeLanguageOptions, so implementation class is a sub-class and CodeLanguageOptions contains only static fields and methods

#### Removed
* Removed unused twg2.parser.documentParser.block package

#### Fixed
* Fixed a regression in C# method signature parsing


--------
###[0.3.0](https://github.com/TeamworkGuy2/JParseCode/commit/0b7128980ba31623d17f85d9f10bd4d99bd1288e) - 2016-01-16
#### Added
* __Java parsing support and tests.__
* Added Keyword interface for generic language keyword operations such as isKeyword(), isBlockModifierKeyword(), isDataTypeKeyword(), etc.

#### Changed
* Made interm parsing more generic, added a bunch of parser creator functions to AstExtractor.  Converted some of the C# interm parsers into more generic parsers to be used by C# and Java (see BaseFieldExtractor, BaseMethodExtractor, BaseMethodParametersParser)
* Renamed getFullyQualifyingName() methods to getFullName(), (note: this may change again in future, possibly to getFqName())
* Moved and renamed ParserCondition, Precondition -> ParserFactory, TokenParserCondition -> TokenParser, CharParserCondition -> CharParser, and ParserStartChars to the [JTextParser] (https://github.com/TeamworkGuy2/JTextParser) project
* Simplified CharConditions and StringConditions sub-class names (i.e. StringConditions.StringStartFilter -> StringConditions.Start)


--------
###[0.2.1](https://github.com/TeamworkGuy2/JParseCode/commit/a33f37ad6a116e7e697498af88327dfaa46709a0) - 2016-01-13
#### Added
* Simple C# parse example with resulting JSON.  Added and refactored some test cases.


--------
###[0.2.0](https://github.com/TeamworkGuy2/JParseCode/commit/5b692bc3476ca94c6dedb5b6424d1319fcad2057) - 2016-01-12
#### Added
* __Finished command line interface (CLI) argument parsing for ['sources', 'destinations', 'log'] and ParserWorkflow.__

#### Changed
* Simplified CodeFileSrc generic signature. Added interfaces and setup entire parsing process to be generic (added CodeLanguage.getExtractor() and AstExtractor interface).
* Refactored C# classes to support this more generic approach.
* Added Simple and Resolved sub-classes of ProjectClassSet. Renamed CsMain -> ParserMain.
* Added some additional EclipseClasspathFile/Utils methods for finding project dependencies.

#### Removed
* Removed IntermClass.getBlockTree(), it should be tracked higher up in the parsing process.
