package im.mange.sews

object Example extends App {

  final case class Color(red: Int, green: Int, blue: Int)

  sealed abstract class Shape extends Product with Serializable
  final case class Circle(radius: Double, color: Color) extends Shape
  final case class Rectangle(width: Double, height: Double, color: Color) extends Shape

  import bridges.elm._
//  import bridges.core.Type._
//  import bridges.core._
  import bridges.core.syntax._
//  import bridges.SampleTypes._
  import bridges.core.Type._
  import bridges.core.syntax._
//  import org.scalatest._
//  import unindent._

  private val decls = List(
    decl[Color],
    decl[Circle],
    decl[Rectangle],
    decl[Shape]
  )

//  println(Elm.render(decls))
//  println(Elm.jsonDecoder(decls))
  println(Elm.buildFile("Codec", decls, Map.empty[Ref, TypeReplacement]))
}