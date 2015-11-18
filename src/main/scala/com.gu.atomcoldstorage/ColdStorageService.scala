package com.gu.contentatomcoldstorage

import scala.util.{ Success, Failure }
import scala.concurrent.{ Future, ExecutionContext }
import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import org.json4s.jackson.Serialization
import org.json4s.{ DefaultFormats, Formats, FieldSerializer }
import org.json4s.JsonAST._
import org.joda.time.DateTime

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ColdStorageServiceActor(val store: AtomReadStore)
    extends Actor with ColdStorageService {

  val formats: Formats = DefaultFormats + AtomSummary.dateSerializer

  implicit val ec = context.dispatcher

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  //def receive = runRoute(healthCheck ~ listAtoms ~ getAtom)
  def receive = runRoute(healthCheck ~ getAtom ~ listAtoms)
}


case class AtomSummary(
  id: String,
  atomType: String,
  dateOfUpdate: DateTime,
  lastModified: Option[JValue] = None,
  createdDate: Option[JValue]  = None
)

object AtomSummary {
  val dateSerializer = FieldSerializer[AtomSummary](
    serializer = {
      case (fieldName, d: DateTime) => Some(fieldName -> d.getMillis)
    }
  )
}

// this trait defines our service behavior independently from the service actor
trait ColdStorageService extends HttpService {
  implicit val ec: ExecutionContext
  implicit val formats: Formats

  val store: AtomReadStore

  val healthCheck =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    }

  val getAtom =
    path("atom" / "([A-Za-z0-9-]+)".r) { id =>
      get {
        onSuccess(store.get(id)) {
          case Some(atom) => asJson(atom)
          case None => respondWithStatus(404)(complete(s"Not found $id"))
        }
      }
    }

  val listAtoms =
    path("atoms".r) { _ =>
      get {
        val summary = store.list().map { items =>
          items.map { item =>
            def dateField(fname: String) =
              (item.jsonData \ "contentChangeDetails" \ fname \ "date").toOption

            AtomSummary(item.id,
                        item.atomType,
                        item.dateOfUpdate,
                        dateField("lastModified"),
                        dateField("created")
            )
          }
        }
        asJsonSuccess(summary)
      }
    }

  def asJson(a: AnyRef) =
    respondWithMediaType(`application/json`) {
      complete(Serialization.write(a))
    }

  def asJsonSuccess(f: => Future[AnyRef]) =
    onSuccess(f) { asJson(_) }

}
