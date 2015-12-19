package twg2.parser.intermAst.block;

import lombok.Getter;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public class IntermClass<T_BLOCK extends CompoundBlock> {
	private @Getter IntermClassSig declaration;
	private @Getter SimpleTree<DocumentFragmentText<CodeFragmentType>> blockTree;
	private @Getter T_BLOCK blockType;


	public IntermClass(IntermClassSig declaration, SimpleTree<DocumentFragmentText<CodeFragmentType>> blockTree, T_BLOCK blockType) {
		this.declaration = declaration;
		this.blockTree = blockTree;
		this.blockType = blockType;
	}


	@Override
	public String toString() {
		return "unparsed block: " + declaration.toString() + ": " + blockTree;
	}

}