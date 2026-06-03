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
        // [POKOK SIKLUS] POPULASI AWAL (30 kromosom acak)
        var population = List(populationSize) { generateRandomSolution() }

        for (generasi in 1..generations) {

            // 1. EVALUASI FITNESS
            // Mengurutkan populasi saat ini hanya untuk mengambil juara bertahan sebagai log monitoring
            val sortedCurrentPop = population.sortedBy { calculateFitness(it) }
            val fitnessTerbaik = calculateFitness(sortedCurrentPop[0])
            Log.d("GA_Optimizer", "Generasi Ke-$generasi -> Fitness Terbaik: $fitnessTerbaik")

            // 2. REPRODUKSI: CROSSOVER DAN MUTASI
            // Mengawinkan populasi saat ini secara acak untuk menghasilkan anak-anak baru
            val anakAnak = mutableListOf<List<Resep>>()
            while (anakAnak.size < populationSize) {
                val parent1 = population.random()
                val parent2 = population.random()
                val child = crossover(parent1, parent2)
                anakAnak.add(mutate(child))
            }

            // Menggabungkan Orang Tua (Populasi Lama) + Anak-Anak Baru (Total menjadi 60 kromosom)
            val semuaKandidat = population + anakAnak

            // Urutkan ke-60 kandidat dari yang fitness-nya paling KECIL/Mendekati target (Terbaik)
            val sortedKandidat = semuaKandidat.sortedBy { calculateFitness(it) }

            val nextGen = mutableListOf<List<Resep>>()

            // 4. ELITISM
            // Ambil 5 kromosom terbaik mutlak tanpa seleksi acak
            nextGen.addAll(sortedKandidat.take(5))

            // 3. SELEKSI INDIVIDU
            // Ambil 25 sisanya dari urutan terbaik berikutnya agar total populasi kembali menjadi 30
            nextGen.addAll(sortedKandidat.drop(5).take(25))

            // 5. POPULASI BARU
            // Overwrite populasi lama dengan nextGen untuk dievaluasi pada iterasi/generasi berikutnya
            population = nextGen
        }

        // Akhir dari 70 Generasi: Ambil 1 kombinasi menu terbaik mutlak
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
        val diffKarNew = abs(targetKarbo - totalKar)

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