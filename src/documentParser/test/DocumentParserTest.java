package documentParser.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import lombok.val;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.TreePrint;
import codeParser.CommentStyle;
import codeParser.ParseCommentsAndWhitespace;
import codeParser.codeStats.ParsedFileStats;

/**
 * @author TeamworkGuy2
 * @since 2015-5-27
 */
public class DocumentParserTest {

	//@Test
	public void testCommentWhitespaceParserDoubleQuotes(EnumSet<CommentStyle> style) throws IOException {
		String src = "/*line one is a comment*/\n" +
				"// line two also\n" +
				"\t\n" +
				"line 3 // end of line comment\n" +
				"line 4 = \"string \\\"with\\\" embedded string\"; /* multiline\n" +
				"line 5 - comment */\n";
		val parserRes = ParseCommentsAndWhitespace.buildCommentsAndWhitespaceTree(style, src);

		// TODO debugging
		TreePrint.printTree(parserRes.getDoc(), (f) -> f.getTextFragment().toString(src), System.out);
	}


	//@Test
	public void testCommentWhitespaceParserDoubleAndSingleQuotes(EnumSet<CommentStyle> style) throws IOException {
		String src = "/*line one is a comment*/\n" +
				"// line two also\n" +
				"\t\n" +
				"line 3 // end of line comment\n" +
				"line 4 = \"string \\\"with\\\" embedded string\"; /* multiline\n" +
				"line 5 - comment */\n" +
				"'last \\'line\\' string' ending";
		val parserRes = ParseCommentsAndWhitespace.buildCommentsAndWhitespaceTree(style, src);

		// TODO debugging
		TreePrint.printTree(parserRes.getDoc(), (f) -> f.getTextFragment().toString(src), System.out);
	}


	public ParsedFileStats parseFileWhitespaceComments(EnumSet<CommentStyle> style) throws IOException {
		Path file = Paths.get("src/documentParser/blocks/ParseBlocks.java");
		String src = StringJoin.join(Files.readAllLines(file), "\n");
		val parserRes = ParseCommentsAndWhitespace.buildCommentsAndWhitespaceTree(style, src);

		return ParseCommentsAndWhitespace.calcCommentsAndWhitespaceLinesTreeStats(file.toString(), src.length(), parserRes.getLines(), parserRes.getDoc());
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
