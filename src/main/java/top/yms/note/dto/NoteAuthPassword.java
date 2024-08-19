package top.yms.note.dto;

/**
 * Created by yangmingsen on 2024/8/19.
 */
public class NoteAuthPassword implements NoteAuth{

    private String username;
    private String password;


    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }


    @Override
    public String toString() {
        return "NoteAuthPassword{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
