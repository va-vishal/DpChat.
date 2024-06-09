package in.connect;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String imageurl;
    public Map<String, Boolean> followers = new HashMap<>();
    public Map<String, Boolean> following = new HashMap<>();

    private String bio;

    public User() {
    }

    public User(String id, String username, String fullName, String email, String imageurl, Map<String, Boolean> followers, Map<String, Boolean> following, String bio) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.imageurl = imageurl;
        this.followers = followers;
        this.following = following;
        this.bio = bio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public Map<String, Boolean> getFollowers() {
        return followers;
    }

    public void setFollowers(Map<String, Boolean> followers) {
        this.followers = followers;
    }

    public Map<String, Boolean> getFollowing() {
        return following;
    }

    public void setFollowing(Map<String, Boolean> following) {
        this.following = following;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
