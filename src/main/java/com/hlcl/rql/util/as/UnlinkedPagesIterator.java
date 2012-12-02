package com.hlcl.rql.util.as;

import com.hlcl.rql.as.*;


/**
 * @author lejafr
 *
 * This class encapsulate chunk reading for unlinked pages.
 * ATTENTION: You have to delete the returned pages, otherwise you will get always the same chunk of pages!
 */
public class UnlinkedPagesIterator {

	private Project project;
	private int chunkSize;

	// currentChunk == null signal end of iterator
	private PageArrayList currentChunk;
	// index of next page to return 
	private int nextPageIndex;
	private int numberOfPages;

	/**
	 * Construct an iterator for unlinked page in the given project.
	 * Chunk size should be big, otherwise will be filled with pages modified within limit only.
	 * This is because the list of unlinked pages is not sorted by modification date, instead uses creation date!
	 */
	public UnlinkedPagesIterator(Project project, int chunkSize) throws RQLException {
		super();
		this.project = project;
		this.chunkSize = chunkSize;
		
		// initialize
		numberOfPages = 0;
		
		// read first chunk
		readChunk(null);
	}

	/**
	 * Returns the next unlinked page sorted by last changed date asc.
	 */
	public Page next() throws RQLException {
		Page nextPg = currentChunk.getPage(nextPageIndex);
		numberOfPages++;
		
		// increment
		nextPageIndex++;
		// new chunk needed?
		if (nextPageIndex >= currentChunk.size()) {
			readChunk(nextPg);
		}
		return nextPg;
	}

	/**
	 * Returns the number of unlinked pages read.
	 */
	public int pagesRead() throws RQLException {
		return numberOfPages;
	}
	/**
	 * Returns true, if another unlinked page at current index is available.
	 */
	public boolean hasNext() throws RQLException {
		// end signalised?
		if (currentChunk == null) {
			return false;
		}
		// new chunk needed?
		if (nextPageIndex >= currentChunk.size()) {
			readChunk(null);
			// end of this read?
			if (currentChunk == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Read the next chunk of unlinked pages.
	 */
	private void readChunk(Page pageToDeleteOrNull) throws RQLException {
		currentChunk = project.getAllUnlinkedPagesSortedByModificationDateAsc(chunkSize);
		// remove the page
		if (pageToDeleteOrNull != null) {
			currentChunk.remove(pageToDeleteOrNull);
		}
		nextPageIndex = 0;
		// set end signal if no more pages available
		if (currentChunk.size() == 0) {
			currentChunk = null;
		}
	}
}
