package twg2.parser.codeParser;

import java.util.List;

import lombok.Getter;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-11-22
 * @param <T_TREE> the type of {@link SimpleTree} data stored in this code file's parsed {@link #getDoc()} structure
 * @param <T_LANG> the code language of the source file
 */
public class CodeFileSrc<T_TREE, T_LANG extends CodeLanguage> {
	@Getter SimpleTree<T_TREE> doc;
	@Getter String srcName;
	@Getter String src;
	@Getter List<String> lines;
	@Getter T_LANG language;


	/**
	 * @param doc
	 * @param src
	 * @param lines
	 * @param language optional
	 */
	public CodeFileSrc(SimpleTree<T_TREE> doc, String srcName, String src, List<String> lines, T_LANG language) {
		this.doc = doc;
		this.srcName = srcName;
		this.src = src;
		this.lines = lines;
		this.language = language;
	}

}
