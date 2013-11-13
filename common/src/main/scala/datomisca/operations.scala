/*
 * Copyright 2012 Pellucid and Zenexity
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package datomisca

import scala.language.reflectiveCalls


trait Operation extends Nativeable

sealed trait DataFunction extends Operation {
  def func: Keyword
}

final case class AddFact(id: DId, attr: Keyword, value: AnyRef) extends DataFunction with TempIdentified {
  override val func = Keyword("add", Some(Namespace.DB))
  // override val id = fact.id

  def toNative: AnyRef =
    datomic.Util.list(func.toNative, id.toNative, attr.toNative, value)
}

final case class RetractFact(id: Long, attr: Keyword, value: AnyRef) extends DataFunction with FinalIdentified {
  override val func = Keyword("retract", Some(Namespace.DB))

  def toNative: AnyRef =
    datomic.Util.list(func.toNative, id: java.lang.Long, attr.toNative, value)
}

final case class RetractEntity(id: Long) extends DataFunction with FinalIdentified {
  override val func = RetractEntity.kw

  def toNative: AnyRef =
    datomic.Util.list(func.toNative, id: java.lang.Long)

  //override def toString = toNative.toString
}

object RetractEntity {
  val kw = Keyword("retractEntity", Some(Namespace.DB.FN))
}

class PartialAddEntity(val props: Map[Keyword, AnyRef]) {

  def ++(other: PartialAddEntity) = new PartialAddEntity(props ++ other.props)

  def toMap = props
  //override def toString = props.toString
}

object PartialAddEntity {

  def empty: PartialAddEntity = new PartialAddEntity(Map())
}

final case class AddEntity(id: DId, partialProps: Map[Keyword, AnyRef]) extends PartialAddEntity(partialProps + (Keyword("id", Namespace.DB) -> id.toNative)) with Operation with TempIdentified {

  def toNative: AnyRef = {
    import scala.collection.JavaConverters._
    props.map { case (k, v) => (k.toNative, v) } .asJava
  }

  override def toString = props.map{ case (kw, dd) => kw.toString + " " + dd.toString }.mkString("{\n", "\n  ", "\n}")
}

final case class AddIdent(ident: Keyword, partition: Partition = Partition.USER) extends Operation with KeywordIdentified {
  def toNative = AddFact(DId(partition), Keyword("ident", Namespace.DB), ident.toNative).toNative

  override def toString = toNative.toString
}


