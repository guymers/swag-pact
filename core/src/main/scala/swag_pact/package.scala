import cats.Show
import cats.instances.string._
import cats.syntax.semigroup._
import cats.syntax.show._
import org.apache.http.entity.ContentType

package object swag_pact {
  import scala.collection.JavaConverters._

  // many swagger methods that return maps or lists can return null

  final implicit class JavaMapExtensions[K, V](map: => java.util.Map[K, V]) {
    def asScalaMap: Map[K, V] = {
      Option(map).map(_.asScala.toMap).getOrElse(Map.empty[K, V])
    }
  }

  final implicit class JavaListExtensions[E](list: => java.util.List[E]) {
    def asScalaList: List[E] = {
      Option(list).map(_.asScala.toList).getOrElse(List.empty[E])
    }
  }

  implicit val showOptionalThrowable: Show[Option[Throwable]] = Show.show {
    case Some(t) => ": " |+| t.show
    case None => ""
  }

  implicit val showThrowable: Show[Throwable] = Show.fromToString

  implicit val showContentType: Show[ContentType] = Show.show { _.toString }
}
