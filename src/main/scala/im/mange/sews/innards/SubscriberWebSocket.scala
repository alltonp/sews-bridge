package im.mange.sews.innards

import im.mange.sews.Subscriber
import org.eclipse.jetty.websocket.api.{Session, WebSocketAdapter}

//TODO: email could be passed in here ... (from header/cookie/request param whatever)
class SubscriberWebSocket(program: WebSocketProgram) extends WebSocketAdapter {
  private val callback: (String) => Unit = message => {
    if (isConnected) getRemote.sendString(message)
  }

  private val subscriber = Subscriber(callback)

  override def onWebSocketConnect(sess: Session): Unit = {
    super.onWebSocketConnect(sess)
    program.init(subscriber)
  }

  override def onWebSocketClose(statusCode: Int, reason: String): Unit = {
    super.onWebSocketClose(statusCode, reason)
    program.fini(subscriber)
  }

  override def onWebSocketText(message: String): Unit = {
    program.onMessage(message, Some(subscriber))
  }
}