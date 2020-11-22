package twg2.ast.interm.field;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig.TypeSigSimple;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.extractors.TypeExtractor;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.output.WriteSettings;
import twg2.text.stringEscape.StringEscapeJson;
import twg2.treeLike.simpleTree.SimpleTree;

/** A {@link FieldSig} with an initializer
 * @author TeamworkGuy2
 * @since 2016-09-04
 */
@Immutable
public class FieldDef extends FieldSig {
	private final @Getter SimpleTree<CodeToken> initializer;


	public FieldDef(String name, List<String> fullName, TypeSigSimple fieldType, List<Keyword> accessModifiers,
			List<AnnotationSig> annotations, List<String> comments, SimpleTree<CodeToken> initializer) {
		super(name, fullName, fieldType, accessModifiers, annotations, comments);
		this.initializer = initializer;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		var json = JsonStringify.inst;

		dst.append("{ ");
		json.toProp("name", (st.fullFieldName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)), dst);

		json.comma(dst).propName("type", dst);
		fieldType.toJson(dst, st);

		json.comma(dst).propName("accessModifiers", dst)
			.toStringArray(accessModifiers, dst, (acs) -> acs.toSrc());

		if(annotations != null && annotations.size() > 0) {
			json.comma(dst).propName("annotations", dst)
				.toArrayConsume(annotations, dst, (ann) -> ann.toJson(dst, st));
		}

		initializerToJson(initializer, true, dst, st);

		json.comma(dst).propName("comments", dst)
			.toStringArray(comments, dst);

		dst.append(" }");
	}


	@Override
	public String toString() {
		return fieldType + " " + NameUtil.joinFqName(fullName);
	}


	/** Write a field initializer to a JSON field named 'initializer' if the value is a number, boolean, string, or null literal, else write it to a field named 'initializerExpression'
	 * @throws IOException
	 */
	public static void initializerToJson(SimpleTree<CodeToken> astNode, boolean preClosingComma, Appendable dst, WriteSettings st) throws IOException {
		CodeToken data = null;
		boolean isNumOrBoolOrNull = false;
		if(astNode != null && !astNode.hasChildren() && (data = astNode.getData()) != null &&
				(data.getTokenType() == CodeTokenType.STRING ||
				(isNumOrBoolOrNull = TypeExtractor.isDefaultValueLiteral(data)))) {
			if(preClosingComma) {
				dst.append(", ");
			}
			dst.append("\"initializer\": ");
			if(isNumOrBoolOrNull) {
				dst.append(data.getText());
			}
			else {
				dst.append('"');
				dst.append(StringEscapeJson.toJsonString(data.getText()));
				dst.append('"');
			}
		}
		else if(astNode != null && astNode.hasChildren()) {
			if(preClosingComma) {
				dst.append(", ");
			}
			dst.append("\"initializerExpression\": ");
			if((data = astNode.getData()) != null && !TypeExtractor.isNullLiteral(data)) {
				dst.append('"');
				dst.append(StringEscapeJson.toJsonString(data.getText()));
				dst.append('"');
			}
			else {
				dst.append(null);
			}
		}
	}

}
