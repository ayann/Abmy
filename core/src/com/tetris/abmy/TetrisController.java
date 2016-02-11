package com.tetris.abmy;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.Random;

import com.tetris.abmy.objects.Tetromino;

/**
 * Created by team AMBY | Insta project on 11-02-16.
 */
public class TetrisController {

    private final TetrisGame game;
    private String TAG = TetrisController.class.getName();

    private TetrominoManager tetrominoManager;
    public boolean tetrominoSpawned = false;
    private Tetromino tetromino;
    public GameState gameState;
    public Tetromino nextTetromino;
    private int levelRowsRemoved;
    private Skin skinLibGdx;
    private Window winOptions;
    public Stage windowStage;
    private Preferences prefs;
    private float winScaleFactor;


    public TetrisController(TetrisGame game, TetrominoManager tetrominoManager) {
        this.game = game;
        this.tetrominoManager = tetrominoManager;
        init();
    }

    private void init() {
        //TODO:

        prefs = Gdx.app.getPreferences("Nitris");

        gameState = GameState.Start;
        tetromino = new Tetromino(tetrominoManager, this);
        nextTetromino = new Tetromino(tetrominoManager, this);
        levelRowsRemoved = 0;

        windowStage = new Stage();

        skinLibGdx = new Skin(Gdx.files.internal("images/uiskin.json"), new TextureAtlas("images/uiskin.atlas"));

        winOptions = new Window("ABMY", skinLibGdx);

        float width = Gdx.graphics.getWidth();

        winScaleFactor = (1/300f) * width - 1/3f;

        winOptions.setScale(winScaleFactor, winScaleFactor);
        // winOptions.add(buildInfoText());
        winOptions.row();
        winOptions.add(buildOptions());
        winOptions.pack();
        winOptions.setColor(1, 1, 1, 0.8f);
        winOptions.setPosition(Gdx.graphics.getWidth() - winOptions.getWidth() * winScaleFactor - 50, 50);
        winOptions.setVisible(false);
        windowStage.addActor(winOptions);

        windowStage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //Gdx.app.log("x", String.valueOf(x));
                //Gdx.app.log("y", String.valueOf(y));
                if (x < winOptions.getX() || x > winOptions.getX() + winOptions.getWidth() * winScaleFactor ||
                        y < winOptions.getY() || y > winOptions.getY() + winOptions.getY() + winOptions.getHeight() * winScaleFactor) {
                    winOptions.setVisible(false);
                    Gdx.input.setInputProcessor(null);
                }
            }
        });
    }

    private Table buildOptions() {
        Table tbl = new Table();
        tbl.columnDefaults(0).padRight(10);
        tbl.columnDefaults(1).padRight(10);
        Label lblSound = new Label("Sound", skinLibGdx);

        final CheckBox chkSound = new CheckBox("", skinLibGdx);

        chkSound.setChecked(prefs.getBoolean("sound"));

        chkSound.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putBoolean("sound", chkSound.isChecked());
                prefs.flush();
            }
        });
        tbl.add(chkSound);
        tbl.add(lblSound);
        return tbl;
    }

    private Table buildInfoText() {
        Table tbl = new Table();
        Label lblText = new Label("Nitris by Pygmalion", skinLibGdx, "default-font", Color.WHITE);
        tbl.add(lblText);
        tbl.row();
        Label lblUrl = new Label("pygmalion.nitri.de", skinLibGdx);
        lblUrl.setColor(0, 0, 1, 1);
        lblUrl.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI("http://pygmalion.nitri.de");
            }
        });
        tbl.add(lblUrl);
        tbl.row();
        if (Gdx.app.getType() != Application.ApplicationType.Android && Gdx.app.getType() != Application.ApplicationType.iOS) {
            Label lblHelp = new Label("Use the arrow keys to move the piece ('up' to rotate right, 'down' to accelerate.)", skinLibGdx);
            lblHelp.setWrap(true);
            tbl.add(lblHelp).width(160f);
            tbl.row();
        }
        return tbl;
    }

    public void update() {

        switch (gameState) {
            case Start:
                checkMenuControls();
                break;
            case Running:
                checkRows();
                boolean moved = false;
                if (!tetrominoSpawned) spawnTetromino();
                if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                    tetromino.rotate();
                    moved = true;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                    tetromino.move(-1, 0);
                    moved = true;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                    tetromino.move(1, 0);
                    moved = true;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                    //tetromino.move(0, 1);
                    tetromino.fall(true);
                    moved = true;
                }
                if (Gdx.input.justTouched() && game.tetrisView != null) {
                    int gx = Gdx.input.getX();
                    int gy = Gdx.input.getY();
                    if (gx > game.tetrisView.leftArrowScreenX &&
                            gx < game.tetrisView.leftArrowScreenX + game.tetrisView.controlScreenWidth &&
                            gy > game.tetrisView.leftArrowScreenY &&
                            gy < game.tetrisView.leftArrowScreenY +  game.tetrisView.controlScreenWidth) {
                        tetromino.move(-1, 0);
                        moved = true;
                    } else if (gx > game.tetrisView.rightArrowScreenX &&
                            gx < game.tetrisView.rightArrowScreenX +  game.tetrisView.controlScreenWidth &&
                            gy > game.tetrisView.rightArrowScreenY &&
                            gy < game.tetrisView.rightArrowScreenY +  game.tetrisView.controlScreenWidth) {
                        tetromino.move(1, 0);
                        moved = true;
                    } else if (gx > game.tetrisView.rotateArrowScreenX &&
                            gx < game.tetrisView.rotateArrowScreenY +  game.tetrisView.controlScreenWidth &&
                            gy > game.tetrisView.rotateArrowScreenY &&
                            gy < game.tetrisView.rotateArrowScreenY +  game.tetrisView.controlScreenWidth) {
                        tetromino.rotate();
                        moved = true;
                    }
                }
                if ((Gdx.input.justTouched() || Gdx.input.isTouched()) && game.tetrisView != null) {
                    int gx = Gdx.input.getX();
                    int gy = Gdx.input.getY();
                    if (gx > game.tetrisView.downArrowScreenX &&
                            gx < game.tetrisView.downArrowScreenX +  game.tetrisView.controlScreenWidth &&
                            gy > game.tetrisView.downArrowScreenY &&
                            gy < game.tetrisView.downArrowScreenY + game.tetrisView.controlScreenWidth) {
                        tetromino.fall(true);
                        moved = true;
                    }
                }
                if (!moved)
                    tetromino.fall(false);
                tetrominoManager.update(tetromino);
                break;
            case GameOver:
                checkMenuControls();
                break;
        }
        windowStage.act();
    }

    private void checkMenuControls() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched()) {
            int gx = Gdx.input.getX();
            int gy = Gdx.input.getY();
            if (gx > game.tetrisView.playScreenX &&
                    gx < game.tetrisView.playScreenX + game.tetrisView.controlScreenWidth &&
                    gy > game.tetrisView.playScreenY &&
                    gy < game.tetrisView.playScreenY + game.tetrisView.controlScreenWidth) {
                tetrominoManager.reset();
                gameState = GameState.Running;
            }
            if (gx > game.tetrisView.optionsScreenX &&
                    gx < game.tetrisView.optionsScreenX + game.tetrisView.controlScreenWidth &&
                    gy > game.tetrisView.optionsScreenY &&
                    gy < game.tetrisView.optionsScreenY + game.tetrisView.controlScreenWidth) {
                winOptions.setVisible(true);

                Gdx.input.setInputProcessor(windowStage);
            }
        }
    }

    private void checkRows() {
        int rowsRemoved = 0;
        if (null == tetromino || System.currentTimeMillis() - tetromino.lastFallTime >= tetromino.delay) {
            boolean checkAgain = false;
            for (int i = 0; i < tetrominoManager.blocks.length; i++) {
                if (checkAgain) {
                    i -= 1;
                }
                boolean full = true;
                for (int j = 0; j < tetrominoManager.blocks[0].length; j++) {
                    if (!tetrominoManager.blocks[i][j]) {
                        full = false;
                    }
                }
                if (full) {
                    removeRow(i);
                    rowsRemoved++;
                    levelRowsRemoved++;
                    checkAgain = true;
                } else {
                    checkAgain = false;
                }
            }
        }

        if (rowsRemoved > 0) {
            play(Assets.instance.sounds.rowCleared);
        }

        switch (rowsRemoved) {
            case 1:
                tetrominoManager.score += 40 * (tetrominoManager.level + 1);
                break;
            case 2:
                tetrominoManager.score += 100 * (tetrominoManager.level + 1);
                break;
            case 3:
                tetrominoManager.score += 300 * (tetrominoManager.level + 1);
                break;
            case 4:
                tetrominoManager.score += 1200 * (tetrominoManager.level + 1);
                break;
        }
        if (levelRowsRemoved >= 10) {
            tetrominoManager.level++;
            levelRowsRemoved = 0;
            play(Assets.instance.sounds.levelUp);
        }
    }

    private void removeRow(int row) {
        for (int j = 0; j < tetrominoManager.blocks[0].length; j++) {
            tetrominoManager.blocks[row][j] = false;
            tetrominoManager.playfield[row][j] = 0;
        }
        for (int i = row; i > 0; i--) {
            for (int j = 0; j < tetrominoManager.blocks[0].length; j++) {
                if (tetrominoManager.blocks[i - 1][j]) {
                    tetrominoManager.blocks[i - 1][j] = false;
                    tetrominoManager.blocks[i][j] = true;
                    tetrominoManager.playfield[i][j] = tetrominoManager.playfield[i - 1][j];
                    tetrominoManager.playfield[i - 1][j] = 0;
                }
            }
        }
    }

    public void gameOver() {
        gameState = GameState.GameOver;
        play(Assets.instance.sounds.gameOver);
    }

    private void spawnTetromino() {

        if (System.currentTimeMillis() - tetromino.lastFallTime >= tetromino.delay) {
            if (nextTetromino.type == 0) {
                tetromino.init(randInt(1, 7));
            } else {
                tetromino.init(nextTetromino.type);
            }
            nextTetromino.init(randInt(1, 7));
            tetrominoSpawned = true;
        }
    }

    private void play(Sound sound) {
        if (prefs.getBoolean("sound", false)) {
            sound.play();
        }
    }

    private static Random rand = new Random();

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.


        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive

        return rand.nextInt((max - min) + 1) + min;
    }

    public void dispose() {
        tetrominoManager.dispose();
        if (skinLibGdx != null) {
            skinLibGdx.dispose();
        }
        if (windowStage != null) {
            windowStage.dispose();
        }
    }

    enum GameState {
        Intro, Start, Running, GameOver, Options
    }

}
