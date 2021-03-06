package com.ilunin.spray.gun

import spray.http.Uri.Path
import spray.http.{HttpMethods, HttpRequest, Uri}

object Get {

  def unapply(request: HttpRequest): Option[String] = request match {
    case HttpRequest(HttpMethods.GET, Uri(_, _, Path(path), _, _), _, _, _) => Some(path)
    case _ => None
  }

}
