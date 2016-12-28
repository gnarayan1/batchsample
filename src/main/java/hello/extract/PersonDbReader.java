package hello.extract;

import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import hello.Person;

@Component
@EnableBatchProcessing
public class PersonDbReader extends JdbcPagingItemReader<Person>
{
	@Autowired
	public DataSource dataSource;
	
	@Value("${read.chunk.size}")
	private Integer readChunkSize;
	
	@Value("${db.page.fetch.size}")
	private Integer dbFetchSize;
	
	// tag::readerwriterprocessor2[]
	@PostConstruct
	public void init() 
	{
		//JdbcPagingItemReader<Person> reader = new JdbcPagingItemReader<Person>();
		this.setDataSource(dataSource);
		this.setFetchSize(dbFetchSize);
		this.setPageSize(dbFetchSize);
		this.setMaxItemCount(dbFetchSize);
		this.setQueryProvider(new SqlServerPagingQueryProvider(){
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
		this.setRowMapper(new BeanPropertyRowMapper<Person>(Person.class));
		
//		return reader;
	}
}
