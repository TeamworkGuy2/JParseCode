package twg2.parser.workflow;

import twg2.annotations.Immutable;
import twg2.collections.primitiveCollections.IntListSorted;
import twg2.parser.fragment.CodeToken;
import twg2.parser.language.CodeLanguage;
import twg2.treeLike.simpleTree.SimpleTree;

/** All the information resulting from building a source string into an AST.
 * Contains an {@link CodeToken} AST tree, source name, source string, source string as list of lines, and a language.
 * @author TeamworkGuy2
 * @since 2015-11-22
 */
@Immutable
public class CodeFileSrc {
	public final SimpleTree<CodeToken> astTree;
	public final String srcName;
	public final char[] src;
	public final int srcOff;
	public final int srcLen;
	public final IntListSorted lineStartOffsets;
	public final CodeLanguage language;


	/**
	 * @param astTree
	 * @param src
	 * @param language optional
	 */
	@SuppressWarnings("unchecked")
	public CodeFileSrc(SimpleTree<? extends CodeToken> astTree, String srcName, char[] src, int srcOff, int srcLen, IntListSorted lineStartOffsets, CodeLanguage language) {
		this.astTree = (SimpleTree<CodeToken>)astTree;
		this.srcName = srcName;
		this.src = src;
		this.srcOff = srcOff;
		this.srcLen = srcLen;
		this.lineStartOffsets = lineStartOffsets;
		this.language = language;
	}


	@Override
	public String toString() {
		return srcName + " (" + language + ", " + lineStartOffsets.size() + " lines)";
	}

}
