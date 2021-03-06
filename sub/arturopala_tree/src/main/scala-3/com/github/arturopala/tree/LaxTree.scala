/*
 * Copyright 2020 Artur Opala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.arturopala.tree

import com.github.arturopala.tree.Tree.{ArrayTree => ArrayTreeClass, NodeTree, empty}
import com.github.arturopala.tree.internal.{ArrayTree, NodeTree}

/** Extension methods providing lax modifications of the [[Tree]] (and [[MutableTree]]).
  *
  * @note The [[Tree]] does not mandate children to be unique
  *       and the main [[TreeLike]] API functions keeps them distinct by default.
  *       However, if your dataset is unique per se, or you do not
  *       care about node uniqueness and do not want to pay a price of
  *       additional checks involved, this extensions allow you to do so.
  *
  * @groupprio laxTransformation 70
  * @groupname laxTransformation Lax transformation
  * @groupprio laxInsertion 71
  * @groupname laxInsertion Lax insert
  * @groupprio laxUpdate 72
  * @groupname laxUpdate Lax update
  * @groupprio laxModification 73
  * @groupname laxModification Lax modify
  * @groupprio laxRemoval 74
  * @groupname laxRemoval Lax removal
  */
trait LaxTree[F[+_], T] {

  /** Maps every node of the tree using provided function and returns a new tree.
    * @group laxTransformation */
  def mapLax[K](f: T => K): F[K]

  /** Flat-maps all nodes of the tree using provided function and returns a new tree.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @group laxTransformation */
  def flatMapLax[K](f: T => F[K]): F[K]

  // LAX INSERTIONS

  /** Inserts a new child node holding the value and returns updated tree.
    * @param value value of the new child leaf
    * @param append whether to append or prepend to the existing children
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @group laxInsertion */
  def insertLeafLax[T1 >: T](value: T1, append: Boolean = false): F[T1]

  /** Inserts new leaf-type children and returns updated tree.
    * @param values values of the new children leaves
    * @param append whether to append or prepend to the existing children
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @group laxInsertion */
  def insertLeavesLax[T1 >: T](values: Iterable[T1], append: Boolean = false): F[T1]

  /** Inserts, at the given path, a new child node holding the value and returns a whole tree updated.
    * If path doesn't fully exist in the tree then remaining suffix will be created.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of node's values forming a path from the root to the parent node.
    * @param value a value to insert as a new child
    * @group laxInsertion */
  def insertLeafLaxAt[T1 >: T](path: Iterable[T1], value: T1, append: Boolean = false): F[T1]

  /** Attempts to insert, at the given path, a new child node holding the value and returns a whole tree updated.
    * If path doesn't fully exist in the tree then the tree will remain intact.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of K items forming a path from the root to the parent node.
    * @param toPathItem extractor of the K path item from the tree's node value
    * @return either right of modified tree or left with existing unmodified tree
    * @group laxInsertion */
  def insertLeafLaxAt[K, T1 >: T](
    path: Iterable[K],
    value: T1,
    toPathItem: T => K,
    append: Boolean
  ): Either[F[T], F[T1]]

  /** Inserts a new sub-tree and returns updated tree.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @group laxInsertion */
  def insertChildLax[T1 >: T](child: F[T1], append: Boolean = false): F[T1]

  /** Inserts new children and returns updated tree.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @group laxInsertion */
  def insertChildrenLax[T1 >: T](children: Iterable[F[T1]], append: Boolean = false): F[T1]

  /** Inserts, at the given path, a new child and returns a whole tree updated.
    * If path doesn't fully exist in the tree then remaining suffix will be created.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of node's values forming a path from the root to the parent node.
    * @group laxInsertion */
  def insertChildLaxAt[T1 >: T](path: Iterable[T1], subtree: F[T1], append: Boolean = false): F[T1]

  /** Attempts to insert, at the given path, a new child and return a whole tree updated.
    * If path doesn't fully exist in the tree then the tree will remain intact.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list K items forming a path from the root to the parent node.
    * @return either right of modified tree or left with existing unmodified tree
    * @group laxInsertion */
  def insertChildLaxAt[K, T1 >: T](
    path: Iterable[K],
    subtree: F[T1],
    toPathItem: T => K,
    append: Boolean
  ): Either[F[T], F[T1]]

