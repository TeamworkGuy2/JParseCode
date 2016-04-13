package twg2.parser.codeParser.java;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.parser.baseAst.AstParser;
import twg2.parser.baseAst.tools.AstFragType;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.extractors.AnnotationExtractor;
import twg2.parser.documentParser.CodeFragment;
import twg2.parser.language.CodeLanguageOptions;
import twg2.treeLike.simpleTree.SimpleTree;

public class JavaAnnotationExtractor implements AstParser<List<AnnotationSig>> {

	static enum State {
		INIT,
		FOUND_ANNOTATION_MARK,
		FOUND_NAME,
		COMPLETE,
		FAILED;
	}


	List<AnnotationSig> annotations = new ArrayList<>();
	String foundName;
	CodeFragmentType foundNameType;
	State state = State.INIT;
	String name = "Java annotation";


	@Override
	public String name() {
		return name;
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeFragment> tokenNode) {
		val lang = CodeLanguageOptions.JAVA;

		if((state == State.COMPLETE || state == State.FAILED || state == State.FOUND_NAME) &&
				(AstFragType.isType(tokenNode.getData(), CodeFragmentType.SEPARATOR) && "@".equals(tokenNode.getData().getText()))) {
			if(state == State.FOUND_NAME) {
				val annot = AnnotationExtractor.parseAnnotationBlock(lang, foundNameType, foundName, tokenNode);
				annotations.add(annot);
				foundName = null;
				foundNameType = null;
			}
			state = State.FOUND_ANNOTATION_MARK;
			return true;
		}
		else if(state == State.FOUND_ANNOTATION_MARK) {
			if(AstFragType.isIdentifier(tokenNode.getData())) {
				foundName = tokenNode.getData().getText();
				foundNameType = tokenNode.getData().getFragmentType();
				state = State.FOUND_NAME;
				return true;
			}
			state = State.FAILED;
		}
		else if(state == State.FOUND_NAME) {
			val annot = AnnotationExtractor.parseAnnotationBlock(lang, foundNameType, foundName, tokenNode);
			annotations.add(annot);
			foundName = null;
			foundNameType = null;
			state = State.COMPLETE;
			return true;
		}
		// annotations are optional
		state = State.COMPLETE;
		return false;
	}


	@Override
	public List<AnnotationSig> getParserResult() {
		return annotations;
	}


	@Override
	public boolean isComplete() {
		return state == State.COMPLETE;
	}


	@Override
	public boolean isFailed() {
		return state == State.FAILED;
	}


	@Override
	public boolean canRecycle() {
		return true;
	}


	@Override
	public JavaAnnotationExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public JavaAnnotationExtractor copy() {
		val copy = new JavaAnnotationExtractor();
		return copy;
	}


	// package-private
	void reset() {
		annotations.clear();
		state = State.INIT;
	}

}
