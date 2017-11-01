package au.com.resolvesw.controller;

/**
 * @author sfcoy
 */
public class JpaPager {

    private final int firstResult;
    private final int maxResults;

    public JpaPager(int requestedPage, int requestedPageSize) {
        final long longFirstResult = (long)requestedPageSize * (requestedPage - 1);
        if (longFirstResult < 0 || longFirstResult > Integer.MAX_VALUE || requestedPageSize <= 0) {
            throw new IllegalArgumentException("Invalid page range");
        }
        this.firstResult = (int) longFirstResult;
        final int rowsRemaining = Integer.MAX_VALUE - this.firstResult;
        this.maxResults = rowsRemaining > requestedPageSize ? requestedPageSize : rowsRemaining + 1;
    }

    public int firstResult() {
        return firstResult;
    }

    public int maxResults() {
        return maxResults;
    }

}
