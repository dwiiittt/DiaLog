package com.example.myapplication

import android.util.Log
import kotlin.math.abs

class GeneticOptimizer(
    private val allResep: List<Resep>,
    private val targetKalori: Int,
    private val targetKarbo: Double,
    private val targetSeratMin: Double,
    private val targetSeratMax: Double
) {
    private val populationSize = 30
    private val generations = 70

    fun solve(): List<Resep> {
        var population = List(populationSize) { generateRandomSolution() }

        // Mengubah repeat menjadi for-loop agar kita bisa tahu angka generasinya (1 sampai 70)
        for (generasi in 1..generations) {
            val sortedPop = population.sortedBy { calculateFitness(it) }

            // --- LOGCAT MONITORING ---
            // Karena sortedPop sudah diurutkan dari yang terkecil (terbaik), indeks ke-0 adalah juaranya
            val fitnessTerbaik = calculateFitness(sortedPop[0])
            Log.d("GA_Optimizer", "Generasi Ke-$generasi -> Fitness Terbaik: $fitnessTerbaik")
            // -------------------------

            val nextGen = mutableListOf<List<Resep>>()

            // Elitisme: Amankan 5 terbaik
            nextGen.addAll(sortedPop.take(5))

            // Reproduksi hingga populasi kembali penuh (30)
            while (nextGen.size < populationSize) {
                val parent1 = sortedPop.take(15).random()
                val parent2 = sortedPop.take(15).random()
                val child = crossover(parent1, parent2)
                nextGen.add(mutate(child))
            }
            population = nextGen
        }

        // Mengambil 1 yang terbaik dari generasi terakhir (generasi 70)
        return population.minByOrNull { calculateFitness(it) } ?: emptyList()
    }

    private fun generateRandomSolution(): List<Resep> {
        return listOf(
            allResep.filter { it.kategori == "Sarapan" }.random(),
            allResep.filter { it.kategori == "Makan Siang" }.random(),
            allResep.filter { it.kategori == "Snack" }.random(),
            allResep.filter { it.kategori == "Makan Malam" }.random()
        )
    }

    private fun calculateFitness(solution: List<Resep>): Double {
        val totalKal = solution.sumOf { it.total_kalori }
        val totalKar = solution.sumOf { it.total_karbo }
        val totalSerat = solution.sumOf { it.total_serat ?: 0.0 }

        val diffKal = abs(targetKalori - totalKal)
        val diffKarNew = abs(targetKarbo - totalKar) // Perbaikan: diubah dari diffKar menjadi diffKarNew agar tidak duplikat variabel

        var penaltySerat = 0.0
        if (totalSerat < targetSeratMin) {
            penaltySerat = (targetSeratMin - totalSerat) * 50
        } else if (totalSerat > targetSeratMax) {
            penaltySerat = (totalSerat - targetSeratMax) * 10
        }

        return (diffKal + (diffKarNew * 5) + penaltySerat)
    }

    private fun crossover(p1: List<Resep>, p2: List<Resep>): List<Resep> {
        return listOf(
            if (Math.random() > 0.5) p1[0] else p2[0],
            if (Math.random() > 0.5) p1[1] else p2[1],
            if (Math.random() > 0.5) p1[2] else p2[2],
            if (Math.random() > 0.5) p1[3] else p2[3]
        )
    }

    private fun mutate(solution: List<Resep>): List<Resep> {
        val mutationRate = 0.1
        return solution.map { resep ->
            if (Math.random() < mutationRate) {
                val kandidat = allResep.filter { it.kategori == resep.kategori }
                if (kandidat.isNotEmpty()) kandidat.random() else resep
            } else {
                resep
            }
        }
    }
}