package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.ast.interm.block.BlockAst;
import twg2.parser.baseAst.AstParser;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.Consume;
import twg2.parser.documentParser.CodeFragment;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-3-20
 */
public class CommentBlockExtractor implements AstParser<List<String>> {

	static enum State {
		INIT,
		FINDING_COMMENTS,
		COMPLETE,
		FAILED;
	}


	BlockAst<? extends CompoundBlock> parentBlock;
	List<String> comments = new ArrayList<>();
	boolean multiLine;
	State state = State.INIT;
	String langName;
	String name;


	public CommentBlockExtractor(String langName, BlockAst<? extends CompoundBlock> parentBlock) {
		this.langName = langName;
		this.name = langName + " field";
		this.parentBlock = parentBlock;
	}


	@Override
	public String name() {
		return name;
	}


	@Override
	public boolean acceptNext(SimpleTree<CodeFragment> tokenNode) {
		if(state == State.COMPLETE || state == State.FAILED) {
			state = State.INIT;
		}
		Consume res = null;

		if(state == State.INIT) {
			res = findComment(tokenNode);
			if(res.isAccept()) { return true; }
		}
		else if(state == State.FINDING_COMMENTS) {
			res = findComment(tokenNode);
			if(res.isAccept()) { return true; }
		}
		return false;
	}


	private Consume findComment(SimpleTree<CodeFragment> tokenNode) {
		if(tokenNode.getData().getFragmentType() == CodeFragmentType.COMMENT) {
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
	private String extractCommentText(CodeFragment nodeData) {
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

		int end = len;
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
	public CommentBlockExtractor recycle() {
		reset();
		return this;
	}


	@Override
	public CommentBlockExtractor copy() {
		val copy = new CommentBlockExtractor(this.langName, this.parentBlock);
		return copy;
	}


	// package-private
	void reset() {
		this.comments.clear();
		this.multiLine = false;
	}

}
