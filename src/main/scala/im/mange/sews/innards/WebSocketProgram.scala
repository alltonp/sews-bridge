package im.mange.sews.innards

import im.mange.sews.{Subscriber, Subscribers}

trait WebSocketProgram {
  private [sews] val subscribers: Subscribers

  def onInit(subscriber: Subscriber): Unit
  def onFini(subscriber: Subscriber): Unit
  def onMessage(message: String, from: Option[Subscriber]): Unit

  def init(subscriber: Subscriber): Unit = {
    subscribers.subscribe(subscriber)
    onInit(subscriber)
  }

  def fini(subscriber: Subscriber): Unit = {
    subscribers.unsubscribe(subscriber)
    onFini(subscriber)
  }

  def finiAll(): Unit = {
    subscribers.unsubscribeAll()
    //TODO: we should probably fire onFini for all ...
  }
}