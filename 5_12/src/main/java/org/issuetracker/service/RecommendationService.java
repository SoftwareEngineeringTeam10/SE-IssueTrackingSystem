package org.issuetracker.service;

import org.issuetracker.model.Issue;
import org.issuetracker.model.RecommendationResult;
import java.util.List;

public interface RecommendationService {

    List<RecommendationResult> recommend(Issue targetIssue, List<Issue> historicalIssues);
}