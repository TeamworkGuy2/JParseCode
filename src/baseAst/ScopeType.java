package baseAst;

/**
 * @author TeamworkGuy2
 * @since 2015-12-3
 */
public enum ScopeType {
	/** includes C# namespaces and TypeScript namespaces AND modules */
	NAMESPACE,
	/** includes interfaces, enums, annotations */
	CLASS,
	/** includes constructors, static and instance initializers, and NAMESPACE level functions in TypeScript */
	METHOD,
	/** a METHOD not declared in the root of a CLASS (i.e. an anonymous function declared in a method */
	LAMBDA,
	/** a chunk of code, not falling into any of the other categories in this enum */
	CODE;
}
