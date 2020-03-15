class Constants {
    companion object {
        val BASE_CRAWL_URL = System.getenv("BASE_CRAWL_URL") ?: "http://snuco.snu.ac.kr/ko/foodmenu"
        val JDBC_URL = System.getenv("JDBC_URL") ?: "jdbc:mysql://localhost:3306/test_db?serverTimezone=UTC&characterEncoding=UTF-8"
        val JDBC_DRIVER = System.getenv("JDBC_DRIVER") ?: "com.mysql.cj.jdbc.Driver"
        val JDBC_USERNAME = System.getenv("JDBC_USERNAME") ?: "root"
        val JDBC_PASSWORD = System.getenv("JDBC_PASSWORD") ?: "dkdltm123"
    }
}