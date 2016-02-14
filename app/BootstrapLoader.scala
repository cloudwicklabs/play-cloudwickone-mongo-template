import dao.impl.{CounterDaoMongo, UserDaoMongo}
import play.api.ApplicationLoader.Context
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, MessagesApi}
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, Logger}
import play.modules.reactivemongo.{DefaultReactiveMongoApi, ReactiveMongoApi}
import services.UserServiceImpl
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


  /** Inject controllers */
  lazy val applicationController = new controllers.Application()
  lazy val userService = new UserServiceImpl(new UserDaoMongo(reactiveMongoApi, new CounterDaoMongo(reactiveMongoApi)))
  lazy val userController = new controllers.UserController(messagesApi, userService = userService)
  lazy val assets = new controllers.Assets(httpErrorHandler)
  lazy val webAssets = new controllers.WebJarAssets(httpErrorHandler, configuration, environment)

  override def router: Router = new Routes(
    httpErrorHandler,
    applicationController,
    userController,
    assets,
    webAssets
  )
}
