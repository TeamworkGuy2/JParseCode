package twg2.parser.codeParser.test;

import static twg2.parser.test.utils.AnnotationAssert.assertAnnotation;
import static twg2.parser.test.utils.FieldAssert.assertField;
import static twg2.parser.test.utils.MethodAssert.assertParameter;
import static twg2.parser.test.utils.TypeAssert.assertType;
import static twg2.parser.test.utils.TypeAssert.ary;
import static twg2.parser.test.utils.TypeAssert.ls;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.method.ParameterSig;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.parser.codeParser.AccessModifierEnum;
import twg2.parser.codeParser.java.JavaBlock;
import twg2.parser.codeParser.java.JavaKeyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.test.utils.CodeFileAndAst;
import twg2.parser.workflow.CodeFileParsed;

/**
 * @author TeamworkGuy2
 * @since 2016-1-15
 */
public class JavaModelParseTest {
	private static List<String> srcLines = ls(
		"package ParserExamples.Samples;",
		"",
		"/** A simple class to test parsing.",
		" * @since 2017-6-24",
		" */",
		"protected class Model1Java {",
		"",
		"    /** The modification count. */",
		"    @MultiLineAnnotation(\"alpha-1\", ",
		"        Double.TYPE ,",
		"        3)",
		"    private int mod;",
		"",
		"    private int? initialMod;",
		"",
		"    /** The name. */",
		"    private String _name = \"initial-name\";",
		"",
		"    /** The names. */",
		"    public Map<Integer, String> Props;",
		"",
		"    /** Set properties",
		"     * @param props the properties",
		"     * @return the properties",
		"     */",
		"    @SetterAnnotation(Prop = \"Props\", UriTemplate = \"/SetProps?props={props}\",",
		"        ResponseFormat = WebMessageFormat.Json)",
		"    public static Result<List<int?>> SetProps(final List<String>[] props) {",
		"        content of SetProps;",
		"    }",
		"",
		"    List<String> hiddenField;",
		"",
		"    List<Tuple<String, int>>[] _fields;",
		"}"
	);

	@Parameter
	private CodeFileAndAst<JavaBlock> simpleJava = CodeFileAndAst.<JavaBlock>parse(CodeLanguageOptions.JAVA, "Model1Java.java", "ParserExamples.Samples.Model1Java", true, srcLines);


	public JavaModelParseTest() throws IOException {
	}


	@Test
	public void model1JavaParseTest() {
		List<CodeFileParsed.Simple<JavaBlock>> blocks = simpleJava.parsedBlocks;
		String fullClassName = simpleJava.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<JavaBlock> clas = blocks.get(0).parsedClass;
		List<FieldDef> fields = clas.getFields();
		Assert.assertEquals(6, fields.size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.NAMESPACE_OR_INHERITANCE_LOCAL, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());

		assertField(fields, 0, fullClassName + ".mod", "int");
		Assert.assertEquals(ls(" The modification count. "), fields.get(0).getComments());
		List<AnnotationSig> as = fields.get(0).getAnnotations();
		// annotations: EmptyAnnotation()
		assertAnnotation(as, 0, "MultiLineAnnotation", new String[] { "arg1", "arg2", "arg3" }, "alpha-1", "Double.TYPE", "3");

		assertField(fields, 1, fullClassName + ".initialMod", "int?");
		assertField(fields, 2, fullClassName + "._name", "String", "\"initial-name\"");
		assertField(fields, 3, fullClassName + ".Props", ary("Map", ary("Integer", "String")));
		assertField(fields, 4, fullClassName + ".hiddenField", ary("List", ary("String")));
		assertField(fields, 5, fullClassName + "._fields", ary("List[]", ary("Tuple", ary("String", "int"))));

		// methods:
		Assert.assertEquals(1, clas.getMethods().size());

		// SetProps()
		MethodSigSimple m = clas.getMethods().get(0);
		Assert.assertEquals(fullClassName + ".SetProps", NameUtil.joinFqName(m.fullName));
		Assert.assertEquals(ls(" Set properties\n" +
		        "     * @param props the properties\n" +
		        "     * @return the properties\n" +
		        "     "), m.comments);
		List<ParameterSig> ps = m.paramSigs;
		assertParameter(ps, 0, "props", "List<String>[]", false, null, ls(JavaKeyword.FINAL), null);

		// annotations:
		// @SetterAnnotation(Prop = "Props", UriTemplate = "/SetProps?props={props}", ResponseFormat = WebMessageFormat.Json)
		assertAnnotation(m.annotations, 0, "SetterAnnotation", new String[] { "Prop", "UriTemplate", "ResponseFormat" }, new String[] { "Props", "/SetProps?props={props}", "WebMessageFormat.Json" });

		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "List", "genericParameters": [ {"typeName": "int", "nullable": true, "primitive": true}]}]}
		assertType(ary("Result", ary("List", ary("int?"))), m.returnType);
	}

}
