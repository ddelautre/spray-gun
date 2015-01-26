package com.ilunin.spray.gun

import com.ilunin.spray.gun.Server.{asyncServer, executeWhileRunning, simpleServer, syncServer}
import com.jayway.restassured.RestAssured._
import org.scalatest.{FreeSpec, Matchers}
import spray.http.{ContentTypes, HttpRequest, HttpResponse, Uri}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ServerTest extends FreeSpec with Matchers {

  "A server should" - {

    "be able to start, stop, start again and stop again" in {
      val server = Server()
      server.start()
      server.stop()
      noException should be thrownBy server.start()
      server.stop()
    }

    "not be able to start twice" in {
      val server = Server()
      server.start()
      val thrown = the[ServerAlreadyStartedException] thrownBy server.start()
      thrown.getMessage should be("Server already started on port 8080")
      server.stop()
    }

    "not be able to start when in used by withServer" in {
      val server = Server()
      executeWhileRunning(server) {
        val thrown = the[ServerAlreadyStartedException] thrownBy server.start()
        thrown.getMessage should be("Server already started on port 8080")
      }
    }

    "be able to start after being used by withServer" in {
      val server = Server()
      executeWhileRunning(server) {}
      noException should be thrownBy server.start()
      server.stop()
    }

    "respond 200 to a HTTP request" - {

      "when built with a function that will always return 200" in {
        val server = syncServer() {
          case _ => HttpResponse()
        }
        executeWhileRunning(server) {
          get("http://localhost:8080/test").statusCode should be(200)
        }
      }

      "on /test when built with a function that will always return 200 on /test" in {
        val server = syncServer() {
          case HttpRequest(_, Uri.Path("/test"), _, _, _) => HttpResponse()
        }
        executeWhileRunning(server) {
          get("http://localhost:8080/test").statusCode should be(200)
        }
      }

      "when built with no function" in {
        executeWhileRunning(Server()) {
          get("http://localhost:8080/test").statusCode should be(200)
        }
      }

      "when built with a function that will always return 200 asynchronously" in {
        val server = asyncServer() {
          case _ => Future(HttpResponse())
        }
        executeWhileRunning(server) {
          get("http://localhost:8080/test").statusCode should be(200)
        }
      }

      "when built with a String" in {
        executeWhileRunning(simpleServer(content = "OK")) {
          get("http://localhost:8080/test").statusCode should be(200)
        }
      }

    }

    "respond 404 to a HTTP request" - {
      "on /test when built with a function that will always return 200 on /other" in {
        val server = syncServer() {
          case HttpRequest(_, Uri.Path("/other"), _, _, _) => HttpResponse()
        }
        executeWhileRunning(server) {
          get("http://localhost:8080/test").statusCode should be(404)
        }
      }
    }

    "respond with the String when built with a String" in {
      executeWhileRunning(simpleServer(content = "OK")) {
        get("http://localhost:8080/test").body().asString() should be("OK")
      }
    }

    "respond with the text/plain(UTF-8) content type when built with a String" in {
      executeWhileRunning(simpleServer(content = "OK")) {
        get("http://localhost:8080/test").contentType() should be("text/plain; charset=UTF-8")
      }
    }

    "respond with the content type when built with a String and a specific content type" in {
      executeWhileRunning(simpleServer(contentType = ContentTypes.`application/json`, content = "OK")) {
        get("http://localhost:8080/test").contentType() should be("application/json; charset=UTF-8")
      }
    }
  }

  "Two servers should be able to start on different ports" in {
    val server1 = Server(port = 8080)
    val server2 = Server(port = 8081)
    server1.start()
    noException should be thrownBy server2.start()
    server1.stop()
    server2.stop()
  }

}
