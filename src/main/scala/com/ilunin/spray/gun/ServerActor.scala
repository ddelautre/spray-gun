package com.ilunin.spray.gun

import akka.actor.Actor
import spray.can.Http
import spray.http.{HttpRequest, HttpResponse}

class ServerActor(handler: PartialFunction[HttpRequest, HttpResponse]) extends Actor {

  override def receive: Receive = {
    case _: Http.Connected => sender ! Http.Register(self)
    case request: HttpRequest => if (handler.isDefinedAt(request)) {
      sender ! handler(request)
    } else {
      sender ! HttpResponse(status = 404)
    }
  }

}
