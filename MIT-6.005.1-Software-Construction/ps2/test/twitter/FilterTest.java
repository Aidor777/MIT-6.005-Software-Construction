package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * Test cases for writtenBy:
     * Will test: empty list, no match, single match, several matches, all match, different case match
     *
     * Test cases for inTimespan:
     * Will test: empty list, no match, single match, several matches, all match, right at start-right at end,
     * instant timespan (will allow testing right before and right after too)
     *
     * Test cases for containing:
     * Will test: empty tweets, empty words, no match, single match, several matches, all match, several word match,
     * all words match, different case match
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2021-01-01T00:00:00Z");
    private static final Instant d4 = Instant.parse("2021-12-31T23:59:59Z");
    private static final Instant d5 = d3.plusSeconds(60L);

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "alyssa", "anyone here ?", d1);
    private static final Tweet tweet4 = new Tweet(4, "alyssa", "well that is awkward", d2);
    private static final Tweet tweet5 = new Tweet(5, "guigui", "well hello there", d1);
    private static final Tweet tweet6 = new Tweet(6, "AlYsSa", "me again...", d5);
    private static final Tweet tweet7 = new Tweet(7, "jojo", "screw you now talk", d5.minusNanos(1L));
    private static final Tweet tweet8 = new Tweet(8, "garfield", "I want bi*ch LaSaGnA", d5.plusNanos(1L));

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testWrittenByEmptyList() {
        List<Tweet> writtenBy = Filter.writtenBy(Collections.emptyList(), "alyssa");

        assertTrue("expected empty list", writtenBy.isEmpty());
    }

    @Test
    public void testWrittenByMultipleTweetsNoMatch() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "guigui");

        assertTrue("expected empty list", writtenBy.isEmpty());
    }

    @Test
    public void testWrittenByMultipleTweetsSingleResult() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "alyssa");

        assertEquals("expected singleton list", 1, writtenBy.size());
        assertTrue("expected list to contain tweet", writtenBy.contains(tweet1));
    }

    @Test
    public void testWrittenByMultipleTweetsSeveralResults() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2, tweet3, tweet4, tweet5), "alyssa");
        List<Tweet> expectedResult = Arrays.asList(tweet1, tweet3, tweet4);

        assertEquals("expected list of size", 3, writtenBy.size());
        assertEquals("expected result list to be", writtenBy, expectedResult);
    }

    @Test
    public void testWrittenByMultipleTweetsAllMatch() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet3, tweet4), "alyssa");
        List<Tweet> expectedResult = Arrays.asList(tweet1, tweet3, tweet4);

        assertEquals("expected list of size", 3, writtenBy.size());
        assertEquals("expected result list to be", writtenBy, expectedResult);
    }

    @Test
    public void testWrittenByMatchDifferentCase() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet2, tweet6, tweet3, tweet5), "ALYSSA");
        List<Tweet> expectedResult = Arrays.asList(tweet6, tweet3);

        assertEquals("expected list of size", 2, writtenBy.size());
        assertEquals("expected result list to be", writtenBy, expectedResult);
    }

    @Test
    public void testInTimespanEmptyList() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");

        List<Tweet> inTimespan = Filter.inTimespan(Collections.emptyList(), new Timespan(testStart, testEnd));

        assertTrue("expected empty list", inTimespan.isEmpty());
    }

    @Test
    public void testInTimespanNoMatch() {
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(d3, d4));

        assertTrue("expected empty list", inTimespan.isEmpty());
    }

    @Test
    public void testInTimespanSingleMatch() {
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet5, tweet4, tweet6), new Timespan(d3, d4));
        List<Tweet> expectedResult = Arrays.asList(tweet6);

        assertEquals("expected list of size", 1, inTimespan.size());
        assertEquals("expected result list to be", inTimespan, expectedResult);
    }

    @Test
    public void testInTimespanSeveralMatches() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");

        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet4, tweet6, tweet5), new Timespan(testStart, testEnd));
        List<Tweet> expectedResult = Arrays.asList(tweet4, tweet5);

        assertEquals("expected list of size", 2, inTimespan.size());
        assertEquals("expected result list to be", inTimespan, expectedResult);
    }

    @Test
    public void testInTimespanMultipleTweetsAllMatch() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");

        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet3, tweet1, tweet2), new Timespan(testStart, testEnd));

        assertFalse("expected non-empty list", inTimespan.isEmpty());
        assertTrue("expected list to contain tweets", inTimespan.containsAll(Arrays.asList(tweet3, tweet1, tweet2)));
        assertEquals("expected same order", 1, inTimespan.indexOf(tweet1));
    }

    @Test
    public void testInTimespanRightAtIntervalBorders() {
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet6, tweet5, tweet4), new Timespan(d2, d5));
        List<Tweet> expectedResult = Arrays.asList(tweet6, tweet4);

        assertEquals("expected list of size", 2, inTimespan.size());
        assertEquals("expected result list to be", inTimespan, expectedResult);
    }

    @Test
    public void testInTimespanInstantInterval() {
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet6, tweet7, tweet8), new Timespan(d5, d5));
        List<Tweet> expectedResult = Arrays.asList(tweet6);

        assertEquals("expected list of size", 1, inTimespan.size());
        assertEquals("expected result list to be", inTimespan, expectedResult);
    }

    @Test
    public void testContainingEmptyTweets() {
        List<Tweet> containing = Filter.containing(Collections.emptyList(), Arrays.asList("talk", "nope"));

        assertTrue("expected empty list", containing.isEmpty());
    }

    @Test
    public void testContainingEmptyWords() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2, tweet7), Collections.emptyList());

        assertTrue("expected empty list", containing.isEmpty());
    }

    @Test
    public void testContainingNoMatch() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2, tweet7),
                Arrays.asList("elephant", "dinosaur", "astronaut"));

        assertTrue("expected empty list", containing.isEmpty());
    }

    @Test
    public void testContainingSingleMatch() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet6, tweet8, tweet7),
                Arrays.asList("elephant", "bi*ch", "astronaut"));
        List<Tweet> expectedResult = Arrays.asList(tweet8);

        assertEquals("expected list of size", 1, containing.size());
        assertEquals("expected result list to be", containing, expectedResult);
    }

    @Test
    public void testContainingSeveralMatches() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet4, tweet8, tweet7),
                Arrays.asList("elephant", "bi*ch", "well"));
        List<Tweet> expectedResult = Arrays.asList(tweet4, tweet8);

        assertEquals("expected list of size", 2, containing.size());
        assertEquals("expected result list to be", containing, expectedResult);
    }

    @Test
    public void testContainingAllMatch() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2, tweet7), Arrays.asList("talk"));

        assertFalse("expected non-empty list", containing.isEmpty());
        assertTrue("expected list to contain tweets", containing.containsAll(Arrays.asList(tweet1, tweet2, tweet7)));
        assertEquals("expected same order", 0, containing.indexOf(tweet1));
    }

    @Test
    public void testContainingSeveralWordsMatch() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet5, tweet6),
                Arrays.asList("there", "bi*ch", "well"));
        List<Tweet> expectedResult = Arrays.asList(tweet5);

        assertEquals("expected list of size", 1, containing.size());
        assertEquals("expected result list to be", containing, expectedResult);
    }

    @Test
    public void testContainingAllWordsMatch() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet4, tweet6),
                Arrays.asList("awkward", "that", "is", "well"));
        List<Tweet> expectedResult = Arrays.asList(tweet4);

        assertEquals("expected list of size", 1, containing.size());
        assertEquals("expected result list to be", containing, expectedResult);
    }

    @Test
    public void testContainingDifferentCaseMatch() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet8, tweet6), Arrays.asList("lasagna"));
        List<Tweet> expectedResult = Arrays.asList(tweet8);

        assertEquals("expected list of size", 1, containing.size());
        assertEquals("expected result list to be", containing, expectedResult);
    }

    /*
     * Warning: all the tests you write here must be runnable against any Filter
     * class that follows the spec. It will be run against several staff
     * implementations of Filter, which will be done by overwriting
     * (temporarily) your version of Filter with the staff's version.
     * DO NOT strengthen the spec of Filter or its methods.
     *
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Filter, because that means you're testing a stronger
     * spec than Filter says. If you need such helper methods, define them in a
     * different class. If you only need them in this test class, then keep them
     * in this test class.
     */


    /* Copyright (c) 2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */
}
