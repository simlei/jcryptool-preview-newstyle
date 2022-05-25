// package simlei.graphs
// import com.github.arturopala.tree.*

// import hu.webarticum.{treeprinter => tp}
// import hu.webarticum.treeprinter.TreeNode

// import hu.webarticum.treeprinter.printer.listing.ListingTreePrinter
// import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter
// import hu.webarticum.treeprinter.decorator.BorderTreeNodeDecorator
// import hu.webarticum.treeprinter.decorator.PadTreeNodeDecorator
// import hu.webarticum.treeprinter.printer.TreePrinter
// import hu.webarticum.treeprinter.misc.fs.DefaultFsTreeNodeDecorator

// import cats.Show

// object TreePrinting:
//   type TreeFormatter = tp.TreeNode => String
//   trait TreeShow[T]:
//     def show(tree: Tree[T]): tp.TreeNode

//   def formatter1_listing_minimal: TreeFormatter = tnode =>
//     val printer = new ListingTreePrinter()
//     val decorated = tnode // no decoration
//     printer.stringify(tnode)
//   def formatter2_topdown_boxes: TreeFormatter = tnode =>
//     val printer = new TraditionalTreePrinter()
//     val decorated = new BorderTreeNodeDecorator(tnode)
//     printer.stringify(decorated)
//   def formatter3_topdown_minimal: TreeFormatter = tnode =>
//     val printer = new TraditionalTreePrinter()
//     val decorated = tnode
//     printer.stringify(decorated)

//   extension[T](tree: Tree[T])
//     def toGraphicalString(formatter: TreeFormatter)(using treeShower: TreeShow[T]): String =
//       formatter(treeShower.show(tree))
//     def toGraphicalString1(using TreeShow[T]): String = toGraphicalString(formatter1_listing_minimal)
//     def toGraphicalString2(using TreeShow[T]): String = toGraphicalString(formatter2_topdown_boxes)
//     def toGraphicalString3(using TreeShow[T]): String = toGraphicalString(formatter3_topdown_minimal)


// @main def TreePrintingMain(): Unit =
//   import TreePrinting.*

//   println("hello from " + TreePrinting)
//   val tE = Tree.empty
//   val t1 = Tree("Top", Tree("Left"), Tree("Middle"), Tree("Right"))

//   object printable:
//     val root = tp.SimpleTreeNode("Top")
//     root.addChild(tp.SimpleTreeNode("Left"))
//     root.addChild(tp.SimpleTreeNode("Middle"))
//     root.addChild(tp.SimpleTreeNode("Right"))

//   val printer1 = new ListingTreePrinter()
//   val printer2 = new TraditionalTreePrinter()
//   // TODO: should be named "decorated", as they are transformed trees AFAIC
//   val decorator1 = new BorderTreeNodeDecorator(printable.root)
//   val decorator2 = new DefaultFsTreeNodeDecorator(printable.root)
//   val decorator3 = new PadTreeNodeDecorator(printable.root)
//   val decorator4_base = new PadTreeNodeDecorator(printable.root, new tp.Insets(0,0))
//   val decorator4 = BorderTreeNodeDecorator(decorator4_base)

//   println(">< ".repeat(40))
//   println(tE.toString)
//   println(t1.toString)
//   println("--- using extension methods...")

//   given TreeShow[String] with
//     def show(tree: Tree[String]): tp.TreeNode =
//       val resultRoot = tp.SimpleTreeNode(tree.head)
//       val children: List[tp.TreeNode] = tree.children.toList.map{ show(_) } // recurse: convert children
//       children.foreach{ child =>
//         resultRoot.addChild(child)
//       }
//       resultRoot

//   println(t1.toGraphicalString1)
//   println(t1.toGraphicalString2)
//   println(t1.toGraphicalString3)


//   println("<> ".repeat(40))

//     // println("===== printer1 :")
//     // println("=========== deco 1: ")
//     // printer1.print(decorator1)
//     // println("=========== deco 2: ")
//     // printer1.print(decorator2)
//     // println("=========== deco 3: ")
//     // printer1.print(decorator3)
//     // println("=========== deco 4: ")
//     // printer1.print(decorator4)
//     // println("===== printer2 :")
//     // println("=========== deco 1: ")
//     // printer2.print(decorator1)
//     // println("=========== deco 2: ")
//     // printer2.print(decorator2)
//     // println("=========== deco 3: ")
//     // printer2.print(decorator3)
//     // println("=========== deco 4: ")
//     // printer2.print(decorator4)


