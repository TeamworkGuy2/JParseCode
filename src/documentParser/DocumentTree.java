package documentParser;

import parser.textFragment.TextTokenConsumer;
import twg2.treeLike.simpleTree.SimpleTreeImpl;

/**
 * @author TeamworkGuy2
 * @since 2015-5-28
 */
public interface DocumentTree<T> {

	public void getText(Appendable dst);


	public void getText(StringBuilder dst);


	public SimpleTreeImpl<DocumentFragment<T>> getTree();


	public void forEveryFragment(TextTokenConsumer<DocumentFragment<T>> consumer);


	public void forEachRootFragment(TextTokenConsumer<DocumentFragment<T>> consumer);

}
