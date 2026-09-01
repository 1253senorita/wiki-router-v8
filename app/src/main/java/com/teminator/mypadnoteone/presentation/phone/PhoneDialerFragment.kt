package com.teminator.mypadnoteone.presentation.phone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
        val btnCall = view.findViewById<FloatingActionButton>(R.id.btnCall)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)

        // 다이얼 버튼 ID 매핑
        val dialButtons = mapOf(
            R.id.btnNum0 to "0", R.id.btnNum1 to "1", R.id.btnNum2 to "2",
            R.id.btnNum3 to "3", R.id.btnNum4 to "4", R.id.btnNum5 to "5",
            R.id.btnNum6 to "6", R.id.btnNum7 to "7", R.id.btnNum8 to "8",
            R.id.btnNum9 to "9", R.id.btnStar to "*", R.id.btnSharp to "#"
        )

        // 각 버튼에 클릭 리스너 일괄 연결
        for ((id, value) in dialButtons) {
            view.findViewById<View>(id)?.setOnClickListener {
                currentNumber.append(value)
                tvInputNumber.text = currentNumber.toString()
            }
        }

        // 백스페이스(지우기) 버튼 동작
        btnDelete.setOnClickListener {
            if (currentNumber.isNotEmpty()) {
                currentNumber.deleteCharAt(currentNumber.length - 1)
                tvInputNumber.text = currentNumber.toString()
            }
        }

        // 백스페이스 길게 누르면 전체 초기화
        btnDelete.setOnLongClickListener {
            currentNumber.clear()
            tvInputNumber.text = ""
            true
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