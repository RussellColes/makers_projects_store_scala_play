package modules

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import services.DatabaseSeeder

class AppModule(environment: Environment, configuration: Configuration) extends AbstractModule {
//  Configure is called during application initialization
  override def configure(): Unit = {
    bind(classOf[DatabaseSeeder]).asEagerSingleton() // Singleton - only one instance will exist during app lifetime
//    Eager - Create immediately at startup
//    EagerSingleton = Create immediately at startup
  }
}