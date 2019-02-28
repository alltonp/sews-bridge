package im.mange.sews

case class Subscribers(private var subscribers: Seq[Subscriber], subscriptionDebug: Boolean = false, messageDebug: Boolean = false) {
  def subscribe(subscriber: Subscriber): Unit = {
    synchronized {
      subscribers = subscribers ++ Seq(subscriber)
      //TODO: remove date here and put in subscriber instead
      if (subscriptionDebug) println(s"- subscribe: $subscriber now have ${subscribers.size} -> ${new java.util.Date()}")
    }
  }

  def unsubscribe(subscriber: Subscriber): Unit = {
    synchronized {
      subscribers = subscribers.filterNot(_ == subscriber)
      if (subscriptionDebug) println(s"- unsubscribe $subscriber now have ${subscribers.size}")
    }
  }

  def unsubscribeAll(): Unit = {
    synchronized {
      subscribers.foreach(unsubscribe)
      if (subscriptionDebug) println(s"- unsubscribeAll now have ${subscribers.size}")
    }
  }

  def send(message: String, to: Subscriber): Unit = {
    to.send(message)
    if (messageDebug) println(s"- send: $message to $to")
  }

  def sendAll(message: String): Unit = {
    //TODO: sometimes some subscribers will be gone and this will bharf, need to handle that more nicely
    subscribers.foreach(_.send(message))
    if (messageDebug) println(s"- sendAll: $message - ${subscribers.size} subscribers")
  }

  def all: Seq[Subscriber] = subscribers
}