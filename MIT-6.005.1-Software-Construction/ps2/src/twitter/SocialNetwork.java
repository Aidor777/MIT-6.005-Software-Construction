package twitter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SocialNetwork provides methods that operate on a social network.
 * 
 * A social network is represented by a Map<String, Set<String>> where map[A] is
 * the set of people that person A follows on Twitter, and all people are
 * represented by their Twitter usernames. Users can't follow themselves. If A
 * doesn't follow anybody, then map[A] may be the empty set, or A may not even exist
 * as a key in the map; this is true even if A is followed by other people in the network.
 * Twitter usernames are not case sensitive, so "ernie" is the same as "ERNie".
 * A username should appear at most once as a key in the map or in any given
 * map[A] set.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class SocialNetwork {

    /**
     * Guess who might follow whom, from evidence found in tweets.
     * 
     * @param tweets
     *            a list of tweets providing the evidence, not modified by this
     *            method.
     * @return a social network (as defined above) in which Ernie follows Bert
     *         if and only if there is evidence for it in the given list of
     *         tweets.
     *         One kind of evidence that Ernie follows Bert is if Ernie
     *         @-mentions Bert in a tweet. This must be implemented. Other kinds
     *         of evidence may be used at the implementor's discretion.
     *         All the Twitter usernames in the returned social network must be
     *         either authors or @-mentions in the list of tweets.
     */
    public static Map<String, Set<String>> guessFollowsGraph(List<Tweet> tweets) {
        Map<String, Set<String>> followersGraph = new HashMap<>();

        Map<String, List<Tweet>> tweetsByUser = tweets.stream()
                .collect(Collectors.groupingBy(tweet -> tweet.getAuthor().toLowerCase(Locale.ROOT)));

        tweetsByUser.forEach((author, theirTweets) -> {
            Set<String> mentionedUsers = Extract.getMentionedUsers(theirTweets);
            mentionedUsers.remove(author);
            followersGraph.put(author, mentionedUsers);
        });

        return followersGraph;
    }

    /**
     * Find the people in a social network who have the greatest influence, in
     * the sense that they have the most followers.
     * 
     * @param followsGraph
     *            a social network (as defined above)
     * @return a list of all distinct Twitter usernames in followsGraph, in
     *         descending order of follower count.
     */
    public static List<String> influencers(Map<String, Set<String>> followsGraph) {
        Map<String, Integer> followerCountByAuthor = new HashMap<>();
        followsGraph.keySet().forEach(follower -> {
            Stream<String> followedBy = followsGraph.get(follower).stream().map(person -> person.toLowerCase(Locale.ROOT));
            followedBy.forEach(person -> followerCountByAuthor.put(person, followerCountByAuthor.getOrDefault(person, 0) + 1));
        });

        List<Influencer> influencers = followerCountByAuthor.entrySet().stream()
                .map(entry -> new Influencer(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        return influencers.stream().sorted(Influencer.FOLLOWERS_COMPARATOR).map(influencer -> influencer.getName())
                .collect(Collectors.toList());
    }

    /* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */
}
