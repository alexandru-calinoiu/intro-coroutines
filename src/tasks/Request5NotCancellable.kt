package tasks

import contributors.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val result = GlobalScope.async {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .body() ?: emptyList()

        val promises = repos.map { repo ->
            async {
                delay(3000)
                service
                    .getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
            }
        }

        promises.awaitAll().flatten().aggregate()
    }

    return result.await()
}