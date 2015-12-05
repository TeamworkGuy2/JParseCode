package codeParser;

import java.util.List;

import lombok.Getter;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-11-22
 * @param <T> the type of {@link SimpleTree} data stored in this code file's parsed {@link #getDoc()} structure
 */
public class CodeFile<T> {
	@Getter SimpleTree<T> doc;
	@Getter String src;
	@Getter List<String> lines;


	public CodeFile(SimpleTree<T> doc, String src, List<String> lines) {
		this.doc = doc;
		this.src = src;
		this.lines = lines;
	}

}
