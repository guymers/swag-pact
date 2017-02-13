package swag_pact
package swagger

import au.com.dius.pact.model.RequestResponseInteraction
import cats.instances.int._
import cats.syntax.eq._
import org.apache.http.entity.ContentType
import swag_pact.properties.Property

final case class UrlPart(part: String)

final case class SwaggerOperation(
  produces: List[ContentType],
  consumes: List[ContentType],
  defaultResponse: Option[DefaultSwaggerResponse],
  responses: List[StatusSwaggerResponse],
  interactions: List[RequestResponseInteraction]
) {
  def findResponse(statusCode: Int): Option[SwaggerResponse] = {
    responses.find(_.statusCode === statusCode).orElse(defaultResponse)
  }
}

sealed trait SwaggerResponse extends Product with Serializable {
  def headers: Map[String, Property]
  def body: Option[Property]
}
final case class DefaultSwaggerResponse(
  headers: Map[String, Property],
  body: Option[Property]
) extends SwaggerResponse

final case class StatusSwaggerResponse(
  statusCode: Int,
  headers: Map[String, Property],
  body: Option[Property]
) extends SwaggerResponse
