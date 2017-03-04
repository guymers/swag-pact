package swag_pact
package pact

import cats.Show
import cats.instances.string._
import cats.syntax.semigroup._
import cats.syntax.show._

sealed trait InteractionExtensionError extends Product with Serializable
object InteractionExtensionError {
  final case class InvalidExtensionFormat(cause: Option[Throwable]) extends InteractionExtensionError
  case object InvalidRequestResponsePactFormat extends InteractionExtensionError
  final case class InvalidPactFormat(cause: Throwable) extends InteractionExtensionError

  implicit val show: Show[InteractionExtensionError] = Show.show {
    case InvalidExtensionFormat(cause) =>
      "Expected a key '" |+| Interactions.extensionKey |+| "' containing an array of objects" |+| cause.show
    case InvalidRequestResponsePactFormat =>
      "Pact is not in request-response format"
    case InvalidPactFormat(cause) =>
      "Invalid pact interaction format: " |+| cause.show
  }
}
