package module;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hadatac.console.controllers.sandbox.Sandbox;
import org.hadatac.data.loader.AnnotationWorker;
import org.hadatac.workingfiles.loader.WorkingFilesWorker;

import akka.actor.ActorSystem;
import play.libs.Akka;
import scala.concurrent.duration.FiniteDuration;

@Singleton
public class MyActorSystem {

	@Inject
	private ActorSystem system;

	@Inject
	public MyActorSystem(ActorSystem system) {
		this.system = system;
		schedule();
	}

	public void schedule() {
		// Create thread for auto csv annotation
		Runnable scanning = new Runnable() {
			@Override
			public void run() {
			    AnnotationWorker.scan();
			}
		};
		
		Runnable annotation = new Runnable() {
            @Override
            public void run() {
                AnnotationWorker.autoAnnotate();
            }
        };
        
        Runnable sandbox = new Runnable() {
            @Override
            public void run() {
                Sandbox.checkSandboxExpiration();
            }
        };

        Runnable workingfiles = new Runnable() {
            @Override
            public void run() {
                WorkingFilesWorker.scan();
            }
        };

        system.scheduler().schedule(
                FiniteDuration.create(0, TimeUnit.SECONDS), 
                FiniteDuration.create(5, TimeUnit.SECONDS), 
                scanning, system.dispatcher());
        
		system.scheduler().schedule(
		        FiniteDuration.create(0, TimeUnit.SECONDS), 
                FiniteDuration.create(15, TimeUnit.SECONDS),
                annotation, system.dispatcher());
		
		system.scheduler().schedule(
                FiniteDuration.create(0, TimeUnit.SECONDS), 
                FiniteDuration.create(60, TimeUnit.SECONDS),
                sandbox, system.dispatcher());

		system.scheduler().schedule(
                FiniteDuration.create(0, TimeUnit.SECONDS), 
                FiniteDuration.create(30, TimeUnit.SECONDS),
                workingfiles, system.dispatcher());
	}
}
