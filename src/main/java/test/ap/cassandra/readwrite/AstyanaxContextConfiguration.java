package test.ap.cassandra.readwrite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.LongSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

@Configuration
@PropertySource(value={"file:app.properties"})
public class AstyanaxContextConfiguration {
	
	final Logger logger = LoggerFactory.getLogger(AstyanaxContextConfiguration.class);

	@Value("${cassandra.cluster-name}")
	private String clusterName;
	
	@Value("${cassandra.keyspace-name}")
	private String keyspaceName;
	
	@Value("${cassandra.connection-pool.name}")
	private String connectionPoolName;
	
	@Value("${cassandra.connection-pool.port}")
	private int connectionPoolPort;

	@Value("${cassandra.connection-pool.max-conns-per-host}")
	private int connectionPoolMaxConnsPerHost;	
	
	@Value("${cassandra.connection-pool.seeds}")
	private String connectionPoolSeeds;	
	
	@Value("${cassandra.cf.input}")
	private String cfInput;
	
	@Value("${cassandra.cf.output}")
	private String cfOutput;	
	
	@Value("${cassandra.cf.output.column}")
	private String cfOutputColumn;
	
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
       PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
       configurer.setIgnoreResourceNotFound(true);
	   return configurer;
    }	
	
    @Bean(name="InputCF")
    public ColumnFamily<Long, String> getInputCF() {
    	return new ColumnFamily<Long, String> (
				cfInput,
				LongSerializer.get(),
				StringSerializer.get());
    }
    
    @Bean(name="OutputCF")
    public ColumnFamily<Long, String> getOutputCF() {
    	return new ColumnFamily<Long, String> (
				cfOutput,
				LongSerializer.get(),
				StringSerializer.get());
    }
    
    @Bean(name="OutputCFColumn")
    public String getOutputCFColumn() {
    	return cfOutputColumn;
    }
    
	@Bean(initMethod="start", destroyMethod="shutdown")
	//@Scope(BeanDefinition.SCOPE_SINGLETON) //Singleton is the default anyway
	public AstyanaxContext<Keyspace> getAstyanaxContext() {
		AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
			.forCluster(clusterName)
			.forKeyspace(keyspaceName)
			.withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
				.setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
			)
			.withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl(connectionPoolName)
				.setPort(connectionPoolPort)
				.setMaxConnsPerHost(connectionPoolMaxConnsPerHost)
				.setSeeds(connectionPoolSeeds)
			)
			.withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
			.buildKeyspace(ThriftFamilyFactory.getInstance());
		
		logger.info(String.format("Creating AstyanaxContext with Cluster: %s and Keyspace: %s", clusterName, keyspaceName));
		
		return context;
	}
	
	@Bean
	public Keyspace getKeyspace() {
		logger.info("Creating Keyspace");
		return getAstyanaxContext().getClient();
	}
	
}
