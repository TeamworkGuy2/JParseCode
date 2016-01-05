package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.val;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.method.IntermParameterSig;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-3
 */
public class CsMethodParametersParser {

	// TODO does not support default parameters
	public static List<IntermParameterSig> extractParamsFromSignature(SimpleTree<DocumentFragmentText<CodeFragmentType>> sigNode) {
		val childs = sigNode.getChildren();
		int size = childs.size();

		if(size == 0) {
			return Collections.emptyList();
		}

		List<IntermParameterSig> params = new ArrayList<>();

		for(int i = 0; i < size; i += 2) {
			val type = childs.get(i + 0).getData().getText();
			boolean optional = false;
			if(childs.get(i + 1).getData().getText().equals("?")) {
				optional = true;
				// each parameter is expected to have 2, so jump the optional one
				i++;
			}
			val name = childs.get(i + 1).getData().getText();
			val param = new IntermParameterSig(name, type, optional, null);
			params.add(param);
		}

		return params;
	}

}
