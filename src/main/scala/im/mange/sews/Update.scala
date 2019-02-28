package im.mange.sews

trait Update[IN, MODEL, OUT] {
  val msgCodec: JsonCodec[IN, OUT]
  val subscribers: Subscribers

  def update(msg: IN, model: MODEL, from: Option[Subscriber]): (MODEL, Cmd)
}
