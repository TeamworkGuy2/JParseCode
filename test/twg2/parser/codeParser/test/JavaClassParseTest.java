package twg2.parser.codeParser.test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import lombok.val;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.method.MethodSig;
import twg2.ast.interm.method.ParameterSig;
import twg2.io.files.FileReadUtil;
import twg2.parser.codeParser.AccessModifierEnum;
import twg2.parser.codeParser.java.JavaBlock;
import twg2.parser.codeParser.java.JavaBlockParser;
import twg2.parser.codeParser.java.JavaKeyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguage;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.main.ParseCodeFile;
import twg2.parser.test.utils.CodeFileAndAst;
import twg2.parser.workflow.CodeFileSrc;
import static twg2.parser.test.utils.ParseAnnotationAssert.*;

/**
 * @author TeamworkGuy2
 * @since 2016-1-15
 */
public class JavaClassParseTest {
	private static List<String> srcLines = Arrays.asList(
		"package ParserExamples.Samples;",
		"",
		"/** A simple class to test parsing.",
		" * @since 2016-1-15",
		" */",
		"public class SimpleJava {",
		"",
		"    /** The modification count. */",
		"    @EmptyAnnotation()]",
		"    @IntAnnotation(-1)]",
		"    @BoolAnnotation(true)]",
		"    @IdentifierAnnotation(Integer.TYPE)]",
		"    @StringAnnotation(Name = \"\")]",
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
		"    /** Add name",
		"     * @param name the name",
		"     * @return the names",
		"     */",
		"    @OperationContract",
		"    @WebInvoke(Method = \"POST\", UriTemplate = \"/AddName?name={name}\",",
		"        ResponseFormat = WebMessageFormat.Json)",
		"    @TransactionFlow(TransactionFlowOption.Allowed)",
		"    Result<List<String>> AddName(String name) {",
		"        content of block;",
		"    }",
		"",
		"    @WebInvoke(Method = \"PUT\", UriTemplate = \"/SetNames?names={names}\",",
		"        ResponseFormat = WebMessageFormat.Json)",
		"    List<Integer> SetNames(final String[] names) {",
		"        content of SetNames;",
		"    }",
		"",
		"}"
	);

	@Parameter
	private CodeFileAndAst<JavaBlock> simpleJava = CodeFileAndAst.<JavaBlock>parse(CodeLanguageOptions.JAVA, "SimpleJava.java", "ParserExamples.Samples.SimpleJava", true, srcLines);

	@Parameter
	private CodeFileSrc<CodeLanguage> file = ParseCodeFile.parseFiles(Arrays.asList(Paths.get("rsc/java/ParserExamples/Models/TrackInfo.java")), FileReadUtil.threadLocalInst(), null).get(0);


	public JavaClassParseTest() throws IOException {
	}


	@Test
	public void parseBlocksTest() {
		val tree = file.getDoc();
		val blocks = new JavaBlockParser().extractClassFieldsAndMethodSignatures(tree);

		Assert.assertEquals(1, blocks.size());

		val trackInfoBlock = blocks.get(0).getValue();
		Assert.assertEquals(JavaBlock.CLASS, trackInfoBlock.getBlockType());
		Assert.assertEquals("TrackInfo", trackInfoBlock.getSignature().getSimpleName());
	}


