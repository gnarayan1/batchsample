package hello;

import java.util.HashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.HsqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;


@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	
	private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Value("${read.chunk.size}")
	private Integer readChunkSize;
	
	@Value("${db.page.fetch.size}")
	private Integer dbFetchSize;
	
	
	// tag::readerwriterprocessor[]
	@Bean
	public FlatFileItemReader<Person> csvReader() {
		FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
		reader.setResource(new ClassPathResource("sample-data.csv"));
		reader.setLineMapper(new DefaultLineMapper<Person>() {
			{
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						setNames(new String[] { "firstName", "lastName" });
					}
				});
				setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {
					{
						setTargetType(Person.class);
					}
				});
			}
		});
		return reader;
	}

	@Bean
	public PersonItemProcessor processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public JdbcBatchItemWriter<Person> dbWriter() {
		JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
		writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
		writer.setDataSource(dataSource);
		return writer;
	}
	// end::readerwriterprocessor[]
	
	
	// tag::readerwriterprocessor2[]
	@Bean
	public JdbcPagingItemReader<Person> dbReader() {
		JdbcPagingItemReader<Person> reader = new JdbcPagingItemReader<Person>();
		reader.setDataSource(dataSource);
		reader.setFetchSize(dbFetchSize);
		reader.setPageSize(dbFetchSize);
		reader.setMaxItemCount(dbFetchSize);
		reader.setQueryProvider(new HsqlPagingQueryProvider(){
			{
			setSelectClause("SELECT first_name, last_name");
	        setFromClause("FROM people");
	        setSortKeys(
	        		new HashMap<String, Order>() {
						private static final long serialVersionUID = 1L;

					{
	        		    put("last_name",Order.ASCENDING);
	        		}});
			}
		});
		reader.setRowMapper(new BeanPropertyRowMapper<Person>(Person.class));
		
		return reader;
	}


	@Bean
	public FlatFileItemWriter<Person> csvWriter() {
		FlatFileItemWriter<Person> writer = new FlatFileItemWriter<Person>();
		writer.setResource(appContext.getResource("file:/tmp/Person.csv"));
		writer.setShouldDeleteIfExists(true);
	
		writer.setLineAggregator(new DelimitedLineAggregator<Person>() {			
			{
				setDelimiter(",");
				setFieldExtractor(new BeanWrapperFieldExtractor<Person>() {
					{
						setNames(new String[] { "firstName", "lastName" });
					}
				});
			}
		});
		return writer;
	}
	// end::readerwriterprocessor2[]
	

	// tag::jobstep[]
	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener, ChunkListener myChunkListener) {
		return jobBuilderFactory.get("importUserJob").incrementer(new RunIdIncrementer()).listener(listener)
				.flow(step1(myChunkListener)).on("COMPLETED").to(step2(myChunkListener)).end().build();
	}

	@Bean
	public Step step1(ChunkListener myChunkListener) {
		log.info("Chunk Size: "+readChunkSize);
		return stepBuilderFactory.get("step1").<Person, Person>chunk(readChunkSize).reader(dbReader()).processor(processor())
				.writer(dbWriter()).listener(myChunkListener).build();
	}
	// end::jobstep[]
	
	
	@Bean
	public Step step2(ChunkListener myChunkListener) {
		return stepBuilderFactory.get("step2").<Person, Person>chunk(1).reader(csvReader()).processor(processor())
				.writer(csvWriter()).listener(myChunkListener).build();
	}
	// end::jobstep[]
	
}