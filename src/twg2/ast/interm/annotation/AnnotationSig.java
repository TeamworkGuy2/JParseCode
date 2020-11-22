package twg2.ast.interm.annotation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import twg2.annotations.Immutable;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;

@Immutable
public class AnnotationSig implements JsonWritableSig {
	public final String name;
	public final List<String> fullName;
	public final Map<String, String> arguments;


	public AnnotationSig(String name, List<String> fullName, Map<String, String> arguments) {
		this.name = name;
		this.fullName = fullName;
		this.arguments = arguments;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		dst.append(" {");
		dst.append("\"name\": \"" + NameUtil.joinFqName(fullName) + "\"");

		if(st.includeEmptyAnnotationArguments || arguments.size() > 0) {
			dst.append(", ");
			dst.append("\"arguments\": { ");
			boolean notFirst = false;
			for(Map.Entry<String, String> argumentEntry : arguments.entrySet()) {
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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (arguments == null ? 0 : arguments.hashCode());
		result = prime * result + (fullName == null ? 0 : fullName.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || this.getClass() != obj.getClass())
			return false;

		AnnotationSig other = (AnnotationSig) obj;
		if (arguments == null) {
			if (other.arguments != null)
				return false;
		}
		else if (!arguments.equals(other.arguments))
			return false;

		if (fullName == null) {
			if (other.fullName != null)
				return false;
		}
		else if (!fullName.equals(other.fullName))
			return false;

		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;

		return true;
	}

}
