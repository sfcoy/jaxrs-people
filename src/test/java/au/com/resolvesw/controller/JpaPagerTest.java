package au.com.resolvesw.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit tests for JpaPager
 */
public class JpaPagerTest {

    @Test
    public void testPageOne() {
        JpaPager sut = new JpaPager(1, 25);
        assertThat("firstResult", sut.firstResult(), is(0));
        assertThat("maxResults", sut.maxResults(), is(25));
    }

    @Test
    public void testPageTwo() {
        JpaPager sut = new JpaPager(2, 25);
        assertThat("firstResult", sut.firstResult(), is(25));
        assertThat("maxResults", sut.maxResults(), is(25));
    }

    @Test
    public void testLastPage() {
        final int lastPage = Integer.MAX_VALUE / 25;
        JpaPager sut = new JpaPager(lastPage + 1, 25);
        assertThat("firstResult", sut.firstResult(), is(25 * lastPage));
        assertThat("maxResults", sut.maxResults(), is(Integer.MAX_VALUE - sut.firstResult() + 1));
    }

    @Test
    public void testLastBigPage() {
        final int lastPage = Integer.MAX_VALUE / 80;
        JpaPager sut = new JpaPager(lastPage + 1, 80);
        assertThat("firstResult", sut.firstResult(), is(80 * lastPage));
        assertThat("maxResults", sut.maxResults(), is(Integer.MAX_VALUE - sut.firstResult() + 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPageOverflow() {
        JpaPager sut = new JpaPager(Integer.MAX_VALUE, 25);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testLargePageOverflow() {
        JpaPager sut = new JpaPager(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }


}
