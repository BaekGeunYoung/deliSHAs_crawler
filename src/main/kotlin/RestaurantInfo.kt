import java.time.LocalDate

data class RestaurantInfo(
    val name: String?,
    val contact: String?
)

data class MenuInfo(
    val name: String?,
    val price: Int?,
    var msg: String?,
    var isValid: Boolean = true
)

data class Menu(
    val restaurantName: String,
    val time: Time,
    val date: LocalDate,
    val name: String?,
    val price: Int?,
    val msg: String?,
    val isValid: Boolean
) {
    companion object {
        fun of(restaurantName: String, menuInfo: MenuInfo, time: Time, date: LocalDate): Menu {
            return Menu(
                restaurantName = restaurantName,
                time = time,
                date = date,
                name = menuInfo.name,
                price = menuInfo.price,
                msg = menuInfo.msg,
                isValid = menuInfo.isValid
            )
        }
    }
}

enum class Time {
    BREAKFAST,
    LUNCH,
    DINNER
}