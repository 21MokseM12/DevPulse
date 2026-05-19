package backend.academy.scrapper.model.github.mappers;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.UpdateType;
import backend.academy.scrapper.model.github.GithubCommit;
import backend.academy.scrapper.model.github.GithubLabel;
import backend.academy.scrapper.model.github.GithubResponse;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GithubResponseMapper {

    private static final int bodyLength = 200;

    public static LinkUpdateDTO mapToPullRequest(GithubResponse response, URI repoLink) {
        String bodyPreview = truncatePreview(response.payload().pullRequest().body());
        URI eventUrl = extractUri(response.payload().pullRequest().htmlUrl(), repoLink);
        return new LinkUpdateDTO(
                response.id(),
                response.payload().pullRequest().title(),
                response.actor().login(),
                response.creationDate(),
                bodyPreview,
                UpdateType.GITHUB_PULL_REQUEST,
                extractLabels(response.payload().pullRequest().labels()),
                eventUrl);
    }

    public static LinkUpdateDTO mapToIssue(GithubResponse response, URI repoLink) {
        String bodyPreview = truncatePreview(response.payload().issue().body());
        URI eventUrl = extractUri(response.payload().issue().htmlUrl(), repoLink);
        return new LinkUpdateDTO(
                response.id(),
                response.payload().issue().title(),
                response.actor().login(),
                response.creationDate(),
                bodyPreview,
                UpdateType.GITHUB_ISSUE,
                extractLabels(response.payload().issue().labels()),
                eventUrl);
    }

    public static LinkUpdateDTO mapToCommit(GithubResponse response, URI repoLink) {
        List<GithubCommit> commits = response.payload().commits() == null
                ? List.of()
                : response.payload().commits();
        String branchName = extractBranchName(response.payload().ref());
        String title =
                switch (commits.size()) {
                    case 0 -> "Push в ветке " + branchName;
                    case 1 -> "Новый коммит в ветке " + branchName;
                    default -> "Новые коммиты (" + commits.size() + ") в ветке " + branchName;
                };
        String description =
                buildCommitDescription(branchName, response.payload().head(), commits);
        return new LinkUpdateDTO(
                response.id(),
                title,
                response.actor().login(),
                response.creationDate(),
                description,
                UpdateType.GITHUB_COMMIT,
                Set.of(),
                buildPushEventUrl(
                        repoLink,
                        response.payload().before(),
                        response.payload().head()));
    }

    private static String buildCommitDescription(String branchName, String head, List<GithubCommit> commits) {
        String shortHead = shortenSha(head);
        if (commits.isEmpty()) {
            return shortHead == null
                    ? "Push в " + branchName + ": детали коммитов недоступны"
                    : "Push в " + branchName + ": HEAD " + shortHead + " (детали коммитов недоступны)";
        }

        String commitLabel = formatCommitLabel(commits.size());
        String base = shortHead == null
                ? "Push в " + branchName + ": " + commits.size() + " " + commitLabel
                : "Push в " + branchName + ": " + commits.size() + " " + commitLabel + " (" + shortHead + ")";
        String messages = commits.stream()
                .filter(commit -> commit != null
                        && commit.message() != null
                        && !commit.message().isBlank())
                .limit(3)
                .map(GithubResponseMapper::normalizeMessage)
                .collect(Collectors.joining("; "));
        return messages.isBlank() ? base : base + ", " + messages;
    }

    private static String formatCommitLabel(int commitsCount) {
        int mod10 = commitsCount % 10;
        int mod100 = commitsCount % 100;
        if (mod10 == 1 && mod100 != 11) {
            return "коммит";
        }
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) {
            return "коммита";
        }
        return "коммитов";
    }

    private static String normalizeMessage(GithubCommit commit) {
        String message = commit.message().trim();
        int newLineIndex = message.indexOf('\n');
        if (newLineIndex >= 0) {
            message = message.substring(0, newLineIndex);
        }
        return message.trim();
    }

    private static Set<String> extractLabels(List<GithubLabel> labels) {
        if (labels == null || labels.isEmpty()) {
            return Set.of();
        }
        return labels.stream()
                .map(GithubLabel::name)
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    private static String extractBranchName(String ref) {
        if (ref == null || ref.isBlank()) {
            return "unknown";
        }
        String normalizedRef = ref.trim();
        int slashIndex = normalizedRef.lastIndexOf('/');
        if (slashIndex == -1 || slashIndex == normalizedRef.length() - 1) {
            return normalizedRef;
        }
        return normalizedRef.substring(slashIndex + 1);
    }

    private static String shortenSha(String sha) {
        if (sha == null || sha.isBlank()) {
            return null;
        }
        String trimmedSha = sha.trim();
        return trimmedSha.substring(0, Math.min(7, trimmedSha.length()));
    }

    private static String truncatePreview(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > bodyLength ? body.substring(0, bodyLength).concat("...") : body;
    }

    private static URI buildPushEventUrl(URI repoLink, String before, String head) {
        if (repoLink == null) {
            return null;
        }
        String base = normalizeRepoUrl(repoLink.toString());
        if (isPresent(before) && isPresent(head) && !isZeroSha(before) && !isZeroSha(head) && !before.equals(head)) {
            return URI.create(base + "/compare/" + before + "..." + head);
        }
        if (isPresent(head) && !isZeroSha(head)) {
            return URI.create(base + "/commit/" + head);
        }
        return repoLink;
    }

    private static URI extractUri(String rawUrl, URI fallback) {
        if (!isPresent(rawUrl)) {
            return fallback;
        }
        return URI.create(rawUrl);
    }

    private static String normalizeRepoUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean isZeroSha(String value) {
        return "0000000000000000000000000000000000000000".equals(value);
    }
}
