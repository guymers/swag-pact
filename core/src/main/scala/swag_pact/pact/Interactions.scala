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

  val extensionKey = "x-pact-interactions"

  private val mapper = new ObjectMapper()

  def findInteractions(
    vendorExtensions: => ObjectMap
  ): Either[InteractionExtensionError, List[RequestResponseInteraction]] = {
    vendorExtensions.asScalaMap.get(extensionKey).map { interactionExtensions =>
      findPactInteractions(interactionExtensions).flatMap(parsePactInteractions)
    }.getOrElse(List.empty.asRight)
  }

  private def findPactInteractions(
    interactionExtensions: Object
  ): Either[InteractionExtensionError, List[ObjectMap]] = {

    interactionExtensions match {
      case ls: java.util.List[_] if ls.size() > 0 =>
        ls.get(0) match {
          case _: ObjectNode =>
            val rawInteractions = ls.asInstanceOf[java.util.List[ObjectNode]].asScalaList
            Try {
              rawInteractions.map(o => mapper.convertValue(o, classOf[ObjectMap]))
            } match {
              case Success(interactions) => interactions.asRight
              case Failure(e) => InvalidExtensionFormat(Option(e)).asLeft
            }
          case _ => InvalidExtensionFormat(None).asLeft
        }
      case _ => InvalidExtensionFormat(None).asLeft
    }
  }

  private def parsePactInteractions(
    interactions: List[ObjectMap]
  ): Either[InteractionExtensionError, List[RequestResponseInteraction]] = {

    // need to build the full pact structure
    val map = new java.util.HashMap[String, Object]()
    val provider = new java.util.HashMap[String, Object]()
    map.put("provider", provider)
    val consumer = new java.util.HashMap[String, Object]()
    map.put("consumer", consumer)
    map.put("interactions", interactions.asJava)

    Try {
      val notUsed = null
      PactReader.loadV3Pact(notUsed, map)
    } match {
      case Success(pact: RequestResponsePact) => pact.getInteractions.asScalaList.asRight
      case Success(_) => InvalidPactFormat(None).asLeft
      case Failure(e) => InvalidPactFormat(Option(e)).asLeft
    }
  }
}
