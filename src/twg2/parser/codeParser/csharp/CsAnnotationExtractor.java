package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.List;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.parser.codeParser.extractors.AnnotationExtractor;
import twg2.parser.fragment.AstFragType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.stateMachine.AstParserReusableBase;
import twg2.treeLike.simpleTree.SimpleTree;

public class CsAnnotationExtractor extends AstParserReusableBase<CsAnnotationExtractor.State, List<AnnotationSig>> {

	static enum State {
		INIT,
		COMPLETE,
		FAILED;
	}


	List<AnnotationSig> annotations = new ArrayList<>();


	public CsAnnotationExtractor() {
		super("C# annotation", State.COMPLETE, State.FAILED);
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeToken> tokenNode) {
		var lang = CodeLanguageOptions.C_SHARP;

		if(state != State.FAILED) {
			var childs = tokenNode.getChildren();
			CodeToken annotTypeFrag = null;
			if(AstFragType.isBlock(tokenNode.getData(), "[") && childs != null && childs.size() > 0 && AstFragType.isIdentifier(annotTypeFrag = childs.get(0).getData())) {
				var annot = AnnotationExtractor.parseAnnotationBlock(lang, annotTypeFrag.getTokenType(), annotTypeFrag.getText(), (childs.size() > 1 ? childs.get(1) : null));
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
	public CsAnnotationExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public CsAnnotationExtractor copy() {
		return new CsAnnotationExtractor();
	}


	// package-private
	void reset() {
		annotations.clear();
		state = State.INIT;
	}

}
