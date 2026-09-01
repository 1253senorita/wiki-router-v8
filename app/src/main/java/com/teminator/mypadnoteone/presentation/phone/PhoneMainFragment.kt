package com.teminator.mypadnoteone.presentation.phone

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.teminator.mypadnoteone.R

class PhoneMainFragment : Fragment() {

    private lateinit var tabDialer: LinearLayout
    private lateinit var tabRecent: LinearLayout
    private lateinit var tabContacts: LinearLayout
    private lateinit var tabPlace: LinearLayout

    private lateinit var tvTabDialer: TextView
    private lateinit var tvTabRecent: TextView
    private lateinit var tvTabContacts: TextView
    private lateinit var tvTabPlace: TextView

    private lateinit var indicatorDialer: View
    private lateinit var indicatorRecent: View
    private lateinit var indicatorContacts: View
    private lateinit var indicatorPlace: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_phone_main, container, false)

        // 탭 레이아웃 바인딩
        tabDialer = view.findViewById(R.id.tabDialer)
        tabRecent = view.findViewById(R.id.tabRecent)
        tabContacts = view.findViewById(R.id.tabContacts)
        tabPlace = view.findViewById(R.id.tabPlace)

        // 텍스트뷰 바인딩
        tvTabDialer = view.findViewById(R.id.tvTabDialer)
        tvTabRecent = view.findViewById(R.id.tvTabRecent)
        tvTabContacts = view.findViewById(R.id.tvTabContacts)
        tvTabPlace = view.findViewById(R.id.tvTabPlace)

        // 인디케이터(밑줄) 바인딩
        indicatorDialer = view.findViewById(R.id.indicatorDialer)
        indicatorRecent = view.findViewById(R.id.indicatorRecent)
        indicatorContacts = view.findViewById(R.id.indicatorContacts)
        indicatorPlace = view.findViewById(R.id.indicatorPlace)

        // 초기 화면 (키패드)
        if (savedInstanceState == null) {
            switchTab(0)
        }

        tabDialer.setOnClickListener { switchTab(0) }
        tabRecent.setOnClickListener { switchTab(1) }
        tabContacts.setOnClickListener { switchTab(2) }
        tabPlace.setOnClickListener { switchTab(3) }

        return view
    }

    private fun switchTab(index: Int) {
        val fragment = when (index) {
            0 -> PhoneDialerFragment()
            1 -> PhoneRecentFragment()
            2 -> PhoneContactsFragment()
            3 -> PhonePlaceFragment()
            else -> PhoneDialerFragment()
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.phoneFragmentContainer, fragment)
            .commit()

        updateTabUI(index)
    }

    private fun updateTabUI(selectedIndex: Int) {
        val textViews = listOf(tvTabDialer, tvTabRecent, tvTabContacts, tvTabPlace)
        val indicators = listOf(indicatorDialer, indicatorRecent, indicatorContacts, indicatorPlace)

        for (i in textViews.indices) {
            if (i == selectedIndex) {
                textViews[i].setTextColor(Color.parseColor("#FFFFFF"))
                textViews[i].setTypeface(null, android.graphics.Typeface.BOLD)
                indicators[i].setBackgroundColor(Color.parseColor("#FFFFFF"))
            } else {
                textViews[i].setTextColor(Color.parseColor("#888888"))
                textViews[i].setTypeface(null, android.graphics.Typeface.NORMAL)
                indicators[i].setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }
}