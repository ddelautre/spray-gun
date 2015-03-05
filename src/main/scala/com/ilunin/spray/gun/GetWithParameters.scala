package com.ilunin.spray.gun

import spray.http.Uri.{Path, Query}
import spray.http.{HttpMethods, HttpRequest, Uri}

object GetWithParameters {

  def unapply(request: HttpRequest): Option[(String, Query)] = request match {
    case HttpRequest(HttpMethods.GET, Uri(_, _, Path(path), query, _), _, _, _) => Some((path, query))
    case _ => None
  }

}
