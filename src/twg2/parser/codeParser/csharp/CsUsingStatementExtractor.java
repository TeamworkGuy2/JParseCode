package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.List;

import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.AstFragType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.stateMachine.AstParserReusableBase;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class CsUsingStatementExtractor extends AstParserReusableBase<CsUsingStatementExtractor.State, List<List<String>>> {

	static enum State {
		INIT,
		FOUND_USING,
		COMPLETE,
		FAILED;
	}


	private static final CodeLanguageOptions.CSharp lang = CodeLanguageOptions.C_SHARP;

	List<List<String>> usingStatements = new ArrayList<>();


	public CsUsingStatementExtractor() {
		super("C# import statement", State.COMPLETE, State.FAILED);
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeToken> tokenNode) {
		if(state != State.FOUND_USING) {
			if(lang.getAstUtil().getChecker().isKeyword(tokenNode.getData(), CsKeyword.USING)) {
				state = State.FOUND_USING;
			}
		}
		else if(state == State.FOUND_USING) {
			var data = tokenNode.getData();
			if(AstFragType.isIdentifier(data)) {
				usingStatements.add(NameUtil.splitFqName(data.getText()));
				state = State.COMPLETE;
				return true;
			}
			else {
				state = State.FAILED;
			}
		}
		return false;
	}


	@Override
	public List<List<String>> getParserResult() {
		return usingStatements;
	}


	@Override
	public CsUsingStatementExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public CsUsingStatementExtractor copy() {
		return new CsUsingStatementExtractor();
	}


	// package-private
	void reset() {
		usingStatements.clear();
		state = State.INIT;
	}

}
