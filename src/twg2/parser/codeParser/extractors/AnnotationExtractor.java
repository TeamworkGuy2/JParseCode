package twg2.parser.codeParser.extractors;

import java.util.HashMap;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.collections.interfaces.ListReadOnly;
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
		var paramChilds = annotParamsNode != null ? annotParamsNode.getChildren() : null;
		int size = paramChilds != null ? paramChilds.size() : 0;

		if(annotNameType != CodeTokenType.IDENTIFIER) {
			throw new IllegalArgumentException("annotation node expected to contain identifier, found '" + annotName + "'");
		}

		var params = new HashMap<String, String>();
		boolean firstParamUnnamed = false;

		// parse an annotation '(arguments, ...)'
		if(size > 0) {
			var operatorUtil = lang.getOperatorUtil();
			var annotParamsBlock = annotParamsNode.getData();
			if(annotParamsBlock.getTokenType() != CodeTokenType.BLOCK) {
				throw new IllegalArgumentException("annotation node expected to contain identifier, found " + annotParamsBlock.getTokenType() + " '" + annotParamsBlock.getText() + "'");
			}

			// += 2, for the value and the separator
			for(int i = 0; i < size; i++) {
				CodeToken param = paramChilds.get(i).getData();
				CodeTokenType paramType = param.getTokenType();
				String paramName = null;

				// parse and step over named arguments, i.e. 'Annotation(id = "...")'
				if(paramType == CodeTokenType.IDENTIFIER && i < size - 2 && paramChilds.get(i + 1).getData().getTokenType() == CodeTokenType.OPERATOR && operatorUtil.assignmentOperators().is(paramChilds.get(i + 1).getData())) {
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
				i += parseAnnotationArgument(lang, param, paramName, paramType, i, size, paramChilds, params);
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


	private static int parseAnnotationArgument(CodeLanguage lang, CodeToken param, String paramName, CodeTokenType paramType, int i, int size, ListReadOnly<SimpleTree<CodeToken>> paramChilds, HashMap<String, String> dstParams) {
		// number: 'Annotation(1)' or 'Annotation(-15)'
		int num;
		if((num = DataTypeExtractor.isNumber(param, (i + 1 < size ? paramChilds.get(i + 1).getData() : null))) > 0) {
			String paramValue = param.getText() + (i + 1 < size && num > 1 ? paramChilds.get(i + 1).getData().getText() : "");
			dstParams.put(paramName, paramValue);
			return (num - 1);
		}
		// string: 'Annotation("str")'
		else if(paramType == CodeTokenType.STRING) {
			String valueStr = StringTrim.trimQuotes(param.getText());

			// handle concatenated strings 'Annotation(name = 'a' + 'b')
			if(i + 2 < size && lang.getOperatorUtil().concatOperators().is(paramChilds.get(i + 1).getData()) && paramChilds.get(i + 2).getData().getTokenType() == CodeTokenType.STRING) {
				valueStr = valueStr + StringTrim.trimQuotes(paramChilds.get(i + 2).getData().getText());
				dstParams.put(paramName, valueStr);
				return 2;
			}
			else {
				dstParams.put(paramName, valueStr);
				return 0;
			}
		}
		// keyword-or-identifier followed by constant-block-expression: 'Annotation(typeof(String))' (for C# default(T), nameof(T), and typeof(T))
		else if((paramType == CodeTokenType.KEYWORD || paramType == CodeTokenType.IDENTIFIER) && i + 1 < size && CodeTokenType.BLOCK == paramChilds.get(i + 1).getData().getTokenType()) {
			dstParams.put(paramName, param.getText() + paramChilds.get(i + 1).getData().getText());
			return 1;
		}
		// type-literal-keyword: 'Annotation(true)'
		else if(paramType == CodeTokenType.KEYWORD && lang.getKeywordUtil().typeLiterals().is(param)) {
			dstParams.put(paramName, param.getText());
			return 0;
		}
		// catches other things like 'Annotation(Integer.TYPE)' or 'Annotation(String.class)'
		else if(paramType == CodeTokenType.IDENTIFIER) {
			dstParams.put(paramName, param.getText());
			return 0;
		}
		else {
			throw new IllegalArgumentException("annotation param expected to start with identifier, string, number, or boolean, found " + paramType + " '" + param.getText() + "'");
		}
	}

}
