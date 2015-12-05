package codeParser.csharp;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import parser.textFragment.TextFragmentRef;
import twg2.treeLike.simpleTree.SimpleTree;
import baseAst.AstUtil;
import baseAst.method.MethodSig;
import codeParser.CodeFile;
import codeParser.CodeFragmentType;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class CSharpInterfaceExtractor {
	List<MethodSig> methods = new ArrayList<>();
	SimpleTree<DocumentFragmentText<CodeFragmentType>> tree;
	String src;


	/**
	 * @param tree
	 * @param src
	 */
	public CSharpInterfaceExtractor(SimpleTree<DocumentFragmentText<CodeFragmentType>> tree, String src) {
		this.methods = new ArrayList<>();
		this.tree = tree;
		this.src = src;
	}


	public void extractInterfaceIndexedSubTreeConsumer(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode, DocumentFragmentText<CodeFragmentType> token,
			int idx, int siblingCount, int depth, List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> siblings,
			SimpleTree<DocumentFragmentText<CodeFragmentType>> parentNode, DocumentFragmentText<CodeFragmentType> parent) {

		TextFragmentRef textFrag = token.getTextFragment();
		String text = textFrag.getText(src).toString();

		if(siblingCount > 0 && token.getFragmentType().isCompound() && text.indexOf('(') == 0) {
			val prev1 = AstUtil.getSiblingData(siblings, idx, -1);
			val prev2 = AstUtil.getSiblingData(siblings, idx, -2);
			val parentFrag = AstUtil.queryParent(tokenNode, 1, (frag) -> {
				return frag.getFragmentType() == CodeFragmentType.BLOCK && frag.getTextFragment().getText(src).toString().indexOf('{') == 0;
			});
			if(parentFrag != null && prev1 != null && prev2 != null) {
				methods.add(new MethodSig(prev2.getTextFragment().getText(src).toString(), prev1.getTextFragment().getText(src).toString(), token.getTextFragment().getText(src).toString()));
			}
		}
	}


	public static List<MethodSig> extractInterfaceMethods(CodeFile<DocumentFragmentText<CodeFragmentType>> parsedFile) {
		CSharpInterfaceExtractor extractor = new CSharpInterfaceExtractor(parsedFile.getDoc(), parsedFile.getSrc());
		AstUtil.forEach(extractor.tree, extractor::extractInterfaceIndexedSubTreeConsumer);
		return extractor.methods;
	}

}
