package com.example.myapplication

import kotlin.math.abs

class GeneticOptimizer(
    private val allResep: List<Resep>,
    private val targetKalori: Int,
    private val targetKarbo: Double
) {
    private val populationSize = 30 // Ukuran populasi sedikit diperbesar agar lebih variatif
    private val generations = 70

    // Target Serat Baru
    private val targetSeratMin = 25.0
    private val targetSeratMax = 30.0

    fun solve(): List<Resep> {
        var population = List(populationSize) { generateRandomSolution() }

        repeat(generations) {
            val sortedPop = population.sortedBy { calculateFitness(it) }
            val nextGen = mutableListOf<List<Resep>>()

            nextGen.addAll(sortedPop.take(5)) // Elitism

            while (nextGen.size < populationSize) {
                val parent1 = sortedPop.take(15).random()
                val parent2 = sortedPop.take(15).random()
                nextGen.add(crossover(parent1, parent2))
            }
            population = nextGen
        }

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
        val totalSerat = solution.sumOf { it.total_serat ?: 0.0 } // Mengambil data serat

        // Perhitungan selisih Kalori & Karbo
        val diffKal = abs(targetKalori - totalKal)
        val diffKar = abs(targetKarbo - totalKar)

        // LOGIKA BARU: Penalti Serat
        var penaltySerat = 0.0
        if (totalSerat < targetSeratMin) {
            // Jika serat kurang dari 25g, beri penalti besar
            penaltySerat = (targetSeratMin - totalSerat) * 50
        } else if (totalSerat > targetSeratMax) {
            // Jika serat lebih dari 30g, beri penalti ringan (serat lebih tidak seburuk kurang serat)
            penaltySerat = (totalSerat - targetSeratMax) * 10
        }

        // Skor akhir: Semakin kecil nilainya, semakin mendekati target (termasuk serat)
        return (diffKal + (diffKar * 5) + penaltySerat)
    }

    private fun crossover(p1: List<Resep>, p2: List<Resep>): List<Resep> {
        return listOf(
            if (Math.random() > 0.5) p1[0] else p2[0],
            if (Math.random() > 0.5) p1[1] else p2[1],
            if (Math.random() > 0.5) p1[2] else p2[2],
            if (Math.random() > 0.5) p1[3] else p2[3]
        )
    }
}