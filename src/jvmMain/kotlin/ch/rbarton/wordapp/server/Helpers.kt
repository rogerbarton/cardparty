package ch.rbarton.wordapp.server

import ch.rbarton.wordapp.common.data.colors
import kotlin.random.Random
import kotlin.random.nextInt

fun getUnusedId(allIds: Collection<Int>, usedIds: Collection<Int>): Int
{
    val unusedColors = allIds.toMutableList()
    unusedColors.removeAll(usedIds)
    return unusedColors.shuffled().getOrElse(0) { Random.nextInt(IntRange(0, colors.size - 1)) }
}