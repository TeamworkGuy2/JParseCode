package twg2.parser.intermAst.field;

import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.parser.baseAst.annotation.AnnotationSig;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.intermAst.type.TypeSig;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
@Immutable
@AllArgsConstructor
public class IntermFieldSig implements JsonWritableSig {
	private final @Getter String name;
	private final @Getter List<String> fullyQualifyingName;
	private final @Getter TypeSig fieldType;
	private final @Getter List<AnnotationSig> annotations;


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		dst.append("{ ");
		dst.append("\"name\": \"" + (st.fullFieldName ? NameUtil.joinFqName(fullyQualifyingName) : fullyQualifyingName.get(fullyQualifyingName.size() - 1)) + "\", ");
		dst.append("\"type\": \"" + fieldType + "\"");
		dst.append(" }");
	}


	@Override
	public String toString() {
		return fieldType + " " + NameUtil.joinFqName(fullyQualifyingName);
	}

}
