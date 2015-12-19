package twg2.parser.textFragment;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import twg2.parser.textParser.ParserPos;

/**
 * @author TeamworkGuy2
 * @since 2015-3-7
 */
public interface TextFragmentRef {

	public TextFragmentRef copy();

	/**
	 * @return absolute start position offset, inclusive, 0 based
	 */
	public int getOffsetStart();

	/**
	 * @return absolute end position offset, exclusive, 0 based
	 */
	public int getOffsetEnd();

	/**
	 * @return starting row inclusive, 0 based
	 */
	public int getLineStart();

	/**
	 * @return ending row inclusive, 0 based
	 */
	public int getLineEnd();

	/**
	 * @return starting row, starting column, inclusive, 0 based
	 */
	public int getColumnStart();

	/**
	 * @return ending row, ending column, inclusive, 0 based
	 */
	public int getColumnEnd();


	public default boolean contains(TextFragmentRef frag) {
		return this.getOffsetStart() <= frag.getOffsetStart() &&
			this.getOffsetEnd() >= frag.getOffsetEnd();
	}


	/** Behaves as if calling:<br>
	 * {@code allText.}{@link CharSequence#subSequence(int, int) subSequence}{@code (this.}{@link #getOffsetStart() getOffsetStart()}{@code , this.}{@link #getOffsetEnd() getOffsetEnd()}{@code )}
	 * @param allText a character sequence containing all of the text from index 0.
	 * @return the text referenced by this text fragment
	 */
	public CharSequence getText(CharSequence allText);

	public CharSequence getText(List<? extends CharSequence> textLines);

	/**
	 * @return combined textual representation of {@link #toString()} and {@link #getText(CharSequence)}
	 */
	public String toString(CharSequence allText);


	@Override
	public boolean equals(Object obj);


	@Override
	public String toString();


	static CharSequence getText(int offsetStart, int offsetEnd, CharSequence chseq) {
		return chseq.subSequence(offsetStart, offsetEnd);
	}


	/**
	 * @param offsetStart - inclusive
	 * @param offsetEnd - exclusive
	 * @param lineStart - inclusive
	 * @param lineEnd - inclusive
	 * @param columnStart - inclusive
	 * @param columnEnd - inclusive
	 * @param lines
	 * @return the text sub-string represented by the offsets, line, and column numbers provided
	 */
	public static CharSequence getText(int offsetStart, int offsetEnd, int lineStart, int lineEnd, int columnStart, int columnEnd, List<? extends CharSequence> lines) {
		StringBuilder sb = new StringBuilder();

		CharSequence ln = lines.get(lineStart);

		sb.append(ln.subSequence(columnStart, lineStart == lineEnd ? Math.min(columnEnd + 1, ln.length()) : ln.length()));

		if(lineEnd > lineStart) {
			for(int i = lineStart + 1; i < lineEnd; i++) {
				ln = lines.get(i);
				sb.append(ln.subSequence(0, ln.length()));
			}

			ln = lines.get(lineEnd);
			sb.append(ln.subSequence(0, Math.min(columnEnd + 1, ln.length())));
		}

		return sb.toString();
	}


	static String _toString(int offsetStart, int offsetEnd, int lineStart, int lineEnd, int columnStart, int columnEnd, CharSequence chseq) {
		return "TextFragmentRef: { off: " + offsetStart + ", len: " + (offsetEnd - offsetStart) +
				", start: [" + lineStart + ":" + columnStart + "], end: [" + lineEnd + ":" + columnEnd + "], " +
				"text: \"" + getText(offsetStart, offsetEnd, chseq) + "\" }";
	}


	static String _toString(int offsetStart, int offsetEnd, int lineStart, int lineEnd, int columnStart, int columnEnd) {
		return "TextFragmentRef: { off: " + offsetStart + ", len: " + (offsetEnd - offsetStart) +
				", start: [" + lineStart + ":" + columnStart + "], end: [" + lineEnd + ":" + columnEnd + "]" +
				" }";
	}


	static TextFragmentRef.Impl copy(TextFragmentRef src) {
		TextFragmentRef.Impl copy = new TextFragmentRef.Impl(src.getOffsetStart(), src.getOffsetEnd(), src.getLineStart(), src.getColumnStart(), src.getLineEnd(), src.getColumnEnd());
		return copy;
	}


	static TextFragmentRef.ImplMut copyMutable(TextFragmentRef src) {
		TextFragmentRef.ImplMut copy = new TextFragmentRef.ImplMut(src.getOffsetStart(), src.getOffsetEnd(), src.getLineStart(), src.getColumnStart(), src.getLineEnd(), src.getColumnEnd());
		return copy;
	}


	static TextFragmentRef.ImplMut merge(TextFragmentRef.ImplMut dst, TextFragmentRef... fragments) {
		// sort the fragments by start offset
		Arrays.sort(fragments, (a, b) -> a.getOffsetStart() - b.getOffsetStart());

		return _merge(fragments, dst);
	}


	static TextFragmentRef merge(TextFragmentRef... fragments) {
		// sort the fragments by start offset
		Arrays.sort(fragments, (a, b) -> a.getOffsetStart() - b.getOffsetStart());

		return _merge(fragments, null);
	}


	static TextFragmentRef merge(Collection<? extends TextFragmentRef> fragments) {
		// sort the fragments by start offset
		TextFragmentRef[] fragmentsAry = fragments.toArray(new TextFragmentRef[fragments.size()]);
		Arrays.sort(fragmentsAry, (a, b) -> a.getOffsetStart() - b.getOffsetStart());

		return _merge(fragmentsAry, null);
	}


