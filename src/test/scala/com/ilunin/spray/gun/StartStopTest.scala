package com.ilunin.spray.gun

import com.ilunin.spray.gun.Server._
import org.scalatest.{FreeSpec, Matchers}

class StartStopTest extends FreeSpec with Matchers {

  "A server should" - {

    "be able to start, stop, start again and stop again" in {
      val server = Server()
      server.start()
      server.stop()
      noException should be thrownBy server.start()
      server.stop()
    }

    "not be able to start twice" in {
      val server = Server()
      server.start()
      val thrown = the[ServerAlreadyStartedException] thrownBy server.start()
      thrown.getMessage should be("Server already started on port 8080")
      server.stop()
    }

    "not be able to start when in used by withServer" in {
      val server = Server()
      executeWhileRunning(server) {
        val thrown = the[ServerAlreadyStartedException] thrownBy server.start()
        thrown.getMessage should be("Server already started on port 8080")
      }
    }

    "be able to start after being used by withServer" in {
      val server = Server()
      executeWhileRunning(server) {}
      noException should be thrownBy server.start()
      server.stop()
    }
  }

  "Two servers should be able to start on different ports" in {
    val server1 = Server(port = 8080)
    val server2 = Server(port = 8081)
    server1.start()
    noException should be thrownBy server2.start()
    server1.stop()
    server2.stop()
  }

}
