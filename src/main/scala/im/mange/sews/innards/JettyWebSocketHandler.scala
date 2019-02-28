package im.mange.sews.innards

import org.eclipse.jetty.websocket.server.WebSocketHandler
import org.eclipse.jetty.websocket.servlet.{ServletUpgradeRequest, ServletUpgradeResponse, WebSocketCreator, WebSocketServletFactory}

case class JettyWebSocketHandler(program: WebSocketProgram) extends WebSocketHandler {

  override def configure(factory: WebSocketServletFactory): Unit = {
    factory.setCreator(new WebSocketCreator {
      override def createWebSocket(req: ServletUpgradeRequest, resp: ServletUpgradeResponse): AnyRef = {
        new SubscriberWebSocket(program)
      }
    })
  }
}
