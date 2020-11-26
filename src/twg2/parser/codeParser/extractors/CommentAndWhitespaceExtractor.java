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

	public static CodeFileSrc buildCommentsAndWhitespaceTreeFromFileExtension(String srcName, String fileExtension, boolean reusable, char[] src, int srcOff, int srcLen) throws IOException {
		EnumSet<CommentStyle> commentStyle = CommentStyle.fromFileExtension(fileExtension);

		return buildCommentsAndWhitespaceTree(reusable, commentStyle, srcName, src, srcOff, srcLen);
	}


	public static CodeFileSrc buildCommentsAndWhitespaceTree(boolean reusable, EnumSet<CommentStyle> style, String srcName, char[] src, int srcOff, int srcLen) throws IOException {
		CharParserFactory stringParser = CodeStringTokenizer.createStringTokenizerForJavascript(reusable);
		CharParserFactory commentParser = CommentTokenizer.createCommentTokenizer(reusable, style);

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
			int lineLen = endIndexExclusive - startIndex;

			if(tokensPerLine.size() <= i) {
				tokensPerLine.add(new ArrayList<>());
			}

			var lineTokens = tokensPerLine.get(i);
			// TODO performance of stream + lambda for every line
			if(lineTokens.size() > 0 && lineTokens.stream().allMatch((t) -> t.getTokenType() == CodeTokenType.COMMENT)) {
				if(lineTokens.size() > 1) {
					// TODO this was causing issues parsing a particular project that had a number of multiple-comments-per-line files
					//throw new RuntimeException("not implemented support for checking if a line is to exclusively comments when more than one comment appears on the line, lineNum=" + (i + 1) + ", srcId='" + srcId + "'");
				}
				else {
					TextFragmentRef comment = lineTokens.get(0).getToken();
					var prefixLen = comment.getOffsetStart() - startIndex;
					var noneOrWhitespacePrefix = comment.getLineStart() < i || StringCheck.isWhitespace(src, startIndex, prefixLen); // if the token started on a previous line, there is no prefix text before it starts on this line
					var suffixLen = endIndexExclusive - comment.getOffsetEnd();
					var noneOrWhitespaceSuffix = comment.getLineEnd() > i || StringCheck.isWhitespace(src, comment.getOffsetEnd(), suffixLen);

					if(noneOrWhitespacePrefix && noneOrWhitespaceSuffix) {
						if(StringCheck.isWhitespace(src, startIndex, lineLen)) {
							whitespaceLines.add(i);
						}
						else {
							commentLines.add(i);
						}
					}
				}
			}
			else if(StringCheck.isWhitespace(src, startIndex, lineLen)) {
				whitespaceLines.add(i);
			}
			//System.out.println("line " + i + " tokens: " + lineTokens);
		}

		System.out.println("line count: " + lineStartOffsets.size());
		System.out.println("line counts: whitespace=" + whitespaceLines.size() + ", comment=" + commentLines.size() + ", total=" + (whitespaceLines.size() + commentLines.size()));
		System.out.println("line numbers:\nwhitespace: " + whitespaceLines + "\ncomments: " + commentLines);

		return new ParsedFileStats(srcId, srcLen, whitespaceLines, commentLines, lineStartOffsets.size());
	}


	public static List<List<CodeToken>> documentTreeToTokensPerLine(SimpleTree<CodeToken> tree) {
		// flatten the document tree into a nested list of tokens per source line of text
		var tokensPerLine = new ArrayList<List<CodeToken>>();

		var treeTraverseParams = SimpleTreeTraverseParameters.of(tree, false, TreeTraversalOrder.PRE_ORDER)
				.setSkipRoot(true)
				.setConsumerSimpleTree((branch, index, size, depth, parentBranch) -> {
					int startLine = branch.getToken().getLineStart();
					int endLine = branch.getToken().getLineEnd();
					while(tokensPerLine.size() <= endLine) {
						tokensPerLine.add(new ArrayList<>());
					}
					for(int i = startLine; i <= endLine; i++) {
						tokensPerLine.get(i).add(branch);
					}
				});
		TreeTraverse.Indexed.traverse(treeTraverseParams);

		return tokensPerLine;
	}

}
