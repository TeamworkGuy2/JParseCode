package baseAst.block;

import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;
import baseAst.ScopeType;
import baseAst.annotation.AnnotationSig;

/**
 * @author TeamworkGuy2
 * @since 2015-12-3
 */
@Accessors(fluent=true)
public class CodeFileAst implements BaseBlock, ClassBlock, CodeBlock, MethodBlock, NamespaceBlock {
	private @Getter ScopeType parentScope;
	private @Getter BaseBlock parent;
	private @Getter ScopeType thisScope;
	private @Getter List<BaseBlock> childs;
	private List<ClassBlock> classSubBlocks;
	private List<MethodBlock> methodSubBlocks;
	private List<CodeBlock> codeSubBlocks;
	private List<String> documentationLines;
	private List<AnnotationSig> annotations;


	/**
	 * @param parentScope
	 * @param parent
	 * @param thisScope
	 * @param childs
	 * @param classSubBlocks
	 * @param methodSubBlocks
	 * @param codeSubBlocks
	 */
	public CodeFileAst(ScopeType parentScope, BaseBlock parent, ScopeType thisScope,
			List<BaseBlock> childs, List<ClassBlock> classSubBlocks, List<MethodBlock> methodSubBlocks, List<CodeBlock> codeSubBlocks) {
		this.parentScope = parentScope;
		this.parent = parent;
		this.thisScope = thisScope;
		this.childs = childs;
		this.classSubBlocks = classSubBlocks;
		this.methodSubBlocks = methodSubBlocks;
		this.codeSubBlocks = codeSubBlocks;
	}


	@Override
	public List<ClassBlock> getClassSubBlocks() {
		return classSubBlocks;
	}


	@Override
	public List<MethodBlock> getMethodSubBlocks() {
		return methodSubBlocks;
	}


	@Override
	public List<CodeBlock> getCodeSubBlocks() {
		return codeSubBlocks;
	}


	@Override
	public List<String> getDocumentation() {
		return documentationLines;
	}


	@Override
	public List<AnnotationSig> getAnnotations() {
		return annotations;
	}

}
