package swag_pact
package swagger

import cats.Show
import cats.data.NonEmptyList
import cats.instances.string._
import cats.syntax.semigroup._
import io.swagger.models.HttpMethod
import swag_pact.pact.InteractionExtensionError

sealed trait SwaggerError extends Product with Serializable
object SwaggerError {
  final case class MissingFile(filename: String) extends SwaggerError
  final case class OperationErrors(httpMethod: HttpMethod, errors: NonEmptyList[SwaggerOperationError])
    extends SwaggerError

  implicit val show: Show[SwaggerError] = Show.show {
    case MissingFile(filename) =>
      "File does not exist " |+| filename
    case OperationErrors(httpMethod, errors) =>
      "Errors for method " |+| httpMethod.name() |+| ": " |+| errors.show
  }
}

sealed trait SwaggerOperationError extends Product with Serializable
object SwaggerOperationError {
  final case class InvalidContentType(contentType: String) extends SwaggerOperationError
  final case class InvalidStatusCode(statusCode: String) extends SwaggerOperationError
  final case class InteractionError(errors: NonEmptyList[InteractionExtensionError]) extends SwaggerOperationError

  implicit val show: Show[SwaggerOperationError] = Show.show {
    case InvalidContentType(contentType) =>
      "Invalid content type " |+| contentType
    case InvalidStatusCode(statusCode) =>
      "Invalid status code " |+| statusCode
    case InteractionError(errors) =>
      errors.show
  }
}
