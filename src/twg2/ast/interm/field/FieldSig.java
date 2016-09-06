package twg2.ast.interm.field;

import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig;
import twg2.io.write.JsonWrite;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringEscape.StringEscapeJson;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
@Immutable
@AllArgsConstructor
public class FieldSig implements JsonWritableSig {
	// package-private
	final @Getter String name;
	final @Getter List<String> fullName;
	final @Getter TypeSig.TypeSigSimple fieldType;
	final @Getter List<AccessModifier> accessModifiers;
	final @Getter List<AnnotationSig> annotations;
	final @Getter List<String> comments;


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

		dst.append("\"comments\": [");
		JsonWrite.joinStrConsume(comments, ", ", dst, (str) -> { dst.append('"'); dst.append(StringEscapeJson.toJsonString(str)); dst.append('"'); });
		dst.append("]");

		dst.append(" }");
	}


	@Override
	public String toString() {
		return fieldType + " " + NameUtil.joinFqName(fullName);
	}

}
