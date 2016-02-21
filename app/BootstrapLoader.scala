package app

import com.mohiva.play.silhouette.api.util.{Clock, PlayHTTPLayer}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticatorSettings, CookieAuthenticatorService, CookieAuthenticator}
import com.mohiva.play.silhouette.impl.providers.{OAuth2Settings, SocialProviderRegistry}
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{CookieStateSettings, CookieStateProvider}
import com.mohiva.play.silhouette.impl.repositories.DelegableAuthInfoRepository
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import dao.impl.{OAuth2InfoDao, UserDaoImpl}
import play.api.ApplicationLoader.Context
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, MessagesApi}
import play.api.routing.Router
import play.api._
import play.modules.reactivemongo.{DefaultReactiveMongoApi, ReactiveMongoApi}
import com.mohiva.play.silhouette.api.{EventBus, Environment}
import services.UserServiceImpl
import scala.concurrent.ExecutionContext.Implicits.global
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import router._

/**
  * Compile time Dependency Injection
  *
  * Compile time DI is achieved by extending ApplicationLoader and providing your own wiring and
  * instantiation of all your stuff: controllers, routes, services, etc.
  */
class BootstrapLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    /** Force load logback.xml when using compile time DI */
    Logger.configure(context.environment)
    new ApplicationComponents(context).application
  }
}

class ApplicationComponents(context: Context) extends BuiltInComponentsFromContext(context) {

  def reactiveMongoApi: ReactiveMongoApi = new DefaultReactiveMongoApi(
    actorSystem,  configuration, applicationLifecycle
  )

  lazy val messagesApi: MessagesApi = new DefaultMessagesApi(environment, configuration, new DefaultLangs(configuration))

  /** Silhouette */
  lazy val userDao = new UserDaoImpl(reactiveMongoApi)
  lazy val userService = new UserServiceImpl(userDao)
  lazy val authenticatorService = new CookieAuthenticatorService(
    configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator"),
    None,
    new DefaultFingerprintGenerator(false),
    new SecureRandomIDGenerator(),
    Clock()
  )
  lazy val env = Environment[models.User, CookieAuthenticator](
    userService,
    authenticatorService,
    Seq(),
    new EventBus
  )
  lazy val oAuth2StateProvider = new CookieStateProvider(
    configuration.underlying.as[CookieStateSettings]("silhouette.oauth2StateProvider"),
    new SecureRandomIDGenerator(),
    Clock()
  )
  lazy val client = {
    val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder()
    new play.api.libs.ws.ning.NingWSClient(builder.build())
  }
  lazy val googleProvider = new GoogleProvider(
    new PlayHTTPLayer(client),
    oAuth2StateProvider,
    configuration.underlying.as[OAuth2Settings]("silhouette.google")
  )
  lazy val socialProviderRegistry = SocialProviderRegistry(
    Seq(
      googleProvider
        //.withSettings(_.copy(authorizationParams = Map("hd" -> "cloudwick.com")))
    )
  )
  lazy val oauth2InfoDAO = new OAuth2InfoDao
  lazy val authInfoRepository = new DelegableAuthInfoRepository(oauth2InfoDAO)

  /** Inject controllers */
  lazy val applicationController = new controllers.Application(messagesApi, env, socialProviderRegistry)
  lazy val userController = new controllers.UserController(messagesApi, env, userService)
  lazy val socialAuthController = new controllers.SocialAuthController(messagesApi, env, userService, authInfoRepository, socialProviderRegistry)
  lazy val assets = new controllers.Assets(httpErrorHandler)
  lazy val webAssets = new controllers.WebJarAssets(httpErrorHandler, configuration, environment)

  override def router: Router = new Routes(
    httpErrorHandler,
    applicationController,
    socialAuthController,
    userController,
    assets,
    webAssets
  )
}
