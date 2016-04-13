package twg2.parser.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import lombok.val;
import twg2.parser.codeParser.CommentStyle;
import twg2.parser.codeParser.codeStats.ParsedFileStats;
import twg2.parser.codeParser.extractors.CommentAndWhitespaceExtractor;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.TreePrint;

/**
 * @author TeamworkGuy2
 * @since 2015-5-27
 */
public class DocumentParserTest {

	//@Test
	public void testCommentWhitespaceParserDoubleQuotes(EnumSet<CommentStyle> style) throws IOException {
		String srcNameBase = "TestCommentWhitespaceParserDoubleQuotes";
		String src = "/*line one is a comment*/\n" +
				"// line two also\n" +
				"\t\n" +
				"line 3 // end of line comment\n" +
				"line 4 = \"string \\\"with\\\" embedded string\"; /* multiline\n" +
				"line 5 - comment */\n";
		val parserRes = CommentAndWhitespaceExtractor.buildCommentsAndWhitespaceTree(style, src, srcNameBase + "1");

		// TODO debugging
		TreePrint.printTree(parserRes.getDoc(), (f) -> f.getText(), System.out);
	}


	//@Test
	public void testCommentWhitespaceParserDoubleAndSingleQuotes(EnumSet<CommentStyle> style) throws IOException {
		String srcNameBase = "TestCommentWhitespaceParserDoubleAndSingleQuotes";
		String src = "/*line one is a comment*/\n" +
				"// line two also\n" +
				"\t\n" +
				"line 3 // end of line comment\n" +
				"line 4 = \"string \\\"with\\\" embedded string\"; /* multiline\n" +
				"line 5 - comment */\n" +
				"'last \\'line\\' string' ending";
		val parserRes = CommentAndWhitespaceExtractor.buildCommentsAndWhitespaceTree(style, src, srcNameBase + "1");

		// TODO debugging
		TreePrint.printTree(parserRes.getDoc(), (f) -> f.getText(), System.out);
	}


	public ParsedFileStats parseFileWhitespaceComments(EnumSet<CommentStyle> style) throws IOException {
		Path file = Paths.get("src/twg2/parser/documentParser/blocks/ParseBlocks.java");
		String src = StringJoin.join(Files.readAllLines(file), "\n");
		val parserRes = CommentAndWhitespaceExtractor.buildCommentsAndWhitespaceTree(style, src, file.toFile().getName());

		return CommentAndWhitespaceExtractor.calcCommentsAndWhitespaceLinesTreeStats(file.toString(), src.length(), parserRes.getLines(), parserRes.getDoc());
	}


	public static void main(String[] args) throws IOException {
		DocumentParserTest documentParseTest = new DocumentParserTest();

		val style = CommentStyle.multiAndSingleLine();

		// test 1
		//documentParseTest.testCommentWhitespaceParserDoubleQuotes(lang);
		// test 2
		documentParseTest.testCommentWhitespaceParserDoubleAndSingleQuotes(style);
		// real I/O test
		//documentParseTest.parseFileWhitespaceComments(lang);
	}

}
