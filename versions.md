--------
####0.2.1
date: 2016-1-13
commit: ?

* Added simple C# parse example with resulting JSON.  Added and refactored some test cases.

--------
####0.2.0:
date: 2016-1-12
commit: 5b692bc3476ca94c6dedb5b6424d1319fcad2057

* Finished setting up command line argument parsing for ['sources', 'destinations', 'log'] and ParserWorkflow.
* Simplified CodeFileSrc generic signature. Added interfaces and setup entire parsing process to be generic (added CodeLanguage.getExtractor() and AstExtractor interface).
* Refactored C# classes to support this more generic approach.
* Removed IntermClass.getBlockTree(), it should be tracked higher up in the parsing process.
* Added Simple and Resolved sub-classes of ProjectClassSet. Renamed CsMain -> ParserMain.
* Added some additional EclipseClasspathFile/Utils methods for finding project dependencies.
