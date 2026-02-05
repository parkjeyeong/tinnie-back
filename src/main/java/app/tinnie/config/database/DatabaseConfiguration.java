package app.tinnie.config.database;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@MapperScan(basePackages = {"app.tinnie.mapper"})
public class DatabaseConfiguration {
  @Bean
  public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
    sessionFactory.setDataSource(dataSource);

    sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
        .getResources("classpath:mappers/**/*.xml"));

    return sessionFactory.getObject();
  }
}
