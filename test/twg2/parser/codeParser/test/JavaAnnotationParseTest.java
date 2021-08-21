package twg2.parser.codeParser.test;

import static twg2.parser.test.utils.AnnotationAssert.assertAnnotation;
import static twg2.parser.test.utils.FieldAssert.assertField;
import static twg2.parser.test.utils.MethodAssert.assertParameter;
import static twg2.parser.test.utils.TypeAssert.ary;
import static twg2.parser.test.utils.TypeAssert.assertType;
import static twg2.parser.test.utils.TypeAssert.ls;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.ast.interm.method.ParameterSig;
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
 * @since 2021-06-27
 */
public class JavaAnnotationParseTest {
	private static List<String> srcLines = ls(
		"package ParserExamples.Samples;",
		"",
		"/** A simple class to test parsing.",
		" * @since 2016-1-15",
		" */",
		"@StringAnnotation(\"-AnnotationJava-\")",
		"public class AnnotationJava {",
		"",
		"    /** The modification count. */",
		"    @EmptyAnnotation()",
		"    @IntAnnotation(-1)",
		"    @BoolAnnotation(true)",
		"    @IdentifierAnnotation(Integer.TYPE)",
		"    @IdentifierAnnotation(Map.class)",
		"    @StringAnnotation(Name = \"\")",
		"    @MultiArgAnnotation(\"abc\", false , 1.23)",
		"    @MultiNamedArgAnnotation(num =1.23, flag=false ,value = \"abc\")",
		"    private int mod;",
		"",
		"    /** The name. */",
		"    @ImplementationDetail(String.class) @Nullable",
		"    private String _name;",
		"",
		"    /** The names. */",
		"    public List<String> Names;",
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
		"    @OperationContract",
		"    @WebInvoke(Method = \"POST\", UriTemplate = \"/AddName?name={name}\",",
		"        ResponseFormat = WebMessageFormat.Json)",
		"    @TransactionFlow(TransactionFlowOption.Allowed)",
		"    Result<List<String>> AddName(@NotNull @Source() String name, int zest = 0) {",
		"        content = block ? yes : no;",
		"    }",
		"",
		"    @WebInvoke(Method = \"PUT\", UriTemplate = \"/SetNames?names={names}\",",
		"        ResponseFormat = WebMessageFormat.Json)",
		"    List<Integer> SetNames(Constraints constraints, final String[] names) {",
		"        new { data = SetNames };",
		"        : new Dictionary<string, object> { content = value } : new { content = object };",
		"    }",
		"",
		"",
		"    private void CheckState(int guid, String... states) {",
		"        class Callback implements Consumer<String> {",
		"            @Override public void accept(@Default(\"--\") String state) {",
		"                System.out.println();",
		"            }",
		"        }",
		"        if(states != null && states.length > 0) Arrays.asList(states).forEach(new Callback());",
		"    }",
		"",
		"}"
	);

	private static List<String> src2Lines = ls(
		"package ParserExamples.Samples;",
		"",
		"/** First comment for annotation and comment parsing.",
		" * @since 2021-8-21",
		" */",
		"@PluralAnnotation(1, \"st\")",
		"/** Second comment.",
		" */",
		"@PluralAnnotation(2, \"nd\")",
		"public class JavaAnnotationComments {",
		"",
		"}"
	);


