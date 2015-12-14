package intermAst.classes;

import java.io.IOException;
import java.util.List;

import output.JsonWritableSig;
import output.WriteSettings;
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
public class IntermClassSig implements JsonWritableSig {
	private final @Getter AccessModifier accessModifier;
	private final @Getter List<String> fullyQualifyingName;
	private final @Getter String declarationType;


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		dst.append("{ ");
		dst.append("\"access\": \"" + accessModifier + "\", ");
		dst.append("\"name\": \"" + (st.fullClassName ? NameUtil.joinFqName(fullyQualifyingName) : fullyQualifyingName.get(fullyQualifyingName.size() - 1)) + "\", ");
		dst.append("\"declarationType\": \"" + declarationType + "\"");
		dst.append(" }");
	}


	@Override
	public String toString() {
		return accessModifier.toSrc() + " " + declarationType + " " + NameUtil.joinFqName(fullyQualifyingName);
	}

}
