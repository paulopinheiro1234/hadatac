package module;

import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.providers.openid.OpenIdAuthProvider;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import org.hadatac.console.providers.MyStupidBasicAuthProvider;
import org.hadatac.console.providers.MyUsernamePasswordAuthProvider;
import scala.collection.Seq;
import org.hadatac.console.service.MyResolver;
import org.hadatac.console.service.MyUserService;

/**
 * Initial DI module.
 */
public class MyModule extends Module {
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(
                bind(Resolver.class).to(MyResolver.class)
                //bind(MyUserService.class).toSelf().eagerly()
                //bind(OpenIdAuthProvider.class).toSelf().eagerly()
                //bind(MyStupidBasicAuthProvider.class).toSelf().eagerly()
                //bind(MyUsernamePasswordAuthProvider.class).toSelf().eagerly()
        );
    }
}
