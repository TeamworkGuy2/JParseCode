ParserToolsTmp
==============
version: 0.2.1

In progress C#/Java/TypeScript parser tools built atop [JTextParser] (https://github.com/TeamworkGuy2/JTextParser), [Jackson] (https://github.com/FasterXML/jackson-core/) (core, databind, annotations) and half a dozen other utility libraries. 

####The Goal:
* A competent source code parser that can turn C#, Java, or JavaScript/TypeScript code into a simple AST like structure ('competent' meaning this project aims to support common use cases, not every syntatic feature of the supported languages). 
* A 'code first' parser aimed at manipulating the resulting AST and writing it back as source code or JSON.  With the goal of allowing simple language constructs like interfaces and data models to be transpiled to different languages. 

####Not Goals: 
* NOT to create another compiler for C#, Java, or JS/TS. This project's parser expects valid code as input, the few error messages that are present are NOT design to highlight syntax errors in the input. 
* NOT to create a valid AST for each supported language. Rather an AST like structure that supports the lowest common denominator between the targeted languages. This means many AST compromises are inevitable due to differences in language specs. 


Example:
--------

Source Code:

	namespace ParserExamples.Samples {
	
		/// <summary>
		/// A simple class to test parsing.
		/// </summary>
		public class SimpleCs {
	
		/// <value>The names.</value>
		public IList<string> Names { get; set; }
	
		/// <value>The number of names.</value>
		public int Count { get; set }
	
		/// <summary>
		/// Add name
		/// </summary>
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


Parser Code:

	CodeFileSrc<CodeLanguage> simpleCsAst = ParseCodeFile.parseCode(simpleCsName, CodeLanguageOptions.C_SHARP, simpleCsCode);
	WriteSettings ws = new WriteSettings(true, true, true);
	
	for(val block : CodeLanguageOptions.C_SHARP.getExtractor().extractClassFieldsAndMethodSignatures(simpleCsAst.getDoc())) {
		CodeFileParsed.Simple<String, CsBlock> fileParsed = new CodeFileParsed.Simple<>(simpleCsName, block.getValue(), block.getKey());
	
		StringBuilder sb = new StringBuilder();
		fileParsed.getParsedClass().toJson(sb, ws);
		System.out.println(sb.toString());
	}


JSON Result:

	{
		"classSignature" : {
			"access" : "PUBLIC",
			"name" : "SimpleCs",
			"declarationType" : "class"
		},
		"blockType" : "CLASS",
		"using" : [],
		"fields" : [{
				"name" : "SimpleCs.Names",
				"type" : "IList[string]"
			}, {
				"name" : "SimpleCs.Count",
				"type" : "int"
			}
		],
		"methods" : [{
				"name" : "SimpleCs.AddName",
				"parameters" : [{
						"type" : "string",
						"name" : "name"
					}
				],
				"annotations" : [{
						"name" : "OperationContract",
						"arguments" : {}
	
					}, {
						"name" : "WebInvoke",
						"arguments" : {
							"ResponseFormat" : "WebMessageFormat.Json",
							"Method" : "POST",
							"UriTemplate" : "/AddName?name={name}"
						}
					}, {
						"name" : "TransactionFlow",
						"arguments" : {
							"value" : "TransactionFlowOption.Allowed"
						}
					}
				],
				"returnType" : {
					"typeName" : "Result",
					"genericParameters" : [{
							"typeName" : "IList",
							"genericParameters" : [{
									"typeName" : "String"
								}
							]
						}
					]
				}
			}
		]
	}
