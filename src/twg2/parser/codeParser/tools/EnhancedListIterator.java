package twg2.parser.codeParser.tools;

import java.util.List;
import java.util.ListIterator;

import twg2.collections.interfaces.ListReadOnly;
import twg2.streams.PeekableIterator;

/** A {@link PeekableIterator Peekable} {@link ListIterator}.<br>
 * Useful for treating a list as an iterable stream which can be rewound one element at a time by calling {@link #previous()}.
 * The iterator returns each element read from the source {@code List} until {@link List#size()} is reached after which point null is returned by {@link #peek()} and {@link #next()}.
 * Warning: the iterator does not copy the source list nor perform modification tracking. If the underlying list size or
 * contents change this iterator will continue to attempt to operate based on the current state of the list.
 * @author TeamworkGuy2
 * @since 2016-1-2
 */
// Port of JStreamish EnhancedListIterator to support ListReadOnly<>
public class EnhancedListIterator<T> implements ListIterator<T>, PeekableIterator<T> {
	private ListReadOnly<T> sourceList;
	private int nextIndex = 0;
	private int mark = -2;


	/** Create a list builder iterator from existing enhanced iterator
	 * @param iterator the source to read input from, null marks the end of the stream
	 */
	public EnhancedListIterator(ListReadOnly<T> sourceList) {
		this.reset(sourceList);
	}


	@Override
	public boolean hasNext() {
		return nextIndex < sourceList.size();
	}


	@Override
	public T peek() {
		if(nextIndex < sourceList.size()) {
			return sourceList.get(nextIndex);
		}
		return null;
	}


	@Override
	public T next() {
		if(nextIndex < sourceList.size()) {
			return sourceList.get(nextIndex++);
		}
		return null;
	}


	@Override
	public boolean hasPrevious() {
		return nextIndex > 0;
	}


	@Override
	public T previous() {
		if(nextIndex == 0) {
			throw new IndexOutOfBoundsException("-1 of [0, unknown)");
		}
		nextIndex--;
		return sourceList.get(nextIndex);
	}


	@Override
	public void remove() {
		throw new UnsupportedOperationException("EnhancedListIteratorTest.remove()");
	}


	@Override
	public void set(T e) {
		throw new UnsupportedOperationException("EnhancedListIteratorTest.set()");
	}


	@Override
	public void add(T e) {
		throw new UnsupportedOperationException("EnhancedListIteratorTest.add()");
	}


	/**
	 * @return the index of the last call to {@link #next()}, (i.e. after each {@code next()} call, {@code previousIndex()} returns indices forming the sequence -1, 0, 1, 2, ...)
	 */
	@Override
	public int previousIndex() {
		return nextIndex - 1;
	}


	/**
	 * @return the index of the next call to {@link #next()} (note: the next value may not exist, see {@link #hasNext()} to check),
	 * (i.e. after each {@code next()} call, {@code nextIndex()} returns indices forming the sequence 1, 2, 3, 4, ...)
	 */
	@Override
	public int nextIndex() {
		return nextIndex;
	}


	public T peekPrevious() {
		if(nextIndex > 0) {
			return sourceList.get(nextIndex - 1);
		}
		throw new IndexOutOfBoundsException("-1 of [0, unknown)");
	}


	public int size() {
		return sourceList.size();
	}


	public int mark() {
		return mark = nextIndex;
	}


	public void reset() {
		if(mark < 0) { throw new IllegalStateException("iterator not yet marked"); }
		reset(this.mark);
	}


	public void reset(int mark) {
		if(mark < 0 || mark > sourceList.size()) { throw new IndexOutOfBoundsException(mark + " of [0, " + sourceList.size() + "]"); }
		nextIndex = mark;
	}


	public void reset(ListReadOnly<T> newSourceList) {
		this.sourceList = newSourceList;
		this.nextIndex = 0;
		this.mark = -2;
	}

}
