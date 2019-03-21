import bridges.core.Type.Ref
import bridges.core.syntax.decl
import bridges.elm.{Elm, TypeReplacement}
import im.mange.sews.{Config, LaunchApplication}

object BridgesExample extends App {
  final case class Color(red: Int, green: Int, blue: Int)

  sealed abstract class Shape extends Product with Serializable
  final case class Circle(radius: Double, color: Color, width: Int) extends Shape
  final case class Rectangle(width: Double, height: Double, color: Color) extends Shape

  import bridges.elm._
//  import bridges.core.Type._
//  import bridges.core._
//  import bridges.SampleTypes._
  import bridges.core.Type._
  import bridges.core.syntax._
//  import org.scalatest._
//  import unindent._

  case class All()

  private val decls = List(
    decl[All],
    decl[Color],
    decl[Circle],
    decl[Rectangle],
    decl[Shape]
  )

//  println(Elm.render(decls))
//  println(Elm.jsonDecoder(decls))
  println(Elm.buildFile("Codec", decls, Map.empty[Ref, TypeReplacement]))
}

//APP
object Runner extends App {
  import im.mange.sews._
  import im.mange.sews.db.{Db, DbCmd, FileStore}
  import io.shaka.http.Http.HttpHandler
  import io.shaka.http.Request.GET
  import io.shaka.http.StaticResponse.static

  private val decls = List(
    decl[ServerModel],
    decl[ToServer],
    decl[FromServer]
  )

  println(Elm.buildFile("Codec", decls, Map.empty[Ref, TypeReplacement]))

  LaunchApplication(8888, Configs.default)

  object Configs {
    private val db = Db(FileStore("target"), Codecs.dbCodec)

    val default = Config(Endpoints.all,
      Program(
        model = db.loadOrElse("chat", ServerModel(Nil, Nil)),
        update = ServerUpdate(Codecs.msgCodec, db),
        init = (subscriber) => Some(Init(subscriber.id)),
        fini = (subscriber) => Some(Fini(subscriber.id))
      ))
  }

  object Codecs {
    import argonaut.DecodeJson
    import argonaut._, ArgonautShapeless._ //TIP: do not optimise imports

    val dbCodec = JsonCodec(DecodeJson.of[ServerModel], EncodeJson.of[ServerModel])
    val msgCodec = JsonCodec(DecodeJson.of[ToServer], EncodeJson.of[FromServer])
  }

  //MODEL
  case class ServerModel(chatMessages: List[ChatMessage], currentChatters: List[String])
  case class ChatMessage(content: String)

  //MSG
  sealed trait ToServer
  case class Init(subscriberName: String) extends ToServer
  case class Fini(subscriberName: String) extends ToServer
  case class Send(message: String) extends ToServer

  sealed trait FromServer
  case class ModelUpdated(model: ServerModel) extends FromServer

  //UPDATE
  case class ServerUpdate(msgCodec: JsonCodec[ToServer, FromServer], db: Db[ServerModel],
                          subscribers: Subscribers = Subscribers(Nil)
                         ) extends Update[ToServer, ServerModel, FromServer] {

    //TODO: possibly these should be passed in ... would support spying etc ...
    private val wsCmd = WsCmd(msgCodec, subscribers)
    private val dbCmd = DbCmd(db)

    override def update(msg: ToServer, model: ServerModel, from: Option[Subscriber]): (ServerModel, Cmd) = {
      msg match {

        case m:Init =>
          val model_ = model.copy(currentChatters = m.subscriberName :: model.currentChatters)
          (model_, wsCmd.sendAll(ModelUpdated(model_)))

        case m:Fini =>
          val model_ = model.copy(currentChatters = model.currentChatters.filterNot(_ == m.subscriberName ))
          (model_, wsCmd.sendAll(ModelUpdated(model_)))

        case m:Send =>
          val model_ = model.copy(chatMessages = ChatMessage(m.message) :: model.chatMessages)
          val cmd = Cmd.batch(
            //TODO: respond to sender ... wsCmd.send("", from),
            dbCmd.save("chat", model_.copy(currentChatters = Nil)),
            wsCmd.sendAll(ModelUpdated(model_))
          )

          (model_, cmd)
      }
    }
  }

  object Endpoints {
    val all: HttpHandler = {
      case GET("/")  => static("src/main/resources", "/index.html")
      case GET(path) => static("src/main/resources", path)
    }
  }
}