package twg2.parser.codeParser.extractors;

import java.util.HashMap;

import lombok.val;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.language.CodeLanguage;
import twg2.text.stringUtils.StringTrim;
import twg2.treeLike.simpleTree.SimpleTree;

/** Annotation parsers
 * @author TeamworkGuy2
 * @since 2016-4-8
 */
public class AnnotationExtractor {

	/** Parse annotation name and arguments.
	 * NOTE: it doesn't check the validity of the argument values
	 * @param annotNameType the {@link CodeTokenType} of the name node of the annotation
	 * @param annotName the name of the annotation
	 * @param annotParamsNode the arguments '(...)' node of the annotation
	 * @return a parsed annotation
	 */
	public static AnnotationSig parseAnnotationBlock(CodeLanguage lang, CodeTokenType annotNameType, String annotName, SimpleTree<CodeToken> annotParamsNode) {
		val paramChilds = annotParamsNode != null ? annotParamsNode.getChildren() : null;
		val size = paramChilds != null ? paramChilds.size() : 0;

		if(annotNameType != CodeTokenType.IDENTIFIER) { throw new IllegalArgumentException("annotation node expected to contain identifier, found '" + annotName + "'"); }

		val params = new HashMap<String, String>();
		boolean firstParamUnnamed = false;

		// parse an annotation '(arguments, ...)'
		if(size > 0) {
			val annotParamsBlock = annotParamsNode.getData();
			if(annotParamsBlock.getTokenType() != CodeTokenType.BLOCK) { throw new IllegalArgumentException("annotation node expected to contain identifier, found '" + annotParamsBlock.getText() + "'"); }

			// += 2, for the value and the separator
			for(int i = 0; i < size; i++) {
				CodeToken param = paramChilds.get(i).getData();
				CodeTokenType paramType = param.getTokenType();
				String paramName = null;

				// parse and step over named arguments, i.e. 'Annotation(id = "...")'
				if(paramType == CodeTokenType.IDENTIFIER && i < size - 2 && paramChilds.get(i + 1).getData().getTokenType() == CodeTokenType.OPERATOR && lang.getOperatorUtil().assignmentOperators().is(paramChilds.get(i + 1).getData())) {
					paramName = param.getText();
					i += 2;
					param = paramChilds.get(i).getData();
					paramType = param.getTokenType();
				}
				else {
					paramName = "arg" + (params.size() + 1);
					if(params.size() == 0) {
						firstParamUnnamed = true;
					}
				}

				// parse the annotation argument value
				// number: 'Annotation(1)' or 'Annotation(-15)'
				int num;
				if((num = DataTypeExtractor.isNumber(param, (i + 1 < size ? paramChilds.get(i + 1).getData() : null))) > 0) {
					val paramValue = param.getText() + (i + 1 < size && num > 1 ? paramChilds.get(i + 1).getData().getText() : "");
					params.put(paramName, paramValue);
					i += (num - 1);
				}
				// string: 'Annotation("str")'
				else if(paramType == CodeTokenType.STRING) {
					String valueStr = StringTrim.trimQuotes(param.getText());

					// handles concatenated strings 'Annotation(name = 'a' + 'b')
					if(i + 2 < size && lang.getOperatorUtil().concatOperators().is(paramChilds.get(i + 1).getData()) && paramChilds.get(i + 2).getData().getTokenType() == CodeTokenType.STRING) {
						valueStr = valueStr + StringTrim.trimQuotes(paramChilds.get(i + 2).getData().getText());
						i += 2;
					}

					params.put(paramName, valueStr);
				}
				else if(paramType == CodeTokenType.KEYWORD) {
					// type-literal-keyword: 'Annotation(true)'
					if(lang.getKeywordUtil().typeLiterals().is(param)) {
						params.put(paramName, param.getText());
					}
				}
				// catches other things like 'Annotation(Integer.TYPE)' or 'Annotation(String.class)'
				else if(paramType == CodeTokenType.IDENTIFIER) {
					params.put(paramName, param.getText());
				}
				else {
					throw new IllegalArgumentException("annotation param expected to start with identifier, string, number, or boolean, found " + paramType + " '" + param.getText() + "'");
				}
			}
		}
		// contains just an annotation name, no (arguments...), e.g. 'Annotation'
		else {
		}

		if(params.size() == 1 && firstParamUnnamed) {
			params.put("value", params.get("arg1"));
			params.remove("arg1");
		}
		return new AnnotationSig(annotName, NameUtil.splitFqName(annotName), params);
	}

}
