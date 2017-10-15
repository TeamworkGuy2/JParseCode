package twg2.parser.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import twg2.parser.codeParser.CommentStyle;
import twg2.parser.codeParser.codeStats.ParsedFileStats;
import twg2.parser.codeParser.extractors.CommentAndWhitespaceExtractor;
import twg2.parser.workflow.CodeFileSrc;
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
		CodeFileSrc parserRes = CommentAndWhitespaceExtractor.buildCommentsAndWhitespaceTree(style, srcNameBase + "1", src.toCharArray(), 0, src.length());

		// TODO debugging
		TreePrint.printTree(parserRes.astTree, (f) -> f.getText(), System.out);
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
		CodeFileSrc parserRes = CommentAndWhitespaceExtractor.buildCommentsAndWhitespaceTree(style, srcNameBase + "1", src.toCharArray(), 0, src.length());

		// TODO debugging
		TreePrint.printTree(parserRes.astTree, (f) -> f.getText(), System.out);
	}


	public ParsedFileStats parseFileWhitespaceComments(EnumSet<CommentStyle> style) throws IOException {
		Path file = Paths.get("src/twg2/parser/documentParser/blocks/ParseBlocks.java");
		String src = StringJoin.join(Files.readAllLines(file), "\n");
		CodeFileSrc parserRes = CommentAndWhitespaceExtractor.buildCommentsAndWhitespaceTree(style, file.toFile().getName(), src.toCharArray(), 0, src.length());

		return CommentAndWhitespaceExtractor.calcCommentsAndWhitespaceLinesTreeStats(file.toString(), src.toCharArray(), 0, src.length(), parserRes.lineStartOffsets, parserRes.astTree);
	}


	public static void main(String[] args) throws IOException {
		DocumentParserTest documentParseTest = new DocumentParserTest();

		EnumSet<CommentStyle> style = CommentStyle.multiAndSingleLine();

		// test 1
		//documentParseTest.testCommentWhitespaceParserDoubleQuotes(lang);
		// test 2
		documentParseTest.testCommentWhitespaceParserDoubleAndSingleQuotes(style);
		// real I/O test
		//documentParseTest.parseFileWhitespaceComments(lang);
	}

}
