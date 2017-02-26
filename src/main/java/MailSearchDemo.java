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
//        final PavlovParser pavlovParser = new PavlovParser();
//
//        final MailSearchService<Map<String, Map<String, String>>> mailSearchService = new GmailSearchService<>();
//        final Optional<Map<String, Map<String, String>>> optionalCampaigns = mailSearchService.search(Sets.newHashSet("pavlov-handled"), pavlovParser::parse);
//        if (optionalCampaigns.isPresent()) {
//            System.out.println(optionalCampaigns.get().keySet());
//        }

        final GithubPullRequestParser parser = new GithubPullRequestParser();
        final MailSearchService<List<String>> mailSearchService = new GmailSearchService<>();
        final Optional<List<String>> optionalPullRequests = mailSearchService.search(Sets.newHashSet("My-Github"), parser::parse);
        if (optionalPullRequests.isPresent()) {
            System.out.println(optionalPullRequests.get());
        }
    }

}
