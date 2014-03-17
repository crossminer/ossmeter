package org.ossmeter.platform.osgi;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.ossmeter.platform.osgi.services.IWorkerService;
import org.ossmeter.platform.osgi.services.WorkerService;

import com.googlecode.pongo.runtime.PongoFactory;
import com.googlecode.pongo.runtime.osgi.OsgiPongoFactoryContributor;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

public class WorkerServiceApplication implements IApplication, ServiceTrackerCustomizer<IWorkerService, IWorkerService> {
	
	protected boolean done = false;
	protected Object appLock = new Object();
	
	protected Mongo mongo;
	
	protected ServiceTracker<IWorkerService, IWorkerService> workerServiceTracker;
	protected ServiceRegistration<IWorkerService> workerRegistration;
	
	@SuppressWarnings({ "unchecked", "rawtypes", "restriction" })// FIXME !!! (I just hate yellow squiggles...)
	@Override
	public Object start(IApplicationContext context) throws Exception {
		// TODO: Details need to come from a configuration file (perhaps specified in the run config

		// Connect to Mongo - single instance per node -- should take connection details from config
		List<ServerAddress> addrs = new ArrayList<>();
		addrs.add(new ServerAddress("144.32.156.177", 27017));
		addrs.add(new ServerAddress("144.32.156.148", 27017));
		addrs.add(new ServerAddress("144.32.156.184", 27017));
		mongo = new Mongo(addrs);
		
		// Ensure OSGi contributors are active
		PongoFactory.getInstance().getContributors().add(new OsgiPongoFactoryContributor());
		
		// Advertise as being a worker
		Dictionary props = new Properties();
		props.put(IDistributionConstants.SERVICE_EXPORTED_INTERFACES, IDistributionConstants.SERVICE_EXPORTED_INTERFACES_WILDCARD);
		props.put(IDistributionConstants.SERVICE_EXPORTED_CONFIGS, "ecf.generic.server");
		props.put(IDistributionConstants.SERVICE_EXPORTED_CONTAINER_FACTORY_ARGUMENTS, "ecftcp://localhost:3788/worker");
		// FIXME: Understand the above: commenting out the props has no effect (at least locally - maybe a clue to network issue).
		
		// TODO: Pass the service any configuration details it needs
		WorkerService worker = new WorkerService(mongo);
		workerRegistration = Activator.getContext().registerService(IWorkerService.class, worker, props);		
		
		// Detect other workers
		workerServiceTracker = new ServiceTracker<IWorkerService, IWorkerService>(Activator.getContext(), IWorkerService.class, this);	
		workerServiceTracker.open();

		// Now, rest.
		waitForDone();
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		synchronized (appLock) {
			done = true;
			appLock.notifyAll();
			
			// Clean up
			mongo.close();
			workerRegistration.unregister();
			workerServiceTracker.close();
		}	
	}
	
	private void waitForDone() {
		// then just wait here
		synchronized (appLock) {
			while (!done) {
				try {
					appLock.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}


	@Override
	public IWorkerService addingService(ServiceReference<IWorkerService> reference) {
		// TODO: This essentially acts as the master at the moment
		/* Something like:
		if (iAmMaster) {
			worker.setMaster(me);
			addToWOrkers(worker);
		} else {
			keepTrack(worker);
		}
		Need a voting thing too
		*/
		
		System.err.println("Worker registered!");
		IWorkerService worker = Activator.getContext().getService(reference);
		
		List<String> projects = new ArrayList<String>(); 
//		projects.add("modeling.mmt.atl");  // git issue - creates folder but doesn't pull any files????
//		projects.add("modeling.epsilon"); // works
//		projects.add("modeling.gmp.gmf-runtime"); // git issue
//		projects.add("modeling.tmf.xtext"); // git issue
//		projects.add("modeling.viatra2"); // works
//		projects.add("modeling.gmt.amw"); //works
//		projects.add("modeling.mdt.papyrus"); // git issue
//		projects.add("modeling.mdt.modisco"); // works
//		projects.add("modeling.gmp.graphiti"); // works
		
		
		worker.queueProjects(projects);
		
		return worker;
	}


	@Override
	public void modifiedService(ServiceReference<IWorkerService> reference, IWorkerService service) {
		
	}


	@Override
	public void removedService(ServiceReference<IWorkerService> reference, IWorkerService service) {
		
	}

}