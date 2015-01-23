package com.ilunin.spray.gun

import com.jayway.restassured.RestAssured._
import org.scalatest.{FreeSpec, Matchers}
import spray.http.{HttpRequest, HttpResponse, Uri}

class ServerTest extends FreeSpec with Matchers {

  "A server should" - {

    "be able to start, stop, start again and stop again" in {
      val server = new Server()
      server.start()
      server.stop()
      noException should be thrownBy server.start()
      server.stop()
    }

    "not be able to start twice" in {
      val server = new Server()
      server.start()
      val thrown = the[ServerAlreadyStartedException] thrownBy server.start()
      thrown.getMessage should be("Server already started on port 8080")
      server.stop()
    }

    "respond 200 to a HTTP request" - {

      "when built with a function that will always return 200" in {
        val server = Server.syncServer() {
          case _ => HttpResponse()
        }
        server.start()
        get("http://localhost:8080/test").statusCode should be(200)
        server.stop()
      }

      "on /test when built with a function that will always return 200 on /test" in {
        val server = Server.syncServer() {
          case HttpRequest(_, Uri.Path("/test"), _, _, _) => HttpResponse()
        }
        server.start()
        get("http://localhost:8080/test").statusCode should be(200)
        server.stop()
      }

      "when built with no function" in {
        val server = new Server()
        server.start()
        get("http://localhost:8080/test").statusCode should be(200)
        server.stop()
      }

    }

    "respond 404 to a HTTP request" - {
      "on /test when built with a function that will always return 200 on /other" in {
        val server = Server.syncServer() {
          case HttpRequest(_, Uri.Path("/other"), _, _, _) => HttpResponse()
        }
        server.start()
        get("http://localhost:8080/test").statusCode should be(404)
        server.stop()
      }
    }
  }

  "Two servers should be able to start on different ports" in {
    val server1 = new Server(port = 8080)
    val server2 = new Server(port = 8081)
    server1.start()
    noException should be thrownBy server2.start()
    server1.stop()
    server2.stop()
  }

}
