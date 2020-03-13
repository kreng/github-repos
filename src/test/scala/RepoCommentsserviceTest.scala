import org.scalatest.{BeforeAndAfter, FunSuite}
import org.squeryl.PrimitiveTypeMode.{from, select, transaction}
import org.squeryl._
import org.squeryl.adapters.PostgreSqlAdapter
import repos.action.{CommitCommentDTO, RepoCommitComments, RepositoryDTO}
import repos.model.{AppSchema, CommitComment, Repository}
import repos.service.RepoCommentsService

/**
 * Тесты для сервиса репозиториев и комментов
 * Тестирование на H2 не настроено. Работает с подключением к Postgres
 */
class CubeCalculatorTest extends FunSuite with BeforeAndAfter {

  def config() = {
    Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        java.sql.DriverManager.getConnection(
          "jdbc:h2:~/test", "test", ""),
        new PostgreSqlAdapter)
    )
  }

  config()

  before {
    transaction {
      //TODO поправить dirty context для работы с H2
      //          AppSchema.drop
      //          AppSchema.create
      //          AppSchema.printDdl

      val savedRepo1 = AppSchema.repos.insert(Repository(11, "nameRepo1", "descr1", "html1"))
      val comment11 = AppSchema.commitComments.insert(CommitComment(savedRepo1.id, "body11", 111, "httpUrl11", "userLogin11"))
      val comment12 = AppSchema.commitComments.insert(CommitComment(savedRepo1.id, "body12", 112, "httpUrl12", "userLogin12"))

      val savedRepo2 = AppSchema.repos.insert(Repository(12, "nameRepo2", "descr2", "html2"))
      savedRepo2.commitComments.associate(CommitComment(savedRepo2.id, "body21", 121, "httpUrl21", "userLogin21"))
      val comment22 = AppSchema.commitComments.insert(CommitComment(savedRepo2.id, "body22", 122, "httpUrl22", "userLogin22"))
      savedRepo2.commitComments.associate(CommitComment(savedRepo2.id, "body23", 123, "httpUrl23", "userLogin23"))

      AppSchema.repos.insert(Repository(13, "nameRepo3", "descr3", "html3"))
    }
  }

  //  after {
  //    transaction {
  //      AppSchema.drop
  //    }
  //  }

  test("Repos have comments") {
    transaction {
      val repos = from(AppSchema.repos)(r =>
        select(r))
      assert(repos.size == 3)

      var commentSize = repos.filter(r => r.githubId == 11).head.commitComments.size
      assert(commentSize == 2)

      commentSize = repos.filter(r => r.githubId == 12).head.commitComments.size
      assert(commentSize == 3)

      commentSize = repos.filter(r => r.githubId == 13).head.commitComments.size
      assert(commentSize == 0)

    }
  }

  test("Update repo and its comments") {
    val changedFirstRepo: RepositoryDTO = RepositoryDTO(githubId = 11, name = "nameRepo1", description = "changedDescr", htmlUrl = "html1", Some(-1))

    val data: RepoCommitComments = RepoCommitComments(
      changedFirstRepo,
      Seq(
        CommitCommentDTO(body = "changedBody11", githubId = 111, httpUrl = "httpUrl11", userLogin = "userLogin11"),
        CommitCommentDTO(body = "newBody1", githubId = 113, httpUrl = "httpUrl", userLogin = "userLogin"),
        CommitCommentDTO(body = "newBody2", githubId = 114, httpUrl = "httpUrl", userLogin = "userLogin")
      )
    )

    RepoCommentsService.updateRepoAndComments(data)

    transaction {
      val repos = from(AppSchema.repos)(r =>
        select(r))
      assert(repos.size == 3)

      val updatedRepo: Repository = repos.find(r => r.githubId == 11).getOrElse(throw new Exception("Не найден репозиторий"))
      assert(updatedRepo.description === "changedDescr")

      val commitComment = updatedRepo.commitComments
      assert(commitComment.size === 3)

      assert(commitComment.find(c => c.body === "changedBody11").get.githubId === 111)
      assert(commitComment.find(c => c.body === "newBody1").get.githubId === 113)
      assert(commitComment.find(c => c.body === "newBody2").get.githubId === 114)
    }
  }

  test("New Repo with comments") {
    val newRepo = RepositoryDTO(14, "nameRepo4", "descr4", "html4", Some(-1))
    val data: RepoCommitComments = RepoCommitComments(
      newRepo,
      Seq(
        CommitCommentDTO(body = "newBody1", githubId = 115, httpUrl = "httpUrl", userLogin = "userLogin"),
        CommitCommentDTO(body = "newBody2", githubId = 116, httpUrl = "httpUrl", userLogin = "userLogin")
      )
    )

    RepoCommentsService.updateRepoAndComments(data)

    transaction {
      val repos = from(AppSchema.repos)(r =>
        select(r))
      assert(repos.size == 4)

      val updatedRepo: Repository = repos.find(r => r.githubId == 14).getOrElse(throw new Exception("Не найден репозиторий"))
      assert(updatedRepo.description === "descr4")

      val commitComment = updatedRepo.commitComments
      assert(commitComment.size === 2)

      assert(commitComment.find(c => c.body === "newBody1").get.githubId === 115)
      assert(commitComment.find(c => c.body === "newBody2").get.githubId === 116)
    }
  }
}