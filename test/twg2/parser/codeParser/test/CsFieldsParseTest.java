package twg2.parser.codeParser.test;

import static twg2.parser.test.utils.AnnotationAssert.assertAnnotation;
import static twg2.parser.test.utils.FieldAssert.assertField;
import static twg2.parser.test.utils.TypeAssert.ary;
import static twg2.parser.test.utils.TypeAssert.ls;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.field.FieldDef;
import twg2.parser.codeParser.AccessModifierEnum;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.csharp.CsKeyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.test.utils.CodeFileAndAst;
import twg2.parser.workflow.CodeFileParsed;

/**
 * @author TeamworkGuy2
 * @since 2021-06-26
 */
public class CsFieldsParseTest {
	private static List<String> srcLines = ls(
		"namespace ParserExamples.Fields",
		"{",
		"  /// <summary>",
		"  /// A simple class to test field parsing.",
		"  /// </summary>",
		"  public class FieldsCs",
		"  {",
		"    /// <value>The modification count.</value>",
		"    [DefaultValue(-1)]",
		"    private int mod = -1;",
		"",
		"    // The lower and upper.",
		"    private string lower;private string upper ;",
		"",
		"    /// <value>The names.</value>",
		"    public IList<string> Names { get; set; } = DEFAULT_NAMES;",
		"",
		"    /// <value>The number of names.</value>",
		"    public int Count { set; } = 1;",
		"",
		"    /// <value>The number of names.</value>",
		"    protected float C2 { get; private set; } = 3.141592f;",
		"",
		"    /// <value>The number of names.</value>",
		"    protected internal decimal C3 { private get; private set; } = (decimal)1.23456789;",
		"",
		"    /// <value>The access timestamps.</value>",
		"    public DateTime[] accesses { set { this.mod++; this.accesses = value; } }",
		"",
		"    public string Name { get { this.mod++; return this.name != null ? this._name : \"\"; } set { this.mod++; this._name = value; } } = \"functional\";",
		"    public TrackInfo TrackInfo { get; }",
		"  }",
		"",
		"}"
	);

	@Parameter
	private CodeFileAndAst<CsBlock> simpleCs;


	public CsFieldsParseTest() throws IOException {
		simpleCs = CodeFileAndAst.<CsBlock>parse(CodeLanguageOptions.C_SHARP, "FieldsCs.cs", "ParserExamples.Fields.FieldsCs", true, srcLines);
	}


	@Test
	public void simpleCsParseTest() {
		List<CodeFileParsed.Simple<CsBlock>> blocks = simpleCs.parsedBlocks;
		String fullClassName = simpleCs.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<CsBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(10, clas.getFields().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());

		List<FieldDef> fields = clas.getFields();
		assertField(fields, 0, fullClassName + ".mod", "int", "-1");
		Assert.assertEquals(ls(" <value>The modification count.</value>\n"), fields.get(0).getComments());
		assertAnnotation(fields.get(0).getAnnotations(), 0, "DefaultValue", new String[] { "value" }, "-1");
		Assert.assertArrayEquals(ary(CsKeyword.PRIVATE), fields.get(0).getAccessModifiers().toArray());

		assertField(fields, 1, fullClassName + ".lower", "string");
		Assert.assertEquals(ls(" The lower and upper.\n"), fields.get(1).getComments());
		Assert.assertArrayEquals(ary(CsKeyword.PRIVATE), fields.get(1).getAccessModifiers().toArray());
		assertField(fields, 2, fullClassName + ".upper", "string");
		Assert.assertArrayEquals(ary(CsKeyword.PRIVATE), fields.get(2).getAccessModifiers().toArray());
		assertField(fields, 3, fullClassName + ".Names", ary("IList", ary("string")), "DEFAULT_NAMES");
		Assert.assertArrayEquals(ary(CsKeyword.PUBLIC), fields.get(3).getAccessModifiers().toArray());
		assertField(fields, 4, fullClassName + ".Count", "int", "1");
		Assert.assertArrayEquals(ary(CsKeyword.PUBLIC), fields.get(4).getAccessModifiers().toArray());
		assertField(fields, 5, fullClassName + ".C2", "float", "3.141592f");
		Assert.assertArrayEquals(ary(CsKeyword.PROTECTED), fields.get(5).getAccessModifiers().toArray());
		assertField(fields, 6, fullClassName + ".C3", "decimal", "(decimal)1.23456789");
		Assert.assertArrayEquals(ary(CsKeyword.PROTECTED, CsKeyword.INTERNAL), fields.get(6).getAccessModifiers().toArray());
		assertField(fields, 7, fullClassName + ".accesses", "DateTime[]");
		Assert.assertArrayEquals(ary(CsKeyword.PUBLIC), fields.get(7).getAccessModifiers().toArray());
		assertField(fields, 8, fullClassName + ".Name", "string");
		Assert.assertArrayEquals(ary(CsKeyword.PUBLIC), fields.get(8).getAccessModifiers().toArray());
		assertField(fields, 9, fullClassName + ".TrackInfo", "TrackInfo");
		Assert.assertArrayEquals(ary(CsKeyword.PUBLIC), fields.get(9).getAccessModifiers().toArray());
	}

}