	static TextFragmentRef.ImplMut _merge(TextFragmentRef[] mutableSortedFragments, TextFragmentRef.ImplMut dstOpt) {
		TextFragmentRef.ImplMut res = dstOpt != null ? dstOpt : copyMutable(mutableSortedFragments[0]);
		int resOffsetEnd = res.offsetEnd;
		int resLineEnd = 0;
		int resColumnEnd = 0;

		// loop over each fragment (note the fragment list is sorted) and set the ending offset/line/column equal to the fragment's
		for(TextFragmentRef frag : mutableSortedFragments) {
			// stop if a fragment is not adjacent to the previous fragment, + 1 because indices are inclusive
			if(frag.getOffsetStart() > resOffsetEnd + 1) {
				break;
			}
			resOffsetEnd = frag.getOffsetEnd();
			resLineEnd = frag.getLineEnd();
			resColumnEnd = frag.getColumnEnd();
		}

		res.offsetEnd = resOffsetEnd;
		res.lineEnd = resLineEnd;
		res.columnEnd = resColumnEnd;
		return res;
	}




	@AllArgsConstructor
	public static class Impl implements TextFragmentRef {
		private final @Getter int offsetStart;
		private final @Getter int offsetEnd;
		private final @Getter int lineStart;
		private final @Getter int columnStart;
		private final @Getter int lineEnd;
		private final @Getter int columnEnd;


		@Override
		public Impl copy() {
			Impl copy = new Impl(offsetStart, offsetEnd, lineStart, columnStart, lineEnd, columnEnd);
			return copy;
		}


		@Override
		public CharSequence getText(CharSequence chseq) {
			return chseq.subSequence(offsetStart, offsetEnd);
		}


		@Override
		public CharSequence getText(List<? extends CharSequence> lines) {
			val res = TextFragmentRef.getText(offsetStart, offsetEnd, lineStart, lineEnd, columnStart, columnEnd, lines);
			return res;
		}


		@Override
		public String toString(CharSequence chseq) {
			return TextFragmentRef._toString(offsetStart, offsetEnd, lineStart, lineEnd, columnStart, columnEnd, chseq);
		}


		@Override
		public String toString() {
			return TextFragmentRef._toString(offsetStart, offsetEnd, lineStart, lineEnd, columnStart, columnEnd);
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + columnEnd;
			result = prime * result + columnStart;
			result = prime * result + lineEnd;
			result = prime * result + lineStart;
			result = prime * result + offsetEnd;
			result = prime * result + offsetStart;
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof TextFragmentRef)) { return false; }
			val frag = (TextFragmentRef)obj;
			return this.offsetStart == frag.getOffsetStart() &&
				this.offsetEnd == frag.getOffsetEnd() &&
				this.columnStart == frag.getColumnStart() &&
				this.columnEnd == frag.getColumnEnd() &&
				this.lineStart == frag.getLineStart() &&
				this.lineEnd == frag.getLineEnd();
		}

	}


	@AllArgsConstructor
	@NoArgsConstructor
	public static class ImplMut implements TextFragmentRef {
		private @Getter @Setter int offsetStart;
		private @Getter @Setter int offsetEnd;
		private @Getter @Setter int lineStart;
		private @Getter @Setter int columnStart;
		private @Getter @Setter int lineEnd;
		private @Getter @Setter int columnEnd;


		public void setStart(ParserPos pos) {
			this.offsetStart = pos.getPosition();
			this.lineStart = pos.getLineNumber() - 1;
			this.columnStart = pos.getColumnNumber() - 1; 
		}


		public void setEnd(ParserPos pos) {
			this.offsetEnd = pos.getPosition() + 1;
			this.lineEnd = pos.getLineNumber() - 1;
			this.columnEnd = pos.getColumnNumber() - 1;
		}


		@Override
		public ImplMut copy() {
			ImplMut copy = new ImplMut(offsetStart, offsetEnd, lineStart, columnStart, lineEnd, columnEnd);
			return copy;
		}


		@Override
		public CharSequence getText(CharSequence chseq) {
			return chseq.subSequence(offsetStart, offsetEnd);
		}


		@Override
		public CharSequence getText(List<? extends CharSequence> lines) {
			val res = TextFragmentRef.getText(offsetStart, offsetEnd, lineStart, lineEnd, columnStart, columnEnd, lines);
			return res;
		}


		@Override
		public String toString(CharSequence chseq) {
			return TextFragmentRef._toString(offsetStart, offsetEnd, lineStart, lineEnd, columnStart, columnEnd, chseq);
		}


		@Override
		public String toString() {
			return TextFragmentRef._toString(offsetStart, offsetEnd, lineStart, lineEnd, columnStart, columnEnd);
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + columnEnd;
			result = prime * result + columnStart;
			result = prime * result + lineEnd;
			result = prime * result + lineStart;
			result = prime * result + offsetEnd;
			result = prime * result + offsetStart;
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof TextFragmentRef)) { return false; }
			val frag = (TextFragmentRef)obj;
			return this.offsetStart == frag.getOffsetStart() &&
				this.offsetEnd == frag.getOffsetEnd() &&
				this.columnStart == frag.getColumnStart() &&
				this.columnEnd == frag.getColumnEnd() &&
				this.lineStart == frag.getLineStart() &&
				this.lineEnd == frag.getLineEnd();
		}

	}

}
