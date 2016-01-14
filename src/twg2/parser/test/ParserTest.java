package twg2.parser.test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import lombok.val;

import org.junit.Assert;
import org.junit.Test;

import twg2.collections.primitiveCollections.IntArrayList;
import twg2.parser.baseAst.AccessModifierEnum;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.CodeFileParsed;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.documentParser.block.IntermediateBlock;
import twg2.parser.documentParser.block.ParseBlocks;
import twg2.parser.documentParser.block.TextBlock;
import twg2.parser.documentParser.block.TextOffsetBlock;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.parser.intermAst.method.IntermParameterSig;
import twg2.parser.main.ParseCodeFile;
import twg2.parser.output.WriteSettings;
import checks.CheckTask;

/**
 * @author TeamworkGuy2
 * @since 2014-9-1
 */
public class ParserTest {
	private static String simpleCsName = "SimpleCs.cs";
	private static String simpleCsCode =
		"namespace ParserExamples.Samples {\n" +
		"\n" +
		"    /// <summary>\n" +
		"    /// A simple class to test parsing.\n" +
		"    /// </summary>\n" +
		"    public class SimpleCs {\n" +
		"\n" +
		"    /// <value>The names.</value>\n" +
		"    public IList<string> Names { get; set; }\n" +
		"\n" +
		"    /// <value>The number of names.</value>\n" +
		"    public int Count { get; set }\n" +
		"\n" +
        "    /// <summary>\n" +
        "    /// Add name\n" +
        "    /// </summary>\n" +
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


