package com.ilunin.spray.gun

import spray.http.Uri.Path
import spray.http.{HttpRequest, Uri}

object AnyMethod {

  def unapply(request: HttpRequest): Option[String] = request match {
    case HttpRequest(_, Uri(_, _, Path(path), _, _), _, _, _) => Some(path)
    case _ => None
  }

}
