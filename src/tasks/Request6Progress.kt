package tasks

import contributors.*

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .body() ?: emptyList()

    val result = repos.foldRight(emptyList<User>(), { repo, acc ->
        val list = (service
            .getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList() + acc).aggregate()
        updateResults(list, false)
        list
    })

    updateResults(result, true)
}
