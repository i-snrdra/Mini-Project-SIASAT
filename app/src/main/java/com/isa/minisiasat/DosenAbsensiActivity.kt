package com.isa.minisiasat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.isa.minisiasat.databinding.ActivityDosenAbsensiBinding
import com.isa.minisiasat.fragments.AbsensiMatkulFragment
import com.isa.minisiasat.fragments.AbsensiHistoryFragment

class DosenAbsensiActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDosenAbsensiBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenAbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViewPager()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupViewPager() {
        val adapter = AbsensiPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Mata Kuliah"
                1 -> "History"
                else -> "Tab $position"
            }
        }.attach()
    }
    
    private class AbsensiPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        
        override fun getItemCount(): Int = 2
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AbsensiMatkulFragment()
                1 -> AbsensiHistoryFragment()
                else -> AbsensiMatkulFragment()
            }
        }
    }
} 