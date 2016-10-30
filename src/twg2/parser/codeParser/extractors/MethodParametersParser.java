package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.val;
import twg2.ast.interm.method.ParameterSig;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-3
 */
public class MethodParametersParser {

	// TODO does not support default parameters
	public static List<ParameterSig> extractParamsFromSignature(KeywordUtil<? extends AccessModifier> keywordUtil, SimpleTree<CodeToken> sigNode) {
		val childs = sigNode.getChildren();
		int size = childs.size();

		if(size == 0) {
			return Collections.emptyList();
		}

		List<ParameterSig> params = new ArrayList<>();

		for(int i = 0; i < size; i += 2) {
			CodeToken token = childs.get(i + 0).getData();
			List<AccessModifier> paramMods = new ArrayList<>();
			if(token.getTokenType() == CodeTokenType.KEYWORD && keywordUtil.isParameterModifier(token.getText(), params.size())) {
				paramMods.add(keywordUtil.toKeyword(token.getText()));
				i++;
				token = childs.get(i + 0).getData();
			}

			val type = token.getText();

			boolean optional = false;
			token = childs.get(i + 1).getData();
			if(token.getText().equals("?")) {
				optional = true;
				// each parameter is expected to have 2, so jump the optional one
				i++;
			}
			val name = childs.get(i + 1).getData().getText();
			val param = new ParameterSig(name, type, paramMods, optional, null);
			params.add(param);
		}

		return params;
	}

}
