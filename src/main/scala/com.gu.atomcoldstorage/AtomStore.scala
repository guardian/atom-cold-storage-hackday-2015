package com.gu.contentatomcoldstorage

import org.joda.time.DateTime
import scala.collection.mutable.Map
import scala.concurrent.Future
import org.json4s.JsonAST._

case class ContentAtomItem(
  id: String,
  dateOfUpdate: DateTime,
  atomType: String,
  jsonData: JValue,
  title: Option[String] = None
)

trait AtomReadStore {
  def get(id: String): Future[Option[ContentAtomItem]]
}

trait AtomWriteStore {
  def put(atom: ContentAtomItem): Future[Unit]
}

trait AtomStore extends AtomReadStore with AtomWriteStore {
  val name: String
}

  // trait AtomStoreResultOrder {
  // }

class AtomMapStore extends AtomStore {
  val name = "Memory MapStore"

  private val contents: Map[String, ContentAtomItem] = Map(
    "1234" -> ContentAtomItem("1234", DateTime.now, "test", JObject(), None)
  )

  def put(atom: ContentAtomItem): Future[Unit] = {
    contents.put(atom.id, atom)
    Future.successful(())
  }

  def get(id: String): Future[Option[ContentAtomItem]] = {
    Future.successful(contents.get(id))
  }
}
