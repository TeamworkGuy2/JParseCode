package codeParser.csharp;

import intermAst.block.IntermBlock;
import intermAst.method.IntermMethodSig;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.treeLike.simpleTree.SimpleTree;
import baseAst.CompoundBlock;
import baseAst.util.AstUtil;
import baseAst.util.NameUtil;
import codeParser.CodeFragmentType;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class CSharpInterfaceMethodExtractor {
	IntermBlock<? extends CompoundBlock> parentBlock;
	List<IntermMethodSig> methods = new ArrayList<>();
	SimpleTree<DocumentFragmentText<CodeFragmentType>> tree;


	/**
	 * @param tree
	 */
	public CSharpInterfaceMethodExtractor(SimpleTree<DocumentFragmentText<CodeFragmentType>> tree, IntermBlock<? extends CompoundBlock> parentBlock) {
		this.methods = new ArrayList<>();
		this.tree = tree;
		this.parentBlock = parentBlock;
	}


	public void extractInterfaceMethodsTreeConsumer(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode, DocumentFragmentText<CodeFragmentType> token,
			int idx, int siblingCount, int depth, List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> siblings,
			SimpleTree<DocumentFragmentText<CodeFragmentType>> parentNode, DocumentFragmentText<CodeFragmentType> parent) {

		if(idx > 1 && token.getFragmentType().isCompound() && token.getText().startsWith("(")) {
			val methodNameNode = AstUtil.getSiblingData(siblings, idx, -1);
			val returnTypeNode = AstUtil.getSiblingData(siblings, idx, -2);
			val parentFrag = AstUtil.queryParent(tokenNode, 1, (frag) -> {
				return AstUtil.isBlock(frag, "{");
			});
			if(parentFrag != null && returnTypeNode != null && AstUtil.isType(methodNameNode, CodeFragmentType.IDENTIFIER) && AstUtil.isType(returnTypeNode, CodeFragmentType.IDENTIFIER)) {
				String methodName = methodNameNode.getText();
				methods.add(new IntermMethodSig(methodName, NameUtil.newFqName(parentBlock.getDeclaration().getFullyQualifyingName(), methodName), token.getText(), returnTypeNode.getText()));
			}
		}
	}


	public static List<IntermMethodSig> extractInterfaceMethods(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenTree, IntermBlock<? extends CompoundBlock> parentBlock) {
		CSharpInterfaceMethodExtractor extractor = new CSharpInterfaceMethodExtractor(tokenTree, parentBlock);
		AstUtil.forEach(extractor.tree, extractor::extractInterfaceMethodsTreeConsumer);
		return extractor.methods;
	}

}
