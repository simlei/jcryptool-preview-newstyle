package sbtproj
import sbt._
import sbt.Keys._
import sbtassembly._
import sbtassembly.AssemblyKeys._
import JCT.Keys._

object BCIAssembly {

  val baseAssemblySettings = Seq(
    assembly / test := {},
    assembly / assemblyMergeStrategy := swtMergeStrategy((assembly / assemblyMergeStrategy).value)
    , assembly / assemblyJarName := "assembly_Base.jar"
    // , assembly / mainClass := Some(mainClassNameAssembly)
  )

  def swtMergeStrategy(oldstrategy: String => MergeStrategy): String => MergeStrategy = {
    case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
    case "application.conf"                            => MergeStrategy.concat
    case "plugin.properties"                                => MergeStrategy.discard
    case "plugin_de.properties"                                => MergeStrategy.discard
    case ".api_description"                                => MergeStrategy.discard
    case ".options"                                => MergeStrategy.discard
    case x => oldstrategy(x)
  }

  val rcpExportAssemblySettings = AssemblyPlugin.baseAssemblySettings ++ AssemblyPlugin.assemblySettings ++ Seq (
      assembly / test := {},
      assembly / assemblyMergeStrategy := swtMergeStrategy((assembly / assemblyMergeStrategy).value),
      assembly / assemblyExcludedJars := {
        val unmanagedNames: Seq[String] = (jctjars_swtplus_list.value ++ jctjars_jctplatform_list.value) map (_.data.getName)
        val cp: Classpath = (assembly / fullClasspath).value
        cp filter {cpf => unmanagedNames.contains(cpf.data.getName)}
      },
      assembly / assemblyJarName := "assembly_RCPExport.jar"
    )

}
