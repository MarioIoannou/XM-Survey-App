package com.marioioannou.surveyapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.marioioannou.surveyapp.R
import com.marioioannou.surveyapp.data.model.Question
import com.marioioannou.surveyapp.databinding.FragmentQuestionBinding
import com.marioioannou.surveyapp.ui.activities.MainActivity
import com.marioioannou.surveyapp.ui.viewmodel.SurveyViewModel
import com.marioioannou.surveyapp.utils.SubmissionStatus
import kotlinx.coroutines.launch

class QuestionFragment : Fragment() {

    private lateinit var binding: FragmentQuestionBinding

    private lateinit var question: Question

    private lateinit var viewModel: SurveyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateSubmitButtonState()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isSubmitEnabled.collect { isEnabled ->
                    binding.btnSubmitQuestion.isEnabled = isEnabled
                    if (isEnabled) {
                        binding.btnSubmitQuestion.setBackgroundColor(resources.getColor(R.color.blue))
                    } else {
                        binding.btnSubmitQuestion.setBackgroundColor(Color.GRAY)
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submissionStatus.collect { status ->
                    if (viewModel.currentQuestionId.value == question.id && isVisible) {
                        when (status) {
                            is SubmissionStatus.Success -> {
                                viewModel.clearSubmissionStatus()
                                showSnackBar("Success")
                                viewModel.clearSubmissionStatus()
                            }
                            is SubmissionStatus.Failure -> {
                                viewModel.clearSubmissionStatus()
                                showSnackBar(
                                    status.message,
                                    showRetryButton = true
                                )
                                viewModel.clearSubmissionStatus()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        binding.tvQuestion.text = question.question

        binding.etAnswerInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                question.answer = s.toString()
                viewModel.checkSubmittedAnswer(question)
                updateSubmitButtonState()
            }
        })

        binding.btnSubmitQuestion.setOnClickListener {
            viewModel.submitAnswer(question.id, binding.etAnswerInput.text.toString())
        }
    }

    companion object {
        fun newInstance(question: Question): QuestionFragment {
            val fragment = QuestionFragment()
            fragment.question = question
            return fragment
        }
    }

    private fun updateSubmitButtonState() {
        if (question.answer != null) {
            val isAnswerNotEmpty = question.answer.isNotBlank()
            val isNotSubmitted = !question.isSubmitted
            binding.btnSubmitQuestion.isEnabled = isAnswerNotEmpty && isNotSubmitted
        } else {
            binding.btnSubmitQuestion.isEnabled = false
        }
    }

    private fun showSnackBar(message: String, showRetryButton: Boolean = false) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        if (showRetryButton) {
            snackbar.setAction("Retry") {
                viewModel.retrySubmission()
            }
        }
        snackbar.show()
    }
}