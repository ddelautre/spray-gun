package com.ilunin.spray.gun

import org.scalatest.{FreeSpec, Matchers}
import spray.http.{HttpMethods, HttpRequest, Uri}

class GetTest extends FreeSpec with Matchers {

  val request = HttpRequest(HttpMethods.GET, Uri("/test?param1=value1&param2=value2"))

  "Get should" - {
    "match an HttpRequest" - {
      "built with a GET method" in {
        request match {
          case Get(_) =>
          case _ => fail("request must be matched by Get")
        }
      }

      "built with a GET method and the right path" in {
        request match {
          case Get("/test") =>
          case _ => fail("request must be matched by Get")
        }
      }

    }

    "not match any HttpRequest" - {
      "built with a POST method " in {
        val post = HttpRequest(HttpMethods.POST)

        post match {
          case Get(_) => fail("request must not be matched by Get")
          case _ =>
        }
      }

      "built with a GET method and the wrong path" in {
        request match {
          case Get("/other") => fail("request must not be matched by Get")
          case _ =>
        }
      }

    }

    "extract" - {
      "the path of a GET request" in {
        val Get(path) = request

        path should be("/test")

      }
    }

  }

}
