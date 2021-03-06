package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.List;

import twg2.ast.interm.type.TypeSig;
import twg2.functions.consumers.BooleanConsumer;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.fragment.AstFragType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.language.CodeLanguage;
import twg2.parser.primitive.NumericParser;
import twg2.parser.stateMachine.AstParserReusableBase;
import twg2.text.stringUtils.StringCheck;
import twg2.text.stringUtils.StringSplit;
import twg2.text.stringUtils.StringTrim;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-12
 */
// TODO contains various hard coded values, but as long as this is one of the centralized places containing these values, it should be fine
public class TypeExtractor extends AstParserReusableBase<TypeExtractor.State, TypeSig.TypeSigSimple> {
	public static int isPossiblyType = 0;

	static enum State {
		INIT,
		FOUND_TYPE_NAME,
		COMPLETE,
		FAILED;
	}


	private final CodeLanguage lang;
	private TypeSig.TypeSigSimple type;
	private String typeName;
	private boolean allowVoid;
	private boolean prevNodeWasBlockId;


	/**
	 * @param allowVoid indicate whether 'void'/'Void' is a valid data type when parsing (true for method return types, but invalid for field/variable types)
	 */
	public TypeExtractor(CodeLanguage lang, boolean allowVoid) {
		super(lang + " data type", State.COMPLETE, State.FAILED);
		this.lang = lang;
		this.allowVoid = allowVoid;
		this.state = State.INIT;
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeToken> tokenNode) {
		if(state == State.COMPLETE || state == State.FAILED) {
			state = State.INIT;
		}

		if(state == State.INIT && !prevNodeWasBlockId) {
			// found type name
			if(isPossiblyType(lang.getKeywordUtil(), tokenNode, allowVoid)) {
				state = State.FOUND_TYPE_NAME;
				typeName = tokenNode.getData().getText();
				prevNodeWasBlockId = lang.getKeywordUtil().blockModifiers().is(tokenNode.getData());
				return true;
			}
			state = State.INIT;
			prevNodeWasBlockId = lang.getKeywordUtil().blockModifiers().is(tokenNode.getData());
			return false;
		}
		else if(state == State.FOUND_TYPE_NAME) {
			boolean isNullable = false;
			// found optional type marker
			if(AstFragType.isOptionalTypeMarker(tokenNode.getData())) {
				isNullable = true;
			}
			this.state = State.COMPLETE;
			this.type = TypeExtractor.extractGenericTypes(typeName + (isNullable ? "?" : ""), lang.getKeywordUtil());
			prevNodeWasBlockId = lang.getKeywordUtil().blockModifiers().is(tokenNode.getData());
			return isNullable;
		}
		state = State.INIT;
		prevNodeWasBlockId = lang.getKeywordUtil().blockModifiers().is(tokenNode.getData());
		return false;
	}


	@Override
	public TypeSig.TypeSigSimple getParserResult() {
		return type;
	}


	@Override
	public TypeExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public TypeExtractor copy() {
		return new TypeExtractor(this.lang, this.allowVoid);
	}


	// package-private
	void reset() {
		type = null;
		typeName = null;
		state = State.INIT;
	}


	/** Check if a string is possibly a simple data type (just a type name, no generics)
	 */
	public static <T> boolean isPossiblyType(KeywordUtil<? extends Keyword> keywordUtil, String typeName, boolean allowVoid) {
		return !StringCheck.isNullOrWhitespace(typeName) && (!keywordUtil.isKeyword(typeName) || keywordUtil.isDataTypeKeyword(typeName)) || (allowVoid ? "void".equalsIgnoreCase(typeName) : false);
	}


	/** Check if a tree node is possibly a data type (just a type name, no generics)
	 */
	public static <T> boolean isPossiblyType(KeywordUtil<? extends Keyword> keywordUtil, SimpleTree<CodeToken> node, boolean allowVoid) {
		isPossiblyType++;
		var nodeData = node.getData();
		return AstFragType.isIdentifierOrKeyword(nodeData) && (!keywordUtil.isKeyword(nodeData.getText()) || keywordUtil.isDataTypeKeyword(nodeData.getText())) || (allowVoid ? "void".equalsIgnoreCase(nodeData.getText()) : false);
	}


	/** Check if a {@link CodeToken} is a boolean literal
	 */
	public static boolean isBooleanLiteral(CodeToken node) {
		return node.getTokenType() == CodeTokenType.KEYWORD && ("true".equals(node.getText()) || "false".equals(node.getText()));
	}


	/** Check if a {@link CodeToken} is a null literal
	 */
	public static boolean isNullLiteral(CodeToken node) {
		return node.getTokenType() == CodeTokenType.KEYWORD && "null".equals(node.getText());
	}


