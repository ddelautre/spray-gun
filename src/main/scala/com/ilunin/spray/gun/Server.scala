package com.ilunin.spray.gun

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.ilunin.spray.gun.Server.Filter
import spray.can.Http
import spray.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class Server(interface: String, port: Int, handler: PartialFunction[HttpRequest, Future[HttpResponse]], val filters: Filter*) {
  var system: Option[ActorSystem] = None


  def start(): Unit = {
    if (system.isDefined) {
      throw new ServerAlreadyStartedException(port)
    }
    implicit val actorSystem = ActorSystem("spray-gun")
    system = Some(actorSystem)
    val listener = actorSystem.actorOf(Props(classOf[ServerActor], handler, filters))
    implicit val timeout = Timeout(5 second)
    val result = Await.result(IO(Http) ? Http.Bind(listener, interface, port), 5 second)
    if (result.isInstanceOf[Http.CommandFailed]) {
      throw new ServerAlreadyStartedException(port)
    }
  }

  def stop(): Unit = {
    system.foreach { actorSystem =>
      actorSystem.shutdown()
      actorSystem.awaitTermination()
    }
    system = None
  }


  def withFilters(filters: Filter*) = new Server(interface, port, handler, filters: _*)

  def beforeEachRequest(block: HttpRequest => Unit): Server = transformEachRequest { request =>
    block(request)
    request
  }

  def transformEachRequest(transform: HttpRequest => HttpRequest): Server = {
    val filter = (request: HttpRequest, next: HttpRequest => Future[HttpResponse]) => next(transform(request))
    new Server(interface, port, handler, filter)
  }

  def afterEachResponse(block: (HttpRequest, HttpResponse) => Unit): Server = transformEachResponse { (request, response) =>
    block(request, response)
    response
  }

  def transformEachResponse(transform: (HttpRequest, HttpResponse) => HttpResponse): Server = {
    val filter = (request: HttpRequest, next: HttpRequest => Future[HttpResponse]) => next(request).map { response =>
      transform(request, response)
    }
    new Server(interface, port, handler, filter)
  }

}

object Server {

  type Filter = (HttpRequest, HttpRequest => Future[HttpResponse]) => Future[HttpResponse]

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
