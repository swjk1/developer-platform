package com.leon.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * The slices of GitHub's responses this project reads.
 *
 * <p>Ignoring unknown properties is required, not merely convenient: GitHub
 * adds fields to these payloads regularly, and a strict binding would turn a
 * routine upstream change into an outage here.
 */
final class GitHubPayloads {

    private GitHubPayloads() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Repo(
            @JsonProperty("stargazers_count") Integer stars,
            @JsonProperty("forks_count") Integer forks,
            @JsonProperty("open_issues_count") Integer openIssues,
            @JsonProperty("language") String language,
            @JsonProperty("pushed_at") Instant pushedAt) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Release(@JsonProperty("tag_name") String tagName) {
    }
}
