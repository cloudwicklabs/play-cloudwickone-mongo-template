import org.junit.runner._
import org.specs2.runner._
import play.api.test._
import test.WithDepsApplication

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends PlaySpecification {

  "Application" should {

    "send 404 on a bad request" in new WithDepsApplication {
      route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

  }
}
