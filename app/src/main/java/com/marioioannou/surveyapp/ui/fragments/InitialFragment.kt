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
import com.google.android.material.snackbar.Snackbar
import com.marioioannou.surveyapp.data.model.Question
import com.marioioannou.surveyapp.databinding.FragmentInitialBinding
import com.marioioannou.surveyapp.ui.activities.MainActivity
import com.marioioannou.surveyapp.ui.viewmodel.SurveyViewModel
import com.marioioannou.surveyapp.utils.ScreenState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InitialFragment : Fragment() {

    private lateinit var binding: FragmentInitialBinding

    private lateinit var viewModel: SurveyViewModel

    private var questionsList: List<Question>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentInitialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getQuestions()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.surveyQuestions.collectLatest { state ->
                    when (state) {
                        is ScreenState.Loading -> {
                            binding.pbInitialFrag.visibility = View.VISIBLE
                            binding.btnStartSurvey.isEnabled = false
                        }
                        is ScreenState.Success -> {
                            questionsList = state.data
                            binding.pbInitialFrag.visibility = View.GONE
                            binding.btnStartSurvey.isEnabled = true
                        }
                        is ScreenState.Error -> {
                            binding.btnStartSurvey.isEnabled = false
                            Snackbar.make(view, "Something went wrong", Snackbar.LENGTH_LONG)
                                .setAction("Retry") {
                                    viewModel.getQuestions()
                                }.show()
                        }
                    }
                }
            }
        }

        binding.btnStartSurvey.setOnClickListener {
            viewModel.resetSurvey()
            val action = InitialFragmentDirections.actionInitialFragmentToSurveyFragment(
                questionsList?.toTypedArray() ?: emptyArray()
            )
            findNavController().navigate(action)
        }
    }
}