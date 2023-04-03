package io.telereso.annotations.client.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.telereso.annotations.client.app.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SampleViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SampleViewModel::class.java]
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel.fetchData()
        viewModel.rocketLauncherState.observe(this) {
            val randomRocketLaunch = it?.getOrNull(Random.nextInt(it.size))

            binding.text.text =
                "\uD83D\uDE80 Total Rockets Launched: ${it?.size ?: "Loading..."} " +
                        "\n\nLast Rocket Mission: ${randomRocketLaunch?.mission_name ?: "Loading..."} " +
                        "\n\nRocket Name: ${randomRocketLaunch?.rocket?.name ?: "Loading..."} \n" +
                        "\n" +
                        "Rocket Type: ${randomRocketLaunch?.rocket?.type ?: "Loading..."} "
        }
    }
}
