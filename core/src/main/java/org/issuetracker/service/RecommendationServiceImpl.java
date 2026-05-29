package org.issuetracker.service;

import org.issuetracker.model.Issue;
import org.issuetracker.model.RecommendationResult;
import java.util.*;

public class RecommendationServiceImpl implements RecommendationService {
    private static final double MIN_SIMILARITY = 0.05; // 최소 유사도 커트라인
    private static final int TOP_N = 3;                // 최대 추천 인원

    @Override
    public List<RecommendationResult> recommend(Issue targetIssue, List<Issue> historicalIssues) {
        if (historicalIssues == null || historicalIssues.isEmpty() || (targetIssue.title == null && targetIssue.description == null)) {
            return new ArrayList<>();
        }

        // 텍스트 결합 및 토큰화
        String targetText = ((targetIssue.title != null ? targetIssue.title : "") + " " + (targetIssue.description != null ? targetIssue.description : "")).toLowerCase();
        List<String> targetTokens = tokenize(targetText);
        if (targetTokens.isEmpty()) return new ArrayList<>();

        List<List<String>> allDocumentsTokens = new ArrayList<>();
        allDocumentsTokens.add(targetTokens);
        for (Issue hist : historicalIssues) {
            String histText = ((hist.title != null ? hist.title : "") + " " + (hist.description != null ? hist.description : "")).toLowerCase();
            allDocumentsTokens.add(tokenize(histText));
        }

        // TF-IDF 계산을 위한 단어 사전 및 IDF 맵 생성
        Set<String> vocabulary = new HashSet<>();
        for (List<String> docTokens : allDocumentsTokens) vocabulary.addAll(docTokens);

        Map<String, Double> idfMap = new HashMap<>();
        int totalDocs = allDocumentsTokens.size();
        for (String word : vocabulary) {
            int docCountWithWord = 0;
            for (List<String> docTokens : allDocumentsTokens) {
                if (docTokens.contains(word)) docCountWithWord++;
            }
            idfMap.put(word, Math.log((double) totalDocs / docCountWithWord) + 1.0);
        }

        // TF-IDF 벡터화 변환
        Map<String, Double> targetVector = calculateTfIdf(targetTokens, idfMap);
        List<Map<String, Double>> historicalVectors = new ArrayList<>();
        for (Issue hist : historicalIssues) {
            String histText = ((hist.title != null ? hist.title : "") + " " + (hist.description != null ? hist.description : "")).toLowerCase();
            historicalVectors.add(calculateTfIdf(tokenize(histText), idfMap));
        }

        // Cosine Similarity 계산 및 개발자별 최고 점수 그룹화
        Map<String, Double> fixerMaxScores = new HashMap<>();
        Map<String, List<Integer>> fixerMatchedIssues = new HashMap<>();

        for (int i = 0; i < historicalIssues.size(); i++) {
            Issue histIssue = historicalIssues.get(i);
            if (histIssue.fixer == null || histIssue.fixer.trim().isEmpty()) continue;

            double similarity = calculateCosineSimilarity(targetVector, historicalVectors.get(i));
            if (similarity < MIN_SIMILARITY) continue;

            String fixer = histIssue.fixer;
            if (!fixerMaxScores.containsKey(fixer)) {

                fixerMaxScores.put(fixer, similarity);

                List<Integer> matchedIds = new ArrayList<>();
                matchedIds.add(histIssue.id);

                fixerMatchedIssues.put(fixer, matchedIds);

            } else {

                if (similarity > fixerMaxScores.get(fixer)) {
                    fixerMaxScores.put(fixer, similarity);
                }

                fixerMatchedIssues
                        .get(fixer)
                        .add(histIssue.id);
            }
        }

        // Top-3 정렬 후 반환
        List<RecommendationResult> results = new ArrayList<>();
        for (String fixer : fixerMaxScores.keySet()) {
            results.add(new RecommendationResult(fixer, fixerMaxScores.get(fixer), fixerMatchedIssues.get(fixer)));
        }
        results.sort((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()));

        return results.size() > TOP_N ? results.subList(0, TOP_N) : results;
    }

    private List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) return new ArrayList<>();
        String cleanText = text.replaceAll("[^a-zA-Z0-9가-힣\\s]", "");
        String[] words = cleanText.split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String w : words) if (!w.trim().isEmpty()) tokens.add(w);
        return tokens;
    }

    private Map<String, Double> calculateTfIdf(List<String> tokens, Map<String, Double> idfMap) {
        Map<String, Double> tfIdfVector = new HashMap<>();
        if (tokens.isEmpty()) return tfIdfVector;

        Map<String, Integer> wordCounts = new HashMap<>();
        for (String token : tokens) wordCounts.put(token, wordCounts.getOrDefault(token, 0) + 1);

        for (String word : wordCounts.keySet()) {
            double tf = (double) wordCounts.get(word) / tokens.size();
            tfIdfVector.put(word, tf * idfMap.getOrDefault(word, 1.0));
        }
        return tfIdfVector;
    }

    private double calculateCosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        double dotProduct = 0.0, normA = 0.0, normB = 0.0;
        Set<String> allWords = new HashSet<>(v1.keySet());
        allWords.addAll(v2.keySet());

        for (String word : allWords) {
            double val1 = v1.getOrDefault(word, 0.0);
            double val2 = v2.getOrDefault(word, 0.0);
            dotProduct += val1 * val2;
            normA += val1 * val1;
            normB += val2 * val2;
        }
        return (normA == 0.0 || normB == 0.0) ? 0.0 : dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}