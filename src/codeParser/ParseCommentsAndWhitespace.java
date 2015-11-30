package codeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import lombok.val;
import parser.condition.Precondition;
import parser.textFragment.TextFragmentRef;
import twg2.collections.primitiveCollections.IntArrayList;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.TreeTraverse;
import twg2.treeLike.parameters.SimpleTreeTraverseParameters;
import twg2.treeLike.simpleTree.SimpleTree;
import codeParser.codeStats.ParsedFileStats;
import codeParser.parsers.CodeStringParser;
import codeParser.parsers.CommentParser;
import documentParser.DocumentFragment;

/**
 * @author TeamworkGuy2
 * @since 2015-9-19
 */
public class ParseCommentsAndWhitespace {

	public static CodeFile buildCommentsAndWhitespaceTreeFromFileExtension(String fileExtension, String src) throws IOException {
		EnumSet<CommentStyle> commentStyle = CommentStyle.fromFileExtension(fileExtension);

		return buildCommentsAndWhitespaceTree(commentStyle, src);
	}


	public static CodeFile buildCommentsAndWhitespaceTree(EnumSet<CommentStyle> style, String src) throws IOException {
		Precondition stringParser = CodeStringParser.createStringParserForJavascript();
		Precondition commentParser = CommentParser.createCommentParser(style);

		ParserBuilder parser = new ParserBuilder()
			.addConstParser(commentParser, CodeFragmentType.COMMENT)
			.addConstParser(stringParser, CodeFragmentType.STRING);
		return parser.buildAndParse(src);
	}


	public static ParsedFileStats calcCommentsAndWhitespaceLinesTreeStats(String srcId, int srcCharCount, List<String> lines, SimpleTree<DocumentFragment<CodeFragmentType>> tree) {
		// flatten the document tree into a nested list of tokens per source line of text
		val tokensPerLine = documentTreeToTokensPerLine(tree);

		// find lines containing only comments (with optional whitespace)
		IntArrayList commentLines = new IntArrayList();
		IntArrayList whitespaceLines = new IntArrayList();
		for(int i = 0, size = lines.size(); i < size; i++) {
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
					String line = lines.get(i);
					String prefix = comment.getLineStart() < i ? "" : line.substring(0, comment.getColumnStart()); // if the token started on a previous line, there is no prefix text before it starts on this line
					String suffix = "";
					// in case the token ends at the end of the line
					if(comment.getColumnEnd() + 1 < line.length()) {
						line.substring(comment.getColumnEnd() + 1, line.length());
					}

					if(prefix.trim().length() == 0 && suffix.trim().length() == 0) {
						commentLines.add(i + 1);
					}
				}
			}
			else if(lines.get(i).trim().length() == 0) {
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


	public static List<List<DocumentFragment<CodeFragmentType>>> documentTreeToTokensPerLine(SimpleTree<DocumentFragment<CodeFragmentType>> tree) {
		// flatten the document tree into a nested list of tokens per source line of text
		List<List<DocumentFragment<CodeFragmentType>>> tokensPerLine = new ArrayList<>();

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
