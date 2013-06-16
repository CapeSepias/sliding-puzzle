package com.sebnozzi.slidingpuzzle.ui

import com.sebnozzi.slidingpuzzle.model.GridSize
import com.sebnozzi.slidingpuzzle.model.Tile
import com.sebnozzi.slidingpuzzle.ui.javafx.JFXPuzzleView
import com.sebnozzi.slidingpuzzle.model.Game
import com.sebnozzi.slidingpuzzle.ui.javafx.JFXAppView

abstract class AppController() {

  private var game: Game = _

  private var appView: AppView = _
  private var puzzleView: PuzzleView = _

  private def hiddenTile = game.tiles.last

  def createAppView(): AppView

  def createPuzzleView(gridSize: GridSize): PuzzleView

  def start() {
    val initialGridSize = GridSize(3, 3)
    appView = createAppView()
    setupAppView(appView, initialGridSize)
    setupGame(initialGridSize)
    appView.show()
  }

  private def setupAppView(appView: AppView, gridSize: GridSize) = {
    appView.selectGridSize(gridSize)

    appView.onArrowKeyPressed { arrowKey =>
      arrowKeyPressed(arrowKey)
    }

    appView.onShuffleClicked {
      hiddenTile.makeHidden()
      game.shuffle()
      puzzleView.requestFocus()
    }

    appView.onResetClicked {
      game.reset()
      puzzleView.requestFocus()
    }

    appView.onNewSizeSelected { newSize =>
      setupGame(newSize)
    }

    appView
  }

  private def setupGame(gridSize: GridSize) {
    val _gridSize = gridSize
    game = new Game(gridSize.columns, gridSize.rows)

    puzzleView = createPuzzleView(gridSize)

    appView.setPuzzleView(puzzleView)

    game.tiles.zip(puzzleView.tileViews).foreach {
      case (modelTile: Tile, uiTile: TileView) => {
        bindUiAndModelTiles(uiTile, modelTile)
      }
    }

    game.onMovesCountChange {
      updateMovesCount()
    }

    game.onGameSolved {
      hiddenTile.makeVisible()
    }

    updateMovesCount()
  }

  private def updateMovesCount() {
    appView.setMovesCount(game.movesDone)
  }

  private def bindUiAndModelTiles(tileView: TileView, modelTile: Tile) {
    modelTile.onTileMoved {
      tileView.moveTileTo(modelTile.currentPosition, animate = true)
    }
    modelTile.onVisibilityChange { toVisible =>
      if (toVisible) {
        tileView.makeVisible(animate = modelTile.game.isSolved)
      } else {
        tileView.makeHidden()
      }
    }
    tileView.onMousePressed {
      modelTile.moveToEmptySlot()
    }
  }

  private def arrowKeyPressed(arrowKey: ArrowKey) {
    import com.sebnozzi.slidingpuzzle.ui.{ Up, Down, Left, Right }
    val tileToMove: Option[Tile] = arrowKey match {
      case Up => { hiddenTile.tileBelow }
      case Down => { hiddenTile.tileAbove }
      case Left => { hiddenTile.tileRight }
      case Right => { hiddenTile.tileLeft }
    }

    tileToMove.map { _.moveToEmptySlot() }
  }

}