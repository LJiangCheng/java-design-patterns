JdbcTemplate
------------

## 传统JDBC编程回顾

```java
//如下，可见重复代码之多
class Demo{
    public int insert(){
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8&useSSL=true";
        String username = "root";
        String password = "1234";
        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
            String sql = "insert into users(nickname,comment,age) values('小小谭','I love three thousand times', '21')";
            statement = connection.createStatement();
            int i = statement.executeUpdate(sql);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != statement) {
                    statement.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
    }
    
    public void query(){
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8&useSSL=true";
        String username = "root";
        String password = "1234";
        Connection connection = null;
        Statement statement = null;
        try{
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
            String sql = "select nickname,comment,age from users";
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            List<Users> usersList = new ArrayList<>();
            while (resultSet.next()) {
                Users users = new Users();
                users.setNickname(resultSet.getString(1));
                users.setComment(resultSet.getString(2));
                users.setAge(resultSet.getInt(3));
                usersList.add(users);
            }
            return usersList;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != statement) {
                    statement.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;

    }
}
```

## JdbcTemplate提供了什么

见名知义，JdbcTemplate简化了JDBC开发，而且很可能大量应用了模板方法。那么具体提供了哪些东西？

1. 平台无关的异常处理机制
   * 原生JDBC开发中几乎所有的操作代码都需要我们强制捕获异常，但是在出现异常时我们往往无法通过异常读懂错误。
   * Spring解决了我们的问题它提供了多个数据访问异常，并且分别描述了他们抛出时对应的问题，同时对异常进行了包装不强制要求我们进行捕获

2. 数据访问的模板化以及通过回调提供的可变部分定制化
   * 固定部分
     * 事务控制、资源的获取关闭以及异常处理等
   * 可变部分
     * 结果集的处理实体的绑定，参数的绑定等
   * Spring将数据访问过程中固定部分和可变部分划分为了两个不同的类(Template)和回调(Callback),模板处理过程中不变的部分，回调处理自定义的访问代码

## 模板方法 + 回调在JdbcTemplate中的应用

> 传统的模板方法模式是基类定义了一系列步骤用于实现一个功能，使用者根据自己的需要去<em>继承<em>这个基类并实现或覆写其中一部分步骤
> 上述模式的问题在于，通过继承的方式，每个子类都同时继承了基类中的所有抽象方法，当基类中可定制的节点较多时这种方式并不灵活
> 模板方法 + 回调可以解决这个问题：
> 在方法参数中传递一个接口，父类在调用此方法时，必须调用方法中传递的接口实现类对象的对应方法
> 这种方式事实上是<font color="red">将传统实现中的抽象方法转移到了额外的接口中，每个接口只需要负责定义一个节点，</font>然后在模板类中调用这个接口的实现以达成同样的目的

