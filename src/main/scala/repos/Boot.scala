package repos

import xitrum.Server
import xitrum.util.Loader
import org.squeryl._
import adapters.PostgreSqlAdapter

object Config {
  val properties = Loader.propertiesFromFile("config/config.properties")
  val liquiProperties = Loader.propertiesFromFile("liquibase/liquibase.properties")

  def init = {
    Class.forName("org.postgresql.Driver");
    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        java.sql.DriverManager.getConnection(
          "jdbc:postgresql://localhost:5432/repositories", Config.liquiProperties.get("username").toString, Config.liquiProperties.get("password").toString),
        new PostgreSqlAdapter)
    )
  }
}


object Boot {

  def main(args: Array[String]) {
    Config.init
    Server.start()
    Server.stopAtShutdown()
  }
}