	/** Check if a {@link CodeToken} is a numeric literal, a boolean literal, or a null literal
	 */
	public static boolean isDefaultValueLiteral(CodeToken node) {
		return (node.getTokenType() == CodeTokenType.NUMBER || TypeExtractor.isBooleanLiteral(node) || TypeExtractor.isNullLiteral(node));
	}


	/** Check if one or two nodes are a number with an optional -/+ sign
	 * @param node1 the numeric token or optional a sign (i.e. '-' or '+')
	 * @param node2Optional if the first node is a sign, then this node is the numeric token
	 * @return the number of nodes used, 0 if neither node was a number, 1 if the first node was a number, 2 if the first node was a sign and the second node was a number
	 */
	public static int isNumber(CodeToken node1, CodeToken node2Optional) {
		String n1Text;
		int matches = node1.getTokenType() == CodeTokenType.NUMBER ? 1 : 0;
		if(matches > 0) {
			return matches;
		}
		matches = (node2Optional != null && (n1Text = node1.getText()).length() == 1 && NumericParser.isSign.test(n1Text.charAt(0)) && node2Optional.getTokenType() == CodeTokenType.NUMBER) ? 2 : 0;
		return matches;
	}


	/** Whether a set of code tokens represent a simple type (numeric, boolean, or null literal) and return the source code representation of those tokens concatenated together
	 * @param astNodes the list of simple tree {@link CodeToken}s to check
	 * @param isNotString an output flag, the flag is set if this call returns a non-null value,
	 * if set to true then the returned value represents numeric, boolean, or null,
	 * otherwise if set to false then the returned value represents a string literal
	 * @return a source code string representation of the tokens if they represent a number, boolean, string, or null literal, null otherwise
	 */
	public static String isSimpleLiteral(List<SimpleTree<CodeToken>> astNodes, BooleanConsumer isNotString) {
		CodeToken data = null;
		CodeToken data2 = null;
		boolean isNumOrBoolOrNull = false;
		int astNodeCount = astNodes != null ? astNodes.size() : 0;
		if(astNodeCount == 1 && (data = astNodes.get(0).getData()) != null && (data.getTokenType() == CodeTokenType.STRING || (isNumOrBoolOrNull = TypeExtractor.isDefaultValueLiteral(data)))) {
			isNotString.accept(isNumOrBoolOrNull);
			return data.getText();
		}
		else if (astNodeCount == 2 && (data = astNodes.get(0).getData()) != null && TypeExtractor.isNumber(data, (data2 = astNodes.get(1).getData())) > 0) {
			isNotString.accept(true);
			return data.getText() + data2.getText();
		}
		else {
			return null;
		}
	}


	/** Parse a generic type signature (i.e. {@code Map<String, String>}) to a {@link TypeSig.TypeSigSimple}.
	 * @param typeSig the type signature to parse (i.e. {@code Tuple<List<String>, Map<String, List<Integer>>>})
	 * @param keywordUtil the {@link KeywordUtil} instance for the type of language being parsed
	 * @return A list of simple types parsed from the generic parameters of the signature
	 */
	public static TypeSig.TypeSigSimple extractGenericTypes(String typeSig, KeywordUtil<? extends Keyword> keywordUtil) {
		String genericMark = "#";

		if(typeSig.contains(genericMark)) {
			throw new IllegalArgumentException("cannot parse a type signature containing '" + genericMark + "' (because this is a simple parser implementation)");
		}

		var sb = new StringBuilder(typeSig);
		var genericParamSets = new ArrayList<String>();
		int i = 0;
		while(true) {
			var paramSet = extractFirstClosestPair(sb, "<", ">", genericMark + i);
			if(paramSet == null) {
				break;
			}
			if(!paramSet.startsWith("<") || !paramSet.endsWith(">")) {
				throw new IllegalStateException("invalid generic type parameter list '" + paramSet + "'");
			}
			String paramSetStr = paramSet.substring(1, paramSet.length() - 1);
			genericParamSets.add(paramSetStr);
			i++;
		}

		// convert the generic parameters to TypeSig nested
		// ex: BaseType<A, B>[][]?
		// ex: BaseType[]
		var rootNameAndMarker = StringSplit.firstMatchParts(sb.toString(), '#'); // ex: ('BaseType', '#0[][]?') OR ('BaseType[]', '')
		String rootName = rootNameAndMarker.getKey(); // 'BaseType' OR 'BaseType[]'
		String markerAndMeta = rootNameAndMarker.getValue(); // '#0[][]?' OR ''
		String typeMeta = markerAndMeta.length() > 0 ? markerAndMeta : rootName; // '#0[][]?' OR 'BaseType[]'
		String paramValue = StringTrim.trimTrailing(typeMeta, '?'); // '#0[][]' OR BaseType[]'
		boolean isOptional = paramValue.length() < typeMeta.length();
		var valueAndArrayDimensions = StringTrim.countAndTrimTrailing(paramValue, "[]", true); // (2, '#0') OR (1, 'BaseType')
		var typeName = typeMeta == rootName ? valueAndArrayDimensions.getValue() : StringTrim.trimTrailing(rootName, "[]", true); // 'BaseType' or 'BaseType'
		var root = new TypeSig.TypeSigSimpleBase(typeName, valueAndArrayDimensions.getKey(), isOptional, keywordUtil.isPrimitive(typeName));
		TypeSig.TypeSigSimple sig;

		int rootMarker = !StringCheck.isNullOrEmpty(markerAndMeta) ? Integer.parseInt(valueAndArrayDimensions.getValue()) : -1;
		if(rootMarker > -1) {
			var sigChilds = expandGenericParamSet(keywordUtil, rootMarker, genericParamSets);
			sig = new TypeSig.TypeSigSimpleGeneric(root.getTypeName(), sigChilds, root.getArrayDimensions(), root.isNullable(), keywordUtil.isPrimitive(root.getTypeName()));
		}
		else {
			sig = root;
		}

		return sig;
	}


