package com.example.mycampuscomp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.mycampuscomp.adapter.OnboardingAdapter
import com.example.mycampuscomp.databinding.ActivityOnboardingBinding
import com.example.mycampuscomp.model.OnboardingItem

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val onboardingItems = listOf(
            OnboardingItem(
                R.drawable.onboarding1,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1)
            ),
            OnboardingItem(
                R.drawable.onboarding2,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2)
            ),
            OnboardingItem(
                R.drawable.onboarding3,
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3)
            )
        )

        adapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = adapter

        binding.buttonNext.setOnClickListener {
            if (binding.viewPager.currentItem < adapter.itemCount - 1) {
                binding.viewPager.currentItem += 1
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        binding.buttonBack.setOnClickListener {
            if (binding.viewPager.currentItem > 0) {
                binding.viewPager.currentItem -= 1
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == 0) {
                    binding.buttonBack.visibility = View.GONE
                } else {
                    binding.buttonBack.visibility = View.VISIBLE
                }

                if (position == adapter.itemCount - 1) {
                    binding.buttonNext.text = getString(R.string.get_started)
                } else {
                    binding.buttonNext.text = getString(R.string.continue_text)
                }
            }
        })
    }
}
