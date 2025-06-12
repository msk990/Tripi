package com.example.tripi.ui.quests

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tripi.databinding.FragmentQuestDetailBinding
import androidx.navigation.fragment.navArgs
import com.example.tripi.R

class QuestDetailFragment : Fragment() {

    private var _binding: FragmentQuestDetailBinding? = null
    private val binding get() = _binding!!

    private val args: QuestDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val questId = args.questId
        val quest = QuestRepository.loadQuests(requireContext())
            .find { it.id == questId }

        binding.questTitle.text = quest?.title ?: "Unknown Quest"
        binding.questDescription.text = quest?.description ?: "No description available."

        quest?.image?.let { imageFileName ->
            try {
                val inputStream = requireContext().assets.open("quests/$imageFileName")
                val fullBitmap = BitmapFactory.decodeStream(inputStream)

                // Dynamically calculate 40% of screen width
                val displayMetrics = resources.displayMetrics
                val targetSize = (displayMetrics.widthPixels * 0.4).toInt()

                // Crop a square from the top-center
                val cropped = cropTopCenterSquare(fullBitmap, targetSize)

                // Set the image
                binding.questCroppedImage.setImageBitmap(cropped)

                // Resize the ImageView to match the cropped size
                binding.questCroppedImage.layoutParams = binding.questCroppedImage.layoutParams.apply {
                    width = targetSize
                    height = targetSize
                }

            } catch (e: Exception) {
                e.printStackTrace()

                 binding.questCroppedImage.setImageResource(R.drawable.placeholder_image)
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
private fun cropTopCenterSquare(bitmap: Bitmap, size: Int): Bitmap {
    val x = (bitmap.width - size) / 2
    val y = 0
    return Bitmap.createBitmap(bitmap, x, y, size, size)
}
