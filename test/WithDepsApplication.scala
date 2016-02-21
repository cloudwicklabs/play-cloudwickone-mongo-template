package test

import app.BootstrapLoader
import play.api.test.WithApplicationLoader

class WithDepsApplication extends WithApplicationLoader(new BootstrapLoader)