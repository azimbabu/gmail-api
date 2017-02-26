package com.azimbabu.mail;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 */
public interface MailSearchService<T> {
    /**
     * Searches for emails having any of the label in searchLabelNames, parse them using the parser and returns the parsed object.
     * @param searchLabelNames
     * @param parser
     * @return
     */
    Optional<T> search(Set<String> searchLabelNames, Function<List<String>, T> parser) throws IOException;
}
