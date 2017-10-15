package twg2.ast.interm.block;

import twg2.annotations.Immutable;
import twg2.ast.interm.classes.ClassSigSimple;
import twg2.parser.codeParser.BlockType;
import twg2.parser.fragment.CodeToken;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
@Immutable
public class BlockAst<T_BLOCK extends BlockType> {
	public final ClassSigSimple declaration;
	public final SimpleTree<CodeToken> blockTree;
	public final T_BLOCK blockType;


	public BlockAst(ClassSigSimple declaration, SimpleTree<CodeToken> blockTree, T_BLOCK blockType) {
		this.declaration = declaration;
		this.blockTree = blockTree;
		this.blockType = blockType;
	}


	@Override
	public String toString() {
		return "unparsed block: " + declaration.toString() + ": " + blockTree;
	}

}
