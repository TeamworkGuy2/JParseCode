package twg2.ast.interm.field;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig.TypeSigResolved;
import twg2.io.write.JsonWrite;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.CodeFragment;
import twg2.parser.output.WriteSettings;
import twg2.text.stringEscape.StringEscapeJson;
import twg2.treeLike.simpleTree.SimpleTree;

/** A {@link FieldSigResolved} with an initializer
 * @author TeamworkGuy2
 * @since 2016-09-04
 */
@Immutable
public class FieldDefResolved extends FieldSigResolved {
	private final @Getter SimpleTree<CodeFragment> initializer;


	public FieldDefResolved(String name, List<String> fullName, TypeSigResolved fieldType, List<AccessModifier> accessModifiers,
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

		FieldDef.initializerToJson(initializer, dst, st);

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
