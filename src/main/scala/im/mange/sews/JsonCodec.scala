package im.mange.sews

import argonaut.{DecodeJson, DecodeResult, EncodeJson, Parse}

case class JsonCodec[IN, OUT](decoder: DecodeJson[IN], encoder: EncodeJson[OUT]) extends JsonDecoder[IN] with JsonEncoder[OUT]

trait JsonDecoder[IN] {
  val decoder: DecodeJson[IN]

  def decode(value: String): IN =
    Parse.parse(value) match {
      case Left(e) => throw new RuntimeException(s"error $e parsing: $value")
      case Right(json) =>
        val result: DecodeResult[IN] = decoder.decodeJson(json)
        result.getOr(throw new RuntimeException(s"error $result decoding: $json"))
    }
}

trait JsonEncoder[OUT] {
  val encoder: EncodeJson[OUT]

  def encode(out: OUT, pretty: Boolean = false): String = {
    val json = encoder.encode(out)
    if (pretty) json.spaces2 else json.nospaces
  }
}
