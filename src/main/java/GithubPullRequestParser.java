import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses email message for github pull request urls.
 */
public class GithubPullRequestParser {

    private static final String PR_PREFIX = "You can view, comment on, or merge this pull request online at:[\r\n]+  ";

    private static final String URL_REGEX = "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" +
            "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +
            "|mil|biz|info|mobi|name|aero|jobs|museum" +
            "|travel|[a-z]{2}))(:[\\d]{1,5})?" +
            "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" +
            "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
            "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
            "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b";

    /**
     * Parses email messages for github pull request urls and returns a list of pr urls.
     * @param messages
     * @return
     */
    public List<String> parse(final List<String> messages) {
        final List<String> pullRequests = new ArrayList<>();

        for (final String message : messages) {
            final Optional<String> pullRequest = parseMessage(message);
            if (pullRequest.isPresent()) {
                pullRequests.add(pullRequest.get());
            }
        }

        return pullRequests;
    }

    private Optional<String> parseMessage(final String message) {
        if (!message.contains("You can view, comment on, or merge this pull request online at")) {
            return Optional.empty();
        } else {
            final String regex = String.format("(%s)(", PR_PREFIX) + URL_REGEX + ")";
            final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return Optional.of(matcher.group(2).trim());
            } else {
                return Optional.empty();
            }
        }
    }
}
