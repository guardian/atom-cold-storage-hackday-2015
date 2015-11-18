package com.gu.contentatomcoldstorage

import org.slf4j
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object Boot extends App with Logging {

  val startWebServer = false
  val startReader    = false

  val port = Option(System.getProperty("coldstorage.port")).map(_.toInt).getOrElse(8080)

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("content-atom-cold-storage")

  lazy val store = new AtomMapStore

  if(Config.startWebServer) {
    // create and start our service actor
    val service = system.actorOf(Props(classOf[ColdStorageServiceActor], store), "cold-storage-service")

    implicit val timeout = Timeout(5.seconds)
    // start a new HTTP server on port 8080 with our service actor as the handler
    IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = port)
  } else {
    log.info("Skipping web server startup")
  }

  if(Config.startReader) {
    val processor = system.actorOf(Props(classOf[MessageProcessor], store),
                                   "cold-storage-processor")
    val readerThread = (new KinesisStreamReader(processor)).kinesisThread
    readerThread.start
  } else {
    log.info("Skipping reader startup")
  }
}
