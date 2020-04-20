package twg2.parser.codeParser.test;

import static twg2.parser.test.utils.AnnotationAssert.assertAnnotation;
import static twg2.parser.test.utils.FieldAssert.assertField;
import static twg2.parser.test.utils.MethodAssert.assertParameter;
import static twg2.parser.test.utils.TypeAssert.assertType;
import static twg2.parser.test.utils.TypeAssert.ary;
import static twg2.parser.test.utils.TypeAssert.ls;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.method.ParameterSig;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.io.files.FileReadUtil;
import twg2.parser.codeParser.AccessModifierEnum;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.csharp.CsBlockParser;
import twg2.parser.codeParser.csharp.CsKeyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.CodeToken;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.main.ParseCodeFile;
import twg2.parser.test.utils.AnnotationAssert;
import twg2.parser.test.utils.CodeFileAndAst;
import twg2.parser.workflow.CodeFileParsed;
import twg2.parser.workflow.CodeFileSrc;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class CsClassParseTest {
	private static List<String> srcLines = ls(
		"namespace ParserExamples.Samples {",
		"",
		"  /// <summary>",
		"  /// A simple class to test parsing.",
		"  /// </summary>",
		"  [StringAnnotation(\"-SimpleCs-\")]",
		"  public class SimpleCs {",
		"",
		"    /// <value>The modification count.</value>",
		"    [EmptyAnnotation()]",
		"    [IntAnnotation(-1)]",
		"    [BoolAnnotation(true)]",
		"    [IdentifierAnnotation(Integer.TYPE)]",
		"    [IdentifierAnnotation(default(float?))]",
		"    [IdentifierAnnotation(nameof(System.String))]",
		"    [IdentifierAnnotation(typeof(Dictionary<String, Integer>))]",
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
		"    public string name { get { this.mod++; return this.name != null ? this._name : \"\"; } set { this.mod++; this._name = value; } }",
		"",
		"    /// <value>The track</value>",
		"    public TrackInfo TrackInfo { get; }",
		"",
		"    /// <summary>Add name</summary>",
		"    /// <param name=\"name\">the name</param>",
		"    /// <returns>the names</returns>",
		"    [OperationContract]",
		"    [WebInvoke(Method = \"POST\", UriTemplate = \"/AddName?name={name}\",",
		"        ResponseFormat = WebMessageFormat.Json)]",
		"    [TransactionFlow(TransactionFlowOption.Allowed)]",
		"    Result<IList<String>> AddName([NotNull]string name, int zest = 0) {",
		"        content = block ? yes : no;",
		"    }",
		"",
		"    /// <summary>Set names</summary>",
		"    /// <param name=\"names\">the names</param>",
		"    /// <returns>the names</returns>",
		"    [OperationContract]",
		"    [WebInvoke(Method = \"PUT\", UriTemplate = \"/SetNames?names={names}\",",
		"        ResponseFormat = WebMessageFormat.Json)]",
		"    IList<int?> SetNames(this SimpleCs inst, ref Constraints constraints, params string[] names) {",
		"        new { data = SetNames };",
		"        : new Dictionary<string, object> { content = value } : new { content = object };",
		"    }",
		"",
		"  }",
		"",
		"}"
	);

	@Parameter
	private CodeFileAndAst<CsBlock> simpleCs = CodeFileAndAst.<CsBlock>parse(CodeLanguageOptions.C_SHARP, "SimpleCs.cs", "ParserExamples.Samples.SimpleCs", true, srcLines);

	@Parameter
	private CodeFileSrc file = ParseCodeFile.parseFiles(ls(Paths.get("rsc/csharp/ParserExamples/Models/TrackInfo.cs")), FileReadUtil.threadLocalInst(), null).get(0);


	public CsClassParseTest() throws IOException {
	}


	@Test
	public void parseBlocksTest() {
		SimpleTree<CodeToken> tree = file.astTree;
		List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<CsBlock>>> blocks = new CsBlockParser().extractClassFieldsAndMethodSignatures(tree);

		Assert.assertEquals(1, blocks.size());

		ClassAst.SimpleImpl<CsBlock> trackInfoBlock = blocks.get(0).getValue();
		Assert.assertEquals(CsBlock.CLASS, trackInfoBlock.getBlockType());
		Assert.assertEquals("TrackInfo", trackInfoBlock.getSignature().getSimpleName());

		Assert.assertEquals(ls("ISerializable", "IComparable<TrackInfo>"), trackInfoBlock.getSignature().getExtendImplementSimpleNames());
	}


	@Test
	public void simpleCsParseTest() {
		List<CodeFileParsed.Simple<CsBlock>> blocks = simpleCs.parsedBlocks;
		String fullClassName = simpleCs.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<CsBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(9, clas.getFields().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());
		AnnotationAssert.assertAnnotation(clas.getSignature().getAnnotations(), 0, "StringAnnotation", new String[] { "value" }, "-SimpleCs-");

		List<FieldSig> fields = clas.getFields();
		assertField(fields, 0, fullClassName + ".mod", "int");
		Assert.assertEquals(ls(" <value>The modification count.</value>\n"), fields.get(0).getComments());
		List<AnnotationSig> as = fields.get(0).getAnnotations();
		// annotation: EmptyAnnotation()
		assertAnnotation(as, 0, "EmptyAnnotation", new String[0], new String[0]);
		// annotation: IntAnnotation(-1)
		assertAnnotation(as, 1, "IntAnnotation", new String[] { "value" }, "-1");
		// annotation: BoolAnnotation(-1)
		assertAnnotation(as, 2, "BoolAnnotation", new String[] { "value" }, "true");
		// annotation: IdentifierAnnotation(Integer.TYPE)
		// annotation: IdentifierAnnotation(Integer.TYPE)
		assertAnnotation(as, 3, "IdentifierAnnotation", new String[] { "value" }, "Integer.TYPE");
		// annotation: IdentifierAnnotation(default(float?))
		assertAnnotation(as, 4, "IdentifierAnnotation", new String[] { "value" }, "default(float?)");
		// annotation: IdentifierAnnotation(nameof(System.String))
		assertAnnotation(as, 5, "IdentifierAnnotation", new String[] { "value" }, "nameof(System.String)");
		// annotation: IdentifierAnnotation(typeof(Dictionary<String, Integer>))
		assertAnnotation(as, 6, "IdentifierAnnotation", new String[] { "value" }, "typeof(Dictionary<String, Integer>)");
		// annotation: StringAnnotation(Name = "")
		assertAnnotation(as, 7, "StringAnnotation", new String[] { "Name" }, "");
		// annotation: MultiArgAnnotation(\"abc\", false, 1.23)
		assertAnnotation(as, 8, "MultiArgAnnotation", new String[] { "arg1", "arg2", "arg3" }, "abc", "false", "1.23");
		// annotations: MultiNamedArgAnnotation(num =1.23, flag=false ,value = "abc")
		assertAnnotation(as, 9, "MultiNamedArgAnnotation", new String[] { "num", "flag", "value" }, "1.23", "false", "abc");

		assertField(fields, 1, fullClassName + "._name", "string");
		assertField(fields, 2, fullClassName + ".Names", ary("IList", ary("string")));
		assertField(fields, 3, fullClassName + ".Count", "int");
		assertField(fields, 4, fullClassName + ".C2", "float");
		assertField(fields, 5, fullClassName + ".C3", "decimal");
		assertField(fields, 6, fullClassName + ".accesses", "DateTime[]");
		assertField(fields, 7, fullClassName + ".name", "string");
		assertField(fields, 8, fullClassName + ".TrackInfo", "TrackInfo");

		// methods:
		Assert.assertEquals(2, clas.getMethods().size());

		// AddName(...)
		MethodSigSimple m = clas.getMethods().get(0);
		Assert.assertEquals(fullClassName + ".AddName", NameUtil.joinFqName(m.fullName));
		Assert.assertEquals(ls(" <summary>Add name</summary>\n",
				" <param name=\"name\">the name</param>\n",
				" <returns>the names</returns>\n"), m.comments);
		List<ParameterSig> ps = m.paramSigs;
		Assert.assertEquals(2, ps.size());
		assertParameter(ps, 0, "name", "string", null, null, ls(new AnnotationSig("NotNull", NameUtil.splitFqName("NotNull"), Collections.emptyMap())));
		assertParameter(ps, 1, "zest", "int", "0", null, null);
		// annotations:
		//{"name": "OperationContract", "arguments": {  } },
		assertAnnotation(m.annotations, 0, "OperationContract", new String[0], new String[0]);

		//{"name": "WebInvoke", "arguments": { "ResponseFormat": "WebMessageFormat.Json", "Method": "POST", "UriTemplate": "/AddName?name={name}" } },
		assertAnnotation(m.annotations, 1, "WebInvoke", new String[] { "ResponseFormat", "Method", "UriTemplate" }, new String[] { "WebMessageFormat.Json", "POST", "/AddName?name={name}" });

		//{"name": "TransactionFlow", "arguments": { "value": "TransactionFlowOption.Allowed" } }
		assertAnnotation(m.annotations, 2, "TransactionFlow", new String[] { "value" }, new String[] { "TransactionFlowOption.Allowed" });

		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "IList", "genericParameters": [ {"typeName": "String"}]}]}
		assertType(ary("Result", ary("IList", ary("String"))), m.returnType);

		// SetNames(...)
		m = clas.getMethods().get(1);
		Assert.assertEquals(fullClassName + ".SetNames", NameUtil.joinFqName(m.fullName));
		ps = m.paramSigs;
		assertParameter(ps, 0, "inst", "SimpleCs", null, ls(CsKeyword.THIS), null);
		assertParameter(ps, 1, "constraints", "Constraints", null, ls(CsKeyword.REF), null);
		assertParameter(ps, 2, "names", "string[]", null, ls(CsKeyword.PARAMS), null);
	}

}
