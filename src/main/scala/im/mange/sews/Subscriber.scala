package im.mange.sews

//TODO: maybe have an atomic counter in here
//TODO: defo have user (aka email) in here
case class Subscriber(send: (String) => Unit) {
  val id: String = toString.split("@").reverse.head.replace(")", "") //TIP: temporary hack for now
}
