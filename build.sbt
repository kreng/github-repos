organization := "Sberbank"
name         := "github_repos"
version      := "1.0-SNAPSHOT"

scalaVersion := "2.12.10"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

// Xitrum requires Java 8
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

//------------------------------------------------------------------------------

libraryDependencies += "tv.cntt" %% "xitrum" % "3.29.0"

// Xitrum uses SLF4J, an implementation of SLF4J is needed
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

// For writing condition in logback.xml
libraryDependencies += "org.codehaus.janino" % "janino" % "3.1.0"

libraryDependencies += "org.webjars.bower" % "bootstrap-css" % "3.3.6"

// Scalate template engine config for Xitrum -----------------------------------

libraryDependencies += "tv.cntt" %% "xitrum-scalate" % "2.9.0"

// Precompile Scalate templates
import org.fusesource.scalate.ScalatePlugin._
scalateSettings
ScalateKeys.scalateTemplateConfig in Compile := Seq(TemplateConfig(
  (sourceDirectory in Compile).value / "scalate",
  Seq(),
  Seq(Binding("helper", "xitrum.Action", importMembers = true))
))

// xgettext i18n translation key string extractor is a compiler plugin ---------

autoCompilerPlugins := true
addCompilerPlugin("tv.cntt" %% "xgettext" % "1.5.3")
scalacOptions += "-P:xgettext:xitrum.I18n"

// Put config directory in classpath for easier development --------------------

// For "sbt console"
unmanagedClasspath in Compile += baseDirectory.value / "config"

// For "sbt fgRun"
unmanagedClasspath in Runtime += baseDirectory.value / "config"

// Copy these to target/xitrum when sbt xitrum-package is run
XitrumPackage.copy("config", "public", "script")

libraryDependencies += "org.squeryl" %% "squeryl" % "0.9.5-7"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.10"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % Test
libraryDependencies += "com.h2database" % "h2" % "1.4.193" % Test


