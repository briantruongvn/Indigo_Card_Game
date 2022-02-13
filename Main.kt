package indigo

class Card {
    companion object {
        val ranks = listOf("K", "Q", "J", "10", "9", "8", "7", "6", "5", "4", "3", "2", "A")
        private val suits = listOf("♣", "♦", "♥", "♠")
        var cards = mutableListOf<String>()

        fun createCard() {
            for (s in suits) {
                for (r in ranks) {
                    cards.add("$r$s")
                }
            }
        }
    }

    init {
        createCard()
    }

    fun getCard() = cards

    fun get() {
        println("Number of cards:")
        try {
            val res = readLine()!!.toInt()
            if (res !in 1..52) {
                println("Invalid number of cards.")
                return
            } else if (res > cards.size) {
                println("The remaining cards are insufficient to meet the request.")
            } else {
                println(cards.subList(0, res).joinToString(" "))
                cards.removeAll(cards.subList(0, res))
            }
        } catch (e: Exception) {
            println("Invalid number of cards.")
        }
    }
}

class Game {
    companion object {
        const val projectTitle = "Indigo Card Game"
        var cardsOnTable = mutableListOf<String>()
        var exit = false
        var cards = Card().getCard().shuffled().toMutableList()
        val player = Player()
        val computer = Computer()
        var currentWinner = ""

        fun displayResult() {
            println("Score: Player ${player.score} - Computer ${computer.score}")
            println("Cards: Player ${player.cardsWon.size} - Computer ${computer.cardsWon.size}")
        }
    }

    private fun whoPlayFirst(): String {
        while (true) {
            println("Play first?")
            when (readLine()!!.lowercase()) {
                "yes" -> return "player"
                "no" -> return "computer"
            }
        }
    }

    init {
        println(projectTitle)
    }

    private fun makeFirstMove() {
        val removedCards = cards.subList(0, 4)
        println("Initial cards on the table: ${removedCards.joinToString(" ")}")
        cardsOnTable.addAll(removedCards)
        cards.removeAll(removedCards)
    }

    fun start() {
        val firstPlayer = whoPlayFirst()
        makeFirstMove()
        do {
            printInfo()
            if (firstPlayer == "player") {
                player.playCard()
                if (exit) return
                printInfo()
                computer.playCard()
            } else {
                computer.playCard()
                printInfo()
                player.playCard()
            }
            if (player.cardsWon.size + computer.cardsWon.size + cardsOnTable.size == 52) {
                break
            }
        } while (!exit)
        if (exit) return
        printInfo()
        calFinalScore(firstPlayer)
        displayResult()
        println("Game Over")
    }

    private fun printInfo() {
        if (cardsOnTable.isEmpty()) {
            println("No cards on the table")
        } else {
            println("${cardsOnTable.size} cards on the table, and the top card is ${cardsOnTable.last()}")
        }
    }

    private fun calFinalScore(firstPlayer: String) {
        if (currentWinner == "Player") {
            player.cardsWon.addAll(cardsOnTable)
            player.calScore()
        } else {
            computer.cardsWon.addAll(cardsOnTable)
            computer.calScore()
        }
        if (player.cardsWon.size > computer.cardsWon.size) {
            player.score += 3
        }
        if (player.cardsWon.size < computer.cardsWon.size) {
            computer.score += 3
        }
        if (player.cardsWon.size == computer.cardsWon.size) {
            if (firstPlayer == "Player") {
                player.score += 3
            } else computer.score += 3
        }

    }
}

open class Player {
    open val name = "Player"
    open var cardsInHand = mutableListOf<String>()
    var score = 0
    var cardsWon = mutableListOf<String>()
    open fun chooseACard(): Int {
        while (true) {
            try {
                println("Choose a card to play (1-${cardsInHand.size}):")
                val res = readLine()!!
                if (res == "exit") {
                    Game.exit = true
                    return -1
                }
                if (res.toInt() in 1..cardsInHand.size) {
                    return res.toInt()
                }
            } catch (e: Exception) {
                println("Choose a card to play (1-${cardsInHand.size}):")
            }
        }
    }

    fun removeCard(num: Int) {
        val cardRemoved = cardsInHand[num - 1]
        Game.cardsOnTable += cardRemoved
        cardsInHand.remove(cardRemoved)
    }

    open fun printCardInHand() {
        val output = mutableListOf<String>()
        for (i in cardsInHand.indices) {
            output.add("${i + 1})${cardsInHand[i]}")
        }
        println("Cards in hand: ${output.joinToString(" ")}")
    }

    fun isCardEnough() = cardsInHand.size == 0 && Game.cards.size > 0

    fun resetCardInHand() {
        cardsInHand = Game.cards.take(6).toMutableList()
        Game.cards.removeAll(cardsInHand)
    }

    open fun move() {
        if (isCardEnough()) resetCardInHand()
        printCardInHand()
        val num = chooseACard()
        if (num == -1) {
            Game.exit = true
            println("Game Over")
            return
        } else removeCard(num)
    }

