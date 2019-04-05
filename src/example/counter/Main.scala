package counter

object Main extends App {
  import scala.sys.process._

  //PRE:
  //elm install NoRedInk/elm-json-decode-pipeline
  //elm install billstclair/elm-websocket-client

  //TODO: write Codec.elm
  //TODO: write Main.html
  //TODO: compile to elm.js instead
  //TODO: do the port gubbins in the .html
  //TODO: add the pipeline/decoder stuff that bridges needs

  //TODO: run this in currently working directory
  "./build.sh".!<


  //TODO: docs, point to https://github.com/davegurnell/bridges
  //see: https://github.com/NoRedInk/elm-decode-pipeline -> https://github.com/NoRedInk/elm-json-decode-pipeline
  //see: https://github.com/billstclair/elm-websocket-client
}
