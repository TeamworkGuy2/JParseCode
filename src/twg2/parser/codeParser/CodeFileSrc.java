package twg2.parser.codeParser;

import java.util.List;

import lombok.Getter;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-11-22
 * @param <T> the type of {@link SimpleTree} data stored in this code file's parsed {@link #getDoc()} structure
 */
public class CodeFileSrc<T, L extends CodeLanguage> {
	@Getter SimpleTree<T> doc;
	@Getter String src;
	@Getter List<String> lines;
	@Getter L language;


	/**
	 * @param doc
	 * @param src
	 * @param lines
	 * @param language optional
	 */
	public CodeFileSrc(SimpleTree<T> doc, String src, List<String> lines, L language) {
		this.doc = doc;
		this.src = src;
		this.lines = lines;
		this.language = language;
	}

}
