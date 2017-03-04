package swag_pact
package swagger

import au.com.dius.pact.model.RequestResponseInteraction
import cats.data.{NonEmptyList, ValidatedNel}
import cats.instances.list._
import cats.instances.string._
import cats.syntax.cartesian._
import cats.syntax.either._
import cats.syntax.eq._
import cats.syntax.option._
import cats.syntax.traverse._
import io.swagger.models.properties.{Property => SwaggerProperty}
import io.swagger.models.{HttpMethod, Operation, Response, Swagger => SwaggerSwagger}
import io.swagger.parser.SwaggerParser
import org.apache.http.entity.ContentType
import swag_pact.http.ContentTypeUtils
import swag_pact.pact.{InteractionExtensionError, Interactions}
import swag_pact.properties.Property

import scala.util.Try

final case class Swagger(
  title: String,
  operations: Map[UrlPart, Map[HttpMethod, SwaggerOperation]]
)

object Swagger {

  private val defaultResponseKey = "default"

  def parse(file: String): ValidatedNel[SwaggerError, Swagger] = {
    Option(new SwaggerParser().read(file)).toValidNel(SwaggerError.MissingFile(file))
      .andThen(convertSwagger)
  }

  private def convertSwagger(swagger: SwaggerSwagger): ValidatedNel[SwaggerError, Swagger] = {
    val definitions = swagger.getDefinitions.asScalaMap
    def createProperty(schema: SwaggerProperty) = Property.fromSwagger(definitions, schema)

    val operations = swagger.getPaths.asScalaMap.toList.traverseU { case (urlPart, path) =>
      val pathParams = path.getParameters.asScalaList
      val operations = path.getOperationMap.asScalaMap.toList.traverseU { case (httpMethod, operation) =>
        pathParams.foreach(operation.addParameter)

        convertOperation(operation, createProperty)
          .map(v => httpMethod -> v)
          .leftMap(e => NonEmptyList.of(SwaggerError.OperationErrors(httpMethod, e)))
      }

      operations.map(v => UrlPart(urlPart) -> v.toMap)
    }

    operations.map { operations =>
      Swagger(
        title = swagger.getInfo.getTitle,
        operations = operations.toMap
      )
    }
  }

  private def convertOperation(
    operation: Operation,
    createProperty: SwaggerProperty => Property
  ): ValidatedNel[SwaggerOperationError, SwaggerOperation] = {

    {
      validateContentTypes(operation.getProduces) |@|
        validateContentTypes(operation.getConsumes) |@|
        validateResponses(operation.getResponses, createProperty) |@|
        validateInteractions(operation.getVendorExtensions)
    } map { case (produces, consumes, (defaultResponse, responses), interactions) =>
      SwaggerOperation(
        produces = produces,
        consumes = consumes,
        defaultResponse = defaultResponse,
        responses = responses,
        interactions = interactions
      )
    }
  }

  private def validateContentTypes(
    contentTypes: => java.util.List[String]
  ): ValidatedNel[SwaggerOperationError, List[ContentType]] = {

    contentTypes.asScalaList.traverseU { s =>
      ContentTypeUtils.parse(s).toValidNel(SwaggerOperationError.InvalidContentType(s))
    }
  }

  private def validateResponses(
    responses: => java.util.Map[String, Response],
    createProperty: SwaggerProperty => Property
  ): ValidatedNel[SwaggerOperationError, (Option[DefaultSwaggerResponse], List[StatusSwaggerResponse])] = {

    def createHeaders(headers: => java.util.Map[String, SwaggerProperty]) = headers.asScalaMap.mapValues(createProperty)
    def createBody(schema: SwaggerProperty) = Option(schema).map(createProperty)

    val rawResponses = responses.asScalaMap

    val convertedResponses = rawResponses
      .filter { case (status, _) => status =!= defaultResponseKey }
      .toList
      .traverseU { case (status, response) =>
        val statusCode = Try { status.toInt }.toOption.toValidNel(SwaggerOperationError.InvalidStatusCode(status))
        statusCode.map { statusCode =>
          StatusSwaggerResponse(
            statusCode = statusCode,
            headers = createHeaders(response.getHeaders),
            body = createBody(response.getSchema)
          )
        }
      }

    convertedResponses.map { convertedResponses =>
      val defaultResponse = rawResponses.get(defaultResponseKey).map { response =>
        DefaultSwaggerResponse(
          headers = createHeaders(response.getHeaders),
          body = createBody(response.getSchema)
        )
      }

      (defaultResponse, convertedResponses)
    }
  }

  private def validateInteractions(
    vendorExtensions: => java.util.Map[String, Object]
  ): ValidatedNel[SwaggerOperationError, List[RequestResponseInteraction]] = {
    Interactions.findInteractions(vendorExtensions).toValidatedNel
      .leftMap { (errors: NonEmptyList[InteractionExtensionError]) =>
        NonEmptyList.of(SwaggerOperationError.InteractionError(errors))
      }
  }
}
