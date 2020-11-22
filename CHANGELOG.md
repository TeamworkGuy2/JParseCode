# Change Log
All notable changes to this project will be documented in this file.
This project does its best to adhere to [Semantic Versioning](http://semver.org/).


--------
### [0.20.0](N/A) - 2020-11-22
__Method generic type parameters parsing support__ (i.e. 'public T Create<T>()' in C#).
#### Added
* Method generic type parameters added - `MethodSig` and sub-classes have a new `typeParameters` field. Note: if these 'types' contain lower/upper type bounds these are included in the `typeName` property in Java and are not yet included in C# parsing
  * Added basic unit tests for this new method generic type parameters parsing support
* `AstFragType.isBlock(CodeToken, char)` overload for attempted performance optimization

#### Changed
* Renamed `DataTypeExtractor` -> `TypeExtractor`
* Added `EnhancedListIterator` as a replacement for `TokenListIterable` to try and simplify fragment iteration since it is a fairly hot section of code in most parsing scenarios
* `ParseTimes.TrackerAction` enums renamed:
  * `LOAD` -> `READ`
  * `PARSE` -> `EXTRACT_AST`
* Update dependency jtext-tokenizer@0.6.0 and code to match
  * Adjustments to `new CharParserMatchableFactory()` and `CharConditions.Identifier.newInstance()` calls in `IdentifierTokenizer` and `NumberTokenizer`
* JSON output now excludes empty `annotations` arrays on fields, methods, and parameters
* Additional performance log counters added to several methods and printed when performance info is enabled via `-debug` CLI flag

#### Removed
* `AstFragType.isType()` static method and overloads since they were unused
* Unused `CommentTokenizer.createCommentTokenizerForJava()`

#### Fixed
* An exception being thrown when resolving class names if a file imported a namespace and also imported a child namespace of the first namespace


--------
### [0.19.2](https://github.com/TeamworkGuy2/JParseCode/commit/9967d655b7c274b8d58892ab74b7fd1fe4dea522) - 2020-05-23
__Parameter varargs parsing support__ (i.e. 'int...' in Java).
#### Changed
* Update dependency `jtext-parser@0.16.0` and `jtext-tokenizer@0.4.0`
  * Classes combined, class names simplified, and unused classes and methods removed from libraries
  * Code identifier parser now provided by `jtext-tokenizer`
  * Several bug fixes around compound optional parser conditions
* Added `char[] src, int srcOff, int srcLen` parameters to `CodeTokenizer.tokenizeDocument()`
* Renamed `IdentifierTokenizer` `newIdentifierTokenizer()` to `createIdentifierTokenizer()`
* Improved unit tests

#### Removed
* `IdentifierTokenizer.createIdentifierTokenizer()`


--------
### [0.19.1](https://github.com/TeamworkGuy2/JParseCode/commit/691c019ee2b8a889bd44a8048957fdf86a02bcd4) - 2020-04-20
#### Changed
* Finish `CommentAndWhitespaceExtractor` and tests for it
* `TextToken` interface now includes `hashCode()` and `equals(Object)`
  * `TextFragmentRefToken` now implements `hashCode()` and `equals(Object)`
* Minor code cleanup, use `StringSplit.split()` with `char` instead of `String` where possible


--------
### [0.19.0](https://github.com/TeamworkGuy2/JParseCode/commit/9e95bec5e50e9fd229d25f181d17da98a8d238b3) - 2019-07-04
#### Changed
* `IdentifierTokenizer.createIdentifierWithGenericTypeTokenizer()` now takes one parameter `int maxGenericTypeDepth`
* Changed `CsFileTokenizer.createFileParser()` -> `createCsTokenizers()` and `JavaFileTokenizer.createFileParser()` -> `createJavaTokenizers()`
* Added some private constructors that throw AssertionError to static classes

#### Removed
* Changed `GenericTypeTokenizer._createGenericTypeTokenizer()` from public to private
* Removed `IdentifierTokenizer` field `static int genericTypeDepth` in favor of callers explicitly passing the argument to `createIdentifierWithGenericTypeTokenizer()` which now takes one parameter `int maxGenericTypeDepth`
* Removed `CodeTokenizerBuilder` in favor of `CodeTokenizer` static methods
  * Manually build a tokenizer list of type `PairList<CharParserFactory, TextTransformer<CodeTokenType>>`
  * Call `CodeTokenizer.createTokenizer()` with the language you used to pass to the `CodeTokenizerBuilder` constructor and the list of tokenizers you manually created

#### Fixed
* `AnnotationExtractor` to handle all C# keyword-followed-by-a-block annotation arguments like `default(T)`, `nameof(T)`, and `typeof(T)`


--------
### [0.18.1](https://github.com/TeamworkGuy2/JParseCode/commit/88ab130b4a6e79bdefa3f071ec64c19e316e91af) - 2019-07-02
#### Fixed
* `AnnotationExtractor` to handle C# `typeof(T)` annotation arguments


--------
### [0.18.0](https://github.com/TeamworkGuy2/JParseCode/commit/bb033bcc4cffc4382a20d7f2394976e087690557) - 2019-03-30
#### Changed
* Added `-debug` and `-threads #` command line arguments
* More detailed debug and log file information
* Renamed `TokenizeStepLogger` -> `ParserActionLogger`
* `ParseTimes.log()` renamed `setActionTime()`
* Simplified and synchronized `PerformanceTrackers` so instance can be shared across threads
* Renamed node.js plugin file `plugins/node-js/jparser-tools-cli` -> `plugins/node-js/jparse-code-cli` and renamed associated test file

#### Removed
* Unused `ScopeType` enum

#### Fixed
* Fixed multi-threaded parsing! Handling of FileReadUtil, performance logs, and result lists are now synchronized in `ParserMisc.parseFileSet()`
* Update dependency jfile-io@0.8.3 (fix for decoding empty streams/files)


--------
### [0.17.0](https://github.com/TeamworkGuy2/JParseCode/commit/7745de03fb4ce135ddb8e6a7f158f6d1f27c5329) - 2019-03-30
Performance refactor, several libraries updated: JArrays, JCollectionUtil, JFileIo, JTextParser, and JTextTokenizer
#### Changed
* Changed to new `FileReadUtil.readChars(InputStream)` (`jfile-io@0.8.2`)
* Switched lombok `val` usage to Java 9 `var`
* Added `HashMap<String, *Keyword> keywordSet` field to `CsKeyword` and `JavaKeyword` for performance
* Added some duplicate code in `IdentifierTokenizer` to work with optimized `CharConditions.ContainsFirstSpecial` constructor


--------
### [0.16.1](https://github.com/TeamworkGuy2/JParseCode/commit/4d9ae1065f328e0354e979ab90c21eee5ade2338) - 2019-03-17
#### Fixed
* Accidentally deleted compiled *.jar files in 0.16.0 release


--------
### [0.16.0](https://github.com/TeamworkGuy2/JParseCode/commit/21510d6f793b960193bb1b76f0276ecf09d62739) - 2019-03-17
#### Added
* Class signature annotation parsing (in `BlockExtractor`, `CsBlockParser`, and `JavaBlockParser`)

#### Changed
* Simplified `ProjectClassSet` (removed two unnecessary generic parameters, renamed private fields):
  * Renamed `resolveSimpleNameToClass()` -> `resolveClassNameAgainstNamespaces()`
  * Renamed `resolveSimpleNameToClassSingleNamespace()` -> `resolveClassNameAgainstNamespace()`
* Performance improvements to collection allocations in `NameUtil` and `ClassSigResolver`
* Renamed `AccessModifier` interface to `Keyword`
* `PerformanceTrackers.getTopParseTimes()` and `getTopParseStepDetails()` switched first parameter from `javax.swing.SortOrder` to `boolean`

#### Removed
* Unused `AstNodeConsumer` and `AstNodePredicate` interfaces
* Removed lombok.val usage/dependency from several classes and packages in favor of Java 10 `var` or actual type.

#### Fixed
* Bug in TokenizeStepLogger trying to `StringCase.toCamelCase()` action names


--------
### [0.15.7](https://github.com/TeamworkGuy2/JParseCode/commit/b36432e04ec4757a2e58102e88e4fe55c65965c4) - 2018-09-23
#### Changed
* Updated `CodeTokenizerBuilder.removeChildren()` to use `SimpleTreeImpl.removeChildRef()` instead of `removeChild()` for improved performance
* Updated dependencies:
  * jcollection-interfaces@0.3.0
  * jcollection-util@0.7.5
  * jtree-walker@0.2.0


--------
### [0.15.6](https://github.com/TeamworkGuy2/JParseCode/commit/d14366d6e4ad70ac7102c505d6d8fe900693f6b0) - 2018-09-22
#### Added
* Parameter default value parsing support added to `MethodParametersParser`
* Added `DataTypeExtractor.isDefaultValueLiteral()` to check for field/parameter default values

#### Changed
* Renamed `CsKeyword.Inst` -> `CsKeyword.CsKeywordUtil`
* Renamed `JavaKeyword.Inst` -> `JavaKeyword.JavaKeywordUtil`
* Unit tests changed to use static imports of `TypeAssert.ary()` instead of `new Object[] {...}` and `TypeAssert.ls()` instead of `Arrays.asList()`


--------
### [0.15.5](https://github.com/TeamworkGuy2/JParseCode/commit/b1176a9f80f4d8eecb79831af4289224189a57df) - 2018-09-14
#### Changed
* `ParserWorkFlow` returns the `-help` message if no arguments are given when run

#### Fixed
* `bin/jparse_code.jar` wasn't properly compiled as a runnable jar


--------
### [0.15.4](https://github.com/TeamworkGuy2/JParseCode/commit/bb9f2d6d58967c622505453a97933a7c895b2630) - 2018-09-13
#### Added
* Annotations to parameter signatures (with basic parameter annotation parsing added to `MethodParametersParser.extractParamsFromSignature()`)
* Documented DataTypeExtractor methods
* Added/Improved test cases for:
  * Class signatures (generics, multiple extend/implement types)
  * Generic types with multiple parameters
  * Annotations on method parameters
  * TODO: fix failing test for default parameters

#### Removed
* Old code from MainParser

#### Fixed
* `CsBlockParser.readClassIdentifierAndExtends()` was incorrectly trying to parse `new {` object initializer blocks as class declarations
* `DataTypeExtractor` was reversing the parameter order of multi-paremeter generic types


--------
### [0.15.3](https://github.com/TeamworkGuy2/JParseCode/commit/7704ebf8f49e2841dc9e362fef1752252e283b71) - 2017-12-30
#### Changed
* Update dependency `jtwg2-logging@0.3.0`


--------
### [0.15.2](https://github.com/TeamworkGuy2/JParseCode/commit/6d8af0544a624f451828f8dde70bc7f78f281ea8) - 2017-12-22
#### Changed
* Upgrade to Java 9
* Upgrade to JUnit 5

#### Fixed
* Fix a minor compile issue found by Java 9 upgrade


--------
### [0.15.1](https://github.com/TeamworkGuy2/JParseCode/commit/6136531b49a776590a9d8b23ab2cd27c40d4be10) - 2017-11-11
#### Changed
* Update dependency `jtext-parser@0.13.0`
* Add some test cases


--------
### [0.15.0](https://github.com/TeamworkGuy2/JParseCode/commit/db38f7ec5d369b60ef8e79480d981d3bcdf3006c) - 2017-10-15
#### Changed
Simplified class names and generic type signatures:
* Changed `AnnotationSig`, `BlockAst`, `MethodSig`, `CodeFileSrc`, and `CodeFileParsed` to have `public final` properties and remove getters
* CodeFileSrc `language` property type is now `CodeLanguage`, not a generic type parameter
* CodeFileSrc `doc` property renamed `astTree`
* `CodeFileParsed`, `CodeTokenizerBuilder`,
* `CodeFileParsed` and `ProjectClassSet` added `Intermediate` implementations to existing `Simple` and `Resolved` implementations with simplified generic type parameters
* Split ClassSig `SimpleImpl` and `ResolvedImpl` into separate files
* Split MethodSig `SimpleImpl` and `ResolvedImpl` into separate `MethodSigSimple` and `MethodSigResolved` files


--------
### [0.14.5](https://github.com/TeamworkGuy2/JParseCode/commit/283d487a7e04d7355451f2fe641cacff2e026cc3) - 2017-08-20
#### Changed
* Update dependencies:
  * `jfunc@0.3.0` (`Predicates.Char` -> `CharPredicate`)
  * `jtext-parser@0.12.0` (`CharParserPredicate` interface instead of `BiPredicates.CharObject<TextParser>`)


--------
### [0.14.4](https://github.com/TeamworkGuy2/JParseCode/commit/8ac1384c8de1a310d5097b843e657bacc029f11a) - 2017-06-25
#### Added
* Two more JUnit tests, CsModelParseTest and JavaModelParseTest
* Added unit test helpers: test.twg2.parser.test.utils FieldAssert, MethodAssert, and TypeAssert

#### Changed
* Ensure output write order of parsed file definitions (sorted by fully qualifying name)
* Updated/refactored unit tests to use new unit test helpers
* Renamed unit test helper ParseAnnotationAssert -> AnnotationAssert


--------
### [0.14.3](https://github.com/TeamworkGuy2/JParseCode/commit/d91e7d4a82827c3289d72d2feea9a5b9a0fe4cd3) - 2017-02-06
#### Changed
* Forgot to remove test code from MainParser


--------
### [0.14.2](https://github.com/TeamworkGuy2/JParseCode/commit/c9ff08c26752a18e8f38e4365181876d81461190) - 2017-02-06
#### Changed
* Removed lombok.val usage/dependency from test classes.

#### Fixed
* Fixed parsing C# classes that extend/implement multiple types, unrecognized types are assumed to possibly be interfaces.
* Fixed parsing Java classes that implement multiple types, unrecognized types are assumed to possibly be interfaces.


--------
### [0.14.1](https://github.com/TeamworkGuy2/JParseCode/commit/53806a53d3b8152b35e3166a81dbe9a81a49f354) - 2016-12-03
#### Changed
* Updated dependencies:
  * `jtext-parser@0.11.0`
  * `jtext-tokenizer@0.2.0`
  * `jparser-primitive@0.2.0`
* This includes a new parsing strategy which tries to parse non-compound tokens from start to finish using one parser at a time without passing the characters to compound parser, this improves performance and simplifies some of the compound parsers, but makes some compound parsers more difficult, such as ending conditions that try to keep track of characters between the start and end of the compound parser segment 
* `GenericTypeTokenizer` and `IdentifierTokenizer` changes to properly parse nullable generic parameters


--------
### [0.14.0](https://github.com/TeamworkGuy2/JParseCode/commit/07bd715ecd29a42d781b663cd3a20c2436e69bff) - 2016-10-30
__Reduced library complexity/scope by moving twg2.parser.text conditions/tokenizers to separate [jtext-tokenizer](https://github.com/TeamworkGuy2/JTextTokenizer) library__
#### Changed
* Moved twg2.parser.text package to jtext-tokenizer library
* Moved twg2.parser.Inclusion to jtext-parser library
* Renamed classes *Fragment -> *Token:
  * CodeFragment -> CodeToken
  * CodeFragmentType -> CodeTokenType
  * DocumentFragment -> TextToken and renamed methods:
    * getTextFragment() -> getToken()
	* getFragmentType() -> getTokenType()
  * DocumentFragmentText -> TextFragmentRefToken
  * CodeFragmentEnumSubSet -> CodeTokenEnumSubSet
* Renamed twg2.parser.codeParser.tools.performance -> twg2.parser.codeParser.analytics
* TokenizeStepDetails -> TokenizeStepLogger and now implements TypedLogger from jtext-tokenizer library

#### Removed
* Removed twg2.parser.text package (moved to jtext-tokenizer library)


--------
### [0.13.0](https://github.com/TeamworkGuy2/JParseCode/commit/76734b17d16c67a89df7245a2cea2a1133c3b6b0) - 2016-10-26
__Parameter modifier parsing support__ (i.e. 'final' in Java or 'params' in C#):
#### Added
* Added ParameterSig and ParameterSigResolved 'parameterModifiers' field
* Added KeywordUtil parameterModifiers() and isParameterModifier()

#### Changed
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
### [0.12.1](https://github.com/TeamworkGuy2/JParseCode/commit/a0ef508705fb685798e8762fbc08cd5a92eff273) - 2016-10-02
#### Changed
* Updated dependencies, specifically jfile-io to 0.7.0 which no longer contains JsonWrite, so added json-stringify library for new equivalent JsonStringify class and updated related code


--------
### [0.12.0](https://github.com/TeamworkGuy2/JParseCode/commit/0ca793a0dd5f2d41f629e7f133dbd3bf2a2c4adb) - 2016-09-13
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
### [0.11.0](https://github.com/TeamworkGuy2/JParseCode/commit/3029d0d08bda6cc308d3732eb09eb971fd0e6030) - 2016-09-06
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
### [0.10.8](https://github.com/TeamworkGuy2/JParseCode/commit/a89e38e23341bb999ec136fc61212c8271ad4332) - 2016-09-02
#### Changed
* Updated dependency, switched jparser-data-type-like (now deprecated/removed) to jparse-primitive which is a separate project containing just the primitive parsing code from jparser-data-type-like
* Renamed project from JParserTools -> JParseCode
* Moved plugin-js/ -> plugin/node-js/


--------
### [0.10.7](https://github.com/TeamworkGuy2/JParseCode/commit/ee313a3fe1bfe4e4be59c85b5997c5b13c26d1c8) - 2016-08-28
#### Added
* Added jdate-times dependency (since dependent date/time code was moved from jdata-util and jparser-data-type-like to jdate-times)

#### Changed
* Updated dependencies to latest version


--------
### [0.10.6](https://github.com/TeamworkGuy2/JParseCode/commit/d1099ba9ef35ff0109c2c923044efe2219bd061d) - 2016-08-27
#### Changed
* Fixed version numbers and jackson-* dependency names in package-lib.json


--------
### [0.10.5](https://github.com/TeamworkGuy2/JParseCode/commit/d2efb99774df457392525dbdf4341c438fb20160) - 2016-08-21
#### Changed
* Added JCollectionBuilders and JTuples dependencies
* Updated jcollection-util to latest 0.7.x version (removed twg2.collections.builder and twg2.collections.tuple)


--------
### [0.10.4](https://github.com/TeamworkGuy2/JParseCode/commit/5a3686f828a91eac9896755fb8f6ee8d888b3ca7) - 2016-08-18
#### Changed
* Updated jdata-util to latest 0.3.x version (EnumUtil renamed ErrorUtil, TimeUnitUtil package name changed)
* Fixed compiled jar path and name


--------
### [0.10.3](https://github.com/TeamworkGuy2/JParseCode/commit/5e2e75da19451f6e99427405b6b04f844fb260de) - 2016-08-07
#### Changed
* Updated jcollection-util to latest 0.5.x version
* Updated jfile-io to latest 0.6.x version (SourceInfo renamed to DirectorySearchInfo)
* Updated jtext-util to latest 0.10.x version (some classes moved to new twg2.text.stringSearch package)
* Added jcollection-interfaces dependency


--------
### [0.10.2](https://github.com/TeamworkGuy2/JParseCode/commit/719509161f795fbafc56c8beefd51562103b6cb7) - 2016-06-21
#### Added
* plugin-js to help generate CLI strings from TypeScript/Javascript projects, with Node.js in mind
* Readme section about the CLI


--------
### [0.10.1](https://github.com/TeamworkGuy2/JParseCode/commit/5cdf7fabab17d8d9d8037c83c29047979a6438e7) - 2016-05-13
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
### [0.10.0](https://github.com/TeamworkGuy2/JParseCode/commit/3e8a324ccada6af273339e6f29ae569795e3abcd) - 2016-04-12
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
### [0.9.0](https://github.com/TeamworkGuy2/JParseCode/commit/679778bafd13a413854bd169cabe747b12bbc894) - 2016-03-20
#### Added
* Added commented parsing for comments attached to methods and fields (future TODO: add comment parsing for comments attached to classes and namespaces)

#### Changed
* Renamed intermAst packages to 'twg2.ast.interm'
* Renamed most AST classes, removed 'interm' from the name
* Moved type resolution out of AST classes into new 'twg2.parser.resolver' classes (i.e. ClassSigResolver, FieldSigResolver, etc.)
* Created 'twg2.parser.language' package for code language management classes


--------
### [0.8.0](https://github.com/TeamworkGuy2/JParseCode/commit/32ee2a5ec5c218d3f90d1438f893a86e34b9c716) - 2016-02-28
#### Changed
Move from assuming that conditions can list the initial chars that match them (CharParser.WithMarks.getMatchFirstChars()) to CharParserMatchable and new getFirstCharMatcher() method which allows for a flexible definition of matching first chars
* Moved/renamed ParserWorkFlow SourceInfo and LoadResult \(renamed to SourceFiles) nested classes and ParserMain.getFilesByExtension() to [JFileIo] (https://github.com/TeamworkGuy2/JFileIo) library
* Moved twg2.parser.output JsonWrite and JsonWritable to JFileIO project's twg2.io.write package
* Renamed ParserMain -> ParserMisc
* Implemented new CharParserMatchable with getFirstCharMatcher() methods in place of old CharParser.WithMarks interface from JTextParser
* Updated to use latest version of JFileIo and JTextParser


--------
### [0.7.0](https://github.com/TeamworkGuy2/JParseCode/commit/218036c37673615e6bced0eecfb8a9b7d6eb7808) - 2016-02-24
#### Changed
* Updated to latest version of JTextParser and JStreamish
* Switched from StringLineSupplier for reading lines from a source string to CharLineSupplier (slightly less garbage generated due to less conversion between strings and char arrays)
* By default, annotation arguments map is include in toJson() output even if empty
* ITrackSearchService.cs test file was using '\r' for newlines, replaced with '\n'
* Moved twg2.parser.test package to separate test directory

#### Fixed
* Fixed toJson() not formatting generic types correctly


--------
### [0.6.0](https://github.com/TeamworkGuy2/JParseCode/commit/5ae0793feb0475654bbdf835ef5d350e91cdd438) - 2016-02-21
#### Added
* Added numeric literal parsing \(i.e. '23' or '1.5f')
* Added field and method access modifier parsing \(i.e. public, static, synchronized, volatile)

#### Changed
* Fields now write their annotations when toJson\() is called
* Changed JsonWrite method names to help differentiate their purposes
* Update to use latest version of multiple libraries, including: JFileIo, JStreamish, JTextParser, and JParserDataTypeLike


--------
### [0.5.0](https://github.com/TeamworkGuy2/JParseCode/commit/eea353c111f789b315ab5471661c6a305c0701d2) - 2016-02-09
#### Added
* Added array type parsing, \(i.e. 'int\[]\[]')
* Added some more tests

#### Changed
* Moved twg2.parser.codeParser.eclipseProject to another project
* Moved none JUnit experiment files to 'miscellaneous' package


--------
### [0.4.0](https://github.com/TeamworkGuy2/JParseCode/commit/ab23d86656221e6b1a540d7129446b08c808aca4) - 2016-01-16
#### Changed
* Moved twg2.parser.condition.AstParser -> twg2.parser.baseAst.AstParser
* Modified CodeLanguageOptions, so implementation class is a sub-class and CodeLanguageOptions contains only static fields and methods

#### Removed
* Removed unused twg2.parser.documentParser.block package

#### Fixed
* Fixed a regression in C# method signature parsing


--------
### [0.3.0](https://github.com/TeamworkGuy2/JParseCode/commit/0b7128980ba31623d17f85d9f10bd4d99bd1288e) - 2016-01-16
#### Added
* __Java parsing support and tests.__
* Added Keyword interface for generic language keyword operations such as isKeyword(), isBlockModifierKeyword(), isDataTypeKeyword(), etc.

#### Changed
* Made interm parsing more generic, added a bunch of parser creator functions to AstExtractor.  Converted some of the C# interm parsers into more generic parsers to be used by C# and Java (see BaseFieldExtractor, BaseMethodExtractor, BaseMethodParametersParser)
* Renamed getFullyQualifyingName() methods to getFullName(), (note: this may change again in future, possibly to getFqName())
* Moved and renamed ParserCondition, Precondition -> ParserFactory, TokenParserCondition -> TokenParser, CharParserCondition -> CharParser, and ParserStartChars to the [JTextParser] (https://github.com/TeamworkGuy2/JTextParser) project
* Simplified CharConditions and StringConditions sub-class names (i.e. StringConditions.StringStartFilter -> StringConditions.Start)


--------
### [0.2.1](https://github.com/TeamworkGuy2/JParseCode/commit/a33f37ad6a116e7e697498af88327dfaa46709a0) - 2016-01-13
#### Added
* Simple C# parse example with resulting JSON.  Added and refactored some test cases.


--------
### [0.2.0](https://github.com/TeamworkGuy2/JParseCode/commit/5b692bc3476ca94c6dedb5b6424d1319fcad2057) - 2016-01-12
#### Added
* __Finished command line interface (CLI) argument parsing for ['sources', 'destinations', 'log'] and ParserWorkflow.__

#### Changed
* Simplified CodeFileSrc generic signature. Added interfaces and setup entire parsing process to be generic (added CodeLanguage.getExtractor() and AstExtractor interface).
* Refactored C# classes to support this more generic approach.
* Added Simple and Resolved sub-classes of ProjectClassSet. Renamed CsMain -> ParserMain.
* Added some additional EclipseClasspathFile/Utils methods for finding project dependencies.

#### Removed
* Removed IntermClass.getBlockTree(), it should be tracked higher up in the parsing process.
