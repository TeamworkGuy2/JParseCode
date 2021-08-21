package twg2.parser.codeParser.test;

import static twg2.parser.test.utils.AnnotationAssert.assertAnnotation;
import static twg2.parser.test.utils.FieldAssert.assertField;
import static twg2.parser.test.utils.MethodAssert.assertParameter;
import static twg2.parser.test.utils.TypeAssert.assertType;
import static twg2.parser.test.utils.TypeAssert.ary;
import static twg2.parser.test.utils.TypeAssert.ls;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.method.ParameterSig;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.parser.codeParser.AccessModifierEnum;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.csharp.CsKeyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.test.utils.AnnotationAssert;
import twg2.parser.test.utils.CodeFileAndAst;
import twg2.parser.workflow.CodeFileParsed;

/**
 * @author TeamworkGuy2
 * @since 2021-06-27
 */
public class CsAnnotationParseTest {
	private static List<String> srcLines = ls(
		"namespace ParserExamples.Samples {",
		"",
		"  /// <summary>",
		"  /// A simple class to test parsing.",
		"  /// </summary>",
		"  [StringAnnotation(\"-CsAnnotations-\")]",
		"  public class CsAnnotations {",
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
		"    [ImplementationDetail(typeof(string)), Nullable]",
		"    private string _name;",
		"",
		"    /// <value>The names.</value>",
		"    public IList<string> Names { get; }",
		"",
		"    /// <value>The number of names.</value>",
		"    public int Count { get; private set; }",
		"",
		"    /// <value>The access timestamps.</value>",
		"    public DateTime[] accesses { set { this.mod++; this.accesses = value; } }",
		"",
		"    /// <value>The access timestamps.</value>",
		"    public string Name { get { this.mod++; return this._name != null ? this._name : \"\"; } set { this.mod++; this._name = value; } }",
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
		"    Result<IList<String>> AddName([NotNull][Source]string name, int zest = 0) {",
		"        content = block ? yes : no;",
		"    }",
		"",
		"    /// <summary>Set names</summary>",
		"    /// <param name=\"names\">the names</param>",
		"    /// <returns>the names</returns>",
		"    [WebInvoke(Method = \"PUT\", UriTemplate = \"/SetNames?names={names}\",",
		"        ResponseFormat = WebMessageFormat.Json)]",
		"    IList<int?> SetNames(this CsAnnotations inst, ref Constraints constraints, params string[] names) {",
		"        new { data = SetNames };",
		"        : new Dictionary<string, object> { content = value } : new { content = object };",
		"    }",
		"",
		"    private void CheckState(int guid, params string[] states) {",
		"",
		"        void Accept([Default(\"--\")] string state) {",
		"            Console.WriteLine(state);",
		"        }",
		"",
		"        if (states != null && states.Length > 0) new List<string>(states).ForEach(Accept);",
		"    }",
		"",
		"  }",
		"",
		"}"
	);

	private static List<String> src2Lines = ls(
		"namespace ParserExamples.Samples {",
		"  /// <summary>",
		"  /// First comment for annotation and comment parsing.",
		"  /// Line two.",
		"  /// </summary>",
		"  [PluralAnnotation(1, \"st\")]",
		"  /// <summary>Second comment.</summary>",
		"  [PluralAnnotation(2, \"nd\")]",
		"  public class CsAnnotationComments {",
		"",
		"  }",
		"",
		"}"
	);


