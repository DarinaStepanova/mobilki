package com.example.blablacat2.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.blablacat2.data.model.Car

@Dao
interface CarDao {
    @Insert
    suspend fun insertCar(car: Car)

    @Insert
    suspend fun insertAll(cars: List<Car>)

    @Query("SELECT * FROM cars")
    suspend fun getAllCars(): List<Car>

    @Query("SELECT * FROM cars WHERE brand LIKE :brand")
    suspend fun searchCars(brand: String): List<Car>
}
