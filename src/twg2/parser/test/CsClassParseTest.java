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

import twg2.parser.baseAst.AccessModifierEnum;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFileParsed;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.csharp.CsBlockParser;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.parser.intermAst.method.IntermParameterSig;
import twg2.parser.main.ParseCodeFile;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class CsClassParseTest {
	private static String simpleCsName = "SimpleCs.cs";
	private static String simpleCsCode =
		"namespace ParserExamples.Samples {\n" +
		"\n" +
		"  /// <summary>\n" +
		"  /// A simple class to test parsing.\n" +
		"  /// </summary>\n" +
		"  public class SimpleCs {\n" +
		"\n" +
		"    /// <value>The names.</value>\n" +
		"    public IList<string> Names { get; set; }\n" +
		"\n" +
		"    /// <value>The number of names.</value>\n" +
		"    public int Count { get; set; }\n" +
		"\n" +
        "    /// <summary>Add name</summary>\n" +
        "    /// <param name=\"name\">the name</param>\n" +
        "    /// <returns>the names</returns>\n" +
        "    [OperationContract]\n" +
        "    [WebInvoke(Method = \"POST\", UriTemplate = \"/AddName?name={name}\",\n" +
        "        ResponseFormat = WebMessageFormat.Json)]\n" +
        "    [TransactionFlow(TransactionFlowOption.Allowed)]\n" +
        "    Result<IList<String>> AddName(string name) {\n" +
        "        content of block;\n" +
        "    }\n" +
        "\n" +
        "  }\n" +
		"\n" +
		"}\n";
	private static CodeFileSrc<CodeLanguage> simpleCsAst = ParseCodeFile.parseCode(simpleCsName, CodeLanguageOptions.C_SHARP, simpleCsCode);
	private static List<CodeFileParsed.Simple<String, CsBlock>> simpleCsBlocks = new ArrayList<>();

	static {
		System.out.println(simpleCsCode);

		val blockDeclarations = CodeLanguageOptions.C_SHARP.getExtractor().extractClassFieldsAndMethodSignatures(simpleCsAst.getDoc());
		for(val block : blockDeclarations) {
			//CodeFileParsed.Simple<CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>, CompoundBlock> fileParsed = new CodeFileParsed.Simple<>(parsedFile, block.getValue(), block.getKey());
			CodeFileParsed.Simple<String, CsBlock> fileParsed = new CodeFileParsed.Simple<>(simpleCsName, block.getValue(), block.getKey());
			simpleCsBlocks.add(fileParsed);

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
	private CodeFileSrc<CodeLanguage> file = ParseCodeFile.parseFiles(Arrays.asList(Paths.get("rsc/csharp/ParserExamples/Models/TrackInfo.cs"))).get(0);


	public CsClassParseTest() throws IOException {
	}


	@Test
	public void parseBlocksTest() {
		val tree = file.getDoc();
		val blocks = new CsBlockParser().extractClassFieldsAndMethodSignatures(tree);

		Assert.assertEquals(1, blocks.size());

		val trackInfoBlock = blocks.get(0).getValue();
		Assert.assertEquals(CsBlock.CLASS, trackInfoBlock.getBlockType());
		Assert.assertEquals("TrackInfo", trackInfoBlock.getSignature().getSimpleName());
	}


	@Test
	public void simpleCsParseTest() {
		Assert.assertEquals(1, simpleCsBlocks.size());
		val csClass = simpleCsBlocks.get(0).getParsedClass();
		Assert.assertEquals(2, csClass.getFields().size());

		Assert.assertEquals("ParserExamples.Samples.SimpleCs", NameUtil.joinFqName(csClass.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, csClass.getSignature().getAccessModifier());
		Assert.assertEquals("class", csClass.getSignature().getDeclarationType());

		IntermFieldSig f = csClass.getFields().get(0);
		Assert.assertEquals("ParserExamples.Samples.SimpleCs.Names", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("IList", f.getFieldType().getTypeName());
		Assert.assertEquals("string", f.getFieldType().getGenericParams().get(0).getTypeName());

		f = csClass.getFields().get(1);
		Assert.assertEquals("ParserExamples.Samples.SimpleCs.Count", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("int", f.getFieldType().getTypeName());

		Assert.assertEquals(1, csClass.getMethods().size());
		IntermMethodSig.SimpleImpl m = csClass.getMethods().get(0);
		Assert.assertEquals("ParserExamples.Samples.SimpleCs.AddName", NameUtil.joinFqName(m.getFullName()));
		IntermParameterSig p = m.getParamSigs().get(0);
		Assert.assertEquals("name", p.getName());
		Assert.assertEquals("string", p.getTypeSimpleName());
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
		Assert.assertEquals("IList", m.getReturnType().getGenericParams().get(0).getTypeName());
		Assert.assertEquals("String", m.getReturnType().getGenericParams().get(0).getGenericParams().get(0).getTypeName());
	}

}
