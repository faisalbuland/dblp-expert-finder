package edu.ucdavis.cs.dblp.service.aop;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class PerformanceMonitor {
	private static final Logger logger = Logger.getLogger(PerformanceMonitor.class);
	
	@Around("execution(public * edu.ucdavis.cs.dblp.experts.SolrSearchService.fullTextSearch(..))")
	public Object monitorExecTime(ProceedingJoinPoint pjp) throws Throwable{
		StopWatch timer = new StopWatch();
		timer.start();
		try {
			return pjp.proceed();
		} finally {
			timer.stop();
			logger.info("exec time:"+timer.getTime()+" msecs");
		}
	}
}
