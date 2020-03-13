package repos.service

import repos.action.{CommitCommentDTO, RepoCommitComments, RepositoryDTO}
import repos.model.{AppSchema, CommitComment, Repository}
import org.squeryl.PrimitiveTypeMode._

/**
 * Сервис для работы с Repositories + CommitComments
 */
object RepoCommentsService {

  def updateRepoAndComments(data: RepoCommitComments): (String, Long) = {
    val incRepo = data.repository
    transaction {
      val repo: Option[Repository] = from(AppSchema.repos)(r => where(r.name === incRepo.name) select (r)).headOption

      val savedRepo: Repository = repo match {
        case None => {
          val newRepo = Repository(name = incRepo.name,
            description = incRepo.description,
            githubId = incRepo.githubId,
            htmlUrl = incRepo.htmlUrl)

          AppSchema.repos.insert(newRepo)
        }
        case Some(repo) => {
          //у существующего репо удаляем все комментарии, чтобы вставить новый полный список далее
          repo.commitComments.deleteAll
          val updatedRepo = repo.copy(description = incRepo.description,
            githubId = incRepo.githubId,
            htmlUrl = incRepo.htmlUrl)

          AppSchema.repos.update(updatedRepo)
          updatedRepo
        }
      }

      val comments: Seq[CommitComment] =
        data.commitComments.map(c =>
          CommitComment(repoId = savedRepo.id,
            body = c.body,
            githubId = c.githubId,
            httpUrl = c.httpUrl,
            userLogin = c.userLogin))

      AppSchema.commitComments.insert(comments)

      repo match {
        case None => ("INSERT", savedRepo.id)
        case Some(_) => ("UPDATE", savedRepo.id)
      }
    }
  }

  def getCommitCommentsByRepo(repoId: Long): Iterable[CommitCommentDTO] = {
    transaction {
      val comments = from(AppSchema.commitComments)(c => where(c.repoId === repoId) select (c))

      val commentsResponse: Iterable[CommitCommentDTO] =
        comments.map(c =>
          CommitCommentDTO(body = c.body,
            githubId = c.githubId,
            httpUrl = c.httpUrl,
            userLogin = c.userLogin)).seq

      commentsResponse
    }
  }

  def getRepoByName(repoName: String): Option[RepositoryDTO] = {
    transaction {
      val repo: Option[Repository] = from(AppSchema.repos)(r => where(r.name === repoName) select (r)).headOption
      repo match {
        case Some(r) => Some(RepositoryDTO(githubId = r.githubId,
          name = r.name,
          description = r.description,
          htmlUrl = r.htmlUrl,
          id = Option(r.id)))
        case None => None
      }
    }
  }


  def deleteRepoByName(repoName: String): Option[Long] = {
    transaction {
      val repo: Option[Repository] = from(AppSchema.repos)(r => where(r.name === repoName) select (r)).headOption
      repo match {
        case Some(r) => {
          Option(r.commitComments).map(c => c.deleteAll)
          AppSchema.repos.delete(r.id)
          Some(r.id)
        };
        case None => None
      }
    }
  }
}
