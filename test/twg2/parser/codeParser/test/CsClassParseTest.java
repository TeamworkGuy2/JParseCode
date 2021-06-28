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
		"    [IntAnnotation(-1)]",
		"    private int mod;",
		"",
		"    /// <value>The name.</value>",
		"    private string _name;",
		"",
		"    /// <value>The access timestamps.</value>",
		"    public string Name { get { this.mod++; return this._name != null ? this._name : \"\"; } set { this.mod++; this._name = value; } }",
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
		"    /// <value>The track</value>",
		"    public TrackInfo TrackInfo { get; }",
		"",
		"    /// <summary>Add name</summary>",
		"    /// <param name=\"name\">the name</param>",
		"    /// <returns>the names</returns>",
		"    Result<IList<String>> AddName([NotNull]string name, int zest = 0) {",
		"        content = block ? yes : no;",
		"    }",
		"",
		"    /// <summary>Set names</summary>",
		"    /// <param name=\"names\">the names</param>",
		"    /// <returns>the names</returns>",
		"    IList<int?> SetNames(this SimpleCs inst, ref Constraints constraints, params string[] names) {",
		"        new { data = SetNames };",
		"        : new Dictionary<string, object> { content = value } : new { content = object };",
		"    }",
		"",
		"    private void CheckState(int guid, params string[] states) {",
		"",
		"        void Accept(string state) {",
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


	@Test
	public void simpleCsParseTest() {
		CodeFileAndAst<CsBlock> simpleCs = CodeFileAndAst.<CsBlock>parse(CodeLanguageOptions.C_SHARP, "SimpleCs.cs", "ParserExamples.Samples.SimpleCs", true, srcLines);
		List<CodeFileParsed.Simple<CsBlock>> blocks = simpleCs.parsedBlocks;

		String fullClassName = simpleCs.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<CsBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(9, clas.getFields().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());
		AnnotationAssert.assertAnnotation(clas.getSignature().getAnnotations(), 0, "StringAnnotation", new String[] { "value" }, "-SimpleCs-");

		var fields = clas.getFields();
		assertField(fields, 0, fullClassName + ".mod", "int");
		Assert.assertEquals(ls(" <value>The modification count.</value>\n"), fields.get(0).getComments());
		var as = fields.get(0).getAnnotations();
		// annotation: IntAnnotation(-1)
		assertAnnotation(as, 0, "IntAnnotation", new String[] { "value" }, "-1");

		assertField(fields, 1, fullClassName + "._name", "string");
		assertField(fields, 2, fullClassName + ".Name", "string");
		assertField(fields, 3, fullClassName + ".Names", ary("IList", ary("string")));
		assertField(fields, 4, fullClassName + ".Count", "int");
		assertField(fields, 5, fullClassName + ".C2", "float");
		assertField(fields, 6, fullClassName + ".C3", "decimal");
		assertField(fields, 7, fullClassName + ".accesses", "DateTime[]");
		assertField(fields, 8, fullClassName + ".TrackInfo", "TrackInfo");

		// methods:
		Assert.assertEquals(3, clas.getMethods().size());

		// AddName(...)
		MethodSigSimple m = clas.getMethods().get(0);
		Assert.assertEquals(fullClassName + ".AddName", NameUtil.joinFqName(m.fullName));
		Assert.assertEquals(ls(" <summary>Add name</summary>\n",
				" <param name=\"name\">the name</param>\n",
				" <returns>the names</returns>\n"), m.comments);
		var ps = m.paramSigs;
		Assert.assertEquals(2, ps.size());
		assertParameter(ps, 0, "name", "string", false, null, null, ls(new AnnotationSig("NotNull", NameUtil.splitFqName("NotNull"), Collections.emptyMap())));
		assertParameter(ps, 1, "zest", "int", false, "0", null, null);
		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "IList", "genericParameters": [ {"typeName": "String"}]}]}
		assertType(ary("Result", ary("IList", ary("String"))), m.returnType);

		// SetNames(...)
		m = clas.getMethods().get(1);
		Assert.assertEquals(fullClassName + ".SetNames", NameUtil.joinFqName(m.fullName));
		ps = m.paramSigs;
		assertParameter(ps, 0, "inst", "SimpleCs", false, null, ls(CsKeyword.THIS), null);
		assertParameter(ps, 1, "constraints", "Constraints", false, null, ls(CsKeyword.REF), null);
		assertParameter(ps, 2, "names", "string[]", false, null, ls(CsKeyword.PARAMS), null);

		// CheckState(...)
		m = clas.getMethods().get(2);
		Assert.assertEquals(fullClassName + ".CheckState", NameUtil.joinFqName(m.fullName));
		ps = m.paramSigs;
		assertParameter(ps, 0, "guid", "int", false, null, null, null);
		assertParameter(ps, 1, "states", "string[]", false, null, ls(CsKeyword.PARAMS), null);
	}


	private static List<String> srcLines2 = ls(
		"namespace ParserExamples.Samples {",
		"",
		"  /// <summary>An abstract class</summary>",
		"  public abstract class AbstractClass {",
		"    [JsonProperty(\"id\")]",
		"    public int? Id { get; set; }",
		"    [JsonProperty(\"email\")]",
		"    public string Email { get; set; }",
		"  }",
		"}"
	);


	@Test
	public void abstractClassParseTest() {
		CodeFileAndAst<CsBlock> abstractClass = CodeFileAndAst.<CsBlock>parse(CodeLanguageOptions.C_SHARP, "AbstractClass.cs", "ParserExamples.Samples.AbstractClass", true, srcLines2);
		List<CodeFileParsed.Simple<CsBlock>> blocks = abstractClass.parsedBlocks;

		String fullClassName = abstractClass.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<CsBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(2, clas.getFields().size());
		Assert.assertEquals(0, clas.getMethods().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());

		var fields = clas.getFields();
		assertField(fields, 0, fullClassName + ".Id", "int?");
		assertField(fields, 1, fullClassName + ".Email", "string");
	}

}
