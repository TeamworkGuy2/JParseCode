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
import twg2.parser.codeParser.java.JavaBlock;
import twg2.parser.codeParser.java.JavaKeyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.test.utils.AnnotationAssert;
import twg2.parser.test.utils.CodeFileAndAst;
import twg2.parser.workflow.CodeFileParsed;

/**
 * @author TeamworkGuy2
 * @since 2016-1-15
 */
public class JavaClassParseTest {
	private static List<String> srcLines = ls(
		"package ParserExamples.Samples;",
		"",
		"/** A simple class to test parsing.",
		" * @since 2016-1-15",
		" */",
		"@StringAnnotation(\"-SimpleJava-\")",
		"public class SimpleJava {",
		"",
		"    /** The modification count. */",
		"    @IntAnnotation(-1)",
		"    private int mod;",
		"",
		"    /** The name. */",
		"    private String _name;",
		"",
		"    /** The names. */",
		"    public List<String> Names;",
		"",
		"    /** The number of names. */",
		"    public int Count;",
		"",
		"    /** The access timestamps. */",
		"    public ZonedDateTime[] accesses;",
		"",
		"    /** The track */",
		"    private TrackInfo TrackInfo;",
		"",
		"    /** Add name",
		"     * @param name the name",
		"     * @return the names",
		"     */",
		"    Result<List<String>> AddName(@NotNull String name, int zest = 0) {",
		"        content = block ? yes : no;",
		"    }",
		"",
		"    List<Integer> SetNames(Constraints constraints, final String[] names) {",
		"        new { data = SetNames };",
		"        : new Dictionary<string, object> { content = value } : new { content = object };",
		"    }",
		"",
		"",
		"    private void CheckState(int guid, String... states) {",
		"        class Callback implements Consumer<String> {",
		"            @Override public void accept(String state) {",
		"                System.out.println();",
		"            }",
		"        }",
		"        if(states != null && states.length > 0) Arrays.asList(states).forEach(new Callback());",
		"    }",
		"",
		"}"
	);


	@Test
	public void simpleJavaParseTest() {
		CodeFileAndAst<JavaBlock> simpleJava = CodeFileAndAst.<JavaBlock>parse(CodeLanguageOptions.JAVA, "SimpleJava.java", "ParserExamples.Samples.SimpleJava", true, srcLines);
		List<CodeFileParsed.Simple<JavaBlock>> blocks = simpleJava.parsedBlocks;

		String fullClassName = simpleJava.fullClassName;
		Assert.assertEquals(2, blocks.size());
		ClassAst.SimpleImpl<JavaBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(6, clas.getFields().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());
		AnnotationAssert.assertAnnotation(clas.getSignature().getAnnotations(), 0, "StringAnnotation", new String[] { "value" }, "-SimpleJava-");
		Assert.assertEquals(ls(" A simple class to test parsing.\n" +
				" * @since 2016-1-15\n" + " "), clas.getSignature().getComments());

		var fields = clas.getFields();
		assertField(fields, 0, fullClassName + ".mod", "int");
		Assert.assertEquals(ls(" The modification count. "), fields.get(0).getComments());
		var as = fields.get(0).getAnnotations();
		// annotation: IntAnnotation(-1)
		assertAnnotation(as, 0, "IntAnnotation", new String[] { "value" }, "-1");

		assertField(fields, 1, fullClassName + "._name", "String");
		assertField(fields, 2, fullClassName + ".Names", ary("List", ary("String")));
		assertField(fields, 3, fullClassName + ".Count", "int");
		assertField(fields, 4, fullClassName + ".accesses", "ZonedDateTime[]");
		assertField(fields, 5, fullClassName + ".TrackInfo", "TrackInfo");

		// methods:
		Assert.assertEquals(3, clas.getMethods().size());

		// AddName(...)
		MethodSigSimple m = clas.getMethods().get(0);
		Assert.assertEquals(fullClassName + ".AddName", NameUtil.joinFqName(m.fullName));
		Assert.assertEquals(ls(" Add name\n" +
		        "     * @param name the name\n" +
		        "     * @return the names\n" +
		        "     "), m.comments);
		var ps = m.paramSigs;
		Assert.assertEquals(2, ps.size());
		assertParameter(ps, 0, "name", "String", false, null, null, ls(new AnnotationSig("NotNull", NameUtil.splitFqName("NotNull"), Collections.emptyMap())));
		assertParameter(ps, 1, "zest", "int", false, "0", null, null);
		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "IList", "genericParameters": [ {"typeName": "String"}]}]}
		assertType(ary("Result", ary("List", ary("String"))), m.returnType);

		// SetNames(...)
		m = clas.getMethods().get(1);
		Assert.assertEquals(fullClassName + ".SetNames", NameUtil.joinFqName(m.fullName));
		ps = m.paramSigs;
		assertParameter(ps, 0, "constraints", "Constraints", false, null, null, null);
		assertParameter(ps, 1, "names", "String[]", false, null, ls(JavaKeyword.FINAL), null);

		// CheckState(...) (int guid, String... states)
		m = clas.getMethods().get(2);
		Assert.assertEquals(fullClassName + ".CheckState", NameUtil.joinFqName(m.fullName));
		ps = m.paramSigs;
		assertParameter(ps, 0, "guid", "int", false, null, null, null);
		assertParameter(ps, 1, "states", "String...", false, null, null, null);
	}


	private static List<String> srcLines2 = ls(
		"package ParserExamples.Samples;",
		"",
		"/** An abstract class */",
		"public abstract class AbstractClass {",
		"  @JsonProperty(\"id\")",
		"  public int? Id;",
		"  @JsonProperty(\"email\")",
		"  public String Email;",
		"}"
	);


	@Test
	public void abstractClassParseTest() {
		CodeFileAndAst<JavaBlock> abstractClass = CodeFileAndAst.<JavaBlock>parse(CodeLanguageOptions.JAVA, "AbstractClass.java", "ParserExamples.Samples.AbstractClass", true, srcLines2);
		List<CodeFileParsed.Simple<JavaBlock>> blocks = abstractClass.parsedBlocks;

		String fullClassName = abstractClass.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<JavaBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(2, clas.getFields().size());
		Assert.assertEquals(0, clas.getMethods().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());

		var fields = clas.getFields();
		assertField(fields, 0, fullClassName + ".Id", "int?");
		assertField(fields, 1, fullClassName + ".Email", "String");
	}

}
