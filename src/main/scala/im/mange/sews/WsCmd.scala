package im.mange.sews

//TIP: interestingly, there is nothing actually WebSockety about this... it could work for anything e.g. CLI
//.. although the jsonising is possibly a seperate concern
//TODO: maybe move to sews.websocket
//TODO: think about ConsoleCmd
case class WsCmd[IN, OUT](codec: JsonCodec[IN, OUT], all: Subscribers) {
  def send(msg: OUT, to: Option[Subscriber]): Cmd = () => { to.foreach(all.send(jsonise(msg), _)) }
  def sendAll(msg: OUT): Cmd = () => { all.sendAll(jsonise(msg)) }

  private def jsonise(msg: OUT) = codec.encode(msg)
}
