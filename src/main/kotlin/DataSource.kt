import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

class DataSource {
    private val config = HikariConfig()
    private val dataSource = HikariDataSource(config)

    fun getConnection() = dataSource.connection

    init {
        config.jdbcUrl = Constants.JDBC_URL
        config.username = Constants.JDBC_USERNAME
        config.password = Constants.JDBC_PASSWORD
    }
}