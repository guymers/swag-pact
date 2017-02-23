package swag_pact
package validation

import cats.Show
import cats.instances.string._
import cats.syntax.semigroup._
import cats.syntax.show._
import org.apache.http.entity.ContentType
import swag_pact.properties.Property

sealed abstract class PactResponseError extends Product with Serializable
object PactResponseError {
  final case class MissingSwaggerResponse(statusCode: Int, availableStatusCodes: List[Int]) extends PactResponseError
  final case class InvalidContentType(contentType: String) extends PactResponseError
  final case class MissingContentType(contentType: ContentType, availableContentTypes: List[ContentType]) extends PactResponseError

  case object MissingBody extends PactResponseError
  final case class UnexpectedBody(body: String) extends PactResponseError
  final case class InvalidBody(contentType: ContentType, e: Option[Throwable]) extends PactResponseError
  final case class UnsupportedContentType(contentType: ContentType) extends PactResponseError

  case object MissingSchema extends PactResponseError
  final case class UnexpectedSchema(schema: Property) extends PactResponseError

  def missingBody: PactResponseError = MissingBody
  def unexpectedBody(body: String): PactResponseError = UnexpectedBody(body)
  def unexpectedSchema(schema: Property): PactResponseError = UnexpectedSchema(schema)

  implicit val show: Show[PactResponseError] = Show.show {
    case MissingSwaggerResponse(statusCode, availableStatusCodes) =>
      "No Swagger response with status code " |+| statusCode.toString |+| " available status codes: " |+| availableStatusCodes.mkString(", ")
    case InvalidContentType(contentType) =>
      "Invalid content type " |+| contentType
    case MissingContentType(contentType, availableContentTypes) =>
      "No content type " |+| contentType.show |+| " in Swagger response available status codes: " |+| availableContentTypes.map(_.show).mkString(", ")

    case MissingBody =>
      "Swagger response has body but Pact does not"
    case UnexpectedBody(body) =>
      "Swagger response has no body but Pact does: " |+| body
    case InvalidBody(contentType, e) =>
      "Pact response with content type " |+| contentType.show |+| " could not be parsed" |+| e.show
    case UnsupportedContentType(contentType) =>
      "Content type " |+| contentType.show |+| " is not supported"

    case MissingSchema =>
      "Could not find schema in Swagger response"
    case UnexpectedSchema(schema) =>
      "Swagger response has schema " |+| schema.show |+| " when it should have had none"
  }
}
