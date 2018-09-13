package twg2.parser.codeParser.java;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.parser.codeParser.extractors.AnnotationExtractor;
import twg2.parser.fragment.AstFragType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.stateMachine.AstParserReusableBase;
import twg2.treeLike.simpleTree.SimpleTree;

public class JavaAnnotationExtractor extends AstParserReusableBase<JavaAnnotationExtractor.State, List<AnnotationSig>> {

	static enum State {
		INIT,
		FOUND_ANNOTATION_MARK,
		FOUND_NAME,
		COMPLETE,
		FAILED;
	}


	List<AnnotationSig> annotations = new ArrayList<>();
	String foundName;
	CodeTokenType foundNameType;


	public JavaAnnotationExtractor() {
		super("Java annotation", State.COMPLETE, State.FAILED);
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeToken> tokenNode) {
		val lang = CodeLanguageOptions.JAVA;

		if(state != State.FOUND_ANNOTATION_MARK && AstFragType.isSeparator(tokenNode.getData(), "@")) {
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
				foundNameType = tokenNode.getData().getTokenType();
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
