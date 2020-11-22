package twg2.ast.interm.method;

import java.util.List;

import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.codeParser.Keyword;

@Immutable
public class MethodSigSimple extends MethodSig<ParameterSig, TypeSig.TypeSigSimple> {

	public MethodSigSimple(String name, List<String> fullName, List<? extends ParameterSig> paramSigs,
			TypeSig.TypeSigSimple returnType, List<? extends Keyword> accessModifiers, List<? extends TypeSig.TypeSigSimple> typeParameters, List<? extends AnnotationSig> annotations, List<String> comments) {
		super(name, fullName, paramSigs, returnType, accessModifiers, typeParameters, annotations, comments);
	}

}