package im.mange.sews.innards

import org.eclipse.jetty.server.handler.{AbstractHandler, ContextHandler, ContextHandlerCollection}

case class WebServer(port: Int, contextHandlers: ContextHandler*){
  import org.eclipse.jetty.server.{Handler, Server}

  private val server = new Server(port)
  server.setStopAtShutdown(true)

  private val contexts = new ContextHandlerCollection
  contexts.setHandlers(contextHandlers.toArray[Handler])
  server.setHandler(contexts)

  def start(): Unit = {
    server.start()
    println(s"- Server started on port $port")
  }
}

object Handler {
  def apply(path: String, handler: AbstractHandler): ContextHandler = {
    val contextHandler = new ContextHandler(path)
    contextHandler.setHandler(handler)
    contextHandler
  }
}