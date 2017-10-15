package twg2.ast.interm.annotation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;

@Immutable
@AllArgsConstructor
public class AnnotationSig implements JsonWritableSig {
	public final String name;
	public final List<String> fullName;
	public final Map<String, String> arguments;


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		dst.append(" {");
		dst.append("\"name\": \"" + NameUtil.joinFqName(fullName) + "\"");

		if(st.includeEmptyAnnotationArguments || arguments.size() > 0) {
			dst.append(", ");
			dst.append("\"arguments\": { ");
			boolean notFirst = false;
			for(val argumentEntry : arguments.entrySet()) {
				// TODO Csv style escape
				dst.append((notFirst ? ", " : "") + '"' + argumentEntry.getKey() + "\": \"" + argumentEntry.getValue().replace("\"", "\\\"") + '"');
				notFirst = true;
			}
			dst.append(" }");
		}

		dst.append(" }");
	}


	@Override
	public String toString() {
		return "annotation: " + name + "(" + arguments + ")";
	}

}