```java
    /**
    * JdbcTemplate继承了JdbcAccessor实现了JdbcOperations
    * JdbcAccessor主要封装了处理数据源DataSource和sql的异常检验操作，
    * JdbcOperations则定义了具体的查询query/update/batchUpdate等操作
    */
    public class JdbcTemplate extends JdbcAccessor implements JdbcOperations {
        public JdbcTemplate() {
        }
        //调用父类方法设置数据源和其他参数
        public JdbcTemplate(DataSource dataSource) {
            this.setDataSource(dataSource);
            this.afterPropertiesSet();
        }
        //调用父类方法设置数据源，懒加载策略和其他参数
        public JdbcTemplate(DataSource dataSource, boolean lazyInit) {
            this.setDataSource(dataSource);
            this.setLazyInit(lazyInit);
            this.afterPropertiesSet();
        }
        
        /**
        * JdbcOperations接口中定义的execute方法的实现
        * 这是一个典型的模板方法 + 回调模式：
        * 我们不用再写过多的重复代码，只需要通过回调实现对结果的处理就好（StatementCallback）
        * 事实上，JdbcTemplate帮我们把通用的回调也实现了，通过下面的query方法
        */
        @Nullable
        public <T> T execute(StatementCallback<T> action) throws DataAccessException {
            //参数检查
            Assert.notNull(action, "Callback object must not be null");
            //获取连接
            Connection con = DataSourceUtils.getConnection(this.obtainDataSource());
            Statement stmt = null;
            Object var11;
            try {
                //创建一个Statement
                stmt = con.createStatement();
                //设置查询超时时间，最大行等参数（就是一开始那些成员变量）
                this.applyStatementSettings(stmt);
                //执行回调方法获取结果集
                T result = action.doInStatement(stmt);
                //处理警告
                this.handleWarnings(stmt);
                var11 = result;
            } catch (SQLException var9) {
                //出现错误优雅退出
                String sql = getSql(action);
                JdbcUtils.closeStatement(stmt);
                stmt = null;
                DataSourceUtils.releaseConnection(con, this.getDataSource());
                con = null;
                throw this.translateException("StatementCallback", sql, var9);
            } finally {
                JdbcUtils.closeStatement(stmt);
                DataSourceUtils.releaseConnection(con, this.getDataSource());
            }
            return var11;
        }
        
        /**
        * 模拟查询
        */
        public List<Users> findAll() {
            JdbcTemplate jdbcTemplate = DataSourceConfig.getTemplate();
            String sql = "select nickname,comment,age from users";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<Users>(Users.class));
        }
        
        /**
        * query(驱动) --> query(实现回调并交给execute使用) --> execute(执行查询和使用回调) 
        */
        public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
            return (List)result(this.query((String)sql, (ResultSetExtractor)(new RowMapperResultSetExtractor(rowMapper))));
        }

        @Nullable
        public <T> T query(final String sql, final ResultSetExtractor<T> rse) throws DataAccessException {
            Assert.notNull(sql, "SQL must not be null");
            Assert.notNull(rse, "ResultSetExtractor must not be null");
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Executing SQL query [" + sql + "]");
            }
            //实现回调接口
            class QueryStatementCallback implements StatementCallback<T>, SqlProvider {
                QueryStatementCallback() {
                }
                @Nullable
                public T doInStatement(Statement stmt) throws SQLException {
                    ResultSet rs = null;
                    Object var3;
                    try {
                        //这里真正的执行我们的sql语句
                        rs = stmt.executeQuery(sql);
                        //处理对象映射
                        var3 = rse.extractData(rs);
                    } finally {
                        JdbcUtils.closeResultSet(rs);
                    }
                    return var3;
                }
                public String getSql() {
                    return sql;
                }
            }
            //调用execute接口
            return this.execute((StatementCallback)(new QueryStatementCallback()));
        }

    }
    
    /**
    * 首先看抽象类JdbcAccessor：
    * 这里面封装了JDBC的数据源操作，处理过程中不变的部分
    */
    public abstract class JdbcAccessor implements InitializingBean {
        protected final Log logger = LogFactory.getLog(this.getClass());
        //数据源
        @Nullable
        private DataSource dataSource;
        //异常翻译
        @Nullable
        private volatile SQLExceptionTranslator exceptionTranslator;
        //懒加载策略
        private boolean lazyInit = true;
        public JdbcAccessor() {
        }
        public void setDataSource(@Nullable DataSource dataSource) {
            this.dataSource = dataSource;
        }
        @Nullable
        public DataSource getDataSource() {
            return this.dataSource;
        }
        protected DataSource obtainDataSource() {
            DataSource dataSource = this.getDataSource();
            Assert.state(dataSource != null, "No DataSource set");
            return dataSource;
        }
        public void setDatabaseProductName(String dbName) {
            this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dbName);
        }
        public void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
            this.exceptionTranslator = exceptionTranslator;
        }
        /**
        * 单例模式获取SQLExceptionTranslator
        * 这正是JdbcTemplate封装的异常处理类
        */
        public SQLExceptionTranslator getExceptionTranslator() {
            SQLExceptionTranslator exceptionTranslator = this.exceptionTranslator;
            if (exceptionTranslator != null) {
                return exceptionTranslator;
            } else {
                synchronized(this) {
                    SQLExceptionTranslator exceptionTranslator = this.exceptionTranslator;
                    if (exceptionTranslator == null) {
                        DataSource dataSource = this.getDataSource();
                        if (dataSource != null) {
                            exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
                        } else {
                            exceptionTranslator = new SQLStateSQLExceptionTranslator();
                        }
                        this.exceptionTranslator = exceptionTranslator;
                    }
                    return exceptionTranslator;
                }
            }
        }
    }

    /**
    * JdbcOperations接口，这里定义了JDBC的差异化操作
    * 看其中定义的一个用于执行SQL的方法，参数为一个函数式回调接口
    */
    public interface JdbcOperations {
        @Nullable
        <T> T execute(StatementCallback<T> var1) throws DataAccessException;
    }
    
    /**
    * 回调接口，用于定制自己的SQL操作，如结果集封装，参数绑定等
    * 适用于函数式编程
    */
    @FunctionalInterface
    public interface StatementCallback<T> {
        @Nullable
        T doInStatement(Statement var1) throws SQLException, DataAccessException;
    }

    
```



























