package hello.extract;

import javax.annotation.PostConstruct;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import hello.Person;

@Component
@EnableBatchProcessing
public class PersonCsvReader extends FlatFileItemReader<Person> 
{
	@PostConstruct
	public void init() 
	{
		//FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
		this.setResource(new ClassPathResource("sample-data.csv"));
		this.setLineMapper(new DefaultLineMapper<Person>() {
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
//		return reader;
	}
}
