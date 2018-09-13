package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.val;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.method.ParameterSig;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.stateMachine.AstParser;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-3
 */
public class MethodParametersParser {

	// TODO does not support default parameters
	public static List<ParameterSig> extractParamsFromSignature(KeywordUtil<? extends AccessModifier> keywordUtil, AstParser<List<AnnotationSig>> annotationParser, SimpleTree<CodeToken> sigNode) {
		val childs = sigNode.getChildren();
		int size = childs.size();

		if(size == 0) {
			return Collections.emptyList();
		}

		List<ParameterSig> params = new ArrayList<>();

		for(int i = 0; i < size; i += 2) {
			// try to read annotation before parameter modifiers
			int annotationCompletedAt = i;
			while(annotationParser.acceptNext(childs.get(i + 0))) {
				i++;
				if(annotationParser.isComplete()) {
					// TODO bit of a hack to figure out if the last token was actually part of the annotation or not since C# and Java annotation parsers always consume all tokens passed to them
					annotationCompletedAt = (childs.get(i - 1).getData().getTokenType() == CodeTokenType.BLOCK ? i : i - 1);
				}
			}
			i = annotationCompletedAt;

			CodeToken token = childs.get(i + 0).getData();

			// read parameter modifiers
			List<AccessModifier> paramMods = new ArrayList<>(1);
			if(token.getTokenType() == CodeTokenType.KEYWORD && keywordUtil.isParameterModifier(token.getText(), params.size())) {
				paramMods.add(keywordUtil.toKeyword(token.getText()));
				i++;
				token = childs.get(i + 0).getData();
			}

			// try to read annotation after parameter modifiers
			annotationCompletedAt = i;
			while(annotationParser.acceptNext(childs.get(i + 0))) {
				i++;
				if(annotationParser.isComplete()) {
					// TODO bit of a hack to figure out if the last token was actually part of the annotation or not since C# and Java annotation parsers always consume all tokens passed to them
					annotationCompletedAt = (childs.get(i - 1).getData().getTokenType() == CodeTokenType.BLOCK ? i : i - 1);
				}
			}
			i = annotationCompletedAt;

			token = childs.get(i + 0).getData();

			val type = token.getText();

			boolean optional = false;
			token = childs.get(i + 1).getData();

			if(token.getText().equals("?")) {
				optional = true;
				// each parameter is expected to have 2, so jump the optional one
				i++;
			}
			List<AnnotationSig> annotations = annotationParser.getParserResult();
			annotations = annotations.size() > 0 ? new ArrayList<>(annotations) : null;
			val name = childs.get(i + 1).getData().getText();
			val param = new ParameterSig(name, type, paramMods, annotations, optional, null);
			params.add(param);
		}

		return params;
	}

}
