package im.mange.sews

import im.mange.sews.innards._
import io.shaka.http.Http.HttpHandler

//TODO: make it so we csn pass in /ws/ path
object LaunchApplication {
  def apply(port: Int, config: Config): WebSocketProgram = {
    WebServer(port,
      Handler("/ws/*", JettyWebSocketHandler(config.program)),
      Handler("", NaiveJettyHandler(config.endpoints))
    ).start()
    config.program
  }
}

case class Config(endpoints: HttpHandler, program: WebSocketProgram)
