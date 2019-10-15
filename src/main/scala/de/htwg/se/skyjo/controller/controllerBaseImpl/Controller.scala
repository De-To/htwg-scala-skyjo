package de.htwg.se.skyjo.controller.controllerBaseImpl

import com.google.inject.{Guice, Inject}
import de.htwg.se.skyjo.SkyjoModule
import de.htwg.se.skyjo.controller.ControllerInterface
import de.htwg.se.skyjo.model.deckComponent.deckBaseImpl.Deck
import de.htwg.se.skyjo.model.playerComponent
import de.htwg.se.skyjo.model.playerComponent.Player
import de.htwg.se.skyjo.util.{Observer, UndoManager}


class Controller @Inject() extends ControllerInterface with Observer {

  val injector = Guice.createInjector(new SkyjoModule)

  val undoManager = new UndoManager
  var deck = injector.getInstance(classOf[Deck])
  deck.shuffle()
  var players = scala.collection.mutable.ArrayBuffer.empty[Player]
  var turn = 0
  var shutdown = false

  def createPlayer(name: String): Unit = {
    players += playerComponent.Player(name, deck)
    players.last.hand.cards = deck.drawHand()
    add(players.last)
  }

  def moveCursor(dir: String, player: Player): Unit = {
    player.hand.move(dir)
    player.stillMyTurn = true
    notifyObservers
  }

  def setCursor(posX : Int, posY : Int, player: Player): Unit = {
    player.hand.posX = posX
    player.hand.posY = posY
    player.stillMyTurn = true
  }

  def uncoverCard(player: Player): Unit = {
    undoManager.doStep(new UncoverCommand(player))
    player.stillMyTurn = false
    player.canDrawCard = true
  }

  def undo(player: Player): Unit = {
    undoManager.undoStep
    player.stillMyTurn = true
    player.canDrawCard = true
  }

  def redo(player: Player): Unit = {
    undoManager.redoStep
    player.stillMyTurn = true
    player.canDrawCard = true
  }

  def tradeCard(player: Player): Unit = {
    undoManager.doStep(new TradeCommand(player))
    player.stillMyTurn = false
    player.canDrawCard = true
  }

  def drawCard(player: Player): Unit = {
    undoManager.doStep(new DrawCommand(player))
    player.stillMyTurn = true
    player.canDrawCard = false
  }

  def boardToString() : String = {
    val sb = new StringBuilder()
    if (deck.discardPile.nonEmpty) {
      sb.append("Ablagestapel: " + deck.discardPileTopCard() + "\n")
    }
    sb.append("Karten im Deck: " + deck.sumCardInDeck() + "\n")
    for (i <- 0 until players.length) {
      sb.append(players(i).playerString)
    }
    sb.append("-------------------------------------------------\n")
    sb.toString()
  }

  override def update: Boolean = {
    true
  }

}