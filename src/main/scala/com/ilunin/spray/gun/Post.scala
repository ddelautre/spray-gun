package com.ilunin.spray.gun

import spray.http.Uri.Path
import spray.http.{HttpEntity, HttpMethods, HttpRequest, Uri}

object Post {

  def unapply(request: HttpRequest): Option[(String, HttpEntity)] = request match {
    case HttpRequest(HttpMethods.POST, Uri(_, _, Path(path), _, _), _, entity, _) => Some((path, entity))
    case _ => None
  }

}
