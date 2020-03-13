package repos.model

import org.squeryl._
import org.squeryl.annotations.Column
import PrimitiveTypeMode._

case class Repository(
                       @Column("github_id")
                       githubId: Long,
                       @Column("name")
                       name: String,
                       @Column("description")
                       description: String,
                       @Column("html_url")
                       htmlUrl: String,
                       id: Long = -1) extends KeyedEntity[Long] {
  lazy val commitComments = AppSchema.repoComments.left(this)
}

case class CommitComment(
                          @Column("repo_id")
                          repoId: Long,
                          body: String,
                          @Column("github_id")
                          githubId: Long,
                          @Column("http_url")
                          httpUrl: String,
                          @Column("user_login")
                          userLogin: String,
                          id: Long = -1) extends KeyedEntity[Long] {
  lazy val repository = AppSchema.repoComments.right(this).single
}

object AppSchema extends Schema {
  val repos = table[Repository]("repositories")
  on(repos)(r => declare(
    r.id is (autoIncremented("s_repositories_id"))
  ))
  val commitComments = table[CommitComment](name = "commit_comments")
  on(commitComments)(c => declare(
    c.id is (autoIncremented("s_commit_comments_id"))
  ))
  val repoComments = oneToManyRelation(repos, commitComments) via ((r, c) => r.id === c.repoId)

  override def applyDefaultForeignKeyPolicy(foreignKeyDeclaration: ForeignKeyDeclaration) =
    foreignKeyDeclaration.constrainReference

  repoComments.foreignKeyDeclaration.constrainReference(onDelete cascade)
}
