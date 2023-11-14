package com.marioioannou.surveyapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.marioioannou.surveyapp.data.model.Question
import com.marioioannou.surveyapp.databinding.FragmentSurveyBinding
import com.marioioannou.surveyapp.ui.activities.MainActivity
import com.marioioannou.surveyapp.ui.adapters.ViewPagerAdapter
import com.marioioannou.surveyapp.ui.viewmodel.SurveyViewModel
import kotlinx.coroutines.launch

class SurveyFragment : Fragment() {

    private lateinit var binding: FragmentSurveyBinding

    private lateinit var questionList: List<Question>

    private lateinit var viewModel: SurveyViewModel

    private val args: SurveyFragmentArgs by navArgs()

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: ViewPagerAdapter

    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSurveyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = binding.viewpagerQuestions

        questionList = args.questions.toList()

        adapter = ViewPagerAdapter(requireActivity(), questionList)

        binding.viewpagerQuestions.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submittedAnswers.collect() { count ->
                    binding.tvQuestionSubmittedNumber.text = count.toString()
                }
            }
        }

        binding.btnBack.setOnClickListener {
            viewModel.clearSubmissionStatus()
            findNavController().popBackStack()
        }

        binding.btnPrevious.setOnClickListener {
            if (currentPage > 0) {
                viewPager.currentItem = currentPage - 1
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentPage < questionList.size - 1) {
                viewPager.currentItem = currentPage + 1
            }
        }

        binding.viewpagerQuestions.apply {
            registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPage = position
                    updatePageCounter()
                    val currentQuestion = questionList[currentPage]
                    viewModel.setCurrentQuestion(currentQuestion)
                }
            })
            isUserInputEnabled = true
        }
        updatePageCounter()
    }

    private fun updatePageCounter() {
        binding.tvQuestionNumber.text = (currentPage + 1).toString()
    }
}