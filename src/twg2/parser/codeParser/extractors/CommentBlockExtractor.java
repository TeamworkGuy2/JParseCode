package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.List;

import twg2.ast.interm.block.BlockAst;
import twg2.parser.codeParser.BlockType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.stateMachine.AstParserReusableBase;
import twg2.parser.stateMachine.Consume;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-3-20
 */
public class CommentBlockExtractor extends AstParserReusableBase<CommentBlockExtractor.State, List<String>> {

	static enum State {
		INIT,
		FINDING_COMMENTS,
		COMPLETE,
		FAILED;
	}


	BlockAst<? extends BlockType> parentBlock;
	List<String> comments = new ArrayList<>();
	boolean multiLine;
	String langName;


	public CommentBlockExtractor(String langName, BlockAst<? extends BlockType> parentBlock) {
		super(langName + " field", State.COMPLETE, State.FAILED);
		this.langName = langName;
		this.parentBlock = parentBlock;
		this.state = State.INIT;
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeToken> tokenNode) {
		if(state == State.COMPLETE || state == State.FAILED) {
			state = State.INIT;
		}
		Consume res = null;

		if(state == State.INIT || state == State.FINDING_COMMENTS) {
			res = findComment(tokenNode);
			if(res.isAccept()) { return true; }
		}
		return false;
	}


	private Consume findComment(SimpleTree<CodeToken> tokenNode) {
		if(tokenNode.getData().getTokenType() == CodeTokenType.COMMENT) {
			this.state = State.FINDING_COMMENTS;
			this.comments.add(extractCommentText(tokenNode.getData()));
			return Consume.ACCEPTED;
		}
		else {
			this.state = this.comments.size() > 0 ? State.COMPLETE : State.FAILED;
			return Consume.REJECTED;
		}
	}


	// TODO makes assumptions about comment begin and end markers
	private String extractCommentText(CodeToken nodeData) {
		String text = nodeData.getText();
		int len = text.length();
		if(len == 0) { return text; }

		int whitespaces = 0;
		// skip leading whitespace
		while(Character.isWhitespace(text.charAt(whitespaces))) { whitespaces++; }

		// skip leading slashes from single line comments and beginning of multi-line comments
		int slashes = 0;
		while(text.charAt(whitespaces + slashes) == '/') { slashes++; }
		if(whitespaces > 0) {
			return text.substring(whitespaces + slashes);
		}

		// skip leading asterisks if it's a multi-line comment
		int asterisks = 0;
		if(slashes == 1) {
			while(text.charAt(whitespaces + slashes + asterisks) == '*') { asterisks++; }
			if(asterisks > 0) {
				this.multiLine = true;
			}
		}

		// skip trailing asterisk(s) and slash
		int end = len;
		if(end > 1 & text.charAt(end - 2) == '\r') { end -= 2; }
		else if(text.charAt(end - 1) == '\r') { end -= 1; }
		if(this.multiLine) {
			if(text.charAt(end - 1) == '/' && end > 1 && text.charAt(end - 2) == '*') { end -= 2; }
			while(end > 0 && text.charAt(end - 1) == '*') { end--; }
		}

		return text.substring(whitespaces + slashes + asterisks, end);
	}


	@Override
	public List<String> getParserResult() {
		return comments;
	}


	@Override
	public CommentBlockExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public CommentBlockExtractor copy() {
		return new CommentBlockExtractor(this.langName, this.parentBlock);
	}


	// package-private
	void reset() {
		this.comments.clear();
		this.multiLine = false;
	}

}
