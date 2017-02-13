package swag_pact.http

import org.apache.http.entity.ContentType

import scala.util.Try

object ContentTypeUtils {

  def parse(s: String): Option[ContentType] = {
    Option(s).flatMap(str => Try { ContentType.parse(str) }.toOption)
  }
}
