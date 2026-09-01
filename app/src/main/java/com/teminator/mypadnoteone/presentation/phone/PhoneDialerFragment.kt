package com.teminator.mypadnoteone.presentation.phone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.teminator.mypadnoteone.R

class PhoneDialerFragment : Fragment() {

    private lateinit var tvInputNumber: TextView
    private val currentNumber = StringBuilder()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_phone_dialer, container, false)
        tvInputNumber = view.findViewById(R.id.tvInputNumber)

        val gridLayout = view.findViewById<GridLayout>(R.id.gridLayoutDialer)
        val btnCall = view.findViewById<FloatingActionButton>(R.id.btnCall)

        // 키패드 버튼 클릭 리스너 바인딩 (0~9, *, #)
        val dialValues = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)
            if (child is LinearLayout) {
                val value = dialValues.getOrNull(i) ?: ""
                child.setOnClickListener {
                    currentNumber.append(value)
                    tvInputNumber.text = currentNumber.toString()
                }
            }
        }

        // 통화 버튼 클릭 동작
        btnCall.setOnClickListener {
            val number = currentNumber.toString()
            if (number.isNotEmpty()) {
                Toast.makeText(requireContext(), "$number 번으로 통화 연결 중...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}