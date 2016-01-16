package twg2.parser.intermAst.annotation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;

@Immutable
@AllArgsConstructor
public class AnnotationSig implements JsonWritableSig {
	private final @Getter String name;
	private final @Getter List<String> fullName;
	private final @Getter Map<String, String> arguments;


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		dst.append(" {");
		dst.append("\"name\": \"" + NameUtil.joinFqName(fullName) + "\", ");

		dst.append("\"arguments\": { ");
		boolean notFirst = false;
		for(val argumentEntry : arguments.entrySet()) {
			// TODO Csv style escape
			dst.append((notFirst ? ", " : "") + '"' + argumentEntry.getKey() + "\": \"" + argumentEntry.getValue().replace("\"", "\\\"") + '"');
			notFirst = true;
		}
		dst.append(" }");

		dst.append(" }");
	}


	@Override
	public String toString() {
		return "annotation: " + name + "(" + arguments + ")";
	}

}
