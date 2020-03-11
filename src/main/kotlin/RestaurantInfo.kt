data class RestaurantInfo(
    val name: String,
    val contact: String,
    val breakfast: List<MenuInfo>,
    val lunch: List<MenuInfo>,
    val dinner: List<MenuInfo>
)

data class MenuInfo(
    val menuName: String,
    val price: Int
)