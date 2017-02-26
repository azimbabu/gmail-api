package com.azimbabu.mail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 */
public class GmailSearchService<T> implements MailSearchService<T> {

    public static final char MULTIPART_JOINER = '\n';
    private static Logger logger = LoggerFactory.getLogger(GmailSearchService.class);

    private static final String USER_ID = "me";

    private static final String FULL_FORMAT = "full";

    private static final String OFFLINE = "offline";

    /** Application name. */
    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/gmail-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY);

    /** Directory to store user credentials for this application. */
    private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".credentials/gmail-java-quickstart");

    /** gmail client secret json file */
    private static final String CLIENT_SECRET_JSON = "client_secret.json";

    /** Global instance of the {@link DataStoreFactory} */
    private static DataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    private final Gmail gmailService;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (final Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public GmailSearchService() throws IOException {
        this.gmailService = getGmailService();
    }

    @Override
    public Optional<T> search(final Set<String> searchLabelNames, final Function<List<String>, T> parser) throws IOException {
        final ListLabelsResponse listLabelsResponse = gmailService.users().labels().list(USER_ID).execute();
        final List<Label> labels = listLabelsResponse.getLabels();

        final List<String> searchLabelIds = new ArrayList<>();

        if (labels.isEmpty()) {
            logger.error("No labels found.");
            return Optional.empty();
        } else {
            for (final Label label : labels) {
                if (searchLabelNames.contains(label.getName())) {
                    searchLabelIds.add(label.getId());
                }
            }
        }

        if (searchLabelIds.isEmpty()) {
            logger.error("No matching labels found {}.", searchLabelNames);
            return Optional.empty();
        } else {
            final List<String> messages = listMessagesWithLabels(searchLabelIds);
            return Optional.of(parser.apply(messages));
        }
    }


    private Gmail getGmailService() throws IOException {
        Credential credential = authorize();
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential authorize() throws IOException {
        // Load client secrets.
        final InputStream inputStream = GmailSearchService.class.getResourceAsStream("/" + CLIENT_SECRET_JSON);
        final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));

        // Build flow and trigger user authorization request.
        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType(OFFLINE)
                .build();

        final Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

        return credential;
    }

    /**
     * List all Messages of the user's mailbox with labelIds applied.
     *
     * @param labelIds Only return Messages with these labelIds applied.
     * @throws IOException
     */
    private List<String> listMessagesWithLabels(final List<String> labelIds) throws IOException {
        ListMessagesResponse response = this.gmailService.users().messages().list(USER_ID).setLabelIds(labelIds).execute();

        final List<Message> messages = new ArrayList<>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = this.gmailService.users().messages().list(USER_ID).setLabelIds(labelIds)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }

        final List<String> messageTexts = messages.stream().map(this::parseBody).collect(Collectors.toList());

        return messageTexts;
    }

    private String parseBody(final Message message) {
        try {
            final Message fullMessage = this.gmailService.users().messages().get(USER_ID, message.getId()).setFormat(FULL_FORMAT).execute();

            byte[] decodeData = null;
            if (fullMessage.getPayload() != null && fullMessage.getPayload().getBody() != null) {
                decodeData = fullMessage.getPayload().getBody().decodeData();
            }

            if (decodeData != null) {
                return new String(decodeData);
            }

            if (fullMessage.getPayload() != null && fullMessage.getPayload().getParts() != null){
                final List<String> parts = fullMessage.getPayload().getParts()
                        .stream()
                        .map(part -> new String(part.getBody().decodeData()))
                        .collect(Collectors.toList());
                return StringUtils.join(parts, MULTIPART_JOINER);

            } else {
                return new String(Base64.getDecoder().decode(fullMessage.getRaw()));
            }
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
