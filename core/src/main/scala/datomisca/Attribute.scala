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


final case class Attribute[DD, Card <: Cardinality](
    override val ident: Keyword,
    valueType:   SchemaType[DD],
    cardinality: Card,
    doc:         Option[String]  = None,
    unique:      Option[Unique]  = None,
    index:       Option[Boolean] = None,
    fulltext:    Option[Boolean] = None,
    isComponent: Option[Boolean] = None,
    noHistory:   Option[Boolean] = None
) extends Operation with Namespaceable with KeywordIdentified {

  def withDoc(str: String)        = copy( doc = Some(str) )
  def withUnique(u: Unique)       = copy( unique = Some(u) )
  def withIndex(b: Boolean)       = copy( index = Some(b) )
  def withFullText(b: Boolean)    = copy( fulltext = Some(b) )
  def withIsComponent(b: Boolean) = copy( isComponent = Some(b) )
  def withNoHistory(b: Boolean)   = copy( noHistory = Some(b) )

  // using partiton :db.part/db
  val id = DId(Partition.DB)
  override val name = ident.name
  override val ns = ident.ns

  lazy val toAddOps: AddEntity = {
    val mb = new scala.collection.mutable.MapBuilder[Keyword, AnyRef, Map[Keyword, AnyRef]](Map(
      Attribute.id          -> id,
      Attribute.ident       -> ident.toNative,
      Attribute.valueType   -> valueType.keyword.toNative,
      Attribute.cardinality -> cardinality.keyword.toNative
    ))
    if(doc.isDefined) mb += Attribute.doc -> doc.get
    if(unique.isDefined) mb += Attribute.unique -> unique.get.keyword.toNative
    if(index.isDefined) mb += Attribute.index -> (index.get: java.lang.Boolean)
    if(fulltext.isDefined) mb += Attribute.fulltext -> (fulltext.get: java.lang.Boolean)
    if(isComponent.isDefined) mb += Attribute.isComponent -> (isComponent.get: java.lang.Boolean)
    if(noHistory.isDefined) mb += Attribute.noHistory -> (noHistory.get: java.lang.Boolean)
    
    // installing attribute
    mb += Attribute.installAttr -> Partition.DB.keyword.toNative

    AddEntity(id, mb.result())
  }
  
  override def toNative: AnyRef = toAddOps.toNative
  override def toString = ident.toString

  def stringify = s"""
{ 
  ${Attribute.id} $id
  ${Attribute.ident} ${ident}
  ${Attribute.valueType} ${valueType.keyword}
  ${Attribute.cardinality} ${cardinality.keyword}""" +
  ( if(doc.isDefined) { "\n  " + Attribute.doc + " " + doc.get } else { "" } ) +
  ( if(unique.isDefined) { "\n  " + Attribute.unique + " " + unique.get.keyword } else { "" } ) +
  ( if(index.isDefined) { "\n  " + Attribute.index + " " + index.get } else { "" }) +
  ( if(fulltext.isDefined) { "\n  " + Attribute.fulltext + " " + fulltext.get } else { "" }) +
  ( if(isComponent.isDefined) { "\n  " + Attribute.isComponent + " " + isComponent.get } else { "" }) +
  ( if(noHistory.isDefined) { "\n  " + Attribute.noHistory + " " + noHistory.get } else { "" }) +
  "\n  " + Attribute.installAttr + " " + Partition.DB.keyword + 
  "\n}"

} 

object Attribute {
  val id = Keyword(Namespace.DB, "id")
  val ident = Keyword(Namespace.DB, "ident")
  val valueType = Keyword(Namespace.DB, "valueType")
  val cardinality = Keyword(Namespace.DB, "cardinality")
  val doc = Keyword(Namespace.DB, "doc")
  val unique = Keyword(Namespace.DB, "unique")
  val index = Keyword(Namespace.DB, "index")
  val fulltext = Keyword(Namespace.DB, "fulltext")
  val isComponent = Keyword(Namespace.DB, "isComponent")
  val noHistory = Keyword(Namespace.DB, "noHistory")
  val installAttr = Keyword(Namespace.DB.INSTALL, "_attribute")
}
