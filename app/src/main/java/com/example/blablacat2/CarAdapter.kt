package com.example.blablacat2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blablacat2.data.model.Car

class CarAdapter(
    private var cars: List<Car>,
    private val onDetailsClick: (Car) -> Unit,
    private val onBookClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    class CarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val carImage: ImageView = view.findViewById(R.id.carImage)
        val carModel: TextView = view.findViewById(R.id.carModel)
        val carBrand: TextView = view.findViewById(R.id.carBrand)
        val carPrice: TextView = view.findViewById(R.id.carPrice)
        val btnDetails: Button = view.findViewById(R.id.btnDetails)
        val btnBook: Button = view.findViewById(R.id.btnBook)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = cars[position]
        holder.carModel.text = car.model
        holder.carBrand.text = car.brand
        holder.carPrice.text = "${car.pricePerDay} ₽/день"

//        Glide.with(holder.itemView.context)
//            .load(car.imageUrl)
//            .into(holder.carImage)

        Glide.with(holder.itemView.context)
            .load(car.imageUrl)
            .placeholder(R.drawable.img) // Опционально: изображение-заглушка
            .error(R.drawable.img) // Опционально: изображение для ошибки
            .into(holder.carImage)

        holder.btnDetails.setOnClickListener { onDetailsClick(car) }
        holder.btnBook.setOnClickListener { onBookClick(car) }
    }

    override fun getItemCount(): Int = cars.size

    fun updateData(newCars: List<Car>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