  /** Inserts, at the given path, new children and returns a whole tree updated.
    * If path doesn't fully exist in the tree then remaining suffix will be created.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of node's values forming a path from the root to the parent node.
    * @group laxInsertion */
  def insertChildrenLaxAt[T1 >: T](
    path: Iterable[T1],
    children: Iterable[F[T1]],
    append: Boolean = false
  ): F[T1]

  /** Attempts to insert, at the given path, new children and returns a whole tree updated.
    * If path doesn't fully exist in the tree then the tree will remain intact.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list K items forming a path from the root to the parent node.
    * @return either right of modified tree or left with existing unmodified tree
    * @group laxInsertion */
  def insertChildrenLaxAt[K, T1 >: T](
    path: Iterable[K],
    children: Iterable[F[T1]],
    toPathItem: T => K,
    append: Boolean
  ): Either[F[T], F[T1]]

  // LAX UPDATES

  /** Updates the value of a first child node holding a given value.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param existingValue value of the child node
    * @param replacement replacement value
    * @return modified tree if contains the value
    * @group laxUpdate */
  def updateChildValueLax[T1 >: T](existingValue: T1, replacement: T1): F[T1]

  /** Updates the first value selected by the given path, and returns a whole tree updated.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of node's values forming a path from the root to the parent node.
    * @param replacement replacement value
    * @return either right of modified tree or left with the tree intact
    * @group laxUpdate */
  def updateValueLaxAt[T1 >: T](path: Iterable[T1], replacement: T1): Either[F[T], F[T1]]

  /** Updates the first value selected by the given path, and returns a whole tree updated.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of K items forming a path from the root to the parent node.
    * @param replacement replacement value
    * @param toPathItem extractor of the K path item from the tree's node value
    * @return either right of modified tree or left with the tree intact
    * @group laxUpdate */
  def updateValueLaxAt[K, T1 >: T](
    path: Iterable[K],
    replacement: T1,
    toPathItem: T => K
  ): Either[F[T], F[T1]]

  /** Updates the first child holding a given value.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param value value of the child node
    * @param replacement replacement tree
    * @return modified tree if contains the value
    * @group laxUpdate */
  def updateChildLax[T1 >: T](value: T1, replacement: F[T1]): F[T1]

  /** Updates the first tree selected by the given path, and returns a whole tree updated.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of node's values forming a path from the root to the parent node.
    * @param replacement replacement tree
    * @return either right of modified tree or left with the tree intact
    * @group laxUpdate */
  def updateTreeLaxAt[T1 >: T](
    path: Iterable[T1],
    replacement: F[T1]
  ): Either[F[T], F[T1]]

  /** Updates the first tree selected by the given path, and returns a whole tree updated.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list K items forming a path from the root to the parent node.
    * @param replacement replacement tree
    * @param toPathItem extractor of the K path item from the tree's node value
    * @return either right of modified tree or left with the tree intact
    * @group laxUpdate */
  def updateTreeLaxAt[K, T1 >: T](
    path: Iterable[K],
    replacement: F[T1],
    toPathItem: T => K
  ): Either[F[T], F[T1]]

  // LAX MODIFICATIONS

  /** Modifies all values of the tree.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param modify function to modify values
    * @group laxModification */
  def modifyAllLax[T1 >: T](modify: T => T1): F[T1] = ???

  /** Modifies the value of a child node holding a given value.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param value value of the child node
    * @param modify function to modify the value
    * @return modified tree if contains the value
    * @group laxModification */
  def modifyChildValueLax[T1 >: T](value: T1, modify: T => T1): F[T1]

  /** Modifies the value selected by the given path, and returns a whole tree updated.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of node's values forming a path from the root to the parent node.
    * @param modify function to modify the value
    * @return either right of modified tree or left with the tree intact
    * @group laxModification */
  def modifyValueLaxAt[T1 >: T](path: Iterable[T1], modify: T => T1): Either[F[T], F[T1]]

  /** Modifies the value selected by the given path, and returns a whole tree updated.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of K items forming a path from the root to the parent node.
    * @param modify function to modify the value
    * @param toPathItem extractor of the K path item from the tree's node value
    * @return either right of modified tree or left with the tree intact
    * @group laxModification */
  def modifyValueLaxAt[K, T1 >: T](
    path: Iterable[K],
    modify: T => T1,
    toPathItem: T => K
  ): Either[F[T], F[T1]]

