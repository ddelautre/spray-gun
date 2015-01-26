package com.ilunin.spray.gun

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import spray.http._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class Server(interface: String, port: Int, handler: PartialFunction[HttpRequest, Future[HttpResponse]]) {

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

  def apply(interface: String = "0.0.0.0", port: Int = 8080, handler: PartialFunction[HttpRequest, Future[HttpResponse]] = {
    case _ => Future.successful(HttpResponse())
  }): Server = {
    new Server(interface, port, handler)
  }

  def asyncServer(interface: String = "0.0.0.0", port: Int = 8080)(handler: PartialFunction[HttpRequest, Future[HttpResponse]]): Server = {
    new Server(interface, port, handler)
  }

  def simpleServer(interface: String = "0.0.0.0", port: Int = 8080, contentType: ContentType = ContentTypes.`text/plain(UTF-8)`, content: String): Server = {
    Server.syncServer(interface, port) {
      case _ => HttpResponse(entity = HttpEntity(contentType, content))
    }
  }

  def syncServer(interface: String = "0.0.0.0", port: Int = 8080)(handler: PartialFunction[HttpRequest, HttpResponse]): Server = {
    new Server(interface, port, handler andThen (httpResponse => Future.successful(httpResponse)))
  }

  @Deprecated

  /**
   * Deprecated. Use executeWhileRunning instead.
   */
  def withServer[T](server: Server)(body: => T) = executeWhileRunning(server)(body)

  def executeWhileRunning[T](server: Server)(body: => T) = {
    server.start()
    try {
      body
    } finally {
      server.stop()
    }
  }

}
