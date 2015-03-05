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

#### 3. Async Server
