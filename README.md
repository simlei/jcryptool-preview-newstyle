# This repository...

... is a trimmed-down version of org.jcryptool.graphvc and RSA-plugin for JCrypTool for __preview purposes__ for coworkers.

- It is built with SBT, the Scala Build Tool
- the files directories of interest are:
  - `build.sbt`, `./project/*.scala` -- Build definitions
  - `src/main/scala-3/` -- the root project files 
    - includes `bci`, the cleaned-up, current state of BouncyCastle integration; here also reside the `metamodel` package which is the now-obsolete but still used draft of a graph-based Model in the MVC paradigm.
    - includes `swt` and `view` abstractions that target the Eclipse Rich Client Platform. Here reside the `PlatformForSwt` and related abstractions, that make the same code run standalone as well as in the Eclipse Rich Client Platform
    - includes `rsaelgamal`, code for a JCrypTool plugin that, based on the above abstractions, explains the RSA and ElGamal algorithms. It is currently under development.
  - `sub/graphs` is the current main focus of development and represents a greenfield implementation of the ideas introduced in the root-project `metamodel` package.
  - `sub/arturopala-tree` — in lieu of a Maven-published artifact, this project currently relies on a carbon copy of the code in https://github.com/arturopala/scala-tree. It is probable that this may be removed soon but was not easy to expunge from this repository right now. Apache-2.0 licenced.

Currently, this whole project should be importable in IntelliJ/IDEA Community edition with the latest Scala Plugin installed and should compile under Linux.

_Why is it probably not working just out of the box yet on every machine?_

The answer has to do with the fact that this project targets the Eclipse ecosystem which uses OSGi dependencies. For example, the UI toolkit used is SWT; Eclipse actually publishes this as pure maven artifacts. However, to date, declaration of that Maven dependency fails in the dependency resolution phase in SBT due to an `${ARCH}` property required but not properly communicated to the maven repository. This is just one of the kinks one has to deal with when targeting OSGi artifacts from a maven-dependency-based project like this one. Currently, this shortcoming is addressed by custom build cruft that pulls the SWT jars from an eclipse workspace into the local directory `./jctjars` which is included in this repository for convenience, and which is put on the `unmanagedLibraryJars` classpath. In theory, this should suffice to make the project compile and run; it is however only a intermediate solution. Better solutions are in development.

In theory, the standalone RSA App could be launched using the sbt target:

```sbt
runMain rsaelgamal.RSAAppServiceMain
```

