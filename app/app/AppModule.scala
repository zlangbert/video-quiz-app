package app

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{CookieStateProvider, CookieStateSettings}
import com.mohiva.play.silhouette.impl.providers.{OAuth2Info, OAuth2Settings, OAuth2StateProvider, SocialProviderRegistry}
import com.mohiva.play.silhouette.impl.repositories.DelegableAuthInfoRepository
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import daos.OAuth2InfoDAO
import models.User
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WSClient
import services._
import services.impl._

class AppModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {

    /* Services */
    bind[UserService].to[UserServiceImpl]
    bind[QuizService].to[QuizServiceImpl]

    /* Silhouette */
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
    bind[DelegableAuthInfoDAO[OAuth2Info]].to[OAuth2InfoDAO]
  }

  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  @Provides
  def provideEnvironment(userService: UserService,
                         authenticatorService: AuthenticatorService[CookieAuthenticator],
                         eventBus: EventBus): Environment[User, CookieAuthenticator] = {
    Environment[User, CookieAuthenticator](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  @Provides
  def provideAuthenticatorService(fingerprintGenerator: FingerprintGenerator,
                                  idGenerator: IDGenerator,
                                  configuration: Configuration,
                                  clock: Clock): AuthenticatorService[CookieAuthenticator] = {
    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    new CookieAuthenticatorService(config, None, fingerprintGenerator, idGenerator, clock)
  }

  @Provides
  def provideSocialProviderRegistry(googleProvider: GoogleProvider): SocialProviderRegistry = {
    SocialProviderRegistry(Seq(
      googleProvider
    ))
  }

  @Provides
  def provideAuthInfoRepository(oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info]): AuthInfoRepository = {
    new DelegableAuthInfoRepository(oauth2InfoDAO)
  }

  @Provides
  def provideOAuth2StateProvider(idGenerator: IDGenerator,
                                 configuration: Configuration,
                                 clock: Clock): OAuth2StateProvider = {
    val settings = configuration.underlying.as[CookieStateSettings]("silhouette.oauth2StateProvider")
    new CookieStateProvider(settings, idGenerator, clock)
  }

  @Provides
  def provideGoogleProvider(httpLayer: HTTPLayer,
                            stateProvider: OAuth2StateProvider,
                            configuration: Configuration): GoogleProvider = {
    new GoogleProvider(httpLayer, stateProvider, configuration.underlying.as[OAuth2Settings]("silhouette.google"))
  }
}
