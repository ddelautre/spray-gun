package com.ilunin.spray.gun

import akka.actor.Actor
import akka.pattern.Patterns
import com.ilunin.spray.gun.Server.Filter
import spray.can.Http
import spray.http.{HttpRequest, HttpResponse}

import scala.concurrent.Future

class ServerActor(handler: PartialFunction[HttpRequest, Future[HttpResponse]], filters: Filter*) extends Actor {

  override def receive: Receive = {
    case _: Http.Connected => sender ! Http.Register(self)
    case request: HttpRequest => handleRequest(request)
  }

  private def handleRequest(request: HttpRequest): Unit = {
    val handlerWithDefault: PartialFunction[HttpRequest, Future[HttpResponse]] = handler orElse {
      case _ => Future.successful(HttpResponse(status = 404))
    }
    val destination = sender()
    val newHandler = filters.foldRight[HttpRequest => Future[HttpResponse]](handlerWithDefault) { (filter, next) =>
      filter(_, next)
    }
    val response = newHandler(request)
    Patterns.pipe(response, context.dispatcher).to(destination)
  }

}
