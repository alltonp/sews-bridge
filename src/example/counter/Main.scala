package counter

object Main extends App {
  import scala.sys.process._

  //PRE:
  //elm install NoRedInk/elm-json-decode-pipeline
  //elm install billstclair/elm-websocket-client

  //TODO: auto-generate Codec.elm
  //TODO: hand-write Main.html (inc the port gubbins see:  https://github.com/billstclair/elm-websocket-client)
  //TODO: have build.sh compile to elm.js instead

  //TODO: run this in currently working directory
  "./build.sh".!<

  //TODO: start the app itself
  //TODO: docs, point to https://github.com/davegurnell/bridges
}
