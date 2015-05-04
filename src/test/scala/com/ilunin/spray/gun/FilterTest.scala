package com.ilunin.spray.gun

import com.jayway.restassured.RestAssured._
import org.scalatest.{FreeSpec, Matchers}
import spray.http.Uri.Path
import spray.http.{HttpRequest, HttpResponse, Uri}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FilterTest extends FreeSpec with Matchers {

  val server = Server.syncServer() {
    case Get("/test") => HttpResponse(entity = "response for /test")
    case Get("/test2") => HttpResponse(entity = "response for /test2")
  }

  "A filter" - {
    "can modify the request then calling the handler" in {
      val serverWithFilter = server.withFilters { (request, nextFilter) =>
        nextFilter(request.copy(uri = Uri(path = Path("/test2"))))
      }

      Server.executeWhileRunning(serverWithFilter) {
        get("http://localhost:8080/test").body().asString() should be("response for /test2")
      }
    }

    "can be created with the transformEachRequest method" in {
      val serverWithFilter = server.transformEachRequest(_.copy(uri = Uri(path = Path("/test2"))))

      Server.executeWhileRunning(serverWithFilter) {
        get("http://localhost:8080/test").body().asString() should be("response for /test2")
      }
    }

    "can be created with the beforeEachRequest method" in {
      var lastRequestPath = ""
      val serverWithFilter = server.beforeEachRequest { request =>
        lastRequestPath = request.uri.path.toString()
      }

      Server.executeWhileRunning(serverWithFilter) {
        get("http://localhost:8080/test").body().asString() should be("response for /test")
        lastRequestPath should be("/test")
      }
    }

    "can modify the response returned by the handler" in {
      val serverWithFilter = server.withFilters { (request, nextFilter) =>
        nextFilter(request).map {
          case response => response.copy(entity = response.entity.asString + " modified")
        }
      }

      Server.executeWhileRunning(serverWithFilter) {
        get("http://localhost:8080/test").body().asString() should be("response for /test modified")
      }
    }

    "can be created with the transformEachResponse method" in {
      val serverWithFilter = server.transformEachResponse {
        case (request, response) => response.copy(entity = response.entity.asString + " modified for request " + request.uri.path.toString())
      }

      Server.executeWhileRunning(serverWithFilter) {
        get("http://localhost:8080/test").body().asString() should be("response for /test modified for request /test")
      }
    }

    "can be created with the afterEachResponse method" in {
      var lastRequestPath = ""
      var lastResponseBody = ""
      val serverWithFilter = server.afterEachResponse { (request, response) =>
        lastRequestPath = request.uri.path.toString()
        lastResponseBody = response.entity.asString
      }

      Server.executeWhileRunning(serverWithFilter) {
        get("http://localhost:8080/test").body().asString() should be("response for /test")
        lastRequestPath should be("/test")
        lastResponseBody should be("response for /test")
      }
    }


    "can return a reponse and ignore the one returned by the handler" in {
      val serverWithFilter = server.withFilters { (request, nextFilter) =>
        Future.successful(HttpResponse(404))
      }

      Server.executeWhileRunning(serverWithFilter) {
        get("http://localhost:8080/test").statusCode() should be(404)
      }
    }
  }

  "Filters can be chained" in {
    val firstFilter = (request: HttpRequest, nextFilter: HttpRequest => Future[HttpResponse]) => nextFilter(request.copy(uri = Uri(path = Path("/test2"))))
    val secondFilter = (request: HttpRequest, nextFilter: HttpRequest => Future[HttpResponse]) => nextFilter(request).map {
      case response => response.copy(entity = response.entity.asString + " modified")
    }
    val serverWithFilter = server.withFilters(firstFilter, secondFilter)
    Server.executeWhileRunning(serverWithFilter) {
      get("http://localhost:8080/test").body().asString() should be("response for /test2 modified")
    }
  }

}
