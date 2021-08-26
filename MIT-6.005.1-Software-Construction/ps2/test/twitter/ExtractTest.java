package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.Test;

public class ExtractTest {

    /*
     * Test cases for getTimespan:
     * When the list is null or empty it is not clear what is expected, so no test for that.
     * Will test: simple case (two tweets), single element, every element same instant, complex case,
     * one second difference
     *
     * Test cases for getMentionedUsers:
     * Strings will be compared by ignoring their case (here transforming them to lowercase first)
     * Will test: no username mentioned, empty list, single case, several case, same username different case,
     * non-reference @, same mention in same tweet, several mentions same tweet
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d4 = Instant.parse("1950-10-10T23:59:59Z");
    private static final Instant d5 = Instant.parse("2159-12-31T00:00:00Z");
    private static final Instant d6 = Instant.parse("2016-02-17T09:59:59Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "me", "what the hell", d3);
    private static final Tweet tweet4 = new Tweet(4, "jojo", "@guigui is fabulous", d1);
    private static final Tweet tweet5 = new Tweet(5, "guigui", "Did Twitter exist then ? @jojo", d4);
    private static final Tweet tweet6 = new Tweet(6, "i am death", "the destroyer of worlds @Eisenhower", d5);
    private static final Tweet tweet7 = new Tweet(7, "alice bob", "just on time @GuiGui", d6);
    private static final Tweet tweet8 = new Tweet(8, "charlie dave", "my email: charlie.dave@hotmail.com, @+!", d6);
    private static final Tweet tweet9 = new Tweet(9, "it is me", "Truly @guigui @GuiGui is the best @GUIGUI there is", d6);
    private static final Tweet tweet10 = new Tweet(10, "again", "@gui_gui's and @jo-jo's are okay by @me2", d6);

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }
    
    @Test
    public void testGetTimespanOneTweet() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d1, timespan.getEnd());
    }
    
    @Test
    public void testGetTimespanAllSameInstant() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet3, tweet4));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d3, timespan.getEnd());
    }

    @Test
    public void testGetTimespanComplex() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2, tweet3, tweet4, tweet5, tweet6));

        assertEquals("expected start", d4, timespan.getStart());
        assertEquals("expected end", d5, timespan.getEnd());
    }

    @Test
    public void testGetTimespanOneSecond() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet3, tweet7));

        assertEquals("expected start", d6, timespan.getStart());
        assertEquals("expected end", d3, timespan.getEnd());
    }
    
    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1, tweet2));
        
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersEmptyList() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Collections.emptyList());

        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersSingleMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet3, tweet4)).stream()
                .map(user -> user.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("guigui");

        assertEquals("set size", expectedResult.size(), mentionedUsers.size());
        assertEquals("set content", expectedResult, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersSeveralMentions() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet3, tweet4, tweet5, tweet6)).stream()
                .map(user -> user.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("guigui");
        expectedResult.add("eisenhower");
        expectedResult.add("jojo");

        assertEquals("set size", expectedResult.size(), mentionedUsers.size());
        assertEquals("set content", expectedResult, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersSameUserDifferentCase() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet3, tweet4, tweet7)).stream()
                .map(user -> user.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("guigui");

        assertEquals("set size", expectedResult.size(), mentionedUsers.size());
        assertEquals("set content", expectedResult, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersNotAMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet3, tweet6, tweet8)).stream()
                .map(user -> user.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("eisenhower");

        assertEquals("set size", expectedResult.size(), mentionedUsers.size());
        assertEquals("set content", expectedResult, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersSeveralMentionsSameUser() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet9)).stream()
                .map(user -> user.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("guigui");

        assertEquals("set size", expectedResult.size(), mentionedUsers.size());
        assertEquals("set content", expectedResult, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersSeveralUsersMentionedSameTweet() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet10)).stream()
                .map(user -> user.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

        Set<String> expectedResult = new HashSet<>();
        expectedResult.add("gui_gui");
        expectedResult.add("me2");
        expectedResult.add("jo-jo");

        assertEquals("set size", expectedResult.size(), mentionedUsers.size());
        assertEquals("set content", expectedResult, mentionedUsers);
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */


    /* Copyright (c) 2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */

}
