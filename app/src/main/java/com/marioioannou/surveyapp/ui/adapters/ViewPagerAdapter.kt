package com.marioioannou.surveyapp.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.marioioannou.surveyapp.data.model.Question
import com.marioioannou.surveyapp.ui.fragments.QuestionFragment

class ViewPagerAdapter(
    fragment: FragmentActivity,
    private val questionsList: List<Question>,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return questionsList.size
    }

    override fun createFragment(position: Int): Fragment {
        val question = questionsList[position]
        return QuestionFragment.newInstance(question)
    }
}