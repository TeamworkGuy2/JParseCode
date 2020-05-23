package twg2.parser.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import twg2.collections.dataStructures.PairList;
import twg2.collections.primitiveCollections.CharArrayList;
import twg2.parser.condition.text.CharParserMatchable;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.language.CodeLanguage;
import twg2.parser.textFragment.TextTransformer;
import twg2.parser.tokenizers.CodeTokenizer;
import twg2.parser.workflow.CodeFileSrc;
import twg2.text.tokenizer.CharConditionPipe;
import twg2.text.tokenizer.CharConditions;
import twg2.text.tokenizer.CharParserFactory;
import twg2.text.tokenizer.CharParserMatchableFactory;
import twg2.text.tokenizer.Inclusion;
import twg2.text.tokenizer.StringConditions;

/**
 * @author TeamworkGuy2
 * @since 2017-10-21
 */
public class HtmlTemplateTest {

	@Test
	public void compileInputSinglelineTest() {
		String ln = "\n";
		String srcName = "compile-input-test-1";

		String src =
			"<head>" + ln +
			"	<body>" + ln +
			"	<stuff>things 1</stuff>" + ln +
			"	$importHtml(name=\"arc 12\")$" + ln +
			"	</body>" + ln +
			"</head>";

		String expect =
			"<head>" + ln +
			"	<body>" + ln +
			"	<stuff>things 1</stuff>" + ln +
			"	custom(arc 12)" + ln +
			"	</body>" + ln +
			"</head>";

		StringBuilder dst = new StringBuilder(src);
		compileTemplate(createHtmlParser(), src, dst, srcName, true, createHtmlVarsSingleline());

		Assert.assertEquals(expect, dst.toString());
	}


	@Test
	public void compileInputMultilineTest() {
		String ln = "\n";
		String srcName = "compile-input-test-2";

		String src =
			"<head>" + ln +
			"	<body>" + ln +
			"	<stuff>things 1</stuff>" + ln +
			"	$importHtml(name=\"arc 12\")$" + ln +
			"	</body>" + ln +
			"</head>";

		String expect =
			"<head>" + ln +
			"	<body>" + ln +
			"	<stuff>things 1</stuff>" + ln +
			"	custom: {" + ln +
			"		arc 12" + ln +
			"	}" + ln +
			"	</body>" + ln +
			"</head>";

		StringBuilder dst = new StringBuilder(src);
		compileTemplate(createHtmlParser(), src, dst, srcName, true, createHtmlVarsMultiline());

		Assert.assertEquals(expect, dst.toString());
	}


	private static final CodeTokenizer createHtmlParser() {
		CharParserFactory parser = createHtmlNamedVarParser("$", "importHtml", "$");
		var parsers = new PairList<CharParserFactory, TextTransformer<CodeTokenType>>();
		parsers.add(parser, CodeTokenizer.ofType(CodeTokenType.IDENTIFIER));

		return CodeTokenizer.createTokenizer((CodeLanguage)null, parsers);
	}


	private static final List<TemplateVar> createHtmlVarsSingleline() {
		return Arrays.asList(
			new TemplateVar("$", "$", "importHtml", (s) -> {
				return Arrays.asList("custom(" + s + ")");
			})
		);

	}


	private static final List<TemplateVar> createHtmlVarsMultiline() {
		return Arrays.asList(
			new TemplateVar("$", "$", "importHtml", (s) -> {
				return Arrays.asList("custom: {", "\t" + s, "}");
			})
		);

	}


	public static final CharParserFactory createHtmlNamedVarParser(String startMark, String templateName, String endMark) {
		CharParserFactory htmlParser = new CharParserMatchableFactory<CharParserMatchable>("HTML Named Var Template", false, Arrays.asList(CharConditionPipe.createPipeAllRequired("HTML Named Var Template", Arrays.asList(
			new StringConditions.Literal("start tag", new String[] { startMark + templateName + "(name=" }, Inclusion.INCLUDE),
			new CharConditions.Literal("attribute-string-start", CharArrayList.of('"'), Inclusion.INCLUDE),
			new CharConditions.EndNotPrecededBy("attribute-string-end", CharArrayList.of('"'), Inclusion.INCLUDE, CharArrayList.of('\\')),
			new StringConditions.Literal("end tag", new String[] { ")" + endMark }, Inclusion.INCLUDE)
		))));
		return htmlParser;
	}


	public static void compileTemplate(CodeTokenizer parser, String src, StringBuilder srcDst, String srcName, boolean preserveIndentation, List<TemplateVar> vars) {
		char[] srcChars = src.toCharArray();
		CodeFileSrc docParser = parser.tokenizeDocument(srcChars, 0, srcChars.length, srcName, null);

		var childs = docParser.astTree.getChildren();
		for(int i = childs.size() - 1; i > -1; i--) {
			var child = childs.get(i);
			var frag = child.getData().getToken();
			var text = child.getData().getText();
			var startI = frag.getOffsetStart();
			var endI = frag.getOffsetEnd();
			boolean foundOne = false;

			for(var var : vars) {
				if(var.isMatch(text)) {
					if(foundOne) {
						throw new IllegalStateException("found two vars both matching token " + i + ": " + frag + ", text: '" + text + "'");
					}

					var.insert(child.getData(), true, srcDst, startI, endI - startI);
					foundOne = true;
				}
			}
		}
	}

}
