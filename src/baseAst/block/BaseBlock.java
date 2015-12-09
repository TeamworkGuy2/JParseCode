package baseAst.block;

import java.util.List;

import baseAst.ScopeType;
import baseAst.annotation.AnnotationSig;

/**
 * @author TeamworkGuy2
 * @since 2015-12-3
 */
public interface BaseBlock {

	public String getSimpleName();

	public List<String> getFullyQualifiedName();

	public ScopeType parentScope();

	public BaseBlock parent();

	public ScopeType thisScope();

	public List<BaseBlock> childs();

	public List<ClassBlock> getClassSubBlocks();

	public List<MethodBlock> getMethodSubBlocks();

	public List<CodeBlock> getCodeSubBlocks();


	// ==== utility methods ====
	public List<String> getDocumentation();

	public List<AnnotationSig> getAnnotations();
}