	/** Recursively convert a generic type signature (i.e. {@code Tuple<List<String>, List<String>>}) to a list of nested {@link TypeSig.TypeSigSimple}.
	 * 'remainingParamSets' contains the parameter sets split up by '<>' pairs.
	 * Example: {@code Tuple<String, String>} has one parameter set {@code <String, String>}.
	 * Whereas {@code Tuple<String, Map<String, List<Integer>>>} has two parameter sets {@code <String, Map<String, List<Integer>>>} and {@code <String, List<Integer>>}.<br>
	 * Each parameter set is marked by a number and this method looks up those numbers as indexes into 'remainingParamSets' and recursively splits
	 * each parameter set string at ',' and converts the resulting identifiers/data types to a list of nested {@link TypeSig.TypeSigSimple}.
	 * @param keywordUtil the {@link KeywordUtil} instance for the type of language being parsed
	 * @param parentParamMarker the 'remainingParamSets' marker index being expanded by this recursive call
	 * @param paramSets a inner-nested-to-outer-nested left-to-right list of generics extracted from the original type signature
	 * @return A list of simple types parsed from the generic parameters of the signature
	 */
	public static List<TypeSig.TypeSigSimple> expandGenericParamSet(KeywordUtil<? extends Keyword> keywordUtil, int parentParamMarker, List<String> paramSets) {
		String paramSetStr = paramSets.get(parentParamMarker);
		var params = paramSetStr.split(", ");
		var paramSigs = new ArrayList<TypeSig.TypeSigSimple>(params.length);

		for(String param : params) {
			// Split the generic parameter name and possible marker indicating further nested generic type
			var paramNameAndMarker = StringSplit.firstMatchParts(param, '#');

			// Create basic generic parameter using the name
			String paramName = StringTrim.trimTrailing(paramNameAndMarker.getKey(), '?');
			var nameAndArrayDimensions = StringTrim.countAndTrimTrailing(paramName, "[]", true);
			boolean isOptional = paramNameAndMarker.getKey().endsWith("?");
			var paramSigInit = new TypeSig.TypeSigSimpleBase(nameAndArrayDimensions.getValue(), nameAndArrayDimensions.getKey(), isOptional, keywordUtil.isPrimitive(nameAndArrayDimensions.getValue()));
			TypeSig.TypeSigSimple paramSig;

			// if this generic parameter has a marker, parse it's sub parameters and add them to a new compound generic type signature
			int paramMarker = !StringCheck.isNullOrEmpty(paramNameAndMarker.getValue()) ? Integer.parseInt(paramNameAndMarker.getValue()) : -1;
			if(paramMarker > -1) {
				var childParams = expandGenericParamSet(keywordUtil, paramMarker, paramSets);
				paramSig = new TypeSig.TypeSigSimpleGeneric(paramSigInit.getTypeName(), childParams, paramSigInit.getArrayDimensions(), paramSigInit.isNullable(), keywordUtil.isPrimitive(paramSigInit.getTypeName()));
			}
			// else just use the generic parameter's basic signature (no nested generic types)
			else {
				paramSig = paramSigInit;
			}

			paramSigs.add(paramSig);
		}
		return paramSigs;
	}


	public static String extractFirstClosestPair(StringBuilder src, String start, String end, String replace) {
		int endI = src.indexOf(end);
		int startI = endI > -1 ? src.substring(0, endI).lastIndexOf(start) : -1;
		if(startI > -1 && endI > -1) {
			String res = src.substring(startI, endI + end.length());
			src.replace(startI, endI + end.length(), replace);
			return res;
		}
		else if(startI > -1 || endI > -1) {
			throw new IllegalArgumentException("remaining type signature '" + src.toString() + "' invalid, contains " +
					(startI > -1 ? "start '" + start + "', but no end '" + end + "'" : "end '" + start + "', but no start '" + end + "'"));
		}
		return null;
	}

}
