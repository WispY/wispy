package com.wispy.wispy.service;

import com.wispy.wispy.controller.Session;
import com.wispy.wispy.processor.Task;
import org.apache.log4j.Logger;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author WispY
 */
@Component
public class MergeService {
    public static final Logger LOG = Logger.getLogger(MergeService.class);

    @Value("${github.organization}") private String defaultOrganization;

    public void listPullRequests(Task task, Session session) {
        GitHub github = session.get("github");
        if (github == null) {
            task.log("github not found in session");
            task.append("You have to be logged in to use this command");
            task.setFailed(true);
            return;
        }

        String overrideOrganization = task.getArguments()[0];
        String organizationName = StringUtils.hasText(overrideOrganization) ? overrideOrganization : defaultOrganization;

        task.log("creating async task to list pull requests from '{0}'", organizationName);
        task.setAsync(async -> {
            async.log("starting async listing task from '{0}'", organizationName);
            long time = System.currentTimeMillis();

            List<GHRepository> repositories = new LinkedList<>();
            if (organizationName.equals("personal")) {
                repositories.addAll(github.getMyself().getRepositories().values());
            } else {
                GHOrganization organization = github.getOrganization(organizationName);
                if (organization == null) {
                    async.log("organization '{0}' not found");
                    async.append("Organization `{0}` was not found");
                    async.setFailed(true);
                    return;
                }
                repositories.addAll(organization.getRepositories().values());
            }
            int repositoryCount = repositories.size();
            async.log("will search for pull requests in {0} repositories", repositoryCount);

            Map<GHRepository, List<GHPullRequest>> pullRequests = new TreeMap<>((f, s) -> f.getName().compareTo(s.getName()));

            Iterator<GHRepository> iterator = repositories.iterator();
            while (iterator.hasNext()) {
                GHRepository repository = iterator.next();
                List<GHPullRequest> requests = repository.getPullRequests(GHIssueState.OPEN);
                if (requests.isEmpty()) {
                    iterator.remove();
                } else {
                    Collections.sort(requests, (f, s) -> Integer.compare(f.getNumber(), s.getNumber()));
                    pullRequests.put(repository, requests);
                }
            }

            List<GHPullRequest> listedRequests = new LinkedList<>();
//            requests.put(user, listedRequests);
//
//            output.add("");
//            int index = 0;
//            if (pullRequests.isEmpty()) {
//                output.add("No open pull requests found");
//            } else {
//                for (Map.Entry<GHRepository, List<GHPullRequest>> entry : pullRequests.entrySet()) {
//                    GHRepository repository = entry.getKey();
//                    output.add(link(repository.getName(), repository.getHtmlUrl()) + " " + (repository.isPrivate() ? "(private)" : "(public)"));
//                    for (GHPullRequest request : entry.getValue()) {
//                        output.add("> `" + index + "` " +
//                                wrappedLink("link", request.getHtmlUrl()) + " " +
//                                wrappedLink("ticket", ticketLink(request.getTitle(), request.getHead().getRef())) + " " +
//                                request.getTitle() +
//                                (request.getMergeable() ? "" : " `can't merge!`")
//                        );
//                        listedRequests.add(request);
//                        index++;
//                    }
//                }
//                output.add("");
//                output.add("Merge with: `/git merge id [message]`");
//            }
//
//            time = System.currentTimeMillis() - time;
//            output.add(0, "Looked at `" + repositoryCount + "` repositories in `" + time + " ms`");
//            return success(text(output));
            return;
        });
        task.log("async task for listing created");
        task.append("Looking for pull requests... Please, wait");
    }
}