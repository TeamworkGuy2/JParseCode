package intermAst.classes;

import java.io.IOException;
import java.util.List;

import twg2.annotations.Immutable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import baseAst.AccessModifier;
import baseAst.util.NameUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
@Immutable
@AllArgsConstructor
public class IntermClassSig {
	private final @Getter AccessModifier accessModifier;
	private final @Getter List<String> fullyQualifyingName;
	private final @Getter String declarationType;


	public void toJson(Appendable dst) throws IOException {
		dst.append("{ ");
		dst.append("\"access\": \"" + accessModifier + "\", ");
		dst.append("\"name\": \"" + NameUtil.joinFqName(fullyQualifyingName) + "\", ");
		dst.append("\"declarationType\": \"" + declarationType + "\"");
		dst.append(" }");
	}


	@Override
	public String toString() {
		return accessModifier.toSrc() + " " + declarationType + " " + NameUtil.joinFqName(fullyQualifyingName);
	}

}
