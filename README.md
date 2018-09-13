JParseCode
==============
version: 0.15.4

In progress C#/Java/TypeScript parser tools built atop [JTextParser](https://github.com/TeamworkGuy2/JTextParser), [Jackson](https://github.com/FasterXML/jackson-core/) (core, databind, annotations) and half a dozen other utility libraries. 

### Goals:
* A source code parser that can turn C#, Java, or JavaScript/TypeScript code into a simple AST like structure ('competent' meaning this project aims to support common use cases, not every syntatic feature of the supported languages). 
* A code first parser aimed at manipulating the resulting AST and writing it back as source code or JSON.  With the goal of allowing simple language constructs like interfaces and data models to be transpiled to different languages. 

### Not Goals:
* NOT to create another compiler for C#, Java, or JavaScript/TypeScript. This project's parser expects valid code as input, the few error messages that are present are NOT design to highlight syntax errors in the input. 
* NOT to create a valid AST for each supported language. Rather an AST like structure that supports the lowest common denominator between the targeted languages. This means many AST compromises are inevitable due to differences in language specs. 


Example:
--------

Source Code (SimpleCs.cs):
```C#
namespace ParserExamples.Samples {

	/// <summary>
	/// A simple class to test parsing.
	/// </summary>
	public class SimpleCs {

		/// <value>The modification count.</value>
		private int mod;

		/// <value>The name.</value>
		private string _name;

		/// <value>The names.</value>
		public IList<string> Names { get; }

		/// <value>The number of names.</value>
		public int Count { set; }

		/// <value>The access timestamps.</value>
		public DateTime[] accesses { set { this.mod++; this.accesses = value; } }

		/// <value>The access timestamps.</value>
		public string name { get { this.mod++; return this._name; } set { this.mod++; this._name = value; } }

		/// <summary>Add name</summary>
		/// <param name="name">the name</param>
		/// <returns>the names</returns>
		[OperationContract]
		[WebInvoke(Method = "POST", UriTemplate = "/AddName?name={name}",
				ResponseFormat = WebMessageFormat.Json)]
		[TransactionFlow(TransactionFlowOption.Allowed)]
		Result<IList<String>> AddName(string name) {
			content of block;
		}

	}

}
```


Java code to parser SimpleCs.cs (simple_cs_source_string is a string containing the contents of SimpleCs.cs):
```Java
CodeFileSrc<CodeLanguage> simpleCsAst = ParseCodeFile.parseCode("SimpleCs.cs", CodeLanguageOptions.C_SHARP, simple_cs_source_string);
WriteSettings ws = new WriteSettings(true, true, true, true);

for(Map.Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<CsBlock>> block : CodeLanguageOptions.C_SHARP.getExtractor().extractClassFieldsAndMethodSignatures(simpleCsAst.getDoc())) {
	CodeFileParsed.Simple<String, CsBlock> fileParsed = new CodeFileParsed.Simple<>("SimpleCs.cs", block.getValue(), block.getKey());

	StringBuilder sb = new StringBuilder();
	fileParsed.getParsedClass().toJson(sb, ws);
	System.out.println(sb.toString()); // Print the parsed AST to System.out
}
```


JSON Result (printed to System.out):
```JSON
{
	"classSignature": {
		"access": "PUBLIC",
		"name": "ParserExamples.Samples.SimpleCs",
		"declarationType": "class"
	},
	"blockType": "CLASS",
	"using": [],
	"fields": [{
		"name": "ParserExamples.Samples.SimpleCs.mod",
		"type": {
			"typeName": "int",
			"primitive": true
		},
		"accessModifiers": ["private"],
		"annotations": [],
		"comments": [" <value>The modification count.</value>\n"]
	}, {
		"name": "ParserExamples.Samples.SimpleCs._name",
		"type": {
			"typeName": "string"
		},
		"accessModifiers": ["private"],
		"annotations": [],
		"comments": [" <value>The name.</value>\n"]
	}, {
		"name": "ParserExamples.Samples.SimpleCs.Names",
		"type": {
			"typeName": "IList",
			"genericParameters": [{
				"typeName": "string"
			}]
		},
		"accessModifiers": ["public"],
		"annotations": [],
		"comments": [" <value>The names.</value>\n"]
	}, {
		"name": "ParserExamples.Samples.SimpleCs.Count",
		"type": {
			"typeName": "int",
			"primitive": true
		},
		"accessModifiers": ["public"],
		"annotations": [],
		"comments": [" <value>The number of names.</value>\n"]
	}, {
		"name": "ParserExamples.Samples.SimpleCs.accesses",
		"type": {
			"typeName": "DateTime",
			"arrayDimensions": 1
		},
		"accessModifiers": ["public"],
		"annotations": [],
		"comments": [" <value>The access timestamps.</value>\n"]
	}, {
		"name": "ParserExamples.Samples.SimpleCs.name",
		"type": {
			"typeName": "string"
		},
		"accessModifiers": ["public"],
		"annotations": [],
		"comments": [" <value>The access timestamps.</value>\n"]
	}],
	"methods": [{
		"name": "ParserExamples.Samples.SimpleCs.AddName",
		"parameters": [{
			"type": "string",
			"name": "name"
		}],
		"accessModifiers": [],
		"annotations": [{
			"name": "OperationContract",
			"arguments": {}
		}, {
			"name": "WebInvoke",
			"arguments": {
				"ResponseFormat": "WebMessageFormat.Json",
				"Method": "POST",
				"UriTemplate": "/AddName?name={name}"
			}
		}, {
			"name": "TransactionFlow",
			"arguments": {
				"value": "TransactionFlowOption.Allowed"
			}
		}],
		"returnType": {
			"typeName": "Result",
			"genericParameters": [{
				"typeName": "IList",
				"genericParameters": [{
					"typeName": "String"
				}]
			}]
		},
		"comments": [" <summary>Add name</summary>\n", " <param name=\"name\">the name</param>\n", " <returns>the names</returns>\n"]
	}]
}
```


--------

### Command Line Interface (CLI)

A command line call looks like:
```
path/to/java -jar path/to/jparse-code.jar 
 -sources './src/java/Server/Services=1,[cs];./src/java/Server/Models=3,[cs]'
 -destinations './output/Services.json=[App.Services];./output/Models.json=[App.Entities]'
 -log './output/parser.log'
```
Where ./src/java/Server/** is where source files are kept
And the files in ./src/java/Server/Services belong to the C# namespace `App.Services` and ./src/java/Server/Models/ belong to the C# namespace `App.Entities`


#### Sources
A semicolon separated list of paths set equal to a directory depth followed by a comma and a comma separated, brackets wrapped, list of file extensions. 
The path, child directory depth, and file extensions are used to create a file system filter and all matching files are parsed.
The following formats are valid:
'path=depth,[fileExt,fileExt,...]'
'path=depth'
'path'

Example: ```/project/myApp/Models=3,[java,json]```
Note: the brackets around the '[java,json]' file extension list are literal.


#### Destinations
A semicolon separated list of output file names associated with lists of namespaces.  Each parsed file who's namespace falls into one of these lists is written to that file. 
The following format is valid:
'path=[namespace,namespace,...]'

Example: ```/project/output/models.json=[MyApp.Models]```


#### Log
An optional log file name to write parser information to, in the format:
'path'

Example: ```/project/output/parser-log.log```


--------
Plugins:
Currently there is one plugin, a dual purpose TypeScript/Javascript plugin for create the CLI argument strings used by jparse-code.jar
