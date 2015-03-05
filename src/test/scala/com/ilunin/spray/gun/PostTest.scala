package com.ilunin.spray.gun

import org.scalatest.{FreeSpec, Matchers}
import spray.http._

class PostTest extends FreeSpec with Matchers {

  val request = HttpRequest(HttpMethods.POST, Uri("/test?param1=value1&param2=value2"), entity = "body")

  "Post should" - {
    "match an HttpRequest" - {
      "built with a POST method" in {
        request match {
          case Post(_) =>
          case _ => fail("request must be matched by Post")
        }
      }

      "built with a POST method and the right path" in {
        request match {
          case Post("/test", _) =>
          case _ => fail("request must be matched by Post")
        }
      }

    }

    "not match any HttpRequest" - {
      "built with a GET method " in {
        val post = HttpRequest(HttpMethods.GET)

        post match {
          case Post(_) => fail("request must not be matched by Post")
          case _ =>
        }
      }

      "built with a POST method and the wrong path" in {
        request match {
          case Post("/other", _) => fail("request must not be matched by Post")
          case _ =>
        }
      }

    }

    "extract" - {
      "the path of a POST request" in {
        val Post(path, _) = request

        path should be("/test")

      }

      "the entity" in {
        val Post(_, entity) = request

        entity.asString should be("body")
      }
    }
  }
}
