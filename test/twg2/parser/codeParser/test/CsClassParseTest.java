package twg2.parser.codeParser.test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import lombok.val;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.method.MethodSig;
import twg2.ast.interm.method.ParameterSig;
import twg2.io.files.FileReadUtil;
import twg2.parser.codeParser.AccessModifierEnum;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.csharp.CsBlockParser;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguage;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.main.ParseCodeFile;
import twg2.parser.test.utils.CodeFileAndAst;
import twg2.parser.workflow.CodeFileSrc;
import static twg2.parser.test.utils.ParseAnnotationAssert.*;

/**
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class CsClassParseTest {
	private static CodeFileAndAst<CsBlock> simpleCs = CodeFileAndAst.<CsBlock>parse(CodeLanguageOptions.C_SHARP, "SimpleCs.cs", "ParserExamples.Samples.SimpleCs", true, Arrays.asList(
		"namespace ParserExamples.Samples {",
		"",
		"  /// <summary>",
		"  /// A simple class to test parsing.",
		"  /// </summary>",
		"  public class SimpleCs {",
		"",
		"    /// <value>The modification count.</value>",
		"    [EmptyAnnotation()]",
		"    [IntAnnotation(-1)]",
		"    [BoolAnnotation(true)]",
		"    [IdentifierAnnotation(Integer.TYPE)]",
		"    [StringAnnotation(Name = \"\")]",
		"    [MultiArgAnnotation(\"abc\", false , 1.23)]",
		"    [MultiNamedArgAnnotation(num =1.23, flag=false ,value = \"abc\")]",
		"    private int mod;",
		"",
		"    /// <value>The name.</value>",
		"    private string _name;",
		"",
		"    /// <value>The names.</value>",
		"    public IList<string> Names { get; }",
		"",
		"    /// <value>The number of names.</value>",
		"    public int Count { set; }",
		"",
		"    /// <value>The number of names.</value>",
		"    public float C2 { get; private set; }",
		"",
		"    /// <value>The number of names.</value>",
		"    public decimal C3 { private get; private set; }",
		"",
		"    /// <value>The access timestamps.</value>",
		"    public DateTime[] accesses { set { this.mod++; this.accesses = value; } }",
		"",
		"    /// <value>The access timestamps.</value>",
		"    public string name { get { this.mod++; return this._name; } set { this.mod++; this._name = value; } }",
		"",
		"    /// <summary>Add name</summary>",
		"    /// <param name=\"name\">the name</param>",
		"    /// <returns>the names</returns>",
		"    [OperationContract]",
		"    [WebInvoke(Method = \"POST\", UriTemplate = \"/AddName?name={name}\",",
		"        ResponseFormat = WebMessageFormat.Json)]",
		"    [TransactionFlow(TransactionFlowOption.Allowed)]",
		"    Result<IList<String>> AddName(string name) {",
		"        content of block;",
		"    }",
		"",
		"  }",
		"",
		"}"
	));


	@Parameter
	private CodeFileSrc<CodeLanguage> file = ParseCodeFile.parseFiles(Arrays.asList(Paths.get("rsc/csharp/ParserExamples/Models/TrackInfo.cs")), FileReadUtil.threadLocalInst(), null).get(0);


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
		val blocks = simpleCs.parsedBlocks;
		val fullClassName = simpleCs.fullClassName;
		Assert.assertEquals(1, blocks.size());
		val clas = blocks.get(0).getParsedClass();
		Assert.assertEquals(8, clas.getFields().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());

		FieldSig f = clas.getFields().get(0);
		Assert.assertEquals(fullClassName + ".mod", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("int", f.getFieldType().getTypeName());
		Assert.assertEquals(Arrays.asList(" <value>The modification count.</value>\n"), f.getComments());
		// annotation: EmptyAnnotation()
		assertAnnotation(f.getAnnotations(), 0, "EmptyAnnotation", new String[0], new String[0]);
		// annotation: IntAnnotation(-1)
		assertAnnotation(f.getAnnotations(), 1, "IntAnnotation", new String[] { "value" }, "-1");
		// annotation: BoolAnnotation(-1)
		assertAnnotation(f.getAnnotations(), 2, "BoolAnnotation", new String[] { "value" }, "true");
		// annotation: IdentifierAnnotation(Integer.TYPE)
		assertAnnotation(f.getAnnotations(), 3, "IdentifierAnnotation", new String[] { "value" }, "Integer.TYPE");
		// annotation: StringAnnotation(Name = "")
		assertAnnotation(f.getAnnotations(), 4, "StringAnnotation", new String[] { "Name" }, "");
		// annotation: MultiArgAnnotation(\"abc\", false, 1.23)
		assertAnnotation(f.getAnnotations(), 5, "MultiArgAnnotation", new String[] { "arg1", "arg2", "arg3" }, "abc", "false", "1.23");
		// annotations: MultiNamedArgAnnotation(num =1.23, flag=false ,value = "abc")
		assertAnnotation(f.getAnnotations(), 6, "MultiNamedArgAnnotation", new String[] { "num", "flag", "value" }, "1.23", "false", "abc");

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
		assertAnnotation(m.getAnnotations(), 0, "OperationContract", new String[0], new String[0]);

		//{"name": "WebInvoke", "arguments": { "ResponseFormat": "WebMessageFormat.Json", "Method": "POST", "UriTemplate": "/AddName?name={name}" } },
		assertAnnotation(m.getAnnotations(), 1, "WebInvoke", new String[] { "ResponseFormat", "Method", "UriTemplate" }, new String[] { "WebMessageFormat.Json", "POST", "/AddName?name={name}" });

		//{"name": "TransactionFlow", "arguments": { "value": "TransactionFlowOption.Allowed" } }
		assertAnnotation(m.getAnnotations(), 2, "TransactionFlow", new String[] { "value" }, new String[] { "TransactionFlowOption.Allowed" });

		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "IList", "genericParameters": [ {"typeName": "String"}]}]}
		Assert.assertEquals("Result", m.getReturnType().getTypeName());
		Assert.assertEquals("IList", m.getReturnType().getParams().get(0).getTypeName());
		Assert.assertEquals("String", m.getReturnType().getParams().get(0).getParams().get(0).getTypeName());
	}

}
