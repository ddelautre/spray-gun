package com.ilunin.spray.gun

import spray.http.Uri.{Path, Query}
import spray.http.{HttpEntity, HttpMethods, HttpRequest, Uri}

object PostWithParameters {

  def unapply(request: HttpRequest): Option[(String, Query, HttpEntity)] = request match {
    case HttpRequest(HttpMethods.POST, Uri(_, _, Path(path), query, _), _, entity, _) => Some((path, query, entity))
    case _ => None
  }

}
