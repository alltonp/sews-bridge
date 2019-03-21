

//APP
object Example extends App {
  import im.mange.sews._
  import im.mange.sews.db.{Db, DbCmd, FileStore}
  import io.shaka.http.Http.HttpHandler
  import io.shaka.http.Request.GET
  import io.shaka.http.StaticResponse.static
  import bridges.core.Type.Ref
  import bridges.core.syntax.decl
  import bridges.elm.{Elm, TypeReplacement}

  private val decls = List(
    decl[ServerModel],
    decl[ToServer],
    decl[FromServer]
  )

  println(Elm.buildFile("Codec", decls, Map.empty[Ref, TypeReplacement]))

  LaunchApplication(8888, Configs.default)

  object Configs {
    //TODO: ultimately remove the db ness and make this be Counter
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
      //TODO: should be src/example now
      case GET("/")  => static("src/main/resources", "/index.html")
      case GET(path) => static("src/main/resources", path)
    }
  }
}