package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hello.extract.PersonCsvReader;
import hello.extract.PersonDbReader;
import hello.load.PersonCsvWriter;
import hello.load.PersonDbWriter;
import hello.transform.PersonItemProcessor;


@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	
	private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

	@Autowired
	public JobRepository jobRepository;
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	private PersonDbReader personDbReader;

	@Autowired
	private PersonDbWriter personDbWriter;
	
	@Autowired 
	PersonCsvReader personCsvReader;
	
	@Autowired 
	PersonCsvWriter personCsvWriter;
	
	@Autowired
	private PersonItemProcessor personItemProcessor;
	
	@Value("${read.chunk.size}")
	private Integer readChunkSize;
	
	@Bean
	public Job importUserJob(JobExecutionListener listener, ChunkListener myChunkListener) {
		return jobBuilderFactory.get("importUserJob").incrementer(new RunIdIncrementer()).listener(listener)
				.flow(step1(myChunkListener)).on("COMPLETED").to(step2(myChunkListener)).end().build();
	}

	@Bean
	public Step step1(ChunkListener myChunkListener) 
	{
		log.info("Chunk Size: "+readChunkSize);
		return stepBuilderFactory.get("step1").<Person, Person>chunk(readChunkSize).reader(personDbReader).processor(personItemProcessor)
				.writer(personDbWriter).listener(myChunkListener).build();
	}
	
	@Bean
	public Step step2(ChunkListener myChunkListener) 
	{
		return stepBuilderFactory.get("step2").<Person, Person>chunk(1).reader(personCsvReader).processor(personItemProcessor)
				.writer(personCsvWriter).listener(myChunkListener).build();
	}
	
}