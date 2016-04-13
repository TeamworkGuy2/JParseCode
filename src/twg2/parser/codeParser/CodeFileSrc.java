package twg2.parser.codeParser;

import java.util.List;

import lombok.Getter;
import twg2.parser.documentParser.CodeFragment;
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
	@Getter String src;
	@Getter List<char[]> lines;
	@Getter T_LANG language;


	/**
	 * @param doc
	 * @param src
	 * @param lines
	 * @param language optional
	 */
	@SuppressWarnings("unchecked")
	public CodeFileSrc(SimpleTree<? extends CodeFragment> doc, String srcName, String src, List<char[]> lines, T_LANG language) {
		this.doc = (SimpleTree<CodeFragment>) doc;
		this.srcName = srcName;
		this.src = src;
		this.lines = lines;
		this.language = language;
	}

}
