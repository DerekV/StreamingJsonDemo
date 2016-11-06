
public class GithubIssue {
    private int number;
    private int id;
    private String url;
    private String title;

    public int getNumber() {
        return number;
    }

    public GithubIssue withNumber(int number) {
        this.number = number;
        return this;
    }

    public int getId() {
        return id;
    }

    public GithubIssue withId(int id) {
        this.id = id;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public GithubIssue withUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public GithubIssue withTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public String toString() {
        return "GithubIssue{" +
                "number=" + number +
                ", id=" + id +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
