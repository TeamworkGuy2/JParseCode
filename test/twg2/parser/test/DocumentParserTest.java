package twg2.parser.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Test;

import twg2.parser.codeParser.CommentStyle;
import twg2.parser.codeParser.extractors.CommentAndWhitespaceExtractor;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.textFragment.TextFragmentRefImpl;
import twg2.parser.workflow.CodeFileSrc;
import twg2.treeLike.TreePrint;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.simpleTree.SimpleTreeUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-5-27
 */
public class DocumentParserTest {
	EnumSet<CommentStyle> style = CommentStyle.multiAndSingleLine();

	@Test
	public void parseCommentsAndWhitespaceTree() throws IOException {
		String srcNameBase = "parseCommentsAndNestedDoubleAndSingleQuotes";
		String src = "/*line one is a comment*/\n" +
				"// line two also\n" +
				"\t\n" +
				"line 3 // end of line comment\n" +
				"line 4 = \"string \\\"with\\\" embedded string\"; /* multiline\n" +
				"line 5 - comment */\n" +
				"'last \\'line\\' string' ending\n" +
				"";
		CodeFileSrc parserRes = CommentAndWhitespaceExtractor.buildCommentsAndWhitespaceTree(style, srcNameBase + "-1", src.toCharArray(), 0, src.length());

		// TODO debugging
		TreePrint.printTree(parserRes.astTree, (f) -> f.getText(), System.out);

		var tokens = new ArrayList<CodeToken>();
		SimpleTreeUtil.traverseNodesDepthFirst(parserRes.astTree, TreeTraversalOrder.POST_ORDER, (branch, idx, size, depth, parentBranch) -> { tokens.add(branch); System.out.println(branch); });
		Assert.assertArrayEquals(new CodeToken[] {
			new CodeToken(CodeTokenType.COMMENT, new TextFragmentRefImpl(0, 25, 0, 0, 0, 24), src.substring(0, 25)),
			new CodeToken(CodeTokenType.COMMENT, new TextFragmentRefImpl(26, 43, 1, 0, 2, -1), src.substring(26, 43)),
			new CodeToken(CodeTokenType.COMMENT, new TextFragmentRefImpl(52, 75, 3, 7, 4, -1), src.substring(52, 75)),
			new CodeToken(CodeTokenType.STRING, new TextFragmentRefImpl(84, 117, 4, 9, 4, 41), src.substring(84, 117)),
			new CodeToken(CodeTokenType.COMMENT, new TextFragmentRefImpl(119, 151, 4, 44, 5, 18), src.substring(119, 151)),
			new CodeToken(CodeTokenType.STRING, new TextFragmentRefImpl(152, 174, 6, 0, 6, 21), src.substring(152, 174)),
			new CodeToken(CodeTokenType.DOCUMENT, new TextFragmentRefImpl(0, 182, 0, 0, 7, -1), src.substring(0, 182)),
		}, tokens.toArray(new CodeToken[0]));
	}


	@Test
	public void parseCommentsAndWhitespace1() throws IOException {
		String src = "\n" +
				"/*line two is a comment*/\n" +
				"\t\n" +
				"line 3 {\n" +
				"line 4 = \"string \\\"with\\\" embedded string\"; /* multiline\n" +
				"line 5 - comment */ }\n" +
				"\r\n" +
				"";
		CodeFileSrc parserRes = CommentAndWhitespaceExtractor.buildCommentsAndWhitespaceTree(style, "parseStats-1", src.toCharArray(), 0, src.length());

		var stats = CommentAndWhitespaceExtractor.calcCommentsAndWhitespaceLinesTreeStats("parseStats-1", src.toCharArray(), 0, src.length(), parserRes.lineStartOffsets, parserRes.astTree);
		Assert.assertEquals(8, stats.getTotalLineCount());
		Assert.assertArrayEquals(new int[] { 0, 2, 6, 7 }, stats.getWhitespaceLineNumbers().toArray());
		Assert.assertArrayEquals(new int[] { 1 } , stats.getCommentLineNumbers().toArray()); // only lines which are 100% comment count (except for whitespace)
	}


	@Test
	public void parseCommentsAndWhitespace2() throws IOException {
		String src = "/*line one is a comment*/\n" +
				"// line two also\n" +
				"\t\n" +
				"line 3 // end of line comment\n" +
				"line 4 = \"string \\\"with\\\" embedded string\"; /* multiline\n" +
				"line 5 - comment */\n" +
				"'last \\'line\\' string' ending\n" +
				"";
		CodeFileSrc parserRes = CommentAndWhitespaceExtractor.buildCommentsAndWhitespaceTree(style, "parseStats-2", src.toCharArray(), 0, src.length());

		var stats = CommentAndWhitespaceExtractor.calcCommentsAndWhitespaceLinesTreeStats("parseStats-1", src.toCharArray(), 0, src.length(), parserRes.lineStartOffsets, parserRes.astTree);
		Assert.assertEquals(8, stats.getTotalLineCount());
		Assert.assertArrayEquals(new int[] { 2, 7 }, stats.getWhitespaceLineNumbers().toArray());
		Assert.assertArrayEquals(new int[] { 0, 1, 5 } , stats.getCommentLineNumbers().toArray()); // only lines which are 100% comment count (except for whitespace)
	}

}
