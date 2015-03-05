package com.ilunin.spray.gun

import org.scalatest.{FreeSpec, Matchers}
import spray.http.{HttpMethods, HttpRequest, Uri}

class AnyMethodTest extends FreeSpec with Matchers {

  val get = HttpRequest(HttpMethods.GET, Uri("/test?param1=value1&param2=value2"))
  val post = HttpRequest(HttpMethods.POST, Uri("/test?param1=value1&param2=value2"), entity = "body")

  "AnyMethod should" - {
    "match an HttpRequest" - {
      "built with a GET method" in {
        get match {
          case AnyMethod(_) =>
          case _ => fail("request must be matched by AnyMethod")
        }
      }

      "built with a GET method and the right path" in {
        get match {
          case AnyMethod("/test") =>
          case _ => fail("request must be matched by AnyMethod")
        }
      }

      "built with a POST method" in {
        post match {
          case AnyMethod(_) =>
          case _ => fail("request must be matched by AnyMethod")
        }
      }

      "built with a POST method and the right path" in {
        post match {
          case AnyMethod("/test") =>
          case _ => fail("request must be matched by AnyMethod")
        }
      }

    }

    "not match any HttpRequest" - {
      "built with a GET method and the wrong path" in {
        get match {
          case AnyMethod("/other") => fail("request must not be matched by AnyMethod")
          case _ =>
        }
      }

      "built with a POST method and the wrong path" in {
        post match {
          case AnyMethod("/other") => fail("request must not be matched by AnyMethod")
          case _ =>
        }
      }

    }

    "extract" - {
      "the path of a GET request" in {
        val AnyMethod(path) = get

        path should be("/test")

      }

      "the path of a POST request" in {
        val AnyMethod(path) = post

        path should be("/test")

      }
    }

  }

}
