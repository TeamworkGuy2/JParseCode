package twg2.parser.intermAst.type;

import java.util.List;

import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.parser.codeParser.csharp.CsKeyword;

/**
 * @author TeamworkGuy2
 * @since 2015-12-12
 */
@Immutable
public class TypeSig {
	private final @Getter String name;
	private final @Getter List<String> fullyQualifyingName;
	private final @Getter boolean nullable;
	private final @Getter boolean primitive;


	public TypeSig(List<String> fullyQualifyingName, boolean nullable) {
		this.name = fullyQualifyingName.get(fullyQualifyingName.size() - 1);
		this.fullyQualifyingName = fullyQualifyingName;
		this.nullable = nullable;
		this.primitive = CsKeyword.isPrimitive(name);
	}


	@Override
	public String toString() {
		return name + (nullable ? "?" : "");
	}

}
