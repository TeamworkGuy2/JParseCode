package documentParser;

import java.io.IOException;
import java.io.UncheckedIOException;

import parser.textFragment.TextTokenConsumer;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.simpleTree.SimpleTreeImpl;
import twg2.treeLike.simpleTree.SimpleTreeUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-5-27
 */
public class DocumentTreeBuilder<T> implements DocumentTree<T> {
	private String text;
	private SimpleTreeImpl<DocumentFragment<T>> tree;


	public DocumentTreeBuilder(DocumentFragment<T> root) {
		tree = new SimpleTreeImpl<>(root);
	}


	@Override
	public void getText(Appendable dst) {
		try {
			dst.append(text);
		} catch(IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}


	@Override
	public void getText(StringBuilder dst) {
		dst.append(text);
	}


	public void setText(String text) {
		this.text = text;
	}


	@Override
	public SimpleTreeImpl<DocumentFragment<T>> getTree() {
		return tree;
	}


	public void setTree(SimpleTreeImpl<DocumentFragment<T>> tree) {
		this.tree = tree;
	}


	@Override
	public void forEveryFragment(TextTokenConsumer<DocumentFragment<T>> consumer) {
		SimpleTreeUtil.traverseAllNodes(tree, TreeTraversalOrder.POST_ORDER, (t, d, p) -> {
			consumer.accept(t, text, 0, 0, d, p);
		});
	}


	@Override
	public void forEachRootFragment(TextTokenConsumer<DocumentFragment<T>> consumer) {
		SimpleTreeUtil.traverseLeafNodes(tree, TreeTraversalOrder.POST_ORDER, (t, d, p) -> {
			consumer.accept(t, text, 0, 0, d, p);
		});
	}

}
