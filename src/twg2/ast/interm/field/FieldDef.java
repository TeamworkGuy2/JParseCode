package twg2.ast.interm.field;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig.TypeSigSimple;
import twg2.io.write.JsonWrite;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.extractors.DataTypeExtractor;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.documentParser.CodeFragment;
import twg2.parser.fragment.CodeFragmentType;
import twg2.parser.output.WriteSettings;
import twg2.text.stringEscape.StringEscapeJson;
import twg2.treeLike.simpleTree.SimpleTree;

/** A {@link FieldSig} with an initializer
 * @author TeamworkGuy2
 * @since 2016-09-04
 */
@Immutable
public class FieldDef extends FieldSig {
	private final @Getter SimpleTree<CodeFragment> initializer;


	public FieldDef(String name, List<String> fullName, TypeSigSimple fieldType, List<AccessModifier> accessModifiers,
			List<AnnotationSig> annotations, List<String> comments, SimpleTree<CodeFragment> initializer) {
		super(name, fullName, fieldType, accessModifiers, annotations, comments);
		this.initializer = initializer;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		dst.append("{ ");
		dst.append("\"name\": \"" + (st.fullFieldName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)) + "\", ");

		dst.append("\"type\": ");
		fieldType.toJson(dst, st);
		dst.append(", ");

		dst.append("\"accessModifiers\": [");
		JsonWrite.joinStr(accessModifiers, ", ", dst, (acs) -> '"' + acs.toSrc() + '"');
		dst.append("], ");

		dst.append("\"annotations\": [");
		JsonWrite.joinStrConsume(annotations, ", ", dst, (ann) -> ann.toJson(dst, st));
		dst.append("], ");

		initializerToJson(initializer, dst, st);

		dst.append("\"comments\": [");
		JsonWrite.joinStrConsume(comments, ", ", dst, (str) -> { dst.append('"'); dst.append(StringEscapeJson.toJsonString(str)); dst.append('"'); });
		dst.append("]");

		dst.append(" }");
	}


	@Override
	public String toString() {
		return fieldType + " " + NameUtil.joinFqName(fullName);
	}


	/** Write a field initializer to a JSON field named 'initializer' if the value is a number, boolean, string, or null literal, else write it to a field named 'initializerExpression'
	 * @throws IOException
	 */
	public static void initializerToJson(SimpleTree<CodeFragment> astNode, Appendable dst, WriteSettings st) throws IOException {
		CodeFragment data = null;
		boolean isNumOrBoolOrNull = false;
		if(astNode != null && !astNode.hasChildren() && (data = astNode.getData()) != null &&
				(data.getFragmentType() == CodeFragmentType.STRING ||
				(isNumOrBoolOrNull = (data.getFragmentType() == CodeFragmentType.NUMBER || DataTypeExtractor.isBooleanLiteral(data) || DataTypeExtractor.isNullLiteral(data))))) {
			dst.append("\"initializer\": ");
			if(isNumOrBoolOrNull) {
				dst.append(data.getText());
			}
			else {
				dst.append('"');
				dst.append(StringEscapeJson.toJsonString(data.getText()));
				dst.append('"');
			}
			dst.append(", ");
		}
		else if(astNode != null && astNode.hasChildren()) {
			dst.append("\"initializerExpression\": ");
			if((data = astNode.getData()) != null && !DataTypeExtractor.isNullLiteral(data)) {
				dst.append('"');
				dst.append(StringEscapeJson.toJsonString(data.getText()));
				dst.append('"');
			}
			else {
				dst.append(null);
			}
			dst.append(", ");
		}
	}

}
