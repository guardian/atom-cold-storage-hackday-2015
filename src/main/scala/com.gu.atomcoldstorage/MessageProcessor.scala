package com.gu.contentatomcoldstorage

import scala.util.{ Success, Failure }
import akka.actor.Actor
import com.amazonaws.services.kinesis.model.Record
import com.gu.contentatom.thrift.ContentAtomEvent
import org.json4s.jackson.Serialization
import org.json4s.{ Formats, DefaultFormats }

class MessageProcessor
    extends Actor
    with ThriftDeserializer[ContentAtomEvent]
    with Logging {

  implicit val defaultFormats: Formats = DefaultFormats

  val codec = ContentAtomEvent

  def processRecord(record: Record) = {
    val buffer = record.getData
    deserializeEvent(buffer) match {
      case Success(e)   => {
        val json = Serialization.write(e.atom)
        log.info(s"Converted to json as follows: $json")
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
