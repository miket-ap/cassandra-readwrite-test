package test.ap.cassandra.readwrite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ColumnFamily;

@Repository
public class OutputRepository {
	
	final Logger logger = LoggerFactory.getLogger(OutputRepository.class);
	
	@Autowired
	private Keyspace keyspace;
	
	@Autowired
	@Qualifier("OutputCF")
	private ColumnFamily<Long, String> CF_OUTPUT;
	
	@Autowired
	@Qualifier("OutputCFColumn")
	private String columnName;
	
	public void saveAmount(long rowKey, float amount) {
		try {
			keyspace.prepareColumnMutation(CF_OUTPUT, rowKey, columnName)
			.putValue(amount, null)
			.execute();
		} catch (ConnectionException e) {
			logger.error("Astyanax connection exception while writing total amount", e);
			//e.printStackTrace();
		}
	}

}
