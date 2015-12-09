package intermAst.field;

import java.io.IOException;
import java.util.List;

import baseAst.util.NameUtil;
import twg2.annotations.Immutable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
@Immutable
@AllArgsConstructor
public class IntermFieldSig {
	private final @Getter String name;
	private final @Getter List<String> fullyQualifyingName;
	private final @Getter String fieldType;


	public void toJson(Appendable dst) throws IOException {
		dst.append("{ ");
		dst.append("\"name\": \"" + NameUtil.joinFqName(fullyQualifyingName) + "\", ");
		dst.append("\"type\": \"" + fieldType.replace('\n', ' ').replace("\"", "\\\"") + "\"");
		dst.append(" }");
	}


	@Override
	public String toString() {
		return fieldType + " " + NameUtil.joinFqName(fullyQualifyingName);
	}

}
