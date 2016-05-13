package twg2.parser.codeParser.test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.val;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.method.MethodSig;
import twg2.ast.interm.method.ParameterSig;
import twg2.io.files.FileReadUtil;
import twg2.parser.baseAst.AccessModifierEnum;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFileParsed;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.csharp.CsBlockParser;
import twg2.parser.language.CodeLanguage;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.main.ParseCodeFile;
import twg2.parser.output.WriteSettings;
import twg2.parser.test.ParseAnnotationTest;

/**
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class CsClassParseTest {
	private static String fileName = "SimpleCs.cs";
	private static String fullClassName = "ParserExamples.Samples.SimpleCs";
	private static final String simpleCsCode =
		"namespace ParserExamples.Samples {\n" +
		"\n" +
		"  /// <summary>\n" +
		"  /// A simple class to test parsing.\n" +
		"  /// </summary>\n" +
		"  public class SimpleCs {\n" +
		"\n" +
		"    /// <value>The modification count.</value>\n" +
		"    [EmptyAnnotation()]\n" +
		"    [IntAnnotation(-1)]\n" +
		"    [BoolAnnotation(true)]\n" +
		"    [IdentifierAnnotation(Integer.TYPE)]\n" +
		"    [StringAnnotation(Name = \"\")]\n" +
		"    [MultiArgAnnotation(\"abc\", false , 1.23)]\n" +
		"    [MultiNamedArgAnnotation(num =1.23, flag=false ,value = \"abc\")]\n" +
		"    private int mod;\n" +
		"\n" +
		"    /// <value>The name.</value>\n" +
		"    private string _name;\n" +
		"\n" +
		"    /// <value>The names.</value>\n" +
		"    public IList<string> Names { get; }\n" +
		"\n" +
		"    /// <value>The number of names.</value>\n" +
		"    public int Count { set; }\n" +
		"\n" +
		"    /// <value>The number of names.</value>\n" +
		"    public float C2 { get; private set; }\n" +
		"\n" +
		"    /// <value>The number of names.</value>\n" +
		"    public decimal C3 { private get; private set; }\n" +
		"\n" +
		"    /// <value>The access timestamps.</value>\n" +
		"    public DateTime[] accesses { set { this.mod++; this.accesses = value; } }\n" +
		"\n" +
		"    /// <value>The access timestamps.</value>\n" +
		"    public string name { get { this.mod++; return this._name; } set { this.mod++; this._name = value; } }\n" +
		"\n" +
        "    /// <summary>Add name</summary>\n" +
        "    /// <param name=\"name\">the name</param>\n" +
        "    /// <returns>the names</returns>\n" +
        "    [OperationContract]\n" +
        "    [WebInvoke(Method = \"POST\", UriTemplate = \"/AddName?name={name}\",\n" +
        "        ResponseFormat = WebMessageFormat.Json)]\n" +
        "    [TransactionFlow(TransactionFlowOption.Allowed)]\n" +
        "    Result<IList<String>> AddName(string name) {\n" +
        "        content of block;\n" +
        "    }\n" +
        "\n" +
        "  }\n" +
		"\n" +
		"}\n";
	private static CodeFileSrc<CodeLanguage> simpleAst = ParseCodeFile.parseCode(fileName, CodeLanguageOptions.C_SHARP, simpleCsCode);
	private static List<CodeFileParsed.Simple<String, CsBlock>> parsedBlocks = new ArrayList<>();

	static {
		System.out.println(simpleCsCode);

		val blockDeclarations = CodeLanguageOptions.C_SHARP.getExtractor().extractClassFieldsAndMethodSignatures(simpleAst.getDoc());
		for(val block : blockDeclarations) {
			//CodeFileParsed.Simple<CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>, CompoundBlock> fileParsed = new CodeFileParsed.Simple<>(parsedFile, block.getValue(), block.getKey());
			CodeFileParsed.Simple<String, CsBlock> fileParsed = new CodeFileParsed.Simple<>(fileName, block.getValue(), block.getKey());
			parsedBlocks.add(fileParsed);

			try {
				val ws = new WriteSettings(true, true, true, true);
				val sb = new StringBuilder();
				fileParsed.getParsedClass().toJson(sb, ws);
				System.out.println(sb.toString());
			} catch(IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}
	}


	@Parameter
	private CodeFileSrc<CodeLanguage> file = ParseCodeFile.parseFiles(Arrays.asList(Paths.get("rsc/csharp/ParserExamples/Models/TrackInfo.cs")), FileReadUtil.threadLocalInst()).get(0);


	public CsClassParseTest() throws IOException {
	}


	@Test
	public void parseBlocksTest() {
		val tree = file.getDoc();
		val blocks = new CsBlockParser().extractClassFieldsAndMethodSignatures(tree);

		Assert.assertEquals(1, blocks.size());

		val trackInfoBlock = blocks.get(0).getValue();
		Assert.assertEquals(CsBlock.CLASS, trackInfoBlock.getBlockType());
		Assert.assertEquals("TrackInfo", trackInfoBlock.getSignature().getSimpleName());
	}


	@Test
	public void simpleCsParseTest() {
		Assert.assertEquals(1, parsedBlocks.size());
		val clas = parsedBlocks.get(0).getParsedClass();
		Assert.assertEquals(8, clas.getFields().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());

		FieldSig f = clas.getFields().get(0);
		Assert.assertEquals(fullClassName + ".mod", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("int", f.getFieldType().getTypeName());
		Assert.assertEquals(Arrays.asList(" <value>The modification count.</value>\n"), f.getComments());
		// annotation: EmptyAnnotation()
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 0, "EmptyAnnotation", new String[0], new String[0]);
		// annotation: IntAnnotation(-1)
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 1, "IntAnnotation", new String[] { "value" }, "-1");
		// annotation: BoolAnnotation(-1)
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 2, "BoolAnnotation", new String[] { "value" }, "true");
		// annotation: IdentifierAnnotation(Integer.TYPE)
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 3, "IdentifierAnnotation", new String[] { "value" }, "Integer.TYPE");
		// annotation: StringAnnotation(Name = "")
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 4, "StringAnnotation", new String[] { "Name" }, "");
		// annotation: MultiArgAnnotation(\"abc\", false, 1.23)
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 5, "MultiArgAnnotation", new String[] { "arg1", "arg2", "arg3" }, "abc", "false", "1.23");
		// annotations: MultiNamedArgAnnotation(num =1.23, flag=false ,value = "abc")
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 6, "MultiNamedArgAnnotation", new String[] { "num", "flag", "value" }, "1.23", "false", "abc");

		f = clas.getFields().get(1);
		Assert.assertEquals(fullClassName + "._name", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("string", f.getFieldType().getTypeName());

		f = clas.getFields().get(2);
		Assert.assertEquals(fullClassName + ".Names", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("IList", f.getFieldType().getTypeName());
		Assert.assertEquals("string", f.getFieldType().getParams().get(0).getTypeName());

		f = clas.getFields().get(3);
		Assert.assertEquals(fullClassName + ".Count", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("int", f.getFieldType().getTypeName());

		f = clas.getFields().get(4);
		Assert.assertEquals(fullClassName + ".C2", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("float", f.getFieldType().getTypeName());

		f = clas.getFields().get(5);
		Assert.assertEquals(fullClassName + ".C3", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("decimal", f.getFieldType().getTypeName());

		f = clas.getFields().get(6);
		Assert.assertEquals(fullClassName + ".accesses", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("DateTime", f.getFieldType().getTypeName());
		Assert.assertEquals(1, f.getFieldType().getArrayDimensions());

		Assert.assertEquals(1, clas.getMethods().size());
		MethodSig.SimpleImpl m = clas.getMethods().get(0);
		Assert.assertEquals(fullClassName + ".AddName", NameUtil.joinFqName(m.getFullName()));
		ParameterSig p = m.getParamSigs().get(0);
		Assert.assertEquals("name", p.getName());
		Assert.assertEquals("string", p.getTypeSimpleName());
		Assert.assertEquals(Arrays.asList(" <summary>Add name</summary>\n",
				" <param name=\"name\">the name</param>\n",
				" <returns>the names</returns>\n"), m.getComments());
		// annotations:
		//{"name": "OperationContract", "arguments": {  } },
		ParseAnnotationTest.assertAnnotation(m.getAnnotations(), 0, "OperationContract", new String[0], new String[0]);

		//{"name": "WebInvoke", "arguments": { "ResponseFormat": "WebMessageFormat.Json", "Method": "POST", "UriTemplate": "/AddName?name={name}" } },
		ParseAnnotationTest.assertAnnotation(m.getAnnotations(), 1, "WebInvoke", new String[] { "ResponseFormat", "Method", "UriTemplate" }, new String[] { "WebMessageFormat.Json", "POST", "/AddName?name={name}" });

		//{"name": "TransactionFlow", "arguments": { "value": "TransactionFlowOption.Allowed" } }
		ParseAnnotationTest.assertAnnotation(m.getAnnotations(), 2, "TransactionFlow", new String[] { "value" }, new String[] { "TransactionFlowOption.Allowed" });

		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "IList", "genericParameters": [ {"typeName": "String"}]}]}
		Assert.assertEquals("Result", m.getReturnType().getTypeName());
		Assert.assertEquals("IList", m.getReturnType().getParams().get(0).getTypeName());
		Assert.assertEquals("String", m.getReturnType().getParams().get(0).getParams().get(0).getTypeName());
	}

}
