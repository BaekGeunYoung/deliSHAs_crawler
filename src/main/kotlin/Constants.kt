class Constants {
    companion object {
        val BASE_CRAWL_URL: String = System.getenv("BASE_CRAWL_URL") ?: "http://snuco.snu.ac.kr/ko/foodmenu"
        private val JDBC_HOST: String = System.getenv("JDBC_URL") ?: "primavera.clonrz83cm8c.ap-northeast-2.rds.amazonaws.com"
        val JDBC_URL: String = "jdbc:mysql://${this.JDBC_HOST}:3306/BapMukSha?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
        val JDBC_DRIVER: String = System.getenv("JDBC_DRIVER") ?: "com.mysql.cj.jdbc.Driver"
        val JDBC_USERNAME: String = System.getenv("JDBC_USERNAME") ?: "root"
        val JDBC_PASSWORD: String = System.getenv("JDBC_PASSWORD") ?: "Th1ASGuaA0YOWet37W4N"
        val CRAWL_DAYS_UNTIL: Int = System.getenv("CRAWL_DAYS_UNTIL")?.toInt() ?: 3
        val WAS_URL: String = System.getenv("WAS_URL") ?: "http://934d2e64.ngrok.io"
    }
}