	@Test
	public void simpleCsParseTest() {
		Assert.assertEquals(1, simpleCsBlocks.size());
		val csClass = simpleCsBlocks.get(0).getParsedClass();
		Assert.assertEquals(2, csClass.getFields().size());

		Assert.assertEquals("SimpleCs", NameUtil.joinFqName(csClass.getSignature().getFullyQualifyingName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, csClass.getSignature().getAccessModifier());
		Assert.assertEquals("class", csClass.getSignature().getDeclarationType());

		IntermFieldSig f = csClass.getFields().get(0);
		Assert.assertEquals("SimpleCs.Names", NameUtil.joinFqName(f.getFullyQualifyingName()));
		Assert.assertEquals("IList", f.getFieldType().getTypeName());
		Assert.assertEquals("string", f.getFieldType().getGenericParams().get(0).getTypeName());

		f = csClass.getFields().get(1);
		Assert.assertEquals("SimpleCs.Count", NameUtil.joinFqName(f.getFullyQualifyingName()));
		Assert.assertEquals("int", f.getFieldType().getTypeName());

		Assert.assertEquals(1, csClass.getMethods().size());
		IntermMethodSig.SimpleImpl m = csClass.getMethods().get(0);
		Assert.assertEquals("SimpleCs.AddName", NameUtil.joinFqName(m.getFullyQualifyingName()));
		IntermParameterSig p = m.getParamSigs().get(0);
		Assert.assertEquals("name", p.getName());
		Assert.assertEquals("string", p.getTypeSimpleName());
		//annotations:
		//{"name": "OperationContract", "arguments": {  } },
		AnnotationSig sig = m.getAnnotations().get(0);
		Assert.assertEquals("OperationContract", NameUtil.joinFqName(sig.getFullyQualifyingName()));
		Assert.assertEquals(0, sig.getArguments().size());

		//{"name": "WebInvoke", "arguments": { "ResponseFormat": "WebMessageFormat.Json", "Method": "POST", "UriTemplate": "/AddName?name={name}" } },
		sig = m.getAnnotations().get(1);
		Assert.assertEquals("WebInvoke", NameUtil.joinFqName(sig.getFullyQualifyingName()));
		Assert.assertTrue(sig.getArguments().containsKey("ResponseFormat"));
		Assert.assertEquals("WebMessageFormat.Json", sig.getArguments().get("ResponseFormat"));
		Assert.assertTrue(sig.getArguments().containsKey("Method"));
		Assert.assertEquals("POST", sig.getArguments().get("Method"));
		Assert.assertTrue(sig.getArguments().containsKey("UriTemplate"));
		Assert.assertEquals("/AddName?name={name}", sig.getArguments().get("UriTemplate"));

		//{"name": "TransactionFlow", "arguments": { "value": "TransactionFlowOption.Allowed" } }
		sig = m.getAnnotations().get(2);
		Assert.assertEquals("TransactionFlow", NameUtil.joinFqName(sig.getFullyQualifyingName()));
		Assert.assertTrue(sig.getArguments().containsKey("value"));
		Assert.assertEquals("TransactionFlowOption.Allowed", sig.getArguments().get("value"));

		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "IList", "genericParameters": [ {"typeName": "String"}]}]}
		Assert.assertEquals("Result", m.getReturnType().getTypeName());
		Assert.assertEquals("IList", m.getReturnType().getGenericParams().get(0).getTypeName());
		Assert.assertEquals("String", m.getReturnType().getGenericParams().get(0).getGenericParams().get(0).getTypeName());
	}


	@Test
	public void parseBlocksTest() {
		/*String str =
				"bla\n" +
				"bla\n" +
				"block %{\n" +
				"	stuff in block\n" +
				"		%{ adjacent block %}\n" +
				"		%{ nested block %{ double nesting %} nested after %}\n" +
				"	stuff after nested block\n" +
				"	%{ secondary: %{ secondary1 %}, %{ secondary2 %}, %{ secondary3 %} %}\n" +
				"%}\n" +
				"more text\n" +
				"%{ another block %}\n" +
				"xyz";
		*/
		String str = "function test(t) {\n" +
			"  t = Number.parseInt(t, 10);\n" +
			"  {\n" +
			"    var value = 3 * 5, neg = -1, pos = 1;\n" +
			"    t = t < value ? t * neg : value;\n" +
			"    t = Math.max(t, pos);\n" +
			"  }\n" +
			"  return t;\n" +
			"}";
		String[] expect = {
			"function test(t) ",

			"\n  " +
			"t = Number.parseInt(t, 10);\n  ",

			"\n" +
			"    var value = 3 * 5, neg = -1, pos = 1;\n" +
			"    t = t < value ? t * neg : value;\n" +
			"    t = Math.max(t, pos);\n" +
			"  ",

			"\n" +
			"  return t;\n",

			""
		};

		IntArrayList blockIndices = new IntArrayList();
		StringBuilder strB = new StringBuilder(str);
		IntArrayList ints = new IntArrayList();
		BiFunction<Integer, Integer, Integer> minGreater = (a, b) -> a < 0 ? (b < 0 ? a : b) : Math.min(a, b);
		for(int off = 0, i = minGreater.apply(str.indexOf('{', off + 1), str.indexOf('}', off + 1)), size = str.length();
				i < size && i > -1;
				off = i, i = minGreater.apply(str.indexOf('{', off + 1), str.indexOf('}', off + 1))) {
			ints.add(i);
		}
		System.out.println("indices {}: " + ints);

		TextBlock blocks = ParseBlocks.splitIntoBlocks(strB, '{', '}', blockIndices);
		List<TextBlock> blockList = new ArrayList<>();

		System.out.println("parse string (length: " + str.length() + "):\n" + str + "\n\n");
		System.out.println("\n\n==block==");
		((IntermediateBlock)blocks).forEachLeaf((txtBlock, idx) -> {
			System.out.println("====\n" + txtBlock.toString(str).trim() + "\n====\n");
			blockList.add(txtBlock);
		});

		CheckTask.assertTests(blockList.toArray(new TextOffsetBlock[blockList.size()]), expect, (block) -> ((TextOffsetBlock)block).toString(str));
	}


	public static void main(String[] args) throws IOException {
		ParserTest parserTest = new ParserTest();
		new IdentifierParserTest().identifierWithGenericTypeParse();
		new IdentifierParserTest().compoundIdentifierParse();
		//parserTest.parseBlocksTest();
		//parserTest.lineBufferTest();

		/*
		stringToCaseTest();
		readCharTypeTest();
		parseDateTimeTest();
		readJsonLiteArrayTest();
		readJsonLiteNumberTest();
		lineBufferTest();
		*/
		System.out.println("float min_normal: " + Float.MIN_NORMAL + ", min_value: " + Float.MIN_VALUE);
		System.out.println("double min_normal: " + Double.MIN_NORMAL + ", min_value: " + Double.MIN_VALUE);
		System.out.println();

		Random rand = new Random();
		int size = 200;
		int similarCount = 0;
		for(int i = 0; i < size; i++) {
			long randLong = rand.nextLong();
			double d = Double.longBitsToDouble(randLong);
			String numStr = Double.toString(d);
			double dParsed = Double.parseDouble(numStr);
			float fParsed = (float)dParsed; //Float.parseFloat(numStr);
			double diff = dParsed - fParsed;
			System.out.println(dParsed + "\t " + fParsed + " :\t " + diff + " | " + (diff > Float.MIN_NORMAL));
			if(diff < Float.MIN_NORMAL) {
				similarCount++;
			}
		}
		System.out.println("similar " + similarCount + "/" + size);
	}

}
