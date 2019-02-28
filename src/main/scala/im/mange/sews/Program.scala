package im.mange.sews

import im.mange.sews.innards.WebSocketProgram

case class Program[IN, MODEL, OUT](private var model: MODEL,
                                   private val update: Update[IN, MODEL, OUT],
                                   private val init: (Subscriber => Option[IN]) = (s: Subscriber) => None,
                                   private val fini: (Subscriber => Option[IN]) = (s: Subscriber) => None,
                                   private val updateDebug: Boolean = false,
                                   private val rollbackModelChangesOnError: Boolean = false
                                  ) extends WebSocketProgram {

  private [sews] val subscribers = update.subscribers

  override def onInit(subscriber: Subscriber): Unit = init(subscriber).foreach(doUpdate(_, Some(subscriber)))
  override def onFini(subscriber: Subscriber): Unit = fini(subscriber).foreach(doUpdate(_, Some(subscriber)))
  override def onMessage(message: String, from: Option[Subscriber]): Unit = doUpdate(update.msgCodec.decode(message), from)

  private def doUpdate(msg: IN, from: Option[Subscriber]): Unit = {
    synchronized {
      val modelBeforeUpdate = model

      try {
        val (model_, cmd: Cmd) = update.update(msg, model, from)
        model = model_
        cmd.run()
      }

      catch {
        case e: Exception =>
          //TIP: when we stop the server we get a lot of: RemoteEndpoint unavailable, current state [CLOSING], expecting [OPEN or CONNECTED]
          //... maybe should protect against this
          println("* Error during update: " + e.getMessage ++ "\n" ++ e.getStackTrace.toList.mkString("\n"))
          if (rollbackModelChangesOnError) {
            println("* Rolling back model changes")
            model = modelBeforeUpdate
          }

          //TODO: should we re-throw these? for closed connections defo not
          //TODO: should we catch and throw errors?
          //TODO: if in dev then we should tell client that an error has occured
      }

      finally {
        if (updateDebug) println(s"- Update: $msg to model now: $model -> was: $modelBeforeUpdate")
      }
    }
  }

  //TODO: instead of this (or as well as) we could make doUpdate return MODEL.
  def copyServerModel: MODEL = model //TIP: this could be dirty obvs
}