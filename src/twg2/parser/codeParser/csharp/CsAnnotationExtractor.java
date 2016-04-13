package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.parser.baseAst.AstParser;
import twg2.parser.baseAst.tools.AstFragType;
import twg2.parser.codeParser.extractors.AnnotationExtractor;
import twg2.parser.documentParser.CodeFragment;
import twg2.parser.language.CodeLanguageOptions;
import twg2.treeLike.simpleTree.SimpleTree;

public class CsAnnotationExtractor implements AstParser<List<AnnotationSig>> {

	static enum State {
		INIT,
		COMPLETE,
		FAILED;
	}


	List<AnnotationSig> annotations = new ArrayList<>();
	State state = State.INIT;
	String name = "C# annotation";


	@Override
	public String name() {
		return name;
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeFragment> tokenNode) {
		val lang = CodeLanguageOptions.C_SHARP;

		if(state != State.FAILED) {
			val childs = tokenNode.getChildren();
			if(AstFragType.isBlock(tokenNode.getData(), "[") && childs != null && childs.size() > 0 && AstFragType.isIdentifier(childs.get(0).getData())) {
				val annot = AnnotationExtractor.parseAnnotationBlock(lang, childs.get(0).getData().getFragmentType(), childs.get(0).getData().getText(), (childs.size() > 1 ? childs.get(1) : null));
				annotations.add(annot);
				state = State.COMPLETE;
				return true;
			}
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
	public CsAnnotationExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public CsAnnotationExtractor copy() {
		val copy = new CsAnnotationExtractor();
		return copy;
	}


	// package-private
	void reset() {
		annotations.clear();
		state = State.INIT;
	}

}
