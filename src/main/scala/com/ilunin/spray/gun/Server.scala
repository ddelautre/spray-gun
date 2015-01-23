package com.ilunin.spray.gun

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import spray.http.{HttpRequest, HttpResponse}

import scala.concurrent.Await
import scala.concurrent.duration._

class Server(interface: String = "0.0.0.0", port: Int = 8080, handler: PartialFunction[HttpRequest, HttpResponse] = {
  case _ => HttpResponse()
}) {

  var system: Option[ActorSystem] = None

  def start(): Unit = {
    if (system.isDefined) {
      throw new ServerAlreadyStartedException(port)
    }
    implicit val actorSystem = ActorSystem("spray-gun")
    system = Some(actorSystem)
    val listener = actorSystem.actorOf(Props(classOf[ServerActor], handler))
    implicit val timeout = Timeout(1 second)
    val result = Await.result(IO(Http) ? Http.Bind(listener, interface, port), 1 second)
    if (result.isInstanceOf[Http.CommandFailed]) {
      throw new ServerAlreadyStartedException(port)
    }
  }


  def stop(): Unit = {
    system.foreach(_.shutdown())
    system = None
  }

}

object Server {
  def apply(interface: String = "0.0.0.0", port: Int = 8080)(handler: PartialFunction[HttpRequest, HttpResponse]): Server = {
    new Server(interface, port, handler)
  }

}
