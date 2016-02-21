package controllers

import com.mohiva.play.silhouette.api.{Environment, LoginInfo}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.test._
import models.User
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import play.api.test.{FakeRequest, PlaySpecification}
import test.WithDepsApplication
import scala.concurrent.ExecutionContext.Implicits.global

@RunWith(classOf[JUnitRunner])
class ApplicationControllerSpec extends PlaySpecification with Mockito {
  sequential

  "The `index` action" should {
    "redirect to login page if user is unauthorized" in new Context {
      new WithDepsApplication {
        val Some(redirectResult) = route(FakeRequest(routes.Application.index())
          .withAuthenticator[CookieAuthenticator](LoginInfo("invalid", "invalid"))
        )

        status(redirectResult) must be equalTo SEE_OTHER

        val redirectURL = redirectLocation(redirectResult).getOrElse("")
        redirectURL must contain(routes.Application.signIn().toString())

        val Some(unauthorizedResult) = route(FakeRequest(GET, redirectURL))

        status(unauthorizedResult) must be equalTo OK
        contentType(unauthorizedResult) must beSome("text/html")
        contentAsString(unauthorizedResult) must contain("Cloudwick One - Sign In")
      }
    }

    "return 200 if user is authorized" in new Context {
      new WithDepsApplication {
        val Some(result) = route(FakeRequest(routes.Application.index())
          .withAuthenticator[CookieAuthenticator](identity.loginInfo)
        )

        status(result) must beEqualTo(OK)
      }
    }
  }

  /**
    * The context.
    */
  trait Context extends Scope {

    /**
      * An identity.
      */
    val identity = User(
      _id = "abc@cloudwick.com",
      loginInfo = LoginInfo("google", "abc@cloudwick.com"),
      firstName = None,
      lastName = None,
      fullName = None,
      avatarURL = None
    )

    implicit val env: Environment[User, CookieAuthenticator] = new FakeEnvironment[User, CookieAuthenticator](Seq(identity.loginInfo -> identity))
  }
}
