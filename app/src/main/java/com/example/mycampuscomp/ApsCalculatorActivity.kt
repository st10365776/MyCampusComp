package com.example.mycampuscomp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.adapter.ApsModuleAdapter
import com.example.mycampuscomp.model.ApsModule
import com.example.mycampuscomp.repository.UserRepository
import com.example.mycampuscomp.utils.NetworkUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApsCalculatorActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var layoutOfflineBanner: LinearLayout
    private lateinit var spinnerGradingSystem: Spinner
    private lateinit var rvModules: RecyclerView
    private lateinit var btnAddModule: MaterialButton
    private lateinit var btnCalculate: MaterialButton
    private lateinit var cardResult: MaterialCardView
    private lateinit var tvComputedAps: TextView
    private lateinit var tvApsSummary: TextView
    private lateinit var btnSaveAps: MaterialButton

    private val modulesList = mutableListOf<ApsModule>()
    private lateinit var adapter: ApsModuleAdapter
    private lateinit var userRepository: UserRepository

    private var calculatedApsInt: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aps_calculator)

        userRepository = UserRepository(context = applicationContext)

        initViews()
        setupSpinner()
        setupRecyclerView()
        observeNetwork()

        // Populate initial 3 default empty modules
        if (modulesList.isEmpty()) {
            modulesList.add(ApsModule(name = "Module 1", markText = "", creditsText = "16"))
            modulesList.add(ApsModule(name = "Module 2", markText = "", creditsText = "16"))
            modulesList.add(ApsModule(name = "Module 3", markText = "", creditsText = "16"))
            adapter.notifyDataSetChanged()
        }

        btnBack.setOnClickListener { onBackPressed() }

        btnAddModule.setOnClickListener {
            val newNum = modulesList.size + 1
            modulesList.add(ApsModule(name = "Module $newNum", markText = "", creditsText = "16"))
            adapter.notifyItemInserted(modulesList.size - 1)
        }

        btnCalculate.setOnClickListener {
            calculateAps()
        }

        btnSaveAps.setOnClickListener {
            saveApsToProfile()
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBackAps)
        layoutOfflineBanner = findViewById(R.id.layoutOfflineBannerAps)
        spinnerGradingSystem = findViewById(R.id.spinnerGradingSystem)
        rvModules = findViewById(R.id.rvApsModules)
        btnAddModule = findViewById(R.id.btnAddModule)
        btnCalculate = findViewById(R.id.btnCalculateAps)
        cardResult = findViewById(R.id.cardApsResult)
        tvComputedAps = findViewById(R.id.tvComputedAps)
        tvApsSummary = findViewById(R.id.tvApsSummary)
        btnSaveAps = findViewById(R.id.btnSaveApsToAccount)
    }

    private fun setupSpinner() {
        val gradingSystems = arrayOf(
            "South African NSC / University Scale (1-7 Per Module)",
            "Credit-Weighted Average Percentage (%)",
            "GPA Scale (4.0 Scale)"
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gradingSystems)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGradingSystem.adapter = spinnerAdapter

        spinnerGradingSystem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                cardResult.visibility = View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = ApsModuleAdapter(modulesList) { position ->
            if (modulesList.size <= 1) {
                Toast.makeText(this, "At least one module is required", Toast.LENGTH_SHORT).show()
                return@ApsModuleAdapter
            }
            modulesList.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, modulesList.size)
        }
        rvModules.layoutManager = LinearLayoutManager(this)
        rvModules.adapter = adapter
    }

    private fun observeNetwork() {
        NetworkUtils.NetworkStateLiveData(this).observe(this) { isConnected ->
            layoutOfflineBanner.visibility = if (isConnected) View.GONE else View.VISIBLE
        }
    }

    private fun calculateAps() {
        if (modulesList.isEmpty()) {
            Toast.makeText(this, "Please add at least one module", Toast.LENGTH_SHORT).show()
            return
        }

        var hasValidationError = false
        var firstErrorMsg = ""

        for (i in modulesList.indices) {
            val mod = modulesList[i]
            val viewHolder = rvModules.findViewHolderForAdapterPosition(i) as? ApsModuleAdapter.ModuleViewHolder

            val name = mod.name.trim()
            val markDouble = mod.markText.toDoubleOrNull()
            val creditsDouble = mod.creditsText.toDoubleOrNull()

            if (name.isEmpty()) {
                hasValidationError = true
                viewHolder?.tilName?.error = "Module name required"
                if (firstErrorMsg.isEmpty()) firstErrorMsg = "Module #${i + 1} name is missing"
            }

            if (markDouble == null) {
                hasValidationError = true
                viewHolder?.tilMark?.error = "Enter valid mark (0-100)"
                if (firstErrorMsg.isEmpty()) firstErrorMsg = "Module #${i + 1} mark is invalid or missing"
            } else if (markDouble < 0.0 || markDouble > 100.0) {
                hasValidationError = true
                viewHolder?.tilMark?.error = "Mark must be between 0 and 100"
                if (firstErrorMsg.isEmpty()) firstErrorMsg = "Module #${i + 1} mark must be between 0 and 100"
            }

            if (creditsDouble == null || creditsDouble <= 0.0) {
                hasValidationError = true
                viewHolder?.tilCredits?.error = "Enter valid credits (>0)"
                if (firstErrorMsg.isEmpty()) firstErrorMsg = "Module #${i + 1} credits value is invalid or missing"
            }
        }

        if (hasValidationError) {
            cardResult.visibility = View.GONE
            Toast.makeText(this, "Validation Failed: $firstErrorMsg", Toast.LENGTH_LONG).show()
            return
        }

        val selectedSystemPosition = spinnerGradingSystem.selectedItemPosition

        when (selectedSystemPosition) {
            0 -> {
                // South African NSC (1-7 scale)
                var totalPoints = 0
                for (mod in modulesList) {
                    val mark = mod.markText.toDouble()
                    val pts = when {
                        mark >= 80 -> 7
                        mark >= 70 -> 6
                        mark >= 60 -> 5
                        mark >= 50 -> 4
                        mark >= 40 -> 3
                        mark >= 30 -> 2
                        else -> 1
                    }
                    totalPoints += pts
                }
                calculatedApsInt = totalPoints
                tvComputedAps.text = "$totalPoints APS Points"
                tvApsSummary.text = "NSC 1-7 Scale across ${modulesList.size} modules"
            }
            1 -> {
                // Credit-Weighted Average %
                var totalWeightedMarks = 0.0
                var totalCredits = 0.0
                for (mod in modulesList) {
                    val mark = mod.markText.toDouble()
                    val credits = mod.creditsText.toDouble()
                    totalWeightedMarks += (mark * credits)
                    totalCredits += credits
                }
                val avg = if (totalCredits > 0) totalWeightedMarks / totalCredits else 0.0
                calculatedApsInt = avg.toInt()
                tvComputedAps.text = String.format("%.1f%%", avg)
                tvApsSummary.text = "Weighted Average across ${modulesList.size} modules (${totalCredits.toInt()} total credits)"
            }
            2 -> {
                // GPA 4.0 Scale
                var totalWeightedGpa = 0.0
                var totalCredits = 0.0
                for (mod in modulesList) {
                    val mark = mod.markText.toDouble()
                    val credits = mod.creditsText.toDouble()
                    val gpaPoint = when {
                        mark >= 80 -> 4.0
                        mark >= 70 -> 3.0
                        mark >= 60 -> 2.0
                        mark >= 50 -> 1.0
                        else -> 0.0
                    }
                    totalWeightedGpa += (gpaPoint * credits)
                    totalCredits += credits
                }
                val gpa = if (totalCredits > 0) totalWeightedGpa / totalCredits else 0.0
                calculatedApsInt = (gpa * 10).toInt() // Normalized for score field
                tvComputedAps.text = String.format("%.2f GPA", gpa)
                tvApsSummary.text = "4.0 GPA Scale across ${modulesList.size} modules"
            }
        }

        cardResult.visibility = View.VISIBLE
        Toast.makeText(this, "APS Calculated successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun saveApsToProfile() {
        btnSaveAps.isEnabled = false
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.updateApsScore(calculatedApsInt)
            withContext(Dispatchers.Main) {
                btnSaveAps.isEnabled = true
                Toast.makeText(this@ApsCalculatorActivity, "APS Score & Gamification points saved to profile!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
