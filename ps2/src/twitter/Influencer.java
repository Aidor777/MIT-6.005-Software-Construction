package twitter;

import java.util.Comparator;

public class Influencer {

    public static final Comparator<Influencer> FOLLOWERS_COMPARATOR = Comparator.comparing(Influencer::getFollowerCount).reversed();

    private String name;

    private Integer followerCount;

    public Influencer(String name, Integer followerCount) {
        this.name = name;
        this.followerCount = followerCount;
    }

    public String getName() {
        return name;
    }

    public Integer getFollowerCount() {
        return followerCount;
    }

}