  /** Modifies the child holding a given value.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param value value of the child node
    * @param modify function to modify the value
    * @return modified tree if contains the value
    * @group laxModification */
  def modifyChildLax[T1 >: T](value: T1, modify: F[T] => F[T1]): F[T1]

  /** Modifies the tree selected by the given path, and returns a whole tree updated.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of node's values forming a path from the root to the parent node.
    * @param modify function transforming the tree
    * @return either right of modified tree or left with the tree intact
    * @group laxModification */
  def modifyTreeLaxAt[T1 >: T](path: Iterable[T1], modify: F[T] => F[T1]): Either[F[T], F[T1]]

  /** Modifies the tree selected by the given path, and returns a whole tree updated.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list K items forming a path from the root to the parent node.
    * @param modify function transforming the tree
    * @param toPathItem extractor of the K path item from the tree's node value
    * @return either right of modified tree or left with the tree intact
    * @group laxModification */
  def modifyTreeLaxAt[K, T1 >: T](
    path: Iterable[K],
    modify: F[T] => F[T1],
    toPathItem: T => K
  ): Either[F[T], F[T1]]

  // LAX REMOVALS

  /** Removes child node holding a value, re-inserts nested children into this tree.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @return modified tree
    * @group laxRemoval */
  def removeChildValueLax[T1 >: T](value: T1): F[T]

  /** Removes the value selected by the given path, merges node's children with remaining siblings,
    * and returns a whole tree updated.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of node's values forming a path from the root to the parent node.
    * @return modified tree
    * @group laxRemoval */
  def removeValueLaxAt[T1 >: T](path: Iterable[T1]): F[T]

  /** Removes the value selected by the given path, merges node's children with remaining siblings,
    * and returns a whole tree updated.
    * @note This is a lax method, it doesn't preserve children values uniqueness.
    * @param path list of K items forming a path from the root to the parent node.
    * @param toPathItem extractor of the K path item from the tree's node value
    * @return modified tree
    * @group laxRemoval */
  def removeValueLaxAt[K, T1 >: T](path: Iterable[K], toPathItem: T => K): F[T]

}

/** Lax extensions of the [[Tree]] API. */
object LaxTreeOps {

