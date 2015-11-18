package com.gu.contentatomcoldstorage

import org.joda.time.DateTime
import scala.util.{ Success, Failure }
import akka.actor.Actor
import com.amazonaws.services.kinesis.model.Record
import com.gu.contentatom.thrift.ContentAtomEvent
import org.json4s.jackson.{ Serialization, JsonMethods }
import org.json4s.{ Formats, DefaultFormats }

class MessageProcessor(store: AtomStore)
    extends Actor
    with ThriftDeserializer[ContentAtomEvent]
    with Logging {

  implicit val defaultFormats: Formats = DefaultFormats

  val codec = ContentAtomEvent

  def processRecord(record: Record) = {
    val buffer = record.getData
    deserializeEvent(buffer) match {
      case Success(e)   => {
        val item = ContentAtomItem(
          id = e.atom.id,
          dateOfUpdate = DateTime.now,
          atomType = e.atom.atomType.name.toLowerCase,
          JsonMethods.parse(Serialization.write(e.atom))
        )
        store.put(item)
      }
      case Failure(err) => log.error(s"Could not deserialise event $err")
    }
  }

  def receive = {
    case r: Record =>
      println(s"Received record")
      processRecord(r)
  }
}
