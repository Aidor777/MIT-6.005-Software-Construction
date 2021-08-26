package twitter;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Extract consists of methods that extract information from a list of tweets.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class Extract {

    /**
     * Get the time period spanned by tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return a minimum-length time interval that contains the timestamp of
     *         every tweet in the list.
     */
    public static Timespan getTimespan(List<Tweet> tweets) {
        if (tweets == null || tweets.isEmpty()) {
            return null;
        }

        Tweet firstTweet = tweets.get(0);
        Instant start = firstTweet.getTimestamp(), end = start;

        for (Tweet tweet : tweets) {
            if (tweet.getTimestamp().isBefore(start)) {
                start = tweet.getTimestamp();
            } else if (tweet.getTimestamp().isAfter(end)) {
                end = tweet.getTimestamp();
            }
        }

        return new Timespan(start, end);
    }

    /**
     * Get usernames mentioned in a list of tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return the set of usernames who are mentioned in the text of the tweets.
     *         A username-mention is "@" followed by a Twitter username (as
     *         defined by Tweet.getAuthor()'s spec).
     *         The username-mention cannot be immediately preceded or followed by any
     *         character valid in a Twitter username. (is is not also 'invalid' here ?)
     *         For this reason, an email address like bitdiddle@mit.edu does NOT 
     *         contain a mention of the username mit.
     *         Twitter usernames are case-insensitive, and the returned set may
     *         include a username at most once.
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        if (tweets == null || tweets.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> mentionedUsers = new HashSet<>();
        // Pattern that only matches a sequence of valid user name characters
        Pattern userPattern = Pattern.compile("(([0-9]|[a-zA-Z]|-|_)+)");

        for (Tweet tweet : tweets) {
            String text = tweet.getText();
            List<String> mentions = Arrays.stream(text.split(" ")).filter(word -> word.startsWith("@"))
                    .map(candidate -> getRegexFirstMatch(candidate, userPattern)).filter(optional -> optional.isPresent())
                    .map(presentOptional -> presentOptional.get()).collect(Collectors.toList());
            mentions.forEach(mention -> mentionedUsers.add(mention.toLowerCase(Locale.ROOT)));
        }

        return mentionedUsers;
    }

    // Helper that optionally returns the first substring in str that matches the pattern
    private static Optional<String> getRegexFirstMatch(String str, Pattern pattern) {
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        } else {
            return Optional.empty();
        }
    }

    /* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */
}
