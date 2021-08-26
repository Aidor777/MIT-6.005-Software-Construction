package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * Test cases for guessFollowsGraph:
     * We cannot text the equality of the returned network, but we can test that @-mentioned people are at least there.
     * Will test: empty tweets, one mention in tweets, several mentions of same person but different case,
     * one person makes several different mentions, does not contain non-author or non-mentioned people,
     * users cannot follow themselves, not a valid mention, several mentions same tweet, graph with several followers
     * following one or more people
     *
     * Test cases for influencers:
     * Obviously we assume we get a valid graph, NOT from our own implementation (which may be faulty, even though we do
     * not hope that it is !).
     * Will test: empty graph, no follower, only one influencer-follower relation, tie between two influencers,
     * same influencer different case
     */

    private static final Instant d = Instant.parse("2016-02-17T10:00:00Z");

    private static final Tweet tweet1 = new Tweet(1, "jojo", "@guigui is fabulous", d);
    private static final Tweet tweet2 = new Tweet(2, "alice", "what a nice day", d);
    private static final Tweet tweet3 = new Tweet(3, "JoJo", "I repeat, @GuiGui is fa-bu-lous !", d);
    private static final Tweet tweet4 = new Tweet(4, "JoJo", "Okay, maybe @kitty is not bad either", d);
    private static final Tweet tweet5 = new Tweet(5, "jojo", "After some thought, @jojo is the best", d);
    private static final Tweet tweet6 = new Tweet(6, "jojo", "Hey what is this @he.ll!/<>", d);
    private static final Tweet tweet7 = new Tweet(7, "jojo", "Hi to @alice @guigui and @kitty", d);
    private static final Tweet tweet8 = new Tweet(8, "alice", "Hi to you to @jojo", d);

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testGuessFollowsGraphEmptyTweets() {
        Map<String, List<String>> followsGraph = prepareSocialNetworkForComparison(SocialNetwork
                .guessFollowsGraph(new ArrayList<>()));

        assertTrue("expected empty graph", followsGraph.isEmpty());
    }

    @Test
    public void testGuessFollowsGraphOneMention() {
        Map<String, List<String>> followsGraph = prepareSocialNetworkForComparison(SocialNetwork
                .guessFollowsGraph(Arrays.asList(tweet1, tweet2)));

        assertTrue("expected at least one relation", followsGraph.size() >= 1);
        assertTrue("expected the relation to be from", followsGraph.containsKey("jojo"));
        assertTrue("expected the relation to be to", followsGraph.get("jojo").contains("guigui"));
    }

    @Test
    public void testGuessFollowsGraphSeveralMentionsDifferentCase() {
        Map<String, List<String>> followsGraph = prepareSocialNetworkForComparison(SocialNetwork
                .guessFollowsGraph(Arrays.asList(tweet1, tweet2, tweet3)));

        assertTrue("expected at least one relation", followsGraph.size() >= 1);
        assertTrue("expected the relation to be from", followsGraph.containsKey("jojo"));
        assertTrue("expected the follower to be present only once",
                containsStringAtMostOnce(followsGraph.keySet(), "jojo"));
        assertTrue("expected the relation to be to", followsGraph.get("jojo").contains("guigui"));
        assertTrue("expected the relation to be unique", containsStringAtMostOnce(followsGraph.get("jojo"),
                "guigui"));
    }

    @Test
    public void testGuessFollowsGraphOnePersonSeveralMentions() {
        Map<String, List<String>> followsGraph = prepareSocialNetworkForComparison(SocialNetwork
                .guessFollowsGraph(Arrays.asList(tweet1, tweet4)));

        assertTrue("expected some relations to be from", followsGraph.containsKey("jojo"));
        assertTrue("expected the follower to be present only once",
                containsStringAtMostOnce(followsGraph.keySet(), "jojo"));
        assertTrue("expected at least two relations", followsGraph.get("jojo").size() >= 2);
        assertTrue("expected one relation to be to", followsGraph.get("jojo").contains("guigui"));
        assertTrue("expected another relation to be to", followsGraph.get("jojo").contains("kitty"));
    }

    @Test
    public void testGuessFollowsGraphOnlyAvailablePeople() {
        Map<String, List<String>> followsGraph = prepareSocialNetworkForComparison(SocialNetwork
                .guessFollowsGraph(Arrays.asList(tweet1, tweet2, tweet3, tweet4)));
        Set<String> presentPeople = new HashSet() {{add("jojo"); add("alice"); add("kitty"); add("guigui");}};

        assertTrue("cannot have absent followers", presentPeople.containsAll(followsGraph.keySet()));
        assertTrue("cannot have absent followed people", presentPeople.containsAll(followsGraph.values()
                .stream().flatMap(List::stream).collect(Collectors.toSet())));
    }

    @Test
    public void testGuessFollowsGraphCannotSelfFollow() {
        Map<String, List<String>> followsGraph = prepareSocialNetworkForComparison(SocialNetwork
                .guessFollowsGraph(Arrays.asList(tweet5)));

        if (followsGraph.containsKey("jojo")) {
            assertFalse("cannot self follow", followsGraph.get("jojo").contains("jojo"));
        } else {
            assertTrue("the user was not present anyway", true);
        }
    }

    @Test
    public void testGuessFollowsGraphInvalidMention() {
        Map<String, List<String>> followsGraph = prepareSocialNetworkForComparison(SocialNetwork
                .guessFollowsGraph(Arrays.asList(tweet6)));

        if (followsGraph.containsKey("jojo")) {
            assertFalse("cannot follow an invalid user", followsGraph.get("jojo").contains("he.ll!/<>"));
        } else {
            assertTrue("the user was not present anyway", true);
        }
    }

    @Test
    public void testGuessFollowsGraphOnePersonOneTweetSeveralMentions() {
        Map<String, List<String>> followsGraph = prepareSocialNetworkForComparison(SocialNetwork
                .guessFollowsGraph(Arrays.asList(tweet7)));

        assertTrue("expected some relations to be from", followsGraph.containsKey("jojo"));
        assertTrue("expected at least three relations", followsGraph.get("jojo").size() >= 3);
        assertTrue("expected one relation to be to", followsGraph.get("jojo").contains("guigui"));
        assertTrue("expected another relation to be to", followsGraph.get("jojo").contains("kitty"));
        assertTrue("expected another relation to be to", followsGraph.get("jojo").contains("alice"));
    }

    @Test
    public void testGuessFollowsGraphComplexCase() {
        Map<String, List<String>> followsGraph = prepareSocialNetworkForComparison(SocialNetwork
                .guessFollowsGraph(Arrays.asList(tweet1, tweet4, tweet8)));


        assertTrue("expected at least two relations", followsGraph.keySet().size() >= 2);
        assertTrue("expected some relations to be from", followsGraph.containsKey("jojo"));
        assertTrue("expected some relations also to be from", followsGraph.containsKey("alice"));
        // jojo
        assertTrue("expected at least two relations", followsGraph.get("jojo").size() >= 2);
        assertTrue("expected one relation to be to", followsGraph.get("jojo").contains("guigui"));
        assertTrue("expected another relation to be to", followsGraph.get("jojo").contains("kitty"));
        // alice
        assertTrue("expected at least one relation", followsGraph.get("alice").size() >= 1);
        assertTrue("expected one relation to be to", followsGraph.get("alice").contains("jojo"));
    }

    // Test helper to compare strings in lower case only as it should be case-insensitive
    private Map<String, List<String>> prepareSocialNetworkForComparison(Map<String, Set<String>> network) {
        return network.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(Locale.ROOT),
                entry -> entry.getValue().stream().map(item -> item.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toList())));
    }

    // Test helper to check the appearance of a string at most once in a collection
    private boolean containsStringAtMostOnce(Collection<String> names, String test) {
        return names.stream().filter(name -> name.equalsIgnoreCase(test)).count() <= 1;
    }

    @Test
    public void testInfluencersEmptyGraph() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> influencers = treatInfluencersList(SocialNetwork.influencers(followsGraph));

        assertTrue("expected empty list", influencers.isEmpty());
    }

    @Test
    public void testInfluencersNoInfluencer() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("jojo", Collections.emptySet());
        List<String> influencers = treatInfluencersList(SocialNetwork.influencers(followsGraph));

        assertTrue("expected empty list", influencers.isEmpty());
    }

    @Test
    public void testInfluencersOneInfluencerFollowerRelation() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("jojo", new HashSet<>(Arrays.asList("guigui")));
        List<String> influencers = treatInfluencersList(SocialNetwork.influencers(followsGraph));

        List<String> expectedResult = Arrays.asList("guigui");

        assertEquals("expected list to contain one element", 1, influencers.size());
        assertEquals("expected list to be", expectedResult, influencers);
    }

    @Test
    public void testInfluencersFollowerCountTie() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("jojo", new HashSet<>(Arrays.asList("guigui", "kitty")));
        followsGraph.put("guigui", new HashSet<>(Arrays.asList("jojo", "kitty")));
        List<String> influencers = treatInfluencersList(SocialNetwork.influencers(followsGraph));

        assertEquals("expected list to contain three elements", 3, influencers.size());
        assertTrue("expected list to contain", influencers.containsAll(Arrays.asList("kitty", "guigui", "jojo")));
        assertTrue("expected higher follower count to arrive before", influencers.indexOf("jojo") > influencers.indexOf("kitty"));
        assertTrue("expected higher follower count to arrive before", influencers.indexOf("guigui") > influencers.indexOf("kitty"));
    }

    @Test
    public void testInfluencersSameInfluencerDifferentCase() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("jojo", new HashSet<>(Arrays.asList("kitty")));
        followsGraph.put("guigui", new HashSet<>(Arrays.asList("jojo", "kitty")));
        followsGraph.put("kitty", new HashSet<>(Arrays.asList("JoJo")));
        followsGraph.put("alice", new HashSet<>(Arrays.asList("JOJO", "guigui")));
        List<String> influencers = treatInfluencersList(SocialNetwork.influencers(followsGraph));

        assertEquals("expected list to contain three elements", 3, influencers.size());
        assertTrue("expected list to contain", influencers.containsAll(Arrays.asList("kitty", "guigui", "jojo")));
        assertTrue("expected higher follower count to arrive before", influencers.indexOf("kitty") > influencers.indexOf("jojo"));
        assertTrue("expected higher follower count to arrive before", influencers.indexOf("guigui") > influencers.indexOf("kitty"));
    }

    private List<String> treatInfluencersList(List<String> influencers) {
        return influencers.stream().map(person -> person.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * SocialNetwork class that follows the spec. It will be run against several
     * staff implementations of SocialNetwork, which will be done by overwriting
     * (temporarily) your version of SocialNetwork with the staff's version.
     * DO NOT strengthen the spec of SocialNetwork or its methods.
     *
     * In particular, your test cases must not call helper methods of your own
     * that you have put in SocialNetwork, because that means you're testing a
     * stronger spec than SocialNetwork says. If you need such helper methods,
     * define them in a different class. If you only need them in this test
     * class, then keep them in this test class.
     */


    /* Copyright (c) 2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */
}
