package twg2.parser.output;

/**
 * @author TeamworkGuy2
 * @since 2015-12-10
 */
public class WriteSettings {
	public final boolean fullClassName;
	public final boolean fullFieldName;
	public final boolean fullMethodName;
	public final boolean includeEmptyAnnotationArguments;


	public WriteSettings(boolean fullClassName, boolean fullMethodName, boolean fullFieldName, boolean includeEmptyAnnotationArguments) {
		this.fullClassName = fullClassName;
		this.fullMethodName = fullMethodName;
		this.fullFieldName = fullFieldName;
		this.includeEmptyAnnotationArguments = includeEmptyAnnotationArguments;
	}

}
