package baseAst.annotation;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import twg2.annotations.Immutable;

@Immutable
@AllArgsConstructor
public class AnnotationSig {
	private final @Getter String name;
	private final @Getter List<String> fullyQualifyingName;
	private final @Getter Map<String, String> arguments;


	@Override
	public String toString() {
		return "annotation: " + name + "(" + arguments + ")";
	}

}
