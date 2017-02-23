package swag_pact
package validation

import au.com.dius.pact.model.{Response => PactResponse}
import cats.data.{NonEmptyList, ValidatedNel}
import cats.instances.string._
import cats.kernel.Eq
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.eq._
import cats.syntax.option._
import cats.syntax.validated._
import org.apache.http.HttpHeaders
import org.apache.http.entity.ContentType
import swag_pact.http.ContentTypeUtils
import swag_pact.properties.{Compare, CompareError, Property}
import swag_pact.swagger.SwaggerOperation

object Validation {
  import PactResponseError._

  def validateResponse(
    operation: SwaggerOperation,
    response: PactResponse
  ): ValidatedNel[PactResponseError, List[CompareError]] = {
    val statusCode = response.getStatus.toInt
    val swaggerResponse = operation.findResponse(statusCode)

    swaggerResponse.toValidNel(MissingSwaggerResponse(statusCode, operation.responses.map(_.statusCode)))
      .andThen { swaggerResponse =>
        val rawBody = response.getBody

        response.getHeaders.asScalaMap.get(HttpHeaders.CONTENT_TYPE) match {
          case None =>
            val validateBodyNotPresentMissing =
              if (rawBody.isPresent) unexpectedBody(rawBody.getValue).invalidNel
              else List.empty[CompareError].validNel
            val validateNoSchema = swaggerResponse.body match {
              case None => List.empty[CompareError].validNel
              case Some(schema) => unexpectedSchema(schema).invalidNel
            }
            validateBodyNotPresentMissing.map2(validateNoSchema)(_ ++ _)

          case Some(rawContentType) =>
            val validateBodyPreset = if (!rawBody.isPresent) missingBody.invalidNel else rawBody.getValue.validNel

            validateValidContentType(rawContentType)
              .andThen(validateContentTypeInList(operation.produces))
              .map2(validateBodyPreset)((_, _))
              .andThen { case (contentType, body) =>
                if (contentType.getMimeType === ContentType.APPLICATION_JSON.getMimeType) {
                  val json = io.circe.parser.parse(body)
                  json.toValidated.bimap(e => NonEmptyList.of(InvalidBody(contentType, e.some)), Property.fromJson)
                } else UnsupportedContentType(contentType).invalidNel
              }
              .andThen { property =>
                swaggerResponse.body match {
                  case None => MissingSchema.invalidNel
                  case Some(schema) => Compare.compare(schema, property).validNel
                }
              }
        }
      }
  }

  private def validateValidContentType(contentType: String): ValidatedNel[PactResponseError, ContentType] =
    ContentTypeUtils.parse(contentType).toValidNel(InvalidContentType(contentType))

  private def validateContentTypeInList(contentTypes: List[ContentType])(contentType: ContentType): ValidatedNel[PactResponseError, ContentType] =
    contentTypes.find(_ === contentType).toValidNel(MissingContentType(contentType, contentTypes))

  implicit val eqContentType: Eq[ContentType] = new Eq[ContentType] {
    override def eqv(x: ContentType, y: ContentType): Boolean =
      x.getMimeType === y.getMimeType
  }

}
