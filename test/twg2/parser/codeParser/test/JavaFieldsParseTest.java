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
import twg2.parser.codeParser.java.JavaBlock;
import twg2.parser.codeParser.java.JavaKeyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.test.utils.CodeFileAndAst;
import twg2.parser.workflow.CodeFileParsed;

/**
 * @author TeamworkGuy2
 * @since 2021-08-14
 */
public class JavaFieldsParseTest {
	private static List<String> srcLines = ls(
		"package ParserExamples.Fields;",
		"",
		"/** A simple class to test field parsing.",
		" * @since 2021-8-14",
		" */",
		"public class FieldsJava {",
		"",
		"    /** The modification count. */",
		"    @DefaultValue(-1)",
		"    private int mod = -1;",
		"",
		"    // The lower and upper.",
		"    private String lower;private String upper ;",
		"",
		"    /** The names. */",
		"    public List<String> Names = DEFAULT_NAMES;",
		"",
		"    /** The number of Names. */",
		"    public int Count = 1;",
		"",
		"    /** The number of names. */",
		"    protected float C2 = 3.141592f;",
		"",
		"    /** The number of names. */",
		"    protected double C3 = (double)1.23456789D;",
		"",
		"    /** The access timestamps. */",
		"    public ZonedDateTime[] accesses;",
		"",
		"    public String name = \"functional\";",
		"    public TrackInfo TrackInfo;",
		"",
		"}"
	);

	@Parameter
	private CodeFileAndAst<JavaBlock> simpleJava;


	public JavaFieldsParseTest() throws IOException {
		simpleJava = CodeFileAndAst.<JavaBlock>parse(CodeLanguageOptions.JAVA, "FieldsJava.java", "ParserExamples.Fields.FieldsJava", true, srcLines);
	}


	@Test
	public void simpleJavaParseTest() {
		List<CodeFileParsed.Simple<JavaBlock>> blocks = simpleJava.parsedBlocks;
		String fullClassName = simpleJava.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<JavaBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(10, clas.getFields().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());

		List<FieldDef> fields = clas.getFields();
		assertField(fields, 0, fullClassName + ".mod", "int", "-1");
		Assert.assertEquals(ls(" The modification count. "), fields.get(0).getComments());
		assertAnnotation(fields.get(0).getAnnotations(), 0, "DefaultValue", new String[] { "value" }, "-1");
		Assert.assertArrayEquals(ary(JavaKeyword.PRIVATE), fields.get(0).getAccessModifiers().toArray());

		assertField(fields, 1, fullClassName + ".lower", "String");
		Assert.assertEquals(ls(" The lower and upper.\n"), fields.get(1).getComments());
		Assert.assertArrayEquals(ary(JavaKeyword.PRIVATE), fields.get(1).getAccessModifiers().toArray());
		assertField(fields, 2, fullClassName + ".upper", "String");
		Assert.assertArrayEquals(ary(JavaKeyword.PRIVATE), fields.get(2).getAccessModifiers().toArray());
		assertField(fields, 3, fullClassName + ".Names", ary("List", ary("String")), "DEFAULT_NAMES");
		Assert.assertArrayEquals(ary(JavaKeyword.PUBLIC), fields.get(3).getAccessModifiers().toArray());
		assertField(fields, 4, fullClassName + ".Count", "int", "1");
		Assert.assertArrayEquals(ary(JavaKeyword.PUBLIC), fields.get(4).getAccessModifiers().toArray());
		assertField(fields, 5, fullClassName + ".C2", "float", "3.141592f");
		Assert.assertArrayEquals(ary(JavaKeyword.PROTECTED), fields.get(5).getAccessModifiers().toArray());
		assertField(fields, 6, fullClassName + ".C3", "double", "(double)1.23456789D");
		Assert.assertArrayEquals(ary(JavaKeyword.PROTECTED), fields.get(6).getAccessModifiers().toArray());
		assertField(fields, 7, fullClassName + ".accesses", "ZonedDateTime[]");
		Assert.assertArrayEquals(ary(JavaKeyword.PUBLIC), fields.get(7).getAccessModifiers().toArray());
		assertField(fields, 8, fullClassName + ".name", "String");
		Assert.assertArrayEquals(ary(JavaKeyword.PUBLIC), fields.get(8).getAccessModifiers().toArray());
		assertField(fields, 9, fullClassName + ".TrackInfo", "TrackInfo");
		Assert.assertArrayEquals(ary(JavaKeyword.PUBLIC), fields.get(9).getAccessModifiers().toArray());
	}

}
