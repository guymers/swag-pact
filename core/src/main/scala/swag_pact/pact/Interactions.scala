package swag_pact
package pact

import au.com.dius.pact.model.{PactReader, RequestResponseInteraction, RequestResponsePact}
import cats.syntax.either._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object Interactions {
  import InteractionExtensionError._

  type ObjectMap = java.util.Map[String, Object]

  val extensionKey: String = "x-pact-interactions"

  private val mapper = new ObjectMapper()

  def findInteractions(
    vendorExtensions: => ObjectMap
  ): Either[InteractionExtensionError, List[RequestResponseInteraction]] = {
    vendorExtensions.asScalaMap.get(extensionKey).map { interactionExtensions =>
      findPactInteractions(interactionExtensions).flatMap(parsePactInteractions)
    }.getOrElse(List.empty[RequestResponseInteraction].asRight[InteractionExtensionError])
  }

  private def findPactInteractions(
    interactionExtensions: Object
  ): Either[InteractionExtensionError, List[ObjectMap]] = {

    interactionExtensions match {
      case ls: java.util.List[_] if ls.size() > 0 =>
        ls.get(0) match {
          case _: ObjectNode =>
            @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
            val rawInteractions = ls.asInstanceOf[java.util.List[ObjectNode]].asScalaList
            Try {
              rawInteractions.map(o => mapper.convertValue(o, classOf[ObjectMap]))
            } match {
              case Success(interactions) => interactions.asRight[InteractionExtensionError]
              case Failure(e) => InvalidExtensionFormat(Option(e)).asLeft[List[ObjectMap]]
            }
          case _ => InvalidExtensionFormat(None).asLeft[List[ObjectMap]]
        }
      case _ => InvalidExtensionFormat(None).asLeft[List[ObjectMap]]
    }
  }

  private def parsePactInteractions(
    interactions: List[ObjectMap]
  ): Either[InteractionExtensionError, List[RequestResponseInteraction]] = {

    // need to build the full pact structure
    val map = createPactStructure(interactions)

    Try {
      @SuppressWarnings(Array("org.wartremover.warts.Null"))
      val notUsed = null
      PactReader.loadV3Pact(notUsed, map)
    } match {
      case Success(pact: RequestResponsePact) => pact.getInteractions.asScalaList.asRight[InteractionExtensionError]
      case Success(_) => InvalidRequestResponsePactFormat.asLeft[List[RequestResponseInteraction]]
      case Failure(e) => InvalidPactFormat(e).asLeft[List[RequestResponseInteraction]]
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private def createPactStructure(interactions: List[ObjectMap]) = {
    val map = new java.util.HashMap[String, Object]()
    val provider = new java.util.HashMap[String, Object]()
    map.put("provider", provider)
    val consumer = new java.util.HashMap[String, Object]()
    map.put("consumer", consumer)
    map.put("interactions", interactions.asJava)
    map
  }
}