  /** [[LaxTree]] extensions for a [[Tree]]. */
  implicit final class LaxTreeExt[T](val t: Tree[T]) extends LaxTree[Tree, T] {

    override def mapLax[K](f: T => K): Tree[K] = t match {
      case Tree.empty => Tree.empty

      case node: NodeTree[T] =>
        val (structure, content) = NodeTree.arrayMap(node, f)
        TreeBuilder
          .fromIterators[K, Tree](structure.iterator, content.iterator, None)
          .headOption
          .getOrElse(empty)

      case tree =>
        ArrayTree.mapLax(tree, f)
    }

    override def flatMapLax[K](f: T => Tree[K]): Tree[K] = t match {
      case Tree.empty => Tree.empty

      case node: NodeTree[T] =>
        val list: Vector[(Int, Tree[K])] =
          NodeTree.listFlatMap(f, Vector((node.children.size, f(node.head))), node.children.toVector)

        TreeBuilder
          .fromSizeAndTreePairsSequence(list, Nil, TreeBuilder.MergeStrategy.AppendLax)
          .headOption
          .getOrElse(empty)

      case tree =>
        ArrayTree.flatMapLax(tree, f)
    }

    override def insertLeafLax[T1 >: T](value: T1, append: Boolean = false): Tree[T1] = t match {
      case Tree.empty => Tree(value)

      case node: NodeTree[T] =>
        Tree(node.head, if (append) node.children.toSeq :+ Tree(value) else Tree(value) +: node.children.toSeq)

      case tree =>
        ArrayTree.insertLeaf(tree.size - 1, value, tree, append, keepDistinct = false)
    }

    override def insertLeavesLax[T1 >: T](values: Iterable[T1], append: Boolean = false): Tree[T1] =
      t match {
        case Tree.empty => if (values.size == 1) Tree(values.head) else Tree.empty

        case node: NodeTree[T] =>
          Tree(
            node.head,
            if (append) node.children ++ values.map(Tree.apply[T1])
            else values.map(Tree.apply[T1]) ++ node.children
          )

        case tree =>
          ArrayTree.insertLeaves(tree.size - 1, values, tree, append, keepDistinct = false)
      }

    override def insertLeafLaxAt[T1 >: T](
      path: Iterable[T1],
      value: T1,
      append: Boolean = false
    ): Tree[T1] = t match {
      case Tree.empty =>
        Tree.empty.insertBranch(path.toSeq :+ value)

      case node: NodeTree[T] =>
        NodeTree.insertChildAt(node, path.iterator, Tree(value), append, keepDistinct = false).getOrElse(node)

      case tree =>
        ArrayTree.insertLeafAt(path, value, tree, append, keepDistinct = false)
    }

    override def insertLeafLaxAt[K, T1 >: T](
      path: Iterable[K],
      value: T1,
      toPathItem: T => K,
      append: Boolean
    ): Either[Tree[T], Tree[T1]] = t match {
      case Tree.empty => Left(Tree.empty)

      case node: NodeTree[T] =>
        NodeTree.insertChildAt(node, path.iterator, toPathItem, Tree(value), append, keepDistinct = false)

      case tree =>
        ArrayTree.insertLeafAt(path, value, tree, toPathItem, append, keepDistinct = false)
    }

    override def insertChildLax[T1 >: T](child: Tree[T1], append: Boolean = false): Tree[T1] =
      t match {
        case Tree.empty => child

        case node: NodeTree[T] =>
          child match {
            case Tree.empty => node
            case tree: NodeTree[T1] =>
              Tree(node.head, if (append) node.children.toSeq :+ tree else tree +: node.children.toSeq)
            case tree =>
              if (Tree.preferInflated(node, tree))
                Tree(
                  node.head,
                  if (append) node.children.toSeq :+ tree.inflated
                  else tree.inflated +: node.children.toSeq
                )
              else node.deflated.insertChildLax(tree, append)
          }

        case tree =>
          val insertIndex = if (append) 0 else tree.size - 1
          ArrayTree.insertTreeAtIndex(insertIndex, tree.size - 1, child, tree)
      }

    override def insertChildrenLax[T1 >: T](
      children: Iterable[Tree[T1]],
      append: Boolean = false
    ): Tree[T1] = {
      val validChildren = children.filterNot(_.isEmpty)
      t match {
        case Tree.empty => if (validChildren.size == 1) validChildren.head else Tree.empty

        case node: NodeTree[T] =>
          if (validChildren.isEmpty) t
          else if (validChildren.size == 1) t.insertChild(validChildren.head, append)
          else if (validChildren.forall(_.isInstanceOf[NodeTree[T1]]))
            Tree(
              node.head,
              if (append) node.children ++ validChildren
              else validChildren ++ node.children
            )
          else if (append) ArrayTree.insertAfterChildren[Tree, T, T1](node, validChildren, keepDistinct = false)
          else ArrayTree.insertBeforeChildren[Tree, T, T1](node, validChildren, keepDistinct = false)

        case tree =>
          if (append) ArrayTree.insertAfterChildren[Tree, T, T1](tree, validChildren, keepDistinct = false)
          else ArrayTree.insertBeforeChildren[Tree, T, T1](tree, validChildren, keepDistinct = false)
      }
    }

    override def insertChildLaxAt[T1 >: T](
      path: Iterable[T1],
      child: Tree[T1],
      append: Boolean = false
    ): Tree[T1] = t match {
      case Tree.empty =>
        if (path.isEmpty) child
        else if (child.isEmpty) empty
        else TreeBuilder.linearTreeFromSequence(path.toSeq).insertChildLaxAt(path, child, append)

      case node: NodeTree[T] =>
        child match {
          case Tree.empty => node

          case tree: NodeTree[T1] =>
            NodeTree.insertChildAt(node, path.iterator, tree, append, keepDistinct = false).getOrElse(node)

          case tree =>
            if (Tree.preferInflated(node, tree))
              NodeTree
                .insertChildAt(
                  node,
                  path.iterator,
                  tree.inflated,
                  append,
                  keepDistinct = false
                )
                .getOrElse(node)
            else node.deflated.insertChildLaxAt(path, tree, append)
        }

      case tree =>
        ArrayTree.insertChildAt(path, child, tree, append, keepDistinct = false)
    }

    override def insertChildLaxAt[K, T1 >: T](
      path: Iterable[K],
      child: Tree[T1],
      toPathItem: T => K,
      append: Boolean
    ): Either[Tree[T], Tree[T1]] = t match {
      case Tree.empty =>
        if (path.isEmpty) Right(child) else Left(empty)

      case node: NodeTree[T] =>
        child match {
          case Tree.empty => Left(node)

          case tree: NodeTree[T1] =>
            NodeTree.insertChildAt(node, path.iterator, toPathItem, tree, append, keepDistinct = false)

          case tree =>
            NodeTree
              .insertChildAt(
                node,
                path.iterator,
                toPathItem,
                tree.inflated,
                append,
                keepDistinct = false
              )
        }

      case tree =>
        ArrayTree.insertChildAt(path, child, tree, toPathItem, append, keepDistinct = false)
    }

    override def insertChildrenLaxAt[T1 >: T](
      path: Iterable[T1],
      children: Iterable[Tree[T1]],
      append: Boolean = false
    ): Tree[T1] = {
      val validChildren = children.filterNot(_.isEmpty)
      t match {
        case Tree.empty => Tree.empty

        case node: NodeTree[T] =>
          if (validChildren.isEmpty) t
          else if (validChildren.size == 1) t.insertChildLaxAt(path, validChildren.head, append)
          else if (validChildren.forall(_.isInstanceOf[NodeTree[T1]]))
            NodeTree
              .insertChildrenAt(
                node,
                path.iterator,
                validChildren.map(_.inflated),
                append,
                keepDistinct = false
              )
              .getOrElse(node)
          else
            ArrayTree.insertChildrenAt(path, validChildren, node.deflated, append, keepDistinct = false)

        case tree =>
          ArrayTree.insertChildrenAt(path, validChildren, tree, append, keepDistinct = false)
      }
    }

    override def insertChildrenLaxAt[K, T1 >: T](
      path: Iterable[K],
      children: Iterable[Tree[T1]],
      toPathItem: T => K,
      append: Boolean
    ): Either[Tree[T], Tree[T1]] = {
      val validChildren = children.filterNot(_.isEmpty)
      t match {
        case Tree.empty => Left(Tree.empty)

        case node: NodeTree[T] =>
          if (validChildren.isEmpty) Right(t)
          else if (validChildren.size == 1) t.insertChildLaxAt(path, validChildren.head, toPathItem, append)
          else
            NodeTree
              .insertChildrenAt(
                node,
                path.iterator,
                toPathItem,
                validChildren.map(_.inflated),
                append,
                keepDistinct = false
              )

        case tree =>
          ArrayTree.insertChildrenAt(path, validChildren, tree, toPathItem, append, keepDistinct = false)
      }
    }

    override def updateChildValueLax[T1 >: T](existingValue: T1, replacement: T1): Tree[T1] =
      t match {
        case Tree.empty => empty

        case node: NodeTree[T] =>
          NodeTree.updateChildValue(node, existingValue, replacement, rightmost = false, keepDistinct = false)

        case tree =>
          ArrayTree.updateChildValue(existingValue, replacement, tree, rightmost = false, keepDistinct = false)
      }

    override def updateValueLaxAt[T1 >: T](
      path: Iterable[T1],
      replacement: T1
    ): Either[Tree[T], Tree[T1]] = t match {
      case Tree.empty => Left(empty)

      case node: NodeTree[T] =>
        NodeTree.updateValueAt(node, path.iterator, replacement, rightmost = false, keepDistinct = false)

      case tree =>
        ArrayTree.updateValueAt(path, replacement, tree, rightmost = false, keepDistinct = false)
    }

    override def updateValueLaxAt[K, T1 >: T](
      path: Iterable[K],
      replacement: T1,
      toPathItem: T => K
    ): Either[Tree[T], Tree[T1]] = t match {
      case Tree.empty => Left(empty)

      case node: NodeTree[T] =>
        NodeTree.updateValueAt(node, path.iterator, toPathItem, replacement, rightmost = false, keepDistinct = false)

      case tree =>
        ArrayTree.updateValueAt(path, replacement, tree, toPathItem, rightmost = false, keepDistinct = false)
    }

    override def updateChildLax[T1 >: T](value: T1, replacement: Tree[T1]): Tree[T1] =
      t match {
        case Tree.empty => empty

        case node: NodeTree[T] =>
          NodeTree.updateChild(node, value, replacement, rightmost = false, keepDistinct = false)

        case tree =>
          ArrayTree.updateChild(value, replacement, tree, rightmost = false, keepDistinct = false)
      }

    override def updateTreeLaxAt[T1 >: T](
      path: Iterable[T1],
      replacement: Tree[T1]
    ): Either[Tree[T], Tree[T1]] = t match {
      case Tree.empty => Left(empty)

      case node: NodeTree[T] =>
        NodeTree.updateTreeAt(node, path.iterator, replacement, rightmost = false, keepDistinct = false)

      case tree =>
        ArrayTree.updateTreeAt(path, replacement, tree, rightmost = false, keepDistinct = false)

    }

    override def updateTreeLaxAt[K, T1 >: T](
      path: Iterable[K],
      replacement: Tree[T1],
      toPathItem: T => K
    ): Either[Tree[T], Tree[T1]] = t match {
      case Tree.empty => Left(empty)

      case node: NodeTree[T] =>
        NodeTree.updateTreeAt(node, path.iterator, toPathItem, replacement, rightmost = false, keepDistinct = false)

      case tree =>
        ArrayTree.updateTreeAt(path, replacement, tree, toPathItem, rightmost = false, keepDistinct = false)
    }

    override def modifyChildValueLax[T1 >: T](value: T1, modify: T => T1): Tree[T1] =
      t match {
        case Tree.empty => empty

        case node: NodeTree[T] =>
          NodeTree.modifyChildValue(node, value, modify, rightmost = false, keepDistinct = false)

        case tree =>
          ArrayTree.modifyChildValue(value, modify, tree, rightmost = false, keepDistinct = false)
      }

    override def modifyValueLaxAt[T1 >: T](
      path: Iterable[T1],
      modify: T => T1
    ): Either[Tree[T], Tree[T1]] = t match {
      case Tree.empty => Left(empty)

      case node: NodeTree[T] =>
        NodeTree.modifyValueAt(node, path.iterator, modify, rightmost = false, keepDistinct = false)

      case tree =>
        ArrayTree.modifyValueAt(path, modify, tree, rightmost = false, keepDistinct = false)
    }

    override def modifyValueLaxAt[K, T1 >: T](
      path: Iterable[K],
      modify: T => T1,
      toPathItem: T => K
    ): Either[Tree[T], Tree[T1]] = t match {
      case Tree.empty => Left(empty)

      case node: NodeTree[T] =>
        NodeTree.modifyValueAt(node, path.iterator, toPathItem, modify, rightmost = false, keepDistinct = false)

      case tree =>
        ArrayTree.modifyValueAt(path, modify, tree, toPathItem, rightmost = false, keepDistinct = false)
    }

    override def modifyChildLax[T1 >: T](value: T1, modify: Tree[T] => Tree[T1]): Tree[T1] =
      t match {
        case Tree.empty => empty

        case node: NodeTree[T] =>
          NodeTree.modifyChild(node, value, modify, rightmost = false, keepDistinct = false)

        case tree =>
          ArrayTree.modifyChild(value, modify, tree, rightmost = false, keepDistinct = false)
      }

    override def modifyTreeLaxAt[T1 >: T](
      path: Iterable[T1],
      modify: Tree[T] => Tree[T1]
    ): Either[Tree[T], Tree[T1]] = t match {
      case Tree.empty => Left(empty)

      case node: NodeTree[T] =>
        NodeTree.modifyTreeAt(node, path.iterator, modify, rightmost = false, keepDistinct = false)

      case tree =>
        ArrayTree.modifyTreeAt(path, modify, tree, rightmost = false, keepDistinct = false)

    }

    override def modifyTreeLaxAt[K, T1 >: T](
      path: Iterable[K],
      modify: Tree[T] => Tree[T1],
      toPathItem: T => K
    ): Either[Tree[T], Tree[T1]] = t match {
      case Tree.empty => Left(empty)

      case node: NodeTree[T] =>
        NodeTree.modifyTreeAt(node, path.iterator, toPathItem, modify, rightmost = false, keepDistinct = false)

      case tree =>
        ArrayTree.modifyTreeAt(path, modify, tree, toPathItem, rightmost = false, keepDistinct = false)

    }

    override def removeChildValueLax[T1 >: T](value: T1): Tree[T] = t match {
      case Tree.empty => empty

      case node: NodeTree[T] =>
        NodeTree.removeChildValue(node, value, rightmost = false, keepDistinct = false)

      case tree =>
        ArrayTree.removeChildValue(value, tree, rightmost = false, keepDistinct = false)
    }

    override def removeValueLaxAt[T1 >: T](path: Iterable[T1]): Tree[T] = t match {
      case Tree.empty => empty

      case node: NodeTree[T] =>
        NodeTree.removeValueAt(node, path.iterator, rightmost = false, keepDistinct = false)

      case tree =>
        ArrayTree.removeValueAt(path, tree, rightmost = false, keepDistinct = false)
    }

    override def removeValueLaxAt[K, T1 >: T](path: Iterable[K], toPathItem: T => K): Tree[T] =
      t match {
        case Tree.empty => empty

        case node: NodeTree[T] =>
          NodeTree.removeValueAt(node, path.iterator, toPathItem, rightmost = false, keepDistinct = false)

        case tree =>
          ArrayTree.removeValueAt(path, tree, toPathItem, rightmost = false, keepDistinct = false)
      }

  }

