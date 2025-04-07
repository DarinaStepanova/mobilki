import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blablacat2.CarAdapter
import com.example.blablacat2.R
import com.example.blablacat2.data.model.Car

class HomeFragment : Fragment() {

    private lateinit var carRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchInput: EditText
    private lateinit var carAdapter: CarAdapter
    private val viewModel: CarViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        carRecyclerView = view.findViewById(R.id.carRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        searchInput = view.findViewById(R.id.searchInput)

        carAdapter = CarAdapter(emptyList(), { car -> openDetails(car) }, { car -> bookCar(car) })
        carRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        carRecyclerView.adapter = carAdapter

        progressBar.visibility = View.VISIBLE
        viewModel.loadCars { cars ->
            progressBar.visibility = View.GONE
            carAdapter.updateData(cars)
        }

        searchInput.addTextChangedListener { text ->
            viewModel.searchCars(text.toString()) { result ->
                carAdapter.updateData(result)
            }
        }

        return view
    }

    private fun openDetails(car: Car) {
        // Переход на экран деталей
    }

    private fun bookCar(car: Car) {
        // Переход на экран бронирования
    }
}
