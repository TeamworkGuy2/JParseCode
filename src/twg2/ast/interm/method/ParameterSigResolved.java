package twg2.ast.interm.method;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringEscape.StringEscapeJson;


/**
 * @author TeamworkGuy2
 * @since 2016-1-3
 */
@Immutable
@AllArgsConstructor
public class ParameterSigResolved implements JsonWritableSig {
	private @Getter String name;
	private @Getter TypeSig.TypeSigResolved type;
	private @Getter boolean optional;
	private @Getter String defaultValue;


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		dst.append(" {");

		dst.append("\"type\": ");
		type.toJson(dst, st);
		dst.append(", ");

		dst.append("\"name\": \"" + name + "\"");

		if(optional) {
			dst.append(", ");
			dst.append("\"optional\": " + optional);
		}

		if(defaultValue != null) {
			dst.append(", ");
			dst.append("\"defaultValue\": \"");
			StringEscapeJson.toJsonString(defaultValue, 0, defaultValue.length(), dst);
			dst.append("\"");
		}

		dst.append(" }");
	}


	@Override
	public String toString() {
		return type.toString() + " " + (optional ? "?" : "") + name + (defaultValue != null ? " = " + defaultValue : "");
	}

}

