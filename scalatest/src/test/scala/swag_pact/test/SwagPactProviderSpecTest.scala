package swag_pact
package test

import scala.concurrent.Future

class SwagPactProviderSpecTest extends SwagPactProviderSpec {

  override def swaggerFile: String = "petstore.json"
  override def url(state: Option[String]): (Future[String], Option[Unit => Unit]) = (
    Future.successful("http://petstore.swagger.io/v2"),
    None
  )

  verify()
}
