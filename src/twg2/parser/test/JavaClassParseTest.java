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

import twg2.io.files.FileReadUtil;
import twg2.parser.baseAst.AccessModifierEnum;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFileParsed;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.codeParser.java.JavaBlock;
import twg2.parser.codeParser.java.JavaBlockParser;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.parser.intermAst.method.IntermParameterSig;
import twg2.parser.main.ParseCodeFile;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2016-1-15
 */
public class JavaClassParseTest {
	private static String simpleJavaName = "SimpleJava.java";
	private static String simpleJavaCode =
		"package ParserExamples.Samples;\n" +
		"\n" +
		"/** A simple class to test parsing.\n" +
		" * @since 2016-1-15\n" +
		" */\n" +
		"public class SimpleJava {\n" +
		"\n" +
		"    /** The names. */\n" +
		"    public List<String> Names;\n" +
		"\n" +
		"    /** The number of names. */\n" +
		"    public int Count;\n" +
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
	private static CodeFileSrc<CodeLanguage> simpleJavaAst = ParseCodeFile.parseCode(simpleJavaName, CodeLanguageOptions.JAVA, simpleJavaCode);
	private static List<CodeFileParsed.Simple<String, JavaBlock>> simpleJavaBlocks = new ArrayList<>();

	static {
		System.out.println(simpleJavaCode);

		val blockDeclarations = CodeLanguageOptions.JAVA.getExtractor().extractClassFieldsAndMethodSignatures(simpleJavaAst.getDoc());
		for(val block : blockDeclarations) {
			//CodeFileParsed.Simple<CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>, CompoundBlock> fileParsed = new CodeFileParsed.Simple<>(parsedFile, block.getValue(), block.getKey());
			CodeFileParsed.Simple<String, JavaBlock> fileParsed = new CodeFileParsed.Simple<>(simpleJavaName, block.getValue(), block.getKey());
			simpleJavaBlocks.add(fileParsed);

			try {
				val ws = new WriteSettings(true, true, true);
				val sb = new StringBuilder();
				fileParsed.getParsedClass().toJson(sb, ws);
				System.out.println(sb.toString());
			} catch(IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}
	}


	@Parameter
	private CodeFileSrc<CodeLanguage> file = ParseCodeFile.parseFiles(Arrays.asList(Paths.get("rsc/java/ParserExamples/Models/TrackInfo.java")), FileReadUtil.defaultInst()).get(0);


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
		Assert.assertEquals(1, simpleJavaBlocks.size());
		val csClass = simpleJavaBlocks.get(0).getParsedClass();
		Assert.assertEquals(2, csClass.getFields().size());

		Assert.assertEquals("ParserExamples.Samples.SimpleJava", NameUtil.joinFqName(csClass.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, csClass.getSignature().getAccessModifier());
		Assert.assertEquals("class", csClass.getSignature().getDeclarationType());

		IntermFieldSig f = csClass.getFields().get(0);
		Assert.assertEquals("ParserExamples.Samples.SimpleJava.Names", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("List", f.getFieldType().getTypeName());
		Assert.assertEquals("String", f.getFieldType().getParams().get(0).getTypeName());

		f = csClass.getFields().get(1);
		Assert.assertEquals("ParserExamples.Samples.SimpleJava.Count", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("int", f.getFieldType().getTypeName());

		Assert.assertEquals(1, csClass.getMethods().size());
		IntermMethodSig.SimpleImpl m = csClass.getMethods().get(0);
		Assert.assertEquals("ParserExamples.Samples.SimpleJava.AddName", NameUtil.joinFqName(m.getFullName()));
		IntermParameterSig p = m.getParamSigs().get(0);
		Assert.assertEquals("name", p.getName());
		Assert.assertEquals("String", p.getTypeSimpleName());
		//annotations:
		//{"name": "OperationContract", "arguments": {  } },
		AnnotationSig sig = m.getAnnotations().get(0);
		Assert.assertEquals("OperationContract", NameUtil.joinFqName(sig.getFullName()));
		Assert.assertEquals(0, sig.getArguments().size());

		//{"name": "WebInvoke", "arguments": { "ResponseFormat": "WebMessageFormat.Json", "Method": "POST", "UriTemplate": "/AddName?name={name}" } },
		sig = m.getAnnotations().get(1);
		Assert.assertEquals("WebInvoke", NameUtil.joinFqName(sig.getFullName()));
		Assert.assertTrue(sig.getArguments().containsKey("ResponseFormat"));
		Assert.assertEquals("WebMessageFormat.Json", sig.getArguments().get("ResponseFormat"));
		Assert.assertTrue(sig.getArguments().containsKey("Method"));
		Assert.assertEquals("POST", sig.getArguments().get("Method"));
		Assert.assertTrue(sig.getArguments().containsKey("UriTemplate"));
		Assert.assertEquals("/AddName?name={name}", sig.getArguments().get("UriTemplate"));

		//{"name": "TransactionFlow", "arguments": { "value": "TransactionFlowOption.Allowed" } }
		sig = m.getAnnotations().get(2);
		Assert.assertEquals("TransactionFlow", NameUtil.joinFqName(sig.getFullName()));
		Assert.assertTrue(sig.getArguments().containsKey("value"));
		Assert.assertEquals("TransactionFlowOption.Allowed", sig.getArguments().get("value"));

		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "IList", "genericParameters": [ {"typeName": "String"}]}]}
		Assert.assertEquals("Result", m.getReturnType().getTypeName());
		Assert.assertEquals("List", m.getReturnType().getParams().get(0).getTypeName());
		Assert.assertEquals("String", m.getReturnType().getParams().get(0).getParams().get(0).getTypeName());
	}

}