	@Test
	public void annotationsParseTest() {
		CodeFileAndAst<CsBlock> csAnnotations = CodeFileAndAst.<CsBlock>parse(CodeLanguageOptions.C_SHARP, "CsAnnotations.cs", "ParserExamples.Samples.CsAnnotations", true, srcLines);
		List<CodeFileParsed.Simple<CsBlock>> blocks = csAnnotations.parsedBlocks;

		String fullClassName = csAnnotations.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<CsBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(7, clas.getFields().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());
		AnnotationAssert.assertAnnotation(clas.getSignature().getAnnotations(), 0, "StringAnnotation", new String[] { "value" }, "-CsAnnotations-");

		var fields = clas.getFields();
		assertField(fields, 0, fullClassName + ".mod", "int");
		Assert.assertEquals(ls(" <value>The modification count.</value>\n"), fields.get(0).getComments());
		var as = fields.get(0).getAnnotations();
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
		as = fields.get(1).getAnnotations();
		// annotation: @ImplementationDetail(String.class)
		assertAnnotation(as, 0, "ImplementationDetail", new String[] { "value" }, "typeof(string)");
		// annotation: @Nullable
		assertAnnotation(as, 1, "Nullable", new String[0], new String[0]);

		assertField(fields, 2, fullClassName + ".Names", ary("IList", ary("string")));
		assertField(fields, 3, fullClassName + ".Count", "int");
		assertField(fields, 4, fullClassName + ".accesses", "DateTime[]");
		assertField(fields, 5, fullClassName + ".Name", "string");
		assertField(fields, 6, fullClassName + ".TrackInfo", "TrackInfo");

		// methods:
		Assert.assertEquals(3, clas.getMethods().size());

		// AddName(...)
		MethodSigSimple m = clas.getMethods().get(0);
		Assert.assertEquals(fullClassName + ".AddName", NameUtil.joinFqName(m.fullName));
		Assert.assertEquals(ls(" <summary>Add name</summary>\n",
				" <param name=\"name\">the name</param>\n",
				" <returns>the names</returns>\n"), m.comments);
		List<ParameterSig> ps = m.paramSigs;
		Assert.assertEquals(2, ps.size());
		assertParameter(ps, 0, "name", "string", false, null, null, ls(new AnnotationSig("NotNull", NameUtil.splitFqName("NotNull"), Collections.emptyMap()), new AnnotationSig("Source", NameUtil.splitFqName("Source"), Collections.emptyMap())));
		assertParameter(ps, 1, "zest", "int", false, "0", null, null);
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
		assertParameter(ps, 0, "inst", "CsAnnotations", false, null, ls(CsKeyword.THIS), null);
		assertParameter(ps, 1, "constraints", "Constraints", false, null, ls(CsKeyword.REF), null);
		assertParameter(ps, 2, "names", "string[]", false, null, ls(CsKeyword.PARAMS), null);
		// annotations:
		//{"name": "WebInvoke", "arguments": { "ResponseFormat": "WebMessageFormat.Json", "Method": "PUT", "UriTemplate": "/SetNames?names={names}" } },
		assertAnnotation(m.annotations, 0, "WebInvoke", new String[] { "ResponseFormat", "Method", "UriTemplate" }, new String[] { "WebMessageFormat.Json", "PUT", "/SetNames?names={names}" });

		// CheckState(...)
		m = clas.getMethods().get(2);
		Assert.assertEquals(fullClassName + ".CheckState", NameUtil.joinFqName(m.fullName));
		ps = m.paramSigs;
		assertParameter(ps, 0, "guid", "int", false, null, null, null);
		assertParameter(ps, 1, "states", "string[]", false, null, ls(CsKeyword.PARAMS), null);
	}


	@Test
	public void annotationCommentsParseTest() {
		CodeFileAndAst<CsBlock> csAnnotations = CodeFileAndAst.<CsBlock>parse(CodeLanguageOptions.C_SHARP, "CsAnnotationComments.cs", "ParserExamples.Samples.CsAnnotationComments", true, src2Lines);
		List<CodeFileParsed.Simple<CsBlock>> blocks = csAnnotations.parsedBlocks;

		String fullClassName = csAnnotations.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<CsBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(0, clas.getUsingStatements().size());
		Assert.assertEquals(0, clas.getFields().size());
		Assert.assertEquals(0, clas.getMethods().size());
		Assert.assertEquals(ls(" <summary>\n",
				" First comment for annotation and comment parsing.\n",
				" Line two.\n",
				" </summary>\n",
				" <summary>Second comment.</summary>\n"), clas.getSignature().getComments());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());
		AnnotationAssert.assertAnnotation(clas.getSignature().getAnnotations(), 0, "PluralAnnotation", new String[] { "arg1", "arg2" }, "1", "st");
		AnnotationAssert.assertAnnotation(clas.getSignature().getAnnotations(), 1, "PluralAnnotation", new String[] { "arg1", "arg2" }, "2", "nd");
	}
}
