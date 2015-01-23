package com.ilunin.spray.gun

import akka.actor.Actor
import akka.pattern.Patterns
import spray.can.Http
import spray.http.{HttpRequest, HttpResponse}

import scala.concurrent.Future

class ServerActor(handler: PartialFunction[HttpRequest, Future[HttpResponse]]) extends Actor {

  override def receive: Receive = {
    case _: Http.Connected => sender ! Http.Register(self)
    case request: HttpRequest =>
      val handlerWithDefault: PartialFunction[HttpRequest, Future[HttpResponse]] = handler orElse { case _ => Future.successful(HttpResponse(status = 404))}
      val destination = sender()
      Patterns.pipe(handlerWithDefault(request), context.dispatcher).to(destination)
  }

}
