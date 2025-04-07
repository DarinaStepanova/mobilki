import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blablacat2.data.database.CarDatabase
import com.example.blablacat2.data.model.Car
import kotlinx.coroutines.launch

class CarViewModel(application: Application) : AndroidViewModel(application) {
    private val carDao = CarDatabase.getDatabase(application).carDao()

    val cars = mutableListOf<Car>()

    init {
        preloadData() // Загружаем тестовые данные при создании ViewModel
    }

    private fun preloadData() {
        viewModelScope.launch {
            if (carDao.getAllCars().isEmpty()) {
                val sampleCars = listOf(
                    Car(brand = "Toyota", model = "Camry", pricePerDay = 3000.0, imageUrl = "https://swiss-limousine-service.com/wp-content/uploads/2022/12/S-class.png"),
                    Car(brand = "BMW", model = "X5", pricePerDay = 7000.0, imageUrl = "https://swiss-limousine-service.com/wp-content/uploads/2022/12/S-class.png")
                )
                carDao.insertAll(sampleCars)
            }
        }
    }

    fun loadCars(onResult: (List<Car>) -> Unit) {
        viewModelScope.launch {
            val carList = carDao.getAllCars()
            cars.clear()
            cars.addAll(carList)
            onResult(carList)
        }
    }

    fun searchCars(brand: String, onResult: (List<Car>) -> Unit) {
        viewModelScope.launch {
            val result = if (brand.isEmpty()) cars else carDao.searchCars("%$brand%")
            onResult(result)
        }
    }
}
