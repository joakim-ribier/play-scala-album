import play.Project._
import sbt._
import Keys._

object ApplicationBuild extends Build {

    val appName         = "gallery"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
    	anorm, javaJdbc,
      "postgresql" % "postgresql" % "9.1-901.jdbc4",
      "net.coobird" % "thumbnailator" % "0.4.2",
      "com.typesafe" %% "play-plugins-mailer" % "2.1.0"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
    )

}
