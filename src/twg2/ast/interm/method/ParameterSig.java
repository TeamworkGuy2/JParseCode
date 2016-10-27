package twg2.ast.interm.method;

import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;

/** Represents a method parameter
 * @author TeamworkGuy2
 * @since 2016-1-3
 */
@Immutable
@AllArgsConstructor
public class ParameterSig implements JsonWritableSig {
	final @Getter String name;
	final @Getter String typeSimpleName;
	final @Getter List<AccessModifier> parameterModifiers;
	final @Getter boolean optional;
	final @Getter String defaultValue;


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		val json = JsonStringify.inst;

		dst.append("{ ");
		json.toProp("type", typeSimpleName, dst);

		json.comma(dst).toProp("name", name, dst);

		json.comma(dst).propName("parameterModifiers", dst)
			.toStringArray(parameterModifiers, dst, (acs) -> acs.toSrc());

		if(optional) {
			json.comma(dst).toProp("optional", optional, dst);
		}

		if(defaultValue != null) {
			json.comma(dst).toProp("defaultValue", defaultValue, dst);
		}

		dst.append(" }");
	}


	@Override
	public String toString() {
		return typeSimpleName + " " + (optional ? "?" : "") + name + (defaultValue != null ? " = " + defaultValue : "");
	}

}
