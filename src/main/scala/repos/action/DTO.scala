package repos.action

case class RepositoryDTO(githubId: Long, name: String, description: String, htmlUrl: String, id: Option[Long])

case class CommitCommentDTO(githubId: Long, httpUrl: String, body: String, userLogin: String)

case class RepoCommitComments(repository: RepositoryDTO, commitComments: Seq[CommitCommentDTO])