  /** [[LaxTree]] extensions for a [[MutableTree]]. */
  implicit final class LaxMutableTreeExt[T](val tree: MutableTree[T]) extends LaxTree[MutableTree, T] {

    override def mapLax[K](f: T => K): MutableTree[K] =
      ArrayTree.mapLax(tree, f)

    override def flatMapLax[K](f: T => MutableTree[K]): MutableTree[K] =
      ArrayTree.flatMapLax(tree, f)

    override def insertLeafLax[T1 >: T](value: T1, append: Boolean = false): MutableTree[T1] =
      ArrayTree.insertLeaf(tree.size - 1, value, tree, append, keepDistinct = false)

    override def insertLeavesLax[T1 >: T](values: Iterable[T1], append: Boolean = false): MutableTree[T1] =
      ArrayTree.insertLeaves(tree.size - 1, values, tree, append, keepDistinct = false)

    override def insertLeafLaxAt[T1 >: T](
      path: Iterable[T1],
      value: T1,
      append: Boolean = false
    ): MutableTree[T1] =
      ArrayTree.insertLeafAt(path, value, tree, append, keepDistinct = false)

    override def insertLeafLaxAt[K, T1 >: T](
      path: Iterable[K],
      value: T1,
      toPathItem: T => K,
      append: Boolean
    ): Either[MutableTree[T], MutableTree[T1]] =
      ArrayTree.insertLeafAt(path, value, tree, toPathItem, append, keepDistinct = false)

    override def insertChildLax[T1 >: T](child: MutableTree[T1], append: Boolean = false): MutableTree[T1] = {
      val insertIndex = if (append) 0 else tree.size - 1
      ArrayTree.insertTreeAtIndex(insertIndex, tree.size - 1, child, tree)
    }

    override def insertChildrenLax[T1 >: T](
      children: Iterable[MutableTree[T1]],
      append: Boolean = false
    ): MutableTree[T1] = {
      val validChildren = children.filterNot(_.isEmpty)
      if (append) ArrayTree.insertAfterChildren[MutableTree, T, T1](tree, validChildren, keepDistinct = false)
      else ArrayTree.insertBeforeChildren[MutableTree, T, T1](tree, validChildren, keepDistinct = false)
    }

    override def insertChildLaxAt[T1 >: T](
      path: Iterable[T1],
      child: MutableTree[T1],
      append: Boolean = false
    ): MutableTree[T1] =
      ArrayTree.insertChildAt(path, child, tree, append, keepDistinct = false)

    override def insertChildLaxAt[K, T1 >: T](
      path: Iterable[K],
      child: MutableTree[T1],
      toPathItem: T => K,
      append: Boolean
    ): Either[MutableTree[T], MutableTree[T1]] =
      ArrayTree.insertChildAt(path, child, tree, toPathItem, append, keepDistinct = false)

    override def insertChildrenLaxAt[T1 >: T](
      path: Iterable[T1],
      children: Iterable[MutableTree[T1]],
      append: Boolean = false
    ): MutableTree[T1] = {
      val validChildren = children.filterNot(_.isEmpty)
      ArrayTree.insertChildrenAt(path, validChildren, tree, append, keepDistinct = false)
    }

    override def insertChildrenLaxAt[K, T1 >: T](
      path: Iterable[K],
      children: Iterable[MutableTree[T1]],
      toPathItem: T => K,
      append: Boolean
    ): Either[MutableTree[T], MutableTree[T1]] = {
      val validChildren = children.filterNot(_.isEmpty)
      ArrayTree.insertChildrenAt(path, validChildren, tree, toPathItem, append, keepDistinct = false)
    }

    override def updateChildValueLax[T1 >: T](existingValue: T1, replacement: T1): MutableTree[T1] =
      ArrayTree.updateChildValue(existingValue, replacement, tree, rightmost = false, keepDistinct = false)

    override def updateValueLaxAt[T1 >: T](
      path: Iterable[T1],
      replacement: T1
    ): Either[MutableTree[T], MutableTree[T1]] =
      ArrayTree.updateValueAt(path, replacement, tree, rightmost = false, keepDistinct = false)

    override def updateValueLaxAt[K, T1 >: T](
      path: Iterable[K],
      replacement: T1,
      toPathItem: T => K
    ): Either[MutableTree[T], MutableTree[T1]] =
      ArrayTree.updateValueAt(path, replacement, tree, toPathItem, rightmost = false, keepDistinct = false)

    override def updateChildLax[T1 >: T](value: T1, replacement: MutableTree[T1]): MutableTree[T1] =
      ArrayTree.updateChild(value, replacement, tree, rightmost = false, keepDistinct = false)

    override def updateTreeLaxAt[T1 >: T](
      path: Iterable[T1],
      replacement: MutableTree[T1]
    ): Either[MutableTree[T], MutableTree[T1]] =
      ArrayTree.updateTreeAt(path, replacement, tree, rightmost = false, keepDistinct = false)

    override def updateTreeLaxAt[K, T1 >: T](
      path: Iterable[K],
      replacement: MutableTree[T1],
      toPathItem: T => K
    ): Either[MutableTree[T], MutableTree[T1]] =
      ArrayTree.updateTreeAt(path, replacement, tree, toPathItem, rightmost = false, keepDistinct = false)

    override def modifyChildValueLax[T1 >: T](value: T1, modify: T => T1): MutableTree[T1] =
      ArrayTree.modifyChildValue(value, modify, tree, rightmost = false, keepDistinct = false)

    override def modifyValueLaxAt[T1 >: T](
      path: Iterable[T1],
      modify: T => T1
    ): Either[MutableTree[T], MutableTree[T1]] =
      ArrayTree.modifyValueAt(path, modify, tree, rightmost = false, keepDistinct = false)

    override def modifyValueLaxAt[K, T1 >: T](
      path: Iterable[K],
      modify: T => T1,
      toPathItem: T => K
    ): Either[MutableTree[T], MutableTree[T1]] =
      ArrayTree.modifyValueAt(path, modify, tree, toPathItem, rightmost = false, keepDistinct = false)

    override def modifyChildLax[T1 >: T](value: T1, modify: MutableTree[T] => MutableTree[T1]): MutableTree[T1] =
      ArrayTree.modifyChild(value, modify, tree, rightmost = false, keepDistinct = false)

    override def modifyTreeLaxAt[T1 >: T](
      path: Iterable[T1],
      modify: MutableTree[T] => MutableTree[T1]
    ): Either[MutableTree[T], MutableTree[T1]] =
      ArrayTree.modifyTreeAt(path, modify, tree, rightmost = false, keepDistinct = false)

    override def modifyTreeLaxAt[K, T1 >: T](
      path: Iterable[K],
      modify: MutableTree[T] => MutableTree[T1],
      toPathItem: T => K
    ): Either[MutableTree[T], MutableTree[T1]] =
      ArrayTree.modifyTreeAt(path, modify, tree, toPathItem, rightmost = false, keepDistinct = false)

    override def removeChildValueLax[T1 >: T](value: T1): MutableTree[T] =
      ArrayTree.removeChildValue(value, tree, rightmost = false, keepDistinct = false)

    override def removeValueLaxAt[T1 >: T](path: Iterable[T1]): MutableTree[T] =
      ArrayTree.removeValueAt(path, tree, rightmost = false, keepDistinct = false)

    override def removeValueLaxAt[K, T1 >: T](path: Iterable[K], toPathItem: T => K): MutableTree[T] =
      ArrayTree.removeValueAt(path, tree, toPathItem, rightmost = false, keepDistinct = false)

  }

}
