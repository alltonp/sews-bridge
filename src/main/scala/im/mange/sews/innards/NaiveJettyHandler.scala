package im.mange.sews.innards

import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import im.mange.sews.innards.NaiveJettyHandler.{ResponseOps, toRequest}
import io.shaka.http.Http.HttpHandler
import io.shaka.http.HttpHeader.httpHeader
import io.shaka.http.Method.method
import io.shaka.http.{Request, _}
import org.eclipse.jetty.server.handler.AbstractHandler

import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter

case class NaiveJettyHandler(handler: HttpHandler) extends AbstractHandler {
  //  def additionalHeaders: List[(HttpHeader, String)]
  //  protected val cacheTtl: Int

  @Override def handle(target: String,
                       baseRequest: org.eclipse.jetty.server.Request,
                       servletRequest: HttpServletRequest,
                       httpServletResponse: HttpServletResponse): Unit = {
    val request: Request = NaiveJettyHandler.toRequest(servletRequest)

    //    additionalHeaders.foreach {
    //      case (header, value) => httpServletResponse.addHeader(header.name, value)
    //    }

    handler(request).writeToServletResponse(httpServletResponse)
    baseRequest.setHandled(true)
  }

}

object NaiveJettyHandler {

  private def optionalEntity(bytes: Array[Byte]) = if (bytes.length > 0) Some(Entity(bytes)) else None

  def toRequest(servletRequest: ServletRequest): Request = {
    val httpRequest = servletRequest.asInstanceOf[HttpServletRequest]

    val entity = optionalEntity(Stream.continually(servletRequest.getInputStream.read).takeWhile(-1 != _).map(_.toByte).toArray)

    val queryParams = Option(httpRequest.getQueryString).filter(_.nonEmpty).map("?" + _).getOrElse("")

    Request(
      method(httpRequest.getMethod),
      httpRequest.getRequestURI + queryParams,
      Headers(httpRequest.getHeaderNames.asScala.toList.flatMap(name => httpRequest.getHeader(name).split(",").toList.map(httpHeader(name) -> _.trim))),
      entity
    )
  }

  //  val penTestHeaders: List[(HttpHeader, String)] = List(
  //    unknownHeader("Strict-Transport-Security") -> "max-age=31536000",
  //    unknownHeader("X-XSS-Protection")          -> "1; mode=block",
  //    unknownHeader("X-Content-Security-Policy") -> "",
  //    unknownHeader("X-WebKit-CSP")              -> "",
  //    unknownHeader("X-Content-Type-Options")    -> "nosniff"
  //  )

  implicit class ResponseOps(response: Response) {
    def writeToServletResponse(servletResponse: HttpServletResponse): Unit = {
      servletResponse.setStatus(response.status.code)
      response.headers.filter { case (header, _) => headerOk(header) }.groupBy(_._1).foreach {
        case (key, values) => servletResponse.addHeader(key.name, values.map(_._2).mkString(","))
      }

      response.entity.foreach(entity => {
        val out = servletResponse.getOutputStream
        out.write(entity.content)
        out.close()
      })
    }
  }

  // This used to remove every unknown header (unknown to Naive) for an unknown reason !
  // It's now keeping everything because we need some unknown headers (like content disposition) & we need a method
  // to lock this in via a unit test.
  def headerOk(httpHeader: HttpHeader): Boolean = httpHeader.name != null
}