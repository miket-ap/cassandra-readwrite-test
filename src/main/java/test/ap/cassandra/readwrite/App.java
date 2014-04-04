package test.ap.cassandra.readwrite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class App {
	
	final static Logger logger = LoggerFactory.getLogger(App.class);
	
	@Autowired
	private InputRepository inputRepo;
	
	@Autowired
	private OutputRepository outputRepo;
	
	public static void main(String[] args) {
		logger.info("Initializing...");
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.scan("test.ap.cassandra.readwrite");
		ctx.refresh();
		App app = ctx.getBean(App.class);
		if(validParams(args)) {
			app.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		}
		ctx.close();
		
	}
	
	private static boolean validParams(String[] args)  {
		boolean result = args.length >= 2;
		System.out.println("");
		System.out.println("Missing parameters. Usage: app <page_size> <number_of_threads>");
		return result;
	}
	
	private void start(int pageSize, int threads) {
		logger.info("App started!");
		inputRepo.readAndProcessInParallel(pageSize, threads);		
	}
}
