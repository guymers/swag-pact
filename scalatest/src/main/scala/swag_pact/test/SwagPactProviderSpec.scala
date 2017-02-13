package swag_pact
package test

import au.com.dius.pact.model.{FullResponseMatch, RequestResponseInteraction, ResponseMatching}
import au.com.dius.pact.provider.sbtsupport.HttpClient
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.AsyncWordSpec
import swag_pact.swagger.{Swagger, SwaggerOperation}
import swag_pact.validation.Validation
import cats.syntax.semigroup._
import cats.syntax.show._
import cats.instances.string._
import org.scalatest.compatible.Assertion

import scala.concurrent.{Future, Promise}

trait SwagPactProviderSpec extends AsyncWordSpec {

  /**
    * Path to the swagger file to run against
    */
  def swaggerFile: String

  /**
    * Return the base url and an optional onComplete function.
    */
  def url(state: Option[String]): (Future[String], Option[Unit => Unit])

  /**
    * Verifies a Swagger file with Pact.
    * Each interaction will be run as a standalone test.
    */
  def verify(): Unit = {
    val swagger = Swagger.parse(swaggerFile)
    swagger match {
      case Invalid(errs) => fail(errs.show)
      case Valid(s) =>
        s.title when {

          s.operations.foreach { case (urlPart, methods) =>
            methods.foreach { case (method, operation) =>
              if (operation.interactions.nonEmpty) {
                "a " |+| method.name() |+| " request to " |+| urlPart.part should {
                  operation.interactions.foreach(runInteraction(operation))
                }
              }
            }
          }
        }
    }
  }

  private def runInteraction(operation: SwaggerOperation)(interaction: RequestResponseInteraction): Unit = {
    val providerState = Option(interaction.getProviderState)
    val providerStateDescription = providerState.map(state => " given " |+| state).getOrElse("")

    interaction.getDescription + providerStateDescription taggedAs SwagPactProviderTag in {

      val result = Validation.validateResponse(operation, interaction.getResponse)
      result match {
        case Invalid(errs) => fail(errs.show)
        case Valid(errs) if errs.nonEmpty => fail(errs.map(_.show).mkString("\n"))
        case Valid(_) =>
          val p = Promise[Assertion]

          val (urlFuture, onComplete) = url(providerState)
          urlFuture
            .flatMap { url =>
              val request = interaction.getRequest.copy
              request.setPath(url |+| interaction.getRequest.getPath)
              HttpClient.run(request)
            }
            .map { response =>
              val responseMatch = ResponseMatching.matchRules(interaction.getResponse, response)
              assert(responseMatch === FullResponseMatch)
            }
            .onComplete { t =>
              onComplete.foreach { func => func(()) }
              p.complete(t)
            }
          p.future
      }
    }
  }
}