    private fun isWon(): Boolean {
        return if (Game.cardsOnTable.isEmpty() || Game.cardsOnTable.size == 1) false
        else Game.cardsOnTable[Game.cardsOnTable.size - 2].last() == Game.cardsOnTable.last().last()
                || getRank(Game.cardsOnTable[Game.cardsOnTable.size - 2]) == getRank(Game.cardsOnTable.last())
    }

    fun calScore() {
        val cardsWithPoint = listOf("A", "10", "J", "Q", "K")
        for (card in Game.cardsOnTable) {
            val rank = getRank(card)
            if (cardsWithPoint.contains(rank)) {
                score++
            }
        }
    }

    fun getRank(card: String): String {
        return if (card.length == 2) {
            card.take(1)
        } else {
            card.take(2)
        }
    }

    open fun playCard() {
        if (Game.exit) return
        move()
        if (isWon()) {
            cardsWon.addAll(Game.cardsOnTable)
            calScore()
            Game.cardsOnTable.clear()
            Game.currentWinner = name
            println("$name wins cards")
            Game.displayResult()
        }
    }
}

class Computer : Player() {
    override val name = "Computer"
    override fun chooseACard(): Int {
        if (cardsInHand.size == 1) return 1
        if (getCandidate().size == 1) {
            return cardsInHand.indexOf(getCandidate().first()) + 1
        }
        if (getCandidateInSuit().size >= 2) {
            return pickCandidate(getCandidateInSuit())
        }
        if (getCandidateInRank().size >= 2) {
            return pickCandidate(getCandidateInRank())
        }
        if (getCandidate().size == 2) {
            return pickCandidate(getCandidate())
        }
        if (groupSuit(cardsInHand).size > 1) {
            return pickCandidate(groupSuit(cardsInHand))
        }
        if (groupRank(cardsInHand).size > 1) {
            return pickCandidate(groupRank(cardsInHand))
        }
        return pickCandidate(cardsInHand)
    }

    override fun printCardInHand() {
        println("Computer plays ${Game.cardsOnTable.last()}")
    }

    override fun move() {
        if (isCardEnough()) resetCardInHand()
        removeCard(chooseACard())
        printCardInHand()
    }

    override fun playCard() {
        if (isCardEnough()) resetCardInHand()
        println(cardsInHand.joinToString(" "))
        super.playCard()
    }

    private fun getCandidateInSuit(): MutableList<String> {
        if (Game.cardsOnTable.isEmpty()) return emptyList<String>().toMutableList()
        val candidateInSuit = mutableListOf<String>()
        val topCard = Game.cardsOnTable.last()
        for (card in cardsInHand) {
            if (card.last() == topCard.last()) {
                candidateInSuit.add(card)
            }
        }
        return candidateInSuit
    }

    private fun getCandidateInRank(): MutableList<String> {
        if (Game.cardsOnTable.isEmpty()) return emptyList<String>().toMutableList()
        val candidateInRank = mutableListOf<String>()
        val topCard = Game.cardsOnTable.last()
        for (card in cardsInHand) {
            if (getRank(card) == getRank(topCard)) {
                candidateInRank.add(card)
            }
        }
        return candidateInRank
    }

    private fun getCandidate(): MutableList<String> {
        return (getCandidateInRank() +
                getCandidateInSuit()).toMutableList()
    }

    private fun pickCandidate(candidateList: MutableList<String>?): Int {
        val candidate = candidateList?.randomOrNull()
        return cardsInHand.indexOf(candidate) + 1
    }

    private fun groupSuit(cardInHand: MutableList<String>): MutableList<String> {
        val map = mutableMapOf("♥" to 0, "♣" to 0, "♦" to 0, "♠" to 0)
        val outPut = mutableListOf<String>()
        for (card in cardInHand) {
            when (card.last().toString()) {
                "♥" -> map["♥"] = map["♥"]!! + 1
                "♣" -> map["♣"] = map["♣"]!! + 1
                "♠" -> map["♠"] = map["♠"]!! + 1
                else -> map["♦"] = map["♦"]!! + 1
            }
        }
        val newMap = map.filter { it.value >= 2 }.keys
        for (card in cardInHand) {
            if (card.last().toString() in newMap) {
                outPut.add(card)
            }
        }
        return outPut
    }

    private fun groupRank(cardInHand: MutableList<String>): MutableList<String> {
        val map = emptyMap<String, Int>().toMutableMap()
        val outPut = mutableListOf<String>()
        for (rank in Card.ranks) {
            map[rank] = 0
        }
        for (card in cardInHand) {
            map[getRank(card)] = map[getRank(card)]!! + 1
        }
        val newMap = map.filter { it.value > 1 }.keys
        for (card in cardInHand) {
            if (getRank(card) in newMap) {
                outPut.add(card)
            }
        }
        return outPut
    }
}

fun main() {
    val game = Game()
    game.start()
}



