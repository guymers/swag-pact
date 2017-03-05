package swag_pact
package test

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.circe.Json
import io.circe.syntax._
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Future

class SwagPactProviderSpecTest extends SwagPactProviderSpec with BeforeAndAfterAll {

  var wireMockServer: WireMockServer = _

  override def beforeAll(): Unit = {
    wireMockServer = createMockServer()
    wireMockServer.start()
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
  }

  override def swaggerFile: String = "petstore.json"
  override def url(state: Option[String]): (Future[String], Option[Unit => Unit]) =
    (Future.successful(s"http://localhost:${wireMockServer.port()}"), None)

  verify()

  private def createMockServer() = {
    val wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort())

    val internalMutationA = wireMockServer.stubFor {
      WireMock
        .get(WireMock.urlEqualTo("/pet/1"))
        .willReturn {
          WireMock
            .aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              Json
                .obj(
                  "id" -> "debbc598-ca32-4603-b3d5-cc734e7cad81".asJson,
                  "category" -> Json.obj("id" -> 0.asJson, "name" -> "string".asJson),
                  "name" -> "doggie".asJson,
                  "dateOfBirth" -> "2017-01-01".asJson,
                  "lastModified" -> "2017-01-01T01:02:03Z".asJson,
                  "photoUrls" -> Json.arr("string".asJson),
                  "tags" -> Json.arr(Json.obj("id" -> 0.asJson, "name" -> "string".asJson)),
                  "status" -> "available".asJson
                )
                .noSpaces
            )
        }
    }

    val internalMutationB = wireMockServer.stubFor {
      WireMock
        .get(WireMock.urlEqualTo("/pet/2"))
        .willReturn {
          WireMock
            .aResponse()
            .withStatus(404)
        }
    }

    wireMockServer
  }
}
