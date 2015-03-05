# spray-gun
A wrapper around spray-can to build a simple HTTP server

## Installation
Add this to your libraryDependencies:
`"spray-gun" %% "spray-gun" % "1.1.0"`

## Usage

### Create a Server
The first thing you need to do is create a server.

You have 3 possibilities:

#### 1. Simple Server
The simplest way to create a Server is with the `simpleServer` method on the `Server` object.

The `simpleServer` method has 4 parameters:  
1. `interface: String` is the interface on which the server will listen. For example `"localhost"` to listen only requests on localhost or `"0.0.0.0"` to listen on all the network interfaces  
2. `port: Int` is the port on which the server will listen. For example: `8080`  
3. `contentType: spray.http.ContentType` is the content type of the response returned by this server. For example `text/plain(UTF-8)`  
4. `content: String` is the body content returned by all the responses from this server  

```scala
import com.ilunin.spray.gun.Server
import spray.http.ContentTypes

Server.simpleServer("localhost", 8080, ContentTypes.`application/json`, """{"JSONCode": true}""")
```
This will create a Server that will listen on port 8080 on localhost and always return the JSON response `{"JSONCode": true}`

This is really useful for tests where you need to create a server quickly that just need to always return the same response.

`simpleServer` support default values for `interface`, `port` and `contentType` parameters.  
The default value for `interface` is `0.0.0.0`  
The default value for `port` is `8080`  
The default value for `contentType` is `text/plain(UTF-8)`    

So you can just specify the content for example:

```scala
import com.ilunin.spray.gun.Server

Server.simpleServer(content = "OK")
```

This will create a server that listens on all interface on port 8080 and returns a `text/plain` response: `"OK"`


#### 2. Sync Server

A more useful way to create a server is to create it with a function `HttpRequest => HttpResponse` with the `syncServer` method on the `Server` object.

The `syncServer` method has 3 parameters (in 2 parameters sets):  
1. `interface: String` is the interface on which the server will listen. For example `"localhost"` to listen only requests on localhost or `"0.0.0.0"` to listen on all the network interfaces  
2. `port: Int` is the port on which the server will listen. For example: `8080`  
3. `handler: PartialFunction[spray.http.HttpRequest, spray.http.HttpResponse]` is the function that need to handle the HTTP request and return the HTTP response.

Example:

```scala
import com.ilunin.spray.gun.Server
import spray.http.{ContentTypes, HttpRequest, HttpResponse, StatusCodes, HttpProtocols}

Server.syncServer("localhost", 8080) {
  case request: HttpRequest => HttpResponse(StatusCodes.OK, request.entity, List.empty, HttpProtocols.`HTTP/1.1`)
}
```

This will create a server that will listen on port 8080 on localhost and return a `200 OK` response with the same body as the request and no specific headers.

`syncServer` takes a `PartialFunction` as a parameter. If this function is not defined for a `HttpRequest` it will return a `404 Not Found` response.

For example:

```scala
import com.ilunin.spray.gun.Server
import spray.http.{HttpRequest, HttpResponse, StatusCodes, Uri}

Server.syncServer("0.0.0.0", 8080) {
  case HttpRequest(_, Uri.Path("/test"), _, _, _) => HttpResponse(StatusCodes.OK)
}
```

This will return a `200 OK` response for the path `/test` but will return a `404 Not Found` for every other path.

`syncServer` support default values for `interface` and `port` parameters.  
The default value for `interface` is `0.0.0.0`  
The default value for `port` is `8080`  

So you can write the same example as above simply as:

```scala
import com.ilunin.spray.gun.Server
import spray.http.{HttpRequest, HttpResponse, StatusCodes, Uri}

Server.syncServer() {
  case HttpRequest(_, Uri.Path("/test"), _, _, _) => HttpResponse(StatusCodes.OK)
}
```

#### 3. Async Server

The `asyncServer` method on the `Server` object behaves like the `syncServer` method, except that instead of taking a `PartialFunction[spray.http.HttpRequest, spray.http.HttpResponse]` it takes a `PartialFunction[spray.http.HttpRequest, Future[spray.http.HttpResponse]]`

This method is used when you need to use asynchronous functions to compute the response (like for example if you need to call another web service to generate your response)

Example:

```scala
import com.ilunin.spray.gun.Server
import spray.http.{HttpRequest, HttpResponse, StatusCode, Uri}
import scala.concurrent.Future

Server.syncServer() {
  case HttpRequest(_, Uri.Path("/test"), _, _, _) => {
    val statusCode: Future[StatusCode] = ??? // code that get the status code of another web service for example
    statusCode.map(HttpResponse(_))
  }
}
```

### Running the server

For starting the server, simply call the `start()` method on the `Server` instance that you have created.

For stopping the server, simply call the `stop()` method on the `Server` instance that you have created.

Example:

```scala
import com.ilunin.spray.gun.Server
import spray.http.ContentTypes

val server = Server.simpleServer("localhost", 8080, ContentTypes.`application/json`, """{"JSONCode": true}""")
server.start()

// Some code...

server.stop()
```

You can also use the `executeWhileRunning` method on the `Server` object that takes an instance of `Server` and a block of code. The server is automatically started at the start of the block of code and stopped at its end.

Example:

```scala
import com.ilunin.spray.gun.Server
import spray.http.ContentTypes

val server = Server.simpleServer("localhost", 8080, ContentTypes.`application/json`, """{"JSONCode": true}""")
val result: String = Server.executeWhileRunning(server) {
 val json: String = ??? // Some code that do a reqest on http://localhost:8080 and get the body
 json
}
```
