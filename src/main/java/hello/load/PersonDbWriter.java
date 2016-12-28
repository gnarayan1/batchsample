package hello.load;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hello.Person;

@Component
@EnableBatchProcessing
public class PersonDbWriter extends JdbcBatchItemWriter<Person>
{
	@Autowired
	public DataSource dataSource;
	
	@PostConstruct
	public void init() 
	{
		//JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
		this.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
		this.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
		this.setDataSource(dataSource);
	}
	// end::readerwriterprocessor[]
	
}
