package twg2.parser.codeParser.extractors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import twg2.collections.dataStructures.PairList;
import twg2.collections.primitiveCollections.IntArrayList;
import twg2.collections.primitiveCollections.IntListSorted;
import twg2.parser.codeParser.CommentStyle;
import twg2.parser.codeParser.codeStats.ParsedFileStats;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.language.CodeLanguage;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.parser.textFragment.TextTransformer;
import twg2.parser.tokenizers.CodeStringTokenizer;
import twg2.parser.tokenizers.CodeTokenizer;
import twg2.parser.tokenizers.CommentTokenizer;
import twg2.parser.workflow.CodeFileSrc;
import twg2.text.stringUtils.StringCheck;
import twg2.text.tokenizer.CharParserFactory;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.TreeTraverse;
import twg2.treeLike.parameters.SimpleTreeTraverseParameters;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-9-19
 */
public class CommentAndWhitespaceExtractor {

	public static CodeFileSrc buildCommentsAndWhitespaceTreeFromFileExtension(String srcName, String fileExtension, char[] src, int srcOff, int srcLen) throws IOException {
		EnumSet<CommentStyle> commentStyle = CommentStyle.fromFileExtension(fileExtension);

		return buildCommentsAndWhitespaceTree(commentStyle, srcName, src, srcOff, srcLen);
	}


	public static CodeFileSrc buildCommentsAndWhitespaceTree(EnumSet<CommentStyle> style, String srcName, char[] src, int srcOff, int srcLen) throws IOException {
		CharParserFactory stringParser = CodeStringTokenizer.createStringTokenizerForJavascript();
		CharParserFactory commentParser = CommentTokenizer.createCommentTokenizer(style);

		var parsers = new PairList<CharParserFactory, TextTransformer<CodeTokenType>>();
		parsers.add(commentParser, CodeTokenizer.ofType(CodeTokenType.COMMENT));
		parsers.add(stringParser, CodeTokenizer.ofType(CodeTokenType.STRING));

		var parser = CodeTokenizer.createTokenizer((CodeLanguage)null, parsers);
		return parser.tokenizeDocument(src, srcOff, srcLen, srcName, null);
	}


	public static ParsedFileStats calcCommentsAndWhitespaceLinesTreeStats(String srcId, char[] src, int srcOff, int srcLen, IntListSorted lineStartOffsets, SimpleTree<CodeToken> tree) {
		// flatten the document tree into a nested list of tokens per source line of text
		var tokensPerLine = documentTreeToTokensPerLine(tree);

		// find lines containing only comments (with optional whitespace)
		IntArrayList commentLines = new IntArrayList();
		IntArrayList whitespaceLines = new IntArrayList();
		for(int i = 0, size = lineStartOffsets.size(); i < size; i++) {
			int startIndex = lineStartOffsets.get(i);
			int endIndexExclusive = i + 1 < size ? lineStartOffsets.get(i + 1) : srcLen;
			char[] line = new char[endIndexExclusive - startIndex];
			System.arraycopy(src, startIndex, line, 0, endIndexExclusive);

			if(tokensPerLine.size() <= i) {
				tokensPerLine.add(new ArrayList<>());
			}

			var lineTokens = tokensPerLine.get(i);
			if(lineTokens.size() > 0 && lineTokens.stream().allMatch((t) -> t.getTokenType() == CodeTokenType.COMMENT)) {
				if(lineTokens.size() > 1) {
					// TODO this was causing issues parsing a particular project that had a number of multiple-comments-per-line files
					//throw new RuntimeException("not implemented support for checking if a line is to exclusively comments when more than one comment appears on the line, lineNum=" + (i + 1) + ", srcId='" + srcId + "'");
				}
				else {
					TextFragmentRef comment = lineTokens.get(0).getToken();
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

		System.out.println("line count: " + lineStartOffsets.size());
		System.out.println("line counts: whitespace=" + whitespaceLines.size() + ", comment=" + commentLines.size() + ", total=" + (whitespaceLines.size() + commentLines.size()));
		System.out.println("line numbers:\nwhitespace: " + whitespaceLines + "\ncomments: " + commentLines);

		return new ParsedFileStats(srcId, srcLen, whitespaceLines, commentLines, lineStartOffsets.size());
	}


	public static List<List<CodeToken>> documentTreeToTokensPerLine(SimpleTree<CodeToken> tree) {
		// flatten the document tree into a nested list of tokens per source line of text
		List<List<CodeToken>> tokensPerLine = new ArrayList<>();

		var treeTraverseParams = SimpleTreeTraverseParameters.of(tree, false, TreeTraversalOrder.PRE_ORDER)
				.setSkipRoot(true)
				.setConsumerSimpleTree((branch, index, size, depth, parentBranch) -> {
					int startLine0 = branch.getToken().getLineStart();
					int endLine0 = branch.getToken().getLineEnd();
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
