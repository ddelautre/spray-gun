package com.ilunin.spray.gun

class ServerAlreadyStartedException(port: Int) extends RuntimeException(s"Server already started on port $port")
