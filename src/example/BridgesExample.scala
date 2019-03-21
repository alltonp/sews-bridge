import bridges.core.Type.Ref
import bridges.core.syntax.decl
import bridges.elm.{Elm, TypeReplacement}
import im.mange.sews.{Config, LaunchApplication}
import io.circe
import io.circe.Json
import io.circe.generic.extras.Configuration

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
  private val customTypeReplacements = Map.empty[Ref, TypeReplacement]

  println(Elm.buildFile("Codec", decls, customTypeReplacements))

//  Elm.decoder(decls, customTypeReplacements)

//  import argonaut._, Argonaut._

//  import io.circe.generic.auto._, io.circe.syntax._
////  import io.circe.generic.auto._
//  import io.circe.generic.extras.auto._
//  import io.circe.syntax._
////  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
//
//  import io.circe._, io.circe.parser._
//  import io.circe.syntax._
//  import io.circe.generic.extras._
//
////  import io.circe.{ Decoder, Encoder, JsonObject }
////  import io.circe.generic.extras.Configuration
//  import io.circe.generic.extras.decoding.ConfiguredDecoder
//  import io.circe.generic.extras.encoding.ConfiguredObjectEncoder
//  import io.circe.generic.extras.semiauto.{ deriveDecoder, deriveEncoder }
//  import shapeless.Lazy
//
////  CirceConfiguration.genDevConfig
//
//  import io.circe.generic.extras.semiauto._

  import io.circe.parser.decode, io.circe.syntax._
  import io.circe.generic.extras.Configuration, io.circe.generic.extras.auto._

  implicit val configuration: Configuration =
    Configuration.default.withDiscriminator("type")

//  implicit val encWrap: Encoder[Shape] = deriveEncoder[Shape]

  println("\njson:")
  val x: Shape = Rectangle(1.2, 1.3, Color(1,2,3))
  private val json = x.asJson.noSpaces
  println(json)

//  val json2 = """{"type":"Rectangle", "width":1.2,"height":1.3,"color":{"red":1,"green":2,"blue":3}}"""

  println("\nobject:")
  private val decoded: Either[circe.Error, Shape] = decode[Shape](json)
  println(decoded)
}



//https://underscore.io/blog/posts/2018/12/12/bridges.html
//https://github.com/circe/circe/pull/429
//https://stackoverflow.com/questions/43094140/circe-type-field-not-showing

object CirceConfiguration {
  import io.circe.{ Decoder, Encoder, JsonObject }
  import io.circe.generic.extras.Configuration
  import io.circe.generic.extras.decoding.ConfiguredDecoder
  import io.circe.generic.extras.encoding.ConfiguredObjectEncoder
  import io.circe.generic.extras.semiauto.{ deriveDecoder, deriveEncoder }
  import shapeless.Lazy

  // NOTE: this requires semiauto from 'extras' package to be used in decoders/encoders
  implicit val genDevConfig: Configuration =
    Configuration.default.withDiscriminator("type")

  def deriveCustomEncoder[A](implicit encode: Lazy[ConfiguredObjectEncoder[A]]): Encoder[A] =
    deriveEncoder[A].mapJsonObject(excludeNullsFromJson)

  def deriveCustomDecoder[A](implicit decode: Lazy[ConfiguredDecoder[A]]): Decoder[A] =
    deriveDecoder[A]

  def excludeNullsFromJson(jsonObject: JsonObject): JsonObject =
    jsonObject.filter {
      case (_, value) => !value.isNull
    }
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