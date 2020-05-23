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
import twg2.parser.codeParser.java.JavaBlock;
import twg2.parser.codeParser.java.JavaBlockParser;
import twg2.parser.codeParser.java.JavaKeyword;
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
		"    @OperationContract",
		"    @WebInvoke(Method = \"POST\", UriTemplate = \"/AddName?name={name}\",",
		"        ResponseFormat = WebMessageFormat.Json)",
		"    @TransactionFlow(TransactionFlowOption.Allowed)",
		"    Result<List<String>> AddName(@NotNull String name, int zest = 0) {",
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
		"            @Override public void accept(String state) {",
		"                System.out.println();",
		"            }",
		"        }",
		"        if(states != null && states.length > 0) Arrays.asList(states).forEach(new Callback());",
		"    }",
		"",
		"}"
	);

	@Parameter
	private CodeFileAndAst<JavaBlock> simpleJava;

	@Parameter
	private CodeFileSrc file;


	public JavaClassParseTest() throws IOException {
		simpleJava = CodeFileAndAst.<JavaBlock>parse(CodeLanguageOptions.JAVA, "SimpleJava.java", "ParserExamples.Samples.SimpleJava", true, srcLines);
		file = ParseCodeFile.parseFiles(ls(Paths.get("rsc/java/ParserExamples/Models/TrackInfo.java")), FileReadUtil.threadLocalInst(), null).get(0);
	}


	@Test
	public void parseBlocksTest() {
		SimpleTree<CodeToken> tree = file.astTree;
		List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<JavaBlock>>> blocks = new JavaBlockParser().extractClassFieldsAndMethodSignatures(tree);

		Assert.assertEquals(1, blocks.size());

		ClassAst.SimpleImpl<JavaBlock> trackInfoBlock = blocks.get(0).getValue();
		Assert.assertEquals(JavaBlock.CLASS, trackInfoBlock.getBlockType());
		Assert.assertEquals("TrackInfo", trackInfoBlock.getSignature().getSimpleName());

		Assert.assertEquals(ls("Serializable", "Comparable<TrackInfo>"), trackInfoBlock.getSignature().getExtendImplementSimpleNames());
	}


	@Test
	public void simpleJavaParseTest() {
		List<CodeFileParsed.Simple<JavaBlock>> blocks = simpleJava.parsedBlocks;
		String fullClassName = simpleJava.fullClassName;
		Assert.assertEquals(2, blocks.size());
		ClassAst.SimpleImpl<JavaBlock> clas = blocks.get(0).parsedClass;
		Assert.assertEquals(6, clas.getFields().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());
		AnnotationAssert.assertAnnotation(clas.getSignature().getAnnotations(), 0, "StringAnnotation", new String[] { "value" }, "-SimpleJava-");

		List<FieldSig> fields = clas.getFields();
		assertField(fields, 0, fullClassName + ".mod", "int");
		Assert.assertEquals(ls(" The modification count. "), fields.get(0).getComments());
		List<AnnotationSig> as = fields.get(0).getAnnotations();
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
		List<ParameterSig> ps = m.paramSigs;
		Assert.assertEquals(2, ps.size());
		assertParameter(ps, 0, "name", "String", null, null, ls(new AnnotationSig("NotNull", NameUtil.splitFqName("NotNull"), Collections.emptyMap())));
		assertParameter(ps, 1, "zest", "int", "0", null, null);
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
		assertParameter(ps, 0, "constraints", "Constraints", null, null, null);
		assertParameter(ps, 1, "names", "String[]", null, ls(JavaKeyword.FINAL), null);

		// CheckState(...) (int guid, String... states)
		m = clas.getMethods().get(2);
		Assert.assertEquals(fullClassName + ".CheckState", NameUtil.joinFqName(m.fullName));
		ps = m.paramSigs;
		assertParameter(ps, 0, "guid", "int", null, null, null);
		assertParameter(ps, 1, "states", "String...", null, null, null);
	}

}
