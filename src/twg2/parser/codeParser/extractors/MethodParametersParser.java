package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.method.ParameterSig;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.Operator;
import twg2.parser.codeParser.OperatorUtil;
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
	public static List<ParameterSig> extractParamsFromSignature(KeywordUtil<? extends Keyword> keywordUtil, OperatorUtil<? extends Operator> operatorUtil,
			AstParser<List<AnnotationSig>> annotationParser, SimpleTree<CodeToken> sigNode) {
		var childs = sigNode.getChildren();
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
			List<Keyword> paramMods = new ArrayList<>(1);
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

			// read parameter type and name
			token = childs.get(i + 0).getData();

			var type = token.getText();

			boolean optional = false;
			token = childs.get(i + 1).getData();

			if(token.getText().equals("?")) {
				optional = true;
				// each parameter is expected to have 2, so jump the optional one
				i++;
			}

			var name = childs.get(i + 1).getData().getText();

			// read parameter default value if available
			String defaultValue = null;
			if(i + 3 < size && (token = childs.get(i + 2).getData()).getTokenType() == CodeTokenType.OPERATOR && operatorUtil.assignmentOperators().is(token)) {
				if(DataTypeExtractor.isDefaultValueLiteral(token = childs.get(i + 3).getData())) {
					defaultValue = token.getText();
					i += 2;
				}
			}

			List<AnnotationSig> annotations = annotationParser.getParserResult();
			annotations = annotations.size() > 0 ? new ArrayList<>(annotations) : null;
			var param = new ParameterSig(name, type, paramMods, annotations, optional, defaultValue);
			params.add(param);
		}

		return params;
	}

}
