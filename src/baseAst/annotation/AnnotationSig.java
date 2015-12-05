package baseAst.annotation;

import java.util.Map;

import lombok.Getter;

public class AnnotationSig {
	private @Getter String name;
	private @Getter Map<String, String> arguments;

	/**
	 * @param name
	 * @param arguments
	 */
	public AnnotationSig(String name, Map<String, String> arguments) {
		this.name = name;
		this.arguments = arguments;
	}


	@Override
	public String toString() {
		return "annotation: " + name + "(" + arguments + ")";
	}

}
