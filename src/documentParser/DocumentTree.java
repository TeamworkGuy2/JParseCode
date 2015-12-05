package documentParser;

import parser.textFragment.TextTokenConsumer;
import twg2.treeLike.simpleTree.SimpleTreeImpl;

/**
 * @author TeamworkGuy2
 * @since 2015-5-28
 */
public interface DocumentTree<R extends DocumentFragment<S, T>, S, T> {

	public void getText(Appendable dst);


	public void getText(StringBuilder dst);


	public SimpleTreeImpl<R> getTree();


	public void forEveryFragment(TextTokenConsumer<R> consumer);


	public void forEachRootFragment(TextTokenConsumer<R> consumer);

}
