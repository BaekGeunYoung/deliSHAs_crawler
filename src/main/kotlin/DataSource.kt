import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class DataSource {
    private var config : HikariConfig = HikariConfig()
    private var dataSource : HikariDataSource

    fun getConnection() = dataSource.connection

    init {
        config.jdbcUrl = Constants.JDBC_URL
        config.driverClassName = Constants.JDBC_DRIVER
        config.username = Constants.JDBC_USERNAME
        config.password = Constants.JDBC_PASSWORD

        dataSource = HikariDataSource(config)
    }
}