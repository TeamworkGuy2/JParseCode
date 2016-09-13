package twg2.parser.workflow;

import lombok.Getter;
import twg2.collections.primitiveCollections.IntListSorted;
import twg2.parser.fragment.CodeFragment;
import twg2.treeLike.simpleTree.SimpleTree;

/** All the information resulting from building a source string into an AST.
 * Contains an {@link CodeFragment} AST tree, source name, source string, source string as list of lines, and a language.
 * @author TeamworkGuy2
 * @since 2015-11-22
 * @param <T_LANG> the code language of the source file
 */
public class CodeFileSrc<T_LANG> {
	@Getter SimpleTree<CodeFragment> doc;
	@Getter String srcName;
	@Getter char[] src;
	@Getter int srcOff;
	@Getter int srcLen;
	@Getter IntListSorted lineStartOffsets;
	@Getter T_LANG language;


	/**
	 * @param doc
	 * @param src
	 * @param language optional
	 */
	@SuppressWarnings("unchecked")
	public CodeFileSrc(SimpleTree<? extends CodeFragment> doc, String srcName, char[] src, int srcOff, int srcLen, IntListSorted lineStartOffsets, T_LANG language) {
		this.doc = (SimpleTree<CodeFragment>) doc;
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
