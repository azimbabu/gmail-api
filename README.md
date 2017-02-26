## What?

An email search service to search for emails associated with one or more labels or folders and apply a custom parser on each of these emails to extract useful information.

It accepts any parser implementing this functional interface:
```java 
Function<List<String>, T> parser
```
i.e. the implementation should accept a list of String representing a list of email messages(body) and will return any data type of your choice.

## Prerequisites??

### Turn on the Gmail API 

 - Use [this wizard](https://console.developers.google.com/start/api?id=gmail) to create or select a project in the Google Developers Console and automatically turn on the API. Click Continue, then Go to credentials.
 - On the Add credentials to your project page, click the Cancel button.
 - At the top of the page, select the OAuth consent screen tab. Select an Email address, enter a Product name if not already set, and click the Save button.
 - Select the Credentials tab, click the Create credentials button and select OAuth client ID.
 - Select the application type Other, enter the name "Gmail API Quickstart" or anything you line, and click the Create button.
 - Click OK to dismiss the resulting dialog.
 - Click the file_download (Download JSON) button to the right of the client ID.
 - Move this file to src/main/resources directory and rename it client_secret.json.
 
### Write your own Parser
 
 An example parser is included src/main/java/GithubPullRequestParser.java and it's usage is demonstrated in src/main/java/MailSearchDemo.java
```
final MailSearchService<List<String>> mailSearchService = new GmailSearchService<>();
 
final GithubPullRequestParser parser = new GithubPullRequestParser();
 
final Optional<List<String>> optionalPullRequests = mailSearchService.search(Sets.newHashSet("My-Github"), parser::parse);
 
if (optionalPullRequests.isPresent()) {
    System.out.println(optionalPullRequests.get());
}
```

This example searches the user's mailbox for label "My-Github" and apply the GithubPullRequestParser to fetch a list of pull request urls.

Your parser does not necessarily need to return List\<String\> . It can return anything as long as it implements the funcrtional interface
```java 
Function<List<String>, T> parser
```