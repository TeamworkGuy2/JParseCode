package twg2.parser.test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import twg2.parser.baseAst.AccessModifierEnum;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFileParsed;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.java.JavaBlock;
import twg2.parser.codeParser.java.JavaBlockParser;
import twg2.parser.language.CodeLanguage;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.main.ParseCodeFile;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2016-1-15
 */
public class JavaClassParseTest {
	private static String fileName = "SimpleJava.java";
	private static String fullClassName = "ParserExamples.Samples.SimpleJava";
	private static String simpleJavaCode =
		"package ParserExamples.Samples;\n" +
		"\n" +
		"/** A simple class to test parsing.\n" +
		" * @since 2016-1-15\n" +
		" */\n" +
		"public class SimpleJava {\n" +
		"\n" +
		"    /** The modification count. */\n" +
		"    @EmptyAnnotation()]\n" +
		"    @IntAnnotation(-1)]\n" +
		"    @BoolAnnotation(true)]\n" +
		"    @IdentifierAnnotation(Integer.TYPE)]\n" +
		"    @StringAnnotation(\"\")]\n" +
		"    @MultiArgAnnotation(\"abc\", false , 1.23)\n" +
		"    @MultiNamedArgAnnotation(num =1.23, flag=false ,value = \"abc\")\n" +
		"    private int mod;\n" +
		"\n" +
		"    /** The name. */\n" +
		"    private String _name;\n" +
		"\n" +
		"    /** The names. */\n" +
		"    public List<String> Names;\n" +
		"\n" +
		"    /** The number of names. */\n" +
		"    public int Count;\n" +
		"\n" +
		"    /** The access timestamps. */\n" +
		"    public ZonedDateTime[] accesses;\n" +
		"\n" +
        "    /** Add name\n" +
        "     * @param name the name\n" +
        "     * @return the names\n" +
        "     */\n" +
        "    @OperationContract\n" +
        "    @WebInvoke(Method = \"POST\", UriTemplate = \"/AddName?name={name}\",\n" +
        "        ResponseFormat = WebMessageFormat.Json)\n" +
        "    @TransactionFlow(TransactionFlowOption.Allowed)\n" +
        "    Result<List<String>> AddName(String name) {\n" +
        "        content of block;\n" +
        "    }\n" +
		"\n" +
		"}\n";
	private static CodeFileSrc<CodeLanguage> simpleAst = ParseCodeFile.parseCode(fileName, CodeLanguageOptions.JAVA, simpleJavaCode);
	private static List<CodeFileParsed.Simple<String, JavaBlock>> parsedBlocks = new ArrayList<>();

	static {
		System.out.println(simpleJavaCode);

		val blockDeclarations = CodeLanguageOptions.JAVA.getExtractor().extractClassFieldsAndMethodSignatures(simpleAst.getDoc());
		for(val block : blockDeclarations) {
			//CodeFileParsed.Simple<CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>, CompoundBlock> fileParsed = new CodeFileParsed.Simple<>(parsedFile, block.getValue(), block.getKey());
			CodeFileParsed.Simple<String, JavaBlock> fileParsed = new CodeFileParsed.Simple<>(fileName, block.getValue(), block.getKey());
			parsedBlocks.add(fileParsed);

			try {
				val ws = new WriteSettings(true, true, true, true);
				val sb = new StringBuilder();
				fileParsed.getParsedClass().toJson(sb, ws);
				System.out.println(sb.toString());
			} catch(IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}
	}


	@Parameter
	private CodeFileSrc<CodeLanguage> file = ParseCodeFile.parseFiles(Arrays.asList(Paths.get("rsc/java/ParserExamples/Models/TrackInfo.java")), FileReadUtil.threadLocalInst()).get(0);


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
		Assert.assertEquals(1, parsedBlocks.size());
		val clas = parsedBlocks.get(0).getParsedClass();
		Assert.assertEquals(5, clas.getFields().size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());

		FieldSig f = clas.getFields().get(0);
		Assert.assertEquals(fullClassName + ".mod", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("int", f.getFieldType().getTypeName());
		Assert.assertEquals(Arrays.asList(" The modification count. "), f.getComments());
		// annotations: EmptyAnnotation()
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 0, "EmptyAnnotation", new String[0], new String[0]);
		// annotations: IntAnnotation(-1)
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 1, "IntAnnotation", new String[] { "value" }, "-1");
		// annotations: BoolAnnotation(-1)
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 2, "BoolAnnotation", new String[] { "value" }, "true");
		// annotations: IdentifierAnnotation(Integer.TYPE)
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 3, "IdentifierAnnotation", new String[] { "value" }, "Integer.TYPE");
		// annotations: StringAnnotation("")
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 4, "StringAnnotation", new String[] { "value" }, "");
		// annotations: MultiArgAnnotation("abc", false , 1.23)
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 5, "MultiArgAnnotation", new String[] { "arg1", "arg2", "arg3" }, "abc", "false", "1.23");
		// annotations: MultiNamedArgAnnotation(num =1.23, flag=false ,value = "abc")
		ParseAnnotationTest.assertAnnotation(f.getAnnotations(), 6, "MultiNamedArgAnnotation", new String[] { "num", "flag", "value" }, "1.23", "false", "abc");

		f = clas.getFields().get(1);
		Assert.assertEquals(fullClassName + "._name", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("String", f.getFieldType().getTypeName());

		f = clas.getFields().get(2);
		Assert.assertEquals(fullClassName + ".Names", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("List", f.getFieldType().getTypeName());
		Assert.assertEquals("String", f.getFieldType().getParams().get(0).getTypeName());

		f = clas.getFields().get(3);
		Assert.assertEquals(fullClassName + ".Count", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("int", f.getFieldType().getTypeName());

		f = clas.getFields().get(4);
		Assert.assertEquals(fullClassName + ".accesses", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("ZonedDateTime", f.getFieldType().getTypeName());
		Assert.assertEquals(1, f.getFieldType().getArrayDimensions());

		Assert.assertEquals(1, clas.getMethods().size());
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
		ParseAnnotationTest.assertAnnotation(m.getAnnotations(), 0, "OperationContract", new String[0], new String[0]);

		//{"name": "WebInvoke", "arguments": { "ResponseFormat": "WebMessageFormat.Json", "Method": "POST", "UriTemplate": "/AddName?name={name}" } },
		ParseAnnotationTest.assertAnnotation(m.getAnnotations(), 1, "WebInvoke", new String[] { "ResponseFormat", "Method", "UriTemplate" }, new String[] { "WebMessageFormat.Json", "POST", "/AddName?name={name}" });

		//{"name": "TransactionFlow", "arguments": { "value": "TransactionFlowOption.Allowed" } }
		ParseAnnotationTest.assertAnnotation(m.getAnnotations(), 2, "TransactionFlow", new String[] { "value" }, new String[] { "TransactionFlowOption.Allowed" });

		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "IList", "genericParameters": [ {"typeName": "String"}]}]}
		Assert.assertEquals("Result", m.getReturnType().getTypeName());
		Assert.assertEquals("List", m.getReturnType().getParams().get(0).getTypeName());
		Assert.assertEquals("String", m.getReturnType().getParams().get(0).getParams().get(0).getTypeName());
	}

}