	@Test
	public void simpleJavaParseTest() {
		val blocks = simpleJava.parsedBlocks;
		val fullClassName = simpleJava.fullClassName;
		Assert.assertEquals(1, blocks.size());
		val clas = blocks.get(0).getParsedClass();
		val fields = clas.getFields();
		Assert.assertEquals(5, fields.size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());

		FieldSig f = fields.get(0);
		Assert.assertEquals(fullClassName + ".mod", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("int", f.getFieldType().getTypeName());
		Assert.assertEquals(Arrays.asList(" The modification count. "), f.getComments());
		// annotations: EmptyAnnotation()
		assertAnnotation(f.getAnnotations(), 0, "EmptyAnnotation", new String[0], new String[0]);
		// annotations: IntAnnotation(-1)
		assertAnnotation(f.getAnnotations(), 1, "IntAnnotation", new String[] { "value" }, "-1");
		// annotations: BoolAnnotation(-1)
		assertAnnotation(f.getAnnotations(), 2, "BoolAnnotation", new String[] { "value" }, "true");
		// annotations: IdentifierAnnotation(Integer.TYPE)
		assertAnnotation(f.getAnnotations(), 3, "IdentifierAnnotation", new String[] { "value" }, "Integer.TYPE");
		// annotations: StringAnnotation(Name = "")
		assertAnnotation(f.getAnnotations(), 4, "StringAnnotation", new String[] { "Name" }, "");
		// annotations: MultiArgAnnotation("abc", false , 1.23)
		assertAnnotation(f.getAnnotations(), 5, "MultiArgAnnotation", new String[] { "arg1", "arg2", "arg3" }, "abc", "false", "1.23");
		// annotations: MultiNamedArgAnnotation(num =1.23, flag=false ,value = "abc")
		assertAnnotation(f.getAnnotations(), 6, "MultiNamedArgAnnotation", new String[] { "num", "flag", "value" }, "1.23", "false", "abc");

		f = fields.get(1);
		Assert.assertEquals(fullClassName + "._name", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("String", f.getFieldType().getTypeName());

		f = fields.get(2);
		Assert.assertEquals(fullClassName + ".Names", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("List", f.getFieldType().getTypeName());
		Assert.assertEquals("String", f.getFieldType().getParams().get(0).getTypeName());

		f = fields.get(3);
		Assert.assertEquals(fullClassName + ".Count", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("int", f.getFieldType().getTypeName());

		f = fields.get(4);
		Assert.assertEquals(fullClassName + ".accesses", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("ZonedDateTime", f.getFieldType().getTypeName());
		Assert.assertEquals(1, f.getFieldType().getArrayDimensions());

		// methods:
		Assert.assertEquals(2, clas.getMethods().size());

		// AddName()
		MethodSig.SimpleImpl m = clas.getMethods().get(0);
		Assert.assertEquals(fullClassName + ".AddName", NameUtil.joinFqName(m.getFullName()));
		ParameterSig p = m.getParamSigs().get(0);
		Assert.assertEquals("name", p.getName());
		Assert.assertEquals("String", p.getTypeSimpleName());
		Assert.assertEquals(Arrays.asList(" Add name\n" +
		        "     * @param name the name\n" +
		        "     * @return the names\n" +
		        "     "), m.getComments());
		// annotations:
		//{"name": "OperationContract", "arguments": {  } },
		assertAnnotation(m.getAnnotations(), 0, "OperationContract", new String[0], new String[0]);

		//{"name": "WebInvoke", "arguments": { "ResponseFormat": "WebMessageFormat.Json", "Method": "POST", "UriTemplate": "/AddName?name={name}" } },
		assertAnnotation(m.getAnnotations(), 1, "WebInvoke", new String[] { "ResponseFormat", "Method", "UriTemplate" }, new String[] { "WebMessageFormat.Json", "POST", "/AddName?name={name}" });

		//{"name": "TransactionFlow", "arguments": { "value": "TransactionFlowOption.Allowed" } }
		assertAnnotation(m.getAnnotations(), 2, "TransactionFlow", new String[] { "value" }, new String[] { "TransactionFlowOption.Allowed" });

		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "IList", "genericParameters": [ {"typeName": "String"}]}]}
		Assert.assertEquals("Result", m.getReturnType().getTypeName());
		Assert.assertEquals("List", m.getReturnType().getParams().get(0).getTypeName());
		Assert.assertEquals("String", m.getReturnType().getParams().get(0).getParams().get(0).getTypeName());

		// SetNames()
		m = clas.getMethods().get(1);
		Assert.assertEquals(fullClassName + ".SetNames", NameUtil.joinFqName(m.getFullName()));
		p = m.getParamSigs().get(0);
		Assert.assertEquals("names", p.getName());
		Assert.assertEquals("String[]", p.getTypeSimpleName());
		Assert.assertEquals(Arrays.asList(JavaKeyword.FINAL), p.getParameterModifiers());
	}

}
