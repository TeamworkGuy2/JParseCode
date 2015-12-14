package output;

/**
 * @author TeamworkGuy2
 * @since 2015-12-10
 */
public class WriteSettings {
	public final boolean fullClassName;
	public final boolean fullFieldName;
	public final boolean fullMethodName;


	public WriteSettings(boolean fullClassName, boolean fullMethodName, boolean fullFieldName) {
		this.fullClassName = fullClassName;
		this.fullMethodName = fullMethodName;
		this.fullFieldName = fullFieldName;
	}

}
