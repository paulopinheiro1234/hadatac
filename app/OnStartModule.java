
import com.google.inject.AbstractModule;

public class OnStartModule extends AbstractModule {
    @Override
    public void configure() {
        bind(OnStart.class).asEagerSingleton();
    }
}