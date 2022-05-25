import sbt._

object Dependencies {

  // -----------------------------------------------------------------------
  val zio2Version = "2.0.0-RC2"
  lazy val zios = Seq(
      "dev.zio" %% "zio-streams" % zio2Version withSources() withJavadoc(),
    )

  val magnoliaVersion = "1.1.0"
  lazy val magnolia = "com.softwaremill.magnolia1_3" %% "magnolia" % magnoliaVersion

  // -----------------------------------------------------------------------
  val circeVersion = "0.15.0-M1"
  lazy val circes = Seq(
        "io.circe" %% "circe-core",
        "io.circe" %% "circe-generic",
        "io.circe" %% "circe-parser"
      ).map(_ % circeVersion withSources() withJavadoc())

  // -----------------------------------------------------------------------
  val monocleVersion = "3.0.0"
  lazy val monocles = Seq(
    "dev.optics" %% "monocle-core"  % monocleVersion,
  )

  // -----------------------------------------------------------------------
  val oslibVersion = "0.7.8"
  val oslibs = Seq(
    "com.lihaoyi" %% "os-lib" % "0.7.8" withSources() withJavadoc(),
  )

  // -----------------------------------------------------------------------
  val bouncycastleVersion = "1.59"
  lazy val bouncycastles = Seq(
    "org.bouncycastle" % "bcprov-jdk15on" % bouncycastleVersion withSources() withJavadoc(),
    "org.bouncycastle" % "bcpkix-jdk15on" % "1.59" withSources() withJavadoc(),
    )


  // -----------------------------------------------------------------------
  val shapeless3derivingVersion = "3.0.1"
  lazy val shapeless3derivings = Seq(
    "org.typelevel" %% "shapeless3-deriving" % shapeless3derivingVersion
  )

  // -----------------------------------------------------------------------
  val cats = Seq("org.typelevel" %% "cats-core" % "2.7.0" withSources() withJavadoc())

  // -----------------------------------------------------------------------
  val flexmarkVersion = "0.64.0"
  val flexmarks = Seq(
    "com.vladsch.flexmark" % "flexmark" % flexmarkVersion withSources(),
    "com.vladsch.flexmark" % "flexmark-util" % flexmarkVersion,
    )

  // -----------------------------------------------------------------------
  val http4sVersion = "0.23.6"
  val g4sVersion = "0.30.0"

  // lazy val collected_graphrelated = zios
  //   .++(cats)
  //   .++(monocles)
  // lazy val collected_bcilogic = zios
  //   .++(cats)
  //   // .++(circes)
  //   // .++(monocles)
  //   .++(oslibs)
  //   .++(bouncycastles)

}

// still on wishlist

// tree

// from bci_logic main dependency list: libs:

// Seq(
    // Seq("org.slf4j" % "slf4j-simple" % "1.7.32")
    // , Seq("com.47deg" %% "github4s" % g4sVersion)
    // , Seq(
    //     "org.http4s" %% "http4s-dsl" % http4sVersion,
    //     "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    //     "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    //     "org.http4s" %% "http4s-circe" % http4sVersion
    //   )
    // , Seq("org.typelevel" %% "cats-core" % catscoreVersion withSources() withJavadoc())
    // , Seq("org.typelevel" %% "cats-effect" % catseffectVersion withSources() withJavadoc())
    // , Seq("com.novocode" % "junit-interface" % "0.11" % "test")
    // , Seq("dev.zio" %% "zio" % "2.0.0-M4" withSources() withJavadoc())
    // , Seq("com.eed3si9n.expecty" %% "expecty" % "0.15.4")
    // , Seq("com.eed3si9n.expecty" %% "expecty" % "0.15.4")
    // , Seq("com.raquo" %%% "airstream" % "0.14.2" withSources() withJavadoc())
    // , Seq(("io.github.xencura" %% "kagera-api" % "0.5.0-M1+75-ab7dc8bf-SNAPSHOT").cross(CrossVersion.for3Use2_13).withSources())
    // , Seq("org.scalaz" %% "scalaz-core" % "7.4.0-M10" withSources() withJavadoc())
    // , Seq(
      // ("org.scala-graph" %% "graph-core" % "1.13.2").cross(CrossVersion.for3Use2_13).withSources()
      // ("org.scala-graph" %% "graph-dot" % "1.13.3").cross(CrossVersion.for3Use2_13).withSources()
    // )
    // , Seq(("com.lihaoyi" %% "scalarx" % "0.4.3").cross(CrossVersion.for3Use2_13).withSources())
    // , Seq("io.monix" %% "monix-reactive" % "3.4.0" withSources() withJavadoc() )
    // , Seq(
    //   "org.reactivestreams" % "reactive-streams" % "1.0.3", 
    //   "org.reactivestreams" % "reactive-streams-examples" % "1.0.3"
    //   )
  // ).flatten




// lazy val izumiDeps = Seq(
  // Typetags:
  // "dev.zio" %% "izumi-reflect" % "2.0.1", 
  // LogStage core library
  /* "io.7mind.izumi" %% "logstage-core" % "1.0.1", */

  // distage:
  // distage core library
  // "io.7mind.izumi" %% "distage-core" % "1.0.8",
  // // distage-testkit for ScalaTest
  // "io.7mind.izumi" %% "distage-testkit-scalatest" % "1.0.8" % Test,
  // // distage-framework: Roles, Entrypoints, Effect modules
  // "io.7mind.izumi" %% "distage-framework" % "1.0.8",
  // // Typesafe Config support
  // "io.7mind.izumi" %% "distage-extension-config" % "1.0.8",

  // // Classpath discovery support
  // "io.7mind.izumi" %% "distage-extension-plugins" % "1.0.8",
  // // LogStage integration with DIStage
  // "io.7mind.izumi" %% "distage-extension-logstage" % "1.0.8",

  // // Write logs as JSON
  // "io.7mind.izumi" %% "logstage-rendering-circe " % "1.0.8",
  // // Route Slf4J logs to LogStage
  // "io.7mind.izumi" %% "logstage-adapter-slf4j " % "1.0.8",
  // // Route LogStage logs to Slf4J
  // "io.7mind.izumi" %% "logstage-sink-slf4j " % "1.0.8",
