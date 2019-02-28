package im.mange.sews

case class WsCmd[IN, OUT](codec: JsonCodec[IN, OUT], all: Subscribers) {
  def send(msg: OUT, to: Option[Subscriber]): Cmd = () => { to.foreach(all.send(jsonise(msg), _)) }
  def sendAll(msg: OUT): Cmd = () => { all.sendAll(jsonise(msg)) }

  private def jsonise(msg: OUT) = codec.encode(msg)
}
