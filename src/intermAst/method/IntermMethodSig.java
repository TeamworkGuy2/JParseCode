package intermAst.method;

import java.io.IOException;
import java.util.List;

import baseAst.util.NameUtil;
import twg2.annotations.Immutable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
@Immutable
@AllArgsConstructor
public class IntermMethodSig {
	private final @Getter String name;
	private final @Getter List<String> fullyQualifyingName;
	private final @Getter String paramsSig;
	private final @Getter String returnType;


	public void toJson(Appendable dst) throws IOException {
		dst.append("{ ");
		dst.append("\"name\": \"" + NameUtil.joinFqName(fullyQualifyingName) + "\", ");
		dst.append("\"parametersSignature\": \"" + paramsSig.replace('\n', ' ').replace("\"", "\\\"") + "\", ");
		dst.append("\"returnType\": \"" + returnType + "\"");
		dst.append(" }");
	}


	@Override
	public String toString() {
		return returnType + " " + NameUtil.joinFqName(fullyQualifyingName) + paramsSig;
	}

}
