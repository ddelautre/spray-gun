package com.ilunin.spray.gun

import org.scalatest.{FreeSpec, Matchers}
import spray.http.Uri.Query
import spray.http._

class PostWithParametersTest extends FreeSpec with Matchers {

  val request = HttpRequest(HttpMethods.POST, Uri("/test?param1=value1&param2=value2"), entity = "body")

  "Post should" - {
    "match an HttpRequest" - {
      "built with a POST method" in {
        request match {
          case PostWithParameters(_) =>
          case _ => fail("request must be matched by Post")
        }
      }

      "built with a POST method and the right path" in {
        request match {
          case PostWithParameters("/test", _, _) =>
          case _ => fail("request must be matched by Post")
        }
      }

      "built with a POST method, the right path and the right parameters" in {
        val query = Query("param1" -> "value1", "param2" -> "value2")
        request match {
          case PostWithParameters("/test", `query`, _) =>
          case _ => fail("request must be matched by Post")
        }
      }
    }

    "not match any HttpRequest" - {
      "built with a GET method " in {
        val post = HttpRequest(HttpMethods.GET)

        post match {
          case PostWithParameters(_) => fail("request must not be matched by Post")
          case _ =>
        }
      }

      "built with a POST method and the wrong path" in {
        request match {
          case PostWithParameters("/other", _, _) => fail("request must not be matched by Post")
          case _ =>
        }
      }

      "built with the wrong parameters" in {
        val query = Query("param1" -> "value1", "param2" -> "value3")
        request match {
          case PostWithParameters(_, `query`, _) => fail("request must not be matched by Post")
          case _ =>
        }
      }
    }

    "extract" - {
      "the path of a POST request" in {
        val PostWithParameters(path, _, _) = request

        path should be("/test")

      }

      "the parameters" in {
        val PostWithParameters(_, query, _) = request

        query should contain only("param1" -> "value1", "param2" -> "value2")
      }

      "the entity" in {
        val PostWithParameters(_, _, entity) = request

        entity.asString should be("body")
      }
    }
  }
}
