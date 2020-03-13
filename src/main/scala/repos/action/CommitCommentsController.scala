package repos.action

import io.netty.handler.codec.http.HttpResponseStatus
import repos.service.RepoCommentsService
import xitrum.{Action, SkipCsrfCheck}
import xitrum.annotation.{DELETE, GET, POST, Swagger}
import xitrum.util.SeriDeseri
import xitrum.validator.Required


@Swagger(
  Swagger.Tags("Операции с репозиториями Github и их комментариями к коммитам."),
  Swagger.Produces("application/json")
)
trait Api extends Action with SkipCsrfCheck

@POST("update/repo-and-comments")
@Swagger(
  Swagger.Summary("Обновить/создать репозиторий + его комментраии к коммитам"),
  Swagger.Response(200, "[UPDATE или INSERT] + id репозитория"),
  Swagger.StringPath("repoName", "Имя реопзитория в в Github")
)
class UpdateRepoAndComments extends Api {
  def execute() {
    try {
      var data: Option[RepoCommitComments] = SeriDeseri.fromJValue[RepoCommitComments](requestContentJValue)
      val res = data match {
        case Some(r) => {
          val res = RepoCommentsService.updateRepoAndComments(r)
          respondJson(Map("success" -> res))
        }
        case None => {
          response.setStatus(HttpResponseStatus.BAD_REQUEST)
          respondJson(Map("error" -> "Не верно указаны параметры"))
        }
      }
    } catch {
      case e: Exception => {
        log.error("Ошибка при обновлении репозиториев", e)
        response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR)
        respondJson(Map("error" -> "Не удалось обновить репозиторий"))
      }
    }
  }
}

@DELETE("delete/repo-and-comments/:repoName")
@Swagger(
  Swagger.Summary("Удалить данные о репозитории и все его комменты"),
  Swagger.Response(200, "ID удаленного репо в системе"),
  Swagger.StringPath("repoName", "Имя реопзитория в в Github")
)
class DeleteRepoByName extends Api {
  def execute() {
    val repoName = param("repoName")
    Required.exception("repoName", repoName)

    try {
      RepoCommentsService.deleteRepoByName(repoName) match {
        case Some(id) => respondJson(Map("success" -> ("id", id)))
        case None => respondJson(Map("success" -> "{}"))
      }
    } catch {
      case e: Exception => {
        log.error("Ошибка при удалении репозитория", e)
        response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR)
        respondJson(Map("error" -> "Не удалось удалить репозиторий"))
      }
    }
  }
}

@GET("repo/:repoName")
@Swagger(
  Swagger.Summary("Получить репозиторий по имени"),
  Swagger.Response(200, "Данные о найденном репозитории"),
  Swagger.StringPath("repoName", "Имя реопзитория в Github")
)
class GetRepoByName extends Api {
  def execute() {
    val repoName = param("repoName")
    Required.exception("repoName", repoName)
    try {
      RepoCommentsService.getRepoByName(repoName) match {
        case Some(r) => respondJson(Map("success" -> r))
        case None => respondJson(Map("success" -> "{}"))
      }
    } catch {
      case e: Exception => {
        log.error("Ошибка при получении репозитория", e)
        response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR)
        respondJson(Map("error" -> "Не удалось получить репозиторий"))
      }
    }
  }
}

@GET("comments/:repoId")
@Swagger(
  Swagger.Summary("Получить комментарии репозитория"),
  Swagger.Response(200, "Данные о комментариях"),
  Swagger.StringPath("repoId", "id репозитория в системе")
)
class GetCommentCommitsByRepo extends Api {
  def execute() {
    val repoId: Long = param[Long]("repoId")
    try {
      respondJson(Map("success" -> RepoCommentsService.getCommitCommentsByRepo(repoId)))
    } catch {
      case e: Exception => {
        log.error("Ошибка при получении комментариев", e)
        response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR)
        respondJson(Map("error" -> "Не удалось получить комментарии"))
      }
    }
  }
}