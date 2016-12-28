package hello.load;

import javax.annotation.PostConstruct;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import hello.Person;

@Component
@EnableBatchProcessing
public class PersonCsvWriter extends FlatFileItemWriter<Person> 
{
	
	@Autowired
	private ApplicationContext appContext;
	
	@PostConstruct
	public void init() 
	{
//		FlatFileItemWriter<Person> writer = new FlatFileItemWriter<Person>();
		this.setResource(appContext.getResource("file:/tmp/Person.csv"));
		this.setShouldDeleteIfExists(true);
	
		this.setLineAggregator(new DelimitedLineAggregator<Person>() {			
			{
				setDelimiter(",");
				setFieldExtractor(new BeanWrapperFieldExtractor<Person>() {
					{
						setNames(new String[] { "firstName", "lastName" });
					}
				});
			}
		});
//		return writer;
	}
}
