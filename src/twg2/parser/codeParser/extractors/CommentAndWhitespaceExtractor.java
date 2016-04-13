package twg2.parser.codeParser.extractors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import lombok.val;
import twg2.collections.primitiveCollections.IntArrayList;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CommentStyle;
import twg2.parser.codeParser.ParserBuilder;
import twg2.parser.codeParser.codeStats.ParsedFileStats;
import twg2.parser.codeParser.parsers.CodeStringParser;
import twg2.parser.codeParser.parsers.CommentParser;
import twg2.parser.documentParser.CodeFragment;
import twg2.parser.language.CodeLanguage;
import twg2.parser.text.CharParserFactory;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.text.stringUtils.StringCheck;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.TreeTraverse;
import twg2.treeLike.parameters.SimpleTreeTraverseParameters;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-9-19
 */
public class CommentAndWhitespaceExtractor {

	public static CodeFileSrc<CodeLanguage> buildCommentsAndWhitespaceTreeFromFileExtension(String srcName, String fileExtension, String src) throws IOException {
		EnumSet<CommentStyle> commentStyle = CommentStyle.fromFileExtension(fileExtension);

		return buildCommentsAndWhitespaceTree(commentStyle, src, srcName);
	}


	public static CodeFileSrc<CodeLanguage> buildCommentsAndWhitespaceTree(EnumSet<CommentStyle> style, String src, String srcName) throws IOException {
		CharParserFactory stringParser = CodeStringParser.createStringParserForJavascript();
		CharParserFactory commentParser = CommentParser.createCommentParser(style);

		ParserBuilder parser = new ParserBuilder()
			.addConstParser(commentParser, CodeFragmentType.COMMENT)
			.addConstParser(stringParser, CodeFragmentType.STRING);
		return parser.buildAndParse(src, null, srcName, true);
	}


	public static ParsedFileStats calcCommentsAndWhitespaceLinesTreeStats(String srcId, int srcCharCount, List<char[]> lines, SimpleTree<CodeFragment> tree) {
		// flatten the document tree into a nested list of tokens per source line of text
		val tokensPerLine = documentTreeToTokensPerLine(tree);

		// find lines containing only comments (with optional whitespace)
		IntArrayList commentLines = new IntArrayList();
		IntArrayList whitespaceLines = new IntArrayList();
		for(int i = 0, size = lines.size(); i < size; i++) {
			char[] line = lines.get(i);

			if(tokensPerLine.size() <= i) {
				tokensPerLine.add(new ArrayList<>());
			}

			val lineTokens = tokensPerLine.get(i);
			if(lineTokens.size() > 0 && lineTokens.stream().allMatch((t) -> t.getFragmentType() == CodeFragmentType.COMMENT)) {
				if(lineTokens.size() > 1) {
					// TODO this was causing issues parsing a particular project that had a number of multiple-comments-per-line files
					//throw new RuntimeException("not implemented support for checking if a line is to exclusively comments when more than one comment appears on the line, lineNum=" + (i + 1) + ", srcId='" + srcId + "'");
				}
				else {
					TextFragmentRef comment = lineTokens.get(0).getTextFragment();
					String prefix = comment.getLineStart() < i ? "" : new String(line, 0, comment.getColumnStart()); // if the token started on a previous line, there is no prefix text before it starts on this line
					String suffix = "";
					// in case the token ends at the end of the line
					if(comment.getColumnEnd() + 1 < line.length) {
						// TODO not sure why this was originally here
						//new String(line, comment.getColumnEnd() + 1, line.length - (comment.getColumnEnd() + 1));
					}

					if(prefix.trim().length() == 0 && suffix.trim().length() == 0) {
						commentLines.add(i + 1);
					}
				}
			}
			else if(StringCheck.isWhitespace(line, 0, line.length)) {
				whitespaceLines.add(i + 1);
			}
			System.out.println("line " + i + " tokens: " + lineTokens);
		}

		System.out.println("line count: " + lines.size());
		System.out.println("line counts: whitespace=" + whitespaceLines.size() + ", comment=" + commentLines.size() + ", total=" + (whitespaceLines.size() + commentLines.size()));
		System.out.println("line numbers:\nwhitespace: " + whitespaceLines + "\ncomments: " + commentLines);

		val stats = new ParsedFileStats(srcId, srcCharCount, whitespaceLines, commentLines, lines.size());
		return stats;
	}


	public static List<List<CodeFragment>> documentTreeToTokensPerLine(SimpleTree<CodeFragment> tree) {
		// flatten the document tree into a nested list of tokens per source line of text
		List<List<CodeFragment>> tokensPerLine = new ArrayList<>();

		val treeTraverseParams = SimpleTreeTraverseParameters.of(tree, false, TreeTraversalOrder.PRE_ORDER)
				.setSkipRoot(true)
				.setConsumerSimpleTree((branch, index, size, depth, parentBranch) -> {
					int startLine0 = branch.getTextFragment().getLineStart();
					int endLine0 = branch.getTextFragment().getLineEnd();
					while(tokensPerLine.size() <= endLine0) {
						tokensPerLine.add(new ArrayList<>());
					}
					for(int i = startLine0; i <= endLine0; i++) {
						tokensPerLine.get(i).add(branch);
					}
				});
		TreeTraverse.Indexed.traverse(treeTraverseParams);

		return tokensPerLine;
	}

}