	@Test
	public void annotationParseTest() {
		CodeFileAndAst<JavaBlock> annotationJava = CodeFileAndAst.<JavaBlock>parse(CodeLanguageOptions.JAVA, "AnnotationJava.java", "ParserExamples.Samples.AnnotationJava", true, srcLines);
		List<CodeFileParsed.Simple<JavaBlock>> blocks = annotationJava.parsedBlocks;

		String fullClassName = annotationJava.fullClassName;
		Assert.assertEquals(2, blocks.size());
		ClassAst.SimpleImpl<JavaBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(5, clas.getFields().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());
		AnnotationAssert.assertAnnotation(clas.getSignature().getAnnotations(), 0, "StringAnnotation", new String[] { "value" }, "-AnnotationJava-");

		var fields = clas.getFields();
		assertField(fields, 0, fullClassName + ".mod", "int");
		Assert.assertEquals(ls(" The modification count. "), fields.get(0).getComments());
		var as = fields.get(0).getAnnotations();
		// annotation: EmptyAnnotation()
		assertAnnotation(as, 0, "EmptyAnnotation", new String[0], new String[0]);
		// annotation: IntAnnotation(-1)
		assertAnnotation(as, 1, "IntAnnotation", new String[] { "value" }, "-1");
		// annotation: BoolAnnotation(-1)
		assertAnnotation(as, 2, "BoolAnnotation", new String[] { "value" }, "true");
		// annotation: IdentifierAnnotation(Integer.TYPE)
		assertAnnotation(as, 3, "IdentifierAnnotation", new String[] { "value" }, "Integer.TYPE");
		// annotation: IdentifierAnnotation(Map.class)
		assertAnnotation(as, 4, "IdentifierAnnotation", new String[] { "value" }, "Map.class");
		// annotation: StringAnnotation(Name = "")
		assertAnnotation(as, 5, "StringAnnotation", new String[] { "Name" }, "");
		// annotation: MultiArgAnnotation("abc", false , 1.23)
		assertAnnotation(as, 6, "MultiArgAnnotation", new String[] { "arg1", "arg2", "arg3" }, "abc", "false", "1.23");
		// annotation: MultiNamedArgAnnotation(num =1.23, flag=false ,value = "abc")
		assertAnnotation(as, 7, "MultiNamedArgAnnotation", new String[] { "num", "flag", "value" }, "1.23", "false", "abc");

		assertField(fields, 1, fullClassName + "._name", "String");
		as = fields.get(1).getAnnotations();
		// annotation: @ImplementationDetail(String.class)
		assertAnnotation(as, 0, "ImplementationDetail", new String[] { "value" }, "String.class");
		// annotation: @Nullable
		assertAnnotation(as, 1, "Nullable", new String[0], new String[0]);

		assertField(fields, 2, fullClassName + ".Names", ary("List", ary("String")));
		assertField(fields, 3, fullClassName + ".accesses", "ZonedDateTime[]");
		assertField(fields, 4, fullClassName + ".TrackInfo", "TrackInfo");

		// methods:
		Assert.assertEquals(3, clas.getMethods().size());

		// AddName(...)
		MethodSigSimple m = clas.getMethods().get(0);
		Assert.assertEquals(fullClassName + ".AddName", NameUtil.joinFqName(m.fullName));
		Assert.assertEquals(ls(" Add name\n" +
		        "     * @param name the name\n" +
		        "     * @return the names\n" +
		        "     "), m.comments);
		List<ParameterSig> ps = m.paramSigs;
		Assert.assertEquals(2, ps.size());
		assertParameter(ps, 0, "name", "String", false, null, null, ls(new AnnotationSig("NotNull", NameUtil.splitFqName("NotNull"), Collections.emptyMap()), new AnnotationSig("Source", NameUtil.splitFqName("Source"), Collections.emptyMap())));
		assertParameter(ps, 1, "zest", "int", false, "0", null, null);
		// annotations:
		//{"name": "OperationContract", "arguments": {  } },
		assertAnnotation(m.annotations, 0, "OperationContract", new String[0], new String[0]);
		//{"name": "WebInvoke", "arguments": { "ResponseFormat": "WebMessageFormat.Json", "Method": "POST", "UriTemplate": "/AddName?name={name}" } },
		assertAnnotation(m.annotations, 1, "WebInvoke", new String[] { "ResponseFormat", "Method", "UriTemplate" }, new String[] { "WebMessageFormat.Json", "POST", "/AddName?name={name}" });
		//{"name": "TransactionFlow", "arguments": { "value": "TransactionFlowOption.Allowed" } }
		assertAnnotation(m.annotations, 2, "TransactionFlow", new String[] { "value" }, new String[] { "TransactionFlowOption.Allowed" });
		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "IList", "genericParameters": [ {"typeName": "String"}]}]}
		assertType(ary("Result", ary("List", ary("String"))), m.returnType);

		// SetNames(...)
		m = clas.getMethods().get(1);
		Assert.assertEquals(fullClassName + ".SetNames", NameUtil.joinFqName(m.fullName));
		ps = m.paramSigs;
		assertParameter(ps, 0, "constraints", "Constraints", false, null, null, null);
		assertParameter(ps, 1, "names", "String[]", false, null, ls(JavaKeyword.FINAL), null);
		// annotations:
		//{"name": "WebInvoke", "arguments": { "ResponseFormat": "WebMessageFormat.Json", "Method": "PUT", "UriTemplate": "/SetNames?names={names}" } },
		assertAnnotation(m.annotations, 0, "WebInvoke", new String[] { "ResponseFormat", "Method", "UriTemplate" }, new String[] { "WebMessageFormat.Json", "PUT", "/SetNames?names={names}" });

		// CheckState(...) (int guid, String... states)
		m = clas.getMethods().get(2);
		Assert.assertEquals(fullClassName + ".CheckState", NameUtil.joinFqName(m.fullName));
		ps = m.paramSigs;
		assertParameter(ps, 0, "guid", "int", false, null, null, null);
		assertParameter(ps, 1, "states", "String...", false, null, null, null);
	}


	@Test
	public void annotationCommentsParseTest() {
		CodeFileAndAst<JavaBlock> annotationJava = CodeFileAndAst.<JavaBlock>parse(CodeLanguageOptions.JAVA, "JavaAnnotationComments.java", "ParserExamples.Samples.JavaAnnotationComments", true, src2Lines);
		List<CodeFileParsed.Simple<JavaBlock>> blocks = annotationJava.parsedBlocks;

		String fullClassName = annotationJava.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<JavaBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(0, clas.getUsingStatements().size());
		Assert.assertEquals(0, clas.getFields().size());
		Assert.assertEquals(0, clas.getMethods().size());
		Assert.assertEquals(ls(" First comment for annotation and comment parsing.\n" +
				" * @since 2021-8-21\n ",
				" Second comment.\n "), clas.getSignature().getComments());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());
		AnnotationAssert.assertAnnotation(clas.getSignature().getAnnotations(), 0, "PluralAnnotation", new String[] { "arg1", "arg2" }, "1", "st");
		AnnotationAssert.assertAnnotation(clas.getSignature().getAnnotations(), 1, "PluralAnnotation", new String[] { "arg1", "arg2" }, "2", "nd");
	}

}
