package test.ap.cassandra.readwrite;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.recipes.reader.AllRowsReader;

@Repository
public class InputRepository {
	
	final static Logger logger = LoggerFactory.getLogger(InputRepository.class);

	@Autowired
	private Keyspace keyspace;
	
	@Autowired
	@Qualifier("InputCF")
	private ColumnFamily<Long, String> CF_INPUT; 
	
	@Autowired
	private OutputRepository resultRepo;
	
	public boolean readAndProcessInParallel(int pageSize, int concurrency) { //Function<Row<Long,String>,Boolean> callBack

		boolean result = false;
		final AtomicLong rowCount = new AtomicLong(0);
		final long startTime = System.currentTimeMillis();
		
		try {
			result = new AllRowsReader.Builder<Long, String>(keyspace, CF_INPUT)
					.withPageSize(pageSize)
					.withConcurrencyLevel(concurrency)
					.withPartitioner(null)
					.forEachRow(processRow(rowCount, startTime))
					.build()
					.call();
		} catch (Exception e) {
			logger.error("Error reading records in parallel", e);
			//e.printStackTrace();
		}
		
		long endTime = System.currentTimeMillis();
		logger.info(String.format("Processed %d records in %d seconds using %d threads", rowCount.get(), (endTime-startTime)/1000, concurrency));
		
		return result;
		
	}
	
	private Function<Row<Long, String>, Boolean> processRow(final AtomicLong rowCount, final long startTime) {
		return new Function<Row<Long, String>, Boolean>() {
			@Override
			public Boolean apply(@Nullable Row<Long, String> row){
				rowCount.incrementAndGet();
				float total = getAllColumnsTotal(row);
				resultRepo.saveAmount(row.getKey(), total);
				displayProgress(rowCount, startTime);
				return true;
			}
		};
	}
	
	private static float getAllColumnsTotal(Row<Long, String> row) {
		float total = 0;
		for(Column<String> col : row.getColumns()) {
			total = total + col.getFloatValue();
		}
		return total;
	}
	
	private static void displayProgress(AtomicLong rowCount, long startTime) {
		
		//display progress only every 10K records
		if(rowCount.get()%10000==0) {	
			long currentTime = (System.currentTimeMillis() - startTime);
			logger.info(String.format("Processed: %d records @ %d/s", rowCount.get(), rowCount.get()/currentTime*1000));
		}
	}
	
}
