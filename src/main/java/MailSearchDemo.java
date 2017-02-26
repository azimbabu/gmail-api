import com.azimbabu.mail.GmailSearchService;
import com.azimbabu.mail.MailSearchService;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 */
public class MailSearchDemo {

    public static void main(String[] args) throws IOException {
        final MailSearchService<List<String>> mailSearchService = new GmailSearchService<>();

        final GithubPullRequestParser parser = new GithubPullRequestParser();

        final Optional<List<String>> optionalPullRequests = mailSearchService.search(Sets.newHashSet("My-Github"), parser::parse);

        if (optionalPullRequests.isPresent()) {
            System.out.println(optionalPullRequests.get());
        }
    }

}
