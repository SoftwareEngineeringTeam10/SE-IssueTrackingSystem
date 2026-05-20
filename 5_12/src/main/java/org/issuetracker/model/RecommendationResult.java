package org.issuetracker.model;

import java.util.Collections;
import java.util.List;

public class RecommendationResult {
    private final String fixerId;
    private final double score;
    private final List<Integer> matchedIssueIds;

    public RecommendationResult(String fixerId, double score, List<Integer> matchedIssueIds) {
        this.fixerId = fixerId;
        this.score = score;
        this.matchedIssueIds = Collections.unmodifiableList(matchedIssueIds);
    }

    public String getFixerId() { return fixerId; }
    public double getScore() { return score; }
    public List<Integer> getMatchedIssueIds() { return matchedIssueIds; }
}